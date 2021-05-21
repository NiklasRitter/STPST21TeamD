package de.uniks.stp.wedoit.accord.client.controller;

/**
 * Interface for Controller
 * Used for easier handling of Controller
 */
public interface Controller {

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     *
     * Load necessary GUI elements
     * Add action listeners
     */
    void init();


    /**
     * Called to stop this controller
     *
     * Remove action listeners
     */
    void stop();
}
