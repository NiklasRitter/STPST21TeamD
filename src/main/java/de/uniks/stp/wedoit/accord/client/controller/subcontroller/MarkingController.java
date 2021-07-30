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


public class MarkingController implements Controller {

    private final Channel currentChannel;
    private ObservableList<User> selectUserObservableList;
    private ObservableList<Channel> selectChannelObservableList;
    private ListView<User> lvSelectUser;
    private ListView<Channel> lvSelectChannel;
    private VBox vBoxTextField;
    private int caret = 0;
    private int textLength = 0;
    private ArrayList<AtPositions> atPositions = new ArrayList<>();
    private TextArea textArea;

    public MarkingController(TextArea textArea, Channel channel, VBox vBoxTextField) {
        this.textArea = textArea;
        this.currentChannel = channel;
        this.vBoxTextField = vBoxTextField;
    }

    @Override
    public void init() {
        this.textArea.setOnKeyTyped(this::isMarking);
        lvSelectUser = new ListView<>();
        lvSelectUser.setVisible(false);
        lvSelectUser.setId("lvSelectUser");

        lvSelectChannel = new ListView<>();
        lvSelectChannel.setVisible(false);
        lvSelectChannel.setId("lvSelectChannel");
    }

    @Override
    public void stop() {
        this.textArea.setOnKeyTyped(null);
        selectUserObservableList = null;
        lvSelectUser = null;
        lvSelectChannel = null;
        vBoxTextField = null;
        atPositions = null;
        textArea = null;
    }

    public static class AtPositions {
        int start;
        int end;
        boolean complete;
        String content;

        public AtPositions(int start, int end, String content) {
            this.start = start;
            this.end = end;
            this.complete = false;
            this.content = content;
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

        if (!textArea.getText().contains("@") && !textArea.getText().contains("#")) {
            removeSelectionMenu();
            atPositions = new ArrayList<>();
            textLength = textArea.getLength();
            return;
        }

        AtPositions atHit;

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

            if (atHit != null) {
                if (keyEvent.getCharacter().equals("@") && currentChannel != null) {
                    atHit = new AtPositions(textArea.getCaretPosition() - 1, textArea.getCaretPosition() - 1, "@");
                    atPositions.add(atHit);
                    checkMarkingPossible(atHit.getContent().substring(1), atHit);
                    shiftAtsRight(atHit);
                } else if (keyEvent.getCharacter().equals("#") && currentChannel != null) {
                    atHit = new AtPositions(textArea.getCaretPosition() - 1, textArea.getCaretPosition() - 1, "#");
                    atPositions.add(atHit);
                    checkMarkingPossible(atHit.getContent().substring(1), atHit);
                    shiftAtsRight(atHit);
                } else {
                    deleteOrActivateAt(atHit, true);
                }

            } else {

                AtPositions newAt = null;
                if (keyEvent.getCharacter().equals("@") && !lvSelectUser.isVisible() && currentChannel != null) {
                    newAt = new AtPositions(textArea.getCaretPosition() - 1, textArea.getCaretPosition() - 1, "@");
                    atPositions.add(newAt);

                    initLwSelectUser(lvSelectUser);
                } else if (keyEvent.getCharacter().equals("#") && !lvSelectChannel.isVisible() && currentChannel != null) {
                    newAt = new AtPositions(textArea.getCaretPosition() - 1, textArea.getCaretPosition() - 1, "#");
                    atPositions.add(newAt);

                    initLwSelectChannel(lvSelectChannel);
                }

                shiftAtsRight(newAt);
            }
        }
        textLength = textArea.getLength();
    }

    private void shiftAtsRight(AtPositions currentAt) {
        int caretPosition = caret - 1;
        for (AtPositions at : atPositions) {
            if (at.getStart() >= caretPosition && at != currentAt) {
                at.shiftRight();
            }
        }
    }

    private void shiftAtsLeft(AtPositions currentAt) {
        int caretPosition = caret;
        for (AtPositions at : atPositions) {
            if (at.getStart() > caretPosition && at != currentAt) {
                at.shiftLeft();
            }
        }
    }

    private void deleteOrActivateAt(AtPositions at, boolean contentAdded) {
        if (!at.isComplete()) {
            if (contentAdded) {
                at.setContent(at.getContent() + textArea.getText().charAt(caret - 1));
                at.setEnd(at.getEnd() + 1);
                shiftAtsRight(at);
                checkMarkingPossible(at.getContent().substring(1), at);

            } else {
                if (at.getLength() - 1 <= 0) {
                    shiftAtsLeft(at);
                    atPositions.remove(at);
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

            for (AtPositions atToShift : atPositions) {
                if (atToShift != at && atToShift.getStart() > at.getEnd()) {
                    for (int i = 0; i < at.getLength(); i++) {
                        atToShift.shiftLeft();
                    }
                }
            }
            atPositions.remove(at);
        }
    }

    private AtPositions checkAtHit(boolean contentAdded) {
        int caretPosition = caret - 1;
        if (contentAdded) {
            for (AtPositions at : atPositions) {
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
            for (AtPositions at : atPositions) {
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

    private void initLwSelectUser(ListView<User> lvSelectUser) {

        this.lvSelectUser.setOnMousePressed(this::lvSelectUserOnClick);

        vBoxTextField.getChildren().add(lvSelectUser);

        lvSelectUser.setMinHeight(45);
        lvSelectUser.setPrefHeight(45);
        lvSelectUser.setVisible(true);

        // init list view
        lvSelectUser.setCellFactory(new SelectUserCellFactory());

        ArrayList<User> availableUsers;
        if (currentChannel != null && currentChannel.isPrivileged()) {
            availableUsers = new ArrayList<>(currentChannel.getMembers());
        } else {
            availableUsers = new ArrayList<>(currentChannel.getCategory().getServer().getMembers());
        }

        this.selectUserObservableList = FXCollections.observableList(availableUsers);

        this.selectUserObservableList.sort((Comparator.comparing(User::isOnlineStatus).reversed()
                .thenComparing(User::getName, String::compareToIgnoreCase).reversed()).reversed());

        this.lvSelectUser.setItems(selectUserObservableList);
    }

    private void initLwSelectChannel(ListView<Channel> lvSelectUser) {

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

            User selectedUser = lvSelectUser.getSelectionModel().getSelectedItem();
            String currentText = textArea.getText();

            int correspondingAtPosition = -1;

            for (int i = caret - 1; i >= 0; i--) {
                if (currentText.charAt(i) == '@') {
                    correspondingAtPosition = i;
                    break;
                }
            }

            if (correspondingAtPosition != -1) {

                AtPositions correspondingAt = null;
                for (AtPositions at : atPositions) {
                    if (at.getStart() == correspondingAtPosition) {
                        correspondingAt = at;
                    }
                }

                if (correspondingAt != null) {

                    String firstPart = currentText.substring(0, correspondingAtPosition);
                    String secondPart = currentText.substring(caret);

                    textArea.setText(firstPart + "@" + selectedUser.getName() + secondPart);
                    correspondingAt.setEnd(correspondingAt.getStart() + selectedUser.getName().length());
                    correspondingAt.setContent("@" + selectedUser.getName());

                    correspondingAt.setComplete(true);
                    removeSelectionMenu();

                    for (AtPositions atToShift : atPositions) {
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

    private void checkMarkingPossible(String text, AtPositions at) {

        ArrayList<User> possibleUsers;

        if (currentChannel != null && currentChannel.isPrivileged()) {
            possibleUsers = new ArrayList<>(currentChannel.getMembers());
        } else {
            possibleUsers = new ArrayList<>(currentChannel.getCategory().getServer().getMembers());
        }

        for (User user : possibleUsers) {
            if (!user.getName().contains(text)) {
                selectUserObservableList.remove(user);
            } else if (user.getName().equals(text)) {
                at.setComplete(true);
                break;
            } else if (!selectUserObservableList.contains(user)) {
                selectUserObservableList.add(user);
            }
        }

        if (!lvSelectUser.isVisible() && !at.isComplete() && !selectUserObservableList.isEmpty()) {
            showSelectionMenu();
        }

        if (selectUserObservableList.isEmpty() || at.isComplete()) {
            removeSelectionMenu();
        }
    }

    private void removeSelectionMenu() {
        vBoxTextField.getChildren().remove(lvSelectUser);
        lvSelectUser.setVisible(false);
        this.lvSelectUser.setOnMousePressed(null);
    }

    private void showSelectionMenu() {
        vBoxTextField.getChildren().add(lvSelectUser);
        lvSelectUser.setVisible(true);
        this.lvSelectUser.setOnMousePressed(this::lvSelectUserOnClick);
    }
}
