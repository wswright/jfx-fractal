package org.example.editor;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.example.fxui.SmartGroup;

public class EditorUtils {
    public static Cylinder createConnection(Point3D origin, Point3D target, double radius) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(radius, height);

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

	/**
	 * Describes a {@link Node} using words.
	 *
	 * @param n The {@link Node} to describe.
	 * @return Returns a String.
	 */
	public static String describe(Node n) {
		if (n == null)
			return "Hey.. this is NULL!";

		String description = "";
		if (n instanceof VBox vBox)
			description += String.format("This is a VBox. It contains %d Nodes. ", vBox.getChildren().size());
		if (n instanceof HBox hBox)
			description += String.format("This is an HBox. It contains %d Nodes.", hBox.getChildren().size());
		if (n instanceof Button button)
			description += String.format("This is a Button. Text: %s ", button.getText());
		if (n instanceof Sphere sphere)
			description += String.format("This is a Sphere. Radius: %f ", sphere.getRadius());
		if (n instanceof Text text)
			description += String.format("This is Text: %s ", text.getText());
		if (n instanceof ToolBar toolBar)
			description += String.format("This is a Toolbar. It contains %d Nodes. Orientation: %s.", toolBar.getItems().size(), toolBar.getOrientation().name());
		if (n instanceof MeshView meshView)
			description += String.format("This is a MeshView. Draw Mode: %s", meshView.getDrawMode().name());
		if (n instanceof SmartGroup smartGroup) {
			description += String.format("This is a SmartGroup. It contains %d Nodes.", smartGroup.getChildren().size());
		}
		if (description.isEmpty())
			description = "Hmm.. I'm not sure what this is. Type: " + n.getClass();
		return description;
	}
}
