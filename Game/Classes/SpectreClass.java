package Game.Classes;

import Game.GamePlayer;
import Game.Projectiles.SpectreProj;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jared on 3/16/2017.
 */
public class SpectreClass extends PvPClass {

    private int armorCheckTimer = 0;

    public SpectreClass(){
        ability1_setcd = 4f;
        ability2_setcd = 7f;

        classIcon = Material.ENDER_PEARL;

        ItemStack classWeapon = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta weaponMeta = classWeapon.getItemMeta();
        weaponMeta.setDisplayName("§r§aEthereal Sword");
        weaponMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        classWeapon.setItemMeta(weaponMeta);
        weapon = classWeapon;

        ItemStack itemAb1 = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta ab1Meta = itemAb1.getItemMeta();
        String[] ab1Details = {"Become immune to damage","for §b1.5§2 seconds and gain §b40%§2 speed","While invincible, your sword is gone"};
        writeAbilityLore("Surfaceless", ab1Meta, true, ab1Details, (int)ability1_setcd);
        itemAb1.setItemMeta(ab1Meta);
        ability1Icon = itemAb1;

        ItemStack itemAb2 = new ItemStack(Material.ENDER_PEARL, 1);
        ItemMeta ab2Meta = itemAb2.getItemMeta();
        String[] ab2Details = {"Fires a beam that teleports"," you to it when it hits something"," or reaches its max range"};
        writeAbilityLore("Warp", ab2Meta, false, ab2Details, (int)ability2_setcd);
        itemAb2.setItemMeta(ab2Meta);
        ability2Icon = itemAb2;
    }

    @Override
    void loadArmor() {
        genericArmor(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.AIR, Material.AIR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 0));
    }

    @Override
    void specialTick() {
        if (armorCheckTimer == 0){
            if (invincbilityPeriod <= 0) {
                loadArmor();
                armorCheckTimer = 30;
            }
        } else {
            armorCheckTimer--;
        }
        if (invincbilityPeriod > 0) player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 0.75, 0), 25, 0.4, 0.7, 0.4, 0);
        if (invincbilityPeriod == 1) player.getInventory().setItem(0, weapon);
    }

    @Override
    void ability1Effect() {
        invincbilityPeriod = 30;
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        armorCheckTimer = 30;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1));
        player.getInventory().remove(weapon);
    }

    @Override
    void ability2Effect() {
        GamePlayer gamePlayer = getGamePlayer();
        if (gamePlayer != null) {
            manager.createProjectile(player, new SpectreProj(getGamePlayer(), player.getEyeLocation()), 30);
        }
    }
}
