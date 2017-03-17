package Game.Classes;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

/**
 * Created by Jared on 3/11/2017.
 */
public class RangerClass extends PvPClass {


    private Location trapLocation;
    private boolean trapActive = false;

    public RangerClass(){
        ability1_setcd = 6f;
        ability2_setcd = 2f;
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

        ItemStack itemAb2 = new ItemStack(Material.WOOD_PLATE, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Sets a hidden trap at your location","","If an enemy steps over it, they","are blinded and revealed to you"
                ,"","If there already is a trap,","the trap will instead be moved"};
        writeAbilityLore("Trap", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void loadArmor(){
        genericArmor(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
    }

    @Override
    void specialTick(){ //Trap clock
        if (trapActive){
            Entity[] unluckyEntities = getNearbyEntities(trapLocation, .5);
            if (unluckyEntities.length > 0) {
                ArrayList<Entity> sortedEntities = new ArrayList<>();
                for (Entity e : unluckyEntities) {
                    if (manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName()))) {
                        sortedEntities.add(e);
                    }
                }
                for (Entity e : sortedEntities) {
                    if (e instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity) e;
                        le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0));
                    }
                }
                if (sortedEntities.size() > 0){
                    trapActive = false;
                    trapLocation.getWorld().playSound(trapLocation, Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1.2f);
                    trapLocation.getWorld().spawnParticle(Particle.SPELL, trapLocation.add(0, .25, 0), 40, 1, .1, 1, 0);
                }
            }
        }
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
    void ability2Effect(){ //Trap
        trapLocation = player.getLocation();
        trapActive = true;
        trapLocation.getWorld().spawnParticle(Particle.CLOUD, trapLocation, 2, 0, 0, 0, 0);
        player.playSound(trapLocation, Sound.ITEM_ARMOR_EQUIP_CHAIN, 2f, 1.2f);
    }
}
