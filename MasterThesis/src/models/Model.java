package models;

import graph.Plot;
import input.Data;

public abstract class Model {

	protected Data data;
	protected int noPersAhead;
	protected double[] parameters;
	
	protected String name;
	protected int noParameters;
	
	protected String category;
	
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
			noPersAhead = periods;
			trainingForecasted = false;
		}
	}
	
	public void setCategory(String cat)
	{
		category = cat;
	}
	
	public abstract void train();
	public abstract void validate();
	public abstract void test();
	
	public void plotForecast(String mode)
	{
		String[] pars = new String[1];
		pars[0] = "pivot";
		
		String[] cats = new String[2];
		cats[0] = category;
		cats[1] = "Forecast";
		
		switch (mode)
		{
			case "training":
			{
				if(!trainingForecasted)
				{
					modelError("plotForecast","train model first");
					return;
				}
				
				double[][] vols = merge(trainingReal,trainingForecast);
				
				Plot.initialize(pars,vols,trainingDates,cats,data.getLabels());
				break;
			}
			case "validation":
			{
				if(!validationForecasted)
				{
					modelError("plotForecast","validate model first");
					return;
				}
				
				double[][] vols = merge(validationReal,validationForecast);
				
				Plot.initialize(pars,vols,validationDates,cats,data.getLabels());
				break;
			}
			case "testing":
			{
				if(!testingForecasted)
				{
					modelError("plotForecast","test model first");
					return;
				}
				
				double[][] vols = merge(testingReal,testingForecast);
				
				Plot.initialize(pars,vols,testingDates,cats,data.getLabels());
				break;
			}
			default:
			{
				modelError("plotForecast","default case reached");
			}
		}
	}
	
	public void setParameters(double[] newPars)
	{
		if(newPars.length == noParameters)
		{
			parameters = newPars;
		}
		else
		{
			System.out.println("Error (setParameters): number of parameters inadequate");
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
	
	public String getName()
	{
		return name;
	}
	
	public int getNoParameters()
	{
		return noParameters;
	}
	
	public boolean isTrainingForecasted()
	{
		return trainingForecasted;
	}
	
	public String getCategory()
	{
		return category;
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
	
	protected static void modelError(String model, String txt)
	{
		System.out.printf("Error (%s): %s\n",model,txt);
	}
	
	private static double[][] merge(double[] array1,double[] array2)
	{
		if(array1.length != array2.length)
		{
			System.out.println("Error (merge): arrays have unequal length");
			return null;
		}
		
		double[][] output = new double[array1.length][2];
		
		for(int idx=0;idx<array1.length;++idx)
		{
			output[idx][0] = array1[idx];
			output[idx][1] = array2[idx];
		}
		
		return output;
	}
}
