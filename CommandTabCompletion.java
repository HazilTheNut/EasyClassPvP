import Game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Jared on 3/17/2017.
 */
public class CommandTabCompletion implements TabCompleter {

    GameManager manager;

    CommandTabCompletion(GameManager gameManager) {manager = gameManager; }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) return null;
        switch(strings[0]){
            case "pick":
                return manager.getClassNameRoster();
            case "vote":
                return null;
            case "leave":
                return null;
            case "debug":
                return null;
            case "add":
                return getPlayerRoster();
            default:
                return null;
        }
    }

    private ArrayList<String> getPlayerRoster(){
        ArrayList<String> roster = new ArrayList<>();
        Collection<? extends Player> playerList = Bukkit.getServer().getOnlinePlayers();
        for (Player play : playerList){
            roster.add(play.getName());
        }
        return roster;
    }

}
