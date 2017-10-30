package models;

public class ExponentialSmoothing {

	public static double[] trainSES(double alpha, int periods, double[] data)
	{
		if( (alpha < 0) || (alpha > 1) )
			return modelError("trainSES","alpha should be between 0 and 1");
		
		if( data.length < periods)
			return modelError("trainSES","data is of length smaller than the number of periods");
		
		int noData = data.length;
		double[] output = new double[noData];
		double[] avals = new double[noData];
		
		avals[0] = data[0];
		
		for(int idx=1;idx<noData;idx++)
			avals[idx] = alpha*data[idx] + (1-alpha)*avals[idx-1];
		
		for(int idx=0;idx<periods;++idx)
			output[idx] = data[idx];
		
		for(int idx=periods;idx<noData;++idx)
			output[idx] = avals[idx-periods];
		
		return output;
	}
	
	public static double[] trainDES(double alpha, double beta, int periods, double[] data)
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
	
	public static double[] trainTES(String mode,double alpha, double beta, double gamma, int L, int periods, double[] data)
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
	
	private static double[] modelError(String model, String txt)
	{
		System.out.printf("Error (%s): %s\n",model,txt);
		return null;
	}
}
