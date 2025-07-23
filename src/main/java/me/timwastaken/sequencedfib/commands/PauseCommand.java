package me.timwastaken.sequencedfib.commands;

import me.timwastaken.sequencedfib.gamelogic.GameManager;
import me.timwastaken.sequencedfib.gamelogic.GameState;
import me.timwastaken.sequencedfib.ui.SfibMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PauseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (GameManager.getCurrentGame() == null) {
            sender.sendMessage(SfibMessages.gameStateError(GameState.READY));
            return false;
        }
        GameState currentState = GameManager.getCurrentGame().getGameState();
        if (currentState == GameState.RUNNING) {
            GameManager.getCurrentGame().pause();
        } else if (currentState == GameState.PAUSED) {
            GameManager.getCurrentGame().resume(3);
        } else {
            sender.sendMessage(SfibMessages.gameStateError(currentState));
            return false;
        }
        return true;
    }
}
