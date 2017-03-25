package Game.Classes;

import Game.Projectiles.IceLordProjEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jared on 3/11/2017.
 */
public class IceLordClass extends PvPClass {

    private short armorHealth = 0;
    private boolean armorActive = false;

    public IceLordClass(){
        ability1_setcd = 5f;
        ability2_setcd = 8f; //Irrelevant given that IceLord modifies its own cooldown

        ItemStack classWeapon = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        weaponMeta.setDisplayName("§r§aGlacial Shard");
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.DIAMOND, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Fires an ice blast that","deals §b4§2 damage and","slows by §b20%§2 for §b2.5§2 seconds"};
        writeAbilityLore("Ice Blast", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta(); //Writing Lore
        String[] ab2Details = {"Creates frost armor that","reduces damage taken,","but slows you by §b20%§2","",
                "The armor breaks upon using","this ability again or taking","too much damage"};
        writeAbilityLore("Frost Armor", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void loadArmor() {
        if (!armorActive) {
            genericArmor(Material.IRON_HELMET, Material.DIAMOND_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.IRON_BOOTS);
        } else {
            genericArmor(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);
        }
    }

    private void createArmorBreakEffect(){
        Location spawnLoc = player.getLocation();
        spawnLoc.setY(spawnLoc.getY() + 1);
        player.getLocation().getWorld().spawnParticle(Particle.BLOCK_CRACK, spawnLoc, 50, 0.6, 0.5, 0.6, new MaterialData(Material.ICE));
        player.getWorld().playSound(spawnLoc, Sound.BLOCK_GLASS_BREAK, 5f, 1.25f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15, 0));
        player.removePotionEffect(PotionEffectType.SLOW);
        armorActive = false;
        ability2Icon.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
        loadArmor();
    }

    @Override
    void specialTick(){
        if (armorActive){
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 15, 0));
        }
    }

    @Override
    public void onReceiveDamage(){
        if (armorHealth > 0) armorHealth--;
        if (armorHealth == 0 && armorActive){
            ability2_cd = 8f;
            showCooldownItem(2, ability2_cd);
            createArmorBreakEffect();
        }
    }

    @Override
    void ability1Effect() { //Ice Blast
        manager.createProjectile(player, new IceLordProjEffect(), 12);
    }

    @Override
    void ability2Effect(){ //Frost Armor
        if (armorHealth < 3) {
            armorActive = true;
            armorHealth = 3;
            ability2_setcd = 2f;
            ability2Icon.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            loadArmor();
        }
        else {
            createArmorBreakEffect();
            armorHealth = 0;
            ability2_setcd = 8f;
        }
    }
}
