package graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class LineGraph{

	private final double defWidth = 900;
	private final double defHeight = 600;
	
	private double topMargin = 20;
	private double rightMargin = 20;
	private double bottomMargin = 20;
	private double leftMargin = 20;
	private double xAxisSpace = 80;
	private double yAxisSpace = 80;
	
	private double tickSize = 5;
	private double xNameMargin = 30;
	private double yNameMargin = 20;

	private Pane drawPane,rightPane;
	private CheckBox[] checkboxes;
	private Color[] colors;

	private double[][][] yValues;
	private String[][] categories;
	private String[][] xValues;
	
	double[][][] lineCoordinates;

	private double[][] axisCoordinates;
	private String[][] axisNames;

	private String[] yAxisCategories;

	private Label[] xLabels;
	private Label[] yLabels;
	
	private int current;

	private int xMode = 1;
	private int xInterval;
	private boolean minorXTicks = true;
	private int yMode = 0;
	private int yInterval;
	private int yNoIntervals;
	private int maxNoMajorXTicks = 30;
	private int maxNoMajorYTicks = 10;
	private boolean minorYTicks = false;
	
	private String outputLoc;
	
	public LineGraph(double[][][] input,String[][] leftLine,String[][] header,String[][] labels)
	{
		setData(input,leftLine,header,labels);
		colors = makeColors(15);
	}
	
	public void setData(double[][][] input,String[][] leftLine,String[][] header,String[][] labels)
	{
		if( (input.length != leftLine.length) || (input.length != header.length) || (input.length != labels.length) )
			initializeError();

		categories = header;
		xValues = leftLine;
		axisNames = labels;
		yValues = input;		
		current = 0;
	}
	
	public void plot()
	{
		String[] args = new String[0];
		Plot.plot(args,this);
	}
	
	public void autoplot(String oloc)
	{
		outputLoc = oloc;
		String[] args = {"auto"};
		Plot.plot(args,this);
	}
	
	public void draw(Stage stage)
	{
		axisCoordinates = calculateAxesCoordinates();
		drawAxes();
		drawData();
	}
	
	public void autodraw(Stage stage)
	{
		axisCoordinates = calculateAxesCoordinates();
		drawAxes();
		
		for(int idx=0;idx<yValues.length;++idx)
		{
			current = idx;
			drawData();
			
			if(saveGraph())
				System.out.println("Succesfully saved graph "+current);
		}
	}
	
	private void drawData()
	{
		double[] extremes = getExtremes(yValues[current]);
		yAxisCategories = new String[(int) Math.ceil(extremes[1]) + 1];

		for(int idx=0;idx<yAxisCategories.length;++idx)
			yAxisCategories[idx] = Integer.toString(idx);
		
		drawTicks();
		calculateLineCoordinates();
		createCheckboxes();
	}
	
	public void setDrawPane(Pane pane)
	{
		drawPane = pane;
	}
	
	public void setRightPane(Pane pane)
	{
		rightPane = pane;
	}
	
	public double getDefWidth()
	{
		return defWidth;
	}
	
	public double getDefHeight()
	{
		return defHeight;
	}
	
	public double getTopMargin()
	{
		return topMargin;
	}
	
	public double getRightMargin()
	{
		return rightMargin;
	}
	
	public double getBottomMargin()
	{
		return bottomMargin;
	}
	
	public double getLeftMargin()
	{
		return leftMargin;
	}
	
	public double getXAxisSpace()
	{
		return xAxisSpace;
	}
	
	public double getYAxisSpace()
	{
		return yAxisSpace;
	}
	
	public CheckBox[] getCheckboxes()
	{
		return checkboxes;
	}
	
	public String[][] getCategories()
	{
		return categories;
	}
	
	public Color[] getColors()
	{
		return colors;
	}
	
	public Pane getDrawpane()
	{
		return drawPane;
	}
	
	public Pane getRightpane()
	{
		return rightPane;
	}
	
	public int getCurrent()
	{
		return current;
	}
	
	private boolean saveGraph()
	{
		String number = Integer.toString(current);
		while(number.length() < 3) number = "0" + number;
		
		WritableImage image = drawPane.snapshot(new SnapshotParameters(), null);
        File file = new File(outputLoc + "/graph"+ number + ".png");        

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
	}
	
	private void calculateIntervals()
	{
		xInterval = (int)Math.ceil( ( (double)(xValues[current].length-1) ) / maxNoMajorXTicks );
		
		int guess = (int)Math.ceil(Double.parseDouble(yAxisCategories[yAxisCategories.length-1])/(maxNoMajorYTicks-1) );
		int powerOf10 = (int) Math.floor(Math.log10(guess) );
		
		if( ( (double) (guess) / Math.pow(10,powerOf10) ) > 5 )
			yInterval = (int) Math.pow(10,powerOf10+1);
		else if( ( (double) (guess) / Math.pow(10,powerOf10) ) > 2 )
			yInterval = (int) (5*Math.pow(10,powerOf10) );
		else
			yInterval = (int) (2*Math.pow(10,powerOf10) );
		
		yNoIntervals = (int)Math.ceil(Double.parseDouble(yAxisCategories[yAxisCategories.length-1])/yInterval);
	}

	private void drawAxes()
	{
		Line[] axes = new Line[4];

		for(int idx=0;idx<3;++idx)
		{
			axes[idx] = new Line();
			axes[idx].setStartX(axisCoordinates[idx][0]);
			axes[idx].setStartY(axisCoordinates[idx][1]);
			axes[idx].setEndX(axisCoordinates[idx+1][0]);
			axes[idx].setEndY(axisCoordinates[idx+1][1]);
		}

		axes[3] = new Line();
		axes[3].setStartX(axisCoordinates[3][0]);
		axes[3].setStartY(axisCoordinates[3][1]);
		axes[3].setEndX(axisCoordinates[0][0]);
		axes[3].setEndY(axisCoordinates[0][1]);

		drawPane.getChildren().addAll(axes);
	}
	
	private void drawTicks()
	{
		calculateIntervals();
		Line[] xTicks = new Line[xValues[current].length];
		String[] xTickNames = new String[xValues[current].length];
		xLabels = new Label[xValues[current].length];

		for(int idx=0;idx<xValues[current].length;++idx)
		{
			xTicks[idx] = new Line();

			if( (idx%xInterval) == 0)
			{
				xTicks[idx].setStartX(axisCoordinates[1][0] + idx*(axisCoordinates[2][0] - axisCoordinates[1][0])/(xValues[current].length-1));
				xTicks[idx].setStartY(axisCoordinates[1][1]);
				xTicks[idx].setEndX(xTicks[idx].getStartX());
				xTicks[idx].setEndY(axisCoordinates[1][1] + tickSize);
			}
			else if( ( (idx%xInterval) != 0) && minorXTicks)
			{
				xTicks[idx].setStartX(axisCoordinates[1][0] + idx*(axisCoordinates[2][0] - axisCoordinates[1][0])/(xValues[current].length-1));
				xTicks[idx].setStartY(axisCoordinates[1][1]);
				xTicks[idx].setEndX(xTicks[idx].getStartX());
				xTicks[idx].setEndY(axisCoordinates[1][1] + 0.5*tickSize);
			}

			xTickNames[idx] = xValues[current][(int) (idx*(xValues[current].length-1)/(xValues[current].length-1))];

			xLabels[idx] = new Label(xTickNames[idx]);
		}

		addXLabels();
		
		Line[] yTicks = new Line[yInterval*yNoIntervals+1];
		String[] yTickNames = new String[yInterval*yNoIntervals+1];
		yLabels = new Label[yInterval*yNoIntervals+1];

		for(int idx=0;idx<=(yInterval*yNoIntervals);++idx)
		{
			yTicks[idx] = new Line();

			if( (idx%yInterval) == 0)
			{
				yTicks[idx].setStartX(axisCoordinates[1][0]);
				yTicks[idx].setStartY(axisCoordinates[1][1] - idx*(axisCoordinates[1][1] - axisCoordinates[0][1])/(yAxisCategories.length-1));
				yTicks[idx].setEndX(axisCoordinates[1][0] - tickSize);
				yTicks[idx].setEndY(yTicks[idx].getStartY());
			}
			else if( ( (idx%yInterval) != 0) && minorYTicks)
			{
				yTicks[idx].setStartX(axisCoordinates[1][0]);
				yTicks[idx].setStartY(axisCoordinates[1][1] - idx*(axisCoordinates[1][1] - axisCoordinates[0][1])/(yAxisCategories.length-1));
				yTicks[idx].setEndX(axisCoordinates[1][0] - 0.5*tickSize);
				yTicks[idx].setEndY(yTicks[idx].getStartY());
			}

			yTickNames[idx] = Integer.toString(idx);

			yLabels[idx] = new Label(yTickNames[idx]);
		}

		addYLabels();

		drawPane.getChildren().addAll(xTicks);
		drawPane.getChildren().addAll(yTicks);

		Label xLabel = new Label(axisNames[current][0]);
		xLabel.setLayoutX(axisCoordinates[1][0] + (axisCoordinates[2][0]-axisCoordinates[1][0])/2);
		xLabel.setLayoutY(drawPane.getHeight() - xNameMargin);
		Label yLabel = new Label(axisNames[current][1]);
		yLabel.setLayoutX(yNameMargin);
		yLabel.setLayoutY((axisCoordinates[1][1]-axisCoordinates[0][1])/2);

		drawPane.getChildren().add(xLabel);
		drawPane.getChildren().add(yLabel);
	}

	private void addXLabels()
	{
		switch (xMode)
		{
		case 0:
		{
			for(int idx=0;idx<xLabels.length;idx+=xInterval)
			{
				xLabels[idx].widthProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustXLabels();
					}
				});

				xLabels[idx].heightProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustXLabels();
					}
				});

				drawPane.getChildren().add(xLabels[idx]);
			}

			break;
		}
		case 1:
		{
			for(int idx=0;idx<xLabels.length;idx+=xInterval)
			{
				xLabels[idx].setRotate(-90);

				xLabels[idx].widthProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustXLabels();
					}
				});

				xLabels[idx].heightProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustXLabels();
					}
				});

				drawPane.getChildren().add(xLabels[idx]);
			}

			break;
		}
		default:
		{
			System.out.println("Warning (addXLabels): default case reached");
		}
		}
	}

	private void addYLabels()
	{
		switch (yMode)
		{
		case 0:
		{
			for(int idx=0;idx<=yNoIntervals;idx++)
			{
				yLabels[idx*yInterval].widthProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				yLabels[idx*yInterval].heightProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				drawPane.getChildren().add(yLabels[idx*yInterval]);
			}

			break;
		}
		case 1:
		{
			for(int idx=0;idx<=yNoIntervals;idx++)
			{
				yLabels[idx*yInterval].setRotate(-90);

				yLabels[idx*yInterval].widthProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				yLabels[idx*yInterval].heightProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				drawPane.getChildren().add(yLabels[idx*yInterval]);
			}

			break;
		}
		default:
		{
			System.out.println("Warning (addYLabels): default case reached");
		}
		}
	}

	private void adjustYLabels()
	{
		for(int idx=0;idx<yLabels.length;idx+=yInterval)
		{
			yLabels[idx].setLayoutX(axisCoordinates[0][0] - 2*tickSize - yLabels[idx].getWidth());
			yLabels[idx].setLayoutY(axisCoordinates[1][1] - idx*(axisCoordinates[1][1]-axisCoordinates[0][1])/(yAxisCategories.length-1) - 0.5*yLabels[idx].getHeight());
		}
	}

	private void adjustXLabels()
	{
		for(int idx=0;idx<xLabels.length;idx+=xInterval)
		{
			xLabels[idx].setLayoutX(axisCoordinates[0][0] + idx*(axisCoordinates[3][0]-axisCoordinates[0][0])/(xLabels.length-1) - 0.5*xLabels[idx].getWidth());
			xLabels[idx].setLayoutY(axisCoordinates[1][1] + 2*tickSize + xLabels[idx].getHeight());
		}
	}

	private void drawLines()
	{
		drawPane.getChildren().clear();
		drawAxes();
		drawTicks();

		Line line = null;

		for(int idx1=0;idx1<lineCoordinates.length;++idx1)
		{
			if(checkboxes[idx1].isSelected())
			{
				for(int idx2=0;idx2<lineCoordinates[idx1].length;++idx2)
				{
					line = new Line();
					line.setStroke(colors[idx1]);
					line.setStartX(lineCoordinates[idx1][idx2][0]);
					line.setStartY(lineCoordinates[idx1][idx2][1]);
					line.setEndX(lineCoordinates[idx1][idx2][2]);
					line.setEndY(lineCoordinates[idx1][idx2][3]);
					drawPane.getChildren().add(line);
				}
			}
		}
	}

	private void createCheckboxes()
	{
		checkboxes = new CheckBox[categories[current].length];

		final CheckBox all = new CheckBox("Show All");
		all.setLayoutX(10);
		all.setLayoutY(10);
		final CheckBox none = new CheckBox("Show None");
		none.setLayoutX(10);
		none.setLayoutY(30);

		for(int idx=0;idx<categories[current].length;++idx)
		{
			checkboxes[idx] = new CheckBox(categories[current][idx]);
			checkboxes[idx].setLayoutX(10);
			checkboxes[idx].setLayoutY(60 + idx*20);

			checkboxes[idx].selectedProperty().addListener(new ChangeListener<Boolean>() {

			    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			    {
			    	if(!newValue)
			    		all.setSelected(false);

			    	if(newValue)
			    		none.setSelected(false);

			        drawLines();
			    }
			});

			rightPane.getChildren().add(checkboxes[idx]);
		}

		all.selectedProperty().addListener(new ChangeListener<Boolean>() {

		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		    {
		        if(newValue)
		        {
		        	none.setSelected(false);

		        	for(int idx=0;idx<categories[current].length;++idx)
		        		checkboxes[idx].setSelected(true);
		        }
		    }
		});

		none.selectedProperty().addListener(new ChangeListener<Boolean>() {

		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		    {
		        if(newValue)
		        {
		        	all.setSelected(false);

		        	for(int idx=0;idx<categories[current].length;++idx)
		        		checkboxes[idx].setSelected(false);
		        }
		    }
		});

		rightPane.getChildren().addAll(all,none);
	}

	private void calculateLineCoordinates()
	{
		if(yValues[current].length == 0)
			return;

		lineCoordinates = new double[yValues[current][0].length][yValues[current].length][4];
		
		double[] coordinates;
		double defWidth = axisCoordinates[2][0] - axisCoordinates[0][0];
		double defHeight = axisCoordinates[2][1] - axisCoordinates[0][1];
		double xStart = axisCoordinates[0][0];
		double yStart = axisCoordinates[0][1];
		double xStep = defWidth/( (double) (xValues[current].length - 1) );

		for(int idx1=0;idx1<(yValues[current][0].length);++idx1)
		{
			for(int idx2=0;idx2<(yValues[current].length-1);idx2++)
			{
				coordinates = new double[4];
				coordinates[0] = idx2*xStep + xStart;
				coordinates[2] = (idx2+1)*xStep + xStart;
				coordinates[1] = yStart + defHeight * (1 - (yValues[current][idx2][idx1] / (yNoIntervals*yInterval) ) );
				coordinates[3] = yStart + defHeight * (1 - (yValues[current][idx2+1][idx1] / (yNoIntervals*yInterval) ) );
				lineCoordinates[idx1][idx2] = coordinates;
			}
		}
	}

	private double[][] calculateAxesCoordinates()
	{
		double[][] output = new double[4][2];
		output[0][0] = leftMargin + yAxisSpace;
		output[0][1] = topMargin;
		output[1][0] = leftMargin + yAxisSpace;
		output[1][1] = drawPane.getHeight() - bottomMargin - xAxisSpace;
		output[2][0] = drawPane.getWidth() - rightMargin;
		output[2][1] = drawPane.getHeight() - bottomMargin - xAxisSpace;
		output[3][0] = drawPane.getWidth() - rightMargin;
		output[3][1] = topMargin;
		
		return output;
	}
	
	private static double[] getExtremes(double[][] table)
	{
		double[] output = new double[2];
		double min = table[0][0];
		double max = table[0][0];

		for(int idx1=0;idx1<table.length;++idx1)
		{
			for(int idx2=0;idx2<table[idx1].length;++idx2)
			{
				if(table[idx1][idx2] > max)
					max = table[idx1][idx2];

				if(table[idx1][idx2] < min)
					min = table[idx1][idx2];
			}
		}

		output[0] = min;
		output[1] = max;

		return output;
	}

	private static Color[] makeColors(int no)
	{
		int maxNoColors = 15;

		if(no > maxNoColors)
		{
			System.out.println("Error (getColors): too many colors requested");
			return null;
		}

		Color[] colors = new Color[maxNoColors];

		colors[0] = Color.RED;
		colors[1] = Color.GREEN;
		colors[2] = Color.YELLOW;
		colors[3] = Color.BLUE;
		colors[4] = Color.ORANGE;
		colors[5] = Color.PURPLE;
		colors[6] = Color.BROWN;
		colors[7] = Color.BLACK;
		colors[8] = Color.PINK;
		colors[9] = Color.AQUAMARINE;
		colors[10] = Color.GRAY;
		colors[11] = Color.GREEN;
		colors[12] = Color.MAGENTA;
		colors[13] = Color.CYAN;
		colors[14] = Color.GOLD;

		Color[] output = new Color[no];

		for(int idx=0;idx<no;++idx)
			output[idx] = colors[idx];

		return output;
	}
	
	private static void initializeError()
	{
		System.out.println("Error (initialize): inputs not valid");
		System.exit(0);
	}
}
