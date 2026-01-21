package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.boost.BoostEffect;
import com.skyblockexp.ezboost.gui.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handles various input operations for the admin GUI including chat input,
 * number input, and sub-GUI interactions.
 */
public class AdminGuiInputHandler {
    private final JavaPlugin plugin;
    private final LegacyComponentSerializer legacySerializer;
    private final AdminGuiRenderer renderer;
    private final java.util.function.BiConsumer<Player, java.util.function.Consumer<String>> inputCallbackRegistrar;

    public AdminGuiInputHandler(JavaPlugin plugin, LegacyComponentSerializer legacySerializer,
                              AdminGuiRenderer renderer,
                              java.util.function.BiConsumer<Player, java.util.function.Consumer<String>> inputCallbackRegistrar) {
        this.plugin = plugin;
        this.legacySerializer = legacySerializer;
        this.renderer = renderer;
        this.inputCallbackRegistrar = inputCallbackRegistrar;
    }

    /**
     * Prompts the player for text input via chat.
     */
    public void promptForInput(Player player, String prompt, Consumer<String> callback) {
        player.closeInventory();
        player.sendMessage(legacySerializer.serialize(Component.text(prompt)));
        player.sendMessage(legacySerializer.serialize(Component.text("Type your input in chat, or 'cancel' to abort.")));
        // Register the callback with the main GUI
        inputCallbackRegistrar.accept(player, callback);
    }

    /**
     * Handles number input from the NumberInputGui.
     */
    public void handleNumberInput(Player player, String action, BoostCreationState state) {
        String title = player.getOpenInventory().getTitle();
        NumberInputGui.NumberInputHolder holder = (NumberInputGui.NumberInputHolder) player.getOpenInventory().getTopInventory().getHolder();
        int currentValue = holder.getCurrentValue();
        String fieldName = holder.getFieldName();

        if ("back".equals(action)) {
            // Reopen the main boost creation GUI
            Inventory newInventory = renderer.createInventory();
            renderer.fillInventory(newInventory, state);
            player.openInventory(newInventory);
            return;
        }

        if ("confirm".equals(action)) {
            if ("Duration".equals(fieldName)) {
                state.setDuration(currentValue);
                player.sendMessage(legacySerializer.serialize(Component.text("§aDuration set to: §6" + currentValue + "s")));
            } else if ("Cooldown".equals(fieldName)) {
                state.setCooldown(currentValue);
                player.sendMessage(legacySerializer.serialize(Component.text("§aCooldown set to: §6" + currentValue + "s")));
            } else if ("Cost".equals(fieldName)) {
                state.setCost(currentValue / 100.0); // Convert back to double
                player.sendMessage(legacySerializer.serialize(Component.text("§aCost set to: §6" + String.format("%.2f", state.getCost()))));
            } else if ("GUI Slot".equals(fieldName)) {
                state.setSlot(currentValue == 0 ? null : currentValue);
                String slotDisplay = currentValue == 0 ? "Auto" : String.valueOf(currentValue);
                player.sendMessage(legacySerializer.serialize(Component.text("§aGUI Slot set to: §6" + slotDisplay)));
            }
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Inventory newInventory = renderer.createInventory();
                renderer.fillInventory(newInventory, state);
                player.openInventory(newInventory);
            }, 1L);
        } else {
            // Handle other actions (adjust, set) by creating appropriate dummy GUI
            NumberInputGui dummyGui;
            if ("Duration".equals(fieldName)) {
                dummyGui = new NumberInputGui(plugin, "Duration", 1, 86400, 60, value -> {});
            } else if ("Cooldown".equals(fieldName)) {
                dummyGui = new NumberInputGui(plugin, "Cooldown", 0, 86400, 0, value -> {});
            } else if ("Cost".equals(fieldName)) {
                dummyGui = new NumberInputGui(plugin, "Cost", 0, 100000000, 0, value -> {});
            } else {
                return; // Unknown GUI
            }
            dummyGui.handleClick(player, action);
        }
    }

    /**
     * Handles permission input from the PermissionInputGui.
     */
    public void handlePermissionInput(Player player, String action, BoostCreationState state) {
        if ("back".equals(action)) {
            // Reopen the main boost creation GUI
            Inventory newInventory = renderer.createInventory();
            renderer.fillInventory(newInventory, state);
            player.openInventory(newInventory);
            return;
        }

        PermissionInputGui permissionGui = new PermissionInputGui(plugin, permission -> {
            state.setPermission(permission);
            // Close permission GUI and reopen main GUI
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Inventory newInventory = renderer.createInventory();
                renderer.fillInventory(newInventory, state);
                player.openInventory(newInventory);
            });
        }, () -> {
            // Reopen main GUI on back button
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Inventory newInventory = renderer.createInventory();
                renderer.fillInventory(newInventory, state);
                player.openInventory(newInventory);
            }, 1L);
        });
        permissionGui.handleClick(player, action);
    }

    /**
     * Handles custom permission input.
     */
    public void handleCustomPermissionInput(Player player, String permission, BoostCreationState state) {
        PermissionInputGui permissionGui = new PermissionInputGui(plugin, perm -> {
            state.setPermission(perm);
            // Close permission GUI and reopen main GUI
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Inventory newInventory = renderer.createInventory();
                renderer.fillInventory(newInventory, state);
                player.openInventory(newInventory);
            });
        }, () -> {
            // Reopen main GUI on back button
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    Inventory newInventory = renderer.createInventory();
                    renderer.fillInventory(newInventory, state);
                    player.openInventory(newInventory);
                }, 1L);
            });
        });
        permissionGui.handleCustomPermissionInput(player, permission);
    }

    /**
     * Handles effect selection.
     */
    public void handleEffectSelection(Player player, String action, Integer page, BoostCreationState state) {
        if ("back".equals(action)) {
            // Reopen the main boost creation GUI
            Inventory newInventory = renderer.createInventory();
            renderer.fillInventory(newInventory, state);
            player.openInventory(newInventory);
            return;
        }

        if (page != null) {
            // Navigation to different page - create a new GUI instance for navigation
            EffectSelectionGui effectGui = new EffectSelectionGui(plugin, effect -> {
                state.addEffect(effect);
                player.sendMessage(legacySerializer.serialize(Component.text("§aEffect added successfully!")));
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        Inventory newInventory = renderer.createInventory();
                        renderer.fillInventory(newInventory, state);
                        player.openInventory(newInventory);
                    }, 1L);
                });
            });
            effectGui.open(player, page);
            return;
        }

        if (action != null) {
            // Effect selected - create GUI instance to handle the selection
            EffectSelectionGui effectGui = new EffectSelectionGui(plugin, effect -> {
                state.addEffect(effect);
                player.sendMessage(legacySerializer.serialize(Component.text("§aEffect added successfully!")));
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        Inventory newInventory = renderer.createInventory();
                        renderer.fillInventory(newInventory, state);
                        player.openInventory(newInventory);
                    }, 1L);
                });
            });
            effectGui.handleClick(player, action, null);
        }
    }

    /**
     * Handles effect amplifier input.
     */
    public void handleEffectAmplifierInput(Player player, String input, BoostCreationState state) {
        try {
            int amplifier = Integer.parseInt(input);
            if (amplifier < 0 || amplifier > 255) {
                player.sendMessage(legacySerializer.serialize(Component.text("§cAmplifier must be between 0 and 255!")));
                return;
            }

            String effectName = player.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "selected-effect"),
                org.bukkit.persistence.PersistentDataType.STRING
            );

            if (effectName != null) {
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    BoostEffect effect = new BoostEffect(effectType, amplifier, null);
                    state.addEffect(effect);
                    player.sendMessage(legacySerializer.serialize(Component.text("§aEffect added successfully!")));
                } else {
                    player.sendMessage(legacySerializer.serialize(Component.text("§cError: Invalid effect type!")));
                }
            } else {
                player.sendMessage(legacySerializer.serialize(Component.text("§cError: No effect selected!")));
            }

            // Clear the stored effect
            player.getPersistentDataContainer().remove(new NamespacedKey(plugin, "selected-effect"));

            // Reopen the main GUI
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory(); // Close any currently open GUI first
                // Create and open a new inventory instead of trying to fill the closed one
                Inventory newInventory = renderer.createInventory();
                renderer.fillInventory(newInventory, state);
                player.openInventory(newInventory);
            });
        } catch (NumberFormatException e) {
            player.sendMessage(legacySerializer.serialize(Component.text("§cInvalid number! Please enter a valid amplifier level.")));
        }
    }

    /**
     * Handles material selection.
     */
    public void handleMaterialSelection(Player player, String action, Integer page, BoostCreationState state) {
        if ("back".equals(action)) {
            Inventory newInventory = renderer.createInventory();
            renderer.fillInventory(newInventory, state);
            player.openInventory(newInventory);
            return;
        }

        if (page != null) {
            MaterialSelectionGui materialGui = new MaterialSelectionGui(plugin, material -> {
                state.setIcon(material);
                player.sendMessage(legacySerializer.serialize(Component.text("§aIcon material selected!")));
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        Inventory newInventory = renderer.createInventory();
                        renderer.fillInventory(newInventory, state);
                        player.openInventory(newInventory);
                    }, 1L);
                });
            });
            materialGui.open(player, page);
            return;
        }

        if (action != null) {
            MaterialSelectionGui materialGui = new MaterialSelectionGui(plugin, material -> {
                state.setIcon(material);
                player.sendMessage(legacySerializer.serialize(Component.text("§aIcon material selected!")));
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        Inventory newInventory = renderer.createInventory();
                        renderer.fillInventory(newInventory, state);
                        player.openInventory(newInventory);
                    }, 1L);
                });
            });
            materialGui.handleClick(player, action, page);
        }
    }
}