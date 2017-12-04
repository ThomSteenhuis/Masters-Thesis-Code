package math;

public class FFANNErrorFunction extends Function {
	
	private int noHiddenUnits;
	private double[][] X;
	private double[][] Y;

	public FFANNErrorFunction(int hidden,double[][] x,double[][] y)
	{
		super((x.length+1)*hidden+(hidden+1)*y.length);

		if(hidden < 1)
			System.out.println("Error (FFANNErrorFunction): no of hidden units must be at least 1");
		
		noHiddenUnits = hidden;
		X = x;
		Y = y;
	}
	
	public double evaluate(double[] weights) throws ArrayIndexOutOfBoundsException 
	{
		if(weights.length != noInputs) {System.out.println("Error (evaluate): input invalid"); return 0;}
		
		return Matrix.sum(calculateSquaredError(weights));
	}
	
	private double[][] calculateSquaredError(double[] weights)
	{
		double[][] output = new double[Y.length][Y[0].length];
		
		for(int idx2=0;idx2<output[0].length;++idx2) 
		{
			double[] forecast = calculateForecast(idx2,weights);
			for(int idx1=0;idx1<output.length;++idx1) output[idx1][idx2] = (Y[idx1][idx2] - forecast[idx1]) * (Y[idx1][idx2] - forecast[idx1]);
		}
		
		return output;
	}

	private double[] calculateForecast(int index,double[] weights)
	{
		double[] output = new double[Y.length];
		
		for(int idx1=0;idx1<output.length;++idx1)
		{
			output[idx1] = weights[weights.length-(noHiddenUnits+1)*Y.length];
			
			for(int idx2=weights.length-noHiddenUnits*Y.length+idx1;idx2<weights.length;idx2+= Y.length) 
				output[idx1] += weights[idx2] * calculateZ(index,(idx2-weights.length+noHiddenUnits*Y.length)/Y.length,weights);
		}

		return output;
	}
	
	private double calculateZ(int index1,int index2,double[] array)
	{
		double input = array[index2];
		int firstIndex = noHiddenUnits + index2;
		
		for(int idx=0;idx<X.length;++idx) input += X[idx][index1] * array[firstIndex+noHiddenUnits*idx];
		
		return Math.tanh(input);
	}
}
