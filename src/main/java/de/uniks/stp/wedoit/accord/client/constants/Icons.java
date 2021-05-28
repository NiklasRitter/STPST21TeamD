package de.uniks.stp.wedoit.accord.client.constants;

public enum Icons {

    SMILING_EYES("\uD83D\uDE01"),
    HAPPY_TEARS("\uD83D\uDE02"),
    SMILE_OPEN_MOUTH("\uD83D\uDE03"),
    SMILE_CLOSED_MOUTH("\uD83D\uDE04"),
    WINKING_FACE("\uD83D\uDE09"),
    DELICIOUS_FOOD_FACE("\uD83D\uDE0B"),
    SMILE_WITH_HEART_FACE("\uD83D\uDE0D"),
    SMIRKING_FACE("\uD83D\uDE0F"),
    UNAMUSED_FACE("\uD83D\uDE12"),
    COLD_SWEAT("\uD83D\uDE13");

    private final String iconName;

    Icons(String iconName) {
        this.iconName = iconName;
    }

    public String character() {
        return iconName;
    }

    @Override
    public String toString() {
        return iconName.toString();
    }

}