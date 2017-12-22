package models;

import input.Data;

public class Naive extends Model {
	private int mode;

	public Naive(int m, Data data, int[] noPeriods, String[] cat)
	{
		super(data,noPeriods,cat);
		noParameters = 0;
		noConstants = 0;
		mode = m;
		name = "Naive"+Integer.toString(mode);		
	}
	
	public boolean train(boolean bootstrap) 
	{
		initializeSets();
		forecast();
		return true;
	}
	
	private void forecast()
	{
		switch(mode)
		{
		case 1:
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
			break;
		}
		case 2:
		{
			for(int idx1=0;idx1<noOutputs;++idx1)
			{
				int per = idx1 % noPersAhead.length;
				int lag = ( (noPersAhead[per] - 1) / 12 + 1) * 12;
				double[] forecasts = new double[trainingForecast[idx1].length+validationForecast[idx1].length+testingForecast[idx1].length];
								
				for(int idx2=0;idx2<lag;++idx2)
					forecasts[idx2] = trainingReal[idx1][idx2];
				
				for(int idx2=lag;idx2<forecasts.length;++idx2)
					forecasts[idx2] = (forecasts[idx2-noPersAhead[per]] + forecasts[idx2-lag]) / 2;
				
				for(int idx2=0;idx2<trainingForecast[idx1].length;++idx2)
					trainingForecast[idx1][idx2] = forecasts[idx2];
				
				for(int idx2=0;idx2<validationForecast[idx1].length;++idx2)
					validationForecast[idx1][idx2] = forecasts[idx2+trainingForecast[idx1].length];
				
				for(int idx2=0;idx2<testingForecast[idx1].length;++idx2)
					testingForecast[idx1][idx2] = forecasts[idx2+trainingForecast[idx1].length+validationForecast[idx1].length];
				
				forecasted[idx1] = true;
			}
			break;
		}
		default: System.out.println("Error (naive method): default case reached");
		}
		
	}
}
