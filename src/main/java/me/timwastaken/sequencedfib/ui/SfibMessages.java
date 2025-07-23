package me.timwastaken.sequencedfib.ui;

import me.timwastaken.sequencedfib.gamelogic.GameState;
import me.timwastaken.sequencedfib.gamelogic.SfibGame;
import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SfibMessages {
    private static final String PREFIX = String.format("[%sForceItemBattle%s] ", ChatColor.DARK_AQUA, ChatColor.RESET);

    public static String materialName(Material mat) {
        String lower = mat.toString().replaceAll("_", " ").toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public static String playerListName(OptionalOnlinePlayer player, SfibGame game) {
        return String.format(
                "%s %s[%s%s%s]",
                player.map(Player::getName).orElse("Unknown"),
                ChatColor.GRAY,
                ChatColor.GOLD,
                materialName(game.getCurrentMaterialTaskFor(player)),
                ChatColor.GRAY
        );
    }

    public static String commandCanOnlyBeUsedByPlayersChat() {
        return String.format(
                "%s%sThis command can only be used by players.",
                PREFIX,
                ChatColor.RED
        );
    }

    public static String gameStateError(GameState state) {
        String errorMsg = "Unexpected error";
        switch (state) {
            case RUNNING -> {
                errorMsg = "The game is already running.";
            }
            case PAUSED -> {
                errorMsg = "The game is currently paused.";
            }
            case FINISHED -> {
                errorMsg = "The game is already finished.";
            }
            case READY -> {
                errorMsg = "The game hasn't been started.";
            }
        }
        return String.format(
                "%s%s%s",
                PREFIX,
                ChatColor.RED,
                errorMsg
        );
    }

    public static TitleMeta newItemTitle(Material mat) {
        return new TitleMeta(
                String.format("%s%s%s", ChatColor.GOLD, ChatColor.BOLD, SfibMessages.materialName(mat)),
                String.format("%sis your new item", ChatColor.GRAY),
                0,
                60,
                0
        );
    }

    public static String newItemChat(Material mat) {
        return String.format(
                "%s%sYour new item is %s%s",
                PREFIX,
                ChatColor.YELLOW,
                ChatColor.GOLD,
                SfibMessages.materialName(mat)
        );
    }

    public static String playerCollectedMaterialBroadcastChat(OptionalOnlinePlayer p, Material mat) {
        return String.format(
                "%s%s%s%s%s collected %s%s",
                PREFIX,
                ChatColor.GRAY,
                ChatColor.BOLD,
                getPlayerName(p),
                ChatColor.GREEN,
                ChatColor.GOLD,
                SfibMessages.materialName(mat)
        );
    }

    public static TitleMeta gamePausedTitle() {
        return new TitleMeta(
                String.format("%s%sPaused", ChatColor.BLUE, ChatColor.BOLD),
                String.format("%sthe game", ChatColor.GRAY),
                0,
                60,
                0
        );
    }

    public static String gamePausedBroadcastChat() {
        return String.format(
                "%s%s%sPaused the game.",
                PREFIX,
                ChatColor.BLUE,
                ChatColor.BOLD
        );
    }

    public static TitleMeta resumingGameTitle(int seconds) {
        return new TitleMeta(
                String.format("%s%sResuming", ChatColor.BLUE, ChatColor.BOLD),
                String.format("%sthe game in %d seconds...", ChatColor.GRAY, seconds),
                0,
                21,
                0
        );
    }

    public static String resumingGameBroadcastChat(int seconds) {
        return String.format(
                "%s%sResuming in %s%d %sseconds.",
                PREFIX,
                ChatColor.GRAY,
                ChatColor.BLUE,
                seconds,
                ChatColor.GRAY
        );
    }

    public static String resumeGameBroadcastChat() {
        return String.format(
                "%s%s%sResuming the game!",
                PREFIX,
                ChatColor.BLUE,
                ChatColor.BOLD
        );
    }

    public static TitleMeta gameEndedTitle(String reason) {
        return new TitleMeta(
                String.format("%s%s%s", ChatColor.GOLD, ChatColor.BOLD, reason),
                String.format("%sThe game ended", ChatColor.YELLOW),
                10,
                80,
                10
        );
    }

    public static String gameEndedBroadcastChat(String reason) {
        return String.format(
                "%s%s%sThe game is over. %s",
                PREFIX,
                ChatColor.DARK_PURPLE,
                ChatColor.BOLD,
                reason
        );
    }

    public static String backpackName(OptionalOnlinePlayer player) {
        return String.format(
                "%s%s's Backpack",
                ChatColor.DARK_GRAY,
                getPlayerName(player)
        );
    }

    private static String formatTimeRemaining(long millisRemaining) {
        Duration duration = Duration.ofMillis(millisRemaining);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long deciSeconds = duration.toMillisPart() / 100;

        if (millisRemaining < 60_000) return String.format("%d.%01ds", seconds, deciSeconds);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String formatGameStatePart(String part) {
        return String.format(
                " %s[%s%s%s%s%s]%s",
                ChatColor.WHITE,
                ChatColor.AQUA,
                ChatColor.BOLD,
                part,
                ChatColor.RESET,
                ChatColor.WHITE,
                ChatColor.RESET
        );
    }

    public static String getHotbarTitle(SfibGame game, OptionalOnlinePlayer participant) {
        if (!game.getParticipants().contains(participant)) return "ERROR: You're not participating!";
        GameState state = game.getGameState();
        String gameStateInfo = state == GameState.RUNNING ? "" : formatGameStatePart(game.getGameState().toString());
        return String.format(
                "%s[ %s%sTime:%s %s%s%s %s]  [ %s%sScore:%s %s%d %s]  [ %s%sSkips used:%s %s%d/%d %s]",
                ChatColor.DARK_GRAY,
                ChatColor.LIGHT_PURPLE,
                ChatColor.BOLD,
                ChatColor.RESET,
                ChatColor.GRAY,
                formatTimeRemaining(game.getRemainingMillis()),
                gameStateInfo,
                ChatColor.DARK_GRAY,
                ChatColor.GREEN,
                ChatColor.BOLD,
                ChatColor.RESET,
                ChatColor.GRAY,
                game.getScoreFor(participant),
                ChatColor.DARK_GRAY,
                ChatColor.BLUE,
                ChatColor.BOLD,
                ChatColor.RESET,
                ChatColor.GRAY,
                game.getSkippedIndexesFor(participant).size(),
                game.getTotalSkips(),
                ChatColor.DARK_GRAY
        );
    }

    private static String getPlayerName(OptionalOnlinePlayer player) {
        return player.map(Player::getName).orElse("Unknown");
    }

    public static String playerHeadName(OptionalOnlinePlayer player) {
        return String.format("%s%s", ChatColor.GREEN, getPlayerName(player));
    }

    public static void sendPlayerTitle(OptionalOnlinePlayer player, TitleMeta titleMeta) {
        player.run(p -> p.sendTitle(
                titleMeta.title(),
                titleMeta.subtitle(),
                titleMeta.fadeIn(),
                titleMeta.stay(),
                titleMeta.fadeOut()
        ));
    }

    public static String collectedItemPlaceholderName() {
        return String.format("%s✔ Collected", ChatColor.GREEN);
    }

    public static String skippedItemPlaceholderName() {
        return String.format("%s✘ Skipped", ChatColor.RED);
    }

    public static String overviewInventoryName() {
        return "Game Overview";
    }

    private static String genericArrowText(String text, String info) {
        return String.format(
                "%s%s %s[ %s%s%s ]",
                ChatColor.GOLD,
                text,
                ChatColor.DARK_GRAY,
                ChatColor.GRAY,
                info,
                ChatColor.DARK_GRAY
        );
    }

    public static String upArrowText(String info) {
        return genericArrowText("Scroll up", info);
    }

    public static String downArrowText(String info) {
        return genericArrowText("Scroll down", info);
    }

    public static String leftArrowText(String info) {
        return genericArrowText("Previous page", info);
    }

    public static String rightArrowText(String info) {
        return genericArrowText("Next page", info);
    }

    public static String skipItemName() {
        return String.format("%s%sSkip", ChatColor.RED, ChatColor.BOLD);
    }

    public static String doubleClickToSkipChat() {
        return String.format("%s%sDouble click to confirm skip!", PREFIX, ChatColor.YELLOW);
    }

    public static List<String> formatPlayerSequenceChat(SfibGame game, OptionalOnlinePlayer player) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("%s%sDiscovered item sequence:", PREFIX, ChatColor.YELLOW));
        if (!game.getParticipants().contains(player)) {
            for (int i = 0; i < game.getMaterialSequence().size(); i++) {
                Material material = game.getMaterialSequence().get(i);
                lines.add(String.format("%s%d. %s", ChatColor.GRAY, i+1, SfibMessages.materialName(material)));
            }
            return lines;
        }
        for (int i = 0; i < game.getMaterialSequence().size(); i++) {
            Material material = game.getMaterialSequence().get(i);
            boolean hasCollected = game.getSequenceIndex(player) > i;
            boolean hasSkipped = game.getSkippedIndexesFor(player).contains(i);
            ChatColor color;
            String indicator;
            if (!hasCollected) {
                color = ChatColor.GRAY;
                indicator = "✘";
            } else if (hasSkipped) {
                color = ChatColor.YELLOW;
                indicator = "→";
            } else {
                color = ChatColor.GREEN;
                indicator = "✔";
            }
            lines.add(String.format(
                    "%s%d. %s[%s%s%s] %s%s",
                    ChatColor.GRAY,
                    i+1,
                    ChatColor.DARK_GRAY,
                    color,
                    indicator,
                    ChatColor.DARK_GRAY,
                    color,
                    SfibMessages.materialName(material)
            ));
        }
        return lines;
    }
}
