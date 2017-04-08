package Game;

import Game.Classes.PvPClass;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
            if (gamePlayer.getPickedClass().weaponCancellable || hotbarSlot > 0) e.setCancelled(true);
        }
        if (gamePlayer != null && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getState() instanceof Sign){
            Sign clickedSign = (Sign)e.getClickedBlock().getState();
            if (clickedSign.getLine(1).equals("§5[Pick Class]")) gamePlayer.classPickMenu();
        }
    }

    private boolean actionAlignsWithClass(PvPClass givenClass, Action eventAction){
        //return givenClass.ability1IsRightClick == (eventAction.equals(Action.RIGHT_CLICK_AIR) || eventAction.equals(Action.RIGHT_CLICK_BLOCK)); //Right-click aligns
        if (givenClass.ability1IsRightClick && (eventAction.equals(Action.RIGHT_CLICK_AIR) || eventAction.equals(Action.RIGHT_CLICK_BLOCK))) return true;
        if (!givenClass.ability1IsRightClick && (eventAction.equals(Action.LEFT_CLICK_AIR) || eventAction.equals(Action.LEFT_CLICK_BLOCK))) return true;
        return false;
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
                if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                    e.setCancelled(true);
                    return;
                }
                if (gamePlayer.getPlayer().getHealth() - e.getDamage() < 1){ //If the player were to die
                    manager.handlePlayerDeath(gamePlayer);
                    e.setCancelled(true);
                } else {
                    gamePlayer.getPickedClass().onReceiveDamage();
                }
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
        if (gamePlayer != null){
            manager.registerFrozenPlayer(gamePlayer);
            System.out.println("Player " + gamePlayer.getPlayerName() + "registered as frozen [ECP]");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        manager.flushFrozenPlayer(e.getPlayer());
    }

    @EventHandler
    public void preCommand(PlayerCommandPreprocessEvent e){
        if (manager.getPlayerFromRoster(e.getPlayer().getName()) != null && !e.getPlayer().isOp()){
            if (e.getMessage().length() > 0 && e.getMessage().substring(0,1).equals("/") && !(e.getMessage().length() >= 4 && e.getMessage().substring(0,4).equals("/ecp"))) {
                e.getPlayer().sendMessage("§a[ECP]§c (Anti-Cheat) commands other than §6/ecp ...§c blocked!§7 Use §f/ecp leave§7 to exit the game and no longer be blocked");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void projectileHitBlock (ProjectileHitEvent e){
        if (e.getEntity() instanceof Arrow){
            if (e.getHitBlock() != null && e.getEntity().getShooter() instanceof Player){
                GamePlayer shooter = manager.getPlayerFromRoster(((Player) e.getEntity().getShooter()).getName());
                if (shooter != null && shooter.gamePlayerValid()){
                    e.getEntity().remove();
                }
            }
        }
    }

    @EventHandler
    public void onInvetoryClick (InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player) {
            GamePlayer gamePlayer = manager.getPlayerFromRoster(e.getWhoClicked().getName());
            if (gamePlayer != null && gamePlayer.pickingClass && e.getCurrentItem() != null){
                String className = e.getCurrentItem().getItemMeta().getDisplayName();
                try {
                    manager.assignClass(gamePlayer, className.substring(4));
                    gamePlayer.getPlayer().closeInventory();
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e1) {
                    gamePlayer.getPlayer().sendMessage("§a[ECP]§c Uh oh! An internal problem occurred trying to set your class! :(");
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryExit (InventoryCloseEvent e){
        GamePlayer gamePlayer = manager.getPlayerFromRoster(e.getPlayer().getName());
        if (gamePlayer != null) gamePlayer.pickingClass = false;
    }

    @EventHandler
    public void onPlayerChangeSign (SignChangeEvent e){
        String[] lines = e.getLines();
        if (lines[1].equals("[Pick Class]") && e.getPlayer().isOp()){
            e.setLine(1, "§5[Pick Class]");
        }
    }
}
