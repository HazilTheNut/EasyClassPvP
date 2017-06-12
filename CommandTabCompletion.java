import Game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jared on 3/17/2017.
 */
public class CommandTabCompletion implements TabCompleter {

    private GameManager manager;
    private EasyClassPvPMain main;

    CommandTabCompletion(GameManager gameManager, EasyClassPvPMain mainClass) {
        manager = gameManager;
        main = mainClass;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) return null;
        switch(strings[0]){
            case "pick":
                return null;
            case "vote":
                ArrayList<String> maps = new ArrayList<>();
                ArrayList<String> rawList = new ArrayList<>();
                rawList.addAll(main.getConfig().getConfigurationSection("Maps").getKeys(false));
                for (String map : rawList){
                    if ((Boolean) main.getConfig().get("Maps." + map + ".enabled")) maps.add(map);
                }
                return maps;
            case "leave":
                return null;
            case "debug":
                return null;
            case "add":
                return getPlayerRoster();
            default:
                String[] cmdList = {"pick","vote","leave"};
                String[] OpCmdList = {"debug","add","createmap","delmap","start","setlobbyspawn","setgametime"};
                ArrayList<String> cmds = new ArrayList<>();
                Collections.addAll(cmds, cmdList);
                if (commandSender.isOp()){
                    ArrayList<String> opCmds = new ArrayList<>();
                    Collections.addAll(opCmds, OpCmdList);
                    cmds.addAll(opCmds);
                }
                return cmds;
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
