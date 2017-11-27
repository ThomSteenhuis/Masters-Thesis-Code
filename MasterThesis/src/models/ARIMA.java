package models;

import java.util.Random;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;

import input.Data;
import math.LLARMAFunction;
import math.Matrix;
import math.NelderMead;

public class ARIMA extends Model {


	private double[] timeSeries;
	private double[] coefficients;
	private double[] residuals;

	public ARIMA(Data data,int periods, Random R)
	{
		super(data,periods);
		noParameters = 3;
		noConstants = 0;
		name = "ARIMA";
		r = R;
	}

	public boolean train()
	{
		if(parameters.length != noParameters)
		{
			{
				modelError("trainSES","inadequate no parameters");
				return false;
			}
		}

		for(int idx=0;idx<noParameters;++idx)
		{
			if(parameters[idx] < 0)
			{
				modelError("train","parameter should be at least 0");
				return false;
			}
		}
		
		createDifferences();
		LLARMAFunction f = new LLARMAFunction(this,(int)parameters[0],(int)parameters[1]);

		NelderMead nm = new NelderMead(f,r);
		nm.optimize();
		coefficients = new double[2+(int)parameters[0]+(int)parameters[1]];

		coefficients[0] = nm.getOptimalIntput()[0];

		for(int idx=1;idx<(coefficients.length-1);++idx)
			coefficients[idx] = Math.tanh(nm.getOptimalIntput()[idx]);

		coefficients[coefficients.length-1] = Math.abs(nm.getOptimalIntput()[coefficients.length-1]);
		calculateResiduals(coefficients);
		logLikelihood = -f.evaluate(coefficients);
		
		/*try {
			System.out.println("a");
			RConnection tmp = new RConnection();
			System.out.println("b");
			r.eval("source(\"C:/Users/emp5220514/Desktop/git/MasterThesis/src/data/arima_script.R\")");
			r = tmp;
			System.out.println("c");
			determineNoDifferences();
			System.out.println("d");
			REXP outcome = r.eval("arma(\"2200EVO\","+parameters[0]+","+parameters[1]+","+noPersAhead+")");
			System.out.println("e");
			r.close();
			coefficients = new double[outcome.asDoubles().length-2];
			
			for(int idx=0;idx<coefficients.length;++idx)
				coefficients[idx] = outcome.asDoubles()[idx];
			
			calculateResiduals(coefficients);
			logLikelihood = outcome.asDoubles()[outcome.asDoubles().length-2];
			
		} catch (RserveException |REXPMismatchException e) 
		{
			e.printStackTrace();
			return false;
		} */
		
		calculateInformationCriteria();
		initializeSets();		
		
		double[] forecast = forecast(convertRealData(),noPersAhead);

		for(int idx=0;idx<trainingReal.length;++idx)
			trainingForecast[idx] = forecast[idx];

		for(int idx=0;idx<validationReal.length;++idx)
			validationForecast[idx] = forecast[trainingReal.length+idx];

		for(int idx=0;idx<testingReal.length;++idx)
			testingForecast[idx] = forecast[trainingReal.length+validationReal.length+idx];
		
		trainingForecasted = true;
		validationForecasted = true;
		testingForecasted = true;
		return true;
	}

	public double[] getTimeSeries()
	{
		return timeSeries;
	}

	public double[] getCoefficients()
	{
		return coefficients;
	}

	public double[] getResiduals()
	{
		return residuals;
	}

	private void calculateResiduals(double[] coefficients)
	{
		residuals = new double[timeSeries.length];

		for(int idx1=(int)parameters[0];idx1<timeSeries.length;++idx1)
		{
			residuals[idx1] = timeSeries[idx1] - coefficients[0];

			for(int idx2=(idx1-(int)parameters[0]);idx2<idx1;++idx2)
				residuals[idx1] -= coefficients[idx1-idx2] * (timeSeries[idx2] - coefficients[0]);

			for(int idx2=Math.max(idx1-(int)parameters[1],0);idx2<idx1;++idx2)
				residuals[idx1] -= coefficients[idx1-idx2+(int)parameters[0]] * residuals[idx2];
		}
	}

	public double[] calculateErrors(double mu, double[] phi, double[] theta)
	{
		double[] error = new double[timeSeries.length];

		for(int idx1=(int)parameters[0];idx1<timeSeries.length;++idx1)
		{
			error[idx1] = timeSeries[idx1] - mu;

			for(int idx2=(idx1-(int)parameters[0]);idx2<idx1;++idx2)
				error[idx1] -= phi[idx1-idx2-1] * (timeSeries[idx2] - mu);

			for(int idx2=Math.max(idx1-(int)parameters[1],0);idx2<idx1;++idx2)
				error[idx1] -= theta[idx1-idx2-1] * error[idx2];
		}

		return error;
	}

	private void createDifferences()
	{
		int index = data.getIndexFromCat(category);
		double[] ts = new double[data.getValidationFirstIndex()[index]-data.getTrainingFirstIndex()[index]];
		double[] temp = new double[ts.length];

		for(int idx=0;idx<ts.length;++idx)
		{
			ts[idx] = data.getVolumes()[data.getTrainingFirstIndex()[index]+idx][index];
			temp[idx] = ts[idx];
		}
		
		if((int)parameters[2] == 0)
			timeSeries = ts;
		else
		{
			for(int idx1=0;idx1<parameters[2];++idx1)
			{
				temp = new double[ts.length-1];

				for(int idx2=0;idx2<temp.length;++idx2)
					temp[idx2] = ts[idx2+1]-ts[idx2];

				ts = new double[temp.length];

				for(int idx2=0;idx2<temp.length;++idx2)
					ts[idx2] = temp[idx2];
			}
			timeSeries = ts;
		}
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

	private double[] forecast(double[] ts,int noPersAhead)
	{
		double[] tmp = new double[ts.length];
		double[] errors = new double[tmp.length];
		
		for(int idx=0;idx<(parameters[0]);++idx)
			tmp[idx] = ts[idx];
		
		for(int idx1=(int)parameters[0];idx1<tmp.length;++idx1)
		{
			tmp[idx1] = coefficients[0];
			
			for(int idx2=idx1-(int)parameters[0];idx2<idx1;++idx2)
				tmp[idx1] += coefficients[idx1-idx2]*(ts[idx2]-coefficients[0]);

			for(int idx2=Math.max(0,idx1-(int)parameters[1]);idx2<idx1;++idx2)
				tmp[idx1] += coefficients[idx1-idx2+(int)parameters[1]]*errors[idx2];
		}
		
		if(noPersAhead > 1)
		{
			return forecast(tmp,noPersAhead-1);
		}
		else
		{			
			for(int idx=0;idx<(parameters[0]+noPersAhead-1);++idx)
				tmp[idx] = timeSeries[idx];
			
			return deriveData(tmp);
		}		
	}

	private double[] deriveData(double[] array)
	{
		if((int)parameters[2] == 0)
		{
			return array;
		}
		else
		{
			int index = data.getIndexFromCat(category);

			double[][] ts = new double[(int)parameters[2]][];

			ts[0] = new double[data.getNoObs()-data.getTrainingFirstIndex()[index]];

			for(int idx2=0;idx2<(int)parameters[2];++idx2)
				ts[0][idx2] = data.getVolumes()[data.getTrainingFirstIndex()[index]+idx2][index];

			for(int idx1=1;idx1<(int)parameters[2];++idx1)
			{
				ts[idx1] = new double[data.getNoObs()-data.getTrainingFirstIndex()[index]-idx1];

				for(int idx2=0;idx2<((int)parameters[2]-idx1);++idx2)
					ts[idx1][idx2] = ts[idx1-1][idx2] - ts[idx1-1][idx2+1];
			}

			for(int idx2=0;idx2<array.length;++idx2)
				ts[(int)parameters[2]-1][idx2+1] = ts[(int)parameters[2]-1][idx2] + array[idx2];

			for(int idx1=((int)parameters[2]-2);idx1>=0;--idx1)
			{
				for(int idx2=((int)parameters[2]-1-idx1);idx2<(ts[idx1].length-1);++idx2)
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

		for(int idx1=0;idx1<(int)parameters[2];++idx1)
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
