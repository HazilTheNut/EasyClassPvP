package Game.Classes;

import Game.Projectiles.ArtificerProj;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jared on 3/11/2017.
 */
public class ArtificerClass extends PvPClass {

    final private int LASER_COOLDOWN = 12;
    private final int TELE_COOLDOWN = 100;

    private int laserCooldown = 0;

    private final String weaponName = "§r§aLaser Cannon";

    private Location teleporter_1_Loc;
    private boolean teleporter_1_Up = false;

    private Location teleporter_2_Loc;
    private boolean teleporter_2_Up = false;


    private int teleport_cooldown = 0;
    private boolean placeTeleOne = true;

    private ArrayList<Location> healthPackLocations = new ArrayList<>();

    public ArtificerClass(){
        ability1_setcd = 4f;
        ability2_setcd = 30f;
        weaponCancellable = true;

        classIcon = Material.DAYLIGHT_DETECTOR;

        ItemStack classWeapon = new ItemStack(Material.GOLD_BARDING, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        String[] weaponDetails = {"Left-click to shoot a laser!"};
        writeWeaponLore(weaponName, weaponMeta, weaponDetails);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.WOOD_PLATE, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Throws a single-use health pack"," that heals your teammates","","§7Max Health Packs at once: §96"};
        writeAbilityLore("Health Pack", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.DAYLIGHT_DETECTOR, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Places a teleportation device at your location.","","Placing a second device links them together","and allows your teammates to pass through them","","§7Teleporter Cooldown Period: §95 §7seconds"};
        writeAbilityLore("Teleportation Device", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void specialTick(){
        //Code for laser cooldown
        if (laserCooldown >= 1) {
            ItemMeta weaponMeta = player.getInventory().getItem(0).getItemMeta();
            laserCooldown--;
            if (laserCooldown > 0) {
                String cdName = "§7[§6";
                for (int ii = 0; ii < LASER_COOLDOWN; ii++) {
                    if (ii == LASER_COOLDOWN - laserCooldown)
                        cdName += "§8";
                    cdName += "==";
                }
                cdName += "§7]";
                weaponMeta.setDisplayName(cdName);
            } else {
                weaponMeta.setDisplayName(weaponName);
            }
            weapon.setItemMeta(weaponMeta);
            player.getInventory().setItem(0, weapon);
        }

        //Code for teleporter
        if (teleport_cooldown < 1){
            if (teleporter_1_Up && teleporter_2_Up){
                Particle toPlay = (manager.redTeam.hasEntry(player.getName())) ? Particle.REDSTONE : Particle.WATER_SPLASH;
                int qty = 1;
                player.getWorld().spawnParticle(toPlay, teleporter_1_Loc, qty, 0.1, 0.1, 0.1, 0);
                player.getWorld().spawnParticle(toPlay, teleporter_2_Loc, qty, 0.1, 0.1, 0.1, 0);
                boolean teleportSuccessful = false;
                Player atTeleOne = getNearestPlayer(teleporter_1_Loc);
                if (atTeleOne != null && !manager.isOnOtherTeam(atTeleOne, getGamePlayer())) {
                    if (!manager.getPlayerFromRoster(atTeleOne.getName()).getPickedClass().inSpawn) {
                        atTeleOne.teleport(teleporter_2_Loc);
                        teleport_cooldown = TELE_COOLDOWN;
                        teleportSuccessful = true;
                    } else {
                        atTeleOne.sendMessage("§cYou can't teleport while spawn-protected!");
                    }
                }
                if (!teleportSuccessful) {
                    Player atTeleTwo = getNearestPlayer(teleporter_2_Loc);
                    if (atTeleTwo != null && !manager.isOnOtherTeam(atTeleTwo, getGamePlayer())) {
                        if (!manager.getPlayerFromRoster(atTeleTwo.getName()).getPickedClass().inSpawn) {
                            atTeleTwo.teleport(teleporter_1_Loc);
                            teleport_cooldown = TELE_COOLDOWN;
                        } else {
                            atTeleTwo.sendMessage("§cYou can't teleport while spawn-protected!");
                        }
                    }
                }
            }
        } else teleport_cooldown--;

        //Code for health packs
        for (int ii = 0; ii < healthPackLocations.size(); ii++){
            Location hpLoc = healthPackLocations.get(ii);
            Particle toPlay = (manager.redTeam.hasEntry(player.getName())) ? Particle.REDSTONE : Particle.WATER_SPLASH;
            player.getWorld().spawnParticle(toPlay, hpLoc, 1, 0.1, 0.1, 0.1, 0);
            Player teammate = getNearestPlayer(hpLoc);
            if (teammate != null){
                teammate.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1));
                destroyHealthPack(ii);
            }
            if (ii >= 6){
                destroyHealthPack(0);
            }
        }
    }

    private Player getNearestPlayer(Location loc) {
        Entity[] nearbys = getNearbyEntities(loc, 0.8);
        double smallestDist = 100;
        Player candidate = null;
        for (Entity e : nearbys){
            if (e.getLocation().distance(loc) < smallestDist && e instanceof Player){
                candidate = (Player)e;
                smallestDist = e.getLocation().distance(loc);
            }
        }
        return candidate;
    }

    public void createHealthPack(Location loc){
        Location blockLoc = loc.getBlock().getLocation().add(0.5,0.25,0.5);
        healthPackLocations.add(blockLoc);
        blockLoc.getBlock().setType(Material.WOOD_PLATE);
    }

    private void destroyHealthPack(int index){
        Location loc = healthPackLocations.get(index);
        loc.getBlock().setType(Material.AIR);
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 20, 0.2, 0.8, 0.2, new MaterialData(Material.WOOD_PLATE));
        healthPackLocations.remove(index);
    }

    @Override
    public void onDeath() {
        if (teleporter_1_Up) teleporter_1_Loc.getBlock().setType(Material.AIR);
        if (teleporter_2_Up) teleporter_2_Loc.getBlock().setType(Material.AIR);
        int numPacks = healthPackLocations.size();
        for (int ii = 0; ii < numPacks; ii++) destroyHealthPack(0);
        teleporter_1_Up = false;
        teleporter_2_Up = false;
    }

    @Override
    public void onLeftClickWeapon() {
        if (laserCooldown < 1) {
            Location laserLoc = player.getEyeLocation();
            for (int ii = 0; ii < 50; ii++) {
                boolean successfulHit = false;
                laserLoc.add(laserLoc.getDirection());
                if (laserLoc.getWorld().getNearbyEntities(laserLoc, 0.4f, 0.4f, 0.4f).size() > 0) {
                    Collection<Entity> hitList = laserLoc.getWorld().getNearbyEntities(laserLoc, .4f, .4f, .4f);
                    for (Entity e : hitList) {
                        if (manager.isOnOtherTeam(e, getGamePlayer())) {
                            damageEnemy(e, 3);
                            successfulHit = true;
                        }
                    }
                } else if (!laserLoc.getBlock().getType().equals(Material.AIR)) { //When it hits something
                    break;
                } else {
                    if (manager.redTeam.hasEntry(player.getName())) //Color-coding laser particles
                        laserLoc.getWorld().spawnParticle(Particle.REDSTONE, laserLoc, 2, 0, 0, 0, 0);
                    else
                        laserLoc.getWorld().spawnParticle(Particle.WATER_DROP, laserLoc, 2, 0, 0, 0, 0);
                }
                if (successfulHit) break;
            }
            laserCooldown = LASER_COOLDOWN;
        }
    }

    @Override
    void loadArmor(){
        genericArmor(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.IRON_BOOTS);
    }

    @Override
    void ability1Effect() { //Health Pack
        manager.createProjectile(player, new ArtificerProj(player.getEyeLocation(), player, manager, this), 50);
    }

    @Override
    void ability2Effect() { //Teleportation Device
        if (player.getWorld().getBlockAt(player.getLocation()).isEmpty()) {
            player.getWorld().getBlockAt(player.getLocation()).setType(Material.DAYLIGHT_DETECTOR);
            if (placeTeleOne){
                if (teleporter_1_Up) teleporter_1_Loc.getBlock().setType(Material.AIR);
                teleporter_1_Loc = player.getLocation().getBlock().getLocation().add(0.5,0.5,0.5); //Place teleportation device 1
                teleporter_1_Up = true;
                placeTeleOne = false;
            } else {
                if (teleporter_2_Up) teleporter_2_Loc.getBlock().setType(Material.AIR);
                teleporter_2_Loc = player.getLocation().getBlock().getLocation().add(0.5,0.5,0.5); //Place teleportation device 2
                teleporter_2_Up = true;
                placeTeleOne = true;
            }
            teleport_cooldown = TELE_COOLDOWN;
        }
    }
}
