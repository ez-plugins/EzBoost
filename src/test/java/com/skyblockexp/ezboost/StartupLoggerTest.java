package com.skyblockexp.ezboost;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class StartupLoggerTest {

    @Test
    void logEnable_withVaultAndPapi_logsVersionAndHooks() {
        Logger log = mock(Logger.class);
        StartupLogger.logEnable(log, "2.1.0", 5, true, true);
        verify(log).info(contains("EzBoost v2.1.0"));
        verify(log).info(contains("Vault"));
        verify(log).info(contains("hooked"));
    }

    @Test
    void logEnable_withoutVaultOrPapi_logsDisabledAndNotFound() {
        Logger log = mock(Logger.class);
        StartupLogger.logEnable(log, "1.0.0", 0, false, false);
        verify(log).info(contains("disabled"));
        verify(log).info(contains("not found"));
    }

    @Test
    void logDisable_logsDisabledMessage() {
        Logger log = mock(Logger.class);
        StartupLogger.logDisable(log);
        verify(log).info(contains("disabled"));
    }
}
