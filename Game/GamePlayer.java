package Game;

import Game.Classes.PvPClass;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
/**
 * Created by Jared on 3/11/2017.
 */
public class GamePlayer {

    private Player player;
    private PvPClass pickedClass;

    private ItemStack[] originalInv;
    private ItemStack[] originalArmor;
    private ItemStack[] originalExtras;
    private Location originalSpawn;
    public Location gameSpawn;

    GamePlayer(Player play){
        player = play;
        if (player != null) {
            originalInv = player.getInventory().getContents();
            originalArmor = player.getInventory().getArmorContents();
            originalExtras = player.getInventory().getExtraContents();
            player.getInventory().clear();
            originalSpawn = player.getBedSpawnLocation();
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage("§a[ECP]§7 Your previous inventory has been saved.");
        }
    }

    String getPlayerName(){
        if (player == null){
            return "null";
        } else {
            return player.getName();
        }
    }

    void setPickedClass(PvPClass picked){
        if (pickedClass == null || pickedClass.inSpawn) {
            pickedClass = picked;
            pickedClass.setPlayer(player);
            pickedClass.loadKit();
            pickedClass.ability1_cd = 2;
            pickedClass.ability2_cd = 2;
            String className = picked.getClass().getSimpleName();
            player.sendMessage("§a[ECP]§d Class Picked: " + className.substring(0, className.length() - 5));
        } else {
            player.sendMessage("§a[ECP]§c You've left the spawn! §oYou'll have to die if you want to change classes");
        }
    }

    Player getPlayer(){ return player;}

    PvPClass getPickedClass(){ return pickedClass; }

    boolean gamePlayerValid(){ return pickedClass != null && player != null;}

    void departPlayer(){
        player.getInventory().setContents(originalInv);
        player.getInventory().setArmorContents(originalArmor);
        player.getInventory().setExtraContents(originalExtras);
        player.setBedSpawnLocation(originalSpawn);
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage("§a[ECP]§e Your inventory has been restored!");
    }
}
