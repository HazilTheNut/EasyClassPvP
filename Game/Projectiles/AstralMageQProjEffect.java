package Game.Projectiles;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Jared on 3/15/2017.
 */
public class AstralMageQProjEffect extends ProjectileEffect{

    Set<String> teamEntries;

    public AstralMageQProjEffect(Set<String> entries) {
        piercing = true;
        teamEntries = entries;
    }

    @Override
    void finalEffect(Location loc){
        Collection<Entity> aoeHits = loc.getWorld().getNearbyEntities(loc, 1.25, 1.25, 1.25);
        for (Entity target : aoeHits) {
            if (target instanceof LivingEntity && !(teamEntries != null && teamEntries.contains(target.getName()))){
                LivingEntity dmgE = (LivingEntity)target;
                dmgE.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 3));
                dmgE.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 3));
                dmgE.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 50, 3));
            }
        }
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 45, 1.5, 1.5, 1.5, new MaterialData(Material.GLOWSTONE));
        loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_TWINKLE, 2f, 1.75f);
    }

    @Override
    void playEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 10, .1, .1, .1, 0);
        //loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 5, .2, .2, .2, 0);
    }
}
