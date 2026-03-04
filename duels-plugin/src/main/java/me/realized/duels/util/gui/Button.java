package me.realized.duels.util.gui;

import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class Button<P extends JavaPlugin> {

    protected final P plugin;
    @Getter
    @Setter
    private ItemStack displayed;

    public Button(final P plugin, final ItemStack displayed) {
        this.plugin = plugin;
        this.displayed = displayed;
    }

    protected void setDisplayName(final String name) {
        getDisplayed().editMeta(meta -> meta.setDisplayName(StringUtil.color(name)));
    }

    protected void setLore(final List<String> lore) {
        getDisplayed().editMeta(meta -> meta.setLore(StringUtil.color(lore)));
    }

    protected void setLore(final String... lore) {
        setLore(Arrays.asList(lore));
    }

    protected void setOwner(final Player player) {
        if (Items.equals(displayed, Items.HEAD)) {
            getDisplayed().editMeta(SkullMeta.class, skullMeta -> skullMeta.setOwningPlayer(player));
        }
    }

    protected void setGlow(final boolean glow) {
        // Normal golden apples do not have enchantment glint even with an enchantment applied, so we change the item type.
        if (displayed.getType().name().endsWith("GOLDEN_APPLE")) {
            final ItemStack item = glow ? Items.ENCHANTED_GOLDEN_APPLE.clone() : ItemBuilder.of(Material.GOLDEN_APPLE).build();
            item.setItemMeta(getDisplayed().getItemMeta());
            setDisplayed(item);
            return;
        }

        getDisplayed().editMeta(meta -> meta.setEnchantmentGlintOverride(glow));
    }

    public void update(final Player player) {
    }

    public void onClick(final Player player) {
    }
}
