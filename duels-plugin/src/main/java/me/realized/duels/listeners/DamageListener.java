package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Overrides damage cancellation by other plugins for players in a duel.
 */
public class DamageListener implements Listener {

    private final ArenaManagerImpl arenaManager;

    public DamageListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();

        if (plugin.getConfiguration().isForceAllowCombat()) {
            plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(this, plugin), 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final Player damager = EventUtil.getDamager(event);

        if (damager == null) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);

        // Only activate when winner is undeclared
        if (arena == null || !arenaManager.isInMatch(damager) || arena.isEndGame()) {
            return;
        }

        KitImpl.Characteristic characteristic = arena.getMatch().getKit().getCharacteristics().stream().filter(
                c -> c == KitImpl.Characteristic.BOXING).findFirst().orElse(null);

        if(characteristic != null) {
            if(arena.getMatch().getHits(damager) >= 100) {
                arena.endMatch(damager.getUniqueId(), player.getUniqueId(), MatchEndEvent.Reason.OPPONENT_DEFEAT);
            }
            event.setDamage(0);
        }

        arena.getMatch().addDamageToPlayer(damager, event.getFinalDamage());

        if(!event.isCancelled()) return;

        event.setCancelled(false);
    }

}
