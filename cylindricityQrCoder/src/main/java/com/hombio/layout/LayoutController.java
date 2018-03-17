package com.hombio.layout;

import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;


import com.hombio.action.Action;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

public class LayoutController implements Initializable{
	@FXML RadioButton horizontalRB;
	@FXML RadioButton verticalRB;
	@FXML TextArea contentTA;
	@FXML TextArea bottomContentTA;
	@FXML TextField radiusTF;
	@FXML TextField focusTF;
	@FXML TextField sideTF;
	@FXML ImageView beforeImage;
	@FXML ImageView afterImage;

	
	@FXML RadioButton cylinderRB;
	@FXML RadioButton globeRB;
	private BufferedImage[] qrImageArray;
	Action action;
	@FXML Label transLB;
	@FXML Label radiusLB;
	public static URL getLayoutFXMLURL(String name) {
		return LayoutController.class.getResource(name);
	}

	public void initialize(URL location, ResourceBundle resources) {
		ToggleGroup tg = new ToggleGroup();
		horizontalRB.setToggleGroup(tg);
		verticalRB.setToggleGroup(tg);
		verticalRB.setSelected(true);
		action = new Action();
		ToggleGroup tg1 = new ToggleGroup();
		cylinderRB.setToggleGroup(tg1);
		globeRB.setToggleGroup(tg1);
		cylinderRB.setSelected(true);
	}

	@FXML 
	public void checkNumber(DragEvent event) {
		
	}

	
	@FXML 
	public void form(ActionEvent event) throws Exception {
		if(!action.beforeCheck(contentTA, sideTF, radiusTF, focusTF)) {
			return ;
		}
		if(cylinderRB.isSelected()) {
			qrImageArray = action.getQrImageInputStreamArray(contentTA, bottomContentTA, sideTF, radiusTF, focusTF,verticalRB);
			
		}else {
			qrImageArray = action.getQrImageInputStreamArray(contentTA, bottomContentTA, sideTF, radiusTF, focusTF);
		}
		action.setImageView(beforeImage, qrImageArray[0]);
		action.setImageView(afterImage, qrImageArray[1]);
	}
	
	@FXML 
	public void saveImage(ActionEvent event) throws Exception {
		if(qrImageArray==null) {
			return ;
		}
		action.saveFile(bottomContentTA.getText(), qrImageArray);
	}

	@FXML 
	public void selectCylinderRB(ActionEvent event) {
		if(cylinderRB.isSelected()) {
			transLB.setVisible(true);
			horizontalRB.setVisible(true);
			verticalRB.setVisible(true);
			radiusLB.setText("圆柱体半径(cm)");
		}
	}

	@FXML public void selectGlobeRB(ActionEvent event) {
		if(globeRB.isSelected()) {
			transLB.setVisible(false);
			horizontalRB.setVisible(false);
			verticalRB.setVisible(false);
			radiusLB.setText("球体半径(cm)");
		}
	}
	

	

	
	
}
