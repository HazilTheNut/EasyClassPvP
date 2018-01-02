package Game.Classes;

import Game.Projectiles.GeomancerProj;
import org.bukkit.*;
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
    private int avalancheTimer = 0;

    public GeomancerClass(){
        ability1_setcd = 7f;
        ability2_setcd = 10f;

        classIcon = Material.CLAY_BALL;

        ItemStack classWeapon = new ItemStack(Material.CLAY_BALL, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {"Shoots a magic spell that","deals §b4§7 damage and","creates a §estone§7 where it lands","","Max stones: §915§2"};
        writeWeaponLore("Stone Orb", weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.COBBLESTONE, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Destroys all placed §estones§2 and","deals §b4§2 damage and slows by §b20%§2","to nearby enemies"};
        writeAbilityLore("Shatter", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.PRISMARINE_CRYSTALS, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Fires a barrage of §b9 §estones§r","","Resets cooldown of Shatter"};
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
        leatherMeta.setColor(Color.fromRGB(0xbaaea9));
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
        if (avalancheTimer > 0) {
            if (avalancheTimer % 10 == 0) {
                for (int ii = 0; ii < 3; ii++) {
                    Location loc = player.getEyeLocation();
                    Random random = new Random();
                    loc.setDirection(loc.getDirection().normalize().multiply(2.75).add(new Vector(0.5 - random.nextDouble(), 0.5 - random.nextDouble(), 0.5 - random.nextDouble())));
                    manager.createProjectile(player, new GeomancerProj(this, player.getEyeLocation()), 12, false);
                }
            }
            avalancheTimer--;
            if (avalancheTimer == 0 && ability1_cd > 0.05f) ability1_cd = 0.05f;
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
            manager.createProjectile(player, new GeomancerProj(this, player.getEyeLocation()), 12, false);
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
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 20, .75, .75, .75, new MaterialData(Material.COBBLESTONE));
        if (dealDamage) {
            Entity[] hitList = getNearbyEntities(loc, 1);
            for (Entity e : hitList) {
                if (manager.isOnOtherTeam(e, getGamePlayer()) && e instanceof LivingEntity) {
                    ((LivingEntity) e).damage(4);
                    ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
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
        avalancheTimer = 30;
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
