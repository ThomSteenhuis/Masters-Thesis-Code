package models;

import java.util.Random;

import input.Data;
import math.Matrix;

public class SVR extends Model {

	private double[][] x;
	private double[] y;
	private int N;
	private double[] lambda;

	private final double stoppingError = 0.001;
	private final int maxIters = 100000;

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

	public void printLambda()
	{
		for(int idx=0;idx<N;++idx)
			System.out.printf("%f\t",lambda[idx]);

		System.out.println();
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
		int iter = 0;

		while(!stop)
		{
			iter ++;
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
				subDifferential = Matrix.scalarMultiplication(normCoeff,subDifferential);

				double currentValue = objective(center);

				if( firstValue || (currentValue < bestValue) )
				{
					bestValue = currentValue;
					bestCenter = new double[N];

					for(int idx=0;idx<N;++idx)
						bestCenter[idx] = center[idx];

					firstValue = false;
				}

				alpha = (currentValue - bestValue) * normCoeff;
			}
			else
			{
				subDifferential = subDiffConstraint(infeasibleConstraint);
				normCoeff = Matrix.innerProduct(subDifferential,Matrix.innerProduct(ellipsMatrix,subDifferential) );
				normCoeff = 1 / Math.sqrt(normCoeff);
				subDifferential = Matrix.scalarMultiplication(normCoeff,subDifferential);
				alpha = currentConstraint(infeasibleConstraint,center) * normCoeff;
			}

			double scalar1 = (1 + N * alpha) / (N + 1);
			double scalar2 = Math.pow(N,2) / (Math.pow(N,2) - 1) * (1 - Math.pow(alpha,2) );
			double scalar3 = ( (2*(1+N*alpha) ) / ( (N+1) * (1+alpha) ) );

			double[] ellMat_subDiff = Matrix.innerProduct(ellipsMatrix,subDifferential);
			center = Matrix.difference(center,Matrix.scalarMultiplication(scalar1,ellMat_subDiff) );
			ellipsMatrix = Matrix.scalarMultiplication(scalar2,(Matrix.difference(ellipsMatrix,Matrix.scalarMultiplication(scalar3,Matrix.outerProduct(ellMat_subDiff,ellMat_subDiff) ) ) ) );

			if( ( (1/normCoeff) < stoppingError) && !firstValue || iter == maxIters )
				stop = true;
		}

		System.out.printf("Stopped at iteration %d. Best value = %f\n", iter,bestValue);

		lambda = bestCenter;
	}

	public double objective(double[] input)
	{
		double output = 0;

		for(int idx1=0;idx1<N;++idx1)
		{
			output += parameters[1] * Math.abs(input[idx1]) - y[idx1] * input[idx1] + 0.5 * Math.pow(input[idx1] , 2);

			for(int idx2=0;idx2<idx1;++idx2)
				output += input[idx1] * input[idx2] * kernel(x[idx1],x[idx2]);
		}

		return output;
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
			double output = -stoppingError;

			for(int idx=0;idx<N;++idx)
				output += center[idx];

			return output;
		}
		else if(no == (-N-1))
		{
			double output = -stoppingError;

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

		if(sum > stoppingError)
			return N+1;

		if(sum < (-stoppingError) )
			return -N-1;

		return 0;
	}

	private double[] initializeCenter()
	{
		double[] c = new double[N];
		double sum = 0;
		double boundary = parameters[0]/(N-1);
		Random r = new Random();

		for(int idx=0;idx<(N-1);++idx)
		{
			c[idx] = boundary * (2 * r.nextDouble() - 1);
			sum += c[idx];
		}

		c[N-1] = -sum;
		return c;
	}

	private double[][] initializeEllipsMatrix(double[] c)
	{
		double[][] P = new double[N][N];
		double maxRadius = Math.pow(2*parameters[0],2) + (N-1) * Math.pow(2*stoppingError,2);

		for(int idx=0;idx<N;++idx)
		{
			P[idx][idx] = 1000;
		}

		return P;
	}
}
