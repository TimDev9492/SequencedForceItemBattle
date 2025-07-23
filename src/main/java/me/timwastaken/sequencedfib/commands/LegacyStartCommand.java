package me.timwastaken.sequencedfib.commands;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;

public class LegacyStartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (GameManager.getCurrentGame() != null && GameManager.getCurrentGame().getGameState() != GameState.READY) {
            sender.sendMessage(SfibMessages.gameStateError(GameManager.getCurrentGame().getGameState()));
            return false;
        }
        int minutes = SequencedForceItemBattle.getPluginConfig().getInt("default-settings.game-minutes");
        int jokers = SequencedForceItemBattle.getPluginConfig().getInt("default-settings.joker-amount");
        int backpackRows = SequencedForceItemBattle.getPluginConfig().getInt("default-settings.backpack-rows");
        try {
            if (args.length >= 3) {
                backpackRows = Integer.parseInt(args[2]);
                if (backpackRows < 1 || backpackRows > 6) {
                    sender.sendMessage(ChatColor.RED + "Backpack rows must be a value between 1 and 6");
                    return false;
                }
            }
            if (args.length >= 2) {
                jokers = Integer.parseInt(args[1]);
            }
            if (args.length >= 1) {
                minutes = Integer.parseInt(args[0]);
            }
//            List<String> materialStrings = SequencedForceItemBattle.getPluginConfig().getStringList("materials");
//            List<Material> materials = new ArrayList<>();
//            for (String materialStr : materialStrings) {
//                Material mat;
//                try {
//                    mat = Material.valueOf(materialStr);
//                    materials.add(mat);
//                } catch (IllegalArgumentException e) {
//                    SequencedForceItemBattle.getInstance().getLogger().warning(String.format("Failed to load material '%s', skipping...", materialStr));
//                }
//            }
            SfibGame newGame = new SfibGame(
                    Bukkit.getOnlinePlayers().stream().map(OptionalOnlinePlayer::of).toList(),
                    new SfibGameConfig(minutes, jokers, backpackRows),
                    new GroupedWeightedMaterialProvider(new File(
                            SequencedForceItemBattle.getInstance().getDataFolder(),
                            "item_groups.json"
                    ))
            );
            SequencedForceItemBattle.getInstance().getResourceManager().registerEventListener(newGame);
            GameManager.startNewGame(newGame);
            return true;
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Bad formatting");
            return false;
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to load item groups!");
            SequencedForceItemBattle.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
    }
}
