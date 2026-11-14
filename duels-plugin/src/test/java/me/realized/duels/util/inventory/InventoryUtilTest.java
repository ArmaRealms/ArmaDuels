package me.realized.duels.util.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InventoryUtilTest {

    @Before
    public void setUp() {
        MockBukkit.mock();
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void reconnectSnapshotRemovesResidualKitItemsFromEmptySlots() {
        final InventoryState state = new InventoryState();
        state.slots.put(0, new ItemStack(Material.BOW));
        state.slots.put(3, new ItemStack(Material.STONE));
        state.armor[0] = new ItemStack(Material.DIAMOND_BOOTS);

        final Map<String, Map<Integer, ItemStack>> snapshot = new HashMap<>();
        snapshot.put("INVENTORY", Map.of(3, new ItemStack(Material.DIAMOND)));
        snapshot.put("ARMOR", Map.of());

        InventoryUtil.fillFromMap(inventory(state), snapshot);

        assertEquals(1, state.clearCount);
        assertFalse(state.slots.containsKey(0));
        assertEquals(Material.DIAMOND, state.slots.get(3).getType());
        assertEquals(0, state.armorItems());
    }

    @Test
    public void completelyEmptySnapshotStillClearsInventory() {
        final InventoryState state = new InventoryState();
        state.slots.put(0, new ItemStack(Material.BOW));

        InventoryUtil.fillFromMap(inventory(state), Map.of("INVENTORY", Map.of(), "ARMOR", Map.of()));

        assertEquals(1, state.clearCount);
        assertTrue(state.slots.isEmpty());
    }

    @Test
    public void excludedInventorySnapshotPreservesOwnInventoryItems() {
        final InventoryState state = new InventoryState();
        state.slots.put(0, new ItemStack(Material.STONE));

        InventoryUtil.fillFromMap(inventory(state), Map.of());

        assertEquals(0, state.clearCount);
        assertEquals(Material.STONE, state.slots.get(0).getType());
    }

    @Test
    public void restoresArmorSlotsAfterClearingKitArmor() {
        final InventoryState state = new InventoryState();
        state.armor[0] = new ItemStack(Material.DIAMOND_BOOTS);

        InventoryUtil.fillFromMap(inventory(state), Map.of(
                "INVENTORY", Map.of(),
                "ARMOR", Map.of(4, new ItemStack(Material.IRON_BOOTS))));

        assertEquals(1, state.clearCount);
        assertEquals(Material.IRON_BOOTS, state.armor[0].getType());
        assertEquals(1, state.armorItems());
    }

    private static PlayerInventory inventory(final InventoryState state) {
        return (PlayerInventory) Proxy.newProxyInstance(PlayerInventory.class.getClassLoader(),
                new Class<?>[]{PlayerInventory.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "clear" -> {
                        state.clearCount++;
                        state.slots.clear();
                        state.armor = new ItemStack[4];
                        yield null;
                    }
                    case "setItem" -> {
                        state.slots.put((Integer) args[0], (ItemStack) args[1]);
                        yield null;
                    }
                    case "setArmorContents" -> {
                        state.armor = (ItemStack[]) args[0];
                        yield null;
                    }
                    default -> throw new AssertionError("Unexpected PlayerInventory method: " + method.getName());
                });
    }

    private static final class InventoryState {
        private final Map<Integer, ItemStack> slots = new HashMap<>();
        private ItemStack[] armor = new ItemStack[4];
        private int clearCount;

        private int armorItems() {
            int total = 0;
            for (final ItemStack item : armor) {
                if (item != null && item.getType() != Material.AIR) {
                    total++;
                }
            }
            return total;
        }
    }
}
