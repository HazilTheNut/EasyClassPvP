package Game.Classes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

/**
 * Created by Jared on 3/11/2017.
 */
public class GrieferClass extends PvPClass {

    private boolean crashLanding = false;

    public GrieferClass(){
        ability1_setcd = 6f;
        ability2_setcd = 12f;

        ItemStack classWeapon = new ItemStack(Material.BLAZE_ROD, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {"Hitting an enemy that's on","fire consumes the fire to","deal §b5§2 bonus damage"};
        writeWeaponLore("§r§aPyro Staff", weaponMeta, weaponDetails);
        weaponMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.BLAZE_POWDER, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Sets enemies in front","you on fire for §b6§2 seconds"};
        writeAbilityLore("Pocket Fire", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.FIREWORK, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Jumps up into the air.","","Deals §b6§2 damage to","enemies nearby when you land"};
        writeAbilityLore("Crash Landing", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void specialTick(){ //Testing for landing of Crash Landing
        if (crashLanding){
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 5, 0.1, 0.1, 0.1, 0);
            if (player.isOnGround()){
                explode();
                crashLanding = false;
            }
        }
    }

    @Override
    void loadArmor(){
        genericArmor(Material.LEATHER_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.LEATHER_BOOTS);
    }

    @Override
    public void onDealDamage(Entity damagee){ //Pyro Staff special effect
        if (damagee.getFireTicks() > 0 && player.getInventory().getHeldItemSlot() == 0){
            Damageable dmgE = (Damageable)damagee;
            dmgE.damage(5);
            damagee.setFireTicks(0);
            damagee.getLocation().getWorld().spawnParticle(Particle.LAVA, damagee.getLocation().add(0, .75, 0), 8, 0.25d, 0.25d, 0.25d, 0);
            damagee.getLocation().getWorld().playSound(damagee.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3f, 1f);
        }
    }

    private void explode(){ //Explosion effect of Crash Landing
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 30, 2, 2, 2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.9f);
        Entity[] dmgList = getNearbyEntities(player.getLocation(), 2);
        for (Entity e : dmgList){
            if (manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName())) && e instanceof Damageable){
                Damageable toHit = (Damageable)e;
                toHit.damage(6);
            }
        }
    }

    @Override
    void ability1Effect() { //Pocket Fire
        Vector playerProjection = player.getEyeLocation().getDirection().normalize().multiply(2.5f);
        Location effectLoc = player.getLocation().add(playerProjection);
        Entity[] hitList = getNearbyEntities(effectLoc, 2);
        for (Entity e : hitList){
            if (manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName())) && e instanceof Damageable) e.setFireTicks(120);
        }
        effectLoc.getWorld().spawnParticle(Particle.FLAME, effectLoc, 50, 1.5d, 1.5d, 1.5d, 0);
        effectLoc.getWorld().spawnParticle(Particle.LAVA, effectLoc, 10, 0.75d, 0.75d, 0.75d, 0);
    }

    @Override
    void ability2Effect(){ //Initiate Crash Landing
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 5f, 1f);
        player.setVelocity(player.getLocation().getDirection().normalize().multiply(0.5d).setY(1.1d));
        crashLanding = true;
    }
}
