package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.boost.BoostEffect;
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
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class EffectSelectionGui {
    private final JavaPlugin plugin;
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final NamespacedKey effectKey;
    private final NamespacedKey pageKey;
    private final Consumer<BoostEffect> onEffectSelected;

    // Common potion effects that make sense for boosts
    private static final PotionEffectType[] COMMON_EFFECTS = {
        PotionEffectType.SPEED,
        PotionEffectType.SLOW,
        PotionEffectType.FAST_DIGGING,
        PotionEffectType.SLOW_DIGGING,
        PotionEffectType.INCREASE_DAMAGE,
        PotionEffectType.HEAL,
        PotionEffectType.HARM,
        PotionEffectType.JUMP,
        PotionEffectType.CONFUSION,
        PotionEffectType.REGENERATION,
        PotionEffectType.DAMAGE_RESISTANCE,
        PotionEffectType.FIRE_RESISTANCE,
        PotionEffectType.WATER_BREATHING,
        PotionEffectType.INVISIBILITY,
        PotionEffectType.BLINDNESS,
        PotionEffectType.NIGHT_VISION,
        PotionEffectType.HUNGER,
        PotionEffectType.WEAKNESS,
        PotionEffectType.POISON,
        PotionEffectType.WITHER,
        PotionEffectType.HEALTH_BOOST,
        PotionEffectType.ABSORPTION,
        PotionEffectType.SATURATION,
        PotionEffectType.GLOWING,
        PotionEffectType.LEVITATION,
        PotionEffectType.LUCK,
        PotionEffectType.UNLUCK,
        PotionEffectType.SLOW_FALLING,
        PotionEffectType.CONDUIT_POWER,
        PotionEffectType.DOLPHINS_GRACE
    };

    public EffectSelectionGui(JavaPlugin plugin, Consumer<BoostEffect> onEffectSelected) {
        this.plugin = plugin;
        this.onEffectSelected = onEffectSelected;
        this.effectKey = new NamespacedKey(plugin, "effect-type");
        this.pageKey = new NamespacedKey(plugin, "page");
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        EffectSelectionHolder holder = new EffectSelectionHolder();
        Inventory inventory = createInventory(holder, page);
        holder.setInventory(inventory);
        fillInventory(inventory, page);
        player.openInventory(inventory);
    }

    private Inventory createInventory(EffectSelectionHolder holder, int page) {
        Inventory inventory = Bukkit.createInventory(holder, 54, "Select Effect - Page " + (page + 1));
        holder.setPage(page);
        return inventory;
    }

    private void fillInventory(Inventory inventory, int page) {
        inventory.clear();

        int startIndex = page * 45; // 45 effects per page (5 rows * 9 columns)
        int endIndex = Math.min(startIndex + 45, COMMON_EFFECTS.length);

        // Add effect items
        for (int i = startIndex; i < endIndex; i++) {
            PotionEffectType effect = COMMON_EFFECTS[i];
            int slot = i - startIndex;
            inventory.setItem(slot, createEffectItem(effect));
        }

        // Navigation buttons
        if (page > 0) {
            inventory.setItem(45, createNavigationItem("Previous Page", Material.ARROW, page - 1));
        }

        if (endIndex < COMMON_EFFECTS.length) {
            inventory.setItem(53, createNavigationItem("Next Page", Material.ARROW, page + 1));
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            ItemMetaCompat.setDisplayName(backMeta, Component.text("Back to Boost Creation"));
            backMeta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, "back");
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(49, backItem);

        // Fill empty slots with glass panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            ItemMetaCompat.setDisplayName(fillerMeta, Component.text(""));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private ItemStack createEffectItem(PotionEffectType effect) {
        Material material = getEffectMaterial(effect);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text(formatEffectName(effect.getName())));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Click to select this effect"));
            lore.add(Component.text("You will be prompted for the amplifier"));
            ItemMetaCompat.setLore(meta, lore);
            meta.getPersistentDataContainer().set(effectKey, PersistentDataType.STRING, effect.getName());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavigationItem(String name, Material material, int page) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text(name));
            meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getEffectMaterial(PotionEffectType effect) {
        return switch (effect.getName().toLowerCase()) {
            case "speed" -> Material.SUGAR;
            case "slow" -> Material.SOUL_SAND;
            case "fast_digging" -> Material.GOLDEN_PICKAXE;
            case "slow_digging" -> Material.WOODEN_PICKAXE;
            case "increase_damage" -> Material.DIAMOND_SWORD;
            case "heal" -> Material.GOLDEN_APPLE;
            case "harm" -> Material.WITHER_SKELETON_SKULL;
            case "jump" -> Material.SLIME_BLOCK;
            case "confusion" -> Material.POISONOUS_POTATO;
            case "regeneration" -> Material.GHAST_TEAR;
            case "damage_resistance" -> Material.SHIELD;
            case "fire_resistance" -> Material.BLAZE_POWDER;
            case "water_breathing" -> Material.PUFFERFISH;
            case "invisibility" -> Material.GLASS;
            case "blindness" -> Material.INK_SAC;
            case "night_vision" -> Material.ENDER_EYE;
            case "hunger" -> Material.ROTTEN_FLESH;
            case "weakness" -> Material.BONE;
            case "poison" -> Material.SPIDER_EYE;
            case "wither" -> Material.WITHER_ROSE;
            case "health_boost" -> Material.REDSTONE;
            case "absorption" -> Material.ENCHANTED_GOLDEN_APPLE;
            case "saturation" -> Material.BREAD;
            case "glowing" -> Material.GLOWSTONE_DUST;
            case "levitation" -> Material.SHULKER_SHELL;
            case "luck" -> Material.EMERALD;
            case "unluck" -> Material.COAL;
            case "slow_falling" -> Material.PHANTOM_MEMBRANE;
            case "conduit_power" -> Material.CONDUIT;
            case "dolphins_grace" -> Material.PRISMARINE_SHARD;
            default -> Material.POTION;
        };
    }

    private String formatEffectName(String name) {
        String formatted = name.toLowerCase().replace("_", " ");
        if (!formatted.isEmpty()) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        return formatted;
    }

    public void handleClick(Player player, String action, Integer page) {
        if ("back".equals(action)) {
            // Close this GUI - the listener will handle reopening the main GUI
            player.closeInventory();
            return;
        }

        if (page != null) {
            // Navigation to different page
            open(player, page);
            return;
        }

        if (action != null) {
            // Effect selected
            try {
                PotionEffectType effectType = PotionEffectType.getByName(action.toUpperCase());
                if (effectType != null) {
                    // Prompt for amplifier
                    promptForAmplifier(player, effectType);
                } else {
                    player.sendMessage(legacySerializer.serialize(Component.text("Invalid effect selected!")));
                }
            } catch (Exception e) {
                player.sendMessage(legacySerializer.serialize(Component.text("Error selecting effect!")));
            }
        }
    }

    private void promptForAmplifier(Player player, PotionEffectType effectType) {
        player.closeInventory();
        player.sendMessage(legacySerializer.serialize(Component.text("Selected effect: " + formatEffectName(effectType.getName()))));
        player.sendMessage(legacySerializer.serialize(Component.text("Enter amplifier level (0-255, 0 = level 1):")));

        // Store the effect type for later use
        player.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "selected-effect"),
            PersistentDataType.STRING,
            effectType.getName()
        );
    }

    public void handleAmplifierInput(Player player, String amplifierInput) {
        try {
            int amplifier = Integer.parseInt(amplifierInput);
            if (amplifier < 0 || amplifier > 255) {
                player.sendMessage(legacySerializer.serialize(Component.text("Amplifier must be between 0 and 255!")));
                return;
            }

            String effectName = player.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "selected-effect"),
                PersistentDataType.STRING
            );

            if (effectName != null) {
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    BoostEffect effect = new BoostEffect(effectType, amplifier, null);
                    onEffectSelected.accept(effect);
                    player.sendMessage(legacySerializer.serialize(Component.text("Effect added successfully!")));
                } else {
                    player.sendMessage(legacySerializer.serialize(Component.text("Error: Invalid effect type!")));
                }
            } else {
                player.sendMessage(legacySerializer.serialize(Component.text("Error: No effect selected!")));
            }

            // Clear the stored effect
            player.getPersistentDataContainer().remove(new NamespacedKey(plugin, "selected-effect"));

        } catch (NumberFormatException e) {
            player.sendMessage(legacySerializer.serialize(Component.text("Invalid number! Please enter a valid amplifier level.")));
        }
    }

    public static class EffectSelectionHolder implements org.bukkit.inventory.InventoryHolder {
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