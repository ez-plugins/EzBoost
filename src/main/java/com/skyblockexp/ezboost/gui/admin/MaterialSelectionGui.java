package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.gui.ItemMetaCompat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class MaterialSelectionGui {
    private final JavaPlugin plugin;
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final NamespacedKey materialKey;
    private final NamespacedKey pageKey;
    private final Consumer<Material> onMaterialSelected;

    // Common materials for boost icons
    private static final Material[] COMMON_MATERIALS = {
        // Tools & Weapons
        Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE,
        Material.GOLDEN_SWORD, Material.GOLDEN_PICKAXE, Material.GOLDEN_AXE,
        Material.IRON_SWORD, Material.IRON_PICKAXE, Material.IRON_AXE,
        Material.STONE_SWORD, Material.STONE_PICKAXE, Material.WOODEN_SWORD,

        // Armor
        Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
        Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.IRON_HELMET, Material.IRON_CHESTPLATE,

        // Food & Potions
        Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.POTION, Material.SPLASH_POTION,
        Material.BREAD, Material.COOKED_BEEF, Material.CAKE,

        // Blocks
        Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.EMERALD_BLOCK,
        Material.BEACON, Material.ENCHANTING_TABLE, Material.ANVIL,

        // Nature
        Material.GRASS_BLOCK, Material.DIRT, Material.STONE, Material.COBBLESTONE,
        Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,

        // Redstone
        Material.REDSTONE, Material.REDSTONE_BLOCK, Material.REDSTONE_TORCH,

        // Miscellaneous
        Material.CHEST, Material.ENDER_CHEST, Material.BOOK, Material.ENCHANTED_BOOK,
        Material.EXPERIENCE_BOTTLE, Material.FIREWORK_ROCKET, Material.TNT
    };

    public MaterialSelectionGui(JavaPlugin plugin, Consumer<Material> onMaterialSelected) {
        this.plugin = plugin;
        this.onMaterialSelected = onMaterialSelected;
        this.materialKey = new NamespacedKey(plugin, "material-type");
        this.pageKey = new NamespacedKey(plugin, "page");
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        MaterialSelectionHolder holder = new MaterialSelectionHolder();
        Inventory inventory = createInventory(holder, page);
        holder.setInventory(inventory);
        fillInventory(inventory, page);
        player.openInventory(inventory);
    }

    private Inventory createInventory(MaterialSelectionHolder holder, int page) {
        Inventory inventory = Bukkit.createInventory(holder, 54, "Select Icon Material - Page " + (page + 1));
        holder.setPage(page);
        return inventory;
    }

    private void fillInventory(Inventory inventory, int page) {
        inventory.clear();

        int startIndex = page * 45; // 45 materials per page
        int endIndex = Math.min(startIndex + 45, COMMON_MATERIALS.length);

        // Add material items
        for (int i = startIndex; i < endIndex; i++) {
            Material material = COMMON_MATERIALS[i];
            int slot = i - startIndex;
            inventory.setItem(slot, createMaterialItem(material));
        }

        // Navigation buttons
        if (page > 0) {
            inventory.setItem(45, createNavigationItem("Previous Page", Material.ARROW, page - 1));
        }

        if (endIndex < COMMON_MATERIALS.length) {
            inventory.setItem(53, createNavigationItem("Next Page", Material.ARROW, page + 1));
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            ItemMetaCompat.setDisplayName(backMeta, Component.text("§cBack to Boost Creation"));
            backMeta.getPersistentDataContainer().set(materialKey, PersistentDataType.STRING, "back");
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(49, backItem);

        // Fill empty slots with glass panes
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta fillerMeta = filler.getItemMeta();
                if (fillerMeta != null) {
                    ItemMetaCompat.setDisplayName(fillerMeta, Component.text(""));
                    filler.setItemMeta(fillerMeta);
                }
                inventory.setItem(i, filler);
            }
        }
    }

    private ItemStack createMaterialItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = formatMaterialName(material.name());
            ItemMetaCompat.setDisplayName(meta, Component.text("§e" + displayName));
            ItemMetaCompat.setLore(meta, java.util.List.of(
                Component.text("§7Click to select this material"),
                Component.text("§7as the boost icon")
            ));
            meta.getPersistentDataContainer().set(materialKey, PersistentDataType.STRING, material.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavigationItem(String name, Material material, int page) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text("§b" + name));
            meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatMaterialName(String name) {
        String formatted = name.toLowerCase().replace("_", " ");
        if (!formatted.isEmpty()) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        return formatted;
    }

    public void handleClick(Player player, String action, Integer page) {
        if ("back".equals(action)) {
            player.closeInventory();
            return;
        }

        if (page != null) {
            open(player, page);
            return;
        }

        if (action != null) {
            try {
                Material material = Material.valueOf(action.toUpperCase());
                onMaterialSelected.accept(material);
                player.sendMessage(legacySerializer.serialize(Component.text("§aIcon material selected: " + formatMaterialName(material.name()))));
                player.closeInventory();
            } catch (IllegalArgumentException e) {
                player.sendMessage(legacySerializer.serialize(Component.text("§cInvalid material selected!")));
            }
        }
    }

    public static class MaterialSelectionHolder implements org.bukkit.inventory.InventoryHolder {
        private Inventory inventory;
        private int page;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }
    }
}