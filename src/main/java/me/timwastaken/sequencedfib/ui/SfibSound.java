package me.timwastaken.sequencedfib.ui;

import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.List;
import java.util.Optional;

public enum SfibSound {
    OBTAIN_ITEM(Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f),
    SKIP_ITEM(Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1f, 1f),
    WITHER_SPAWN(Sound.ENTITY_WITHER_SPAWN, 1f, 1f),
    GAME_END(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f),
    INVENTORY_CLICK_FAIL(Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f),
    INVENTORY_CLICK_SUCCESS(Sound.BLOCK_SNOW_STEP, 1f, 2f),
    SCROLL_PAGE(Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f),
    PAUSE_GAME(Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f),
    COUNTDOWN(Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f),
    RESUME_GAME(Sound.ITEM_GOAT_HORN_SOUND_1, 1f, 2f);

    private final Sound minecraftSound;
    private final float volume;
    private final float pitch;

    SfibSound(Sound minecraftSound, float volume, float pitch) {
        this.minecraftSound = minecraftSound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playTo(OptionalOnlinePlayer player) {
        player.run(p -> p.playSound(p.getLocation(), this.minecraftSound, this.volume, this.pitch));
    }

    public void playTo(List<OptionalOnlinePlayer> players) {
        for (OptionalOnlinePlayer player : players) {
            this.playTo(player);
        }
    }

    public void playAt(Location location) {
        Optional.ofNullable(location.getWorld()).ifPresent(world -> world.playSound(location, this.minecraftSound, this.volume, this.pitch));
    }
}
