package Game.Classes;

import Game.Projectiles.AstralMageLCProj;
import Game.Projectiles.AstralMageQProj;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Jared on 3/15/2017.
 */
public class AstralMageClass extends PvPClass {

    private int attack_cd = 0;
    private final int attack_setcd = 15;

    public AstralMageClass(){
        ability1_setcd = 5f;
        ability2_setcd = 11f;

        classIcon = Material.NETHER_STAR;

        ItemStack classWeapon = new ItemStack(Material.STICK, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        //weaponMeta.setDisplayName("§r§aCosmic Staff");
        String[] weaponDetails = {"Shoots a magic spell that","deals §b4§7 damage and","briefly slows"};
        writeWeaponLore("Cosmic Staff", weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.BLAZE_ROD, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Fires a laser that explodes","on impact, dealing §b3§2 damage"};
        writeAbilityLore("Laser Blast", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Creates a blinding flash","that briefly stuns enemies"};
        writeAbilityLore("Flash", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void loadArmor(){
        genericArmor(Material.DIAMOND_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
        colorLeather(player.getInventory().getChestplate());
        colorLeather(player.getInventory().getLeggings());
        colorLeather(player.getInventory().getBoots());
    }

    private void colorLeather(ItemStack toColor){
        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) toColor.getItemMeta();
        leatherMeta.setColor(Color.fromRGB(0x7A52C2));
        toColor.setItemMeta(leatherMeta);
    }

    @Override
    void specialTick(){
        if (attack_cd > 0){
            attack_cd--;
            ItemMeta weaponMeta = weapon.getItemMeta();
            weaponMeta.setDisplayName(getWeaponName());
            weapon.setItemMeta(weaponMeta);
            player.getInventory().setItem(0, weapon);
        }
    }

    private String getWeaponName(){
        if (attack_cd == 0) return "§r§aCosmic Staff";
        String str = "§r§7[§6";
        for (int ii = 0; ii < attack_setcd; ii++){
            if (ii <= (attack_setcd - attack_cd)) str += "=";
            else{
                if(ii == (attack_setcd - attack_cd)+1) str += "§8";
                str += "-";
            }
        }
        str += "§7]";
        return str;
    }

    @Override
    public void onLeftClickWeapon() {
        if (attack_cd == 0) {
            attack_cd = attack_setcd;
            manager.createProjectile(player, new AstralMageLCProj(player.getEyeLocation()), 15);
        }
    }

    @Override
    void ability1Effect() { //Laser Blast
        Location laserLoc = player.getEyeLocation();
        for (int ii = 0; ii < 50; ii++){
            laserLoc.add(laserLoc.getDirection());
            if (!laserLoc.getBlock().isEmpty() || laserLoc.getWorld().getNearbyEntities(laserLoc, 0.4f, 0.4f, 0.4f).size() > 0){
                laserLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, laserLoc, 10, 1, 1, 1, 0);
                laserLoc.getWorld().spawnParticle(Particle.LAVA, laserLoc, 15, 1, 1, 1, 0);
                laserLoc.getWorld().playSound(laserLoc, Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.8f);
                Collection<Entity> hitList = laserLoc.getWorld().getNearbyEntities(laserLoc, 1, 1, 1);
                for (Entity e : hitList){
                    if (manager.isOnOtherTeam(e, getGamePlayer()) && e instanceof Damageable){
                        Damageable dmgE = (Damageable)e;
                        dmgE.damage(3);
                    }
                }
            } else {
                laserLoc.getWorld().spawnParticle(Particle.CRIT, laserLoc, 5, .1, .1, .1, 0);
            }
        }
    }

    @Override
    void ability2Effect(){ //Flash
        manager.createProjectile(player, new AstralMageQProj(player.getEyeLocation()), 7);
    }

    private Set<String> getCorrectTeamEntries(){
        if (manager.blueTeam.getEntries().contains(player.getName())) return manager.blueTeam.getEntries();
        if (manager.redTeam.getEntries().contains(player.getName())) return manager.redTeam.getEntries();
        return null;
    }
}
