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
        getConfig().addDefault("Lobby.Lobby Corner1", "n");
        getConfig().addDefault("Lobby.Lobby Corner2", "n");
        getConfig().set("Lobby.Lobby Corner1","0,0");
        getConfig().set("Lobby.Lobby Corner2","1,1");
        saveConfig();
    }
}
