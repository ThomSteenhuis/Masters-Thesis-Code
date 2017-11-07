package performance;

import models.Model;

public class PerformanceMeasures {
	
	private Model model;
	private double RMSE;
	private double MAPE;
	private double MAE;
	private double ME;
	
	public PerformanceMeasures(Model mdl)
	{
		model = mdl;
	}
	
	public void calculateMeasures()
	{
		RMSE = calculateRMSE(model.getValidationReal(),model.getValidationForecast());
		MAPE = calculateMAPE(model.getValidationReal(),model.getValidationForecast());
		MAE = calculateMAE(model.getValidationReal(),model.getValidationForecast());
		ME = calculateME(model.getValidationReal(),model.getValidationForecast());
	}
	
	public Model getModel()
	{
		return model;
	}
	
	public double getRMSE()
	{
		return RMSE;
	}
	
	public double getMAPE()
	{
		return MAPE;
	}
	
	public double getMEA()
	{
		return MAE;
	}
	
	public double getME()
	{
		return ME;
	}
	
	private static double calculateRMSE(double[] real, double[] estimates)
	{		
		if(real.length != estimates.length)
		{
			System.out.println("Error (calculateME): real and estimates have unequal length");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<real.length;++idx)
		{
			output += Math.pow(estimates[idx]-real[idx],2);
		}
		
		output = output/real.length;
		output = Math.sqrt(output);	
		
		return output;
	}
	
	private static double calculateMAPE(double[] real, double[] estimates)
	{				
		if(real.length != estimates.length)
		{
			System.out.println("Error (calculateME): real and estimates have unequal length");
			return 0;
		}
		
		double numerator = 0;
		double denominator = 0;
		
		for(int idx=0;idx<real.length;++idx)
		{
			numerator += Math.abs(estimates[idx]-real[idx]);
			denominator += real[idx];
		}
		
		if(denominator == 0)
		{
			return Double.POSITIVE_INFINITY;
		}
		else
		{
			return 100*numerator/denominator;
		}
	}
	
	private static double calculateMAE(double[] real, double[] estimates)
	{		
		if(real.length != estimates.length)
		{
			System.out.println("Error (calculateME): real and estimates have unequal length");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<real.length;++idx)
		{
			output += Math.abs(estimates[idx]-real[idx]);
		}
		
		output = output/real.length;	
		
		return output;
	}
	
	private static double calculateME(double[] real, double[] estimates)
	{					
		if(real.length != estimates.length)
		{
			System.out.println("Error (calculateME): real and estimates have unequal length");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<real.length;++idx)
		{
			output += (estimates[idx]-real[idx]);
		}
		
		output = output/real.length;	
		
		return output;
	}
	
	public void printMeasures()
	{
		System.out.print("RMSE\tMAPE\tMAE\tME\tCategory\tModel\n");
		System.out.printf("%.2f\t%.2f\t%.2f\t%.2f\t%s\t%s\n",RMSE,MAPE,MAE,ME,model.getCategory(),model.getName());
	}
}

