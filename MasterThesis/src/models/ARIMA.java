package models;

import input.Data;
import math.LLARMAFunction;
import math.Matrix;
import math.NelderMead;

public class ARIMA extends Model {
	
	private double[] timeSeries;
	private double[] coefficients;
	private double logLikelihood;
	private double AIC;
	private double BIC;
	
	public ARIMA(Data data,int periods)
	{
		super(data,periods);
		noParameters = 2;
		noConstants = 1;
		name = "ARIMA";
	}

	public void train() 
	{
		if(parameters.length != noParameters)
		{
			{
				modelError("trainSES","inadequate no parameters");
				return;
			}
		}
		
		for(int idx=0;idx<noParameters;++idx)
		{
			if(parameters[idx] < 0)
			{
				modelError("train","parameter should be at least 0");
				return;
			}
		}
		
		determineNoDifferences();
		
		LLARMAFunction f = new LLARMAFunction(timeSeries,(int)parameters[0],(int)parameters[1]);
		NelderMead nm = new NelderMead(f);
		nm.optimize();
		coefficients = nm.getOptimalIntput();
		
		for(int idx=1;idx<(coefficients.length-1);++idx)
			coefficients[idx] = Math.tanh(coefficients[idx]);
		
		coefficients[coefficients.length-1] = Math.abs(coefficients[coefficients.length-1]);
		
		logLikelihood = -nm.getOptimalValue();
		calculateInformationCriteria();
		
		int index = data.getIndexFromCat(category);
		int firstIndex = data.getTrainingFirstIndex()[index];
		int noData1 = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index];
		int noData2 = data.getTestingFirstIndex()[index] - data.getValidationFirstIndex()[index];
		int noData3 = data.getNoObs() - data.getTestingFirstIndex()[index] - 1;
		
		initializeSets(noData1,noData2,noData3);
		
		for(int idx=0;idx<noData1;++idx)
		{
			trainingReal[idx] = data.getVolumes()[firstIndex+idx][index];
			trainingDates[idx] = data.getDates()[firstIndex+idx];
		}
		
		forecast("training");
		
		trainingForecasted = true;
	}

	public void validate() {}

	public void test() {}
	
	public double[] getTimeSeries()
	{
		return timeSeries;
	}
	
	public double[] getCoefficients()
	{
		return coefficients;
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
	
	private void determineNoDifferences()
	{
		int index = data.getIndexFromCat(category);
		double[] ts = new double[data.getValidationFirstIndex()[index]-data.getTrainingFirstIndex()[index]];
		double[] temp = new double[ts.length];
				
		for(int idx=0;idx<ts.length;++idx)
		{
			ts[idx] = data.getVolumes()[data.getTrainingFirstIndex()[index]+idx][index];
			temp[idx] = ts[idx];
		}
			
		int noDiffs = 0;
		
		while(DickyFuller(temp))
		{
			noDiffs ++;
			temp = new double[ts.length-1];
			
			for(int idx=0;idx<temp.length;++idx)
				temp[idx] = ts[idx+1]-ts[idx];
			
			ts = new double[temp.length];
			
			for(int idx=0;idx<temp.length;++idx)
				ts[idx] = temp[idx];
		}
		
		timeSeries = ts;
		constants = new double[1];
		constants[0] = noDiffs;		
	}
	
	private static boolean DickyFuller(double[] ts)
	{
		double[] Y1 = new double[ts.length-1];
		double[] deltaY = new double[Y1.length];
		double[] errors = new double[Y1.length];
		
		for(int idx=0;idx<Y1.length;++idx)
		{
			Y1[idx] = ts[idx];
			deltaY[idx] = ts[idx+1] - ts[idx];
		}
		
		double delta =  Matrix.innerProduct(Y1, deltaY) / Matrix.innerProduct(Y1,Y1);
		
		for(int idx=0;idx<Y1.length;++idx)
			errors[idx] = (deltaY[idx] - delta*Y1[idx]);
		
		double s2 = Matrix.innerProduct(errors,errors) / (Y1.length-1);
		double se = Math.sqrt(s2 / Matrix.innerProduct(Y1, Y1) );
		double tstat = Math.abs(delta/se);
		
		if(tstat < 1.95)
			return true;
		else 
			return false;
	}
	
	private void calculateInformationCriteria()
	{
		AIC = 2*(1+parameters[0]+parameters[1]) - 2*logLikelihood;
		BIC = 2*Math.log(timeSeries.length-parameters[0])*(1+parameters[0]+parameters[1]) - 2*logLikelihood;
	}
	
	private void forecast(String mode)
	{
		switch(mode)
		{
		case "training":
		{
			double[] forecast = new double[timeSeries.length];
			
			for(int idx=0;idx<parameters[0];++idx)
			{
				
			}
		}
		default:
		{
			System.out.println("Error (forecast): default case reached");
		}
		}
	}

}
