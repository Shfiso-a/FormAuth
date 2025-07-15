package org.formauth.form;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;
import org.formauth.FormAuth;
import org.formauth.player.PlayerAuthAttributes;
import org.formauth.player.PlayerData;
import org.formauth.player.PasswordUtils;
import org.formauth.player.PersianTextUtils;

public class LoginForm {
    
    private static final int PASSWORD_FORM_ID = 1;
    
    /**
     * Show the login form to a player
     * 
     * @param player The player to show the form to
     */
    public static void showLoginForm(Player player) {
        boolean persianEnabled = FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false);
        
        String title = FormAuth.getAuthConfig().getString("form.login.title", "Login");
        String label = FormAuth.getAuthConfig().getString("form.login.label", "Welcome back! Please enter your password to login:");
        String passwordLabel = FormAuth.getAuthConfig().getString("form.login.password.label", "Password");
        String passwordPlaceholder = FormAuth.getAuthConfig().getString("form.login.password.placeholder", "Enter your password here");

        if (persianEnabled) {
            title = PersianTextUtils.formatPersianText(title);
            label = PersianTextUtils.formatPersianText(label);
            passwordLabel = PersianTextUtils.formatPersianText(passwordLabel);
            passwordPlaceholder = PersianTextUtils.formatPersianText(passwordPlaceholder);
        }
        
        FormWindowCustom form = new FormWindowCustom(title);
        
        form.addElement(new ElementLabel(label));
        
        form.addElement(new ElementInput(passwordLabel, passwordPlaceholder));
        
        player.showFormWindow(form, PASSWORD_FORM_ID);
    }
    
    /**
     * Process the login form response
     * 
     * @param player The player who submitted the form
     * @param event The form response event
     */
    public static void processResponse(Player player, PlayerFormRespondedEvent event) {
        FormWindow window = event.getWindow();
        
        if (window instanceof FormWindowCustom) {
            FormWindowCustom form = (FormWindowCustom) window;
            
            if (event.getFormID() == PASSWORD_FORM_ID) {
                String password = form.getResponse().getInputResponse(1);
                
                if (password == null || password.isEmpty()) {
                    String message = "Password cannot be empty!";
                    FormAuth.getInstance().getLogger().info("Login failed for " + player.getName() + ": Empty password");
                    player.sendMessage(TextFormat.RED + message);
                    showLoginForm(player);
                    return;
                }
                
                PlayerData playerData = FormAuth.getSessionStorage().getPlayerData(player); 
                
                int maxAttempts = FormAuth.getAuthConfig().getInt("login.max_attempts", 5);
                if (playerData.getFailedLoginAttempts() >= maxAttempts) {
                    String message = "Too many failed login attempts. Please try again later.";
                    if (FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false)) {
                        message = PersianTextUtils.formatPersianText(message);
                    }
                    player.kick(message);
                    return;
                }
                
                if (playerData.getStatus() == PlayerData.STATUS_SEARCH) {
                    String message = "Your data has not been loaded yet... Please try again";
                    FormAuth.getInstance().getLogger().info("Login failed for " + player.getName() + ": Data not loaded");
                    player.sendMessage(TextFormat.RED + message);
                    showLoginForm(player);
                    return;
                }
                
                if (playerData.getStatus() == PlayerData.STATUS_NOT_REGISTERED) {
                    String message = FormAuth.getAuthConfig().getString("messages.not.registered", "You are not registered. Please register first.");
                    if (FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false)) {
                        message = PersianTextUtils.formatPersianText(message);
                    }
                    player.sendMessage(TextFormat.RED + message);
                    RegisterForm.showRegisterForm(player);
                    return;
                }
                
                if (playerData.getStatus() == PlayerData.STATUS_AUTHENTICATED) {
                    String message = FormAuth.getAuthConfig().getString("messages.already.authenticated", "You are already authenticated.");
                    if (FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false)) {
                        message = PersianTextUtils.formatPersianText(message);
                    }
                    player.sendMessage(TextFormat.RED + message);
                    return;
                }
                
                if (PasswordUtils.verifyPassword(password, playerData.getPassword())) {
                    PlayerAuthAttributes.removeRestrictions(player);
                    String message = FormAuth.getAuthConfig().getString("messages.login.success", "You have successfully logged in!");
                    if (FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false)) {
                        message = PersianTextUtils.formatPersianText(message);
                    }
                    player.sendMessage(TextFormat.GREEN + message);
                } else {
                    String message = FormAuth.getAuthConfig().getString("messages.login.error", "Invalid password. Please try again.");
                    if (FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false)) {
                        message = PersianTextUtils.formatPersianText(message);
                    }
                    player.sendMessage(TextFormat.RED + message);
                    showLoginForm(player);
                }
            }
        }
    }
} 
