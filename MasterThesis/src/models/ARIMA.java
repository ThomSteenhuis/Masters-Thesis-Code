package models;

import input.Data;

public class ARIMA extends Model {
	
	public ARIMA(Data data,int pers)
	{
		super(data,pers);
		noParameters = 2;
		noConstants = 1;
		name = "ARIMA";
		
		double[] constant = new double[1];
		constant[0] = determineNoDifferences();
	}

	public void train() 
	{
		
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void test() {
		// TODO Auto-generated method stub

	}
	
	private int determineNoDifferences()
	{
		return 0;
	}

}
