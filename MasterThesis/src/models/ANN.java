package models;

import java.util.Random;

import input.Data;
import math.FFANNErrorFunction;
import math.Matrix;
import math.NelderMead;

public class ANN extends Model {

	private double[][] upperWeights;
	private double[] upperBias;
	private double[][] lowerWeights;
	private double[] lowerBias;

	private double error;
	private double[][] residuals;
	private double[][] Y_hat;
	private double[][] Z;
	private double[][] WTX;

	private double[][] Y;
	private double[][] X;
	private int N;
	private double[] mean;
	private double[] max;

	public double alr;
	public int noTrainingEpochs;

	private int seed;
	private int maxNoIterations;

	private final double maxInitBounds = 0.01;
	private final int epochMultiplyer = 1000;

	public ANN(Data data,int[] noPeriods,String[] cats,int s,int maxIters)
	{
		super(data,noPeriods,cats);
		noParameters = 1;
		noConstants = 2;
		name = "ANN";
		seed = s;
		r = new Random(s);
		maxNoIterations = maxIters;
	}

	public boolean train(boolean bootstrap)
	{
		if(parameters.length != noParameters)
		{
			System.out.println("Error (ANN): invalid no of parameters");
			return false;
		}

		if( ( (int)parameters[0] < 1) || ( (int)constants[1] < 1) && ( (int)constants[0] != 0) && ( (int)constants[0] != 1) )
		{
			System.out.println("Error (ANN): parameters have invalid value");
			return false;
		}

		setNoInputs( ( (int) constants[1] + (int) constants[0])*category.length);
		initializeData();
		
		if(bootstrap) bootstrap();
		//backpropagation();
		if(!nelderMead()) return false;

		initializeSets();
		forecast();

		for(int idx=0;idx<noOutputs;++idx) forecasted[idx] = true;
		return true;
	}

	public double[] predict(double[] x)
	{
		double[] wTx = Matrix.addition(lowerBias,Matrix.innerProduct(lowerWeights,x) );
		double[] Z = Matrix.tanh(wTx);
		return Matrix.addition(Matrix.innerProduct(Z,upperWeights),upperBias);
	}

	public double[][] getX()
	{
		return X;
	}

	public double[][] getY()
	{
		return Y;
	}

	public double[][] getZ()
	{
		return Z;
	}

	public double[][] getWTX()
	{
		return WTX;
	}

	public double[] getLowerBias()
	{
		return lowerBias;
	}

	public double[] getUpperBias()
	{
		return upperBias;
	}

	public double[][] getLowerWeights()
	{
		return lowerWeights;
	}

	public double[][] getUpperWeights()
	{
		return upperWeights;
	}

	private void calculateYHat()
	{
		double[][] XT = Matrix.inverse(X);
		double[][] Y_hatT = new double[N][Y.length];

		for(int idx1=0;idx1<N;++idx1) Y_hatT[idx1] = predict(XT[idx1]);
		Y_hat = Matrix.inverse(Y_hatT);
	}

	private void translateOptimalweights(double[] array)
	{
		lowerBias = new double[(int)parameters[0]];
		for(int idx=0;idx<lowerBias.length;++idx) lowerBias[idx] = array[idx];

		lowerWeights = new double[ (int)parameters[0] ][X.length];

		for(int idx1=0;idx1<lowerWeights.length;++idx1)
		{
			for(int idx2=0;idx2<lowerWeights[idx1].length;++idx2)
				lowerWeights[idx1][idx2] = array[(int)parameters[0] + idx2*(int)parameters[0] + idx1];
		}

		upperBias = new double[Y.length];
		upperWeights = new double[(int)parameters[0]][Y.length];
		for(int idx1=0;idx1<Y.length;++idx1)
		{
			upperBias[idx1] = array[array.length-((int)parameters[0]+1)*Y.length];
			for(int idx2=0;idx2<(int)parameters[0];++idx2)
				upperWeights[idx2][idx1] = array[array.length - ((int)parameters[0]-idx2)*Y.length + idx1];
		}
	}

	private boolean nelderMead()
	{
		FFANNErrorFunction f = new FFANNErrorFunction((int)parameters[0],X,Y);
		NelderMead nm = new NelderMead(f,seed,maxNoIterations);

		nm.optimize();

		translateOptimalweights(nm.getOptimalIntput());
		calculateYHat();

		return true;
	}

	private void forecast()
	{
		for(int idx1=0;idx1<noOutputs;++idx1)
		{
			int cat1 = idx1 / noPersAhead.length;
			int per = noPersAhead[idx1 % noPersAhead.length];
			int limit = Math.max((int)(constants[1]),(int)(constants[0])*12)+Matrix.max(noPersAhead)-1;

			for(int idx2=0;idx2<limit;++idx2)
				trainingForecast[idx1][idx2] = trainingReal[idx1][idx2];

			for(int idx2=limit;idx2<trainingReal[idx1].length;++idx2)
				trainingForecast[idx1][idx2] = destandardize(Y_hat[idx1][idx2-limit],cat1);

			for(int idx2=0;idx2<validationReal[idx1].length;++idx2)
			{
				double[] x = new double[noInputs];

				for(int idx3=0;idx3<noInputs;++idx3)
				{
					int cat2 = idx3 / ((int)(constants[1]) + (int)(constants[0]));
					int lag = idx3 % ((int)(constants[1]) + (int)(constants[0]));

					if( ( (int)constants[0] == 1) && (lag == ((int)constants[1] + (int)constants[0] - 1) ) )
						x[idx3] = standardize(data.getVolumes()[data.getValidationFirstIndex()[cat2] + idx2 - per - 11][cat2],cat2);
					else
						x[idx3] = standardize(data.getVolumes()[data.getValidationFirstIndex()[cat2] + idx2 + lag - (int)(constants[1]) - per + 1][cat2],cat2);
				}

				validationForecast[idx1][idx2] = destandardize(predict(x)[idx1],cat1);
			}

			for(int idx2=0;idx2<testingReal[idx1].length;++idx2)
			{
				double[] x = new double[noInputs];

				for(int idx3=0;idx3<noInputs;++idx3)
				{
					int cat2 = idx3 / ((int)constants[1] + (int)constants[0]);
					int lag = idx3 % ((int)constants[1] + (int)constants[0]);

					if( ( (int)constants[0] == 1) && (lag == ((int)constants[1] + (int)constants[0] - 1) ) )
						x[idx3] = standardize(data.getVolumes()[data.getTestingFirstIndex()[cat2] + idx2 - per - 11][cat2],cat2);
					else
						x[idx3] = standardize(data.getVolumes()[data.getTestingFirstIndex()[cat2] + idx2 + lag - (int)(constants[1]) - per + 1][cat2],cat2);
					}

				testingForecast[idx1][idx2] = destandardize(predict(x)[idx1],cat1);
			}
		}
	}

	/*private void backpropagation()
	{
		noTrainingEpochs = epochMultiplyer*(int)parameters[3];
		alr = parameters[2];

		initializeWeights();

		for(int idx=0;idx<noTrainingEpochs;++idx)
		{
			evaluate();
			updateLowerBias(upperWeights);
			updateLowerWeights(upperWeights);
			updateUpperBias();
			updateUpperWeights();
		}
	}

	private void updateLowerBias(double[] oldUpperWeights)
	{
		for(int idx=0;idx<lowerBias.length;++idx)
			lowerBias[idx] = lowerBias[idx] + alr * oldUpperWeights[idx] * Matrix.sum( Matrix.elementwiseMultiplication( Matrix.scalarSubtraction( 1 , Matrix.elementwiseMultiplication( Matrix.tanh(WTX[idx]) , Matrix.tanh(WTX[idx]) ) ) , residuals) );
	}

	private void updateLowerWeights(double[] oldUpperWeights)
	{
		for(int idx1=0;idx1<lowerWeights.length;++idx1)
		{
			for(int idx2=0;idx2<lowerWeights[idx1].length;++idx2)
				lowerWeights[idx1][idx2] = lowerWeights[idx1][idx2] + alr * oldUpperWeights[idx1] * Matrix.sum( Matrix.elementwiseMultiplication( Matrix.scalarSubtraction( 1 , Matrix.elementwiseMultiplication( Matrix.tanh(WTX[idx1]) , Matrix.tanh(WTX[idx1]) ) ), X[idx2] , residuals) );
		}
	}

	private void updateUpperBias()
	{
		upperBias = upperBias + alr * Matrix.sum(residuals);
	}

	private void updateUpperWeights()
	{
		for(int idx=0;idx<upperWeights.length;++idx)
			upperWeights[idx] = upperWeights[idx] + alr * Matrix.sum(Matrix.elementwiseMultiplication(residuals,Z[idx]) );
	}

	private void evaluate()
	{
		Z = new double[(int)parameters[1]][N];
		WTX = new double[(int)parameters[1]][N];
		Y_hat = new double[N];

		for(int idx1=0;idx1<N;++idx1)
		{
			Y_hat[idx1] = upperBias;

			for(int idx2=0;idx2<(int)parameters[1];++idx2)
			{
				WTX[idx2][idx1] = lowerBias[idx2];

				for(int idx3=0;idx3<(int)parameters[0];++idx3)
					WTX[idx2][idx1] += lowerWeights[idx2][idx3] * X[idx3][idx1];

				Z[idx2][idx1] = Math.tanh(WTX[idx2][idx1]);
				Y_hat[idx1] += upperWeights[idx2] * Z[idx2][idx1];
			}
		}

		residuals = Matrix.difference(Y,Y_hat);
		double newError = 0.5*Math.pow(Matrix.twoNorm(residuals),2);

		if(newError < error)
			alr += constants[0];
		else
			alr = constants[1]*alr;

		error = newError;
	}

	private void initializeWeights()
	{
		upperWeights = new double[ (int)parameters[1] ][noOutputs];
		lowerWeights = new double[ (int)parameters[1] ][noInputs];
		lowerBias = new double[(int)parameters[1]];
		upperBias = new double[noOutputs];

		for(int idx=0;idx<upperBias.length;++idx)
			upperBias[idx] = calculateInitWeight(main.Run.r.nextDouble());

		for(int idx=0;idx<lowerBias.length;++idx)
			lowerBias[idx] = calculateInitWeight(main.Run.r.nextDouble());

		for(int idx1=0;idx1<upperWeights.length;++idx1)
		{
			for(int idx2=0;idx2<upperWeights[idx1].length;++idx2)
				upperWeights[idx1][idx2] = calculateInitWeight(main.Run.r.nextDouble());

			for(int idx2=0;idx2<lowerWeights[idx1].length;++idx2)
				lowerWeights[idx1][idx2] = calculateInitWeight(main.Run.r.nextDouble());
		}
	}

	private double calculateInitWeight(double input)
	{
		return 2*maxInitBounds*(input - 1);
	}*/

	private double standardize(double input,int index)
	{
		return ( (input-mean[index])/(max[index]-mean[index]) );
	}

	private double destandardize(double input,int index)
	{
		return ((max[index]-mean[index])*input+mean[index]);
	}
	
	private void bootstrap()
	{
		int[] idcs = new int[N];
		for(int idx=0;idx<idcs.length;++idx) idcs[idx] = r.nextInt(N);
		
		double[][] x_bootstrap = new double[X.length][idcs.length];
		double[][] y_bootstrap = new double[Y.length][idcs.length];
		
		for(int idx1=0;idx1<idcs.length;++idx1)
		{
			for(int idx2=0;idx2<Y.length;++idx2) y_bootstrap[idx2][idx1] = Y[idx2][idcs[idx1]];
			for(int idx2=0;idx2<X.length;++idx2) x_bootstrap[idx2][idx1] = X[idx2][idcs[idx1]];
		}
		
		for(int idx1=0;idx1<X[0].length;++idx1)
		{
			for(int idx2=0;idx2<Y.length;++idx2) Y[idx2][idx1] = y_bootstrap[idx2][idx1];
			for(int idx2=0;idx2<X.length;++idx2) X[idx2][idx1] = x_bootstrap[idx2][idx1];
		}
	}

	private void initializeData()
	{
		int[] index = new int[category.length];

		for(int idx=0;idx<index.length;++idx) index[idx] = data.getIndexFromCat(category[idx]);

		mean = new double[index.length]; max = new double[index.length];
		for(int idx=0;idx<index.length;++idx) {mean[idx] = Matrix.mean(data.getTrainingSet(category[idx])); max[idx] = Matrix.max(data.getTrainingSet(category[idx]));}

		N = data.getValidationFirstIndex()[index[0]] - data.getTrainingFirstIndex()[index[0]] - Math.max((int) (constants[1]),(int) (constants[0])*12) - Matrix.max(noPersAhead) + 1;
		X = new double[noInputs][N];
		Y = new double[noOutputs][N];

		for(int idx1=0;idx1<N;++idx1)
		{
			for(int idx2=0;idx2<Y.length;++idx2)
			{
				int out = idx2 / noPersAhead.length;
				int cat = data.getIndexFromCat(category[out]);
				int noPers = noPersAhead[idx2 % noPersAhead.length];
				Y[idx2][idx1] = standardize(data.getVolumes()[idx1 + data.getTrainingFirstIndex()[cat] + Math.max((int) (constants[1]),(int) (constants[0])*12) + noPers - 1][cat],out);
			}

			for(int idx2=0;idx2<X.length;++idx2)
			{
				int out = idx2 / ((int)constants[1]+(int)constants[0]);
				int cat = data.getIndexFromCat(category[out]);

				if( ( (int)constants[0] == 1) && (idx2 % ((int)constants[1]+(int)constants[0]) == ((int)constants[1]+(int)constants[0]-1) ) )
					X[idx2][idx1] = standardize(data.getVolumes()[idx1 + data.getTrainingFirstIndex()[cat]][cat],out);
				else
					X[idx2][idx1] = standardize(data.getVolumes()[idx1 + idx2%((int)constants[1]+(int)constants[0]) + (int)(constants[0])*(12 - (int)(constants[1])) + data.getTrainingFirstIndex()[cat]][cat],out);
			}
		}
	}
}
