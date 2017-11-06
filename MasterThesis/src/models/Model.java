package models;

import graph.Plot;
import input.Data;

public abstract class Model {

	protected Data data;
	protected int noPersAhead;
	protected double[] parameters;
	
	protected String name;
	protected int noParameters;
	
	protected boolean trainingForecasted;
	protected String category;
	protected double[] trainingForecast;
	protected double[] trainingReal;
	protected String[] trainingDates;
	
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
	
	public abstract void train(String category);
	
	public void plotTrainingForecast()
	{
		if(!trainingForecasted)
		{
			System.out.println("Error (plotTrainingForecast): train model first");
			return;
		}
				
		String[] pars = new String[1];
		pars[0] = "pivot";
		
		String[] cats = new String[2];
		cats[0] = category;
		cats[1] = "Forecast";
		
		double[][] vols = merge(trainingReal,trainingForecast);
		
		Plot.initialize(pars,vols,trainingDates,cats,data.getLabels());
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
