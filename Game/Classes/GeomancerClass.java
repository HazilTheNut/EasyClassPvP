package Game.Classes;

import Game.Projectiles.AstralMageLCProjEffect;
import Game.Projectiles.AstralMageQProjEffect;
import Game.Projectiles.GeomancerProjEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 * Created by Jared on 3/15/2017.
 */
public class GeomancerClass extends PvPClass {

    private int attack_cd = 0;
    private final int attack_setcd = 15;

    private ArrayList<Location> stoneLocs = new ArrayList<>();
    private boolean breakingStones = false;

    public GeomancerClass(){
        ability1_setcd = 7f;
        ability2_setcd = 10f;

        classIcon = Material.CLAY_BALL;

        ItemStack classWeapon = new ItemStack(Material.CLAY_BALL, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {"Shoots a magic spell that","deals §b4§7 damage and","creates a stone where it lands","","Max stones: §915§2"};
        writeWeaponLore("Stone Orb", weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.COBBLESTONE, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Destroys all placed stones and","deals §b4§2 damage to enemies","near the breaking stones"};
        writeAbilityLore("Shatter", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.PRISMARINE_CRYSTALS, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Fires a spray of §b8§7 stones"};
        writeAbilityLore("Avalanche", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void loadArmor(){
        genericArmor(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
        colorLeather(player.getInventory().getHelmet());
        colorLeather(player.getInventory().getChestplate());
        colorLeather(player.getInventory().getLeggings());
        colorLeather(player.getInventory().getBoots());
    }

    private void colorLeather(ItemStack toColor){
        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) toColor.getItemMeta();
        leatherMeta.setColor(Color.fromRGB(0xc6beb6));
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
        if (attack_cd == 0) return "§r§aStone Orb";
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
            manager.createProjectile(player, new GeomancerProjEffect(this), 12);
        }
    }

    @Override
    void ability1Effect() { //Shatter
        clearStones(true);
    }

    @Override
    public void onDeath() { //Shatter effect on death, no damage dealt
        clearStones(false);
    }

    private void breakStone(Location loc, boolean dealDamage){
        loc.getBlock().setType(Material.AIR);
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 20, .5, .5, .5, new MaterialData(Material.COBBLESTONE));
        if (dealDamage) {
            Entity[] hitList = getNearbyEntities(loc, 1);
            for (Entity e : hitList) {
                if (manager.isOnOtherTeam(e, manager.getPlayerFromRoster(player.getName())) && e instanceof LivingEntity) {
                    ((LivingEntity) e).damage(4);
                }
            }
        }
    }

    private void clearStones(boolean dealDamage){
        breakingStones = true;
        for (Location loc : stoneLocs){
            breakStone(loc, dealDamage);
        }
        stoneLocs.clear();
        breakingStones = false;
    }

    @Override
    void ability2Effect(){ //Avalanche
        for (int ii = 0; ii < 8; ii++){
            Location loc = player.getEyeLocation();
            Random random = new Random();
            loc.setDirection(loc.getDirection().normalize().multiply(1.75).add(new Vector(0.5 - random.nextDouble(), 0.5 - random.nextDouble(), 0.5 - random.nextDouble())));
            manager.createProjectile(player, loc, new GeomancerProjEffect(this), 12, false);
        }
    }

    private Set<String> getCorrectTeamEntries(){
        if (manager.blueTeam.getEntries().contains(player.getName())) return manager.blueTeam.getEntries();
        if (manager.redTeam.getEntries().contains(player.getName())) return manager.redTeam.getEntries();
        return null;
    }

    public void addStoneLoc (Location stoneLoc){
        if (!breakingStones){
            Location steppedBackLoc = stoneLoc.add(stoneLoc.getDirection().multiply(-1));
            if (steppedBackLoc.getBlock().getType().equals(Material.AIR)) {
                if (stoneLocs.size() > 15){
                    breakStone(stoneLocs.get(0), false);
                    stoneLocs.remove(0);
                }
                stoneLocs.add(steppedBackLoc);
                steppedBackLoc.getBlock().setType(Material.STONE);
            }
        }
    }
}
