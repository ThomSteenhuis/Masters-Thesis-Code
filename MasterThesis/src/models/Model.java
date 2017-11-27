package models;

import java.util.Random;

import graph.LineGraph;
import input.Data;

public abstract class Model {

	protected Random r;
	protected Data data;
	protected int noPersAhead;
	protected double[] parameters;
	protected double[] constants;

	protected String name;
	protected int noParameters;
	protected int noConstants;

	protected String category;

	protected double logLikelihood;
	protected double AIC;
	protected double BIC;

	protected boolean trainingForecasted;
	protected double[] trainingForecast;
	protected double[] trainingReal;
	protected String[] trainingDates;

	protected boolean validationForecasted;
	protected double[] validationForecast;
	protected double[] validationReal;
	protected String[] validationDates;

	protected boolean testingForecasted;
	protected double[] testingForecast;
	protected double[] testingReal;
	protected String[] testingDates;

	public Model(Data dataset, int periods)
	{
		if( (periods <= 0) || periods >= (dataset.getNoObs()) )
		{
			throw new IllegalArgumentException();
		}
		else
		{
			data = dataset;
			category = "";
			noPersAhead = periods;
			trainingForecasted = false;
			validationForecasted = false;
			testingForecasted = false;
		}
	}

	public void setCategory(String cat)
	{
		try{
			data.getIndexFromCat(cat);
		}
		catch(NullPointerException e)
		{
			System.out.println("Error (setCategory): category not recognized");
			return;
		}
		category = cat;
	}

	public void setParameters(double[] newParameters)
	{
		if(newParameters.length == noParameters)
		{
			parameters = newParameters;
		}
		else
		{
			System.out.println("Error (setParameters): number of constants inadequate");
		}
	}

	public void setConstants(double[] newConstants)
	{
		if(newConstants.length == noConstants)
		{
			constants = newConstants;
		}
		else
		{
			System.out.println("Error (setConstants): number of constants inadequate");
		}
	}

	public abstract boolean train();

	public void plotForecast(String mode)
	{
		String[][] cats = new String[1][2];
		String[][] lbls = new String[1][];
		String[][] dts = new String[1][];
		cats[0][0] = category;
		cats[0][1] = "Forecast";
		lbls[0] = data.getLabels();
		
		

		switch (mode)
		{
			case "training":
			{
				if(!trainingForecasted)
				{
					modelError("plotForecast","train model first");
					return;
				}

				double[][][] vols = merge(trainingReal,trainingForecast);
				dts[0] = trainingDates;
				
				LineGraph lg = new LineGraph(vols,dts,cats,lbls);
				lg.plot();
				break;
			}
			case "validation":
			{
				if(!validationForecasted)
				{
					modelError("plotForecast","validate model first");
					return;
				}

				double[][][] vols = merge(validationReal,validationForecast);
				dts[0] = validationDates;
				
				LineGraph lg = new LineGraph(vols,dts,cats,lbls);
				lg.plot();
				break;
			}
			case "testing":
			{
				if(!testingForecasted)
				{
					modelError("plotForecast","test model first");
					return;
				}

				double[][][] vols = merge(testingReal,testingForecast);
				dts[0] = testingDates;

				LineGraph lg = new LineGraph(vols,dts,cats,lbls);
				lg.plot();
				break;
			}
			default:
			{
				modelError("plotForecast","default case reached");
			}
		}
	}

	public Data getData()
	{
		return data;
	}

	public int getNoPeriodsAhead()
	{
		return noPersAhead;
	}

	public double[] getParameters()
	{
		return parameters;
	}

	public double[] getConstants()
	{
		return constants;
	}

	public String getName()
	{
		return name;
	}

	public int getNoParameters()
	{
		return noParameters;
	}

	public int getNoConstants()
	{
		return noConstants;
	}

	public boolean isTrainingForecasted()
	{
		return trainingForecasted;
	}

	public String getCategory()
	{
		return category;
	}

	public double getLogLikelihood()
	{
		return logLikelihood;
	}

	public double getAIC()
	{
		return AIC;
	}

	public double getBIC()
	{
		return BIC;
	}

	public double[] getTrainingForecast()
	{
		return trainingForecast;
	}

	public double[] getTrainingReal()
	{
		return trainingReal;
	}

	public String[] getTrainingDates()
	{
		return trainingDates;
	}

	public double[] getValidationForecast()
	{
		return validationForecast;
	}

	public double[] getValidationReal()
	{
		return validationReal;
	}

	public String[] getValidationDates()
	{
		return validationDates;
	}

	public double[] getTestingForecast()
	{
		return testingForecast;
	}

	public double[] getTestingReal()
	{
		return testingReal;
	}

	public String[] getTestingDates()
	{
		return testingDates;
	}

	protected void initializeSets()
	{
		trainingReal = data.getTrainingSet(category);
		trainingForecast = new double[trainingReal.length];
		trainingDates = data.getTrainingDates(category);

		validationReal = data.getValidationSet(category);
		validationForecast = new double[validationReal.length];
		validationDates = data.getValidationDates(category);

		testingReal = data.getTestingSet(category);
		testingForecast = new double[testingReal.length];
		testingDates = data.getTestingDates(category);
	}

	protected static void modelError(String model, String txt)
	{
		System.out.printf("Error (%s): %s\n",model,txt);
	}

	private static double[][][] merge(double[] array1,double[] array2)
	{
		if(array1.length != array2.length)
		{
			System.out.println("Error (merge): arrays have unequal length");
			return null;
		}

		double[][][] output = new double[1][array1.length][2];

		for(int idx=0;idx<array1.length;++idx)
		{
			output[0][idx][0] = array1[idx];
			output[0][idx][1] = array2[idx];
		}

		return output;
	}
}
