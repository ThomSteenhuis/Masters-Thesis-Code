package graph;

import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class LineGraph {

	public static double topMargin = 20;
	public static double rightMargin = 20;
	public static double bottomMargin = 20;
	public static double leftMargin = 20;
	public static double xAxisSpace = 80;
	public static double yAxisSpace = 80;
	
	private static double tickSize = 5;
	private static double xNameMargin = 30;
	private static double yNameMargin = 20;

	public static CheckBox[] checkboxes;
	private static ArrayList<double[]>[] lineCoordinates;

	private static double[][] axisCoordinates;
	private static String xAxisName;
	private static String yAxisName;

	private static String[] xAxisCategories;
	private static String[] yAxisCategories;

	private static Label[] xLabels;
	private static Label[] yLabels;

	public static Color[] colors;

	private static int xMode = 1;
	private static int xInterval = 2;
	private static boolean minorXTicks = true;
	private static int yMode = 0;
	private static int yInterval = 2;
	private static boolean minorYTicks = false;

	public static void plot(String[] xvals,double[][] yvals,String[] categories,String[] labels)
	{
		colors = getColors(categories.length);

		xAxisName = labels[0];
		yAxisName = labels[1];

		axisCoordinates = new double[4][2];
		axisCoordinates[0][0] = leftMargin + yAxisSpace;
		axisCoordinates[0][1] = topMargin;
		axisCoordinates[1][0] = leftMargin + yAxisSpace;
		axisCoordinates[1][1] = Plot.drawPane.getHeight() - bottomMargin - xAxisSpace;
		axisCoordinates[2][0] = Plot.drawPane.getWidth() - rightMargin;
		axisCoordinates[2][1] = Plot.drawPane.getHeight() - bottomMargin - xAxisSpace;
		axisCoordinates[3][0] = Plot.drawPane.getWidth() - rightMargin;
		axisCoordinates[3][1] = topMargin;

		lineCoordinates = getLineCoordinates(yvals,axisCoordinates[0],axisCoordinates[2]);
		xAxisCategories = xvals;

		drawAxes();

		createCheckboxes(categories);
	}

	private static void drawAxes()
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

		Plot.drawPane.getChildren().addAll(axes);

		Line[] xTicks = new Line[xAxisCategories.length];
		String[] xTickNames = new String[xAxisCategories.length];
		xLabels = new Label[xAxisCategories.length];

		for(int idx=0;idx<xAxisCategories.length;++idx)
		{
			xTicks[idx] = new Line();

			if( (idx%xInterval) == 0)
			{
				xTicks[idx].setStartX(axisCoordinates[1][0] + idx*(axisCoordinates[2][0] - axisCoordinates[1][0])/(xAxisCategories.length-1));
				xTicks[idx].setStartY(axisCoordinates[1][1]);
				xTicks[idx].setEndX(xTicks[idx].getStartX());
				xTicks[idx].setEndY(axisCoordinates[1][1] + tickSize);
			}
			else if( ( (idx%xInterval) != 0) && minorXTicks)
			{
				xTicks[idx].setStartX(axisCoordinates[1][0] + idx*(axisCoordinates[2][0] - axisCoordinates[1][0])/(xAxisCategories.length-1));
				xTicks[idx].setStartY(axisCoordinates[1][1]);
				xTicks[idx].setEndX(xTicks[idx].getStartX());
				xTicks[idx].setEndY(axisCoordinates[1][1] + 0.5*tickSize);
			}

			xTickNames[idx] = xAxisCategories[(int) (idx*(xAxisCategories.length-1)/(xAxisCategories.length-1))];

			xLabels[idx] = new Label(xTickNames[idx]);
		}

		addXLabels();

		Line[] yTicks = new Line[yAxisCategories.length];
		String[] yTickNames = new String[yAxisCategories.length];
		yLabels = new Label[yAxisCategories.length];

		for(int idx=0;idx<yAxisCategories.length;++idx)
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

			yTickNames[idx] = yAxisCategories[(int) (idx*(yAxisCategories.length-1)/(yAxisCategories.length-1))];

			yLabels[idx] = new Label(yTickNames[idx]);
		}

		addYLabels();

		Plot.drawPane.getChildren().addAll(xTicks);
		Plot.drawPane.getChildren().addAll(yTicks);

		Label xLabel = new Label(xAxisName);
		xLabel.setLayoutX(axisCoordinates[1][0] + (axisCoordinates[2][0]-axisCoordinates[1][0])/2);
		xLabel.setLayoutY(Plot.drawPane.getHeight() - xNameMargin);
		Label yLabel = new Label(yAxisName);
		yLabel.setLayoutX(yNameMargin);
		yLabel.setLayoutY((axisCoordinates[1][1]-axisCoordinates[0][1])/2);

		Plot.drawPane.getChildren().add(xLabel);
		Plot.drawPane.getChildren().add(yLabel);
	}

	private static void addXLabels()
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

				Plot.drawPane.getChildren().add(xLabels[idx]);
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

				Plot.drawPane.getChildren().add(xLabels[idx]);
			}

			break;
		}
		default:
		{
			System.out.println("Warning (addXLabels): default case reached");
		}
		}
	}

	private static void addYLabels()
	{
		switch (yMode)
		{
		case 0:
		{
			for(int idx=0;idx<yLabels.length;idx+=yInterval)
			{
				yLabels[idx].widthProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				yLabels[idx].heightProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				Plot.drawPane.getChildren().add(yLabels[idx]);
			}

			break;
		}
		case 1:
		{
			for(int idx=0;idx<yLabels.length;idx+=yInterval)
			{
				yLabels[idx].setRotate(-90);

				yLabels[idx].widthProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				xLabels[idx].heightProperty().addListener(new ChangeListener()
				{
					public void changed(ObservableValue arg0, Object arg1, Object arg2)
					{
						adjustYLabels();
					}
				});

				Plot.drawPane.getChildren().add(yLabels[idx]);
			}

			break;
		}
		default:
		{
			System.out.println("Warning (addYLabels): default case reached");
		}
		}
	}

	private static void adjustYLabels()
	{
		for(int idx=0;idx<yLabels.length;idx+=yInterval)
		{
			yLabels[idx].setLayoutX(axisCoordinates[0][0] - 2*tickSize - yLabels[idx].getWidth());
			yLabels[idx].setLayoutY(axisCoordinates[1][1] - idx*(axisCoordinates[1][1]-axisCoordinates[0][1])/(yAxisCategories.length-1) - 0.5*yLabels[idx].getHeight());
		}
	}

	private static void adjustXLabels()
	{
		for(int idx=0;idx<xLabels.length;idx+=xInterval)
		{
			xLabels[idx].setLayoutX(axisCoordinates[0][0] + idx*(axisCoordinates[3][0]-axisCoordinates[0][0])/(xAxisCategories.length-1) - 0.5*xLabels[idx].getWidth());
			xLabels[idx].setLayoutY(axisCoordinates[1][1] + 2*tickSize + xLabels[idx].getHeight());
		}
	}

	private static void drawLines()
	{
		Plot.drawPane.getChildren().clear();
		drawAxes();

		Line line = null;

		for(int idx1=0;idx1<lineCoordinates.length;++idx1)
		{
			if(checkboxes[idx1].isSelected())
			{
				for(int idx2=0;idx2<lineCoordinates[idx1].size();++idx2)
				{
					line = new Line();
					line.setStroke(colors[idx1]);
					line.setStartX(lineCoordinates[idx1].get(idx2)[0]);
					line.setStartY(lineCoordinates[idx1].get(idx2)[1]);
					line.setEndX(lineCoordinates[idx1].get(idx2)[2]);
					line.setEndY(lineCoordinates[idx1].get(idx2)[3]);
					Plot.drawPane.getChildren().add(line);
				}
			}
		}
	}

	private static void createCheckboxes(final String[] names)
	{
		checkboxes = new CheckBox[names.length];

		final CheckBox all = new CheckBox("Show All");
		all.setLayoutX(10);
		all.setLayoutY(10);
		final CheckBox none = new CheckBox("Show None");
		none.setLayoutX(10);
		none.setLayoutY(30);

		for(int idx=0;idx<names.length;++idx)
		{
			checkboxes[idx] = new CheckBox(names[idx]);
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

			Plot.rightPane.getChildren().add(checkboxes[idx]);
		}

		all.selectedProperty().addListener(new ChangeListener<Boolean>() {

		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		    {
		        if(newValue)
		        {
		        	none.setSelected(false);

		        	for(int idx=0;idx<names.length;++idx)
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

		        	for(int idx=0;idx<names.length;++idx)
		        		checkboxes[idx].setSelected(false);
		        }
		    }
		});

		Plot.rightPane.getChildren().addAll(all,none);
	}

	private static ArrayList<double[]>[] getLineCoordinates(double[][] table,double[] topLeft,double[] bottomRight)
	{
		if(table.length <1)
			return null;

		ArrayList<double[]>[] output = new ArrayList[table[0].length];

		for(int idx=0;idx<(table[0].length);++idx)
			output[idx] = new ArrayList<double[]>();

		double[] extremes = getExtremes(table);
		yAxisCategories = new String[(int) (Math.ceil(extremes[1]) - Math.floor(extremes[0]) ) + 1];

		for(int idx=0;idx<yAxisCategories.length;++idx)
		{
			yAxisCategories[idx] = Integer.toString((int) (Math.floor(extremes[0]) ) + idx);
		}

		double[] coordinates;
		double defWidth = bottomRight[0] - topLeft[0];
		double defHeight = bottomRight[1] - topLeft[1];
		double xStart = topLeft[0];
		double yStart = topLeft[1];
		double xStep = defWidth/( (double) (table.length - 1) );

		for(int idx1=0;idx1<(table[0].length);++idx1)
		{
			for(int idx2=0;idx2<(table.length-1);idx2++)
			{
				coordinates = new double[4];
				coordinates[0] = idx2*xStep + xStart;
				coordinates[2] = (idx2+1)*xStep + xStart;
				coordinates[1] = yStart + defHeight * (1 - ( (table[idx2][idx1] - extremes[0]) ) / (extremes[1] - extremes[0]) );
				coordinates[3] = yStart + defHeight * (1 - ( (table[idx2+1][idx1] - extremes[0]) ) / (extremes[1] - extremes[0]) );
				output[idx1].add(coordinates);
			}
		}

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

	private static Color[] getColors(int no)
	{
		int maxNoColors = 9;

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

		Color[] output = new Color[no];

		for(int idx=0;idx<no;++idx)
			output[idx] = colors[idx];

		return output;
	}
}
