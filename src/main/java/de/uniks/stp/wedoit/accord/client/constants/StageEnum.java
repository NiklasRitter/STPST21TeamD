package de.uniks.stp.wedoit.accord.client.constants;

import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import javafx.stage.Stage;


public enum StageEnum {
    STAGE(){
        @Override
        public void setUpStage(Stage currentStage) {

        }
    },
    GAME_STAGE() {
        @Override
        public void setUpStage(Stage currentStage) {
            currentStage.centerOnScreen();
            if(currentStage.getTitle().equals(LanguageResolver.getString("Rock - Paper - Scissors"))){
            currentStage.setHeight(450);
            currentStage.setWidth(600);
            }else{
                currentStage.setMinHeight(0);
                currentStage.setMinWidth(0);
                currentStage.setHeight(170);
                currentStage.setWidth(370);
            }

        }
    },
    POPUP_STAGE() {
        @Override
        public void setUpStage(Stage currentStage) {
            currentStage.centerOnScreen();
        }
    },
    EMOJI_PICKER_STAGE {
        @Override
        public void setUpStage(Stage currentStage) {
            currentStage.sizeToScene();
        }
    };


    public abstract void setUpStage(Stage currentStage);

}
