package models;

import java.util.Random;

import input.Data;
import math.LLARMAFunction;
import math.Matrix;
import math.NelderMead;

public class ARIMA extends Model {
	
	private int seed;
	
	private double[] timeSeries;
	private double[] coefficients;
	
	public ARIMA(Data data,int periods,int s)
	{
		super(data,periods);
		noParameters = 2;
		noConstants = 1;
		name = "ARIMA";
		seed = s;
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
		
		forecast(seed);
		
		trainingForecasted = true;
		validationForecasted = true;
		testingForecasted = true;
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
		AIC = 2*(2+parameters[0]+parameters[1]) - 2*logLikelihood;
		BIC = 2*Math.log(timeSeries.length-parameters[0])*(2+parameters[0]+parameters[1]) - 2*logLikelihood;
	}
	
	private void forecast(int seed)
	{
		double[] realConverted = convertRealData();
		double[] forecastConverted = new double[realConverted.length];
		double[] errors = new double[realConverted.length];
		Random r = new Random(seed);
		
		for(int idx=0;idx<realConverted.length;++idx)
			errors[idx] = r.nextGaussian()*Math.sqrt(coefficients[coefficients.length-1]);
		
		for(int idx=0;idx<(int)parameters[0];++idx)
			forecastConverted[idx] = realConverted[idx];
		
		for(int idx1=(int)parameters[0];idx1<realConverted.length;++idx1)
		{
			forecastConverted[idx1] = coefficients[0];
			
			for(int idx2=(idx1-(int)parameters[0]);idx2<idx1;++idx2)
				forecastConverted[idx1] += coefficients[idx1-idx2]*realConverted[idx2];
			
			for(int idx2=Math.max(0,idx1-(int)parameters[1]);idx2<idx1;++idx2)
				forecastConverted[idx1] += coefficients[idx1-idx2+(int)parameters[0]]*errors[idx2];
		}
		
		double[] real = deriveData(realConverted);
		double[] forecast = deriveData(forecastConverted);
		int index = data.getIndexFromCat(category);
		
		for(int idx=0;idx<trainingReal.length;++idx)
		{
			trainingReal[idx] = real[idx];
			trainingForecast[idx] = forecast[idx];
			trainingDates[idx] = data.getDates()[data.getTrainingFirstIndex()[index]+idx];
		}
		
		for(int idx=0;idx<validationReal.length;++idx)
		{
			validationReal[idx] = real[trainingReal.length+idx];
			validationForecast[idx] = forecast[trainingReal.length+idx];
			validationDates[idx] = data.getDates()[data.getTrainingFirstIndex()[index]+trainingReal.length+idx];
		}
		
		for(int idx=0;idx<testingReal.length;++idx)
		{
			testingReal[idx] = real[trainingReal.length+validationReal.length+idx];
			testingForecast[idx] = forecast[trainingReal.length+validationReal.length+idx];
			testingDates[idx] = data.getDates()[data.getTrainingFirstIndex()[index]+trainingReal.length+validationReal.length+idx];
		}
	}
	
	private double[] deriveData(double[] array)
	{
		if((int)constants[0] == 0)
		{
			return array;
		}
		else
		{
			int index = data.getIndexFromCat(category);
			
			double[][] ts = new double[(int)constants[0]][];
			
			ts[0] = new double[data.getNoObs()-data.getTrainingFirstIndex()[index]];
			
			for(int idx2=0;idx2<(int)constants[0];++idx2)
				ts[0][idx2] = data.getVolumes()[data.getTrainingFirstIndex()[index]+idx2][index];
					
			for(int idx1=1;idx1<(int)constants[0];++idx1)
			{
				ts[idx1] = new double[data.getNoObs()-data.getTrainingFirstIndex()[index]-idx1];
				
				for(int idx2=0;idx2<((int)constants[0]-idx1);++idx2)
					ts[idx1][idx2] = ts[idx1-1][idx2] - ts[idx1-1][idx2+1];
			}
			
			for(int idx2=0;idx2<array.length;++idx2)
				ts[(int)constants[0]-1][idx2+1] = ts[(int)constants[0]-1][idx2] + array[idx2];
			
			for(int idx1=((int)constants[0]-2);idx1>=0;--idx1)
			{			
				for(int idx2=((int)constants[0]-1-idx1);idx2<(ts[idx1].length-1);++idx2)
					ts[idx1][idx2+1] = ts[idx1][idx2] + ts[idx1+1][idx2];
			}
			
			return ts[0];
		}
	}

	private double[] convertRealData()
	{
		int index = data.getIndexFromCat(category);
		
		double[] ts = new double[data.getNoObs()-data.getTrainingFirstIndex()[index]];
		double[] temp = new double[ts.length];
				
		for(int idx=0;idx<ts.length;++idx)
		{
			ts[idx] = data.getVolumes()[data.getTrainingFirstIndex()[index]+idx][index];
			temp[idx] = ts[idx];
		}
			
		for(int idx1=0;idx1<(int)constants[0];++idx1)
		{
			temp = new double[ts.length-1];
			
			for(int idx2=0;idx2<temp.length;++idx2)
				temp[idx2] = ts[idx2+1]-ts[idx2];
			
			ts = new double[temp.length];
			
			for(int idx2=0;idx2<temp.length;++idx2)
				ts[idx2] = temp[idx2];
		}
		
		return ts;
	}
}
