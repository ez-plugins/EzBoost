package com.skyblockexp.ezboost.boost;

import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.economy.EconomyService;
import com.skyblockexp.ezboost.storage.EzBoostRepository;
import com.skyblockexp.ezboost.storage.PlayerBoostStateRecord;
import com.skyblockexp.ezboost.storage.PlayerCooldownRecord;
import com.skyblockexp.ezboost.storage.YamlDataStore;
import com.github.ezframework.jaloquent.model.ModelRepository;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BoostManagerStorageTest {

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

    @Test
    public void constructor_acceptsEzBoostRepository() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        Messages messages        = mock(Messages.class);
        EconomyService economy   = mock(EconomyService.class);
        EzBoostRepository storage = buildRepo();

        BoostManager bm = new BoostManager(plugin, null, messages, economy, storage);
        assertNotNull(bm);
    }

    @Test
    public void constructor_nullStorage_throwsNullPointerException() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        Messages messages      = mock(Messages.class);
        EconomyService economy = mock(EconomyService.class);

        assertThrows(NullPointerException.class,
                () -> new BoostManager(plugin, null, messages, economy, null));
    }
}
