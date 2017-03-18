package Game;

import Game.Classes.*;
import Game.Projectiles.Projectile;
import Game.Projectiles.ProjectileEffect;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by Jared on 3/11/2017.
 */
public class GameManager {
    private HashMap<String, GamePlayer> playerRoster = new HashMap<>();
    private Map<String, PvPClass> classMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private ArrayList<Projectile> flyingProjectiles = new ArrayList<>();

    private ArrayList<GamePlayer> playerAddQueue = new ArrayList<>();
    private ArrayList<Projectile> projAddQueue = new ArrayList<>();
    private ArrayList<GamePlayer> playerRemoveQueue = new ArrayList<>();
    private ArrayList<Projectile> projRemoveQueue = new ArrayList<>();

    public Team redTeam;
    public Team blueTeam;

    private boolean processingQueues = false;

    public GameManager(){
        // CLASS REGISTRY
        // put class into the map here to register it for use!

        classMap.put("Spectre", new SpectreClass());
        classMap.put("Ranger", new RangerClass());
        classMap.put("IceLord", new IceLordClass());
        classMap.put("Griefer", new GrieferClass());
        classMap.put("AstralMage", new AstralMageClass());

        // END / MISC. STUFF BELOW

        redTeam = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getTeam("Red");
        if (redTeam == null){
            redTeam = Bukkit.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("Red");
            redTeam.setAllowFriendlyFire(false);
        }

        blueTeam = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getTeam("Blue");
        if (blueTeam == null){
            blueTeam = Bukkit.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("Blue");
            blueTeam.setAllowFriendlyFire(false);
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("EasyClassPvP"), () -> {
            while(processingQueues){
                try{
                    Thread.sleep(10); //Being patient
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            for (int ii = 0; ii < playerRoster.keySet().size(); ii++){
                String key = (String)playerRoster.keySet().toArray()[ii];
                GamePlayer player = playerRoster.get(key);
                PvPClass playerClass = player.getPickedClass();
                if (playerClass != null){
                    playerClass.receiveTick();
                }
            }
            for (Projectile projectile : flyingProjectiles){
                projectile.move();
                if (!projectile.loc.getBlock().isEmpty() || projectile.travelDist == 0 || projectile.attemptHit()){
                    projectile.endEffect();
                    removeProjectile(projectile);
                }
            }
            emptyQueues();
        }, 0, 1);
    }

    private void emptyQueues(){
        processingQueues = true;
        for (GamePlayer player : playerRemoveQueue){
            if (playerRoster.containsValue(player)) playerRoster.remove(player.getPlayerName(), player);
            player.departPlayer();
        }
        playerRemoveQueue.clear();
        for (GamePlayer player : playerAddQueue){
            playerRoster.put(player.getPlayerName(), player);
        }
        playerAddQueue.clear();
        for (Projectile proj : projRemoveQueue){
            flyingProjectiles.remove(proj);
        }
        projRemoveQueue.clear();
        for (Projectile proj : projAddQueue){
            flyingProjectiles.add(proj);
        }
        projAddQueue.clear();
        processingQueues = false;
    }

    public void exitPlayer(GamePlayer leaving){
        playerRemoveQueue.add(leaving);
    }

    public void printRoster(CommandSender sender){
        for (int ii = 0; ii < playerRoster.keySet().size(); ii++){
            String key = (String)playerRoster.keySet().toArray()[ii];
            sender.sendMessage(key);
        }
    }

    public void addPlayerToRoster(Player toAdd){
        playerAddQueue.add(new GamePlayer(toAdd));
    }

    public GamePlayer getPlayerFromRoster(String s){
        return playerRoster.get(s);
    }

    public void assignClass(GamePlayer player, String className) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            Class<?> c = classMap.get(className.toLowerCase()).getClass();
                try {
                    PvPClass newClass = (PvPClass) c.newInstance();
                    newClass.setManager(this);
                    player.setPickedClass(newClass);
                } catch (NullPointerException e) {
                    player.getPlayer().sendMessage("§a[ECP]§c Uh oh! An internal problem occurred trying to set your class! :(");
                } finally {
                    player.getPlayer().sendMessage("§a[ECP]§d Class Picked: " + className);
                }
        } catch (NullPointerException e){
            player.getPlayer().sendMessage("§a[ECP]§c Error: Class does not exist!");
            printClassOptions(player.getPlayer());
        }
    }

    public void printClassOptions(Player recipient){
        Set<String> keys = classMap.keySet();
        String manifest = "§a[ECP]§7 Available Classes:§r§o";
        for (String s : keys){
            manifest += " " + s + ",";
        }
        manifest = manifest.substring(0, manifest.length() - 1);
        recipient.sendMessage(manifest);
    }

    public boolean isOnOtherTeam(Entity e, GamePlayer player2){
        if (!(e instanceof Player)) return true;
        if (redTeam.hasEntry(e.getName()) && redTeam.hasEntry(player2.getPlayerName())) return false;
        return !(blueTeam.hasEntry(e.getName()) && blueTeam.hasEntry(player2.getPlayerName()));
    }

    public void createProjectile(Player shooter, ProjectileEffect effect, int travelDist){
        Projectile newProj = new Projectile(shooter.getEyeLocation(), effect);
        newProj.travelDist = travelDist;
        Team shooterTeam = null;
        if (redTeam.hasEntry(shooter.getName())) shooterTeam = redTeam;
        if (blueTeam.hasEntry(shooter.getName())) shooterTeam = blueTeam;
        newProj.creatorTeam = shooterTeam;
        projAddQueue.add(newProj);
    }

    public void removeProjectile(Projectile toRemove){
        projRemoveQueue.add(toRemove);
    }
}
