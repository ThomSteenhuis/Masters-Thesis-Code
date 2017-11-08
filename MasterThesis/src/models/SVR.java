package models;

import input.Data;
import math.Matrix;

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
	}

	public void train() 
	{
		initializeXY();
		ellipsoidMethod();
	}

	public void validate() {}

	public void test() {}
	
	public double[][] getX()
	{
		return x;
	}
	
	public double[] getY()
	{
		return y;
	}
	
	public int getN()
	{
		return N;
	}
	
	public double[] getLambda()
	{
		return lambda;
	}

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
		double bestValue = 0;
		double[] bestCenter = new double[N];
		boolean firstValue = true;
		boolean stop = false;
		int count = 0;
		
		while(!stop)
		{
			count ++;
			double[] subDifferential;
			double alpha = 0;
			double normCoeff = 0;
			int infeasibleConstraint = checkFeasible(center);
			
			if(infeasibleConstraint == 0)
			{
				subDifferential = new double[N];
				
				for(int idx1=0;idx1<N;++idx1)
				{
					double sign = center[idx1] / Math.abs(center[idx1]);
					subDifferential[idx1] = parameters[1] * sign - y[idx1];
					
					for(int idx2=0;idx2<N;++idx2)
						subDifferential[idx1] += (center[idx2] * kernel(x[idx1],x[idx2]) );
				}
				
				normCoeff = Matrix.innerProduct(subDifferential,Matrix.innerProduct(ellipsMatrix,subDifferential) );
				normCoeff = 1 / Math.sqrt(normCoeff);
				
				for(int idx=0;idx<N;++idx)
					subDifferential[idx] = subDifferential[idx] * normCoeff;
				
				double currentValue = 0;
				
				for(int idx1=0;idx1<N;++idx1)
				{
					currentValue += parameters[1] * Math.abs(center[idx1]) - y[idx1] * center[idx1] + 0.5 * Math.pow(center[idx1] , 2);
					
					for(int idx2=0;idx2<idx1;++idx2)
						currentValue += center[idx1] * center[idx2] * kernel(x[idx1],x[idx2]);
				}
				
				System.out.printf("Current value = %.2f\n",currentValue);
				/*for(int idx=0;idx<N;++idx)
				{
					System.out.printf("%.2f\t", center[idx]);
				}*/
				
				if( firstValue || (currentValue < bestValue) )
				{
					bestValue = currentValue;
					System.out.printf("Best value = %.2f\n",bestValue);
					
					bestCenter = new double[N];
					
					for(int idx=0;idx<N;++idx)
					{
						bestCenter[idx] = center[idx];
						System.out.printf("%.2f\t", bestCenter[idx]);
					}
					System.out.println();
					
					firstValue = false;
				}
				
				alpha = (currentValue - bestValue) * normCoeff;
			}
			else
			{
				System.out.printf("Infeasible constraint = %d at iteration %d\n",infeasibleConstraint,count);
				
				double sum=0;
				for(int idx=0;idx<N;++idx)
				{
					sum+=center[idx];
					System.out.printf("%f\t", center[idx]);
				}
				System.out.println();
				System.out.println(sum);
				System.out.println(parameters[0]/(N+1));
				
				subDifferential = subDiffConstraint(infeasibleConstraint);
				
				for(int idx=0;idx<N;++idx)
				{
					System.out.printf("%f\t", subDifferential[idx]);
				}
				System.out.println();
				
				normCoeff = Matrix.innerProduct(subDifferential,Matrix.innerProduct(ellipsMatrix,subDifferential) );
				normCoeff = 1 / Math.sqrt(normCoeff);
				
				for(int idx=0;idx<N;++idx)
					subDifferential[idx] = subDifferential[idx] * normCoeff;
				
				alpha = currentConstraint(infeasibleConstraint,center) * normCoeff;
			}
			
			double[] pars1 = {1,1,1};
			double[] pars2 = {2,2,2};
			/*System.out.printf("%f\n", kernel(pars1,pars2));
			
			System.out.println(normCoeff);	
			
			for(int idx1=0;idx1<N;++idx1)
			{
				System.out.printf("%f\t",subDifferential[idx1]);
			}
			System.out.println();
			
			System.out.println(alpha);
			
			for(int idx1=0;idx1<N;++idx1)
			{
				for(int idx2=0;idx2<N;++idx2)
				{
					System.out.printf("%f\t",ellipsMatrix[idx1][idx2]);
				}
				System.out.println();
			}*/
			
			double scalar1 = (1 + N * alpha) / (N + 1);
			double scalar2 = Math.pow(N,2) / (Math.pow(N,2) - 1) * (1 - Math.pow(alpha,2) );
			double scalar3 = ( (2*(1+N*alpha) ) / ( (N+1) * (1+alpha) ) );
			
			double[] ellMat_subDiff = Matrix.innerProduct(ellipsMatrix,subDifferential);
			center = Matrix.difference(center,Matrix.scalarMultiplication(scalar1,ellMat_subDiff) );
			ellipsMatrix = Matrix.scalarMultiplication(scalar2,(Matrix.difference(ellipsMatrix,Matrix.scalarMultiplication(scalar3,Matrix.outerProduct(ellMat_subDiff,ellMat_subDiff) ) ) ) );
		
			System.out.println(1/normCoeff);
			
			if( ( (1/normCoeff) < stoppingError) && !firstValue || count == 5000 )
				stop = true;
		}
		
		System.out.printf("Stopped at iteration %d\n", count);
		
		lambda = bestCenter;
	}
	
	private double[] subDiffConstraint(int no)
	{
		double[] output = new double[N];
		
		if(no == (N+1) )
		{
			for(int idx=0;idx<N;++idx)
				output[idx] = 1;
		}
		else if(no == (-N-1) )
		{
			for(int idx=0;idx<N;++idx)
				output[idx] = -1;
		}
		else if(no>0)
			output[no-1] = 1;
		else
			output[-no-1] = -1;
		
		return output;
	}
	
	private double currentConstraint(int no,double[] center)
	{		
		if(no == (N+1) )
		{
			double output = -constraintError;
			
			for(int idx=0;idx<N;++idx)
				output += center[idx];
			
			return output;
		}
		else if(no == (-N-1))
		{
			double output = -constraintError;
			
			for(int idx=0;idx<N;++idx)
				output -= center[idx];
			
			return output;
		}
		else if(no>0)
			return (center[no-1] - parameters[0]);
		else
			return (-center[-no-1] - parameters[0]);
	}
	
	private double kernel(double[] first,double[] second)
	{
		double[] diff = Matrix.difference(first,second);
		
		double k = Math.exp( -Matrix.innerProduct(diff, diff) / (2 * parameters[2] ) );
		
		if(Double.isInfinite(k))
			return Double.MAX_VALUE;
		else
			return k;
	}
	
	private int checkFeasible(double[] c)
	{
		double sum = 0;
		
		for(int idx=0;idx<N;++idx)
		{
			if(c[idx] > parameters[0]) 
				return (idx+1);
			
			if(c[idx] < (-parameters[0]) )
				return -(idx+1);
			
			sum += c[idx];
		}
		
		if(sum > constraintError)
			return N+1;
		
		if(sum < (-constraintError) )
			return -N-1;
		
		return 0;
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
