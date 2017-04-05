package Game.Classes;

import Game.GameManager;
import Game.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jared on 3/11/2017.
 */
public class PvPClass {
    public float ability1_cd = 0f;
    public float ability2_cd = 0f;
    float ability1_setcd = 3f; //Stats that define ability cooldown
    float ability2_setcd = 3f;

    private float timeAmount = 0.05f;

    public boolean inSpawn = true;

    int ability1_cdflash = 0;
    int ability2_cdflash = 0;

    int invincbilityPeriod = 0;

    public boolean ability1IsRightClick = true;
    public boolean weaponCancellable = true;

    Player player;
    GameManager manager;

    ItemStack weapon = new ItemStack(Material.WOOD_SWORD); //Override with new ones
    ItemStack ability1Icon = new ItemStack(Material.STICK); // ^
    ItemStack ability2Icon = new ItemStack(Material.STICK); // ^
    public Material classIcon = Material.IRON_SWORD;

    void specialTick(){ /* Special code ran for different classes */ }

    void ability1Effect(){ /* Override with custom stuff */}

    void ability2Effect(){ /* Override with custom stuff */}

    public void onDealDamage(Entity damagee){ /* Override with custom stuff*/}

    public void onReceiveDamage(){ /* Override with custom stuff */}

    public void onLeftClickWeapon(){ /* Override with custom stuff */}

    public void receiveTick(){
        if (player != null) {
            player.setFoodLevel(20);
            specialTick();
            countdownAbility1();
            countdownAbility2();
            boolean previousInvulnerability = player.isInvulnerable();
            if (invincbilityPeriod > 0) {
                player.setInvulnerable(true);
                invincbilityPeriod--;
            } else if (inSpawn) {
                player.setInvulnerable(true);
            } else {
                player.setInvulnerable(false);
            }
            if (player.isInvulnerable() != previousInvulnerability) {
                if (player.isInvulnerable()) {
                    ItemStack invulnItem = new ItemStack(Material.SLIME_BALL);
                    ItemMeta meta = invulnItem.getItemMeta();
                    meta.setDisplayName("§6Invulnerable");
                    invulnItem.setItemMeta(meta);
                    player.getInventory().setItem(8, invulnItem);
                } else {
                    ItemStack invulnItem = new ItemStack(Material.FIREWORK_CHARGE);
                    ItemMeta meta = invulnItem.getItemMeta();
                    meta.setDisplayName("§eNot Invulnerable");
                    invulnItem.setItemMeta(meta);
                    player.getInventory().setItem(8, invulnItem);
                }
            }
            if (player.getLocation().getBlock().getType().equals(Material.CARPET)) {
                if (inSpawn) {
                    inSpawn = false;
                    player.sendMessage("§a[ECP]§7 Exiting spawn");
                }
                if ((player.getLocation().getBlock().getData() == 11 && manager.redTeam.hasEntry(player.getName())) ||
                   (player.getLocation().getBlock().getData() == 14 && manager.blueTeam.hasEntry(player.getName()))){
                    player.setInvulnerable(false);
                    player.damage(100);
                    player.sendMessage("§a[ECP]§c Cannot enter other team's spawn!");
                }
            }
            if (player.getLocation().getBlock().getType().equals(Material.GOLD_PLATE)){
                manager.healthPickup(player);
            }
        }
    }

    public void setPlayer(Player set){ player = set; }

    public void setManager(GameManager gameManager) {manager = gameManager;}

    public void loadKit(){
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.getInventory().clear();
        player.getInventory().setItem(0, weapon);
        showCooldownItem(1, ability1_cd);
        showCooldownItem(2, ability2_cd);
        loadArmor();
    }

    void loadArmor(){ /*Override, loads armor onto char*/}

    void genericArmor(Material head, Material chest, Material legs, Material feet){
        player.getInventory().setHelmet(new ItemStack(head));
        player.getInventory().setChestplate(new ItemStack(chest));
        player.getInventory().setLeggings(new ItemStack(legs));
        player.getInventory().setBoots(new ItemStack(feet));
    }

    void writeWeaponLore(String name, ItemMeta meta, String[] weaponDetails){
        ArrayList<String> lore = new ArrayList<>();
        for (String s : weaponDetails){
            lore.add(" §r§7" + s);
        }
        meta.setLore(lore);
        meta.setDisplayName("§a" + name);
        if (player != null) player.sendMessage("Icon created!");
    }

    void writeAbilityLore(String name, ItemMeta meta, boolean isAbility1, String[] abilityDetails, int cooldown){
        ArrayList<String> lore = new ArrayList<>();
        if (isAbility1) //For header
            if (ability1IsRightClick) //Ability 1 is right-click, right-click header
                lore.add("§r§7Right-click main weapon to use");
            else //Otherwise, left-click header
                lore.add("§r§7Left-click main weapon to use");
        else //Toss item header
            lore.add("§r§7Toss [Q] main weapon to use");
        for (String s : abilityDetails){
            lore.add(" §r§2" + s);
        }
        lore.add("");
        lore.add(" §r§7COOLDOWN: §9" + String.valueOf(cooldown) + "§7 seconds");
        meta.setLore(lore);
        meta.setDisplayName("§6" + name);
        if (player != null) player.sendMessage("Icon created!");
    }

    void showCooldownItem(int loc, float cd){
        if (cd > 0f) {
            ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, (int)Math.ceil(cd), getCooldownItemMaterial(loc));
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.setDisplayName(String.format("§7COOLDOWN (§c%1$d§7)", glass.getAmount()));
            glass.setItemMeta(glassMeta);
            player.getInventory().setItem(loc, glass);
        } else if (loc == 1){
            player.getInventory().setItem(loc, ability1Icon);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 5f, 1f);
        } else {
            player.getInventory().setItem(loc, ability2Icon);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 5f, 0.5f);
        }
    }

    private short getCooldownItemMaterial(int loc){
        int flashAmount;
        if (loc == 1){
            flashAmount = ability1_cdflash;
        } else {
            flashAmount = ability2_cdflash;
        }
        if (flashAmount <= 0){
            return 9;
        } else if (flashAmount <= 5){
            return 3;
        } else {
            return 0;
        }
    }

    private void countdownAbility1(){
        if (ability1_cdflash > 0) ability1_cdflash--;
        if (ability1_cd > 0f) {
            ability1_cd -= timeAmount;
            showCooldownItem(1, ability1_cd);
        }
    }

    private void countdownAbility2(){
        if (ability2_cdflash > 0) ability2_cdflash--;
        if (ability2_cd > 0f) {
            ability2_cd -= timeAmount;
            showCooldownItem(2, ability2_cd);
        }
    }

    public void useAbility1(){
        //player.sendMessage(this.getClass().getName());
        if (ability1_cd < 0.05f && !inSpawn) {
            player.getInventory().setItem(0, weapon);
            ability1Effect();
            if (ability1_cd < 0.05f) ability1_cd = ability1_setcd;
            showCooldownItem(1, ability1_cd);
        } else {
            if (inSpawn) ability1_cd = 0.5f;
            ability1_cdflash = 10;
        }
    }

    public void useAbility2() {
        if (ability2_cd < 0.05f && !inSpawn) {
            ability2Effect();
            if (ability2_cd < 0.05f) ability2_cd = ability2_setcd;
            showCooldownItem(2, ability2_cd);
        } else {
            if (inSpawn) ability2_cd = 0.5f;
            ability2_cdflash = 10;
        }
    }

    Entity[] getNearbyEntities(Location l, double radius) { return getNearbyEntities(l, radius, false); }

    Entity[] getNearbyEntities(Location l, double radius, boolean includeInvincibles) {
        Collection<Entity> findList = l.getWorld().getNearbyEntities(l, radius, radius, radius);
        Entity[] finalList = new Entity[findList.size()];
        int ii = 0;
        for (Entity e : findList){
            if (includeInvincibles || !e.isInvulnerable()){
                finalList[ii] = e;
                ii++;
            }
        }
        return finalList;
    }
}
