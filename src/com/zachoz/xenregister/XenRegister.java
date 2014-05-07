package com.zachoz.xenregister;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
 *
 * @author Zachoz
 */
public class XenRegister extends JavaPlugin {

    private static String site, apiHash, usernameField, uuidField;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public void onEnable() {
        saveDefaultConfig();
        site = getConfig().getString("site");
        apiHash = getConfig().getString("apihash");
        usernameField = getConfig().getString("custom_field_names.minecraft_username");
        uuidField = getConfig().getString("custom_field_names.minecraft_uuid");

        if (!site.substring(site.length() - 1).equals("/")) {
            site = (site + "/");
        }
    }

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
                        long x = (long) ran.nextInt(Integer.MAX_VALUE) + 999999L; // Generate random number password
                        String registrationStatus = registerUser(sender.getName(), x + "", args[0]);
                        sender.sendMessage(registrationStatus);
                    }
                });
            }
            return true;
        }
        return false;
    }

    /**
     * Registers a user on the forums
     *
     * @param user     Username of the user
     * @param password Password for the user. A randomly generated one is recommended.
     * @param email    Email address for the user
     * @return Status of the registration attempt
     */
    private static String registerUser(String user, String password, String email) {
        if (email.contains("=") || email.contains("&")) {
            return ChatColor.RED + "Potential injection attack! This event has been recorded!";
        }

        if (!email.matches(EMAIL_PATTERN)) {
            return ChatColor.RED + "Invalid email address!";
        }

        String uuid = "null";
        Player p = Bukkit.getPlayer(user);
        if (p != null) uuid = p.getUniqueId().toString();

        try {
            String link = "api.php?action=register&hash=" + apiHash + "&username=" + user.replace('&', '.') +
                    "&password=" + password + "&email=" + email.replace('&', '.').replace('=', '.') +
                    "&custom_fields=" + usernameField + "=" + user + "," + uuidField + "=" + uuid + "&user_state=email_confirm";

            URL url = new URL(site + link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Open URL connection
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // User already exists?
                if (inputLine.contains("{\"error\":7,\"message\":\"Something went wrong when \\\"registering user\\\": \\\"" +
                        "User already exists\\\"\",\"user_error_id\":40,\"user_error_field\":\"username\",\"" +
                        "user_error_key\":\"usernames_must_be_unique\",\"user_error_phrase\":\"Usernames must be unique." +
                        " The specified username is already in use.\"}"))
                    return ChatColor.RED + "A user under this username already exists!";

                // Email already in use?
                if (inputLine.contains("{\"error\":7,\"message\":\"Something went wrong when \\\"registering user\\\": \\\"" +
                        "Email already used\\\"\",\"user_error_id\":42,\"user_error_field\":\"email\",\"user_error_key\":\"" +
                        "email_addresses_must_be_unique\",\"user_error_phrase\":\"Email addresses must be unique. " +
                        "The specified email address is already in use.\"}"))
                    return ChatColor.RED + "A user under this email address already exists!";
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ChatColor.RED + "An internal error has occured! The stack trace has been printed to the server's console!";
        } catch (IOException e) {
            e.printStackTrace();
            return ChatColor.RED + "An internal error has occured! The stack trace has been printed to the server's console!";
        }

        return ChatColor.DARK_AQUA + "Successfully registered on the forums! \n" +
                "A confirmation email should be sent to your email address shortly! Please change your" +
                " password once you confirm your account!";
    }

}

