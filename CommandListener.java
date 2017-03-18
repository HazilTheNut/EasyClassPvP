import Game.GameManager;
import Game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by Jared on 3/8/2017.
 */
public class CommandListener implements CommandExecutor {
    private GameManager manager;
    private EasyClassPvPMain main;

    public CommandListener(GameManager gameManager, EasyClassPvPMain mainClass){
        main = mainClass;
        manager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0){
            givePluginInfo(commandSender);
            return true;
        } else {
            switch(strings[0]){
                case "pick":
                    if (strings.length == 2 && commandSender instanceof Player){
                        try {
                            GamePlayer player = manager.getPlayerFromRoster(commandSender.getName());
                            if (player != null) {
                                manager.assignClass(player, strings[1]);
                            } else {
                                commandSender.sendMessage("§cError: You are not playing the PvP game right now!");
                            }
                        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            e.printStackTrace();
                        }
                    } else {
                        commandSender.sendMessage("§a[ECP]§c Incorrect Usage: §7/ecp pick <Class name>");
                        if (commandSender instanceof Player) manager.printClassOptions((Player)commandSender);
                    }
                    break;
                case "vote":
                    break;
                case "leave":
                    GamePlayer player = manager.getPlayerFromRoster(commandSender.getName());
                    if (player != null) {
                        manager.exitPlayer(player);
                        commandSender.sendMessage("§a[ECP]§d Leaving game...");
                    }
                    break;
                case "debug":
                    if (commandSender.isOp()){
                        commandSender.sendMessage("PLAYER ROSTER:");
                        manager.printRoster(commandSender);
                    } else {
                        commandSender.sendMessage("§a[ECP]§c Access denied!");
                    }
                    break;
                case "add":
                    if (commandSender.isOp() && strings.length == 2){
                        Player toAdd = commandSender.getServer().getPlayer(strings[1]);
                        manager.addPlayerToRoster(toAdd);
                        commandSender.sendMessage(String.format("§bPlayer \'%1$s\' added to roster!", strings[1]));
                    }
                    break;
                case "createmap":
                    if (!commandSender.isOp()){
                        commandSender.sendMessage("§a[ECP]§c Access denied!");
                        break;
                    }
                    if (strings.length != 8){
                        commandSender.sendMessage("§a[ECP]§c Error: Incorrect Usage: §7/ecp createmap <Map Name> <Red Spawn X> <Red Spawn Y> <Red Spawn Z> <Blue Spawn X> <Blue Spawn Y> <Blue Spawn Z>");
                        break;
                    }
                    main.getConfig().addDefault("Maps." + strings[1] + ".redX", Integer.valueOf(strings[2]));
                    main.getConfig().addDefault("Maps." + strings[1] + ".redY", Integer.valueOf(strings[3]));
                    main.getConfig().addDefault("Maps." + strings[1] + ".redZ", Integer.valueOf(strings[4]));
                    main.getConfig().addDefault("Maps." + strings[1] + ".blueX", Integer.valueOf(strings[5]));
                    main.getConfig().addDefault("Maps." + strings[1] + ".blueY", Integer.valueOf(strings[6]));
                    main.getConfig().addDefault("Maps." + strings[1] + ".blueZ", Integer.valueOf(strings[7]));
                    main.getConfig().addDefault("Maps." + strings[1] + ".isActive", true);
                    main.saveConfig();
                    commandSender.sendMessage("§a[ECP]§e Map §f" + strings[1] + "§e created! §7(Path: Maps." + strings[1] + ")");
                    break;
                case "delmap":
                    if (!commandSender.isOp()){
                        commandSender.sendMessage("§a[ECP]§c Access denied!");
                        break;
                    }
                    if (strings.length != 2){
                        commandSender.sendMessage("§a[ECP]§c Error: Incorrect Usage: §7/ecp delmap <Map Name>");
                        break;
                    }
                    //boolean success = false;
                    if (main.getConfig().contains("Maps." + strings[1])) {
                        main.getConfig().set("Maps." + strings[1], null);
                        commandSender.sendMessage("§a[ECP]§e Deletion of Map §f" + strings[1] + "§e successful! §7(Path: Maps." + strings[1] + ")");
                        main.saveConfig();
                    }
                    else commandSender.sendMessage("§a[ECP]§c Detection of Map §f" + strings[1] + "§c failed! §7(Path: Maps." + strings[1] + ")");
                    //if (!success) commandSender.sendMessage("§a[ECP]§c Deletion of Map §f" + strings[1] + "§c failed!");
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void givePluginInfo(CommandSender sender){
        sender.sendMessage("§6§n[ Easy Class PvP Info ]");
        sender.sendMessage("§b/ecp pick <class name>§r - Picks a class");
        sender.sendMessage("§b/ecp vote <map name>§r - Votes for a map between matches");
        sender.sendMessage("§b/ecp leave§r - Leaves the game");
    }
}
