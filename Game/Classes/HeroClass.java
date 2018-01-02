package Game.Classes;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

/**
 * Created by Jared on 3/11/2017.
 */
public class HeroClass extends PvPClass {

    private int empowermentTimer = 0;

    private ItemStack normalWeapon;
    private ItemStack empoweredWeapon;

    public HeroClass(){
        ability1_setcd = 4f;
        ability2_setcd = 20f;
        weaponCancellable = true;

        classIcon = Material.IRON_SWORD;

        normalWeapon = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta weaponMeta = normalWeapon.getItemMeta();
        String[] weaponDetails = {""};
        writeWeaponLore("§r§aSword of Justice", weaponMeta, weaponDetails);
        normalWeapon.setItemMeta(weaponMeta);
        weapon = normalWeapon;

        empoweredWeapon = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta empoweredMeta = empoweredWeapon.getItemMeta();
        writeWeaponLore("§r§aEmpowered Sword of Justice", empoweredMeta, weaponDetails);
        empoweredWeapon.setItemMeta(weaponMeta);

        ItemStack itemAb1 = new ItemStack(Material.FEATHER, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Leaps forwards"};
        writeAbilityLore("Dash", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Upgrades your sword to diamond and","adds splash damage to your attacks","for the next 4 seconds"};
        writeAbilityLore("Empower", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void specialTick(){ //Timer on Empower
        if (empowermentTimer == 1){
            weapon = normalWeapon;
            player.getInventory().setItem(0, normalWeapon);
        }
        if (empowermentTimer > 0) empowermentTimer--;
    }

    @Override
    public void onDealDamage(Entity damagee) {
        if (empowermentTimer > 0){
            Entity[] nearbys = getNearbyEntities(damagee.getLocation(), 2.25);
            for (Entity e : nearbys){
                if (e instanceof LivingEntity && manager.isOnOtherTeam(e, getGamePlayer())){
                    ((LivingEntity) e).damage(3);
                }
            }
            player.getWorld().spawnParticle(Particle.BLOCK_CRACK, damagee.getLocation(), 40, 2.25, 0.1, 2.25, 1, new MaterialData(Material.DIAMOND_BLOCK));
        }

    }

    @Override
    void loadArmor(){
        genericArmor(Material.LEATHER_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
    }

    @Override
    void ability1Effect() { //Dash
        double multiplier = 1.75;
        if (!player.isOnGround()){
            multiplier = 1.1;
        }
        player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(multiplier).setY(0.25));
    }

    @Override
    void ability2Effect() { //Empower
        empowermentTimer = 80;
        weapon = empoweredWeapon;
        player.getInventory().setItem(0, empoweredWeapon);
    }
}
