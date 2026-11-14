package me.realized.duels.duel;

import me.realized.duels.duel.DuelManager.ArenaTeleportResult;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static org.junit.Assert.assertEquals;

public class DuelManagerTeleportTest {

    @Test
    public void firstTeleportFailureDoesNotMoveOpponent() {
        final Player first = playerAt(1);
        final Player second = playerAt(2);
        final List<String> attempts = new ArrayList<>();

        final ArenaTeleportResult result = DuelManager.teleportParticipants(first, second, at(10), at(20),
                logAttempts(first, attempts, false, true, true));

        assertEquals(ArenaTeleportResult.REJECTED, result);
        assertEquals(List.of("first:10"), attempts);
    }

    @Test
    public void secondTeleportFailureReturnsFirstPlayerToOriginalLocation() {
        final Player first = playerAt(1);
        final Player second = playerAt(2);
        final List<String> attempts = new ArrayList<>();

        final ArenaTeleportResult result = DuelManager.teleportParticipants(first, second, at(10), at(20),
                logAttempts(first, attempts, true, false, true));

        assertEquals(ArenaTeleportResult.REJECTED, result);
        assertEquals(List.of("first:10", "second:20", "first:1"), attempts);
    }

    @Test
    public void failedRollbackIsReportedForDeferredRecovery() {
        final Player first = playerAt(1);
        final Player second = playerAt(2);
        final List<String> attempts = new ArrayList<>();

        final ArenaTeleportResult result = DuelManager.teleportParticipants(first, second, at(10), at(20),
                logAttempts(first, attempts, true, false, false));

        assertEquals(ArenaTeleportResult.ROLLBACK_FAILED, result);
        assertEquals(List.of("first:10", "second:20", "first:1"), attempts);
    }

    @Test
    public void startsOnlyAfterBothTeleportsSucceed() {
        final Player first = playerAt(1);
        final Player second = playerAt(2);
        final List<String> attempts = new ArrayList<>();

        final ArenaTeleportResult result = DuelManager.teleportParticipants(first, second, at(10), at(20),
                logAttempts(first, attempts, true, true, true));

        assertEquals(ArenaTeleportResult.SUCCESS, result);
        assertEquals(List.of("first:10", "second:20"), attempts);
    }

    private static BiPredicate<Player, Location> logAttempts(final Player first, final List<String> attempts,
                                                              final boolean firstAllowed, final boolean secondAllowed,
                                                              final boolean rollbackAllowed) {
        return (player, location) -> {
            final boolean isFirst = player == first;
            attempts.add((isFirst ? "first:" : "second:") + location.getBlockX());
            if (!isFirst) {
                return secondAllowed;
            }
            return location.getBlockX() == 1 ? rollbackAllowed : firstAllowed;
        };
    }

    private static Location at(final int x) {
        return new Location(null, x, 70, 0);
    }

    private static Player playerAt(final int x) {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getLocation") && (args == null || args.length == 0)) {
                        return at(x);
                    }
                    throw new AssertionError("Unexpected Player method: " + method.getName());
                });
    }
}
