package math;

public class LLAR1Function extends Function {

	double[] timeseries;
	
	public LLAR1Function(double[] ts)
	{
		super(3);
		timeseries = ts;
	}
	
	public double evaluate(double[] input) throws ArrayIndexOutOfBoundsException 
	{
		try{
			checkInput(input);
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
			return 0;
		}
		
		double constant1 = -0.5*Math.log(2*Math.PI);
		double constant2 = -0.5*(double)(timeseries.length-1)*Math.log(2*Math.PI);
		double variable1 = -0.5*Math.log(Math.pow(input[2],2)/(1-Math.pow(input[1],2)));
		double variable2 = -Math.pow(timeseries[0]-input[0]/(1-input[1]), 2)/(2*Math.pow(input[2],2)/(1-Math.pow(input[1],2)));
		double variable3 = -(double)(timeseries.length-1)*Math.log(input[2]);
		double variable4 = 0;
		
		for(int idx=1;idx<timeseries.length;++idx)
			variable4 += Math.pow(timeseries[idx] - input[0] - input[1]*timeseries[idx-1],2);
			
		variable4 = -2/(Math.pow(input[2],2)) * variable4;
		
		return constant1 + constant2 + variable1 + variable2 + variable3 + variable4;
	}

}
