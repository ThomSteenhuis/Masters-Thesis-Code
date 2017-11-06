package models;

import input.Data;

public class ExponentialSmoothing extends Model{
	
	private boolean additive;
	private boolean damped;
	private int modelNo;
	
	public ExponentialSmoothing(String model,boolean add,boolean damp,double[] pars,int periods,Data dataset)
	{
		super(dataset,pars,periods);
		name = model;
		
		try{
			switch(model)
			{
				case "SES":
				{
					if(pars.length != 1)
					{
						throw new IllegalArgumentException();
					}
					else
					{
						modelNo = 0;
						parameters = pars;
					}
					break;
				}
				case "DES":
				{
					if( ( (pars.length != 3) && damp ) || ( (pars.length != 2) && !damp) )
					{
						throw new IllegalArgumentException();
					}
					else
					{
						modelNo = 1;
						parameters = pars;
					}
					break;
				}
				case "TES":
				{
					if( ( (pars.length != 4) && damp ) || ( (pars.length != 3) && !damp) )
					{
						throw new IllegalArgumentException();
					}
					else
					{
						modelNo = 2;
						parameters = pars;
					}
					break;
				}
				case "four":
				{
					if(pars.length != 4)
					{
						throw new IllegalArgumentException();
					}
					else
					{
						modelNo = 3;
						parameters = pars;
					}
					break;
				}
				default:
				{
					System.out.println("Error (ExponentialSmoothing): model not recognized");
					return;
				}
			}
		}
		catch(IllegalArgumentException e)
		{
			System.out.println("Error (ExponentialSmoothing): no of parameters not correct");
			return;
		}
			
		additive = add;
		damped = damp;
	}
	
	public void train(String category)
	{
		switch(modelNo){
		case 0:{
			trainSES(category);
			break;
		}
		/*case 1:{
			return trainDES(pars[0],pars[1],periods,data);
		}
		case 2:{
			return trainTES("additive",pars[0],pars[1],pars[2],12,periods,data);
		}
		case 3:{
			return trainTES("multiplicative",pars[0],pars[1],pars[2],12,periods,data);
		}*/
		default:{
			System.out.println("Error (runES): default case reached");
		}
		}
	}
	
	public boolean isAdditive()
	{
		if( (modelNo == 1) || (modelNo == 2) )
		{
			return additive;
		}
		
		return false;
	}
	
	public boolean isMultiplicative()
	{
		if( (modelNo == 1) || (modelNo == 2) )
		{
			return !additive;
		}
		
		return false;
	}
	
	public boolean isDamped()
	{
		if( (modelNo == 1) || (modelNo == 2) )
		{
			return damped;
		}
		
		return false;
	}
	
	public int getModelNo()
	{
		return modelNo;
	}
	
	public String getModel()
	{
		switch (modelNo)
		{
			case 0: return "SES";
			case 1: return "DES";
			case 2: return "TES";
			case 3: return "four";
			default: return null;
		}
	}

	private void trainSES(String cat)
	{
		if( (parameters[0] < 0) || (parameters[0] > 1) )
		{
			modelError("trainSES","alpha should be between 0 and 1");
			return;
		}
		
		int idx1 = data.getIndexFromCat(cat);
		
		if( (data.getValidationFirstIndex()[idx1] - data.getTrainingFirstIndex()[idx1]) < noPersAhead)
		{
			modelError("trainSES","data is of length smaller than the number of periods");
			return;
		}
		
		double[][] dataset = data.getVolumes();
		trainingForecast = new double[data.getNoCats()];
		trainingReal = new double[data.getNoCats()];
		trainingDates = new String[data.getNoCats()];
		category = cat;
		
		int firstIndex = data.getTrainingFirstIndex()[idx1];
		int noData = data.getValidationFirstIndex()[idx1] - data.getTrainingFirstIndex()[idx1];
		trainingForecast = new double[noData];
		trainingReal = new double[noData];
		trainingDates = new String[noData];
		double[] avals = new double[noData];
		
		avals[0] = dataset[firstIndex][idx1];
		trainingReal[0] = dataset[firstIndex][idx1];
		trainingDates[0] = data.getDates()[firstIndex];
		
		for(int idx2=1;idx2<noData;idx2++)
		{
			avals[idx2] = parameters[0]*dataset[firstIndex+idx2][idx1] + (1-parameters[0])*avals[idx2-1];
			trainingReal[idx2] = dataset[firstIndex+idx2][idx1];
			trainingDates[idx2] = data.getDates()[firstIndex+idx2];
		}
		
		for(int idx2=0;idx2<noPersAhead;++idx2)
			trainingForecast[idx2] = dataset[firstIndex+idx2][idx1];
		
		for(int idx2=noPersAhead;idx2<noData;++idx2)
			trainingForecast[idx2] = avals[idx2-noPersAhead];
		
		trainingForecasted = true;
	}
	/*
	private static double[] trainDES(double alpha, double beta, int periods, double[] data)
	{
		if( (alpha < 0) || (alpha > 1) || (beta < 0) || (beta > 1) )
			return modelError("trainDES","alpha and beta should be between 0 and 1");
		
		if( data.length < (periods + 1))
			return modelError("trainDES","data is of length smaller than the number of periods");
		
		int noData = data.length;
		double[] output = new double[noData];
		double[] bvals = new double[noData];
		double[] avals = new double[noData];
		
		avals[1] = data[1];
		bvals[1] = data[1] - data[0];
		
		for(int idx=2;idx<noData;++idx)
		{
			avals[idx] = alpha * data[idx] + (1 - alpha) * (avals[idx-1] + bvals[idx-1]);
			bvals[idx] = beta * (avals[idx] - avals[idx-1]) + (1 - beta) * bvals[idx-1];
		}
		
		for(int idx=0;idx<Math.max(2,periods);++idx)
			output[idx] = data[idx];
		
		for(int idx=Math.max(2,periods);idx<noData;++idx)
			output[idx] = avals[idx-periods] + periods*bvals[idx-periods];
		
		return output;
	}
	
	private static double[] trainTES(String mode,double alpha, double beta, double gamma, int L, int periods, double[] data)
	{
		if( (alpha < 0) || (alpha > 1) || (beta < 0) || (beta > 1) || (gamma < 0) || (gamma > 1) )
			return modelError("trainTES","alpha, beta and gamma should be between 0 and 1");
		
		if( data.length < (L+1) )
			return modelError("trainTES","data is of length smaller than L+1");
		
		if(L<1)
			return modelError("trainTES","L should be larger than 0");
		
		int noData = data.length;
		int noCycles = noData / L;
		double[] output = new double[noData];
		double[] bvals = new double[noData];
		double[] cvals = new double[noData];
		double[] Avals = new double[noCycles];
		double[] avals = new double[noData];
		
		avals[0] = data[0];
		bvals[0] = 0;
		
		for(int idx1=0;idx1<L;++idx1)
		{
			bvals[0] += ( (data[idx1+L] - data[idx1]) / L );
			cvals[idx1] = 0;
			
			for(int idx2=0;idx2<noCycles;++idx2)
			{
				Avals[idx2] = 0;
				
				for(int idx3=0;idx3<L;++idx3)
					Avals[idx2] += data[idx2*L+idx3];
				
				Avals[idx2] = Avals[idx2] / L;
				
				cvals[idx1] += data[idx2*L+idx1] / Avals[idx2];
			}
			
			cvals[idx1] = cvals[idx1] / noCycles;
		}
		
		bvals[0] = bvals[0] / L;
		
		switch(mode){
		case "multiplicative":
		{
			for(int idx=1;idx<L;++idx)
			{
				avals[idx] = alpha * data[idx] / cvals[idx] + (1 - alpha) * (avals[idx-1] + bvals[idx-1]);
				bvals[idx] = beta * (avals[idx] - avals[idx-1]) + (1 - beta) * bvals[idx-1];
			}
			
			for(int idx=L;idx<noData;++idx)
			{
				avals[idx] = alpha * data[idx] / cvals[idx-L] + (1 - alpha) * (avals[idx-1] + bvals[idx-1]);
				bvals[idx] = beta * (avals[idx] - avals[idx-1]) + (1 - beta) * bvals[idx-1];
				cvals[idx] = gamma * data[idx] / avals[idx] + (1 - gamma) * cvals[idx-L];
			}
			
			for(int idx=0;idx<(L+periods);++idx)
				output[idx] = data[idx];
			
			for(int idx=(L+periods);idx<noData;idx++)
				output[idx] = (avals[idx-periods] + periods*bvals[idx-periods])*cvals[idx-periods-L+1+( (periods-1) % L)];
						
			return output;
		}
		case "additive":
		{
			for(int idx=1;idx<L;++idx)
			{
				avals[idx] = alpha * (data[idx] - cvals[idx]) + (1 - alpha) * (avals[idx-1] + bvals[idx-1]);
				bvals[idx] = beta * (avals[idx] - avals[idx-1]) + (1 - beta) * bvals[idx-1];
			}
			
			for(int idx=L;idx<noData;++idx)
			{
				avals[idx] = alpha * (data[idx] - cvals[idx-L]) + (1 - alpha) * (avals[idx-1] + bvals[idx-1]);
				bvals[idx] = beta * (avals[idx] - avals[idx-1]) + (1 - beta) * bvals[idx-1];
				cvals[idx] = gamma * (data[idx] - avals[idx-1] - bvals[idx-1]) + (1 - gamma) * cvals[idx-L];
			}
			
			for(int idx=0;idx<(L+periods);++idx)
				output[idx] = data[idx];
			
			for(int idx=(L+periods);idx<noData;idx++)
				output[idx] = avals[idx-periods] + periods*bvals[idx-periods] + cvals[idx-periods-L+1+( (periods-1) % L)];
			
			return output;
		}
		default:
		{
			System.out.println("Error (trainTES): default case reached");
			return null;
		}
		}
	}
	*/
}
