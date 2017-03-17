package Game;

import Game.Classes.PvPClass;
import org.bukkit.entity.Player;

/**
 * Created by Jared on 3/11/2017.
 */
public class GamePlayer {

    private Player player;
    private PvPClass pickedClass;

    GamePlayer(Player play){
        player = play;
    }

    String getPlayerName(){
        if (player == null){
            return "null";
        } else {
            return player.getName();
        }
    }

    void setPickedClass(PvPClass picked){
        pickedClass = picked;
        pickedClass.setPlayer(player);
        pickedClass.loadKit();
        pickedClass.ability1_cd = 2;
        pickedClass.ability2_cd = 2;
    }

    Player getPlayer(){ return player;}

    PvPClass getPickedClass(){ return pickedClass; }

    boolean gamePlayerValid(){ return pickedClass != null && player != null;}
}
