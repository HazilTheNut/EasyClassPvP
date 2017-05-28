package Game.Classes;

import Game.GamePlayer;
import Game.Projectiles.Projectile;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
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
public class CultistClass extends PvPClass {

    private boolean lifePoolActive = false;
    private int lifePoolTimer = 0;

    public CultistClass(){
        ability1_setcd = 1f;
        ability2_setcd = 5f;
        weaponCancellable = false;

        classIcon = Material.GHAST_TEAR;

        ItemStack classWeapon = new ItemStack(Material.STONE_SWORD, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {"Damaging enemies restores health"};
        writeWeaponLore("§r§aSiphoning Dagger", weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.INK_SACK, 1, (short)1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Toggle to damage yourself while","healing your teammates"};
        writeAbilityLore("Life Pool", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.STRING, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Nearby players at","low health are marked","with a red cloud","","Activate to teleport to them.","","Ability works on both teams"};
        writeAbilityLore("Leap", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void specialTick(){ //Life Pool effect
        if (lifePoolActive){
            if (lifePoolTimer <= 0) {
                Entity[] ents = getNearbyEntities(player.getLocation(), 3.5);
                for (Entity e : ents){
                    if (!manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName())) && e instanceof LivingEntity){
                        LivingEntity toHeal = (LivingEntity) e;
                        toHeal.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15, 3));
                    }
                }
                player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 0.5, 0), 75, 3, .5, 3);
                player.removePotionEffect(PotionEffectType.REGENERATION);
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20, 5));
                lifePoolTimer = 30;
            } else {
                lifePoolTimer--;
            }
        }
        ArrayList<Entity> entList = (ArrayList<Entity>)player.getLocation().getWorld().getNearbyEntities(player.getLocation(), 15, 15, 15);
        for (Entity e : entList){
            if (e instanceof LivingEntity && ((LivingEntity) e).getHealth() < 5d) {
                e.getWorld().spawnParticle(Particle.REDSTONE, e.getLocation().add(0, 2, 0), 5, .05, .05, .05, 0);
            }
        }
    }

    @Override
    public void onDealDamage(Entity damagee) {
        if (player.getHealth() <= 19) player.setHealth(player.getHealth() + 1);
    }

    @Override
    void loadArmor(){
        genericArmor(Material.IRON_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
        colorLeather(player.getInventory().getChestplate());
        colorLeather(player.getInventory().getLeggings());
        colorLeather(player.getInventory().getBoots());
    }

    private void colorLeather(ItemStack toColor){
        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) toColor.getItemMeta();
        leatherMeta.setColor(Color.fromRGB(60, 0, 0));
        toColor.setItemMeta(leatherMeta);
    }

    @Override
    void ability1Effect() { //Life Pool
        lifePoolActive = !lifePoolActive;
        if (lifePoolActive) {
            lifePoolTimer = 0;
        }
    }

    @Override
    void ability2Effect() { //Leap
        Location rayLoc = player.getEyeLocation();
        boolean searchSuccess = false;
        for (int ii = 0; ii < 20 ; ii++){
            rayLoc.add(rayLoc.getDirection().normalize());
            ArrayList<Entity> hitList = (ArrayList<Entity>)rayLoc.getWorld().getNearbyEntities(rayLoc, .4, .4, .4);
            if (hitList.size() > 0){
                for (Entity e : hitList){
                    if (e instanceof LivingEntity && ((LivingEntity)e).getHealth() < 5d){
                        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(), 50, .3, .4, .3, 0);
                        player.teleport(e.getLocation());
                        searchSuccess = true;
                        break;
                    }
                }
            }
            if (searchSuccess) break;
        }
        if (!searchSuccess) ability2_cd = 1f;
    }
}
