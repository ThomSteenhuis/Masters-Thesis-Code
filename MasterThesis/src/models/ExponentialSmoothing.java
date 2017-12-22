package models;

import input.Data;

public class ExponentialSmoothing extends Model{
	
	private boolean additive;
	private boolean damped;
	private int modelNo;
	
	public ExponentialSmoothing(String model,int[] periods,String[] cats,Data dataset)
	{
		super(dataset,periods,cats);
		name = model;
		noOutputs = periods.length * cats.length;
		
		try{
			switch(model)
			{
				case "SES":
				{
					modelNo = 0;
					noParameters = 1;
					noConstants = 0;
					break;
				}
				case "aDES":
				{
					modelNo = 1;
					noConstants = 0;
					noParameters = 2;
					additive = true;
					damped = false;
					break;
				}
				case "mDES":
				{
					modelNo = 1;
					noConstants = 0;
					noParameters = 2;
					additive = false;
					damped = false;
					break;
				}
				case "aDESd":
				{
					modelNo = 1;
					noConstants = 0;
					noParameters = 3;
					additive = true;
					damped = true;
					break;
				}
				case "mDESd":
				{
					modelNo = 1;
					noConstants = 0;
					noParameters = 3;
					additive = false;
					damped = true;
					break;
				}
				case "aTES":
				{
					modelNo = 2;
					noConstants = 1;
					noParameters = 3;
					additive = true;
					damped = false;
					break;
				}
				case "mTES":
				{
					modelNo = 2;
					noConstants = 1;
					noParameters = 3;
					additive = false;
					damped = false;
					break;
				}
				case "aTESd":
				{
					modelNo = 2;
					noConstants = 1;
					noParameters = 4;
					additive = true;
					damped = true;
					break;
				}
				case "mTESd":
				{
					modelNo = 2;
					noConstants = 1;
					noParameters = 4;
					additive = false;
					damped = true;
					break;
				}
				case "four":
				{
					modelNo = 3;
					noParameters = 4;
					noConstants = 1;
					additive = false;
					damped = false;
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
	}
	
	public boolean train(boolean bootstrap)
	{
		if(parameters.length != noParameters)
		{
			{
				modelError("trainSES","inadequate no parameters");
				return false;
			}
		}
		
		for(int idx=0;idx<noParameters;++idx)
		{
			if( (parameters[idx] < 0) || (parameters[idx] > 1) )
			{
				modelError("train","parameter should be between 0 and 1");
				return false;
			}
		}
		
		switch(modelNo){
		case 0:{
			return train_val_testSES();
		}
		case 1:{
			return train_val_testDES();
		}
		case 2:{
			return train_val_testTES();
		}
		case 3:{
			return train_val_testTES();	
		}
		default:{
			System.out.println("Error (train): default case reached");
			return false;
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

	private boolean train_val_testSES()
	{			
		double[][] dataset = data.getVolumes();
		initializeSets();
		
		for(int idx1=0;idx1<noOutputs;++idx1)
		{
			int cat = idx1 / noPersAhead.length;
			int per = idx1 % noPersAhead.length;
			int index = data.getIndexFromCat(category[cat]);
			
			if( (data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index]) < noPersAhead[per])
			{
				modelError("trainSES","data is of length smaller than the number of periods");
				return false;
			}
			
			int firstIndex = data.getTrainingFirstIndex()[index];
			int noData1 = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index];
			int noData2 = data.getTestingFirstIndex()[index] - data.getValidationFirstIndex()[index];
			int noData3 = data.getNoObs() - data.getTestingFirstIndex()[index];
			
			double[] avals = new double[noData1+noData2+noData3];
			
			avals[0] = dataset[firstIndex][idx1];
			
			for(int idx2=1;idx2<noData1;idx2++)
				avals[idx2] = parameters[0]*dataset[firstIndex+idx2][index] + (1-parameters[0])*avals[idx2-1];
			
			for(int idx2=0;idx2<noData2;idx2++)
				avals[noData1+idx2] = parameters[0]*dataset[firstIndex+noData1+idx2][index] + (1-parameters[0])*avals[idx2+noData1-1];
					
			for(int idx2=0;idx2<noData3;idx2++)
				avals[noData1+noData2+idx2] = parameters[0]*dataset[firstIndex+noData1+noData2+idx2][index] + (1-parameters[0])*avals[idx2+noData1+noData2-1];
			
			for(int idx2=0;idx2<noPersAhead[per];++idx2)
				trainingForecast[idx1][idx2] = dataset[firstIndex+idx2][index];
			
			for(int idx2=noPersAhead[per];idx2<noData1;++idx2)
				trainingForecast[idx1][idx2] = avals[idx2-noPersAhead[per]];
			
			for(int idx2=0;idx2<noData2;++idx2)
				validationForecast[idx1][idx2] = avals[idx2+noData1-noPersAhead[per]];
			
			for(int idx2=0;idx2<noData3;++idx2)
				testingForecast[idx1][idx2] = avals[idx2+noData1+noData2-noPersAhead[per]];
			
			forecasted[idx1] = true;
		}

		return true;		
	}
	
	private boolean train_val_testDES()
	{		
		double[][] dataset = data.getVolumes();
		initializeSets();
		
		for(int idx1=0;idx1<noOutputs;++idx1)
		{
			int cat = idx1 / noPersAhead.length;
			int per = idx1 % noPersAhead.length;
			int index = data.getIndexFromCat(category[cat]);
			
			if( (data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index]) < noPersAhead[per])
			{
				modelError("trainDES","data is of length smaller than the number of periods");
				return false;
			}
			
			int firstIndex = data.getTrainingFirstIndex()[index];
			int noData1 = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index];
			int noData2 = data.getTestingFirstIndex()[index] - data.getValidationFirstIndex()[index];
			int noData3 = data.getNoObs() - data.getTestingFirstIndex()[index];			
			
			double[] avals = new double[data.getNoObs() - data.getTrainingFirstIndex()[index]];
			double[] bvals = new double[avals.length];
			
			avals[1] = dataset[firstIndex+1][index];
			
			if(additive)
				bvals[1] = dataset[firstIndex+1][index] - dataset[firstIndex][index];
			else
				bvals[1] = dataset[firstIndex+1][index] / dataset[firstIndex][index];
					
			for(int idx2=2;idx2<(avals.length);idx2++)
			{
				if(additive)
				{
					if(damped)
					{
						avals[idx2] = parameters[0] * dataset[firstIndex+idx2][index] + (1 - parameters[0]) * (avals[idx2-1] + parameters[2] * bvals[idx2-1]);
						bvals[idx2] = parameters[1] * (avals[idx2] - avals[idx2-1]) + (1 - parameters[1]) * parameters[2] * bvals[idx2-1];
					}
					else
					{
						avals[idx2] = parameters[0] * dataset[firstIndex+idx2][index] + (1 - parameters[0]) * (avals[idx2-1] + bvals[idx2-1]);
						bvals[idx2] = parameters[1] * (avals[idx2] - avals[idx2-1]) + (1 - parameters[1]) * bvals[idx2-1];
					}
				}
				else
				{
					if(damped)
					{
						avals[idx2] = parameters[0] * dataset[firstIndex+idx2][index] + (1 - parameters[0]) * avals[idx2-1] * Math.pow(bvals[idx2-1] , parameters[2]);
						bvals[idx2] = parameters[1] * avals[idx2] / avals[idx2-1] + (1 - parameters[1]) * Math.pow(bvals[idx2-1] , parameters[2]);
					}
					else
					{
						avals[idx2] = parameters[0] * dataset[firstIndex+idx2][index] + (1 - parameters[0]) * avals[idx2-1] * bvals[idx2-1];
						bvals[idx2] = parameters[1] * avals[idx2] / avals[idx2-1] + (1 - parameters[1]) * bvals[idx2-1];
					}
				}
			}
			
			for(int idx2=0;idx2<Math.max(2,noPersAhead[per]);++idx2)
				trainingForecast[idx1][idx2] = dataset[firstIndex+idx2][index];
			
			double dampingValue = noPersAhead[per];
				
			if(damped)
			{
				dampingValue = 0;
				
				for(int idx=0;idx<noPersAhead[per];++idx)
					dampingValue += Math.pow(parameters[2],idx+1);
			}	
			
			for(int idx2=Math.max(2,noPersAhead[per]);idx2<noData1;++idx2)
			{
				if(additive)
					trainingForecast[idx1][idx2] = avals[idx2-noPersAhead[per]] + dampingValue * bvals[idx2-noPersAhead[per]];
				else
					trainingForecast[idx1][idx2] = avals[idx2-noPersAhead[per]] + Math.pow(bvals[idx2-noPersAhead[per]] , dampingValue);
			}
			
			for(int idx2=0;idx2<noData2;++idx2)
			{
				if(additive)
					validationForecast[idx1][idx2] = avals[idx2+noData1-noPersAhead[per]] + dampingValue * bvals[idx2+noData1-noPersAhead[per]];
				else
					validationForecast[idx1][idx2] = avals[idx2+noData1-noPersAhead[per]] + Math.pow(bvals[idx2+noData1-noPersAhead[per]] , dampingValue);
			}
			
			for(int idx2=0;idx2<noData3;++idx2)
			{
				if(additive)
					testingForecast[idx1][idx2] = avals[idx2+noData1+noData2-noPersAhead[per]] + dampingValue * bvals[idx2+noData1+noData2-noPersAhead[per]];
				else
					testingForecast[idx1][idx2] = avals[idx2+noData1+noData2-noPersAhead[per]] + Math.pow(bvals[idx2+noData1+noData2-noPersAhead[per]] , dampingValue);
			}
			
			forecasted[idx1] = true;
		}
		
		return true;
	}
	
	private boolean train_val_testTES()
	{		
		double[][] dataset = data.getVolumes();
		initializeSets();
		
		for(int idx1=0;idx1<noOutputs;++idx1)
		{
			int cat = idx1 / noPersAhead.length;
			int per = idx1 % noPersAhead.length;
			int index = data.getIndexFromCat(category[cat]);
			
			if( (data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index]) < (constants[0]+noPersAhead[per]) )
			{
				modelError("trainTES","data is of length smaller than the number of periods");
				return false;
			}
			
			if(constants[0]<1)
			{
				modelError("trainTES","L should be larger than 0");
				return false;
			}
			
			int firstIndex = data.getTrainingFirstIndex()[index];
			int noData1 = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index];
			int noData2 = data.getTestingFirstIndex()[index] - data.getValidationFirstIndex()[index];
			int noData3 = data.getNoObs() - data.getTestingFirstIndex()[index];
			int L = (int) constants[0];
			int noCycles = data.getNoObs() / L;
			
			double[] avals = new double[data.getNoObs() - data.getTrainingFirstIndex()[index]];
			double[] bvals = new double[avals.length];
			double[] cvals = new double[avals.length];
			double[] Avals = new double[noCycles];
			
			avals[0] = dataset[firstIndex][index];
			bvals[0] = 0;
			
			for(int idx2=0;idx2<L;++idx2)
			{
				bvals[0] += ( (dataset[idx2+L][index] - dataset[idx2][index]) / L );
				cvals[idx2] = 0;
				
				for(int idx3=0;idx3<noCycles;++idx3)
				{
					Avals[idx3] = 0;
					
					for(int idx4=0;idx4<L;++idx4)
						Avals[idx3] += dataset[idx3*L+idx4][index];
					
					Avals[idx3] = Avals[idx3] / L;
					
					cvals[idx2] += dataset[idx3*L+idx2][index] / Avals[idx3];
				}
				
				cvals[idx2] = cvals[idx2] / noCycles;
			}
			
			bvals[0] = bvals[0] / L;
			
			if(modelNo == 3)
				train_val_testFour(dataset,idx1,per,index,firstIndex,avals,bvals,cvals,L,noData1,noData2,noData3);
			else if(additive && !damped)
				train_val_testTESadditive(dataset,idx1,per,index,firstIndex,avals,bvals,cvals,L,noData1,noData2,noData3);
			else if(!additive && !damped)
				train_val_testTESmultiplicative(dataset,idx1,per,index,firstIndex,avals,bvals,cvals,L,noData1,noData2,noData3);
			else if(additive && damped)
				train_val_testTESdampedAdditive(dataset,idx1,per,index,firstIndex,avals,bvals,cvals,L,noData1,noData2,noData3);
			else
				train_val_testTESdampedMultiplicative(dataset,idx1,per,index,firstIndex,avals,bvals,cvals,L,noData1,noData2,noData3);
			
			forecasted[idx1] = true;
		}
		
		return true;
	}
	
	private void train_val_testFour(double[][] dataset,int idx1,int per,int index,int firstIndex,double[] avals,double[] bvals,double[] cvals,int L,int noData1,int noData2,int noData3)
	{
		for(int idx=1;idx<L;++idx)
		{
			avals[idx] = parameters[0] * dataset[firstIndex+idx][index] - parameters[3] * cvals[idx] + (1 - parameters[0]) * (avals[idx-1] + bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * bvals[idx-1];
		}
		
		for(int idx=L;idx<(noData1+noData2+noData3);++idx)
		{
			avals[idx] = parameters[0] * dataset[firstIndex+idx][index] - parameters[3] * cvals[idx-L] + (1 - parameters[0]) * (avals[idx-1] + bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * bvals[idx-1];
			cvals[idx] = parameters[2] * (dataset[firstIndex+idx][index] - avals[idx-1] - bvals[idx-1]) + (1 - parameters[2]) * cvals[idx-L];
		}
		
		for(int idx=0;idx<(L+noPersAhead[per]);++idx)
			trainingForecast[idx1][idx] = dataset[firstIndex+idx][index];
		
		for(int idx=(L+noPersAhead[per]);idx<noData1;idx++)
			trainingForecast[idx1][idx] = avals[idx-noPersAhead[per]] + noPersAhead[per]*bvals[idx-noPersAhead[per]] + cvals[idx-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData2;idx++)
			validationForecast[idx1][idx] = avals[idx+noData1-noPersAhead[per]] + noPersAhead[per]*bvals[idx+noData1-noPersAhead[per]] + cvals[idx+noData1-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData3;idx++)
			testingForecast[idx1][idx] = avals[idx+noData1+noData2-noPersAhead[per]] + noPersAhead[per]*bvals[idx+noData1+noData2-noPersAhead[per]] + cvals[idx+noData1+noData2-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
	}
	
	private void train_val_testTESadditive(double[][] dataset,int idx1,int per,int index,int firstIndex,double[] avals,double[] bvals,double[] cvals,int L,int noData1,int noData2,int noData3)
	{
		for(int idx=1;idx<L;++idx)
		{
			avals[idx] = parameters[0] * (dataset[firstIndex+idx][index] - cvals[idx]) + (1 - parameters[0]) * (avals[idx-1] + bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * bvals[idx-1];
		}
		
		for(int idx=L;idx<(noData1+noData2+noData3);++idx)
		{
			avals[idx] = parameters[0] * (dataset[firstIndex+idx][index] - cvals[idx-L]) + (1 - parameters[0]) * (avals[idx-1] + bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * bvals[idx-1];
			cvals[idx] = parameters[2] * (dataset[firstIndex+idx][index] - avals[idx-1] - bvals[idx-1]) + (1 - parameters[2]) * cvals[idx-L];
		}
		
		for(int idx=0;idx<(L+noPersAhead[per]);++idx)
			trainingForecast[idx1][idx] = dataset[firstIndex+idx][index];
				
		for(int idx=(L+noPersAhead[per]);idx<noData1;idx++)
			trainingForecast[idx1][idx] = avals[idx-noPersAhead[per]] + noPersAhead[per]*bvals[idx-noPersAhead[per]] + cvals[idx-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData2;idx++)
			validationForecast[idx1][idx] = avals[idx+noData1-noPersAhead[per]] + noPersAhead[per]*bvals[idx+noData1-noPersAhead[per]] + cvals[idx+noData1-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData3;idx++)
			testingForecast[idx1][idx] = avals[idx+noData1+noData2-noPersAhead[per]] + noPersAhead[per]*bvals[idx+noData1+noData2-noPersAhead[per]] + cvals[idx+noData1+noData2-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
	}
	
	private void train_val_testTESmultiplicative(double[][] dataset,int idx1,int per,int index,int firstIndex,double[] avals,double[] bvals,double[] cvals,int L,int noData1,int noData2,int noData3)
	{
		for(int idx=1;idx<L;++idx)
		{
			avals[idx] = parameters[0] * dataset[firstIndex+idx][index] / cvals[idx] + (1 - parameters[0]) * (avals[idx-1] + bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * bvals[idx-1];
		}
		
		for(int idx=L;idx<(noData1+noData2+noData3);++idx)
		{
			avals[idx] = parameters[0] * dataset[firstIndex+idx][index] / cvals[idx-L] + (1 - parameters[0]) * (avals[idx-1] + bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * bvals[idx-1];
			cvals[idx] = parameters[2] * dataset[firstIndex+idx][index] / (avals[idx-1] + bvals[idx-1]) + (1 - parameters[2]) * cvals[idx-L];
		}
		
		for(int idx=0;idx<(L+noPersAhead[per]);++idx)
			trainingForecast[idx1][idx] = dataset[firstIndex+idx][index];
				
		for(int idx=(L+noPersAhead[per]);idx<noData1;idx++)
			trainingForecast[idx1][idx] = (avals[idx-noPersAhead[per]] + noPersAhead[per]*bvals[idx-noPersAhead[per]]) * cvals[idx-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData2;idx++)
			validationForecast[idx1][idx] = (avals[idx+noData1-noPersAhead[per]] + noPersAhead[per]*bvals[idx+noData1-noPersAhead[per]]) * cvals[idx+noData1-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData3;idx++)
			testingForecast[idx1][idx] = (avals[idx+noData1+noData2-noPersAhead[per]] + noPersAhead[per]*bvals[idx+noData1+noData2-noPersAhead[per]]) * cvals[idx+noData1+noData2-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
	}
	
	private void train_val_testTESdampedAdditive(double[][] dataset,int idx1,int per,int index,int firstIndex,double[] avals,double[] bvals,double[] cvals,int L,int noData1,int noData2,int noData3)
	{
		for(int idx=1;idx<L;++idx)
		{
			avals[idx] = parameters[0] * (dataset[firstIndex+idx][index] - cvals[idx]) + (1 - parameters[0]) * (avals[idx-1] + parameters[3] * bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * parameters[3] * bvals[idx-1];
		}
		
		for(int idx=L;idx<(noData1+noData2+noData3);++idx)
		{
			avals[idx] = parameters[0] * (dataset[firstIndex+idx][index] - cvals[idx-L]) + (1 - parameters[0]) * (avals[idx-1] + parameters[3] * bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * parameters[3] * bvals[idx-1];
			cvals[idx] = parameters[2] * (dataset[firstIndex+idx][index] - avals[idx-1] - parameters[3] * bvals[idx-1]) + (1 - parameters[2]) * cvals[idx-L];
		}
		
		for(int idx=0;idx<(L+noPersAhead[per]);++idx)
			trainingForecast[idx1][idx] = dataset[firstIndex+idx][index];
				
		double dampingValue = 0;
		
		for(int idx=0;idx<noPersAhead[per];++idx)
			dampingValue += Math.pow(parameters[3],idx+1);
		
		for(int idx=(L+noPersAhead[per]);idx<noData1;idx++)
			trainingForecast[idx1][idx] = avals[idx-noPersAhead[per]] + dampingValue*bvals[idx-noPersAhead[per]] + cvals[idx-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData2;idx++)
			validationForecast[idx1][idx] = avals[idx+noData1-noPersAhead[per]] + dampingValue*bvals[idx+noData1-noPersAhead[per]] + cvals[idx+noData1-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData3;idx++)
			testingForecast[idx1][idx] = avals[idx+noData1+noData2-noPersAhead[per]] + dampingValue*bvals[idx+noData1+noData2-noPersAhead[per]] + cvals[idx+noData1+noData2-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
	}
	
	private void train_val_testTESdampedMultiplicative(double[][] dataset,int idx1,int per,int index,int firstIndex,double[] avals,double[] bvals,double[] cvals,int L,int noData1,int noData2,int noData3)
	{
		for(int idx=1;idx<L;++idx)
		{
			avals[idx] = parameters[0] * dataset[firstIndex+idx][index] / cvals[idx] + (1 - parameters[0]) * (avals[idx-1] + parameters[3] * bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * parameters[3] * bvals[idx-1];
		}
		
		for(int idx=L;idx<(noData1+noData2+noData3);++idx)
		{
			avals[idx] = parameters[0] * dataset[firstIndex+idx][index] / cvals[idx-L] + (1 - parameters[0]) * (avals[idx-1] + parameters[3] * bvals[idx-1]);
			bvals[idx] = parameters[1] * (avals[idx] - avals[idx-1]) + (1 - parameters[1]) * parameters[3] * bvals[idx-1];
			cvals[idx] = parameters[2] * dataset[firstIndex+idx][index] / (avals[idx-1] + parameters[3] * bvals[idx-1]) + (1 - parameters[2]) * cvals[idx-L];
		}
		
		for(int idx=0;idx<(L+noPersAhead[per]);++idx)
			trainingForecast[idx1][idx] = dataset[firstIndex+idx][index];
				
		double dampingValue = 0;
		
		for(int idx=0;idx<noPersAhead[per];++idx)
			dampingValue += Math.pow(parameters[3],idx+1);
		
		for(int idx=(L+noPersAhead[per]);idx<noData1;idx++)
			trainingForecast[idx1][idx] = (avals[idx-noPersAhead[per]] + dampingValue*bvals[idx-noPersAhead[per]]) * cvals[idx-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData2;idx++)
			validationForecast[idx1][idx] = (avals[idx+noData1-noPersAhead[per]] + dampingValue*bvals[idx+noData1-noPersAhead[per]]) * cvals[idx+noData1-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
				
		for(int idx=0;idx<noData3;idx++)
			testingForecast[idx1][idx] = (avals[idx+noData1+noData2-noPersAhead[per]] + dampingValue*bvals[idx+noData1+noData2-noPersAhead[per]]) * cvals[idx+noData1+noData2-noPersAhead[per]-L+1+( (noPersAhead[per]-1) % L)];
	}
}
