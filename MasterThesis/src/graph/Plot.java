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

	private static double[][] yValues;
	private static String[] categories;
	private static String[] xValues;
	private static String[] axesLabels;

	public static void main(String[] args)
	{
		launch(args);
	}

	public static void initialize(String[] mode,double[][] input,String[] leftLine,String[] header,String[] labels)
	{
		int tableLength = input.length;

		if( (tableLength == 0) || (tableLength != leftLine.length) || (labels.length != 2) )
			initializeError();

		int tableWidth =  input[0].length;

		if( (tableWidth == 0) || (tableWidth != header.length) )
			initializeError();

		categories = header;
		xValues = leftLine;
		axesLabels = labels;

		yValues = new double[tableLength][];

		for(int idx1=0;idx1<tableLength;++idx1)
		{
			if(input[idx1].length != tableWidth)
				initializeError();

			yValues[idx1] = new double[tableWidth];

			for(int idx2=0;idx2<tableWidth;++idx2)
				yValues[idx1][idx2] = input[idx1][idx2];
		}

		main(mode);
	}

	private static void initializeError()
	{
		System.out.println("Error (initialize): inputs not valid");
		System.exit(0);
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
	    left.setOnAction(graph.Menu.legendAction(false,categories,drawPane));
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
			LineGraph.plot(xValues,yValues,categories,axesLabels);
		}
	}
}
