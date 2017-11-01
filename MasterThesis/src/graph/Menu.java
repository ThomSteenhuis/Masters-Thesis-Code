package graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Menu {
	private static final int defSaveWidth = 250;
	private static final int defSaveHeight = 150;
	
	private static final int legendBorderMargin = 10;
	private static final int legendLineMargin = 10;
	private static final int legendTxtMargin = 5;
	private static final int legendLineLength = 10;
	private static final int legendLineWidth = 1;
	private static final int txtSize = 12;
	
	public static TextField location,name;
	
	public static EventHandler<ActionEvent> legendAction(boolean right,final String[] categories, final Pane drawPane)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent arg0)
			{
				ArrayList<String> lineNames = new ArrayList<String>();
				
				for(int idx=0;idx<graph.LineGraph.checkboxes.length;++idx)
				{
					if(graph.LineGraph.checkboxes[idx].isSelected())
					{
						lineNames.add(categories[idx]);
					}
				}
					
				Line[] lines = new Line[lineNames.size()];
				Label[] txts = new Label[lineNames.size()];
				
				for(int idx=0;idx<lines.length;++idx)
				{
					lines[idx] = new Line();
					lines[idx].setStroke(graph.LineGraph.colors[idx]);
					lines[idx].setStrokeWidth(legendLineWidth);
					lines[idx].setStartX(LineGraph.leftMargin+LineGraph.yAxisSpace+legendBorderMargin+legendLineMargin);
					lines[idx].setEndX(lines[idx].getStartX()+legendLineLength);
					lines[idx].setStartY(LineGraph.topMargin+legendBorderMargin+legendLineMargin+txtSize*idx);
					lines[idx].setEndY(lines[idx].getStartY());
					
					txts[idx] = new Label(categories[idx]);
					txts[idx].setFont(new Font(txtSize));
					txts[idx].setLayoutX(lines[idx].getEndX()+legendTxtMargin);
					txts[idx].setLayoutY(lines[idx].getStartY()-0.7*txtSize);

					drawPane.getChildren().addAll(lines[idx],txts[idx]);
				}
				
				Line[] border = new Line[4];
				border[0] = new Line();
				border[0].setStartX(legendBorderMargin);
			}
		};
	}
	
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
