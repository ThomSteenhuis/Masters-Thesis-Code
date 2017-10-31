package models;

public class Performance {
	public static double validate(int pm,double[] realData, double[] estimates)
	{
		switch(pm){
		case 0:{
			return calculateRMSE(realData,estimates);
		}
		case 1:{
			return calculateMAPE(realData,estimates);
		}
		case 2:{
			return calculateMAE(realData,estimates);
		}
		case 3:{
			return calculateME(realData,estimates);
		}
		default:{
			System.out.println("Error (validate): default case reached");
			return -1;
		}
		}
	}
	
	private static double calculateRMSE(double[] realData, double[] estimates)
	{
		int N = realData.length;
		
		if( (N != estimates.length) || (N==0) )
		{
			System.out.println("Error (calculateRSME): vectors have unequal length or N=0");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<N;++idx)
		{
			output += Math.pow(estimates[idx]-realData[idx],2);
		}
		
		output = output/N;
		output = Math.sqrt(output);
		
		return output;
	}
	
	private static double calculateMAPE(double[] realData, double[] estimates)
	{
		int N = realData.length;
		
		if( (N != estimates.length) || (N==0) )
		{
			System.out.println("Error (calculateMAPE): vectors have unequal length or N=0");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<N;++idx)
		{
			if(realData[idx] > 0)
				output += Math.abs(estimates[idx]-realData[idx])/realData[idx];
			else
			{
				System.out.println("Error (calculateMAPE): division by zero");
				return 0;
			}
		}
		
		output = 100*output/N;
		
		return output;
	}
	
	private static double calculateMAE(double[] realData, double[] estimates)
	{
		int N = realData.length;
		
		if( (N != estimates.length) || (N==0) )
		{
			System.out.println("Error (calculateMAE): vectors have unequal length or N=0");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<N;++idx)
		{
			output += Math.abs(estimates[idx]-realData[idx]);
		}
		
		output = output/N;
		
		return output;
	}
	
	private static double calculateME(double[] realData, double[] estimates)
	{
		int N = realData.length;
		
		if( (N != estimates.length) || (N==0) )
		{
			System.out.println("Error (calculateME): vectors have unequal length or N=0");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<N;++idx)
		{
			output += (estimates[idx]-realData[idx]);
		}
		
		output = output/N;
		
		return output;
	}
	
	public static void printMeasures(String[] categories, double[] rmse, double[] mape, double[] mae, double[] me)
	{
		System.out.print("RMSE\tMAPE\tMAE\tME\tMethod\n");
		
		for(int idx=0;idx<categories.length;++idx)
		{
			System.out.printf("%.2f\t%.2f\t%.2f\t%.2f\t%s\n",rmse[idx],mape[idx],mae[idx],me[idx],categories[idx]);
		}
	}
}
