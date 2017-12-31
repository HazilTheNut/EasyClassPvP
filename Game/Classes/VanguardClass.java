package Game.Classes;

import Game.Projectiles.Projectile;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jared on 3/11/2017.
 */
public class VanguardClass extends PvPClass {

    private int protectionTime = 0;
    private int chargeTime = 0;

    public VanguardClass(){
        ability1_setcd = 8f;
        ability2_setcd = 12f;

        classIcon = Material.GOLD_INGOT;

        ItemStack classWeapon = new ItemStack(Material.GOLD_SWORD, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {""};
        writeWeaponLore("§r§aCommander's Blade", weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.SHIELD, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Blocks hostile projectiles in an","area around you for §b3§2 seconds"};
        writeAbilityLore("Protector", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.FEATHER, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Gain a §b60%§2 speed boost for §b3§2 seconds","At the end of the charge,","applies a §b12§2 health shield.","","This ability applies to nearby allies"};
        writeAbilityLore("Charge", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void specialTick(){ //Protector and charge effects
        if (protectionTime > 0){
            Entity[] ents = getNearbyEntities(player.getLocation(), 3.5);
            for (Entity e : ents){
                if (e instanceof Arrow) {
                    player.getWorld().spawnParticle(Particle.BLOCK_CRACK, e.getLocation(), 25, .25, .25, .25, new MaterialData(Material.GOLD_BLOCK));
                    e.remove();
                }
            }
            for (Projectile proj : manager.getFlyingProjectiles()){
                if (player.getLocation().distanceSquared(proj.loc) < 7 && !(proj.creatorTeam != null && proj.creatorTeam.hasEntry(player.getName()))){
                    manager.removeProjectile(proj);
                    player.getWorld().spawnParticle(Particle.BLOCK_CRACK, proj.loc, 25, .25, .25, .25, new MaterialData(Material.GOLD_BLOCK));
                }
            }
            player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 0.5, 0), 5, .7, .5, .7);
            protectionTime--;
        }
        if (chargeTime > 0){
            Entity[] ents = getNearbyEntities(player.getLocation(), 3.5);
            for (Entity e : ents){
                if (!manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName())) && e instanceof LivingEntity){
                    LivingEntity toRally = (LivingEntity)e;
                    toRally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2));
                    if (chargeTime == 1) {
                        toRally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 80, 2));
                        e.getWorld().spawnParticle(Particle.BLOCK_CRACK, e.getLocation().add(0, 0.5, 0), 40, .5, .65, .5, new MaterialData(Material.GOLD_BLOCK));
                    }
                }
            }
            for (Entity e : ents){
                if (!manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName())) && e instanceof LivingEntity){
                    LivingEntity toHeal = (LivingEntity) e;
                    toHeal.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15, 3));
                }
            }
            player.getWorld().spawnParticle(Particle.SPELL_INSTANT, player.getLocation().add(0, 0.5, 0), 5, 3, .5, 3, 0);
            chargeTime--;
        }
    }

    @Override
    void loadArmor(){
        genericArmor(Material.IRON_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS);
    }

    @Override
    void ability1Effect() { //Protect
        protectionTime = 60;
    }

    @Override
    void ability2Effect() { //Charge
        //manager.createProjectile(player, new AstralMageLCProj(), 12, true);
        chargeTime = 60;
    }
}
