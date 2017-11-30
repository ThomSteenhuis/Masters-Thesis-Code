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

	public double[][] Y;
	public double[][] X;
	public int N;
	public double[] mean;
	public double[] max;

	public double alr;
	public int noTrainingEpochs;

	private final double maxInitBounds = 0.01;
	private final int epochMultiplyer = 1000;

	public ANN(Data data,int noPeriods,Random R)
	{
		super(data,noPeriods);
		noParameters = 2;
		noConstants = 0;
		name = "ANN";
		r = R;
	}

	public boolean train()
	{
		if(parameters.length != noParameters)
		{
			System.out.println("Error (ANN): invalid no of parameters");
			return false;
		}

		/*if( ( (int)parameters[0] < 1) || ( (int)parameters[1] < 1) || (parameters[2] > 1) || (parameters[2] < 0) || ( (int)parameters[3] < 1) )
		{
			System.out.println("Error (ANN): parameters have invalid value");
			return false;
		}*/
		
		if( ( (int)parameters[0] < 1) || ( (int)parameters[1] < 1) )
		{
			System.out.println("Error (ANN): parameters have invalid value");
			return false;
		}
		category = "complete";
		initializeData();
		
		//backpropagation();
		if(!nelderMead()) return false;
		
		category = "2200EVO";
		initializeSets();
		forecast();

		trainingForecasted = true;
		validationForecasted = true;
		testingForecasted = true;
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
		lowerBias = new double[(int)parameters[1]];
		for(int idx=0;idx<parameters[1];++idx) lowerBias[idx] = array[idx];
		
		lowerWeights = new double[ (int)parameters[1] ][ (int)parameters[0] * 2 ];

		for(int idx1=0;idx1<parameters[1];++idx1)
		{
			for(int idx2=0;idx2<(2*parameters[0]);++idx2)
				lowerWeights[idx1][idx2] = array[(int)parameters[1] + idx2*(int)parameters[1] + idx1];
		}
		
		upperBias = new double[Y.length];
		upperWeights = new double[(int)parameters[1]][Y.length];
		for(int idx1=0;idx1<Y.length;++idx1)
		{
			upperBias[idx1] = array[array.length-((int)parameters[1]+1)*Y.length];
			for(int idx2=0;idx2<(int)parameters[1];++idx2) 
				upperWeights[idx2][idx1] = array[array.length - ((int)parameters[1]-idx2)*Y.length + idx1];
		}
	}
	
	private boolean nelderMead()
	{
		FFANNErrorFunction f = new FFANNErrorFunction((int)parameters[1],X,Y);
		NelderMead nm = new NelderMead(f,r);
		
		if(!nm.optimize()) return false;

		translateOptimalweights(nm.getOptimalIntput());
		calculateYHat();
		
		return true;
	}
	
	private void forecast()
	{
		int index = data.getIndexFromCat("2200EVO");

		for(int idx=0;idx<(X.length+noPersAhead-1);++idx)
			trainingForecast[idx] = trainingReal[idx];

		for(int idx=(X.length+noPersAhead-1);idx<trainingReal.length;++idx)
			trainingForecast[idx] = destandardize(Y_hat[0][idx-X.length-noPersAhead+1],0);

		for(int idx=0;idx<validationReal.length;++idx)
		{
			double[] x = new double[(int)parameters[0]*2];

			for(int idx2=0;idx2<parameters[0];++idx2)
			{
				x[idx2] = standardize(data.getVolumes()[data.getValidationFirstIndex()[index]+idx+idx2-x.length-noPersAhead+1][index],0);
				x[idx2+(int)parameters[0]] = standardize(data.getVolumes()[data.getValidationFirstIndex()[index]+idx+idx2-x.length-noPersAhead+1][1],1);
			}
			
			validationForecast[idx] = destandardize(predict(x)[0],0);
		}

		for(int idx=0;idx<testingReal.length;++idx)
		{
			double[] x = new double[(int)parameters[0]*2];

			for(int idx2=0;idx2<parameters[0];++idx2)
			{
				x[idx2] = standardize(data.getVolumes()[data.getTestingFirstIndex()[index]+idx+idx2-x.length-noPersAhead+1][index],0);
				x[idx2+(int)parameters[0]] = standardize(data.getVolumes()[data.getTestingFirstIndex()[index]+idx+idx2-x.length-noPersAhead+1][1],1);
			}
			
			testingForecast[idx] = destandardize(predict(x)[0],0);
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
	}*/
	
	private double standardize(double input,int index)
	{
		return ( (input-mean[index])/(max[index]-mean[index]) );
	}
	
	private double destandardize(double input,int index)
	{
		return ((max[index]-mean[index])*input+mean[index]);
	}

	private void initializeData()
	{
		if(category.equals("complete"))
		{
			int[] index = new int[2]; index[0] = data.getIndexFromCat("2200EVO"); index[1] = data.getIndexFromCat("8800FCQ, RFID");
			mean = new double[2]; max = new double[2];
			mean[0] = Matrix.mean(data.getTrainingSet("2200EVO")); mean[1] = Matrix.mean(data.getTestingSet("8800FCQ, RFID"));
			max[0] = Matrix.max(data.getTrainingSet("2200EVO")); max[1] = Matrix.max(data.getTrainingSet("8800FCQ, RFID"));
			N = data.getValidationFirstIndex()[index[0]] - data.getTrainingFirstIndex()[index[0]] - (int) (parameters[0]) - noPersAhead + 1;
			X = new double[(int) parameters[0] * 2][N];
			Y = new double[6][N];

			for(int idx=0;idx<N;++idx)
			{
				Y[0][idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index[0]] + (int) (parameters[0]) + 1 - 1][index[0]],0);
				Y[1][idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index[0]] + (int) (parameters[0]) + 3 - 1][index[0]],0);
				Y[2][idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index[0]] + (int) (parameters[0]) + 6 - 1][index[0]],0);
				Y[3][idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index[1]] + (int) (parameters[0]) + 1 - 1][index[1]],1);
				Y[4][idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index[1]] + (int) (parameters[0]) + 3 - 1][index[1]],1);
				Y[5][idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index[1]] + (int) (parameters[0]) + 6 - 1][index[1]],1);
			}

			for(int idx1=0;idx1<Y.length;++idx1)
			{
				for(int idx2=0;idx2<(int) parameters[0];++idx2)
				{
					X[idx2][idx1] = standardize(data.getVolumes()[idx1 + idx2 + data.getTrainingFirstIndex()[index[0]]][index[0]],0);
					X[(int) parameters[0] + idx2][idx1] = standardize(data.getVolumes()[idx1 + idx2 + data.getTrainingFirstIndex()[index[1]]][index[1]],1);
				}
			}
		}
		else
		{
			int index = data.getIndexFromCat(category);
			mean = new double[1]; max = new double[1];
			mean[0] = Matrix.mean(data.getTrainingSet(category));
			max[0] = Matrix.max(data.getTrainingSet(category));
			N = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index] - (int) (parameters[0]) - noPersAhead + 1;
			X = new double[(int) parameters[0] ][N];
			Y = new double[1][N];

			for(int idx=0;idx<N;++idx)
				Y[0][idx] = standardize(data.getVolumes()[idx + data.getTrainingFirstIndex()[index] + (int) (parameters[0]) + noPersAhead - 1][index],0);

			for(int idx1=0;idx1<Y.length;++idx1)
			{
				for(int idx2=0;idx2<X.length;++idx2)
					X[idx2][idx1] = standardize(data.getVolumes()[idx1 + idx2 + data.getTrainingFirstIndex()[index]][index],0);
			}
		}			
	}

	private void initializeWeights()
	{
		if(category.equals("complete"))
		{
			upperWeights = new double[ (int)parameters[1] ][6];
			lowerWeights = new double[ (int)parameters[1] ][ (int)parameters[0] * 2 ];
			lowerBias = new double[(int)parameters[1]];
			upperBias = new double[6]; 
			
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
		else
		{
			upperWeights = new double[ (int)parameters[1] ][1];
			lowerWeights = new double[ (int)parameters[1] ][ (int)parameters[0] ];
			lowerBias = new double[(int)parameters[1]];
			upperBias = new double[1]; upperBias[0] = calculateInitWeight(main.Run.r.nextDouble());

			for(int idx=0;idx<lowerBias.length;++idx)
				lowerBias[idx] = calculateInitWeight(main.Run.r.nextDouble());

			for(int idx1=0;idx1<upperWeights.length;++idx1)
			{
				upperWeights[idx1][0] = calculateInitWeight(main.Run.r.nextDouble());

				for(int idx2=0;idx2<lowerWeights[idx1].length;++idx2)
					lowerWeights[idx1][idx2] = calculateInitWeight(main.Run.r.nextDouble());
			}
		}
	}

	private double calculateInitWeight(double input)
	{
		return 2*maxInitBounds*(input - 1);
	}
}
