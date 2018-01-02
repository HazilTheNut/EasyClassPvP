package Game.Projectiles;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;
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
    public int travelDist;
    public Team creatorTeam;

    Vector travelVector;

    public Projectile(Location start){
        loc = start;
        loc.setY(loc.getY()-0.2f);
        travelVector = loc.getDirection().normalize();
    }

    public Projectile(){} //For special uses

    public void move(){
        loc.add(travelVector);
        playEffects(loc);
        travelDist--;
    }

    public boolean attemptHit(){
        Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, 0.4, 0.4, 0.4);
        for (Entity e : nearbyEntities){
            if (isHittable(e)){
                return applyHitEffect(e);
            }
        }
        return false;
    }

    protected boolean isHittable(Entity e){ return !(e instanceof Player && creatorTeam != null && creatorTeam.hasEntry(e.getName())) && !e.isInvulnerable(); }

    public void endEffect(){}

    public boolean applyHitEffect(Entity target){ return false; }

    void damageEntity(Entity e, int amount){
        Damageable toHit = (Damageable)e;
        toHit.damage(amount,e);
    }

    void playEffects(Location loc){}
}
