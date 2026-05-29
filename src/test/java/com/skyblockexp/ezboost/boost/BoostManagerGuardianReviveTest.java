package com.skyblockexp.ezboost.boost;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.economy.EconomyService;
import com.skyblockexp.ezboost.storage.EzBoostRepository;
import com.skyblockexp.ezboost.storage.PlayerBoostStateRecord;
import com.skyblockexp.ezboost.storage.PlayerCooldownRecord;
import com.skyblockexp.ezboost.storage.YamlDataStore;
import com.github.ezframework.jaloquent.model.ModelRepository;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BoostManagerGuardianReviveTest {

    @TempDir
    Path tempDir;

    private EzBoostRepository buildRepo() {
        Logger logger = Logger.getLogger("test");
        YamlDataStore store = new YamlDataStore(tempDir.resolve("data.yml").toFile(), logger);
        ModelRepository<PlayerBoostStateRecord> stateRepo =
                new ModelRepository<>(store, "boost_states", PlayerBoostStateRecord.FACTORY);
        ModelRepository<PlayerCooldownRecord> cooldownRepo =
                new ModelRepository<>(store, "cooldowns", PlayerCooldownRecord.FACTORY);
        return new EzBoostRepository(stateRepo, cooldownRepo, logger);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consumeReviveIfLethal_revivesAndConsumesBoost() throws Exception {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        Messages messages = mock(Messages.class);
        EconomyService economy = mock(EconomyService.class);
        EzBoostRepository repo = buildRepo();
        EzBoostConfig config = mock(EzBoostConfig.class);

        BoostDefinition guardian = new BoostDefinition(
                "guardian",
                "Guardian",
                Material.TOTEM_OF_UNDYING,
                List.of(),
                new BoostCommands(List.of(), List.of(), List.of()),
                300,
                600,
                0.0,
                "ezboost.boost.guardian",
                true,
                true,
                4.0D
        );

        BoostManager manager = new BoostManager(plugin, config, messages, economy, repo);

        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        World world = mock(World.class);
        AttributeInstance maxHealth = mock(AttributeInstance.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getHealth()).thenReturn(5.0D);
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");
        when(player.getAttribute(Attribute.MAX_HEALTH)).thenReturn(maxHealth);
        when(maxHealth.getValue()).thenReturn(20.0D);
        when(player.getNoDamageTicks()).thenReturn(0);
        when(config.getEffectiveBoost("guardian", "world", null)).thenReturn(guardian);

        Field statesField = BoostManager.class.getDeclaredField("states");
        statesField.setAccessible(true);
        Map<UUID, BoostState> states = (Map<UUID, BoostState>) statesField.get(manager);
        BoostState state = new BoostState();
        state.setActiveBoost("guardian", System.currentTimeMillis() + 30_000L);
        states.put(uuid, state);

        boolean consumed = manager.consumeReviveIfLethal(player, 10.0D);
        assertTrue(consumed);
        assertNull(state.activeBoostKey());
        verify(player).setHealth(8.0D);
        verify(player).setFireTicks(0);
    }
}
