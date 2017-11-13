package models;

import input.Data;
import math.LLAR1Function;
import math.Matrix;
import math.NelderMead;

public class ARIMA extends Model {
	
	private double[] timeSeries;
	
	public ARIMA(Data data,int periods)
	{
		super(data,periods);
		noParameters = 2;
		noConstants = 1;
		name = "ARIMA";
	}

	public void train() 
	{
		determineNoDifferences();
		
		LLAR1Function f = new LLAR1Function(timeSeries);
		NelderMead nm = new NelderMead(f);
		nm.optimize();
		System.out.println(nm.isConverged());
		System.out.println(nm.getOptimalValue());
		double[] input = nm.getOptimalIntput();
		input[1] = Math.tanh(input[1]);
		input[2] = Math.pow(input[2], 2);
		Matrix.print(input);
		
		double[] target = new double[3];
		target[0] = timeSeries[0] + timeSeries[timeSeries.length-1];
		
		for(int idx=1;idx<(timeSeries.length-1);++idx)
			target[0] += (1-input[1])*timeSeries[idx];
		
		target[0] = target[0]/(2+(timeSeries.length-2)*(1-input[1]));
		
		target[1] = input[1]*( (Math.pow(timeSeries[0]-input[0],2) - input[2]/(1-Math.pow(input[1],2) ) ) );
		
		for(int idx=1;idx<timeSeries.length;++idx)
			target[1] += ( (timeSeries[idx] - input[0] - input[1]*(timeSeries[idx-1] - input[0]) ) * (timeSeries[idx-1] - input[0]) );
		
		target[2] = Math.pow(timeSeries[0]-input[0], 2)*(1-Math.pow(input[1],2) );
		
		for(int idx=1;idx<timeSeries.length;++idx)
			target[2] += Math.pow(timeSeries[idx] - input[0] - input[1]*(timeSeries[idx-1] - input[0]), 2);
		
		target[2] = target[2]/timeSeries.length;
		
		System.out.printf("%f\t%f\t%f\n",target[0],target[1],target[2]);
		
		/*LLAR1Function f2 = new LLAR1Function(data.getVolumes()[data.getIndexFromCat(category)]);
		NelderMead nm2 = new NelderMead(f2);
		nm2.optimize();
		System.out.println(nm2.isConverged());
		System.out.println(nm2.getOptimalValue());
		double[] input2 = nm.getOptimalIntput();
		input2[1] = Math.tanh(input2[1]);
		input2[2] = Math.pow(input2[2], 2);
		Matrix.print(input2);*/
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void test() {
		// TODO Auto-generated method stub

	}
	
	public double[] getTimeSeries()
	{
		return timeSeries;
	}
	
	private void determineNoDifferences()
	{
		int index = data.getIndexFromCat(category);
		double[] ts = new double[data.getNoObs()-data.getTrainingFirstIndex()[index]];
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
		
		Matrix.print(Y1);
		Matrix.print(deltaY);
		Matrix.print(errors);
		System.out.printf("%f\t%f\t%f\t%f\n",delta,s2,se,tstat);
		
		if(tstat < 1.95)
			return true;
		else 
			return false;
	}

}
