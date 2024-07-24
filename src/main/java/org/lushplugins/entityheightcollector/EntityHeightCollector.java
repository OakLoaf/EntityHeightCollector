package org.lushplugins.entityheightcollector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;

public final class EntityHeightCollector extends JavaPlugin {
    private static final int ENTITY_TYPES_PER_TICK = 5;

    @Override
    public void onEnable() {
        World world = Bukkit.getWorlds().get(0);
        Location location = new Location(world, 0, 0, 0);
        YamlConfiguration data = new YamlConfiguration();

        ArrayDeque<EntityType> entityTypes = new ArrayDeque<>(Registry.ENTITY_TYPE.stream().toList());
        Bukkit.getScheduler().runTaskTimer(this, (task) -> {
            if (entityTypes.isEmpty()) {
                task.cancel();

                File dataFolder = getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }

                String version = getServer().getBukkitVersion().split("-")[0];
                try {
                    data.save(new File(dataFolder, version + ".yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            for (int i = 0; i < ENTITY_TYPES_PER_TICK; i++) {
                EntityType entityType = entityTypes.pop();
                String key = entityType.getKey().toString();

                Entity entity;
                try {
                    entity = world.spawnEntity(location, entityType);
                } catch (IllegalArgumentException e) {
                    getLogger().severe("Failed to spawn entity of type '" + key + "' when gathering data");
                    continue;
                }

                data.set(key + ".height", entity.getHeight());

                if (entity instanceof LivingEntity livingEntity) {
                    data.set(key + ".eye-height", livingEntity.getEyeHeight());
                }

                entity.remove();
            }
        }, 1, 1);
    }
}
