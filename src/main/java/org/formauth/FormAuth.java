package org.formauth;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import org.formauth.form.LoginForm;
import org.formauth.form.RegisterForm;
import org.formauth.player.PasswordUtils;
import org.formauth.player.SessionStorage;

import java.io.File;
import java.util.Map;

public class FormAuth extends PluginBase {
    private static FormAuth instance;
    private static Config authConfig;
    private static SessionStorage sessionStorage;

    @Override
    public void onEnable() {
        instance = this;
        sessionStorage = new SessionStorage();

        getDataFolder().mkdirs();

        if (!new File(getDataFolder() + "/config.yml").exists()) {
            Config config = new Config(getDataFolder() + "/config.yml", Config.YAML);
            config.set("database.path", getDataFolder() + "/players.yml");
            config.set("login.timeout", 60); 
            config.set("afk.timeout", 60); 
            config.set("kick.on.form.close", true); 
            config.set("messages.register.success", "You have successfully registered!");
            config.set("messages.register.error", "Error during registration. Please try again.");
            config.set("messages.login.success", "You have successfully logged in!");
            config.set("messages.login.error", "Invalid password. Please try again.");
            config.set("messages.not.registered", "You are not registered. Please register first.");
            config.set("messages.already.registered", "You are already registered.");
            config.set("messages.already.authenticated", "You are already authenticated.");
            config.set("messages.afk.kick", "You have been kicked for being AFK during authentication");
            config.set("messages.form.close.kick", "Authentication required");
            // customazation 
            config.set("form.login.title", "Login");
            config.set("form.login.label", "Welcome back! Please enter your password to login:");
            config.set("form.login.password.label", "Password");
            config.set("form.login.password.placeholder", "Enter your password here");
            config.set("form.register.title", "Register");
            config.set("form.register.label", "Welcome! Please register to play on this server:");
            config.set("form.register.password.label", "Password");
            config.set("form.register.password.placeholder", "Enter your password here");
            config.set("form.register.confirm.label", "Confirm Password");
            config.set("form.register.confirm.placeholder", "Enter your password again");
            // persian and arabic support 
            config.set("persian.text.enabled", false);
            // login attempt limiter
            config.set("login.max_attempts", 5);
            config.set("messages.login.too_many_attempts", "Too many failed login attempts. Please try again later.");
            
            config.save();
        }

        authConfig = new Config(getDataFolder() + "/config.yml", Config.YAML);

        String dbPath = authConfig.getString("database.path");
        if (!new File(dbPath).exists()) {
            new Config(dbPath, Config.YAML).save();
        }
        
        migratePasswords(dbPath);
        
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        
        getLogger().info("FormAuth plugin has been enabled!");
    }

    /**
     * migrate text
     * 
     * @param dbPath Path to database 
     */
    private void migratePasswords(String dbPath) {
        Config playerDB = new Config(dbPath, Config.YAML);
        boolean needsSave = false;
        
        for (String playerName : playerDB.getKeys(false)) {
            String storedPassword = playerDB.getString(playerName + ".password");
            
            if (storedPassword != null && !storedPassword.contains(":")) {
                String encryptedPassword = PasswordUtils.encryptPassword(storedPassword);
                playerDB.set(playerName + ".password", encryptedPassword);
                needsSave = true;
                getLogger().info("Migrated password for player: " + playerName);
            }
        }
        
        if (needsSave) {
            playerDB.save();
            getLogger().info("Password migration completed successfully!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("FormAuth plugin has been disabled!");
    }

    public static FormAuth getInstance() {
        return instance;
    }

    public static Config getAuthConfig() {
        return authConfig;
    }

    public static SessionStorage getSessionStorage() {
        return sessionStorage;
    }
} 
