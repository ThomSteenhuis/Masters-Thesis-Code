package main;
import models.ExponentialSmoothing;
import models.Performance;

public class Run {

	public static void main(String[] args) 
	{
		input.Run.main(args);

		double[][] inverseData = inverse(input.Run.volumes);
		
		double[][] SESbounds = new double[1][2];
		SESbounds[0][0] = 0.01;
		SESbounds[0][1] = 1;
		
		double[] td = new double[(int) (0.7*inverseData[0].length)];
		double[] vd = new double[(int) (0.2*inverseData[0].length)];
		
		for(int idx=0;idx<td.length;++idx)
			td[idx] = inverseData[0][idx];
		
		for(int idx=0;idx<vd.length;++idx)
			vd[idx] = inverseData[0][td.length+idx];
		
		optimization.Initialize.initializeOptimization("SES","Grid Search",SESbounds,1,0,td,vd);
		double[] SESestimate = optimization.Initialize.estVals;
		/*
		double[] DESestimate = ExponentialSmoothing.trainDES(0.1,0.1,1, inverseData[0]);
		double[] TESestimate1 = ExponentialSmoothing.trainTES("additive", 0.1, 0.1, 0.1, 12, 1, inverseData[0]);
		double[] TESestimate2 = ExponentialSmoothing.trainTES("multiplicative", 0.1, 0.1, 0.1, 12, 1, inverseData[0]);
		*/
		double[][] allData = mergeColVectors(vd,SESestimate);
		/*
		allData = mergeColVectors(allData,DESestimate);
		allData = mergeColVectors(allData,TESestimate1);
		allData = mergeColVectors(allData,TESestimate2);
		
		String[] mode = new String[1];
		mode[0] = "pivot";
		
		String[] header = new String[5];
		header[0] = "Real values";
		header[1] = "Estimated by SES";
		header[2] = "Estimated by Holt additive";
		header[3] = "Estimated by Holt-Winters additive";
		header[4] = "Estimated by Holt-Winters multiplicative";
		
		double[] RMSE = new double[4];
		double[] MAPE = new double[4];
		double[] MAE = new double[4];
		double[] ME = new double[4];
		String[] methods = new String[4];
		
		methods[0] = "SES";
		methods[1] = "Holt additive";
		methods[2] = "Holt-Winters additive";
		methods[3] = "Holt-Winters multiplicative";
		
		double[][] inverseAllData = inverse(allData);
		
		for(int idx=0;idx<4;++idx)
		{
			RMSE[idx] = Performance.calculateRMSE(inverseAllData[0],inverseAllData[idx+1]);
			MAPE[idx] = Performance.calculateMAPE(inverseAllData[0],inverseAllData[idx+1]);
			MAE[idx] = Performance.calculateMAE(inverseAllData[0],inverseAllData[idx+1]);
			ME[idx] = Performance.calculateME(inverseAllData[0],inverseAllData[idx+1]);
		}
		
		Performance.printMeasures(methods,RMSE,MAPE,MAE,ME);
		
		graph.Plot.initialize(mode, allData, input.Run.dates, header, input.Run.labels);*/
	}

	private static double[][] inverse(double[][] input)
	{
		if(input.length == 0)
		{
			inverseError("input is of length 0");
			return null;
		}
		
		int noLines = input.length;
		int noColumns = input[0].length;
		
		double[][] output = new double[noColumns][noLines];
		
		for(int idx1=0;idx1<noLines;++idx1)
		{
			if(input[idx1].length != noColumns)
			{
				inverseError("input does not have the same no of columns everywhere");
				return null;
			}
			
			for(int idx2=0;idx2<noColumns;++idx2)
				output[idx2][idx1] = input[idx1][idx2];
		}
		
		return output;
	}
	
	private static void inverseError(String txt)
	{
		System.out.println("Error (inverse): "+ txt);
	}
	
	private static double[][] mergeColVectors(double[] vec1, double[] vec2)
	{
		if(vec1.length != vec2.length)
		{
			System.out.println("Error (mergeColVectors): vectors not of the same length");
		}
		
		int noLines = vec1.length;
		double[][] output = new double[noLines][2];
		
		for(int idx=0;idx<noLines;++idx)
		{
			output[idx][0] = vec1[idx];
			output[idx][1] = vec2[idx];
		}
		
		return output;
	}
	
	private static double[][] mergeColVectors(double[][] mat1, double[] vec2)
	{
		if( (mat1.length != vec2.length) && (mat1.length != 0) )
		{
			System.out.println("Error (mergeColVectors): vectors not of the same length or of length 0");
			return null;
		}
		
		int noLines = mat1.length;
		int noCols = mat1[0].length;
		double[][] output = new double[noLines][noCols+1];
		
		for(int idx1=0;idx1<noLines;++idx1)
		{
			for(int idx2=0;idx2<noCols;++idx2)
				output[idx1][idx2] = mat1[idx1][idx2];
			
			output[idx1][noCols] = vec2[idx1];
		}
		
		return output;
	}
	
	private static void print(double[] estimates, double[] real)
	{
		if(estimates.length != real.length)
		{
			System.out.println("Error (print): vectors not of the same length");
		}
		
		System.out.print("Estimates:\tReal");
		
		for(int idx=0;idx<real.length;++idx)
		{
			System.out.printf("%.2f\t%.2f\n",estimates[idx],real[idx]);
		}
	}
	
	private static void print(double[] values)
	{
		for(int idx=0;idx<values.length;++idx)
		{
			System.out.printf("%.2f\n",values[idx]);
		}
	}
}
