package org.formauth.player;

import cn.nukkit.Player;

public class PlayerData {
    
    public static final int STATUS_SEARCH = 0;
    public static final int STATUS_NOT_REGISTERED = 1;
    public static final int STATUS_REGISTERED = 2;
    public static final int STATUS_AUTHENTICATED = 3;
    
    private Player player;
    private int status;
    private String password;
    private int failedLoginAttempts;
    
    public PlayerData(Player player) {
        this.player = player;
        this.status = STATUS_SEARCH;
        this.password = "";
        this.failedLoginAttempts = 0;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }
} 
