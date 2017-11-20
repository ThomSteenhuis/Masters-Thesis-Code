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

public class MenuAction {
	private final int defSaveWidth = 250;
	private final int defSaveHeight = 150;
	
	private final int legendBorderMargin = 10;
	private final int legendLineMargin = 10;
	private final int legendTxtMargin = 5;
	private final int legendLineLength = 10;
	private final int legendLineWidth = 1;
	private final int txtSize = 12;
	
	private LineGraph graph;
	public TextField location,name;
	
	public MenuAction(LineGraph g)
	{
		graph = g;
	}
	
	public EventHandler<ActionEvent> legendAction(boolean right)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent arg0)
			{
				ArrayList<String> lineNames = new ArrayList<String>();
				
				for(int idx=0;idx<graph.getCheckboxes().length;++idx)
				{
					if(graph.getCheckboxes()[idx].isSelected())
					{
						lineNames.add(graph.getCategories()[idx]);
					}
				}
					
				Line[] lines = new Line[lineNames.size()];
				Label[] txts = new Label[lineNames.size()];
				
				for(int idx=0;idx<lines.length;++idx)
				{
					lines[idx] = new Line();
					lines[idx].setStroke(graph.getColors()[idx]);
					lines[idx].setStrokeWidth(legendLineWidth);
					lines[idx].setStartX(graph.getLeftMargin()+graph.getYAxisSpace()+legendBorderMargin+legendLineMargin);
					lines[idx].setEndX(lines[idx].getStartX()+legendLineLength);
					lines[idx].setStartY(graph.getTopMargin()+legendBorderMargin+legendLineMargin+txtSize*idx);
					lines[idx].setEndY(lines[idx].getStartY());
					
					txts[idx] = new Label(graph.getCategories()[idx]);
					txts[idx].setFont(new Font(txtSize));
					txts[idx].setLayoutX(lines[idx].getEndX()+legendTxtMargin);
					txts[idx].setLayoutY(lines[idx].getStartY()-0.7*txtSize);

					graph.getDrawpane().getChildren().addAll(lines[idx],txts[idx]);
				}
				
				Line[] border = new Line[4];
				border[0] = new Line();
				border[0].setStartX(legendBorderMargin);
			}
		};
	}
	
	public EventHandler<ActionEvent> saveAction()
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
				
				saveButton.setOnAction(saveImage(saveStage));
				
				saveStage.show();
			}
			
		};
	}
	
	public LineGraph getGraph()
	{
		return graph;
	}
	
	private EventHandler<ActionEvent> saveImage(final Stage saveStage)
	{
		return new EventHandler<ActionEvent>(){
				
			public void handle(ActionEvent a)
			{
				if(!location.getText().equals("") && !name.getText().equals(""))
				{
					saveStage.close();
					
					WritableImage image = graph.getDrawpane().snapshot(new SnapshotParameters(), null);
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
