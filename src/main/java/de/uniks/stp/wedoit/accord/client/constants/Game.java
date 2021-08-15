package de.uniks.stp.wedoit.accord.client.constants;

import java.util.List;

/**
 * This class includes constants for operations in the Accord-game scissors stone paper.
 */
public class Game {
    //game invitation
    public static final String GAME_PREFIX = "!";
    public static final String GAME_INVITE = GAME_PREFIX + "play " + Icons.HANDSHAKE;
    public static final String GAME_ACCEPT = GAME_PREFIX + "Accepts!";
    public static final String GAME_ACCEPTS = GAME_ACCEPT; //GAME_PREFIX + "Is game valid?";
    public static final String GAME_CLOSE = GAME_PREFIX + "play quit";
    public static final String GAME_INGAME = GAME_PREFIX + "Already in game, try again later!";
    public static final String GAME_START = GAME_PREFIX + "Lets start the Game!";
    public static final String GAME_SYSTEM = GAME_PREFIX + "System: Only one game at a time.";
    public static final String GAME_REVENGE = GAME_PREFIX + "revanche";

    //game actions
    public static final String GAME_IMGURL = "/de/uniks/stp/wedoit/accord/client/view/images/game/";
    public static final String GAME_CHOOSINGIMG = GAME_IMGURL + "choosing.png";
    public static final String GAME_CHOOSE_MOVE = "choose ";
    public static final String GAME_ROCK = "rock";
    public static final String GAME_PAPER = "paper";
    public static final String GAME_SCISSORS = "scissors";

    public static final List<String> GAME_NOT_SUPPORTED = List.of("!hangman", "!guess", "!stop", "!imagebot", "!randomimage", "!jokebot", "!tictactoe");
}
