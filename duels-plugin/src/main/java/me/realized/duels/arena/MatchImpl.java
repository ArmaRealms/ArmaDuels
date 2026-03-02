package me.realized.duels.arena;

import lombok.Getter;
import me.realized.duels.api.match.Match;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.queue.Queue;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MatchImpl implements Match {

    @Getter
    private final ArenaImpl arena;
    @Getter
    private final long start;
    @Getter
    private final KitImpl kit;
    private final Map<UUID, List<ItemStack>> items;
    @Getter
    private final int bet;
    @Getter
    private final boolean mcmmoSkills;
    @Getter
    private final Queue source;
    // Default value for players is false, which is set to true if player is killed in the match.
    private final Map<Player, PlayerStatus> players = new HashMap<>();
    @Getter
    public List<Item> droppedItems = new ArrayList<>();
    @Getter
    private boolean finished;

    MatchImpl(final ArenaImpl arena, final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final boolean mcmmoSkills, final Queue source) {
        this.arena = arena;
        this.start = System.currentTimeMillis();
        this.kit = kit;
        this.items = items;
        this.bet = bet;
        this.mcmmoSkills = mcmmoSkills;
        this.source = source;
    }

    Map<Player, PlayerStatus> getPlayerMap() {
        return players;
    }

    Set<Player> getAlivePlayers() {
        return players.entrySet().stream()
                .filter(entry -> !entry.getValue().isDead())
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void addDamageToPlayer(final Player player, final double damage) {
        final PlayerStatus status = players.get(player);
        status.addDamage(damage);
        status.addHit();
    }

    public int getHits(final Player player) {
        return players.get(player).getHits();
    }

    public Player getWinnerOfDamage() {
        return players.entrySet()
                .stream()
                .max(Comparator.comparingDouble(entry -> entry.getValue().getDamageCount()))
                .map(Entry::getKey)
                .orElse(null);
    }

    public Player getLooserOfDamage() {
        return players.entrySet()
                .stream()
                .min(Comparator.comparingDouble(entry -> entry.getValue().getDamageCount()))
                .map(Entry::getKey)
                .orElse(null);
    }

    public Set<Player> getAllPlayers() {
        return players.keySet();
    }

    public boolean isDead(final Player player) {
        return players.getOrDefault(player, new PlayerStatus(true)).isDead();
    }

    public boolean isFromQueue() {
        return source != null;
    }

    public boolean isOwnInventory() {
        return kit == null;
    }

    public List<ItemStack> getItems() {
        return items != null ? items.values().stream().flatMap(Collection::stream).toList() : List.of();
    }

    void setFinished() {
        finished = true;
    }

    public long getDurationInMillis() {
        return System.currentTimeMillis() - start;
    }

    @NotNull
    @Override
    public List<ItemStack> getItems(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");

        if (this.items == null) {
            return List.of();
        }

        final List<ItemStack> items = this.items.get(player.getUniqueId());
        return items != null ? items : List.of();
    }

    @NotNull
    @Override
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(getAlivePlayers());
    }

    @NotNull
    @Override
    public Set<Player> getStartingPlayers() {
        return Collections.unmodifiableSet(getAllPlayers());
    }

    @Getter
    public static class PlayerStatus {

        // Player is dead value
        public boolean isDead;
        // How much damage to your opponent.
        public double damageCount;
        public int hits;

        public PlayerStatus(final boolean isDead) {
            this.isDead = isDead;
        }

        public void addDamage(final double damage) {
            this.damageCount += damage;
        }

        public void addHit() {
            this.hits++;
        }

    }
}
