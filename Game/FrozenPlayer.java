package Game;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
* Created by Jared on 3/19/2017.
*/
public class FrozenPlayer {

    private ItemStack[] originalInv;
    private ItemStack[] originalArmor;
    private ItemStack[] originalExtras;
    private Location originalSpawn;
    private GameMode originalMode;
    String playerName;

    public FrozenPlayer(ItemStack[] inv, ItemStack[] invArmor, ItemStack[] invExtra, Location spawn, GameMode mode, String name){
        originalInv = inv;
        originalArmor = invArmor;
        originalExtras = invExtra;
        originalSpawn = spawn;
        playerName = name;
        originalMode = mode;
    }

    boolean imprintOntoPlayer( Player player){
        if (player.getName().equals(playerName)){
            player.getInventory().setContents(originalInv);
            player.getInventory().setArmorContents(originalArmor);
            player.getInventory().setExtraContents(originalExtras);
            player.setBedSpawnLocation(originalSpawn);
            player.sendMessage("§a[ECP]§e Your inventory has been restored!");
            return true;
        }
        return false;
    }

    public boolean equals(Object o){
        if (!(o instanceof FrozenPlayer)) return false;
        FrozenPlayer other = (FrozenPlayer)o;
        return playerName.equals(other.playerName);
    }
}
