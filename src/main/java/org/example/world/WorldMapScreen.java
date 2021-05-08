package org.example.world;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This will be the World Map Screen, which will show the current World Map.
 */
public class WorldMapScreen extends Stage {

    public WorldMapScreen() {
        //Create the contents of the World Map Screen, then show screen (Stage)
        final VBox worldMapContainer = new VBox();
        worldMapContainer.getChildren().add(new Label("This is the World Map Screen."));
        this.setScene(new Scene(worldMapContainer, 250,250));
        this.show();
    }
}
