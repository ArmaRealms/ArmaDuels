package me.realized.duels.util.compat;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Caches the GameProfile stored in EntityHuman instance to prevent Mojang server lookup.
 */
public final class Skulls {

    private Skulls() {
    }

    /**
     * Sets given player as the owner of the given skull using cached GameProfile information of the player.
     *
     * @param meta   SkullMeta of the skull to set owner
     * @param player Player to display on skull
     */
    public static void setProfile(@NotNull final SkullMeta meta, final Player player) {
        if (meta.hasOwner()) {
            meta.setOwningPlayer(player);
        }
    }
}
