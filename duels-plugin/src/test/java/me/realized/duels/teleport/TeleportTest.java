package me.realized.duels.teleport;

import org.bukkit.entity.Player;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TeleportTest {

    @Test
    public void deadPlayersWaitForRespawnWithoutDismounting() {
        final State state = new State();
        state.dead = true;
        state.riding = true;
        state.hasPassengers = true;

        assertFalse(Teleport.preparePlayer(player(state)));
        assertEquals(0, state.leaveAttempts);
        assertEquals(0, state.ejectAttempts);
    }

    @Test
    public void dismountsPlayerBeforeTeleport() {
        final State state = new State();
        state.riding = true;

        assertTrue(Teleport.preparePlayer(player(state)));
        assertFalse(state.riding);
        assertEquals(1, state.leaveAttempts);
        assertEquals(0, state.ejectAttempts);
    }

    @Test
    public void ejectsPassengersBeforeTeleport() {
        final State state = new State();
        state.hasPassengers = true;

        assertTrue(Teleport.preparePlayer(player(state)));
        assertFalse(state.hasPassengers);
        assertEquals(0, state.leaveAttempts);
        assertEquals(1, state.ejectAttempts);
    }

    @Test
    public void doesNotTeleportWhileVehicleStatePersists() {
        final State state = new State();
        state.riding = true;
        state.hasPassengers = true;
        state.canLeave = false;
        state.canEject = false;

        assertFalse(Teleport.preparePlayer(player(state)));
        assertEquals(1, state.leaveAttempts);
        assertEquals(1, state.ejectAttempts);
    }

    @Test
    public void leavesReadyPlayerUntouched() {
        final State state = new State();

        assertTrue(Teleport.preparePlayer(player(state)));
        assertEquals(0, state.leaveAttempts);
        assertEquals(0, state.ejectAttempts);
    }

    private static Player player(final State state) {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isDead" -> state.dead;
                    case "isInsideVehicle" -> state.riding;
                    case "leaveVehicle" -> {
                        state.leaveAttempts++;
                        if (state.canLeave) {
                            state.riding = false;
                        }
                        yield state.canLeave;
                    }
                    case "isEmpty" -> !state.hasPassengers;
                    case "eject" -> {
                        state.ejectAttempts++;
                        if (state.canEject) {
                            state.hasPassengers = false;
                        }
                        yield state.canEject;
                    }
                    default -> throw new AssertionError("Unexpected Player method: " + method.getName());
                });
    }

    private static final class State {
        private boolean dead;
        private boolean riding;
        private boolean hasPassengers;
        private boolean canLeave = true;
        private boolean canEject = true;
        private int leaveAttempts;
        private int ejectAttempts;
    }
}
