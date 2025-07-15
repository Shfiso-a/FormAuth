package org.formauth.player;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import org.formauth.FormAuth;

import java.util.HashMap;
import java.util.Map;

public class SessionStorage {
    private Map<String, PlayerData> sessions;
    
    public SessionStorage() {
        this.sessions = new HashMap<>();
    }
    
    public PlayerData getPlayerData(Player player) {
        String playerName = player.getName().toLowerCase();
        
        if (sessions.containsKey(playerName)) {
            return sessions.get(playerName);
        }
        
        PlayerData playerData = new PlayerData(player);
        
        Config playerDB = new Config(FormAuth.getAuthConfig().getString("database.path"), Config.YAML);
        
        if (playerDB.exists(playerName)) {
            playerData.setStatus(PlayerData.STATUS_REGISTERED);
            playerData.setPassword(playerDB.getString(playerName + ".password"));
        } else {
            playerData.setStatus(PlayerData.STATUS_NOT_REGISTERED);
        }
        
        sessions.put(playerName, playerData);
        
        return playerData;
    }
    
    public void removePlayerData(Player player) {
        String playerName = player.getName().toLowerCase();
        PlayerData data = sessions.get(playerName);
        if (data != null) {
            data.resetFailedLoginAttempts();
        }
        sessions.remove(playerName);
    }
    
    public void savePlayerData(Player player, String password) {
        String playerName = player.getName().toLowerCase();
        
        PlayerData playerData = getPlayerData(player);
        String encryptedPassword = PasswordUtils.encryptPassword(password);
        playerData.setPassword(encryptedPassword);
        playerData.setStatus(PlayerData.STATUS_AUTHENTICATED);
        
        Config playerDB = new Config(FormAuth.getAuthConfig().getString("database.path"), Config.YAML);
        playerDB.set(playerName + ".password", encryptedPassword);
        playerDB.save();
    }
    
    public boolean isRegistered(Player player) {
        return getPlayerData(player).getStatus() != PlayerData.STATUS_NOT_REGISTERED;
    }
    
    public boolean isAuthenticated(Player player) {
        return getPlayerData(player).getStatus() == PlayerData.STATUS_AUTHENTICATED;
    }
} 
