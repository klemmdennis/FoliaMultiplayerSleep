package de.survivalprojekt.multiplayersleep;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.World;

public class Scheduler {

    private final Plugin plugin;

    public Scheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void runTaskInRegion(Location location, Runnable task) {
        World world = location.getWorld();
        if (world != null) {
            // Verwendet die Chunk-Koordinaten für die Region
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;

            // Führe die Aufgabe in der Region aus, die den Chunk besitzt
            RegionScheduler regionScheduler = Bukkit.getServer().getRegionScheduler();
            regionScheduler.execute(plugin, world, chunkX, chunkZ, task);
        }
    }
}
