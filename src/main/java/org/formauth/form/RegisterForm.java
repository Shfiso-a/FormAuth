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
import org.formauth.player.PersianTextUtils;

public class RegisterForm {
    
    private static final int REGISTER_FORM_ID = 2;
    
    /**
     * Show the registration form to a player
     * 
     * @param player The player to show the form to
     */
    public static void showRegisterForm(Player player) {
        boolean persianEnabled = FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false);
        
        String title = FormAuth.getAuthConfig().getString("form.register.title", "Register");
        String label = FormAuth.getAuthConfig().getString("form.register.label", "Welcome! Please register to play on this server:");
        String passwordLabel = FormAuth.getAuthConfig().getString("form.register.password.label", "Password");
        String passwordPlaceholder = FormAuth.getAuthConfig().getString("form.register.password.placeholder", "Enter your password here");
        String confirmLabel = FormAuth.getAuthConfig().getString("form.register.confirm.label", "Confirm Password");
        String confirmPlaceholder = FormAuth.getAuthConfig().getString("form.register.confirm.placeholder", "Enter your password again");

        if (persianEnabled) {
            title = PersianTextUtils.formatPersianText(title);
            label = PersianTextUtils.formatPersianText(label);
            passwordLabel = PersianTextUtils.formatPersianText(passwordLabel);
            passwordPlaceholder = PersianTextUtils.formatPersianText(passwordPlaceholder);
            confirmLabel = PersianTextUtils.formatPersianText(confirmLabel);
            confirmPlaceholder = PersianTextUtils.formatPersianText(confirmPlaceholder);
        }
        
        FormWindowCustom form = new FormWindowCustom(title);

        form.addElement(new ElementLabel(label));
        
        form.addElement(new ElementInput(passwordLabel, passwordPlaceholder));
        
        form.addElement(new ElementInput(confirmLabel, confirmPlaceholder));
        
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
            
            if (event.getFormID() == REGISTER_FORM_ID) {
                String password = form.getResponse().getInputResponse(1);
                String confirmPassword = form.getResponse().getInputResponse(2);
                
                if (password == null || password.isEmpty()) {
                    String message = "Password cannot be empty!";
                    FormAuth.getInstance().getLogger().info("Registration failed for " + player.getName() + ": Empty password");
                    player.sendMessage(TextFormat.RED + message);
                    showRegisterForm(player);
                    return;
                }
                
                if (!password.equals(confirmPassword)) {
                    String message = "Passwords do not match! Please try again.";
                    FormAuth.getInstance().getLogger().info("Registration failed for " + player.getName() + ": Passwords don't match");
                    player.sendMessage(TextFormat.RED + message);
                    showRegisterForm(player);
                    return;
                }
                
                PlayerData playerData = FormAuth.getSessionStorage().getPlayerData(player);
                
                if (playerData.getStatus() == PlayerData.STATUS_SEARCH) {
                    String message = "Your data has not been loaded yet... Please try again";
                    FormAuth.getInstance().getLogger().info("Registration failed for " + player.getName() + ": Data not loaded");
                    player.sendMessage(TextFormat.RED + message);
                    showRegisterForm(player);
                    return;
                }
                
                if (playerData.getStatus() != PlayerData.STATUS_NOT_REGISTERED) {
                    String message = FormAuth.getAuthConfig().getString("messages.already.registered", "You are already registered.");
                    if (FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false)) {
                        message = PersianTextUtils.formatPersianText(message);
                    }
                    player.sendMessage(TextFormat.RED + message);
                    LoginForm.showLoginForm(player);
                    return;
                }
                
                FormAuth.getSessionStorage().savePlayerData(player, password);
                
                PlayerAuthAttributes.removeRestrictions(player);
                
                String message = FormAuth.getAuthConfig().getString("messages.register.success", "You have successfully registered!");
                if (FormAuth.getAuthConfig().getBoolean("persian.text.enabled", false)) {
                    message = PersianTextUtils.formatPersianText(message);
                }
                player.sendMessage(TextFormat.GREEN + message);
            }
        }
    }
} 