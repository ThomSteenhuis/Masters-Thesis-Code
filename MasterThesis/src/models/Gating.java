package models;

import java.util.ArrayList;

import input.Data;
import math.GatingErrorFunction;
import math.NelderMead;

public class Gating extends Model {
	
	ArrayList<Model> models;
	double[][] X;
	double[] weights;

	public Gating(Data dataset, int[] periods, String[] cat)
	{
		super(dataset,periods,cat);
		name = "Gating";
		noParameters = 0;
		noConstants = 0;
		noOutputs = 1;
		models = new ArrayList<Model>();
	}
	
	public void addModel(Model m)
	{
		models.add(m);
	}
	
	public void setX(double[][] x)
	{
		X = x;
	}
	
	public boolean train() 
	{
		if(models.size() <= 1) {System.out.println("Error (gating): number of models should be at least 2"); return false;}
		for(int idx=0;idx<models.size();++idx) {if(!models.get(idx).train()) {System.out.printf("Error (gating): training of model %d failed\n",idx); return false;}}
		
		double[] Y = models.get(0).getValidationReal()[0];
		double[][] Y_hat = new double[Y.length][models.size()];
		
		for(int idx1=0;idx1<Y_hat.length;++idx1)
		{
			for(int idx2=0;idx2<Y_hat[idx1].length;++idx2) Y_hat[idx1][idx2] = models.get(idx2).getValidationForecast()[0][idx1];
		}
		
		GatingErrorFunction gef = new GatingErrorFunction(X,Y,Y_hat);
		NelderMead nm = new NelderMead(gef,454403204,100000);
		if(!nm.optimize()) return false;
		
		weights = new double[gef.getNoInputs()];
		for(int idx=0;idx<weights.length;++idx) weights[idx] = nm.getOptimalIntput()[idx];
		
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
	
	private void forecast()
	{
		for(int idx1=0;idx1<X.length;++idx1)
		{
			double sum = 0;
			for(int idx2=0;idx2<models.size();++idx2)
			{
				sum += weights[idx2*(X[idx1].length+1)];
				for(int idx3=0;idx3<X[idx1].length;++idx3) sum += weights[idx2*(X[idx1].length+1)+1+idx3]*X[idx1][idx3];
			}
			
			for(int idx2=0;idx2<models.size();++idx2)
			{
				double w = weights[idx2*(X[idx1].length+1)];
				for(int idx3=0;idx3<X[idx1].length;++idx3) w += weights[idx2*(X[idx1].length+1)+1+idx3]*X[idx1][idx3];
				
				validationForecast[0][idx1] += (w/sum)*models.get(idx2).getValidationForecast()[0][idx1];
			}
		}
		forecasted[0] = true;
	}
}
