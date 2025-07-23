package me.timwastaken.sequencedfib.gamelogic;

import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import me.timwastaken.sequencedfib.gamelogic.matproviders.SfibMaterialProvider;
import me.timwastaken.sequencedfib.ui.GameOverview;
import me.timwastaken.sequencedfib.ui.SfibMessages;
import me.timwastaken.sequencedfib.ui.SfibSound;
import me.timwastaken.sequencedfib.utils.ItemBuilder;
import me.timwastaken.sequencedfib.utils.Items;
import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SfibGame implements Listener {
    private static final String SKIP_ITEM_ID = "skip_item";
    private static final Material JOKER_MATERIAL = Material.valueOf(SequencedForceItemBattle.getPluginConfig().getString("joker-material"));
    private static final long SKIP_DELAY_MS = (long) (SequencedForceItemBattle.getPluginConfig().getDouble("skip-confirm-delay") * 1000d);
    private static final String TIMES_UP = "Time's up!";
    private static final String ALL_ITEMS_COLLECTED = "All items collected!";
    private static final String GENERIC = "Game ended!";

    private final HashMap<OptionalOnlinePlayer, Long> lastSkipActions;
    private final List<OptionalOnlinePlayer> participants;
    private final SfibMaterialProvider materialProvider;
    private final HashMap<OptionalOnlinePlayer, Inventory> backpackInventories;
    private final List<Material> discoveredMaterialSequence;
    private final HashMap<OptionalOnlinePlayer, Integer> playerSequencePositions;
    private final HashMap<OptionalOnlinePlayer, List<Integer>> skippedIndexes;
    private final int totalSkips;

    private long elapsedGameMillis = 0L;
    private final long gameMillisLength;

    private GameState gameState;

    public SfibGame(List<OptionalOnlinePlayer> participants, SfibGameConfig gameOptions, SfibMaterialProvider materialProvider) {
        this.lastSkipActions = new HashMap<>();

        this.totalSkips = gameOptions.jokerAmount();

        this.participants = participants;
        this.materialProvider = materialProvider;

        // setup player backpacks
        this.backpackInventories = new HashMap<>();
        this.setupBackpackInventoriesFor(participants, gameOptions.backpackRows() * 9);

        this.discoveredMaterialSequence = new ArrayList<>();
        this.playerSequencePositions = new HashMap<>();

        this.skippedIndexes = new HashMap<>();
        participants.forEach(p -> {
            this.skippedIndexes.put(p, new ArrayList<>());
        });

        this.gameMillisLength = gameOptions.gameMinutes() * 60_000L;

        this.gameState = GameState.READY;
    }

    private boolean discoverNextMaterial() {
        if (!this.materialProvider.hasNext()) return false;
        this.discoveredMaterialSequence.add(this.materialProvider.next());
        return true;
    }

    private void setupBackpackInventoriesFor(List<OptionalOnlinePlayer> players, int slots) {
        for (OptionalOnlinePlayer player : players) {
            Inventory playerBackpackInventory = Bukkit.createInventory(player.get(), slots, SfibMessages.backpackName(player));
            this.backpackInventories.put(player, playerBackpackInventory);
        }
    }

    public void openBackpackFor(OptionalOnlinePlayer player) {
        Inventory backpackInv = this.backpackInventories.get(player);
        if (backpackInv == null) throw new IllegalStateException("Player has no backpack!");
        player.run(p -> p.openInventory(this.backpackInventories.get(player)));
    }

    public void startGame() {
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.KEEP_INVENTORY, true));
        ItemStack skipItems = new ItemBuilder(JOKER_MATERIAL).amount(this.totalSkips).name(SfibMessages.skipItemName()).id(SKIP_ITEM_ID).build();
        participants.forEach(participant -> {
            participant.run(p -> {
                p.setHealth(20d);
                p.setSaturation(5f);
                p.setFoodLevel(20);
                p.setExhaustion(0f);
                p.setLevel(0);
                p.setExp(0f);
                p.setTotalExperience(0);
                p.setFireTicks(0);
                p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
                p.setRemainingAir(p.getMaximumAir());
                p.setGameMode(GameMode.SURVIVAL);
                p.getInventory().clear();
                p.getInventory().addItem(skipItems);
                p.getInventory().setArmorContents(null);
            });
        });

        this.gameState = GameState.RUNNING;
        this.startGameLoop();
    }

    private void startGameLoop() {
        final SfibGame currentGame = this;

        for (OptionalOnlinePlayer player : participants) {
            advanceItemFor(player);
        }

        SequencedForceItemBattle.getInstance().getResourceManager().registerTask(new BukkitRunnable() {
            private long lastMillis = -1;

            @Override
            public void run() {
                if (gameState == GameState.FINISHED) {
                    this.cancel();
                    return;
                }

                for (OptionalOnlinePlayer player : participants) {
                    if (playerCollectedItemInSequence(player)) {
                        advanceItemFor(player);
                    }
                    player.run(p -> p.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent(SfibMessages.getHotbarTitle(currentGame, player))
                    ));
                }

                long elapsedMillis = 0;
                if (lastMillis == -1) {
                    lastMillis = System.currentTimeMillis();
                } else {
                    long currentMillis = System.currentTimeMillis();
                    elapsedMillis = currentMillis - lastMillis;
                    lastMillis = currentMillis;
                }

                if (gameState == GameState.PAUSED) return;

                elapsedGameMillis += elapsedMillis;

                if (elapsedGameMillis >= gameMillisLength) {
                    endGame(TIMES_UP);
                }
            }
        }.runTaskTimer(SequencedForceItemBattle.getInstance(), 0L, 1L).getTaskId());
    }

    private void advanceItemFor(OptionalOnlinePlayer player) {
        this.advanceItemFor(player, false);
    }

    private void advanceItemFor(OptionalOnlinePlayer player, boolean skipped) {
        Integer oldSequenceIndex = this.playerSequencePositions.getOrDefault(player, -1);

        if (oldSequenceIndex >= 0) {
            SfibSound sound = skipped ? SfibSound.SKIP_ITEM : SfibSound.OBTAIN_ITEM;
            sound.playTo(player);
            Material collectedMat = this.discoveredMaterialSequence.get(oldSequenceIndex);

            this.participants.forEach(participant -> participant.run(
                    p -> p.sendMessage(SfibMessages.playerCollectedMaterialBroadcastChat(player, collectedMat))
            ));
        }

        int sequenceIndex = oldSequenceIndex + 1;
        this.playerSequencePositions.put(player, sequenceIndex);
        while (sequenceIndex >= this.discoveredMaterialSequence.size()) {
            if (!this.discoverNextMaterial()) {
                this.endGame(ALL_ITEMS_COLLECTED);
                return;
            }
        }

        Material newItemTask = this.discoveredMaterialSequence.get(sequenceIndex);
        player.run(p -> p.sendMessage(SfibMessages.newItemChat(newItemTask)));
        SfibMessages.sendPlayerTitle(player, SfibMessages.newItemTitle(newItemTask));
        player.run(p -> p.setPlayerListName(SfibMessages.playerListName(player, this)));
    }

    public void skipItemFor(OptionalOnlinePlayer player) {
        if (!this.participants.contains(player)) return;
        List<Integer> skippedItems = this.skippedIndexes.getOrDefault(player, new ArrayList<>());
        skippedItems.add(this.playerSequencePositions.get(player));
        this.advanceItemFor(player, true);
    }

    private boolean playerCollectedItemInSequence(OptionalOnlinePlayer player) {
        Material objective = this.getCurrentMaterialTaskFor(player);
        boolean inPlayerInv = player.map(HumanEntity::getInventory).map(inv -> inv.contains(objective)).orElse(false);
        boolean inBackpackInv = Optional.ofNullable(this.backpackInventories.get(player)).map(inv -> inv.contains(objective)).orElse(false);
        return inPlayerInv || inBackpackInv;
    }

    public Material getCurrentMaterialTaskFor(OptionalOnlinePlayer player) {
        Integer playerSequenceIndex = playerSequencePositions.get(player);
        if (playerSequenceIndex == null) throw new IllegalStateException("Player has no next item in sequence!");
        return this.discoveredMaterialSequence.get(playerSequenceIndex);
    }

    private void endGame() {
        this.endGame(GENERIC);
    }

    private void endGame(String reason) {
        this.gameState = GameState.FINISHED;
        SfibSound.GAME_END.playTo(this.participants);
        this.participants.forEach(participant -> {
            SfibMessages.sendPlayerTitle(
                    participant,
                    SfibMessages.gameEndedTitle(reason)
            );
            participant.run(p -> p.sendMessage(SfibMessages.gameEndedBroadcastChat(reason)));
        });
        final SfibGame game = this;
        SequencedForceItemBattle.getInstance().getResourceManager().registerTask(new BukkitRunnable() {
            @Override
            public void run() {
                game.getParticipants().forEach(p -> {
                    GameOverview overview = new GameOverview(game, p);
                    SequencedForceItemBattle.getInstance().getResourceManager().registerEventListener(overview);
                    overview.openOverview();
                    SfibSound.WITHER_SPAWN.playTo(p);
                });
            }
        }.runTaskLater(SequencedForceItemBattle.getInstance(), 100L).getTaskId());
    }

    public void cleanup() {
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.KEEP_INVENTORY, false));
    }

    public GameState getGameState() {
        return gameState;
    }

    public List<Material> getMaterialSequence() {
        return this.discoveredMaterialSequence;
    }

    public List<Integer> getSkippedIndexesFor(OptionalOnlinePlayer p) {
        return this.skippedIndexes.getOrDefault(p, new ArrayList<>());
    }

    public int getSequenceIndex(OptionalOnlinePlayer p) {
        return this.playerSequencePositions.getOrDefault(p, -1);
    }

    public List<OptionalOnlinePlayer> getParticipants() {
        return participants;
    }

    public int getScoreFor(OptionalOnlinePlayer player) {
        if (!this.participants.contains(player)) return -1;
        return this.playerSequencePositions.get(player) - this.skippedIndexes.get(player).size();
    }

    public long getRemainingMillis() {
        return Math.max(gameMillisLength - elapsedGameMillis, 0);
    }

    public int getTotalSkips() {
        return totalSkips;
    }

    public void pause() {
        this.gameState = GameState.PAUSED;
        this.participants.forEach(participant -> {
            SfibMessages.sendPlayerTitle(
                    participant,
                    SfibMessages.gamePausedTitle()
            );
            participant.run(p -> p.sendMessage(SfibMessages.gamePausedBroadcastChat()));
            SfibSound.PAUSE_GAME.playTo(participant);
            participant.run(p -> p.setInvulnerable(true));
        });
    }

    public void resume(int delaySeconds) {
        final SfibGame game = this;

        SequencedForceItemBattle.getInstance().getResourceManager().registerTask(new BukkitRunnable() {
            int remainingSeconds = delaySeconds;

            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    // resume
                    game.gameState = GameState.RUNNING;
                    game.getParticipants().forEach(participant -> {
                        participant.run(p -> p.sendMessage(SfibMessages.resumeGameBroadcastChat()));
                        SfibSound.RESUME_GAME.playTo(participant);
                        participant.run(p -> p.setInvulnerable(false));
                    });
                    this.cancel();
                    return;
                }

                game.getParticipants().forEach(participant -> {
                    SfibMessages.sendPlayerTitle(
                            participant,
                            SfibMessages.resumingGameTitle(remainingSeconds)
                    );
                    participant.run(p -> p.sendMessage(SfibMessages.resumingGameBroadcastChat(remainingSeconds)));
                    SfibSound.COUNTDOWN.playTo(participant);
                });

                remainingSeconds--;
            }
        }.runTaskTimer(SequencedForceItemBattle.getInstance(), 0L, 20L).getTaskId());
    }

    private ItemStack getSkipStack(OptionalOnlinePlayer player) {
        Optional<Inventory> inv = player.map(HumanEntity::getInventory);
        if (inv.isEmpty()) return null;
        for (ItemStack stack : inv.get()) {
            if (Items.hasId(stack, SKIP_ITEM_ID)) return stack;
        }
        return null;
    }

    @EventHandler
    public void onPlayerUseSkip(PlayerInteractEvent event) {
        if (this.gameState != GameState.RUNNING) return;
        if (event.getItem() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!Items.hasId(event.getItem(), SKIP_ITEM_ID)) return;
        // player used a skip
        event.setCancelled(true);
        OptionalOnlinePlayer player = OptionalOnlinePlayer.of(event.getPlayer());
        long currentTime = System.currentTimeMillis();
        long delay = Optional.ofNullable(lastSkipActions.put(player, currentTime))
                .map(lastSkipAction -> currentTime - lastSkipAction)
                .orElse(SKIP_DELAY_MS + 1);
        if (delay > SKIP_DELAY_MS) {
            player.run(p -> p.sendMessage(SfibMessages.doubleClickToSkipChat()));
            return;
        };
        ItemStack skipStack = this.getSkipStack(player);
        if (skipStack == null) return;
        skipStack.setAmount(skipStack.getAmount()-1);
        this.skipItemFor(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.gameState != GameState.PAUSED) return;
        if (!this.participants.contains(OptionalOnlinePlayer.of(event.getPlayer()))) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.gameState != GameState.PAUSED) return;
        if (!this.participants.contains(OptionalOnlinePlayer.of(event.getPlayer()))) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.gameState != GameState.PAUSED) return;
        if (!this.participants.contains(OptionalOnlinePlayer.of(event.getPlayer()))) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        OptionalOnlinePlayer player = OptionalOnlinePlayer.of(event.getPlayer());
        if (!this.participants.contains(player)) return;
        player.run(p -> p.setPlayerListName(SfibMessages.playerListName(player, this)));
    }
}
