package com.skyblockexp.ezboost.boost;

import java.util.List;

public final class BoostCommands {
    private final List<String> enable;
    private final List<String> disable;
    private final List<String> toggle;

    public BoostCommands(List<String> enable, List<String> disable, List<String> toggle) {
        this.enable = List.copyOf(enable);
        this.disable = List.copyOf(disable);
        this.toggle = List.copyOf(toggle);
    }

    public List<String> enable() {
        return enable;
    }

    public List<String> disable() {
        return disable;
    }

    public List<String> toggle() {
        return toggle;
    }
}
