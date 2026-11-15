package me.realized.duels.setting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.util.gui.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServerMock;
import org.bukkit.event.inventory.InventoryType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ArenaSettingsTest {

    private TestDuelsPlugin plugin;
    private ServerMock server;
    private PlayerMock player;
    private ArenaImpl arena;
    private Settings settings;

    @Before
    public void setUp() {
        server = MockBukkit.mock(new CraftServerMock());
        final var world = server.addSimpleWorld("arena");
        plugin = MockBukkit.loadSimple(TestDuelsPlugin.class);
        player = server.addPlayer("Challenger");
        settings = plugin.getSettingManager().getSafely(player);
        arena = new ArenaImpl(plugin, "test");
        arena.getPositions().put(1, new Location(world, 0, 65, 0));
        arena.getPositions().put(2, new Location(world, 1, 65, 0));
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void arenaClickWithoutTargetRejectsStaleSelection() {
        openSelector();
        arena.onClick(player);
        assertUnavailable();
    }

    @Test
    public void arenaClickAfterOpponentDisconnectsRejectsStaleSelection() {
        final PlayerMock target = server.addPlayer("Opponent");
        settings.setTarget(target);
        openSelector();
        assertTrue(target.disconnect());
        assertNull(Bukkit.getPlayer(target.getUniqueId()));

        arena.onClick(player);

        assertUnavailable();
    }

    @Test
    public void arenaClickWithOnlineTargetOpensSettingsWithOpponentAndArena() {
        final PlayerMock target = server.addPlayer("Opponent");
        settings.setTarget(target);
        openSelector();

        arena.onClick(player);

        assertOpened(target);
    }

    @Test
    public void refreshingSettingsAfterDisconnectClosesMenu() {
        final PlayerMock target = server.addPlayer("Opponent");
        settings.setTarget(target);
        arena.onClick(player);
        assertOpened(target);
        assertTrue(target.disconnect());

        settings.updateGui(player);

        assertUnavailable();
    }

    private void openSelector() {
        player.openInventory(Bukkit.createInventory(null, 18, "Arena selector"));
    }

    private void assertUnavailable() {
        assertNull(settings.getTarget());
        assertNull(settings.getArena());
        assertEquals(InventoryType.CRAFTING, player.getOpenInventory().getType());
        assertEquals(plugin.getLang().getMessage("ERROR.player.no-longer-online"), player.nextMessage());
    }

    private void assertOpened(final PlayerMock target) {
        assertEquals(target.getUniqueId(), settings.getTarget());
        assertSame(arena, settings.getArena());
        final var details = player.getOpenInventory().getTopInventory().getItem(4);
        assertNotNull(details);
        final var lore = details.getItemMeta().getLore();
        assertNotNull(lore);
        assertTrue(lore.stream().anyMatch(line -> line.contains(target.getName())));
        assertTrue(lore.stream().anyMatch(line -> line.contains(arena.getName())));
    }

    public static class TestDuelsPlugin extends DuelsPlugin {

        private Config config;
        private Lang lang;
        private GuiListener<DuelsPlugin> guis;
        private ArenaManagerImpl arenas;
        private SettingsManager settings;

        @Override
        public void onEnable() {
            config = new Config(this);
            lang = new Lang(this);
            try {
                config.handleLoad();
                lang.handleLoad();
            } catch (Exception e) {
                throw new AssertionError("Could not load test configuration", e);
            }
            guis = new GuiListener<>(this);
            arenas = new ArenaManagerImpl(this);
            settings = new SettingsManager(this);
        }

        @Override
        public void onDisable() {
        }

        @Override
        public Config getConfiguration() {
            return config;
        }

        @Override
        public Lang getLang() {
            return lang;
        }

        @Override
        public GuiListener<DuelsPlugin> getGuiListener() {
            return guis;
        }

        @Override
        public ArenaManagerImpl getArenaManager() {
            return arenas;
        }

        @Override
        public SettingsManager getSettingManager() {
            return settings;
        }
    }
}
