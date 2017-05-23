package Game.Projectiles;

import Game.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by Jared on 3/16/2017.
 */
public class SpectreProjEffect extends ProjectileEffect {

    GamePlayer toTeleport;

    public SpectreProjEffect(GamePlayer shooter) {
        toTeleport = shooter;
    }

    @Override
    void finalEffect(Location loc) {
        if (!toTeleport.getPickedClass().inSpawn) {
            Location steppedBackLoc = loc.add(loc.getDirection().multiply(-1));
            toTeleport.getPlayer().teleport(steppedBackLoc);
            loc.getWorld().spawnParticle(Particle.PORTAL, loc, 30, .3, .3, .3, 0);
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 2f, 1f);
        }
    }

    @Override
    void playEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 5, .1, .1, .1, 0);
    }
}
