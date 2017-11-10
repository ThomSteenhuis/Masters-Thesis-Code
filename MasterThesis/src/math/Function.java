package math;

public abstract class Function {
		
	private final double h = 0.00001;
	
	public Function() {}
	
	public double[] derivative(double[] input) throws ArrayIndexOutOfBoundsException
	{
		double[] output = new double[input.length];
		
		for(int idx=0;idx<input.length;++idx)
			output[idx] = partialDerivative(idx,input);
		
		return output;
	}
	
	public double partialDerivative(int inputNo,double[] input) throws ArrayIndexOutOfBoundsException
	{
		double[] vector = new double[input.length];
		
		for(int idx=0;idx<input.length;++idx)
			vector[idx] = input[idx];
		
		vector[inputNo] += h;
		
		return ((evaluate(vector)-evaluate(input))/h);
	}
	
	public double[][] hessian(double[] input) throws ArrayIndexOutOfBoundsException
	{
		double[][] h = new double[input.length][input.length];
		
		for(int idx1=0;idx1<input.length;++idx1)
		{
			for(int idx2=0;idx2<input.length;++idx2)
				h[idx1][idx2] = partialSecondDerivative(idx1,idx2,input);
		}
		
		return h;
	}
	
	public double partialSecondDerivative(int inputNo1,int inputNo2,double[] input) throws ArrayIndexOutOfBoundsException
	{
		double[] vector1 = new double[input.length];
		double[] vector2 = new double[input.length];
		double[] vector3 = new double[input.length];
		
		for(int idx=0;idx<input.length;++idx)
		{
			vector1[idx] = input[idx];
			vector2[idx] = input[idx];
			vector3[idx] = input[idx];
		}
		
		vector1[inputNo1] += h;
		vector2[inputNo2] += h;
		vector3[inputNo1] += h;
		vector3[inputNo2] += h;
		
		return ((evaluate(vector3)-evaluate(vector1)-evaluate(vector2)+evaluate(input))/(h*h));
	}
	
	public abstract double evaluate(double[] input) throws ArrayIndexOutOfBoundsException;
}
