package de.uniks.stp.wedoit.accord.client.constants;

public class Game {
    //game invitation
    public static final String GAME_PREFIX = "###game### ";
    public static final String GAME_INVITE_TEXT = "Invites you to Rock - Paper - Scissors!";
    public static final String GAME_ACCEPTS = "Accepts!";
    public static final String GAME_INVITE = GAME_PREFIX + GAME_INVITE_TEXT;
    public static final String GAME_ACCEPT = GAME_PREFIX + GAME_ACCEPTS;
    public static final String PLAY_AGAIN = GAME_PREFIX + "Play again";
    public static final String PLAY_AGAIN_ACCEPT = GAME_PREFIX + "Play again Accept";

    //game actions
    public static final String GAME_IMGURL = "/de/uniks/stp/wedoit/accord/client/view/images/game/";
    public static final String GAME_CHOOSINGIMG = GAME_IMGURL + "choosing.png";
    public static final String GAME_ROCK = "Rock";
    public static final String GAME_PAPER = "Paper";
    public static final String GAME_SCISSORS = "Scissors";

}
