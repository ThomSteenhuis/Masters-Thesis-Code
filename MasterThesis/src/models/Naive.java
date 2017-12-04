package models;

import input.Data;

public class Naive extends Model {

	public Naive(Data data, int[] noPeriods, String[] cat)
	{
		super(data,noPeriods,cat);
		noParameters = 0;
		noConstants = 0;
		name = "Naive";
	}
	
	public boolean train() 
	{
		initializeSets();
		forecast();
		return true;
	}
	
	private void forecast()
	{
		for(int idx1=0;idx1<noOutputs;++idx1)
		{
			int per = idx1 % noPersAhead.length;
			
			for(int idx2=0;idx2<noPersAhead[per];++idx2)
				trainingForecast[idx1][idx2] = trainingReal[idx1][idx2];
			
			for(int idx2=noPersAhead[per];idx2<trainingReal[idx1].length;++idx2)
				trainingForecast[idx1][idx2] = trainingReal[idx1][idx2-noPersAhead[per]];
			
			for(int idx2=0;idx2<noPersAhead[per];++idx2)
				validationForecast[idx1][idx2] = trainingReal[idx1][trainingReal.length-noPersAhead[per]+idx2];
			
			for(int idx2=noPersAhead[per];idx2<validationReal[idx1].length;++idx2)
				validationForecast[idx1][idx2] = validationReal[idx1][idx2-noPersAhead[per]];
			
			for(int idx2=0;idx2<noPersAhead[per];++idx2)
				testingForecast[idx1][idx2] = validationReal[idx1][validationReal.length-noPersAhead[per]+idx2];
			
			for(int idx2=noPersAhead[per];idx2<testingReal[idx1].length;++idx2)
				testingForecast[idx1][idx2] = testingReal[idx1][idx2-noPersAhead[per]];
			
			forecasted[idx1] = true;
		}
	}
}
