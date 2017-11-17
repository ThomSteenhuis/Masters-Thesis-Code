package math;

public class LLARMAFunction extends Function {
	
	private double[] timeseries;
	private double constant;
	private int p;
	private int q;

	public LLARMAFunction(double[] ts,int P,int Q)
	{
		super(2+P+Q);
		timeseries = ts;
		constant = 0.5*(double)(ts.length-P)*Math.log(2*Math.PI);
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
		double variable1 = 0.5*(double)(timeseries.length-p)*Math.log(input3);
		double variable2 = 0;
		double[] error = new double[timeseries.length];

		for(int idx1=p;idx1<timeseries.length;++idx1)
		{
			error[idx1] = timeseries[idx1] - input[0];
			
			for(int idx2=(idx1-p);idx2<idx1;++idx2)
				error[idx1] -= input1[idx1-idx2-1] * (timeseries[idx2] - input[0]);
			
			for(int idx2=Math.max(idx1-q,0);idx2<idx1;++idx2)
				error[idx1] -= input2[idx1-idx2-1] * error[idx2];
			
			variable2 += Math.pow(error[idx1], 2);
		}

		variable2 = 0.5 * variable2 / input3;

		return constant + variable1 + variable2;
	}
	
	public double[] getTimeSeries()
	{
		return timeseries;
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
