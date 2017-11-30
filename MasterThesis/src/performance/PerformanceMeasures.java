package performance;

import models.Model;

public class PerformanceMeasures {
	
	private Model model;
	private double[] RMSE;
	private double[] MAPE;
	private double[] MAE;
	private double[] ME;
	
	public PerformanceMeasures(Model mdl)
	{
		model = mdl;
	}
	
	public void calculateMeasures(String mode)
	{
		double[][] real = new double[model.getNoOutputs()][]; double[][] forecast = new double[model.getNoOutputs()][];

		switch (mode)
		{
		case "training":
		{
			for(int idx=0;idx<model.getNoOutputs();++idx)
			{
				real[idx] = model.getTrainingReal()[idx];
				forecast[idx] = model.getTrainingForecast()[idx];
			}

			break;
		}
		case "validation":
		{
			for(int idx=0;idx<model.getNoOutputs();++idx)
			{
				real[idx] = model.getValidationReal()[idx];
				forecast[idx] = model.getValidationForecast()[idx];
			}
			
			break;
		}
		case "testing":
		{
			for(int idx=0;idx<model.getNoOutputs();++idx)
			{
				real[idx] = model.getTestingReal()[idx];
				forecast[idx] = model.getTestingForecast()[idx];
			}
			break;
		}
		default:
			real = new double[0][0];
			forecast = new double[0][0];
			System.out.println("Error (calculateMeasures): default case reached");
			return;
		}
		
		RMSE = new double[model.getNoOutputs()]; MAPE = new double[RMSE.length]; MAE = new double[RMSE.length]; ME = new double[RMSE.length];
		for(int idx=0;idx<model.getNoOutputs();++idx)
		{
			RMSE[idx] = calculateRMSE(real[idx],forecast[idx]);
			MAPE[idx] = calculateMAPE(real[idx],forecast[idx]);
			MAE[idx] = calculateMAE(real[idx],forecast[idx]);
			ME[idx] = calculateME(real[idx],forecast[idx]);
		}
	}
	
	public Model getModel()
	{
		return model;
	}
	
	public double[] getRMSE()
	{
		return RMSE;
	}
	
	public double[] getMAPE()
	{
		return MAPE;
	}
	
	public double[] getMAE()
	{
		return MAE;
	}
	
	public double[] getME()
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

