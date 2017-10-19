package graph;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Plot extends Application{
	
	public static final double defWidth = 900;
	public static final double defHeight = 600;
	
	public static Pane drawPane,rightPane;

	public static void main(String[] args) 
	{
		launch(args);
	}

	public void start(Stage stage) throws Exception 
	{
		String argument = getParameters().getRaw().get(0);
		
		stage.setTitle("Plot");
		BorderPane pane = new BorderPane();
		Scene scene = new Scene(pane,defWidth,defHeight);
		stage.setScene(scene);
		
		drawPane = new Pane();
		
		pane.setCenter(drawPane);
		
		MenuBar menu = new MenuBar();
		Menu file = new Menu("File");
		MenuItem save = new MenuItem("Save");
		save.setOnAction(graph.Menu.saveAction(drawPane));
		menu.getMenus().add(file);
		file.getItems().add(save);
		
		Menu display = new Menu("Display");
		Menu legend = new Menu("Show Legend");
		ToggleGroup legendGroup = new ToggleGroup();
	    RadioMenuItem right = new RadioMenuItem("Right");
	    RadioMenuItem left = new RadioMenuItem("Left");
	    RadioMenuItem hide = new RadioMenuItem("Hide");
	    hide.setSelected(true);
	    left.setToggleGroup(legendGroup);
	    right.setToggleGroup(legendGroup);
	    hide.setToggleGroup(legendGroup);
	    menu.getMenus().add(display);
	    display.getItems().add(legend);
	    legend.getItems().addAll(left,right,hide);
	    
		pane.setTop(menu);
		
		rightPane = new Pane();
		pane.setRight(rightPane);
		rightPane.setPrefWidth(0.2*defWidth);

		stage.show();
		
		if(argument.equals("pivot"))
		{
			LineGraph.plot(rawdataprep.Run.pivotTable,rawdataprep.Run.machineNames,"Time","Volume");
		}		
	}

}
