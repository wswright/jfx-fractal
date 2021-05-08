package org.example.game;

import javafx.scene.Node;
import org.example.editor.EditorUtils;

public class GameObject {
	public String objName = "";

	public Node getGameNode() {
		return gameNode;
	}

	protected Node gameNode;

	public GameObject(Node n) {
		gameNode = n;
		if(n.getUserData() == null)
			this.objName = EditorUtils.describe(gameNode);
		else if(n.getUserData() instanceof GameObject gameObject) {
			this.objName = gameObject.getObjName();
		}
		setXpos(gameNode.getTranslateX());
		setYpos(gameNode.getTranslateY());
		setZpos(gameNode.getTranslateZ());
		gameNode.setUserData(this);
	}

	public GameObject(Node n, String name) {
		this(n);
		this.objName = name;
		gameNode.setUserData(this);
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public Double getXpos() {
		return gameNode.getTranslateX();
	}

	public void setXpos(Double xpos) {
		gameNode.setTranslateX(xpos);
	}

	public Double getYpos() {
		return gameNode.getTranslateY();
	}

	public void setYpos(Double ypos) {
		gameNode.setTranslateY(ypos);
	}

	public Double getZpos() {
		return gameNode.getTranslateZ();
	}

	public void setZpos(Double zpos) {
		gameNode.setTranslateZ(zpos);
	}
}
