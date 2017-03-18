import Game.PvPEventListener;
import Game.GameManager;
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
        getServer().getPluginCommand("ecp").setTabCompleter(new CommandTabCompletion(manager));

        //PLUGIN DATA
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("Lobby.Lobby Corner1.x", 0);
        getConfig().addDefault("Lobby.Lobby Corner1.y", 0);
        getConfig().addDefault("Lobby.Lobby Corner2.x", 2);
        getConfig().addDefault("Lobby.Lobby Corner2.y", 2);
        getConfig().addDefault("Lobby.Lobby Spawn.x", 1);
        getConfig().addDefault("Lobby.Lobby Spawn.y", 1);
        saveConfig();
    }
}
