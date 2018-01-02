package Game.Classes;

import Game.Projectiles.EclipseProj;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Created by Jared on 3/11/2017.
 */
public class EclipseClass extends PvPClass {

    Vector sunDash = null;
    int sunDashTime = 0;
    int nightBladeTimer = 0;

    boolean inSunMode = true;

    public EclipseClass(){
        ability1_setcd = 5f;
        ability2_setcd = 5f;
        weaponCancellable = true;

        classIcon = Material.COAL;

        ItemStack classWeapon = new ItemStack(Material.STONE_SWORD, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {"Using an ability will alternate","between two stances: §esun§7 and §bmoon","","Each stance has their own set","of abilities"};
        writeWeaponLore("Sword of the Skies", weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        showSunModeIcons();
    }

    private void changeStance(){
        inSunMode = !inSunMode;
        if (inSunMode){
            showSunModeIcons();
        } else {
            showMoonModeIcons();
        }
        showCooldownItem(1, ability1_cd);
        showCooldownItem(2, ability2_cd);
    }

    private void showSunModeIcons(){
        ItemStack itemAb1 = new ItemStack(Material.INK_SACK, 1, (short)14);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Shoots a fireball that deals §b4§2 damage","to the first enemy it hits"};
        writeAbilityLore("Fireball §7/ Night Blade", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.INK_SACK, 1, (short)11);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Dashes forward, deals §b4§2 damage","to the first enemy you hit"};
        writeAbilityLore("Sun Dash §7/ Vault", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;

        if (player != null) {
            ItemStack indicator = new ItemStack(Material.YELLOW_GLAZED_TERRACOTTA);
            ItemMeta indicatorMeta = indicator.getItemMeta();
            indicatorMeta.setDisplayName("§eSun Stance");
            indicator.setItemMeta(indicatorMeta);
            player.getInventory().setItem(4, indicator);
        }
    }

    private void showMoonModeIcons(){
        ItemStack itemAb1 = new ItemStack(Material.FLINT, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Sharpens your sword and grants","a movement speed bonus with","every attack for §b4§2 seconds"};
        writeAbilityLore("Night Blade §7/ Fireball", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.INK_SACK, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Jump to a nearby enemy and","vault off of them; deals §b2§2 damage"};
        writeAbilityLore("Vault §7/ Sun Dash", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;

        if (player != null) {
            ItemStack indicator = new ItemStack(Material.BLUE_GLAZED_TERRACOTTA);
            ItemMeta indicatorMeta = indicator.getItemMeta();
            indicatorMeta.setDisplayName("§bMoon Stance");
            indicator.setItemMeta(indicatorMeta);
            player.getInventory().setItem(4, indicator);
        }
    }

    @Override
    void specialTick(){
        if (sunDashTime > 0){
            player.setVelocity(new Vector(sunDash.getX(), 0, sunDash.getZ()));
            double DASH_RADIUS = 1d;
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 30, .5, .75, .5, 0d);
            Entity[] nearList = getNearbyEntities(player.getLocation(), DASH_RADIUS);
            if (nearList.length > 1){
                boolean foundEnemy = false;
                for (Entity e : nearList){
                    if (e instanceof LivingEntity && manager.isOnOtherTeam(e, getGamePlayer())){
                        ((LivingEntity) e).damage(4f);
                        foundEnemy = true;
                    }
                }
                if (foundEnemy) {
                    sunDashTime = 0; //End dash
                    player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getEyeLocation(), 30, DASH_RADIUS, DASH_RADIUS, DASH_RADIUS, 0d, new MaterialData(Material.GOLD_BLOCK));
                }
            } else if (player.getLocation().add(0, -1, 0).getBlock().getType().equals(Material.AIR)){
                sunDashTime = 0; //End dash
            } else {
                sunDashTime--;
            }
        }
        if (nightBladeTimer > 0){
            nightBladeTimer--;
        }
    }

    @Override
    public void onDealDamage(Entity damagee) {
        if (nightBladeTimer > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
    }

    @Override
    void loadArmor(){
        genericArmor(Material.GOLD_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
        colorLeather(player.getInventory().getChestplate());
        colorLeather(player.getInventory().getLeggings());
        colorLeather(player.getInventory().getBoots());
    }

    private void colorLeather(ItemStack toColor){
        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) toColor.getItemMeta();
        leatherMeta.setColor(Color.fromRGB(185, 185, 222));
        toColor.setItemMeta(leatherMeta);
    }

    @Override
    void ability1Effect() { // Fireball / Night Blade
        if (inSunMode){ //Fireball
            manager.createProjectile(player, new EclipseProj(player.getEyeLocation()), 15);
        } else { //Night Blade
            nightBladeTimer = 80;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0));
        }
        changeStance();
    }

    @Override
    void ability2Effect() { // Sun Dash / Vault
        if (inSunMode) { //Sun Dash
            if (!player.getLocation().add(0, -1, 0).getBlock().getType().equals(Material.AIR)) {
                sunDash = player.getLocation().getDirection();
                sunDashTime = 20;
                changeStance();
            } else {
                ability2_cd = 1f;
            }
        } else { //Vault
            Location rayLoc = player.getEyeLocation();
            Vector vaultDirection = player.getEyeLocation().getDirection().normalize().multiply(-2).setY(0.5f);
            if (!player.isOnGround()) vaultDirection.setY(0.2f);
            boolean searchSuccess = false;
            for (int ii = 0; ii < 10 ; ii++){
                rayLoc.add(rayLoc.getDirection().normalize());
                rayLoc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, rayLoc, 5, .1, .1, .1, 0);
                ArrayList<Entity> hitList = (ArrayList<Entity>)rayLoc.getWorld().getNearbyEntities(rayLoc, .4, .4, .4);
                if (hitList.size() > 0){
                    for (Entity e : hitList){
                        if (e instanceof LivingEntity && manager.isOnOtherTeam(e, getGamePlayer())) {
                            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 50, .3, .4, .3, 0);
                            player.teleport(e.getLocation().setDirection(rayLoc.getDirection()));
                            ((LivingEntity) e).damage(2);
                            searchSuccess = true;
                            break;
                        }
                    }
                }
                if (searchSuccess) break;
            }
            if (!searchSuccess) ability2_cd = 1f;
            else {
                player.setVelocity(vaultDirection);
                changeStance();
            }
        }
    }
}
