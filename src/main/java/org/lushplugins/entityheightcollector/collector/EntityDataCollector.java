package org.lushplugins.entityheightcollector.collector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.function.Predicate;

public class EntityDataCollector {
    private final Plugin plugin;
    private Predicate<EntityType> filter = (entityType) -> true;
    private BiCallable<ConfigurationSection, Entity> saveData = (section, entity) -> {
        section.set("height", entity.getHeight());

        if (entity instanceof LivingEntity livingEntity) {
            section.set("eye-height", livingEntity.getEyeHeight());
        }
    };
    private int entityTypesPerTick = 10;

    public EntityDataCollector(Plugin plugin) {
        this.plugin = plugin;
    }

    public EntityDataCollector filter(Predicate<EntityType> filter) {
        this.filter = filter;
        return this;
    }

    public EntityDataCollector saveData(BiCallable<ConfigurationSection, Entity> saveData) {
        this.saveData = saveData;
        return this;
    }

    public EntityDataCollector entityTypesPerTick(int typesPerTick) {
        this.entityTypesPerTick = typesPerTick;
        return this;
    }

    public void run() {
        World world = Bukkit.getWorlds().get(0);
        Location location = new Location(world, 0, 0, 0);
        YamlConfiguration data = new YamlConfiguration();

        ArrayDeque<EntityType> entityTypes = new ArrayDeque<>(Registry.ENTITY_TYPE.stream().filter(filter).toList());
        Bukkit.getScheduler().runTaskTimer(plugin, (task) -> {
            if (entityTypes.isEmpty()) {
                task.cancel();

                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }

                String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
                try {
                    data.save(new File(dataFolder, version + ".yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            for (int i = 0; i < entityTypesPerTick; i++) {
                EntityType entityType = entityTypes.pop();
                String key = entityType.getKey().toString();

                Entity entity;
                try {
                    entity = world.spawnEntity(location, entityType);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().severe("Failed to spawn entity of type '" + key + "' when gathering data");
                    continue;
                }

                saveData.call(data.createSection(key), entity);

                entity.remove();
            }
        }, 1, 1);
    }

    @FunctionalInterface
    public interface BiCallable<T, U> {
        void call(T t, U u);
    }
}
