package Game;

import Game.Classes.PvPClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Jared on 3/11/2017.
 */
public class GamePlayer {
    private GameManager manager;

    private Player player;
    private PvPClass pickedClass;

    private ItemStack[] originalInv;
    private ItemStack[] originalArmor;
    private ItemStack[] originalExtras;
    private Location originalSpawn;
    private GameMode originalMode;
    public Location gameSpawn;

    public Inventory classPickInv;
    public boolean pickingClass;

    GamePlayer(Player play, GameManager gameManager){
        manager = gameManager;
        player = play;
        if (player != null) {
            originalInv = player.getInventory().getContents();
            originalArmor = player.getInventory().getArmorContents();
            originalExtras = player.getInventory().getExtraContents();
            player.getInventory().clear();
            originalSpawn = player.getBedSpawnLocation();
            originalMode = player.getGameMode();
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage("§a[ECP]§7 Your previous inventory has been saved.");
        }
    }

    public String getPlayerName(){
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

    public Player getPlayer(){ return player;}

    public PvPClass getPickedClass(){ return pickedClass; }

    boolean gamePlayerValid(){ return pickedClass != null && player != null;}

    void departPlayer(){
        player.getInventory().setContents(originalInv);
        player.getInventory().setArmorContents(originalArmor);
        player.getInventory().setExtraContents(originalExtras);
        player.setBedSpawnLocation(originalSpawn);
        player.setGameMode(originalMode);
        player.sendMessage("§a[ECP]§e Your inventory has been restored!");
    }

    FrozenPlayer createFrozen(){
        return new FrozenPlayer(originalInv, originalArmor, originalExtras, originalSpawn, player.getGameMode(), getPlayerName());
    }

    public void classPickMenu() {
        if (pickedClass != null && !pickedClass.inSpawn){
            player.sendMessage("§a[ECP]§c You have left the spawn! If you want to change class,§o you'll have to die first");
            return;
        }
        classPickInv = Bukkit.createInventory(null, 54, "Pick a Class");
        int invLoc = 10;
        for (String name : manager.getClassNameRoster()) {
            if (invLoc % 9 == 6)
                invLoc += 4;
            invLoc++;
            ItemStack icon = new ItemStack(manager.getClassFromMap(name).classIcon);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.setDisplayName("§r§a" + name);
            writeMenuIconLore(icon, iconMeta, name);
            icon.setItemMeta(iconMeta);
            classPickInv.setItem(invLoc, icon);
        }
        player.openInventory(classPickInv);
        pickingClass = true;
    }

    private void writeMenuIconLore(ItemStack item, ItemMeta meta, String className){
        if (player.hasPermission("easyclasspvp.canplay_" + className.toLowerCase())){
            ArrayList<String> loreList = new ArrayList<>();
            String iconLore = "§r§7Available";
            loreList.add(iconLore);
            meta.setLore(loreList);
        } else {
            ArrayList<String> loreList = new ArrayList<>();
            String iconLore = "§r§cLocked";
            loreList.add(iconLore);
            meta.setLore(loreList);
            item.setType(Material.IRON_FENCE);
        }
    }
}
