package de.uniks.stp.wedoit.accord.client.constants;

/**
 * An enum class for emoji unicodes
 */
public enum Icons {

    SMILING_EYES("\uD83D\uDE01"),
    HAPPY_TEARS("\uD83D\uDE02"),
    SMILE_OPEN_MOUTH("\uD83D\uDE03"),
    SMILE_CLOSED_MOUTH("\uD83D\uDE04"),
    SMILE_OPEN_MOUTH_CLOSED_EYES_COLD_SWEAT("\uD83D\uDE05"),
    SMILE_OPEN_MOUTH_TIGHTLY_CLOSED_EYES("\uD83D\uDE06"),
    WINKING_FACE("\uD83D\uDE09"),
    DELICIOUS_FOOD_FACE("\uD83D\uDE0B"),
    RELIEVED_FACE("\uD83D\uDE0C"),
    SMILE_WITH_HEART_FACE("\uD83D\uDE0D"),
    SMIRKING_FACE("\uD83D\uDE0F"),
    UNAMUSED_FACE("\uD83D\uDE12"),
    FOLDED_HANDS("\uD83D\uDE4F"),
    RAISING_CELEBRATING_HANDS("\uD83D\uDE4C"),
    RAISING_ONE_HAND("\uD83D\uDE4B"),
    SPEAK_NO_EVIL_MONKEY("\uD83D\uDE4A"),
    HEAR_NO_EVIL_MONKEY("\uD83D\uDE49"),
    SEE_NO_EVIL_MONKEY("\uD83D\uDE48"),
    BOWING_DEEPLY("\uD83D\uDE47"),
    FACE_WITH_OK_GESTURE("\uD83D\uDE46"),
    FACE_WITH_NO_GOOD_GESTURE("\uD83D\uDE45"),
    WEARY_CAT_FACE("\uD83D\uDE40"),
    CRYING_CAT_FACE("\uD83D\uDE3F"),
    POUTING_CAT_FACE("\uD83D\uDE3E"),
    KISSING_CAT_FACE_WITH_CLOSED_EYES("\uD83D\uDE3D"),
    CAT_FACE_WITH_WRY_SMILE("\uD83D\uDE3C"),
    SMILING_CAT_WITH_HEART_ON_EYES("\uD83D\uDE3B"),
    SMILING_CAT_OPEN_MOUTH("\uD83D\uDE3A"),
    FACE_WITH_MEDICAL_MASK("\uD83D\uDE37"),
    DIZZY_FACE("\uD83D\uDE35"),
    FLUSHED_FACE("\uD83D\uDE33"),
    ASTONISHED_FACE("\uD83D\uDE32"),
    FACE_SCREAMING_IN_FEAR("\uD83D\uDE31"),
    FACE_OPEN_MOUTH_COLD_SWEAT("\uD83D\uDE30"),
    LOUDLY_CRYING_FACE("\uD83D\uDE2D"),
    TIRED_FACE("\uD83D\uDE2B"),
    SLEEPY_FACE("\uD83D\uDE2A"),
    WEARY_FACE("\uD83D\uDE29"),
    FEARFUL_FACE("\uD83D\uDE28"),
    COLD_SWEAT("\uD83D\uDE13"),
    RECREATIONAL_VEHICLE("\uD83D\uDE99");

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