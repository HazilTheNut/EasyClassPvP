package Game.Projectiles;

import Game.Classes.GeomancerClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.material.MaterialData;

/**
 * Created by Jared on 5/29/2017.
 */
public class GeomancerProjEffect extends ProjectileEffect {

    GeomancerClass geomancer;

    public GeomancerProjEffect (GeomancerClass wizard){
        geomancer = wizard;
    }

    @Override
    void playEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 6, .15, .15, .15, new MaterialData(Material.STONE));
    }

    @Override
    public void applyHitEffect(Entity target) {
        damageEntity(target, 4);
    }

    @Override
    void finalEffect(Location loc) {
        geomancer.addStoneLoc(loc);
    }
}
