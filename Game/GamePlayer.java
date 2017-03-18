package Game;

import Game.Classes.PvPClass;
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

    GamePlayer(Player play){
        player = play;
        if (player != null) {
            originalInv = player.getInventory().getContents();
            originalArmor = player.getInventory().getArmorContents();
            originalExtras = player.getInventory().getExtraContents();
            player.getInventory().clear();
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
        pickedClass = picked;
        pickedClass.setPlayer(player);
        pickedClass.loadKit();
        pickedClass.ability1_cd = 2;
        pickedClass.ability2_cd = 2;
    }

    Player getPlayer(){ return player;}

    PvPClass getPickedClass(){ return pickedClass; }

    boolean gamePlayerValid(){ return pickedClass != null && player != null;}

    public void departPlayer(){
        player.getInventory().setContents(originalInv);
        player.getInventory().setArmorContents(originalArmor);
        player.getInventory().setExtraContents(originalExtras);
        player.sendMessage("§a[ECP]§e Your inventory has been restored!");
    }
}
