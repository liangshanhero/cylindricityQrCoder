package com.hombio.QrCodeTransformation;

import com.hombio.layout.LayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class App extends Application
{
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent parent = FXMLLoader.load(LayoutController.getLayoutFXMLURL("layout.fxml"));
		primaryStage.setScene(new Scene(parent));
		primaryStage.show();
	}
    public static void main( String[] args )
    {
        launch(args);
    }
}
