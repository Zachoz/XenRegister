package com.zachoz.xenregister;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * Plugin to allow in-game registration for the XenForo forum software.
 * @author Zachoz
 */
public class XenRegister extends JavaPlugin {

    public static String site;
    public static String apiHash;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        site = getConfig().getString("site");
        apiHash = getConfig().getString("apihash");
    }

    @Override
    public void onDisable() {

    }

    public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("register")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Please specify your email address!");
                return false;
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                    public void run() {
                        Random ran = new Random();
                        int x = ran.nextInt(Integer.MAX_VALUE) + 999999; // Generate random number password
                        if (registerUser(sender.getName(), x + "", args[0])) {
                            sender.sendMessage(ChatColor.DARK_AQUA + "Successfully registered on the forums!!");
                            sender.sendMessage(ChatColor.DARK_AQUA + "A confirmation email should be " +
                                    "sent to your email address shortly! Please change your password once you confirm your account!");
                            sender.sendMessage(ChatColor.GOLD + site);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Registration failed! An account using the same username or email already exists!");
                        }
                    }
                });
            }
            return true;
        }
        return false;
    }

    /**
     * Registers a user on the xenregister
     *
     * @param user     Username of the user
     * @param password Password for the user. A randomly generated one is recommended.
     * @param email    Email address for the user
     * @return True if reqistration was successful
     */
    public static boolean registerUser(String user, String password, String email) {
        try {
            String link = "api.php?action=register&hash=" + apiHash + "&username=" + user +
                    "&password=" + password + "&email=" + email + "&custom_fields=minecraftusername=" + user
                    + "&user_state=email_confirm";

            URL url = new URL(site + link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Open URL connection
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // User already exists?
                if (inputLine.contains("{\"error\":7,\"message\":\"Something went wrong when \\\"registering user\\\": \\\"" +
                        "User already exists\\\"\",\"user_error_id\":40,\"user_error_field\":\"username\",\"" +
                        "user_error_key\":\"usernames_must_be_unique\",\"user_error_phrase\":\"Usernames must be unique." +
                        " The specified username is already in use.\"}")) return false;

                // Email already in use?
                if (inputLine.contains("{\"error\":7,\"message\":\"Something went wrong when \\\"registering user\\\": \\\"" +
                        "Email already used\\\"\",\"user_error_id\":42,\"user_error_field\":\"email\",\"user_error_key\":\"" +
                        "email_addresses_must_be_unique\",\"user_error_phrase\":\"Email addresses must be unique. " +
                        "The specified email address is already in use.\"}")) return false;
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}

