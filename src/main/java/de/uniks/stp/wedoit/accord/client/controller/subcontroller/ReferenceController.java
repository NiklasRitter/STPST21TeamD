package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.view.SelectChannelCellFactory;
import de.uniks.stp.wedoit.accord.client.view.SelectUserCellFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;



public class ReferenceController implements Controller {

    private final Channel currentChannel;
    private ObservableList<Channel> selectChannelObservableList;
    private ListView<Channel> lvSelectChannel;
    private VBox vBoxTextField;
    private int caret = 0;
    private int textLength = 0;
    private ArrayList<ReferencePositions> referencePositions = new ArrayList<>();
    private TextArea textArea;

    public ReferenceController(TextArea textArea, Channel channel, VBox vBoxTextField) {
        this.textArea = textArea;
        this.currentChannel = channel;
        this.vBoxTextField = vBoxTextField;
    }

    @Override
    public void init() {
        this.textArea.setOnKeyReleased(this::isMarking);
        lvSelectChannel = new ListView<>();
        lvSelectChannel.setVisible(false);
        lvSelectChannel.setId("lvSelectChannel");
    }

    @Override
    public void stop() {
        this.textArea.setOnKeyReleased(null);
        selectChannelObservableList = null;
        lvSelectChannel = null;
        vBoxTextField = null;
        referencePositions = null;
        textArea = null;
    }

    public static class ReferencePositions {
        int start;
        int end;
        boolean complete;
        String content;

        public ReferencePositions(int start, int end) {
            this.start = start;
            this.end = end;
            this.complete = false;
            this.content = "#";
        }

        public void shiftLeft() {
            this.start = this.start - 1;
            this.end = this.end - 1;
        }

        public void shiftRight() {
            this.start = this.start + 1;
            this.end = this.end + 1;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getContent() {
            return content;
        }

        public boolean isComplete() {
            return complete;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public void setComplete(boolean complete) {
            this.complete = complete;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getLength() {
            return content.length();
        }
    }

    private void isMarking(KeyEvent keyEvent) {
        caret = textArea.getCaretPosition();
        if (!textArea.getText().contains("#")) {
            removeSelectionMenu();
            referencePositions = new ArrayList<>();
            textLength = textArea.getLength();
            return;
        }

        ReferencePositions atHit;

        if (textArea.getText().length() < textLength) {
            //character deleted

            atHit = checkAtHit(false);

            if (atHit != null) {
                deleteOrActivateAt(atHit, false);

            } else {
                shiftAtsLeft(atHit);
            }

        } else if (textArea.getText().length() > textLength) {
            //character added

            atHit = checkAtHit(true);
            System.out.println(keyEvent.getCharacter());
            if (atHit != null) {
                if (keyEvent.getCharacter().equals("#") && currentChannel != null) {
                    atHit = new ReferencePositions(textArea.getCaretPosition() - 1, textArea.getCaretPosition() - 1);
                    referencePositions.add(atHit);
                    checkMarkingPossible(atHit.getContent().substring(1), atHit);
                    shiftAtsRight(atHit);
                } else {
                    deleteOrActivateAt(atHit, true);
                }

            } else {

                ReferencePositions newAt = null;
                if (keyEvent.getCharacter().equals("#") && !lvSelectChannel.isVisible() && currentChannel != null) {
                    newAt = new ReferencePositions(textArea.getCaretPosition() - 1, textArea.getCaretPosition() - 1);
                    referencePositions.add(newAt);

                    initLwSelectUser(lvSelectChannel);
                }

                shiftAtsRight(newAt);
            }
        }
        textLength = textArea.getLength();
    }
    ///////////////
    private void shiftAtsRight(ReferencePositions currentAt) {
        int caretPosition = caret - 1;
        for (ReferencePositions at : referencePositions) {
            if (at.getStart() >= caretPosition && at != currentAt) {
                at.shiftRight();
            }
        }
    }
    ///////////////
    private void shiftAtsLeft(ReferencePositions currentAt) {
        int caretPosition = caret;
        for (ReferencePositions at : referencePositions) {
            if (at.getStart() > caretPosition && at != currentAt) {
                at.shiftLeft();
            }
        }
    }
    ///////////////
    private void deleteOrActivateAt(ReferencePositions at, boolean contentAdded) {
        if (!at.isComplete()) {
            if (contentAdded) {
                at.setContent(at.getContent() + textArea.getText().charAt(caret - 1));
                at.setEnd(at.getEnd() + 1);
                shiftAtsRight(at);
                checkMarkingPossible(at.getContent().substring(1), at);

            } else {
                if (at.getLength() - 1 <= 0) {
                    shiftAtsLeft(at);
                    referencePositions.remove(at);
                } else {
                    at.setContent(at.getContent().substring(0, at.getLength() - 1));
                    at.setEnd(at.getEnd() - 1);
                    shiftAtsLeft(at);
                    checkMarkingPossible(at.getContent().substring(1), at);
                }
            }
        } else {
            String currentText = textArea.getText();
            String start = currentText.substring(0, at.getStart());

            //check if correct start is cut
            String end;
            if (contentAdded) {
                end = currentText.substring(at.getEnd() + 2);
            } else {
                end = currentText.substring(at.getEnd());
            }

            textArea.setText(start + end);
            textArea.positionCaret(start.length());

            for (ReferencePositions atToShift : referencePositions) {
                if (atToShift != at && atToShift.getStart() > at.getEnd()) {
                    for (int i = 0; i < at.getLength(); i++) {
                        atToShift.shiftLeft();
                    }
                }
            }
            referencePositions.remove(at);
        }
    }
    ///////////////
    private ReferencePositions checkAtHit(boolean contentAdded) {
        int caretPosition = caret - 1;
        if (contentAdded) {
            for (ReferencePositions at : referencePositions) {
                if (!at.isComplete()) {
                    if (caretPosition > at.getStart() && caretPosition - 1 <= at.getEnd()) {
                        return at;
                    }
                } else {
                    if (caretPosition > at.getStart() && caretPosition <= at.getEnd()) {
                        return at;
                    }
                }
            }
        } else {
            for (ReferencePositions at : referencePositions) {
                if (!at.isComplete()) {
                    if (caretPosition + 1 >= at.getStart() && caretPosition <= at.getEnd()) {
                        return at;
                    }
                } else {
                    if (caretPosition >= at.getStart() && caretPosition <= at.getEnd() - 1) {
                        return at;
                    }
                }
            }
        }
        return null;
    }

    private void initLwSelectUser(ListView<Channel> lvSelectUser) {

        this.lvSelectChannel.setOnMousePressed(this::lvSelectUserOnClick);

        vBoxTextField.getChildren().add(lvSelectUser);

        lvSelectUser.setMinHeight(45);
        lvSelectUser.setPrefHeight(45);
        lvSelectUser.setVisible(true);

        // init list view
        lvSelectUser.setCellFactory(new SelectChannelCellFactory());

        ArrayList<Channel> possibleChannels = new ArrayList<>();

        for (Category category: currentChannel.getCategory().getServer().getCategories()) {
            possibleChannels.addAll(category.getChannels());
        }

        this.selectChannelObservableList = FXCollections.observableList(possibleChannels);

        this.selectChannelObservableList.sort(Comparator.comparing(Channel::getName, String::compareToIgnoreCase).reversed());

        this.lvSelectChannel.setItems(selectChannelObservableList);
    }

    private void lvSelectUserOnClick(MouseEvent mouseEvent) {

        if (mouseEvent.getClickCount() == 1) {

            Channel selectedUser = lvSelectChannel.getSelectionModel().getSelectedItem();
            String currentText = textArea.getText();

            int correspondingAtPosition = -1;

            for (int i = caret - 1; i >= 0; i--) {
                if (currentText.charAt(i) == '#') {
                    correspondingAtPosition = i;
                    break;
                }
            }

            if (correspondingAtPosition != -1) {

                ReferencePositions correspondingAt = null;
                for (ReferencePositions at : referencePositions) {
                    if (at.getStart() == correspondingAtPosition) {
                        correspondingAt = at;
                    }
                }

                if (correspondingAt != null) {

                    String firstPart = currentText.substring(0, correspondingAtPosition);
                    String secondPart = currentText.substring(caret);

                    textArea.setText(firstPart + "#" + selectedUser.getName() + secondPart);
                    correspondingAt.setEnd(correspondingAt.getStart() + selectedUser.getName().length());
                    correspondingAt.setContent("#" + selectedUser.getName());

                    correspondingAt.setComplete(true);
                    removeSelectionMenu();

                    for (ReferencePositions atToShift : referencePositions) {
                        if (atToShift.getStart() > correspondingAt.getStart()) {
                            for (int i = 0; i < textArea.getText().length() - currentText.length(); i++) {
                                atToShift.shiftRight();
                            }
                        }
                    }

                    textArea.positionCaret(correspondingAt.getEnd() + 1);
                }
            }
        }
        textLength = textArea.getLength();
    }

    private void checkMarkingPossible(String text, ReferencePositions at) {
        ArrayList<Channel> possibleChannels = new ArrayList<>();

        for (Category category: currentChannel.getCategory().getServer().getCategories()) {
            possibleChannels.addAll(category.getChannels());
        }

        for (Channel channel : possibleChannels) {
            if (!channel.getName().contains(text)) {
                selectChannelObservableList.remove(channel);
            } else if (channel.getName().equals(text)) {
                at.setComplete(true);
                break;
            } else if (!selectChannelObservableList.contains(channel)) {
                selectChannelObservableList.add(channel);
            }
        }

        if (!lvSelectChannel.isVisible() && !at.isComplete() && !selectChannelObservableList.isEmpty()) {
            showSelectionMenu();
        }

        if (selectChannelObservableList.isEmpty() || at.isComplete()) {
            removeSelectionMenu();
        }
    }
    ///////////////
    private void removeSelectionMenu() {
        vBoxTextField.getChildren().remove(lvSelectChannel);
        lvSelectChannel.setVisible(false);
        this.lvSelectChannel.setOnMousePressed(null);
    }
    ///////////////
    private void showSelectionMenu() {
        vBoxTextField.getChildren().add(lvSelectChannel);
        lvSelectChannel.setVisible(true);
        this.lvSelectChannel.setOnMousePressed(this::lvSelectUserOnClick);
    }
}
