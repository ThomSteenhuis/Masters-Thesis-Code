package models;

import input.Data;

public class SVR extends Model {
	
	private double[][] x;
	private double[] y;
	private int N;
	private double[] lambda;
	
	private final double constraintError = 0.001;
	private final double stoppingError = 0.000001;

	public SVR(Data dataset, int periods) 
	{
		super(dataset, periods);
		name = "SVR";
		noParameters = 4;
		noConstants = 0;
		initializeXY();
	}

	public void train() 
	{
		ellipsoidMethod();
	}

	public void validate() {}

	public void test() {}

	private void initializeXY()
	{
		N = data.getNoObs() - (int) (parameters[3]) - noPersAhead;
		x = new double[N][(int) (parameters[3])];
		y = new double[N];
		int index = data.getIndexFromCat(category);
		
		for(int idx1=0;idx1<N;++idx1)
		{
			y[idx1] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead][index];
			
			for(int idx2=0;idx2<x[idx1].length;++idx2)
			{
				x[idx1][idx2] =  data.getVolumes()[idx1 + idx2][index];
			}
		}
	}
	
	private void ellipsoidMethod()
	{
		double[] center = initializeCenter();
		double[][] ellipsMatrix = initializeEllipsMatrix(center);
		double bestValue;
		boolean firstValue = false;
		boolean stop = false;
		
		while(!stop)
		{
			if(checkFeasible(center))
			{
				
			}
			else
			{
				
			}
		}
		
	}
	
	private boolean checkFeasible(double[] c)
	{
		double sum = 0;
		
		for(int idx=0;idx<N;++idx)
		{
			if( (c[idx] > parameters[0]) || (c[idx] < (-parameters[0]) ) )
				return false;
			
			sum += c[idx];
		}
		
		if( (sum > constraintError) || (sum < (-constraintError) ) )
			return false;
		
		return true;
	}
	
	private double[] initializeCenter()
	{
		double[] c = new double[N];
		double sum = 0;
		double boundary = parameters[0]/(N-1);
		
		for(int idx=0;idx<(N-1);++idx)
		{
			c[idx] = boundary * (2 * Math.random() - 1);
			sum += c[idx];
		}
		
		c[N-1] = -sum;
		return c;
	}
	
	private double[][] initializeEllipsMatrix(double[] c)
	{
		double[][] P = new double[N][N];
		double maxRadius = Math.pow(2*parameters[0],2) + (N-1) * Math.pow(2*constraintError,2);
		
		for(int idx=0;idx<N;++idx)
		{
			P[idx][idx] = 1/maxRadius;
		}
		
		return P;
	}
}
