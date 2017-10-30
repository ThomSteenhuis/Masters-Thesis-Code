package models;

public class ExponentialSmoothing {

	public static double[] trainSES(double alpha, double[] data)
	{
		int noData = data.length;
		double[] output = new double[noData];
		
		output[0] = data[0];
		
		for(int idx=1;idx<noData;idx++)
		{
			output[idx] = alpha*data[idx-1] + (1-alpha)*output[idx-1];
		}
		
		return output;
	}
}
