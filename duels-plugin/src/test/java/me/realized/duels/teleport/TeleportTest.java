package me.realized.duels.teleport;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    @Test
    public void rejectedTeleportClearsTemporaryMetadata() {
        final DispatchState state = new DispatchState();
        state.success = false;

        assertFalse(Teleport.teleportWithMetadata(plugin(), dispatchPlayer(state), new Location(null, 1, 70, 0)));
        assertTrue(state.metadataVisibleDuringTeleport);
        assertEquals(1, state.teleportCalls);
        assertFalse(state.hasMetadata);
    }

    @Test
    public void successfulTeleportClearsTemporaryMetadata() {
        final DispatchState state = new DispatchState();

        assertTrue(Teleport.teleportWithMetadata(plugin(), dispatchPlayer(state), new Location(null, 1, 70, 0)));
        assertEquals(1, state.teleportCalls);
        assertFalse(state.hasMetadata);
    }

    @Test
    public void exceptionalTeleportAlsoClearsTemporaryMetadata() {
        final DispatchState state = new DispatchState();
        state.throwsOnTeleport = true;

        try {
            Teleport.teleportWithMetadata(plugin(), dispatchPlayer(state), new Location(null, 1, 70, 0));
            fail("Expected teleport to fail");
        } catch (IllegalStateException expected) {
            assertEquals("teleport rejected by server", expected.getMessage());
        }

        assertFalse(state.hasMetadata);
    }

    private static Plugin plugin() {
        return (Plugin) Proxy.newProxyInstance(Plugin.class.getClassLoader(), new Class<?>[]{Plugin.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) return "DuelsTest";
                    if (method.getName().equals("equals")) return proxy == args[0];
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    throw new AssertionError("Unexpected Plugin method: " + method.getName());
                });
    }

    private static Player dispatchPlayer(final DispatchState state) {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setMetadata" -> {
                        state.hasMetadata = true;
                        yield null;
                    }
                    case "removeMetadata" -> {
                        state.hasMetadata = false;
                        yield null;
                    }
                    case "teleport" -> {
                        state.teleportCalls++;
                        state.metadataVisibleDuringTeleport = state.hasMetadata;
                        if (state.throwsOnTeleport) {
                            throw new IllegalStateException("teleport rejected by server");
                        }
                        yield state.success;
                    }
                    default -> throw new AssertionError("Unexpected Player method: " + method.getName());
                });
    }

    private static final class DispatchState {
        private boolean success = true;
        private boolean hasMetadata;
        private boolean metadataVisibleDuringTeleport;
        private boolean throwsOnTeleport;
        private int teleportCalls;
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
