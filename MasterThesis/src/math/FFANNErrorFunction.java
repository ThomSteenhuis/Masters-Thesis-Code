package math;

public class FFANNErrorFunction extends Function {
	
	private int noHiddenUnits;
	private double[][] X;
	private double[] Y;

	public FFANNErrorFunction(int hidden,double[][] x,double[] y)
	{
		super((x.length+1)*hidden+hidden+1);
		if(hidden < 1)
			System.out.println("Error (FFANNErrorFunction): no of hidden units must be at least 1");
		
		noHiddenUnits = hidden;
		X = x;
		Y = y;
	}
	
	public double evaluate(double[] input) throws ArrayIndexOutOfBoundsException 
	{
		if(input.length != noInputs) {System.out.println("Error (evaluate): input invalid"); return 0;}
		
		return Matrix.sum(calculateSquaredError(input));
	}
	
	private double[] calculateSquaredError(double[] array)
	{
		double[] output = new double[Y.length];
		
		for(int idx=0;idx<output.length;++idx) output[idx] = Math.pow(Y[idx] - calculateForecast(idx,array), 2);
		
		return output;
	}

	private double calculateForecast(int index,double[] array)
	{
		double output = array[array.length-1-noHiddenUnits];
		
		for(int idx=array.length-noHiddenUnits;idx<array.length;++idx) output += array[idx] * calculateZ(index,idx-(array.length-1-noHiddenUnits),array);
		
		return output;
	}
	
	private double calculateZ(int index1,int index2,double[] array)
	{
		double input = array[index2];
		int firstIndex = noHiddenUnits + index1 * noHiddenUnits + index2;
		
		for(int idx=0;idx<X.length;++idx) input += X[idx][index1] * array[firstIndex+idx];
		
		return Math.tanh(input);
	}
}
