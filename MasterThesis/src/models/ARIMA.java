package models;

import input.Data;
import math.Matrix;

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
