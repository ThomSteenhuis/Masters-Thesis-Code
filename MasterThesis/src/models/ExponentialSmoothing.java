package models;

public class ExponentialSmoothing {

	public static double[] trainSES(double alpha, double[] data)
	{
		if( (alpha < 0) || (alpha > 1) )
			return modelError("trainSES","alpha should be between 0 and 1");
		
		if( data.length == 0)
			return modelError("trainSES","data is of length 0");
		
		int noData = data.length;
		double[] output = new double[noData];
		
		output[0] = data[0];
		
		for(int idx=1;idx<noData;idx++)
		{
			output[idx] = alpha*data[idx-1] + (1-alpha)*output[idx-1];
		}
		
		return output;
	}
	
	public static double[] trainDES(double alpha, double beta, double[] data)
	{
		if( (alpha < 0) || (alpha > 1) || (beta < 0) || (beta > 1) )
			return modelError("trainDES","alpha and beta should be between 0 and 1");
		
		if( data.length < 2)
			return modelError("trainSES","data is of length smaller than 2");
		
		int noData = data.length;
		double[] output = new double[noData];
		double[] bvals = new double[noData];
		
		output[0] = data[0];
		output[1] = data[1];
		bvals[1] = data[1] - data[0];
		
		for(int idx=2;idx<noData;++idx)
		{
			output[idx] = alpha * data[idx-1] + (1 - alpha) * (output[idx-1] + bvals[idx-1]);
			bvals[idx] = beta * (output[idx] - output[idx-1]) + (1 - beta) * bvals[idx-1];
		}
		
		return output;
	}
	
	private static double[] modelError(String model, String txt)
	{
		System.out.printf("Error (%s): %s\n",model,txt);
		return null;
	}
}
