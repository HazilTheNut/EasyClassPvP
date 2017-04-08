import Game.PvPEventListener;
import Game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
        if (getConfig().contains("Game.World")){
            String worldName = (String)getConfig().get("Game.World");
            manager.gameWorld = Bukkit.getWorld(worldName);
            System.out.println(" [ECP] : Game World loaded!");
        } else {
            System.out.println(" [ECP] : WARNING: Game World not set, game disabled until lobby is set! (Hint: /ecp setlobbyspawn)");
        }
        if (!getConfig().contains("Game.GameLength")){
            getConfig().addDefault("Game.GameLength", 6000);
            System.out.println(" [ECP] : Game time (defaulted): 300 sec");
        } else {
            int time = getConfig().getInt("Game.GameLength");
            manager.totalGameTime = time;
            System.out.println(" [ECP] : Game time (loaded): " + (time / 20) + " sec");
        }
        saveConfig();
    }
}
