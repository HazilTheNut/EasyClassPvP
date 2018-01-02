package Game.Projectiles;

import Game.Classes.ArtificerClass;
import Game.GameManager;
import Game.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * Created by Jared on 1/1/2018.
 */
public class ArtificerProj extends Projectile {

    private Player player;
    private GameManager manager;

    private ArtificerClass artificer;

    public ArtificerProj(Location start, Player gamePlayer, GameManager gameManager, ArtificerClass creator) {
        loc = start;
        loc.setY(loc.getY()-0.2f);
        travelVector = start.getDirection();
        player = gamePlayer;
        manager = gameManager;
        artificer = creator;
    }

    @Override
    void playEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 10, 0.1, 0.1, 0.1, new MaterialData(Material.WOOD_PLATE));
        //The stuff below is technically not playing sound or particle effects, but this method is run at every movement of the projectile, and thus allows for 'fake' physics
        travelVector.add(new Vector(0, -0.1, 0));
        if (travelVector.getX() < 0) travelVector.add(new Vector(0.01 , 0, 0)); //Air friction
        if (travelVector.getX() > 0) travelVector.add(new Vector(-0.01, 0, 0));
        if (travelVector.getZ() < 0) travelVector.add(new Vector(0, 0,  0.01));
        if (travelVector.getZ() > 0) travelVector.add(new Vector(0, 0, -0.01));
    }

    @Override
    public void endEffect() {
        if (!loc.getBlock().getType().equals(Material.AIR) && loc.add(0, 1, 0).getBlock().getType().equals(Material.AIR)){ //If projectile hits a wall
            artificer.createHealthPack(loc);
        } else if (!loc.getBlock().getType().equals(Material.AIR)){ //If the effect hit something but failed...
            Vector oppositeVector = travelVector.multiply(-1).setY(0);
            Location startLoc = loc.add(oppositeVector.normalize().multiply(0.5));
            startLoc.setDirection(new Vector(0, 0, 0));
            manager.createProjectile(player, new ArtificerProj(startLoc, player, manager, artificer), 50); //Slide down the wall using a brand new projectile!
        }
    }
}
