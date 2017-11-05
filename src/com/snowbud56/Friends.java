package com.snowbud56;

import com.snowbud56.Commands.FriendCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Friends extends JavaPlugin {
    private static Friends instance;
    private String prefix = "&9&lFriends &8>> &7";
    @Override
    public void onEnable() {
        instance = this;
        getServer().getLogger().info("Friends enabled!");
        getCommand("friend").setExecutor(new FriendCommand());
        getCommand("f").setExecutor(new FriendCommand());
        getServer().getPluginManager().registerEvents(new com.snowbud56.Events.onTeleport(), this);
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        if (!(new File(getDataFolder() + "/Friends.yml").exists())) {
            try {
                YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/Friends.yml")).save(new File(getDataFolder() + "/Friends.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        instance = null;
        getServer().getLogger().info("Plugin disabled!");
    }

    public static Friends getInstance() {
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }
}
