package math;

public class GatingErrorFunction extends Function {
	
	private double[][] X;
	private double[] Y;
	private double[][] Y_hat;

	public GatingErrorFunction(double[][] x,double[] y, double[][] y_hat)
	{
		super((x[0].length+1)*y_hat[0].length);
		
		X = x;
		Y = y;
		Y_hat = y_hat;
	}
	
	public double evaluate(double[] input) throws ArrayIndexOutOfBoundsException 
	{
		if(input.length != noInputs) {System.out.println("Error (evaluate): input invalid"); return 0;}

		double output = 0;
		
		for(int idx1=0;idx1<X.length;++idx1)
		{
			double tmp = Y[idx1];
			
			for(int idx2=0;idx2<Y_hat[idx1].length;++idx2) 
			{
				double tmp2 = Math.abs(input[idx2*(X[idx1].length+1)]);
				for(int idx3=0;idx3<X[idx1].length;++idx3) tmp2 += Math.abs(input[idx2*(X[idx1].length+1)+1+idx3]*X[idx1][idx3]);
				tmp -= (tmp2*Y_hat[idx1][idx2]);
			}
			
			output += tmp*tmp;
		}
		
		return 0.5*output;
	}

}
