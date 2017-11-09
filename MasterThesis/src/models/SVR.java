package models;

import java.util.Random;

import input.Data;
import math.Matrix;

public class SVR extends Model {

	private double[][] x_train;
	private double[][] x_validate;
	private double[][] x_test;
	private double[] y_train;
	private double[] y_validate;
	private double[] y_test;
	private int N_train;
	private int N_validate;
	private int N_test;
	
	private double[] lambda;
	private double error;
	private int noIters;
	private double bias;
	
	private final double stoppingError = 0.001;
	private final int maxIters = 500000;

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
		calculateBias();
		forecast();
	}

	public void validate() {}

	public void test() {}

	public double[][] getXtrain()
	{
		return x_train;
	}

	public double[] getYtrain()
	{
		return y_train;
	}

	public int getNtrain()
	{
		return N_train;
	}
	
	public double[][] getXvalidate()
	{
		return x_validate;
	}

	public double[] getYvalidate()
	{
		return y_validate;
	}

	public int getNvalidate()
	{
		return N_validate;
	}
	
	public double[][] getXtest()
	{
		return x_test;
	}

	public double[] getYtest()
	{
		return y_test;
	}

	public int getNtest()
	{
		return N_test;
	}

	public double[] getLambda()
	{
		return lambda;
	}
	
	public double getError()
	{
		return error;
	}
	
	public int getNoIters()
	{
		return noIters;
	}
	
	public double getBias()
	{
		return bias;
	}

	public void printLambda()
	{
		for(int idx=0;idx<N_train;++idx)
			System.out.printf("%f\t",lambda[idx]);

		System.out.println();
	}

	private void initializeXY()
	{
		int index = data.getIndexFromCat(category);
		
		N_train = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index] - (int) (parameters[3]) - noPersAhead;
		x_train = new double[N_train][(int) (parameters[3])];
		y_train = new double[N_train];

		for(int idx1=0;idx1<N_train;++idx1)
		{
			y_train[idx1] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead - 1][index];

			for(int idx2=0;idx2<x_train[idx1].length;++idx2)
			{
				x_train[idx1][idx2] =  data.getVolumes()[idx1 + idx2][index];
			}
		}
		
		N_validate = data.getTestingFirstIndex()[index] - data.getValidationFirstIndex()[index];
		x_validate = new double[N_validate][(int) (parameters[3])];
		y_validate = new double[N_validate];

		for(int idx1=0;idx1<N_validate;++idx1)
		{
			y_validate[idx1] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead - 1 + N_train][index];

			for(int idx2=0;idx2<x_validate[idx1].length;++idx2)
			{
				x_validate[idx1][idx2] =  data.getVolumes()[idx1 + idx2 + N_train][index];
			}
		}
		
		N_test = data.getNoObs() - data.getTestingFirstIndex()[index];
		x_test = new double[N_test][(int) (parameters[3])];
		y_test = new double[N_test];

		for(int idx1=0;idx1<N_test;++idx1)
		{
			y_test[idx1] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead - 1 + N_train + N_validate][index];

			for(int idx2=0;idx2<x_test[idx1].length;++idx2)
			{
				x_test[idx1][idx2] =  data.getVolumes()[idx1 + idx2 + N_train + N_validate][index];
			}
		}
	}
	
	private void forecast()
	{
		trainingForecast = new double[y_train.length];
		trainingReal = new double[y_train.length];
		trainingDates = new String[y_train.length];
		
		for(int idx=0;idx<N_train;++idx)
		{
			trainingReal[idx] = y_train[idx];
			trainingDates[idx] = data.getDates()[idx + (int) (parameters[3]) + noPersAhead];
			trainingForecast[idx] = bias;
			
			for(int idx2=0;idx2<x_train.length;++idx2)
			{
				trainingForecast[idx] += lambda[idx2] * kernel(x_train[idx2],x_train[idx]);
			}
		}
		
		validationForecast = new double[y_validate.length];
		validationReal = new double[y_validate.length];
		validationDates = new String[y_validate.length];
		
		for(int idx=0;idx<N_validate;++idx)
		{
			validationReal[idx] = y_validate[idx];
			validationDates[idx] = data.getDates()[idx + (int) (parameters[3]) + noPersAhead + N_train];
			validationForecast[idx] = bias;
			
			for(int idx2=0;idx2<x_train.length;++idx2)
			{
				validationForecast[idx] += lambda[idx2] * kernel(x_train[idx2],x_validate[idx]);
			}
		}
		
		testingForecast = new double[y_test.length];
		testingReal = new double[y_test.length];
		testingDates = new String[y_test.length];
		
		for(int idx=0;idx<N_test;++idx)
		{
			testingReal[idx] = y_test[idx];
			testingDates[idx] = data.getDates()[idx + (int) (parameters[3]) + noPersAhead + N_train + N_validate];
			testingForecast[idx] = bias;
			
			for(int idx2=0;idx2<x_train.length;++idx2)
			{
				testingForecast[idx] += lambda[idx2] * kernel(x_train[idx2],x_test[idx]);
			}
		}
		
		trainingForecasted = true;
		validationForecasted = true;
		testingForecasted = true;
	}
	
	private void calculateBias()
	{
		double sumErrors = 0;
		
		for(int idx1=0;idx1<N_train;++idx1)
		{
			sumErrors += y_train[idx1];
			
			for(int idx2=0;idx2<N_train;++idx2)
			{
				sumErrors  -= lambda[idx2] * kernel(x_train[idx2],x_train[idx1]);
			}
		}
		
		bias = sumErrors / N_train;
	}

	private void ellipsoidMethod()
	{
		double[] center = initializeCenter();
		double[][] ellipsMatrix = initializeEllipsMatrix(center);
		double bestValue = 0;
		double[] bestCenter = new double[N_train];
		boolean firstValue = true;
		boolean stop = false;
		int iter = 0;
		double normCoeff = 0;

		while(!stop)
		{
			iter ++;
			double[] subDifferential;
			double alpha = 0;
			int infeasibleConstraint = checkFeasible(center);

			if(infeasibleConstraint == 0)
			{
				subDifferential = new double[N_train];

				for(int idx1=0;idx1<N_train;++idx1)
				{
					double sign = center[idx1] / Math.abs(center[idx1]);
					subDifferential[idx1] = parameters[1] * sign - y_train[idx1];

					for(int idx2=0;idx2<N_train;++idx2)
						subDifferential[idx1] += (center[idx2] * kernel(x_train[idx1],x_train[idx2]) );
				}

				normCoeff = Matrix.innerProduct(subDifferential,Matrix.innerProduct(ellipsMatrix,subDifferential) );
				normCoeff = 1 / Math.sqrt(normCoeff);
				subDifferential = Matrix.scalarMultiplication(normCoeff,subDifferential);

				double currentValue = objective(center);

				if( firstValue || (currentValue < bestValue) )
				{
					bestValue = currentValue;
					bestCenter = new double[N_train];

					for(int idx=0;idx<N_train;++idx)
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

			double scalar1 = (1 + N_train * alpha) / (N_train + 1);
			double scalar2 = Math.pow(N_train,2) / (Math.pow(N_train,2) - 1) * (1 - Math.pow(alpha,2) );
			double scalar3 = ( (2*(1+N_train*alpha) ) / ( (N_train+1) * (1+alpha) ) );

			double[] ellMat_subDiff = Matrix.innerProduct(ellipsMatrix,subDifferential);
			center = Matrix.difference(center,Matrix.scalarMultiplication(scalar1,ellMat_subDiff) );
			ellipsMatrix = Matrix.scalarMultiplication(scalar2,(Matrix.difference(ellipsMatrix,Matrix.scalarMultiplication(scalar3,Matrix.outerProduct(ellMat_subDiff,ellMat_subDiff) ) ) ) );

			if( ( (1/normCoeff) < stoppingError) && !firstValue || iter == maxIters )
				stop = true;
		}

		if(iter == maxIters)
			System.out.println("Warning (ellipsoid method): max iterations reached");

		error = 1/normCoeff;
		noIters = iter;
		lambda = bestCenter;
	}

	private double objective(double[] input)
	{
		double output = 0;

		for(int idx1=0;idx1<N_train;++idx1)
		{
			output += parameters[1] * Math.abs(input[idx1]) - y_train[idx1] * input[idx1] + 0.5 * Math.pow(input[idx1] , 2);

			for(int idx2=0;idx2<idx1;++idx2)
				output += input[idx1] * input[idx2] * kernel(x_train[idx1],x_train[idx2]);
		}

		return output;
	}

	private double[] subDiffConstraint(int no)
	{
		double[] output = new double[N_train];

		if(no == (N_train+1) )
		{
			for(int idx=0;idx<N_train;++idx)
				output[idx] = 1;
		}
		else if(no == (-N_train-1) )
		{
			for(int idx=0;idx<N_train;++idx)
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
		if(no == (N_train+1) )
		{
			double output = -stoppingError;

			for(int idx=0;idx<N_train;++idx)
				output += center[idx];

			return output;
		}
		else if(no == (-N_train-1))
		{
			double output = -stoppingError;

			for(int idx=0;idx<N_train;++idx)
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

		for(int idx=0;idx<N_train;++idx)
		{
			if(c[idx] > parameters[0])
				return (idx+1);

			if(c[idx] < (-parameters[0]) )
				return -(idx+1);

			sum += c[idx];
		}

		if(sum > stoppingError)
			return N_train+1;

		if(sum < (-stoppingError) )
			return -N_train-1;

		return 0;
	}

	private double[] initializeCenter()
	{
		double[] c = new double[N_train];
		double sum = 0;
		double boundary = parameters[0]/(N_train-1);
		Random r = new Random();

		for(int idx=0;idx<(N_train-1);++idx)
		{
			c[idx] = boundary * (2 * r.nextDouble() - 1);
			sum += c[idx];
		}

		c[N_train-1] = -sum;
		return c;
	}

	private double[][] initializeEllipsMatrix(double[] c)
	{
		double[][] P = new double[N_train][N_train];
		double maxRadius = N_train*Math.pow(2*parameters[0],2);

		for(int idx=0;idx<N_train;++idx)
		{
			P[idx][idx] = maxRadius;
		}

		return P;
	}
}
