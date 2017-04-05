package Game.Classes;

import Game.GamePlayer;
import Game.Projectiles.Projectile;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jared on 3/11/2017.
 */
public class CultistClass extends PvPClass {

    private boolean lifePoolActive = false;
    private int lifePoolTimer = 0;

    public CultistClass(){
        ability1_setcd = 1f;
        ability2_setcd = 6f;
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
        String[] ab2Details = {"Dash in the direction you","are facing"};
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
        if (player.isOnGround())
            player.setVelocity(player.getLocation().getDirection().normalize().multiply(2.5).setY(0.25));
        else
            player.setVelocity(player.getLocation().getDirection().normalize().multiply(2.5).setY(-0.1));
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, .5, .5, .5, 0);
    }
}
