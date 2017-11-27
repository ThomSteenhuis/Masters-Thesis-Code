package models;

import input.Data;
import math.FFANNErrorFunction;
import math.Matrix;
import math.NelderMead;

public class ANN extends Model {

	private double[] upperWeights;
	private double upperBias;
	private double[][] lowerWeights;
	private double[] lowerBias;

	private double error;
	private double[] residuals;
	private double[] Y_hat;
	private double[][] Z;
	private double[][] WTX;

	public double[] Y;
	public double[][] X;
	public int N;
	public double mean;
	public double max;

	public double alr;
	public int noTrainingEpochs;

	private final double maxInitBounds = 0.01;
	private final int epochMultiplyer = 1000;

	public ANN(Data data,int noPeriods)
	{
		super(data,noPeriods);
		noParameters = 4;
		noConstants = 2;
		name = "ANN";
	}

	public boolean train()
	{
		if(parameters.length != noParameters)
		{
			System.out.println("Error (ANN): invalid no of parameters");
			return false;
		}

		if( ( (int)parameters[0] < 1) || ( (int)parameters[1] < 1) || (parameters[2] > 1) || (parameters[2] < 0) || ( (int)parameters[3] < 1) )
		{
			System.out.println("Error (ANN): parameters have invalid value");
			return false;
		}

		if(constants.length != noConstants)
		{
			System.out.println("Error (ANN): invalid no of constants");
			return false;
		}

		initializeData();
		
		//backpropagation();
		if(!nelderMead()) return false;
		
		initializeSets();
		forecast();

		trainingForecasted = true;
		validationForecasted = true;
		testingForecasted = true;
		return true;
	}

	public double predict(double[] x)
	{
		double[] wTx = Matrix.addition(lowerBias,Matrix.innerProduct(lowerWeights,x) );
		double[] Z = Matrix.tanh(wTx);
		return Matrix.innerProduct(Z,upperWeights) + upperBias;
	}

	public double[][] getX()
	{
		return X;
	}

	public double[] getY()
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

	public double getUpperBias()
	{
		return upperBias;
	}

	public double[][] getLowerWeights()
	{
		return lowerWeights;
	}

	public double[] getUpperWeights()
	{
		return upperWeights;
	}
	
	private void calculateYHat()
	{
		double[][] XT = Matrix.inverse(X);
		Y_hat = new double[N];
		
		for(int idx=0;idx<N;++idx) Y_hat[idx] = predict(XT[idx]);
	}
	
	private void translateOptimalweights(double[] array)
	{
		lowerBias = new double[(int)parameters[1]];
		for(int idx=0;idx<parameters[1];++idx) lowerBias[idx] = array[idx];
		
		lowerWeights = new double[ (int)parameters[1] ][ (int)parameters[0] ];
		for(int idx1=0;idx1<parameters[1];++idx1)
		{
			for(int idx2=0;idx2<parameters[0];++idx2)
				lowerWeights[idx1][idx2] = array[(int)parameters[1] + idx2*(int)parameters[1] + (int)parameters[0]];
		}
		
		upperBias = array[array.length-1-(int)parameters[1]];
		upperWeights = new double[(int)parameters[1]];
		for(int idx=0;idx<parameters[1];++idx) upperWeights[idx] = array[array.length - (int)parameters[1] + idx];
	}
	
	private boolean nelderMead()
	{
		FFANNErrorFunction f = new FFANNErrorFunction((int)parameters[1],X,Y);
		NelderMead nm = new NelderMead(f);
		
		if(!nm.optimize()) return false;

		translateOptimalweights(nm.getOptimalIntput());
		calculateYHat();
		
		return true;
	}
	
	private void backpropagation()
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

	private void forecast()
	{
		int index = data.getIndexFromCat(category);

		for(int idx=0;idx<(X.length+noPersAhead-1);++idx)
			trainingForecast[idx] = trainingReal[idx];

		for(int idx=(X.length+noPersAhead-1);idx<trainingReal.length;++idx)
			trainingForecast[idx] = destandardize(Y_hat[idx-X.length-noPersAhead+1]);

		for(int idx=0;idx<validationReal.length;++idx)
		{
			double[] x = new double[(int)parameters[0]];

			for(int idx2=0;idx2<x.length;++idx2)
				x[idx2] = standardize(data.getVolumes()[data.getValidationFirstIndex()[index]+idx+idx2-x.length-noPersAhead+1][index]);

			validationForecast[idx] = destandardize(predict(x));
		}

		for(int idx=0;idx<testingReal.length;++idx)
		{
			double[] x = new double[(int)parameters[0]];

			for(int idx2=0;idx2<x.length;++idx2)
				x[idx2] = standardize(data.getVolumes()[data.getTestingFirstIndex()[index]+idx+idx2-x.length-noPersAhead+1][index]);

			testingForecast[idx] = destandardize(predict(x));
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
	
	private double standardize(double input)
	{
		return ( (input-mean)/(max-mean) );
	}
	
	private double destandardize(double input)
	{
		return ((max-mean)*input+mean);
	}

	private void initializeData()
	{
		int index = data.getIndexFromCat(category);
		mean = Matrix.mean(data.getTrainingSet(category));
		max = Matrix.max(data.getTrainingSet(category));
		N = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index] - (int) (parameters[0]) - noPersAhead + 1;
		X = new double[(int) (parameters[0])][N];
		Y = new double[N];

		for(int idx=0;idx<N;++idx)
			Y[idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index] + (int) (parameters[0]) + noPersAhead - 1][index]);

		for(int idx1=0;idx1<Y.length;++idx1)
		{
			for(int idx2=0;idx2<X.length;++idx2)
				X[idx2][idx1] = standardize(data.getVolumes()[idx1 + idx2 + data.getTrainingFirstIndex()[index]][index]);
		}
	}

	private void initializeWeights()
	{
		upperWeights = new double[ (int)parameters[1] ];
		lowerWeights = new double[ (int)parameters[1] ][ (int)parameters[0] ];
		lowerBias = new double[(int)parameters[1]];
		upperBias = calculateInitWeight(main.Run.r.nextDouble());

		for(int idx=0;idx<lowerBias.length;++idx)
			lowerBias[idx] = calculateInitWeight(main.Run.r.nextDouble());

		for(int idx1=0;idx1<upperWeights.length;++idx1)
		{
			upperWeights[idx1] = calculateInitWeight(main.Run.r.nextDouble());

			for(int idx2=0;idx2<lowerWeights[idx1].length;++idx2)
				lowerWeights[idx1][idx2] = calculateInitWeight(main.Run.r.nextDouble());
		}
	}

	private double calculateInitWeight(double input)
	{
		return 2*maxInitBounds*(input - 1);
	}
}
