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

public class LoginForm {
    
    private static final int PASSWORD_FORM_ID = 1;
    
    /**
     * Show the login form to a player
     * 
     * @param player The player to show the form to
     */
    public static void showLoginForm(Player player) {
        
        FormWindowCustom form = new FormWindowCustom("Login");
        
        form.addElement(new ElementLabel("Welcome back! Please enter your password to login:"));
        
        form.addElement(new ElementInput("Password", "Enter your password here"));
        
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
            
            if (form.getTitle().equals("Login")) {
                String password = form.getResponse().getInputResponse(1);
                
                if (password == null || password.isEmpty()) {
                    player.sendMessage(TextFormat.RED + "Password cannot be empty!");
                    showLoginForm(player);
                    return;
                }
                
                PlayerData playerData = FormAuth.getSessionStorage().getPlayerData(player);
                
                if (playerData.getStatus() == PlayerData.STATUS_SEARCH) {
                    player.sendMessage(TextFormat.RED + "Your data has not been loaded yet... Please try again");
                    showLoginForm(player);
                    return;
                }
                
                if (playerData.getStatus() == PlayerData.STATUS_NOT_REGISTERED) {
                    player.sendMessage(TextFormat.RED + FormAuth.getAuthConfig().getString("messages.not.registered", "You are not registered. Please register first."));
                    RegisterForm.showRegisterForm(player);
                    return;
                }
                
                if (playerData.getStatus() == PlayerData.STATUS_AUTHENTICATED) {
                    player.sendMessage(TextFormat.RED + FormAuth.getAuthConfig().getString("messages.already.authenticated", "You are already authenticated."));
                    return;
                }
                
                if (PasswordUtils.verifyPassword(password, playerData.getPassword())) {
                    PlayerAuthAttributes.removeRestrictions(player);
                    player.sendMessage(TextFormat.GREEN + FormAuth.getAuthConfig().getString("messages.login.success", "You have successfully logged in!"));
                } else {
                    player.sendMessage(TextFormat.RED + FormAuth.getAuthConfig().getString("messages.login.error", "Invalid password. Please try again."));
                    showLoginForm(player);
                }
            }
        }
    }
} 
