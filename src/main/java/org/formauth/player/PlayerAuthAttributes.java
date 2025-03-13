package org.formauth.player;

import cn.nukkit.Player;
import org.formauth.FormAuth;

import java.util.HashSet;
import java.util.Set;

public class PlayerAuthAttributes {
    
    private static Set<String> authenticatedPlayers = new HashSet<>();
    
    /**
     * Apply restrictions to an unauthenticated player
     *
     * @param player The player to restrict
     */
    public static void applyRestrictions(Player player) {
        // Immobilize player
        player.setImmobile(true);
        
        authenticatedPlayers.remove(player.getName().toLowerCase());
    }
    
    /**
     * Remove restrictions and mark player as authenticated
     *
     * @param player The player to authenticate
     */
    public static void removeRestrictions(Player player) {
        // Allow movement
        player.setImmobile(false);
        
        FormAuth.getSessionStorage().getPlayerData(player).setStatus(PlayerData.STATUS_AUTHENTICATED);
        
        authenticatedPlayers.add(player.getName().toLowerCase());
    }
    
    /**
     * Check if player is authenticated
     *
     * @param player The player to check
     * @return true if player is authenticated
     */
    public static boolean isAuthenticated(Player player) {
        if (player == null) return false;
        return authenticatedPlayers.contains(player.getName().toLowerCase());
    }
} 