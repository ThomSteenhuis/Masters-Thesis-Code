package math;

import models.ARIMA;

public class LLARMAFunction extends Function {
	
	private ARIMA arima;
	private double constant;
	private int p;
	private int q;

	public LLARMAFunction(ARIMA a,int P,int Q)
	{
		super(2+P+Q);
		arima = a;
		constant = 0.5*(double)(arima.getTimeSeries().length-P)*Math.log(2*Math.PI);
		p = P;
		q = Q;
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

		double[] input1 = new double[p];
		double[] input2 = new double[q];
		
		for(int idx=0;idx<p;++idx)
			input1[idx] = Math.tanh(input[idx+1]);
		
		for(int idx=0;idx<q;++idx)
			input2[idx] = Math.tanh(input[idx+1+p]);
		
		double input3 = Math.abs(input[1+p+q]);
		double variable1 = 0.5*(double)(arima.getTimeSeries().length-p)*Math.log(input3);
		double variable2 = 0;
		double[] error = arima.calculateErrors(input[0],input1,input2);

		for(int idx1=p;idx1<error.length;++idx1)
			variable2 += error[idx1]* error[idx1];

		variable2 = 0.5 * variable2 / input3;

		return constant + variable1 + variable2;
	}
	
	public ARIMA getArima()
	{
		return arima;
	}
	
	public double getConstant()
	{
		return constant;
	}

	public int getP()
	{
		return p;
	}
	
	public int getQ()
	{
		return q;
	}
}
