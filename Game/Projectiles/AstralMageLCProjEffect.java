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
public class AstralMageLCProjEffect extends ProjectileEffect{

    @Override
    public void applyEffect(Entity target){
        if (target instanceof LivingEntity) {
            LivingEntity dmgE = (LivingEntity) target;
            dmgE.damage(4);
            dmgE.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 0));
        }
    }

    @Override
    void playEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 10, .1, .1, .1, 0);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 5, .2, .2, .2, 0);
    }
}
