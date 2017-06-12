import Game.PvPEventListener;
import Game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by Jared on 3/8/2017.
 */
public class EasyClassPvPMain extends JavaPlugin{

    @Override
    public void onEnable(){
        GameManager manager = new GameManager();
        getServer().getPluginCommand("ecp").setExecutor(new CommandListener(manager, this));
        PvPEventListener listener = new PvPEventListener(manager);
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginCommand("ecp").setTabCompleter(new CommandTabCompletion(manager, this));

        //PLUGIN DATA
        getConfig().options().copyDefaults(true);
        /*
        getConfig().addDefault("Lobby.Lobby Corner1.x", 0);
        getConfig().addDefault("Lobby.Lobby Corner1.y", 0);
        getConfig().addDefault("Lobby.Lobby Corner2.x", 2);
        getConfig().addDefault("Lobby.Lobby Corner2.y", 2);
        */
        if (!getConfig().contains("Lobby.Spawn.x"))
            getConfig().addDefault("Lobby.Spawn.x", 1);
        if (!getConfig().contains("Lobby.Spawn.y"))
            getConfig().addDefault("Lobby.Spawn.y", 1);
        if (!getConfig().contains("Lobby.Spawn.z"))
            getConfig().addDefault("Lobby.Spawn.z", 1);
        if (getConfig().contains("Game.World")) {
            String worldName = (String) getConfig().get("Game.World");
            ArrayList<World> worldList = (ArrayList<World>) getServer().getWorlds();
            System.out.println("Searching for: " + worldName);
            boolean success = false;
            for (World world : worldList) {
                if (world.getName().equals(worldName)) {
                    manager.gameWorld = getServer().getWorld(worldName);
                    System.out.println(" [ECP] :  o " + world.getName());
                    success = true;
                } else {
                    System.out.println(" [ECP] :  - " + world.getName());
                }
            }
            if (success) {
                System.out.println(" [ECP] : Game world correctly loaded!");
            } else {
                System.out.println(" [ECP] : Game world not found / incorrectly loaded!");
            }
        }
        if (!getConfig().contains("Game.GameLength")){
            getConfig().addDefault("Game.GameLength", 6000);
            System.out.println(" [ECP] : Game time (defaulted): 300 sec");
        } else {
            int time = getConfig().getInt("Game.GameLength");
            manager.totalGameTime = time;
            System.out.println(" [ECP] : Game time (loaded): " + (time / 20) + " sec");
        }
        ArrayList<String> maps = new ArrayList<>();
        maps.addAll(getConfig().getConfigurationSection("Maps").getKeys(false));
        for (String mapName : maps) {
            if (getConfig().get("Maps." + mapName + ".enabled") == null){
                getConfig().addDefault("Maps." + mapName + ".enabled", true);
                System.out.println(" [ECP] : Map \'" + mapName + "\' configured for ECP ver 1.3.2 and up");
            }
        }
        saveConfig();
    }
}
