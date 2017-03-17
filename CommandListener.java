import Game.GameManager;
import Game.GamePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Jared on 3/8/2017.
 */
public class CommandListener implements CommandExecutor {
    private GameManager manager;

    public CommandListener(GameManager gameManager){
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
                                commandSender.sendMessage("§cError: You are §onot§r§c playing the PvP game right now!");
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
                    break;
                case "debug":
                    if (commandSender.isOp()){
                        commandSender.sendMessage("DEBUG INFO:");
                        manager.printRoster(commandSender);
                    } else {
                        commandSender.sendMessage("Access denied!");
                    }
                case "add":
                    if (commandSender.isOp() && strings.length == 2){
                        Player toAdd = commandSender.getServer().getPlayer(strings[1]);
                        manager.addPlayerToRoster(toAdd);
                        commandSender.sendMessage(String.format("§bPlayer \'%1$s\' added to roster!", strings[1]));
                    }
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
