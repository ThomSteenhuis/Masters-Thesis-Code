package models;

import input.Data;
import math.Matrix;

public class ANN extends Model {

	private double[] upperWeights;
	private double upperBias;
	private double[][] lowerWeights;
	private double[] lowerBias;
	private int noTrainingEpochs;

	private double[] residuals;
	private double[] Y_hat;
	private double[][] Z;
	private double[][] WTX;

	private double[] Y;
	private double[][] X;
	private int N;

	private final double maxInitBounds = 0.01;
	private final int epochMultiplyer = 100;

	public ANN(Data data,int noPeriods)
	{
		super(data,noPeriods);
		noParameters = 4;
		noConstants = 0;
		name = "ANN";
		noTrainingEpochs = epochMultiplyer*(int)parameters[3];
	}

	public void train()
	{
		if(parameters.length != noParameters)
		{
			System.out.println("Error (ANN): invalid no of parameters");
			return;
		}

		if( ( (int)parameters[0] < 1) || ( (int)parameters[1] < 1) || (parameters[2] > 1) || (parameters[2] < 0) || ( (int)parameters[3] < 1) )
		{
			System.out.println("Error (ANN): parameters have invalid value");
			return;
		}

		initializeWeights();
		initializeData();

		for(int idx=0;idx<noTrainingEpochs;++idx)
		{
			predict();
			updateLowerBias();
			updateLowerWeights();
			updateUpperBias();
			updateUpperWeights();
		}
	}

	public void validate() {}

	public void test() {}

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

	private void updateLowerBias()
	{
		for(int idx=0;idx<lowerBias.length;++idx)
			lowerBias[idx] = lowerBias[idx] + parameters[2] * upperWeights[idx] * Matrix.sum( Matrix.elementwiseMultiplication( Matrix.scalarSubtraction( 1 , Matrix.elementwiseMultiplication( Matrix.tanh(WTX[idx]) , Matrix.tanh(WTX[idx]) ) ) , residuals) );
	}

	private void updateLowerWeights()
	{
		for(int idx1=0;idx1<lowerWeights.length;++idx1)
		{
			for(int idx2=0;idx2<lowerWeights[idx1].length;++idx2)
				lowerWeights[idx1][idx2] = lowerWeights[idx1][idx2] + parameters[2] * upperWeights[idx1] * Matrix.sum( Matrix.elementwiseMultiplication( Matrix.scalarSubtraction( 1 , Matrix.elementwiseMultiplication( Matrix.tanh(WTX[idx1]) , Matrix.tanh(WTX[idx1]) ) ), X[idx2] , residuals) );
		}
	}

	private void updateUpperBias()
	{
		upperBias = upperBias + parameters[2] * Matrix.sum(residuals);
	}

	private void updateUpperWeights()
	{
		for(int idx=0;idx<upperWeights.length;++idx)
			upperWeights[idx] = upperWeights[idx] + parameters[2] * Matrix.sum(Matrix.elementwiseMultiplication(residuals,Z[idx]) );
	}

	private void predict()
	{
		Z = new double[(int)parameters[1]][N];
		WTX = new double[(int)parameters[1]][N];

		for(int idx1=0;idx1<N;++idx1)
		{
			Y_hat[idx1] = upperBias;

			for(int idx2=0;idx2<(int)parameters[1];++idx2)
			{
				for(int idx3=0;idx3<(int)parameters[0];++idx3)
				{
					WTX[idx2][idx1] += lowerWeights[idx2][idx3] * X[idx3][idx1] + lowerBias[idx2];
				}
				Z[idx2][idx1] = Math.tanh(WTX[idx2][idx1]);
				Y_hat[idx1] += upperWeights[idx2] * Z[idx2][idx1];
			}
		}

		residuals = Matrix.difference(Y,Y_hat);
	}

	private void initializeData()
	{
		int index = data.getIndexFromCat(category);
		N = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index] - (int) (parameters[0]) - noPersAhead;
		X = new double[(int) (parameters[0])][N];
		Y = new double[N];

		for(int idx1=0;idx1<N;++idx1)
		{
			Y[idx1] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead - 1][index];

			for(int idx2=0;idx2<X[idx1].length;++idx2)
				X[idx2][idx1] =  data.getVolumes()[idx1 + idx2][index];
		}
	}

	private void initializeWeights()
	{
		upperWeights = new double[ (int)parameters[1] ];
		lowerWeights = new double[ (int)parameters[1] ][ (int)parameters[0] ];
		lowerBias = new double[(int)parameters[0]];
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
