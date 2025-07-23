package me.timwastaken.sequencedfib.commands;

import me.timwastaken.sequencedfib.gamelogic.GameManager;
import me.timwastaken.sequencedfib.gamelogic.GameState;
import me.timwastaken.sequencedfib.ui.SfibMessages;
import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SequenceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            if (GameManager.getCurrentGame() == null) {
                p.sendMessage(SfibMessages.gameStateError(GameState.READY));
                return false;
            }
            for (String line : SfibMessages.formatPlayerSequenceChat(GameManager.getCurrentGame(), OptionalOnlinePlayer.of(p))) {
                p.sendMessage(line);
            }
            return true;
        }
        sender.sendMessage(SfibMessages.commandCanOnlyBeUsedByPlayersChat());
        return false;
    }
}
