package Game;

import Game.Classes.*;
import Game.Projectiles.Projectile;
import Game.Projectiles.ProjectileEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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

    public Location redSpawn;
    public Location blueSpawn;
    public World gameWorld;

    private int gameTimer = 0;
    public int totalGameTime = 120000;

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
            checkGameTime();
            if (gameTimer >= 0) gameTimer--;
            if (gameTimer == 0) endGame();
        }, 0, 1);
    }

    private void checkGameTime(){
        if (gameTimer % 6000 == 0){
            broadcastToRoster(gameTimer / 6000 + "min remaining");
        }
        if (gameTimer == 3600){
            broadcastToRoster("3min remaining");
        }
        if (gameTimer == 2400){
            broadcastToRoster("2min remaining");
        }
        if (gameTimer == 1200){
            broadcastToRoster("1min remaining");
        }
        if (gameTimer == 600){
            broadcastToRoster("30 seconds remaining!");
        }
        if (gameTimer < 200 && gameTimer % 20 == 0){
            if (gameTimer > 20)
                broadcastToRoster(gameTimer / 20 + " seconds remaining!");
            else
                broadcastToRoster("1 second remains!");
        }
    }

    private void broadcastToRoster(String s){
        for (int ii = 0; ii < playerRoster.keySet().size(); ii++){
            String key = (String)playerRoster.keySet().toArray()[ii];
            GamePlayer player = playerRoster.get(key);
            player.getPlayer().sendMessage("§a[ECP]§6 " + s);
        }
    }

    private void emptyQueues(){
        processingQueues = true;
        for (GamePlayer player : playerRemoveQueue){
            if (player != null){
                player.departPlayer();
                if (playerRoster.containsValue(player)) playerRoster.remove(player.getPlayerName(), player);
            }
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
            sender.sendMessage(" " + key);
        }
    }

    public void addPlayerToRoster(Player toAdd){
        playerAddQueue.add(new GamePlayer(toAdd));
    }

    private void clearRoster(){
        for (int ii = 0; ii < playerRoster.keySet().size(); ii++){
            String key = (String)playerRoster.keySet().toArray()[ii];
            GamePlayer player = playerRoster.get(key);
            playerRemoveQueue.add(player);
        }
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

    public ArrayList<String> getClassNameRoster(){
        Set<String> keys = classMap.keySet();
        ArrayList<String> translatedKeys = new ArrayList<>();
        for (String s : keys){
            translatedKeys.add(s);
        }
        return translatedKeys;
    }

    public boolean isOnOtherTeam(Entity e, GamePlayer player2) {
        if (!(e instanceof Player)) return true;
        return redTeam.hasEntry(e.getName()) ^ redTeam.hasEntry(player2.getPlayerName()) || (blueTeam.hasEntry(e.getName()) ^ blueTeam.hasEntry(player2.getPlayerName()));
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

    private void removeProjectile(Projectile toRemove){
        projRemoveQueue.add(toRemove);
    }

    public void startGame(String mapName){
        if (gameWorld != null) {
            String fullPath = "Maps." + mapName;
            Plugin serverPlugin = Bukkit.getServer().getPluginManager().getPlugin("EasyClassPvP");
            if (serverPlugin.getConfig().contains(fullPath)) {
                redSpawn = new Location(gameWorld, (int) serverPlugin.getConfig().get(fullPath + ".redX"), (int) serverPlugin.getConfig().get(fullPath + ".redY"), (int) serverPlugin.getConfig().get(fullPath + ".redZ"));
                blueSpawn = new Location(gameWorld, (int) serverPlugin.getConfig().get(fullPath + ".blueX"), (int) serverPlugin.getConfig().get(fullPath + ".blueY"), (int) serverPlugin.getConfig().get(fullPath + ".blueZ"));
            }
            ArrayList<Player> players = (ArrayList<Player>) gameWorld.getPlayers();
            clearTeams();
            boolean goToRedTeam = false;
            for (Player player : players) {
                if (goToRedTeam) {
                    redTeam.addEntry(player.getName());
                    player.teleport(redSpawn);

                } else {
                    blueTeam.addEntry(player.getName());
                    player.teleport(blueSpawn);
                    player.setBedSpawnLocation(blueSpawn);
                }
                goToRedTeam = !goToRedTeam;
                player.sendMessage("§a[ECP]§c§l Game Starting... (Time: " + (float)totalGameTime / 1200 + "min)");
                player.sendMessage("§a[ECP]§7use '/ecp pick <Class Name>' to pick a class");
                addPlayerToRoster(player);
            }
            emptyQueues();
            for (int ii = 0; ii < playerRoster.keySet().size(); ii++){
                String key = (String)playerRoster.keySet().toArray()[ii];
                GamePlayer player = playerRoster.get(key);
                if (redTeam.hasEntry(player.getPlayerName())) player.gameSpawn = redSpawn;
                else player.gameSpawn = blueSpawn;
            }
            gameTimer = totalGameTime;
        }
    }

    private void clearTeams(){
        for (String name : redTeam.getEntries()){
            redTeam.removeEntry(name);
        }
        for (String name : blueTeam.getEntries()){
            blueTeam.removeEntry(name);
        }
    }

    private void endGame(){
        clearTeams();
        clearRoster();
        Plugin serverPlugin = Bukkit.getServer().getPluginManager().getPlugin("EasyClassPvP");
        int lobbyX = serverPlugin.getConfig().getInt("Lobby.Spawn.x");
        int lobbyY = serverPlugin.getConfig().getInt("Lobby.Spawn.y");
        int lobbyZ = serverPlugin.getConfig().getInt("Lobby.Spawn.z");
        Location lobbyLoc = new Location(gameWorld, lobbyX, lobbyY, lobbyZ);
        ArrayList<Player> players = (ArrayList<Player>)gameWorld.getPlayers();
        for (Player play : players){
            play.sendMessage("§a[ECP]§c§l GAME END");
            play.teleport(lobbyLoc);
        }
    }
}
