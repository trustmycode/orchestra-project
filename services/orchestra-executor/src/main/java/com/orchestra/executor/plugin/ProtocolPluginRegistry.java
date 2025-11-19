package com.orchestra.executor.plugin;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProtocolPluginRegistry {

    private final List<ProtocolPlugin> plugins;

    public ProtocolPluginRegistry(List<ProtocolPlugin> plugins) {
        this.plugins = plugins;
    }

    public Optional<ProtocolPlugin> getPlugin(String channelType) {
        return plugins.stream().filter(p -> p.supports(channelType)).findFirst();
    }
}
