package com.hombio.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.hombio.util.QRCoder;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.RadioButton;

public class Action {

	public boolean beforeCheck(TextArea contentTA, TextField sideTF, TextField radiusTF, TextField focusTF) {
		int size;
		float radius;
		float focus;
		if(contentTA.getText().length()==0) {
			showMessage("二维码内容不能为空");
			return false;
		}
		
		try {
			size = new Integer(sideTF.getText());
		}catch(Exception e) {
			showMessage("边长应为整数");
			return false;
		}
		try {
			radius = new Float(radiusTF.getText());
		}catch(Exception e) {
			showMessage("半径应为浮点数");
			return false;
		}
		try {
			focus = new Float(focusTF.getText());
		}catch(Exception e) {
			showMessage("焦距应为浮点数");
			return false;
		}
		double x = Math.asin(radius/(radius+focus));
		double temp = Math.tan(x)*focus;
		if(size/2.0 > temp) {
			showMessage("二维码相对于圆柱太大，无意义");
			return false;
		}
		return true;
	}
	
	public void showMessage(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText(msg);
		alert.show();
	}
	public void setImageView(ImageView imageView, BufferedImage qrImage) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(qrImage, "PNG", baos);
		Image image = new Image(new ByteArrayInputStream(baos.toByteArray()));
		imageView.setFitWidth(image.getWidth());
		imageView.setFitHeight(image.getHeight());
		imageView.setImage(image);
	}
	public BufferedImage[] getQrImageInputStreamArray(TextArea contentTA,TextArea bottomContentTA, TextField sideTF,
			TextField radiusTF, TextField focusTF,RadioButton verticalRB ) throws NumberFormatException, IOException, Exception {
		return QRCoder.encode(contentTA.getText(), bottomContentTA.getText(),
				new Integer(sideTF.getText()),new Float(radiusTF.getText()),new Float(focusTF.getText()),verticalRB.isSelected());
	}
	
	public BufferedImage[] getQrImageInputStreamArray(TextArea contentTA,TextArea bottomContentTA, TextField sideTF,
			TextField radiusTF, TextField focusTF ) throws NumberFormatException, IOException, Exception {
		return QRCoder.encode(contentTA.getText(), bottomContentTA.getText(),
				new Integer(sideTF.getText()),new Float(radiusTF.getText()),new Float(focusTF.getText()));
	}
	private File historyImageSavedDir;
	public void saveFile(String memo, BufferedImage[] imageArray) throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("选择图片存放位置");
		fileChooser.setInitialFileName(memo);
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("(jpg)", "*.jpg","*.JPEG"),
                new ExtensionFilter("(jpeg)", "*.jpeg","*.JPEG"),
                new ExtensionFilter("(bmp)", "*.bmp","*.BMP"),
                new ExtensionFilter("(png)", "*.png","*.PNG"));
		fileChooser.setInitialDirectory(historyImageSavedDir);
		File targetFile = fileChooser.showSaveDialog(new Stage());
		if(targetFile != null) {
			
			int index = targetFile.getAbsolutePath().lastIndexOf('\\')+1;
			String dir = targetFile.getAbsolutePath().substring(0, index);
			String fileName = targetFile.getAbsolutePath().substring(index);
			String filePath = dir+"转换的"+fileName;
			File targetFile1 = new File(filePath);
            historyImageSavedDir = new File(dir);
			String format = fileChooser.getSelectedExtensionFilter().getDescription();
			format = format.replace("(", "");
			format = format.replace(")", "");

			QRCoder.saveImage(imageArray[0], targetFile, format);
			QRCoder.saveImage(imageArray[1], targetFile1, format);
		}
		
	}
}
