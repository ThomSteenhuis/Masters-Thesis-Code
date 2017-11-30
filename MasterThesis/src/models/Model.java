package models;

import java.util.Random;

import graph.LineGraph;
import input.Data;

public abstract class Model {

	protected Random r;
	protected Data data;
	
	protected double[] parameters;
	protected double[] constants;

	protected String name;
	protected int noParameters;
	protected int noConstants;
	protected int noInputs;
	protected int noOutputs;

	protected String[] category;
	protected int[] noPersAhead;
	
	protected double logLikelihood;
	protected double AIC;
	protected double BIC;

	protected boolean[] forecasted;
	protected double[][] trainingForecast;
	protected double[][] trainingReal;
	protected String[][] trainingDates;

	protected double[][] validationForecast;
	protected double[][] validationReal;
	protected String[][] validationDates;

	protected double[][] testingForecast;
	protected double[][] testingReal;
	protected String[][] testingDates;

	public Model(Data dataset, int[] periods, String[] cat)
	{
		if( (periods.length == 0) || (cat.length == 0) ) throw new IllegalArgumentException();
		else
		{
			for(int idx=0;idx<periods.length;++idx)
			{
				if( (periods[idx] <= 0) || (periods[idx] >= (dataset.getNoObs() ) ) ) throw new IllegalArgumentException();
			}
			
			data = dataset;
			setCategory(cat);
			noPersAhead = periods;
			noOutputs = category.length*noPersAhead.length;
			forecasted = new boolean[noOutputs];
		}
	}

	public void setCategory(String[] cat)
	{
		try{
			for(int idx=0;idx<cat.length;++idx) data.getIndexFromCat(cat[idx]);
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
	
	public void setNoInputs(int no)
	{
		noInputs = no;
	}

	public abstract boolean train();

	public void plotForecast(String mode,int forecastNo)
	{
		String[][] cats = new String[1][2];
		String[][] lbls = new String[1][];
		String[][] dts = new String[1][];
		cats[0][0] = category[forecastNo];
		cats[0][1] = "Forecast";
		lbls[0] = data.getLabels();		

		switch (mode)
		{
			case "training":
			{
				if(!forecasted[forecastNo])
				{
					modelError("plotForecast","train model first");
					return;
				}

				double[][][] vols = merge(trainingReal[forecastNo],trainingForecast[forecastNo]);
				dts[0] = trainingDates[forecastNo];
				
				LineGraph lg = new LineGraph(vols,dts,cats,lbls);
				lg.plot();
				break;
			}
			case "validation":
			{
				if(!forecasted[forecastNo])
				{
					modelError("plotForecast","validate model first");
					return;
				}

				double[][][] vols = merge(validationReal[forecastNo],validationForecast[forecastNo]);
				dts[0] = validationDates[forecastNo];
				
				LineGraph lg = new LineGraph(vols,dts,cats,lbls);
				lg.plot();
				break;
			}
			case "testing":
			{
				if(!forecasted[forecastNo])
				{
					modelError("plotForecast","test model first");
					return;
				}

				double[][][] vols = merge(testingReal[forecastNo],testingForecast[forecastNo]);
				dts[0] = testingDates[forecastNo];

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

	public int[] getNoPeriodsAhead()
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
	
	public int getNoOutputs()
	{
		return noOutputs;
	}
	
	public int getNoInputs()
	{
		return noInputs;
	}

	public boolean[] isForecasted()
	{
		return forecasted;
	}

	public String[] getCategory()
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

	public double[][] getTrainingForecast()
	{
		return trainingForecast;
	}

	public double[][] getTrainingReal()
	{
		return trainingReal;
	}

	public String[][] getTrainingDates()
	{
		return trainingDates;
	}

	public double[][] getValidationForecast()
	{
		return validationForecast;
	}

	public double[][] getValidationReal()
	{
		return validationReal;
	}

	public String[][] getValidationDates()
	{
		return validationDates;
	}

	public double[][] getTestingForecast()
	{
		return testingForecast;
	}

	public double[][] getTestingReal()
	{
		return testingReal;
	}

	public String[][] getTestingDates()
	{
		return testingDates;
	}

	protected void initializeSets()
	{
		trainingReal = new double[noOutputs][]; trainingForecast = new double[noOutputs][]; trainingDates = new String[noOutputs][];
		validationReal = new double[noOutputs][]; validationForecast = new double[noOutputs][]; validationDates = new String[noOutputs][];
		testingReal = new double[noOutputs][]; testingForecast = new double[noOutputs][]; testingDates = new String[noOutputs][];
		
		for(int idx=0;idx<noOutputs;++idx)
		{
			trainingReal[idx] = data.getTrainingSet(category[idx]);
			trainingForecast[idx] = new double[trainingReal.length];
			trainingDates[idx] = data.getTrainingDates(category[idx]);

			validationReal[idx] = data.getValidationSet(category[idx]);
			validationForecast[idx] = new double[validationReal.length];
			validationDates[idx] = data.getValidationDates(category[idx]);

			testingReal[idx] = data.getTestingSet(category[idx]);
			testingForecast[idx] = new double[testingReal.length];
			testingDates[idx] = data.getTestingDates(category[idx]);
		}
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
