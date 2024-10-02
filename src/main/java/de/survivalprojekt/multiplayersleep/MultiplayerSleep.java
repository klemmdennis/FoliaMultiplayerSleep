package de.survivalprojekt.multiplayersleep;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;

import java.io.File;

public class MultiplayerSleep extends JavaPlugin implements Listener, CommandExecutor {

    private String sleepMessage;
    private String nightSkipMessage;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("multiplayersleep").setExecutor(this);
        createConfig(); // Erstelle die Konfigurationsdatei
        loadConfigValues(); // Lade die Werte aus der Konfigurationsdatei
        getLogger().info("MultiplayerSleep enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MultiplayerSleep disabled!");
    }

    // Erstellt die Konfigurationsdatei, falls nicht vorhanden
    private void createConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Config file not found, creating a new one...");
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        } else {
            getLogger().info("Config file found.");
        }
    }

    // Lädt die Werte aus der Config-Datei und gibt Debug-Informationen aus
    private void loadConfigValues() {
        sleepMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.sleep", "&e{player} is trying to sleep..."));
        nightSkipMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.night_skip", "&e{player} has skipped the night!"));
    }

    // Event wenn Spieler ins Bett geht
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() == BedEnterResult.OK) {
            Player player = event.getPlayer();
            World world = player.getWorld();

            // Vanilla Verhalten: Spieler muss schlafen, um die Nacht zu überspringen
            GlobalRegionScheduler globalScheduler = Bukkit.getGlobalRegionScheduler();
            globalScheduler.runDelayed(this, task -> {
                if (player.isSleeping()) {
                    skipNight(player, world);
                }
            }, 100L); // Verzögerung, um sicherzustellen, dass der Spieler tatsächlich schläft
        }
    }

    // Methode zum Überspringen der Nacht
    private void skipNight(Player player, World world) {
        GlobalRegionScheduler globalScheduler = Bukkit.getGlobalRegionScheduler();
        globalScheduler.execute(this, () -> {
            long time = world.getTime();
            if (time >= 12541 && time <= 23458) {
                world.setTime(0);  // Ändere die Zeit global
                world.setStorm(false);  // Setze das Wetter zurück
                world.setThundering(false);  // Verhindere Gewitter
                world.setWeatherDuration(0);  // Setze die Wetterdauer zurück
                Bukkit.broadcastMessage(nightSkipMessage.replace("{player}", player.getName()));
            }
        });
    }

    // CommandExecutor für /multiplayersleep reload
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.isOp()) {
                File configFile = new File(getDataFolder(), "config.yml");
                if (configFile.exists()) {
                    getLogger().info("Reloading config...");
                    reloadConfig(); // Konfiguration neu laden
                    getLogger().info("Config successfully reloaded.");
                    loadConfigValues(); // Werte erneut laden und Debug-Nachrichten anzeigen
                    sender.sendMessage(ChatColor.GREEN + "MultiplayerSleep config reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Config file not found!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
            return true;
        }
        return false;
    }
}