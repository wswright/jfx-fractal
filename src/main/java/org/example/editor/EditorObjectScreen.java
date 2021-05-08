package org.example.editor;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import org.example.game.GameObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This screen will display the objects in the scene graph.
 * TODO: Think about allowing edits.
 */
public class EditorObjectScreen extends Stage {
	Label lblCount = new Label("? Objects...");
	TableView<GameObject> items = new TableView<>();
	TableView<Transform> selectionTransforms = new TableView<>();
	private static EditorObjectScreen editorObjectScreen;

	public static EditorObjectScreen getInstance(Group rootSceneObject) {
		if(editorObjectScreen == null)
			editorObjectScreen = new EditorObjectScreen(rootSceneObject);
		editorObjectScreen.show();
		editorObjectScreen.setIconified(false);

		return editorObjectScreen;
	}



	private EditorObjectScreen(Group rootSceneObject) {
		if (rootSceneObject == null) {
			this.close();
			return;
		}

		items.getColumns().addAll(getObjectListColumns());
		rootSceneObject.getChildren().addListener((ListChangeListener<? super Node>) c -> {
			if (c.next()) {
				System.out.println("Added: " + c.getAddedSize());
				System.out.println("Removed: " + c.getRemovedSize());
				items.setItems(c.getList().parallelStream().map(GameObject::new).collect(Collectors.toCollection(FXCollections::observableArrayList)));

				lblCount.setText(String.format("%d Objects.", rootSceneObject.getChildren().size()));
			}
		});

		items.setItems(rootSceneObject.getChildren().parallelStream().map(GameObject::new).collect(Collectors.toCollection(FXCollections::observableArrayList)));
		items.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue != null) {
				System.out.println("You selected: " + newValue.objName);
			}
		});

		lblCount.setText(String.format("%d Objects.", rootSceneObject.getChildren().size()));
		VBox vBoxOuter = new VBox();
		vBoxOuter.getChildren().addAll(lblCount, items);
		this.setScene(new Scene(vBoxOuter, 500, 500));
		this.show();
	}

	private static List<TableColumn<GameObject, ?>> getObjectListColumns() {
		final TableColumn<GameObject, String> colID = new TableColumn<>("ObjName");
		colID.setCellValueFactory(new PropertyValueFactory<>("objName"));

		final TableColumn<GameObject, Double> colX = new TableColumn<>("X");
		colX.setCellValueFactory(new PropertyValueFactory<>("xpos"));

		final TableColumn<GameObject, Double> colY = new TableColumn<>("Y");
		colY.setCellValueFactory(new PropertyValueFactory<>("ypos"));

		final TableColumn<GameObject, Double> colZ = new TableColumn<>("Z");
		colZ.setCellValueFactory(new PropertyValueFactory<>("zpos"));
		return Arrays.asList(colID, colX, colY, colZ);
	}
}
