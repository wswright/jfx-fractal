package org.example.world;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * This will be the World Creation Screen.
 */
public class WorldCreationScreen extends Stage {
	public WorldCreationScreen() {
		final VBox vboxOuter = new VBox();
		vboxOuter.getChildren().addAll(getUIElements());
		this.setScene(new Scene(vboxOuter, 250,500));
		this.show();
	}

	@org.jetbrains.annotations.NotNull
	@org.jetbrains.annotations.Contract(pure = true)
	private List<Node> getUIElements() {
		final ArrayList<Node> nodes = new ArrayList<>();
		Label lblWorldName = new Label("World Name: ");
		TextField txtWorldName = new TextField("Name your world...");
		lblWorldName.setLabelFor(txtWorldName);
		Button btnCreateWorld = new Button("Create World");

		nodes.add(lblWorldName);
		nodes.add(txtWorldName);
		nodes.add(btnCreateWorld);
		return nodes;
	}
}
