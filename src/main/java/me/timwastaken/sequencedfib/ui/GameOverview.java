package me.timwastaken.sequencedfib.ui;

import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import me.timwastaken.sequencedfib.gamelogic.SfibGame;
import me.timwastaken.sequencedfib.utils.ItemBuilder;
import me.timwastaken.sequencedfib.utils.Items;
import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameOverview implements Listener {
    private static final int ITEM_TASKS_PER_PAGE = 8;
    private static final int PLAYER_ROWS = 4;

    private static final int LAST_COLUMN = 8;
    private static final int LAST_ROW = 5;

    private static final String SCROLL_UP_ID = "scroll_up";
    private static final String SCROLL_DOWN_ID = "scroll_down";
    private static final String NEXT_PAGE_ID = "next_page";
    private static final String PREV_PAGE_ID = "prev_page";
    private static final String CANCEL_INTERACTION_ID = "cancel_interaction";

    private static final Material LEFT_ARROW = Material.WARPED_HANGING_SIGN;
    private static final Material RIGHT_ARROW = Material.WARPED_SIGN;
    private static final Material UP_ARROW = Material.CRIMSON_SIGN;
    private static final Material DOWN_ARROW = Material.CRIMSON_HANGING_SIGN;
    private static final ItemStack ACTION_LINE_STACK = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).amount(1).name(" ").id(CANCEL_INTERACTION_ID).build();
    private static final ItemStack BACKGROUND_STACK = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).amount(1).name(" ").id(CANCEL_INTERACTION_ID).build();
    private static final ItemStack COLLECTED_STACK = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).amount(1).name(SfibMessages.collectedItemPlaceholderName()).id(CANCEL_INTERACTION_ID).build();
    private static final ItemStack SKIPPED_STACK = new ItemBuilder(Material.BARRIER).amount(1).name(SfibMessages.skippedItemPlaceholderName()).id(CANCEL_INTERACTION_ID).build();

    private final SfibGame game;
    private final OptionalOnlinePlayer player;
    private final List<OptionalOnlinePlayer> playersSortedByScore;
    private final int totalPages;
    private int currentPage = 0;
    private final int totalPlayerRows;
    private int verticalScroll = 0;

    public GameOverview(SfibGame game, OptionalOnlinePlayer player) {
        this.game = game;
        this.player = player;
        this.totalPages = (int) Math.ceil((double) game.getMaterialSequence().size() / ITEM_TASKS_PER_PAGE);
        this.totalPlayerRows = game.getParticipants().size();

        // sort players by score
        playersSortedByScore = new ArrayList<>(game.getParticipants());
        playersSortedByScore.sort((p1, p2) -> game.getScoreFor(p2) - game.getScoreFor(p1));
    }

    private void logMessage(String message) {
        SequencedForceItemBattle.getInstance().getLogger().warning(String.format(
                "[GameOverview (%s)] %s",
                player.map(Player::getName).orElse("Unknown"),
                message
        ));
    }

    public void openOverview() {
        player.run(p -> p.openInventory(this.constructInventory()));
    }

    private Inventory constructInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, SfibMessages.overviewInventoryName());

        // add arrows
        int startRow = verticalScroll + 1;
        int endRow = Math.min(verticalScroll + PLAYER_ROWS, totalPlayerRows);
        String verticalInfo = String.format("%d-%d of %d", startRow, endRow, totalPlayerRows);
        String pageInfo = String.format("%d/%d", currentPage+1, totalPages);
        ItemStack upArrow = new ItemBuilder(UP_ARROW).amount(1).name(SfibMessages.upArrowText(verticalInfo)).id(SCROLL_UP_ID).build();
        ItemStack downArrow = new ItemBuilder(DOWN_ARROW).amount(1).name(SfibMessages.downArrowText(verticalInfo)).id(SCROLL_DOWN_ID).build();
        ItemStack leftArrow = new ItemBuilder(LEFT_ARROW).amount(1).name(SfibMessages.leftArrowText(pageInfo)).id(PREV_PAGE_ID).build();
        ItemStack rightArrow = new ItemBuilder(RIGHT_ARROW).amount(1).name(SfibMessages.rightArrowText(pageInfo)).id(NEXT_PAGE_ID).build();
        inv.setItem(getSlotIndex(0, 0), upArrow);
        inv.setItem(getSlotIndex(0, LAST_ROW), downArrow);
        inv.setItem(getSlotIndex(LAST_COLUMN - 1, LAST_ROW), leftArrow);
        inv.setItem(getSlotIndex(LAST_COLUMN, LAST_ROW), rightArrow);

        // add background
        for (int i = 1; i <= LAST_COLUMN - 2; i++) {
            inv.setItem(getSlotIndex(i, 5), ACTION_LINE_STACK);
        }

        // add item task row
        for (int x = 1; x <= LAST_COLUMN; x++) {
            int sequenceIndex = currentPage * ITEM_TASKS_PER_PAGE + (x-1);
            if (sequenceIndex >= game.getMaterialSequence().size()) break;
            Material mat = game.getMaterialSequence().get(sequenceIndex);
            inv.setItem(getSlotIndex(x, 0), new ItemBuilder(mat).amount(sequenceIndex+1).maxStackSize(99).id(CANCEL_INTERACTION_ID).build());
        }

        // add player rows
        for (int y = 1; y <= PLAYER_ROWS; y++) {
            int playerIndex = verticalScroll + (y-1);
            if (playerIndex >= playersSortedByScore.size()) break;
            OptionalOnlinePlayer player = playersSortedByScore.get(playerIndex);
            int score = game.getScoreFor(player);
            ItemStack playerHead = SequencedForceItemBattle.getInstance().getResourceManager().getPlayerHeadCached(
                    player.getPlayerUUID(),
                    SfibMessages.playerHeadName(player)
            ).clone();
            playerHead.setAmount(score);
            Items.setId(playerHead, CANCEL_INTERACTION_ID);
            inv.setItem(getSlotIndex(0, y), playerHead);

            for (int x = 1; x <= LAST_COLUMN; x++) {
                int sequenceIndex = currentPage * ITEM_TASKS_PER_PAGE + (x-1);
                boolean reached = game.getSequenceIndex(player) - 1 >= sequenceIndex;
                boolean skipped = game.getSkippedIndexesFor(player).contains(sequenceIndex);
                if (!reached) continue;
                if (skipped) {
                    inv.setItem(getSlotIndex(x, y), SKIPPED_STACK);
                    continue;
                }
                inv.setItem(getSlotIndex(x, y), COLLECTED_STACK);
            }
        }

        // fill rest with background stack
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) != null) continue;
            inv.setItem(i, BACKGROUND_STACK);
        }

        return inv;
    }

    private int getSlotIndex(int column, int row) {
        return 9 * row + column;
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event) {
        boolean prohibited = false;
        boolean successfulAction = true;
        boolean cancelEvent = true;
        ItemStack clicked = event.getCurrentItem();
        OptionalOnlinePlayer p = OptionalOnlinePlayer.of((Player) event.getWhoClicked());
        if (!p.equals(this.player)) return;
        if (clicked == null) return;
        if (Items.hasId(clicked, CANCEL_INTERACTION_ID)) {
            successfulAction = false;
            prohibited = true;
        } else if (Items.hasId(clicked, SCROLL_UP_ID)) {
            if (verticalScroll <= 0) {
                successfulAction = false;
            } else {
                verticalScroll--;
            }
        } else if (Items.hasId(clicked, SCROLL_DOWN_ID)) {
            if (verticalScroll + PLAYER_ROWS >= totalPlayerRows) {
                successfulAction = false;
            } else {
                verticalScroll++;
            }
        } else if (Items.hasId(clicked, NEXT_PAGE_ID)) {
            if (currentPage+1 >= totalPages) {
                successfulAction = false;
            } else {
                currentPage++;
            }
        } else if (Items.hasId(clicked, PREV_PAGE_ID)) {
            if (currentPage <= 0) {
                successfulAction = false;
            } else {
                currentPage--;
            }
        } else {
            cancelEvent = false;
            return;
        }
        if (successfulAction) {
            SfibSound.SCROLL_PAGE.playTo(p);
            openOverview();
        } else {
            if (prohibited) {
                SfibSound.INVENTORY_CLICK_FAIL.playTo(p);
            } else {
                SfibSound.INVENTORY_CLICK_SUCCESS.playTo(p);
            }
        }
        event.setCancelled(cancelEvent);
    }
}
