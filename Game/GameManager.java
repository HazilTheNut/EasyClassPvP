package Game;

import Game.Classes.*;
import Game.Projectiles.Projectile;
import Game.Projectiles.ProjectileEffect;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private ArrayList<GamePlayer> playerRemoveQueue = new ArrayList<>();

    private ArrayList<Projectile> projAddQueue = new ArrayList<>();
    private ArrayList<Projectile> projRemoveQueue = new ArrayList<>();

    private ArrayList<FrozenPlayer> frozenPlayers = new ArrayList<>(); //"Frozen player" are players who left the server in the midst of a game

    private ArrayList<RechargingHealthPack> chargingHP = new ArrayList<>();
    private ArrayList<RechargingHealthPack> HPAdd = new ArrayList<>();
    private ArrayList<RechargingHealthPack> HPRemove = new ArrayList<>();
    public Team redTeam;
    public Team blueTeam;

    private int redTeamPoints = 0;
    private int blueTeamPoints = 0;

    public Location redSpawn;
    public Location blueSpawn;
    public World gameWorld;

    private int gameTimer = 0;
    public int totalGameTime = 120000;

    private int voteCountdown = 0;
    private boolean voting = false;

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
            gameLoop();
        }, 0, 1);
    }

    private void gameLoop(){
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
        for (RechargingHealthPack pack : chargingHP){
            pack.receiveTick();
        }
        chargingHP.removeAll(HPRemove);
        chargingHP.addAll(HPAdd);
        HPAdd.clear();
        HPRemove.clear();
        emptyQueues();
        checkGameTime();
        if (gameTimer >= 0) gameTimer--;
        if (gameTimer == 0) endGame();
        if (voteCountdown > 0) voteCountdown--;
        if (voteCountdown == 0 && voting){
            voting = false;
            compileVotes();
        }
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
                genericPlayerExit(player.getPlayer());
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

    public void registerFrozenPlayer(GamePlayer gonePlayer) {
        playerRemoveQueue.add(gonePlayer);
        frozenPlayers.add(gonePlayer.createFrozen());
    }

    public void printRoster(CommandSender sender){
        for (int ii = 0; ii < playerRoster.keySet().size(); ii++){
            String key = (String)playerRoster.keySet().toArray()[ii];
            sender.sendMessage(" " + key);
        }
        sender.sendMessage("FROZEN:");
        for (FrozenPlayer frozenPlayer : frozenPlayers){
            sender.sendMessage(" " + frozenPlayer.playerName);
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
        return !(e instanceof Player) || redTeam.hasEntry(e.getName()) ^ redTeam.hasEntry(player2.getPlayerName()) || (blueTeam.hasEntry(e.getName()) ^ blueTeam.hasEntry(player2.getPlayerName()));
    }

    public void handlePlayerDeath(GamePlayer gamePlayer){
        gamePlayer.getPickedClass().inSpawn = true;
        gamePlayer.getPlayer().sendMessage("§a[ECP]§7 Returning to spawn...");
        gamePlayer.getPlayer().getWorld().spawnParticle(Particle.LAVA, gamePlayer.getPlayer().getLocation(), 10, .2, .2, .2, 0);
        gamePlayer.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, gamePlayer.getPlayer().getLocation(), 10, .2, .2, .2, 0.1);
        gamePlayer.getPlayer().getWorld().playSound(gamePlayer.getPlayer().getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 1f, 1f);
        gamePlayer.getPlayer().teleport(gamePlayer.gameSpawn);
        gamePlayer.getPlayer().setHealth(20);
        if (redTeam.hasEntry(gamePlayer.getPlayerName())) {
            blueTeamPoints++;
            broadcastToGamePlayers('9', "+1 To Blue Team §2[§9" + blueTeamPoints + "§2 - §c" + redTeamPoints + "§2]");
        }
        if (blueTeam.hasEntry(gamePlayer.getPlayerName())) {
            redTeamPoints++;
            broadcastToGamePlayers('c', "+1 To Red Team §2[§c" + redTeamPoints + "§2 - §9" + blueTeamPoints + "§2]");
        }
    }

    private void broadcastToGamePlayers(char color, String message){
        for (Player player : gameWorld.getPlayers()) player.sendMessage("§a[ECP] §" + color + message);
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

    public int getGameTimer() { return gameTimer; }

    private ArrayList<Vote> votingList = new ArrayList<>();

    public void receiveVote(String mapVote, Player voter) {
        if (gameWorld != null) {
            if (gameTimer < 0 && gameWorld.getPlayers().contains(voter)) {
                if (!voting) {
                    voting = true;
                    voteCountdown = 600;
                    for (Player inLobby : gameWorld.getPlayers())
                        inLobby.sendMessage("§a[ECP]§e Voting begins! do §b/ecp vote <Map Name>§e to cast a vote! Game starts in §a30§a seconds!");
                }
                boolean alreadyVoted = false;
                for (Vote v : votingList) {
                    if (voter.getName().equals(v.playerName)) {
                        alreadyVoted = true;
                        v.mapName = mapVote;
                        break;
                    }
                }
                if (!alreadyVoted) {
                    votingList.add(new Vote(voter.getName(), mapVote));
                    voter.sendMessage("§a[ECP]§6 Voted for §e" + mapVote);
                } else {
                    voter.sendMessage("§a[ECP]§6 Changed vote to §e" + mapVote);
                }
            } else if (!gameWorld.getPlayers().contains(voter)) {
                voter.sendMessage("§a[ECP]§c Error: You are not in the PvP world!");
            } else {
                voter.sendMessage("§a[ECP]§c Error: A game is currently in session!");
            }
        }
    }

    private void compileVotes(){
        HashMap<String, Integer> mapMap = new HashMap<>();
        ArrayList<String> mapList = new ArrayList<>();
        mapList.addAll(Bukkit.getServer().getPluginManager().getPlugin("EasyClassPvP").getConfig().getConfigurationSection("Maps").getKeys(false));
        for (String mapName : mapList) {
            mapMap.put(mapName, 0); //Add all maps to map
            Bukkit.broadcastMessage(mapName);
        }
        for (Vote vote : votingList) {
            if (mapMap.containsKey(vote.mapName))
                mapMap.replace(vote.mapName, mapMap.get(vote.mapName) + 1); //Add up votes
            else
                System.out.println("Invalid vote: " + vote.mapName);
        }
        int topVal = -1;
        String topMap = "";
        Random tieBreaker = new Random();
        for (String mapKey : mapMap.keySet()){
            if (mapMap.get(mapKey) > topVal || (mapMap.get(mapKey) == topVal && tieBreaker.nextBoolean())){
                topVal = mapMap.get(mapKey);
                topMap = mapKey;
            }
            Bukkit.broadcastMessage(mapKey + ": " + mapMap.get(mapKey));
        }
        votingList.clear();
        startGame(topMap);
    }

    public void startGame(String mapName){
        if (gameWorld != null) {
            for (Player player : gameWorld.getPlayers()) player.sendMessage("§a[ECP]§e Map Selected: §f" + mapName);
            String fullPath = "Maps." + mapName;
            Plugin serverPlugin = Bukkit.getServer().getPluginManager().getPlugin("EasyClassPvP");
            ArrayList<String> maps = new ArrayList<>();
            maps.addAll(serverPlugin.getConfig().getConfigurationSection("Maps").getKeys(false));
            if (maps.contains(mapName)) {
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
                player.sendMessage("§a[ECP]§7 use §b/ecp pick <Class Name>§7 to pick a class");
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
        Plugin serverPlugin = Bukkit.getServer().getPluginManager().getPlugin("EasyClassPvP");
        int lobbyX = serverPlugin.getConfig().getInt("Lobby.Spawn.x");
        int lobbyY = serverPlugin.getConfig().getInt("Lobby.Spawn.y");
        int lobbyZ = serverPlugin.getConfig().getInt("Lobby.Spawn.z");
        Location lobbyLoc = new Location(gameWorld, lobbyX, lobbyY, lobbyZ);
        for (String playerName : playerRoster.keySet()){
            Player play = playerRoster.get(playerName).getPlayer();
            play.sendMessage("§a[ECP]§c§l GAME END");
            play.teleport(lobbyLoc);
        }
        for (Player player : gameWorld.getPlayers()){
            player.sendMessage("§a[ECP]§6§n Summary: ");
            player.sendMessage("§6: ");
            if (redTeamPoints > blueTeamPoints){
                player.sendMessage("§6: Final Score: §c" + redTeamPoints + " §6> §9" + blueTeamPoints);
                player.sendMessage("§6: §cRed Team Wins!");
            } else if (blueTeamPoints > redTeamPoints){
                player.sendMessage("§6: Final Score: §9" + blueTeamPoints + " §6> §c" + redTeamPoints);
                player.sendMessage("§6: §9Blue Team Wins!");
            } else {
                player.sendMessage("§6: Final Score: §9" + blueTeamPoints + " §6- §c" + redTeamPoints);
                player.sendMessage("§6: §e§lTIE!");
            }
            player.sendMessage("§6: ");
            player.sendMessage("§6: =================");
            redTeamPoints = 0;
            blueTeamPoints = 0;
            player.setHealth(20);
            player.setFoodLevel(20);
        }
        for (RechargingHealthPack hp : chargingHP) hp.respawn();
        clearRoster();
    }

    void flushFrozenPlayer(Player joining){
        FrozenPlayer frozen = null;
        for (FrozenPlayer test : frozenPlayers){
            if (test.playerName.equals(joining.getName())) frozen = test;
        }
        if (frozen != null) {
            System.out.println("Player " + joining.getName() + "unfreezing [ECP]");
            frozen.imprintOntoPlayer(joining);
            frozenPlayers.remove(frozen);
            genericPlayerExit(joining);
        }
    }

    private void genericPlayerExit(Player exiting){
        exiting.setGameMode(GameMode.SURVIVAL);
        exiting.setHealth(20);
        exiting.setFoodLevel(20);
        for (PotionEffect pe : exiting.getActivePotionEffects()) exiting.removePotionEffect(pe.getType());
        //Teleporting player to lobby
        Plugin serverPlugin = Bukkit.getServer().getPluginManager().getPlugin("EasyClassPvP");
        int lobbyX = serverPlugin.getConfig().getInt("Lobby.Spawn.x");
        int lobbyY = serverPlugin.getConfig().getInt("Lobby.Spawn.y");
        int lobbyZ = serverPlugin.getConfig().getInt("Lobby.Spawn.z");
        Location lobbyLoc = new Location(gameWorld, lobbyX, lobbyY, lobbyZ);
        exiting.teleport(lobbyLoc);
        //End of lobby teleport
        redTeam.removeEntry(exiting.getName());
        blueTeam.removeEntry(exiting.getName());
        exiting.getPlayer().sendMessage("§a[ECP]§7 Returning to lobby...");
    }

    public void healthPickup(Player player){
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1));
        player.setFoodLevel(20);
        HPAdd.add(new RechargingHealthPack(player.getLocation()));
        player.getLocation().getBlock().setType(Material.IRON_PLATE);
    }

    private class RechargingHealthPack{
        Location packLoc;
        int timeRemaining;

        private RechargingHealthPack(Location loc){
            timeRemaining = 200;
            packLoc = loc;
        }

        private void receiveTick(){
            if (timeRemaining == 0){
                respawn();
            }
            timeRemaining--;
        }

        private void respawn(){
            packLoc.getBlock().setType(Material.GOLD_PLATE);
            HPRemove.add(this);
        }
    }

    private class Vote {
        private String playerName;
        private String mapName;

        private Vote(String name, String map){
            playerName = name;
            mapName = map;
        }
    }
}

