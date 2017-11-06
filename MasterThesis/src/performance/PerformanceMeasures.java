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
	
	public void validate()
	{
		RMSE = calculateRMSE(model.getTrainingReal(),model.getTrainingForecast());
		MAPE = calculateMAPE(model.getTrainingReal(),model.getTrainingForecast());
		MAE = calculateMAE(model.getTrainingReal(),model.getTrainingForecast());
		ME = calculateME(model.getTrainingReal(),model.getTrainingForecast());
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
	
	public double[] getMEA()
	{
		return MAE;
	}
	
	public double[] getME()
	{
		return ME;
	}
	
	private static double[] calculateRMSE(double[][] real, double[][] estimates)
	{
		double[] output = new double[real.length];
		
		for(int idx1=0;idx1<real.length;++idx1)
		{
			if(real[idx1].length != estimates[idx1].length)
			{
				System.out.println("Error (calculateME): real and estimates have unequal length");
				return null;
			}
			
			output[idx1] = 0;
			
			for(int idx2=0;idx2<real[idx1].length;++idx2)
			{
				output[idx1] += Math.pow(estimates[idx1][idx2]-real[idx1][idx2],2);
			}
			
			output[idx1] = output[idx1]/real[idx1].length;
			output[idx1] = Math.sqrt(output[idx1]);
		}		
		
		return output;
	}
	
	private static double[] calculateMAPE(double[][] real, double[][] estimates)
	{		
		double[] output = new double[real.length];
		
		for(int idx1=0;idx1<real.length;++idx1)
		{
			if(real[idx1].length != estimates[idx1].length)
			{
				System.out.println("Error (calculateME): real and estimates have unequal length");
				return null;
			}
			
			double numerator = 0;
			double denominator = 0;
			
			for(int idx2=0;idx2<real[idx1].length;++idx2)
			{
				numerator += Math.abs(estimates[idx1][idx2]-real[idx1][idx2]);
				denominator += real[idx1][idx2];
			}
			
			if(denominator == 0)
			{
				output[idx1] = Double.POSITIVE_INFINITY;
			}
			else
			{
				output[idx1] = 100*numerator/denominator;
			}
		}		
		
		return output;
	}
	
	private static double[] calculateMAE(double[][] real, double[][] estimates)
	{
		double[] output = new double[real.length];
		
		for(int idx1=0;idx1<real.length;++idx1)
		{
			if(real[idx1].length != estimates[idx1].length)
			{
				System.out.println("Error (calculateME): real and estimates have unequal length");
				return null;
			}
			
			output[idx1] = 0;
			
			for(int idx2=0;idx2<real[idx1].length;++idx2)
			{
				output[idx1] += Math.abs(estimates[idx1][idx2]-real[idx1][idx2]);
			}
			
			output[idx1] = output[idx1]/real[idx1].length;
		}		
		
		return output;
	}
	
	private static double[] calculateME(double[][] real, double[][] estimates)
	{			
		double[] output = new double[real.length];
		
		for(int idx1=0;idx1<real.length;++idx1)
		{
			if(real[idx1].length != estimates[idx1].length)
			{
				System.out.println("Error (calculateME): real and estimates have unequal length");
				return null;
			}
			
			output[idx1] = 0;
			
			for(int idx2=0;idx2<real[idx1].length;++idx2)
			{
				output[idx1] += (estimates[idx1][idx2]-real[idx1][idx2]);
			}
			
			output[idx1] = output[idx1]/real[idx1].length;
		}		
		
		return output;
	}
	
	public static void printMeasures(String[] categories, double[] rmse, double[] mape, double[] mae, double[] me)
	{
		System.out.print("RMSE\tMAPE\tMAE\tME\tMethod\n");
		
		for(int idx=0;idx<categories.length;++idx)
		{
			System.out.printf("%.2f\t%.2f\t%.2f\t%.2f\t%s\n",rmse[idx],mape[idx],mae[idx],me[idx],categories[idx]);
		}
	}
}

