package org.formauth;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.scheduler.TaskHandler;
import org.formauth.form.LoginForm;
import org.formauth.form.RegisterForm;
import org.formauth.player.PlayerAuthAttributes;
import org.formauth.player.PlayerData;

import java.util.HashMap;
import java.util.Map;

public class EventListener implements Listener {
    
    private Map<Player, TaskHandler> authTimeoutTasks = new HashMap<>();
    
    private Map<Player, TaskHandler> afkTimeoutTasks = new HashMap<>();
    
    private Map<Player, Long> lastActivityTime = new HashMap<>();
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        lastActivityTime.put(player, System.currentTimeMillis());
        
        PlayerAuthAttributes.applyRestrictions(player);
        
        PlayerData playerData = FormAuth.getSessionStorage().getPlayerData(player);
        
        FormAuth.getInstance().getServer().getScheduler().scheduleDelayedTask(FormAuth.getInstance(), () -> {
            if (playerData.getStatus() == PlayerData.STATUS_NOT_REGISTERED) {
                RegisterForm.showRegisterForm(player);
            } else if (playerData.getStatus() == PlayerData.STATUS_REGISTERED) {
                LoginForm.showLoginForm(player);
            }
        }, 20); // 20 ticks
        
        int timeout = FormAuth.getAuthConfig().getInt("login.timeout", 60);
        authTimeoutTasks.put(player, FormAuth.getInstance().getServer().getScheduler().scheduleDelayedTask(FormAuth.getInstance(), () -> {
            if (!PlayerAuthAttributes.isAuthenticated(player)) {
                player.kick("Login timeout exceeded");
            }
        }, timeout * 20)); 
        
        startAfkTimer(player);
    }
    
    /**
     * Start AFK timeout timer for a player
     */
    private void startAfkTimer(Player player) {
        if (afkTimeoutTasks.containsKey(player)) {
            afkTimeoutTasks.get(player).cancel();
        }
        
        int afkTimeout = FormAuth.getAuthConfig().getInt("afk.timeout", 60);
        
        afkTimeoutTasks.put(player, FormAuth.getInstance().getServer().getScheduler().scheduleDelayedRepeatingTask(FormAuth.getInstance(), () -> {
            if (!PlayerAuthAttributes.isAuthenticated(player)) {
                long currentTime = System.currentTimeMillis();
                long lastActive = lastActivityTime.getOrDefault(player, currentTime);
                
                if (currentTime - lastActive >= afkTimeout * 1000) {
                    player.kick(FormAuth.getAuthConfig().getString("messages.afk.kick", "You have been kicked for being AFK during authentication"));
                }
            } else {
                if (afkTimeoutTasks.containsKey(player)) {
                    afkTimeoutTasks.get(player).cancel();
                    afkTimeoutTasks.remove(player);
                }
            }
        }, 20 * 10, 20 * 10)); 
    }
    
    /**
     * Update player's last activity time
     */
    private void updatePlayerActivity(Player player) {
        lastActivityTime.put(player, System.currentTimeMillis());
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updatePlayerActivity(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        updatePlayerActivity(player);
        
        if (!PlayerAuthAttributes.isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * blocking commands for any player that didn't login or register
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        if (!PlayerAuthAttributes.isAuthenticated(player)) {
            event.setCancelled(true);
            
            PlayerData playerData = FormAuth.getSessionStorage().getPlayerData(player);
            if (playerData.getStatus() == PlayerData.STATUS_NOT_REGISTERED) {
                RegisterForm.showRegisterForm(player);
            } else if (playerData.getStatus() == PlayerData.STATUS_REGISTERED) {
                LoginForm.showLoginForm(player);
            }
        }
    }

    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (authTimeoutTasks.containsKey(player)) {
            authTimeoutTasks.get(player).cancel();
            authTimeoutTasks.remove(player);
        }
        
        if (afkTimeoutTasks.containsKey(player)) {
            afkTimeoutTasks.get(player).cancel();
            afkTimeoutTasks.remove(player);
        }
        
        lastActivityTime.remove(player);
        
        FormAuth.getSessionStorage().removePlayerData(player);
    }
    
    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        FormWindow window = event.getWindow();
        
        updatePlayerActivity(player);
        
        boolean isAuthForm = false;
        String title = "";
        
        if (window instanceof FormWindowCustom) {
            title = ((FormWindowCustom) window).getTitle();
            isAuthForm = title.equals("Login") || title.equals("Register");
        } else if (window instanceof FormWindowSimple) {
            title = ((FormWindowSimple) window).getTitle();
            isAuthForm = title.contains("Login") || title.contains("Register");
        }
        
        if (event.wasClosed()) {
            if (isAuthForm && !PlayerAuthAttributes.isAuthenticated(player)) {
                if (FormAuth.getAuthConfig().getBoolean("kick.on.form.close", true)) {
                    FormAuth.getInstance().getServer().getScheduler().scheduleDelayedTask(FormAuth.getInstance(), () -> {
                        player.kick(FormAuth.getAuthConfig().getString("messages.form.close.kick", "Authentication required - you closed the form"));
                    }, 5); 
                    return;
                } else {
                    FormAuth.getInstance().getServer().getScheduler().scheduleDelayedTask(FormAuth.getInstance(), () -> {
                        if (!PlayerAuthAttributes.isAuthenticated(player)) {
                            PlayerData playerData = FormAuth.getSessionStorage().getPlayerData(player);
                            if (playerData.getStatus() == PlayerData.STATUS_NOT_REGISTERED) {
                                RegisterForm.showRegisterForm(player);
                            } else if (playerData.getStatus() == PlayerData.STATUS_REGISTERED) {
                                LoginForm.showLoginForm(player);
                            }
                        }
                    }, 20);
                }
            }
            return;
        }
        
        if (window instanceof FormWindowSimple) {
            FormResponseSimple response = (FormResponseSimple) window.getResponse();
            String windowTitle = ((FormWindowSimple) window).getTitle();
            
            if (windowTitle.contains("Login")) {
                LoginForm.processResponse(player, event);
            } else if (windowTitle.contains("Register")) {
                RegisterForm.processResponse(player, event);
            }
        } else if (window instanceof FormWindowCustom) {
            String windowTitle = ((FormWindowCustom) window).getTitle();
            
            if (windowTitle.equals("Login")) {
                LoginForm.processResponse(player, event);
            } else if (windowTitle.equals("Register")) {
                RegisterForm.processResponse(player, event);
            }
        }
    }
} 
