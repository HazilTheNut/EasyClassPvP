package Game.Projectiles;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

/**
 * Created by Jared on 3/11/2017.
 */
public abstract class ProjectileEffect {

    boolean piercing = false;

    public void applyEffect(Entity target){}

    void damageEntity(Entity e, int amount){
        Damageable toHit = (Damageable)e;
        toHit.damage(amount,e);
    }

    void playEffects(Location loc){}

    void finalEffect(Location loc){}
}
