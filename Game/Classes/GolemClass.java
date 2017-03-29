package Game.Classes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Created by Jared on 3/28/2017.
 */
public class GolemClass extends PvPClass {

    private int grabTimer = 0;
    private Entity caughtEntity;

    private boolean chargingAttack = false;
    private int chargeTimer = 0;

    public GolemClass (){
        ability1_setcd = 8f;
        ability2_setcd = 10f;
        weaponCancellable = false;

        classIcon = Material.STONE_AXE;

        ItemStack classWeapon = new ItemStack(Material.STONE_AXE, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {""};
        writeWeaponLore("§r§aStone Hammer", weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.QUARTZ, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Charges up a powerful attack","over §b3§2 seconds.","§70-1 seconds:§2 None","§c1-2 seconds:§2 10.5 dmg  +Knockback","§62-3 seconds:§2 12.5 dmg ++Knockback","3+  seconds: None"};
        writeAbilityLore("Heavy Smash", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.EMERALD, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Marks a distant enemy,","pulling them towards you","after §b2§2 seconds"};
        writeAbilityLore("Ancient Power", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void specialTick() {
        if (grabTimer > 0){
            grabTimer--;
            if (grabTimer == 0){
                Vector baseVelocity = player.getLocation().toVector().subtract(caughtEntity.getLocation().toVector()).multiply(.25);
                caughtEntity.setVelocity(baseVelocity.add(new Vector(0, 0.2, 0)));
            } else {
                caughtEntity.getWorld().spawnParticle(Particle.CRIT_MAGIC, caughtEntity.getLocation(), 10, .5, .5, .5, 0);
            }
        }
        if (chargingAttack){
            chargeTimer++;
            String axeName;
            ItemMeta meta = weapon.getItemMeta();
            if (chargeTimer < 20) {
                axeName = "§r§7Stone Hammer";
            }
            else if (chargeTimer < 40) {
                axeName = "§r§cStone Hammer";
                meta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
                meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            }
            else if (chargeTimer < 70) {
                if (chargeTimer < 60)
                    axeName = "§r§6Stone Hammer";
                else
                    axeName = "§r§aStone Hammer";
                meta.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
                meta.addEnchant(Enchantment.KNOCKBACK, 5, true);
            } else {
                chargingAttack = false;
                axeName = "§r§aStone Hammer";
            }
            meta.setDisplayName(axeName);
            player.getInventory().getItem(0).setItemMeta(meta);
        }
    }

    @Override
    public void onLeftClickWeapon() {
        chargingAttack = false;
        ItemStack newWeapon = new ItemStack(weapon.getType());
        newWeapon.getItemMeta().setDisplayName("§r§aStone Hammer");
        player.getInventory().setItem(0, newWeapon);
    }

    @Override
    void ability1Effect() { //Heavy Smash
        chargingAttack = true;
        chargeTimer = 0;
    }

    @Override
    public void ability2Effect() { //Ancient Power
        Location laserLoc = player.getEyeLocation();
        caughtEntity = null;
        for (int ii = 0; ii < 50; ii++){
            laserLoc.add(laserLoc.getDirection());
            if (laserLoc.getWorld().getNearbyEntities(laserLoc, 0.4f, 0.4f, 0.4f).size() > 0){
                Collection<Entity> hitList = laserLoc.getWorld().getNearbyEntities(laserLoc, .4f, .4f, .4f);
                for (Entity e : hitList){
                    if (manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName()))){
                        caughtEntity = e;
                        break;
                    }
                }
            } else {
                laserLoc.getWorld().spawnParticle(Particle.CRIT_MAGIC, laserLoc, 2, 0, 0, 0, 0);
            }
        }
        if (caughtEntity != null) {
            grabTimer = 40;
        } else {
            ability2_cd = 4f;
        }
    }

    @Override
    void loadArmor() {
        genericArmor(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
    }
}
