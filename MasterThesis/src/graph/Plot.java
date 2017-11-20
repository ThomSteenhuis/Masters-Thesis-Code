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

	private static LineGraph graph;
	
	public static void plot(String[] args,LineGraph g)
	{
		graph = g;
		launch(args);
	}

	public void start(Stage stage) throws Exception
	{
		stage.setTitle("Plot");
		BorderPane pane = new BorderPane();
		Scene scene = new Scene(pane,graph.getDefWidth(),graph.getDefHeight());
		stage.setScene(scene);

		Pane drawPane = new Pane();

		pane.setCenter(drawPane);

		MenuAction ma = new MenuAction(graph);
		MenuBar menu = new MenuBar();
		Menu file = new Menu("File");
		MenuItem save = new MenuItem("Save");
		save.setOnAction(ma.saveAction());
		menu.getMenus().add(file);
		file.getItems().add(save);

		Menu display = new Menu("Display");
		Menu legend = new Menu("Show Legend");
		ToggleGroup legendGroup = new ToggleGroup();
	    RadioMenuItem right = new RadioMenuItem("Right");
	    RadioMenuItem left = new RadioMenuItem("Left");
	    RadioMenuItem hide = new RadioMenuItem("Hide");
	    hide.setSelected(true);
	    left.setOnAction(ma.legendAction(false));
	    left.setToggleGroup(legendGroup);
	    right.setToggleGroup(legendGroup);
	    hide.setToggleGroup(legendGroup);
	    menu.getMenus().add(display);
	    display.getItems().add(legend);
	    legend.getItems().addAll(left,right,hide);

		pane.setTop(menu);

		Pane rightPane = new Pane();
		pane.setRight(rightPane);
		rightPane.setPrefWidth(0.2*graph.getDefWidth());
		
		graph.setDrawPane(drawPane);
		graph.setRightPane(rightPane);

		stage.show();
	}
	
}
