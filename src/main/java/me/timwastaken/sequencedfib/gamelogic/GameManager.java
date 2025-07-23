package me.timwastaken.sequencedfib.gamelogic;

import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;

public class GameManager {
    private static SfibGame currentGame = null;

    public static SfibGame getCurrentGame() {
        return currentGame;
    }

    public static void startNewGame(SfibGame game) {
        currentGame = game;
        game.startGame();
    }

    public static void openPlayerBackpack(OptionalOnlinePlayer player) {
        if (currentGame == null) return;
        currentGame.openBackpackFor(player);
    }
}
