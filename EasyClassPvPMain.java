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
        getServer().getPluginCommand("ecp").setExecutor(new CommandListener(manager));
        PvPEventListener listener = new PvPEventListener(manager);
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
