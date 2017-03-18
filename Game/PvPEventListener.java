package Game;

import Game.Classes.PvPClass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Jared on 3/11/2017.
 */
public class PvPEventListener implements Listener {
    GameManager manager;

    public PvPEventListener(GameManager gameManager){
        manager = gameManager;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e){
        GamePlayer gamePlayer = manager.getPlayerFromRoster(e.getPlayer().getName());
        if (gamePlayer != null && gamePlayer.gamePlayerValid()){
            int hotbarSlot = gamePlayer.getPlayer().getInventory().getHeldItemSlot();
            if ((actionAlignsWithClass(gamePlayer.getPickedClass(), e.getAction()) && hotbarSlot == 0))
                gamePlayer.getPickedClass().useAbility1();
            else if (actionIsRightClick(e.getAction())) {
                if (hotbarSlot == 1)
                    gamePlayer.getPickedClass().useAbility1();
                if (hotbarSlot == 2)
                    gamePlayer.getPickedClass().useAbility2();
            }
            if ((e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) && hotbarSlot == 0){
                gamePlayer.getPickedClass().onLeftClickWeapon();
            }
            if (gamePlayer.getPickedClass().weaponCancellable || hotbarSlot > 0)e.setCancelled(true);
        }
    }

    private boolean actionAlignsWithClass(PvPClass givenClass, Action eventAction){
        return givenClass.ability1IsRightClick == (eventAction.equals(Action.RIGHT_CLICK_AIR) || eventAction.equals(Action.RIGHT_CLICK_BLOCK)); //Right-click aligns
    }

    private boolean actionIsRightClick(Action eventAction){
        return eventAction.equals(Action.RIGHT_CLICK_AIR) || eventAction.equals(Action.RIGHT_CLICK_BLOCK);
    }

    @EventHandler
    public void onPlayerThrowEvent(PlayerDropItemEvent e){
        GamePlayer gamePlayer = manager.getPlayerFromRoster(e.getPlayer().getName());
        if (gamePlayer != null && gamePlayer.gamePlayerValid()){
            e.setCancelled(true);
            int hotbarSlot = gamePlayer.getPlayer().getInventory().getHeldItemSlot();
            if (hotbarSlot == 0)
                gamePlayer.getPickedClass().useAbility2();
        }
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player){
            Player gotHit = (Player)e.getEntity();
            GamePlayer gamePlayer = manager.getPlayerFromRoster(gotHit.getName());
            if (gamePlayer != null && gamePlayer.gamePlayerValid()){
                gamePlayer.getPickedClass().onReceiveDamage();
            }
        }
    }

    @EventHandler
    public void playerInflictDamage(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player) {
            Player attacker = (Player) e.getDamager();
            GamePlayer player = manager.getPlayerFromRoster(attacker.getName());
            if (player != null && player.gamePlayerValid()){
                player.getPickedClass().onDealDamage(e.getEntity());
            }
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        GamePlayer gamePlayer = manager.getPlayerFromRoster(e.getPlayer().getName());
        manager.exitPlayer(gamePlayer);
    }


}
