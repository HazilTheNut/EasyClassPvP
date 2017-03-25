package Game.Classes;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Created by Jared on 3/11/2017.
 */
public class RangerClass extends PvPClass {

    public RangerClass(){
        ability1_setcd = 6f;
        ability2_setcd = 9f; //Irrelevant given that the cooldown is modified
        ability1IsRightClick = false;
        weaponCancellable = false;

        ItemStack classWeapon = new ItemStack(Material.BOW, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        weaponMeta.setDisplayName("§r§aForester Bow");
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.INK_SACK, 1, (short)2);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Obtain §b3§2 arrows (Max: §95§2)"};
        writeAbilityLore("Forage", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.FEATHER, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Quickly dodges in the","direction you are facing"};
        writeAbilityLore("Dodge", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void loadArmor(){
        genericArmor(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
    }

    @Override
    void ability1Effect() { //Forage
        player.getInventory().addItem(new ItemStack(Material.ARROW, 3));
        int arrowTotal = 0;
        ItemStack[] invItems = player.getInventory().getContents();
        for (ItemStack item : invItems){
            if (item != null && item.getType().equals(Material.ARROW)) arrowTotal += item.getAmount();
        }
        if (arrowTotal > 5){
            player.getInventory().remove(Material.ARROW);
            player.getInventory().addItem((new ItemStack(Material.ARROW, 5)));
        }
    }

    @Override
    void ability2Effect(){ //Dodge
        Vector dir;
        if (player.isOnGround()) {
            dir = player.getLocation().getDirection().normalize().multiply(2f).setY(0.25);
        } else {
            dir = player.getLocation().getDirection().normalize().multiply(1.8f).setY(0);
        }
        player.setVelocity(dir);
    }
}
