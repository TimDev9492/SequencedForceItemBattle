package me.timwastaken.sequencedfib.commands;

import me.timwastaken.intertoryapi.inventories.Intertory;
import me.timwastaken.intertoryapi.inventories.items.Items;
import me.timwastaken.intertoryapi.utils.IntertoryBuilder;
import me.timwastaken.sequencedfib.gamelogic.GameManager;
import me.timwastaken.sequencedfib.gamelogic.GameState;
import me.timwastaken.sequencedfib.ui.SfibMessages;
import me.timwastaken.sequencedfib.utils.ItemBuilder;
import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class InfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player p) {
            if (GameManager.getCurrentGame() == null) {
                p.sendMessage(SfibMessages.gameStateError(GameState.READY));
                return false;
            }
            if (GameManager.getCurrentGame().getGameState() != GameState.RUNNING) {
                p.sendMessage(SfibMessages.gameStateError(GameManager.getCurrentGame().getGameState()));
                return false;
            }
            Material currentMaterialTask = GameManager.getCurrentGame().getCurrentMaterialTaskFor(OptionalOnlinePlayer.of(p));
            Intertory objectiveIntertory = new IntertoryBuilder(9, 1)
                    .withItem(4, 0, new Items.Placeholder(
                            new ItemBuilder(currentMaterialTask).build()
                    ))
                    .withBackground(Material.BLACK_STAINED_GLASS_PANE)
                    .getIntertory(SfibMessages.getCurrentObjective());
            p.spigot().sendMessage(SfibMessages.visitWiki(currentMaterialTask));
            objectiveIntertory.openFor(p);
            return true;
        }
        sender.sendMessage(SfibMessages.commandCanOnlyBeUsedByPlayersChat());
        return false;
    }
}
