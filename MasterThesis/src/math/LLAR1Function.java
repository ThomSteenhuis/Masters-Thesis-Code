package math;

public class LLAR1Function extends Function {

	private double[] timeseries;
	private double constant;

	public LLAR1Function(double[] ts)
	{
		super(3);
		timeseries = ts;
		constant = 0.5*(double)(ts.length)*Math.log(2*Math.PI);
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

		double input1 = Math.tanh(input[1]);
		double input2 = Math.abs(input[2]);
		double variable1 = 0.5*Math.log(input2/(1-Math.pow(input1,2)));
		double variable2 = 0.5*Math.pow(timeseries[0]-input[0]/(1-input1), 2)/(input2/(1-Math.pow(input1,2)));
		double variable3 = 0.5*(double)(timeseries.length-1)*Math.log(input2);
		double variable4 = 0;

		for(int idx=1;idx<timeseries.length;++idx)
			variable4 += Math.pow(timeseries[idx] - input[0] - input1*timeseries[idx-1],2);

		variable4 = 0.5 * variable4 / input2;

		return constant + variable1 + variable2 + variable3 + variable4;
	}

}
