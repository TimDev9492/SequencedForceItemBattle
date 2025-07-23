package me.timwastaken.sequencedfib.intertories;

import me.timwastaken.intertoryapi.inventories.Intertory;
import me.timwastaken.intertoryapi.inventories.items.Items;
import me.timwastaken.intertoryapi.utils.IntertoryBuilder;
import me.timwastaken.intertoryapi.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GameConfigIntertory {
    public static Intertory getConfigIntertory(Player p) {
        Items.RangeSelect playTimeSelect = new Items.RangeSelect(
                Material.CLOCK,
                String.format(
                        "%s%sPlaytime",
                        ChatColor.GOLD,
                        ChatColor.BOLD
                ),
                "The amount of time the game will run for (in minutes)",
                60, 1, Integer.MAX_VALUE,   // initial value, min, max
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
                5, 0, Integer.MAX_VALUE,
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
                3, 1, 6,
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
                        .withItem(4, 0, new Items.ToggleState(
                                Material.WRITABLE_BOOK,
                                String.format(
                                        "%s%sSave config?",
                                        ChatColor.YELLOW,
                                        ChatColor.BOLD
                                ),
                                "Save the configuration for the next time",
                                false
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
                                    p.sendMessage(String.format(
                                            "%sStarting the game with these configured values:",
                                            ChatColor.GREEN
                                    ));
                                    p.sendMessage(String.format(
                                            "%sGame time: %s%d minutes",
                                            ChatColor.BLUE,
                                            ChatColor.GRAY,
                                            playTimeSelect.getValue()
                                    ));
                                    p.sendMessage(String.format(
                                            "%sNumber of skips: %s%d",
                                            ChatColor.BLUE,
                                            ChatColor.GRAY,
                                            numberOfSkipsSelect.getValue()
                                    ));
                                    p.sendMessage(String.format(
                                            "%sBackpack size: %s%d slots",
                                            ChatColor.BLUE,
                                            ChatColor.GRAY,
                                            backpackSpaceSelect.getValue() * 9
                                    ));
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

        return gameConfigIntertory;
    }
}
