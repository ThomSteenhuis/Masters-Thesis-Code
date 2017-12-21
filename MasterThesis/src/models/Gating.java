package models;

import java.util.ArrayList;

import input.Data;
import math.LUfactorization;
import math.Matrix;

public class Gating extends Model {
	
	private ArrayList<Model> models;
	private int seed;
	
	private double[][] X;
	private double[] Y;
	private double[][] Y_hat;
	private double[] weights;

	public Gating(Data dataset, int[] periods, String[] cat,int s)
	{
		super(dataset,periods,cat);
		name = "Gating";
		seed = s;
		noParameters = 0;
		noConstants = 3;
		noOutputs = 1;
		models = new ArrayList<Model>();
	}
	
	public boolean train() 
	{
		addModels();
		
		if(models.size() <= 1) {System.out.println("Error (gating): number of models should be at least 2"); return false;}
		
		for(int idx=0;idx<models.size();++idx) {if(!models.get(idx).train()) {System.out.printf("Error (gating): training of model %d failed\n",idx); return false;}}
		
		initializeData();
		calculateWeights();		
		initializeSets();
		forecast();
		
		return true;
	}

	public ArrayList<Model> getModels()
	{
		return models;
	}
	
	public double[] getWeights()
	{
		return weights;
	}
	
	private void addModels()
	{
		SVR m1 = new SVR(data,noPersAhead,category,(seed*57)%330235458);
		double[] pars1 = {20,0.001,100};
		double[] consts1 = new double[3];
		consts1[1] = 0;
		consts1[2] = constants[0];
		m1.setParameters(pars1);
		m1.setConstants(consts1);
		
		ANN m2 = new ANN(data,noPersAhead,category,(seed*63)%254594983,100000);
		double[] pars2 = {20};
		double[] consts2 = new double[2];
		consts2[0] = 0;
		consts2[1] = constants[0];
		m2.setParameters(pars2);
		m2.setConstants(consts2);
		
		models.add(m1);
		models.add(m2);
	}
	
	private void calculateWeights()
	{
		int N = (X[0].length + 1) * models.size();
		double[][] A = new double[N][N];
		double[] b = new double[N];
		
		for(int idx1=0;idx1<N;++idx1)
		{
			int i1 = idx1 % models.size();
			int j1 = idx1 / models.size();
			
			for(int idx3=0;idx3<X.length;++idx3) 
			{
				if(j1 == 0) b[idx1] += Y[idx3]*Y_hat[idx3][i1];
				else b[idx1] +=  Y[idx3]*Y_hat[idx3][i1]*X[idx3][j1-1];
			}
			
			for(int idx2=0;idx2<N;++idx2)
			{
				int i2 = idx2 % models.size();
				int j2 = idx2 / models.size();
				
				for(int idx3=0;idx3<X.length;++idx3) 
				{
					if(j1 == 0 && j2 == 0) A[idx1][idx2] += Y_hat[idx3][i1]*Y_hat[idx3][i2];
					else if(j1 == 0) A[idx1][idx2] += Y_hat[idx3][i1]*Y_hat[idx3][i2]*X[idx3][j2-1];
					else if(j2 == 0) A[idx1][idx2] += Y_hat[idx3][i1]*Y_hat[idx3][i2]*X[idx3][j1-1];
					else A[idx1][idx2] += Y_hat[idx3][i1]*Y_hat[idx3][i2]*X[idx3][j1-1]*X[idx3][j2-1];
				}
			}
		}
		
		LUfactorization lu = new LUfactorization(A,b);
		lu.solve();
		weights = lu.getSolution();
	}
	
	private void initializeData()
	{
		X = new double[models.get(0).getTrainingReal()[0].length-(int)constants[0]-noPersAhead[0]+1][(int) constants[0]];
		Y = new double[X.length];
		Y_hat = new double[X.length][models.size()];
		
		for(int idx1=0;idx1<X.length;++idx1) 
		{
			int cat =  models.get(0).getData().getIndexFromCat(category[0]);
			
			for(int idx2=0;idx2<X[idx1].length;++idx2) 
			{
				X[idx1][+idx2] = models.get(0).getData().getVolumes()[idx1+idx2][cat];
			}
			
			Y[idx1] = models.get(0).getTrainingReal()[0][(int)constants[0]+noPersAhead[0]-1+idx1];
			
			for(int idx2=0;idx2<Y_hat[idx1].length;++idx2) 
				Y_hat[idx1][idx2] = models.get(idx2).getTrainingForecast()[0][(int)constants[0]+noPersAhead[0]-1+idx1];
		}
	}
	
	private void forecast()
	{
		int start = (int)constants[0]+noPersAhead[0]-1;
		for(int idx1=0;idx1<start;++idx1) trainingForecast[0][idx1] = models.get(0).getTrainingReal()[0][idx1];
		
		for(int idx1=0;idx1<X.length;++idx1)
		{
			double sum = 0;
			for(int idx2=0;idx2<models.size();++idx2)
			{
				sum += weights[idx2];
				for(int idx3=0;idx3<X[idx1].length;++idx3) sum += weights[(idx3+1)*models.size()+idx2]*X[idx1][idx3];
			}
			
			for(int idx2=0;idx2<models.size();++idx2)
			{
				double w = weights[idx2];
				for(int idx3=0;idx3<X[idx1].length;++idx3) w += weights[(idx3+1)*models.size()+idx2]*X[idx1][idx3];
				
				trainingForecast[0][start+idx1] += (w/sum)*models.get(idx2).getTrainingForecast()[0][start+idx1];
			}
		}
		
		int valLength = models.get(0).getValidationForecast()[0].length;
		int testLength = models.get(0).getTestingForecast()[0].length;
		int cat = models.get(0).getData().getIndexFromCat(category[0]);
		
		for(int idx1=0;idx1<valLength;++idx1)
		{
			double[] input = new double[(int)constants[0]];
			int firstValIdx = models.get(0).getData().getValidationFirstIndex()[0];
			
			for(int idx2=0;idx2<input.length;++idx2) 
				input[idx2] = models.get(0).getData().getVolumes()[firstValIdx-(int)constants[0]-noPersAhead[0]+1+idx2][cat];
			
			double sum = 0;
			for(int idx2=0;idx2<models.size();++idx2)
			{
				sum += weights[idx2];
				for(int idx3=0;idx3<input.length;++idx3) sum += weights[(idx3+1)*models.size()+idx2]*input[idx3];
			}
			
			for(int idx2=0;idx2<models.size();++idx2)
			{
				double w = weights[idx2];
				for(int idx3=0;idx3<input.length;++idx3) w += weights[(idx3+1)*models.size()+idx2]*input[idx3];
				
				validationForecast[0][idx1] += (w/sum)*models.get(idx2).getValidationForecast()[0][idx1];
			}
		}
		
		for(int idx1=0;idx1<testLength;++idx1)
		{
			double[] input = new double[(int)constants[0]];
			int firstTestIdx = models.get(0).getData().getTestingFirstIndex()[0];
			
			for(int idx2=0;idx2<input.length;++idx2) 
				input[idx2] = models.get(0).getData().getVolumes()[firstTestIdx-(int)constants[0]-noPersAhead[0]+1+idx2][cat];
			
			double sum = 0;
			for(int idx2=0;idx2<models.size();++idx2)
			{
				sum += weights[idx2];
				for(int idx3=0;idx3<input.length;++idx3) sum += weights[(idx3+1)*models.size()+idx2]*input[idx3];
			}
			
			for(int idx2=0;idx2<models.size();++idx2)
			{
				double w = weights[idx2];
				for(int idx3=0;idx3<input.length;++idx3) w += weights[(idx3+1)*models.size()+idx2]*input[idx3];
				
				testingForecast[0][idx1] += (w/sum)*models.get(idx2).getTestingForecast()[0][idx1];
			}
		}
		
		forecasted[0] = true;
	}
}
