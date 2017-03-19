package Game.Projectiles;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Created by Jared on 3/11/2017.
 */
public class Projectile {
    public Location loc;
    ProjectileEffect projEffect;
    public int travelDist;
    public Team creatorTeam;

    private Vector travelVector;

    public Projectile(Location start,  ProjectileEffect effect){
        loc = start;
        loc.setY(loc.getY()-0.2f);
        projEffect = effect;
        travelVector = loc.getDirection().normalize();
        //travelVector.multiply(1.0f);
    }

    public void move(){
        loc.add(travelVector);
        projEffect.playEffects(loc);
        travelDist--;
    }

    public boolean attemptHit(){
        boolean successful = false;
        Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, 0.4, 0.4, 0.4);
        for (Entity e : nearbyEntities){
            if (!(e instanceof Player && creatorTeam != null && creatorTeam.hasEntry(e.getName())) && !e.isInvulnerable()){
                projEffect.applyEffect(e);
                successful = !projEffect.piercing;
            }
        }
        return successful;
    }

    public void endEffect(){ projEffect.finalEffect(loc); }
}
