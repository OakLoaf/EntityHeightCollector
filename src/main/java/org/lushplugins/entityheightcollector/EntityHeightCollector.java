package org.lushplugins.entityheightcollector;

import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.entityheightcollector.collector.EntityDataCollector;

public final class EntityHeightCollector extends JavaPlugin {

    @Override
    public void onEnable() {
        new EntityDataCollector(this)
            .entityTypesPerTick(5)
            .run();
    }
}
