package graph;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Menu {
	private static final int defSaveWidth = 250;
	private static final int defSaveHeight = 150;
	
	public static TextField location,name;
	
	
	public static EventHandler<ActionEvent> saveAction(final Pane drawPane)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent arg0) 
			{
				final Stage saveStage = new Stage();
				saveStage.setTitle("Save");
				Pane saveRoot = new Pane();
				Scene saveScene = new Scene(saveRoot,defSaveWidth,defSaveHeight);
				saveStage.setScene(saveScene);
				
				Label txt1 = new Label("Enter image location:");
				txt1.setLayoutX(10);
				txt1.setLayoutY(10);
				saveRoot.getChildren().add(txt1);
				
				location = new TextField("C:/Users/emp5220514/Desktop/Test");
				location.setLayoutX(10);
				location.setLayoutY(30);
				location.setPrefWidth(230);
				saveRoot.getChildren().add(location);
				
				Label txt2 = new Label("Enter image name:");
				txt2.setLayoutX(10);
				txt2.setLayoutY(60);
				saveRoot.getChildren().add(txt2);
				
				name = new TextField("test01");
				name.setLayoutX(10);
				name.setLayoutY(80);
				name.setPrefWidth(230);
				saveRoot.getChildren().add(name);
				
				Pane buttonPane = new Pane();
				Button saveButton = new Button("Save");
				saveButton.setLayoutX(0);
				saveButton.setLayoutY(0);
				Button cancelButton = new Button("Cancel");
				cancelButton.setLayoutX(50);
				cancelButton.setLayoutY(0);
				buttonPane.getChildren().addAll(saveButton,cancelButton);
				buttonPane.setLayoutX(10);
				buttonPane.setLayoutY(110);
				saveRoot.getChildren().add(buttonPane);
				
				cancelButton.setOnAction(new EventHandler<ActionEvent>() {
					
					public void handle(ActionEvent arg0) 
					{
						saveStage.close();
					}
				});
				
				saveButton.setOnAction(saveImage(saveStage,drawPane));
				
				saveStage.show();
			}
			
		};
	}
	
	private static EventHandler<ActionEvent> saveImage(final Stage saveStage,final Pane drawPane)
	{
		return new EventHandler<ActionEvent>(){
				
			public void handle(ActionEvent a)
			{
				if(!location.getText().equals("") && !name.getText().equals(""))
				{
					saveStage.close();
					
					WritableImage image = drawPane.snapshot(new SnapshotParameters(), null);
			        File file = new File(location.getText() + "/"+ name.getText() + ".png");
			        

			        try {
			            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			        } catch (IOException ex) {
	                    ex.printStackTrace();
	                }
	                
				}
			}
		};
	}

}
