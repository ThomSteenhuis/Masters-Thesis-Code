package models;

import input.Data;

public class Naive extends Model {

	public Naive(Data data, int noPeriods)
	{
		super(data,noPeriods);
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
		for(int idx=0;idx<noPersAhead;++idx)
			trainingForecast[idx] = trainingReal[idx];
		
		for(int idx=noPersAhead;idx<trainingReal.length;++idx)
			trainingForecast[idx] = trainingReal[idx-noPersAhead];
		
		for(int idx=0;idx<noPersAhead;++idx)
			validationForecast[idx] = trainingReal[trainingReal.length-noPersAhead+idx];
		
		for(int idx=noPersAhead;idx<validationReal.length;++idx)
			validationForecast[idx] = validationReal[idx-noPersAhead];
		
		for(int idx=0;idx<noPersAhead;++idx)
			testingForecast[idx] = validationReal[validationReal.length-noPersAhead+idx];
		
		for(int idx=noPersAhead;idx<testingReal.length;++idx)
			testingForecast[idx] = testingReal[idx-noPersAhead];
		
		trainingForecasted = true;
		validationForecasted = true;
		testingForecasted = true;
	}
}
