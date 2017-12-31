package Game.Projectiles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jared on 3/11/2017.
 */
public class IceLordProj extends Projectile {

    public IceLordProj(Location start) {
        super(start);
    }

    @Override
    public boolean applyHitEffect(Entity target) {
        damageEntity(target, 4);
        LivingEntity le = (LivingEntity)target;
        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 1));
        return false;
    }

    @Override
    void playEffects(Location loc){
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 10, 0.3, 0.3, 0.3, new MaterialData(Material.ICE));
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0, 0, 0, 0);
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 5f, 0.9f);
    }
}
