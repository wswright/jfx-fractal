package org.example.fxui;

import javafx.stage.Window;

import static java.lang.Math.abs;

public class WindowDocker {
	public static final boolean DEBUGGING = false;
	/**
	 * Performs {@link Window} docking one time. Does not setup listeners.
	 * @param parent The parent {@link Window}.
	 * @param child The child {@link Window}.
	 * @param dockDirection A {@link DockDirection}.
	 */
	public static void dockOnce(Window parent, Window child, DockDirection dockDirection) {
		dockOnceX(parent, child, dockDirection);
		dockOnceY(parent, child, dockDirection);
	}

	/**
	 * Performs {@link Window} docking but only for the X-coordinate.
	 * @param parent The parent {@link Window}.
	 * @param child The child {@link Window}.
	 * @param dockDirection A {@link DockDirection}.
	 */
	public static void dockOnceX(Window parent, Window child, DockDirection dockDirection) {
		double pWidth=parent.getWidth(), pX=parent.getX();      //Parent Info
		double cWidth=child.getWidth(), cX= child.getX();       //Child Info
		double xDelta = pX - cX;
		if(DEBUGGING) {
			System.out.println("Window Docking Details: ");
			System.out.println("\tParent Window Details: ");
			System.out.printf("\t\tX: %f%n", pX);
			System.out.printf("\t\tW: %f%n", pWidth);
			System.out.println("\tChild Window Details: ");
			System.out.printf("\t\tX: %f%n", cX);
			System.out.printf("\t\tW: %f%n", cWidth);
			System.out.printf("Child Window needs to move %f units to the %s.%n", abs(xDelta), xDelta < 0 ? "left" : "right" );
		}
		switch (dockDirection) {
			case ParentLeft -> child.setX(child.getX() + xDelta - child.getWidth());
			case ParentRight -> child.setX(child.getX() + xDelta + parent.getWidth());
			case ParentBottom, ParentTop -> child.setX(child.getX() + xDelta);
		}
	}

	/**
	 * Performs {@link Window} docking but only for the Y-coordinate.
	 * @param parent The parent {@link Window}.
	 * @param child The child {@link Window}.
	 * @param dockDirection A {@link DockDirection}.
	 */
	public static void dockOnceY(Window parent, Window child, DockDirection dockDirection) {
		double pHeight=parent.getHeight(), pY=parent.getY();    //Parent Info
		double cHeight=child.getHeight(), cY= child.getY();     //Child Info
		double yDelta = pY - cY;
		if(DEBUGGING) {
			System.out.println("Window Docking Details: \n");
			System.out.println("\tParent Window Details: ");
			System.out.printf("\t\tY: %f%n", pY);
			System.out.printf("\t\tH: %f%n", pHeight);
			System.out.println("\tChild Window Details: ");
			System.out.printf("\t\tY: %f%n", cY);
			System.out.printf("\t\tH: %f%n", cHeight);
			System.out.printf("Child Window needs to move %s %f units.%n", yDelta > 0 ? "down" : "up" , abs(yDelta));
		}
		switch (dockDirection) {
			case ParentLeft, ParentRight -> child.setY(cY+yDelta);
			case ParentBottom -> child.setY(cY+yDelta+pHeight);
			case ParentTop -> child.setY(cY+yDelta-cHeight);
		}
	}

	/**
	 * Performs {@link Window} docking and sets up listeners to keep the child docked. Dock-a-bye-baby. KMN
	 * @param parent The parent {@link Window}.
	 * @param child The child {@link Window}.
	 * @param dockDirection A {@link DockDirection}.
	 */
	public static void keepDocked(Window parent, Window child, DockDirection dockDirection) {
		keepDockedX(parent, child, dockDirection);
		keepDockedY(parent, child, dockDirection);
	}

	/**
	 * Performs {@link Window} docking and sets up a listener to keep the child's X-position docked.
	 * @param parent The parent {@link Window}.
	 * @param child The child {@link Window}.
	 * @param dockDirection A {@link DockDirection}.
	 */
	public static void keepDockedX(Window parent, Window child, DockDirection dockDirection) {
		parent.xProperty().addListener((observable, oldValue, newValue) -> dockOnceX(parent, child, dockDirection));
		dockOnceX(parent, child, dockDirection);
	}

	/**
	 * Performs {@link Window} docking and sets up a listener to keep the child's Y-position docked.
	 * @param parent The parent {@link Window}.
	 * @param child The child {@link Window}.
	 * @param dockDirection A {@link DockDirection}.
	 */
	public static void keepDockedY(Window parent, Window child, DockDirection dockDirection) {
		parent.yProperty().addListener((observable, oldValue, newValue) -> dockOnceY(parent, child, dockDirection));
		dockOnceY(parent, child, dockDirection);
	}

	public enum DockDirection {
		ParentLeft,
		ParentRight,
		ParentTop,
		ParentBottom
	}

}
