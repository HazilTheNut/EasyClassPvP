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
public class GeomancerProj extends Projectile {

    GeomancerClass geomancer;

    public GeomancerProj(GeomancerClass wizard, Location start){
        super(start);
        geomancer = wizard;
    }

    @Override
    void playEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 6, .15, .15, .15, new MaterialData(Material.STONE));
    }

    @Override
    public boolean applyHitEffect(Entity target) {
        damageEntity(target, 4);
        return true;
    }

    @Override
    public void endEffect() {
        geomancer.addStoneLoc(loc);
    }
}
