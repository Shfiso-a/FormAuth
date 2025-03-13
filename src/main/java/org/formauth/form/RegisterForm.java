package org.formauth.form;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.utils.TextFormat;
import org.formauth.FormAuth;
import org.formauth.player.PlayerAuthAttributes;
import org.formauth.player.PlayerData;

public class RegisterForm {
    
    private static final int REGISTER_FORM_ID = 2;
    
    /**
     * Show the registration form to a player
     * 
     * @param player The player to show the form to
     */
    public static void showRegisterForm(Player player) {
        FormWindowCustom form = new FormWindowCustom("Register");

        form.addElement(new ElementLabel("Welcome! Please register to play on this server:"));
        
        form.addElement(new ElementInput("Password", "Enter your password here"));
        
        form.addElement(new ElementInput("Confirm Password", "Enter your password again"));
        
        player.showFormWindow(form, REGISTER_FORM_ID);
    }
    
    /**
     * Process the registration form response
     * 
     * @param player The player who submitted the form
     * @param event The form response event
     */
    public static void processResponse(Player player, PlayerFormRespondedEvent event) {
        FormWindow window = event.getWindow();
        
        if (window instanceof FormWindowCustom) {
            FormWindowCustom form = (FormWindowCustom) window;
            
            if (form.getTitle().equals("Register")) {
                String password = form.getResponse().getInputResponse(1);
                String confirmPassword = form.getResponse().getInputResponse(2);
                
                if (password == null || password.isEmpty()) {
                    player.sendMessage(TextFormat.RED + "Password cannot be empty!");
                    showRegisterForm(player);
                    return;
                }
                
                if (!password.equals(confirmPassword)) {
                    player.sendMessage(TextFormat.RED + "Passwords do not match! Please try again.");
                    showRegisterForm(player);
                    return;
                }
                
                PlayerData playerData = FormAuth.getSessionStorage().getPlayerData(player);
                
                if (playerData.getStatus() == PlayerData.STATUS_SEARCH) {
                    player.sendMessage(TextFormat.RED + "Your data has not been loaded yet... Please try again");
                    showRegisterForm(player);
                    return;
                }
                
                if (playerData.getStatus() != PlayerData.STATUS_NOT_REGISTERED) {
                    player.sendMessage(TextFormat.RED + FormAuth.getAuthConfig().getString("messages.already.registered", "You are already registered."));
                    LoginForm.showLoginForm(player);
                    return;
                }
                
                FormAuth.getSessionStorage().savePlayerData(player, password);
                
                PlayerAuthAttributes.removeRestrictions(player);
                
                player.sendMessage(TextFormat.GREEN + FormAuth.getAuthConfig().getString("messages.register.success", "You have successfully registered!"));
            }
        }
    }
} 