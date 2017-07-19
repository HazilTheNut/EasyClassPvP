package Game.Projectiles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jared on 3/15/2017.
 */
public class EclipseProjEffect extends ProjectileEffect{

    @Override
    public void applyHitEffect(Entity target){
        if (target instanceof LivingEntity) {
            LivingEntity dmgE = (LivingEntity) target;
            dmgE.damage(4);
        }
    }

    @Override
    void playEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 5, .1, .1, .1, 0);
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, .2, .2, .2, 0);
    }
}
