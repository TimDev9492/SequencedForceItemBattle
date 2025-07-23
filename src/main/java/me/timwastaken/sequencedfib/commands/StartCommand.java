package me.timwastaken.sequencedfib.commands;

import me.timwastaken.intertoryapi.inventories.Intertory;
import me.timwastaken.intertoryapi.inventories.items.Items;
import me.timwastaken.intertoryapi.utils.IntertoryBuilder;
import me.timwastaken.intertoryapi.utils.ItemBuilder;
import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import me.timwastaken.sequencedfib.gamelogic.GameManager;
import me.timwastaken.sequencedfib.gamelogic.GameState;
import me.timwastaken.sequencedfib.gamelogic.SfibGame;
import me.timwastaken.sequencedfib.gamelogic.SfibGameConfig;
import me.timwastaken.sequencedfib.gamelogic.matproviders.GroupedWeightedMaterialProvider;
import me.timwastaken.sequencedfib.ui.SfibMessages;
import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (GameManager.getCurrentGame() != null && GameManager.getCurrentGame().getGameState() != GameState.READY) {
            sender.sendMessage(SfibMessages.gameStateError(GameManager.getCurrentGame().getGameState()));
            return false;
        }

        if (!(sender instanceof Player p)) {
            sender.sendMessage(SfibMessages.commandCanOnlyBeUsedByPlayersChat());
            return false;
        }

        int minutes = SequencedForceItemBattle.getPluginConfig().getInt("default-settings.game-minutes");
        int jokers = SequencedForceItemBattle.getPluginConfig().getInt("default-settings.joker-amount");
        int backpackRows = SequencedForceItemBattle.getPluginConfig().getInt("default-settings.backpack-rows");

        try {
            // construct game config intertory
            Items.RangeSelect playTimeSelect = new Items.RangeSelect(
                    Material.CLOCK,
                    String.format(
                            "%s%sPlaytime",
                            ChatColor.GOLD,
                            ChatColor.BOLD
                    ),
                    "The amount of time the game will run for (in minutes)",
                    minutes, 1, Integer.MAX_VALUE,   // initial value, min, max
                    1,                          // small increment
                    10                          // large increment
            );
            Items.RangeSelect numberOfSkipsSelect = new Items.RangeSelect(
                    Material.BARRIER,
                    String.format(
                            "%s%sSkips",
                            ChatColor.GOLD,
                            ChatColor.BOLD
                    ),
                    "The number of skips every player has",
                    jokers, 0, Integer.MAX_VALUE,
                    1,
                    5,
                    true
            );
            Items.RangeSelect backpackSpaceSelect = new Items.RangeSelect(
                    Material.ENDER_CHEST,
                    String.format(
                            "%s%sBackpack",
                            ChatColor.GOLD,
                            ChatColor.BOLD
                    ),
                    "The amount of rows of backpack space " +
                            "(3x for normal chest, 6x for double chest, ...)",
                    backpackRows, 1, 6,
                    1,
                    2,
                    true
            );

            Intertory gameConfigIntertory = new IntertoryBuilder(9, 4)
                    .addSection(0, 0, new IntertoryBuilder(9, 3)
                            .withItem(2, 1, playTimeSelect)
                            .withItem(4, 1, numberOfSkipsSelect)
                            .withItem(6, 1, backpackSpaceSelect)
                            .withBackground(Material.GRAY_STAINED_GLASS_PANE)
                            .getSection()
                    )
                    .addSection(0, 3, new IntertoryBuilder(9, 1)
                            .withItem(0, 0, new Items.Button(
                                    new ItemBuilder(Material.TNT)
                                            .name(String.format(
                                                    "%s%sCancel",
                                                    ChatColor.RED,
                                                    ChatColor.BOLD
                                            ))
                                            .build(),
                                    // the action to be performed
                                    () -> {
                                        p.closeInventory();
                                        p.sendMessage(String.format(
                                                "%sGame configuration canceled.",
                                                ChatColor.RED
                                        ));
                                        // whether the action was successful
                                        return false;
                                    }
                            ))
                            .withItem(8, 0, new Items.Button(
                                    new ItemBuilder(Material.TIPPED_ARROW)
                                            .name(String.format(
                                                    "%s%sStart",
                                                    ChatColor.GREEN,
                                                    ChatColor.BOLD
                                            ))
                                            .build(),
                                    () -> {
                                        p.closeInventory();
                                        // start the game
                                        try {
                                            SfibGame newGame = new SfibGame(
                                                    Bukkit.getOnlinePlayers().stream().map(OptionalOnlinePlayer::of).toList(),
                                                    new SfibGameConfig(
                                                            playTimeSelect.getValue(),
                                                            numberOfSkipsSelect.getValue(),
                                                            backpackSpaceSelect.getValue()
                                                    ),
                                                    new GroupedWeightedMaterialProvider(new File(
                                                            SequencedForceItemBattle.getInstance().getDataFolder(),
                                                            "item_groups.json"
                                                    ))
                                            );
                                            SequencedForceItemBattle.getInstance().getResourceManager().registerEventListener(newGame);
                                            GameManager.startNewGame(newGame);
                                        } catch (IOException exception) {
                                            p.sendMessage(ChatColor.RED + "Failed load item groups!");
                                            SequencedForceItemBattle.getInstance().getLogger().severe(
                                                    exception.getMessage()
                                            );
                                        }
                                        return true;
                                    }
                            ))
                            .withBackground(Material.BLACK_STAINED_GLASS_PANE)
                            .getSection()
                    )
                    .getIntertory(String.format(
                            "%sConfigure game settings",
                            ChatColor.DARK_GRAY
                    ));

            gameConfigIntertory.openFor(p);

            return true;
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Bad formatting");
            return false;
        }
    }
}
