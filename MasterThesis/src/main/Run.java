package main;
import input.Data;
import models.ExponentialSmoothing;
import models.SVR;
import optimization.GridSearch;
import performance.PerformanceMeasures;

public class Run {
	private static final double lowerBound = 0.01;
	private static final double upperBound = 1;

	private static final double propTraining = 0.6;
	private static final double propValidation = 0.2;

	public static void main(String[] args)
	{
		Data data = new Data("src/data/prepared_data.txt");
		data.setDataIndices(propTraining, propValidation);

		SVR testModel = new SVR(data,1);
		double[] pars = {1,1,1,6};
		testModel.setParameters(pars);
		testModel.setCategory("2200EVO");
		testModel.train();
		testModel.printLambda();

		double sum = 0;
		for(int idx=0;idx<testModel.getN();++idx)
			sum += testModel.getLambda()[idx];

		System.out.println(sum);

		/*ExponentialSmoothing testModel = new ExponentialSmoothing("four",true,true,1,data);
		double[] cons = {12};
		testModel.setConstants(cons);
		PerformanceMeasures pm = new PerformanceMeasures(testModel);
		double[][] SESbounds = initBounds(4);
		GridSearch gs = new GridSearch(pm,SESbounds,40);
		gs.optimize("2200EVO");
		gs.printBest();
		testModel.setParameters(gs.getOptimalParameters());
		testModel.train();
		pm.calculateMeasures();
		pm.printMeasures();
		testModel.plotForecast("validation");


		double[] pars = {0.01,0.21,0.01,0.01};
		testModel.setParameters(pars);
		testModel.setCategory("2200EVO");
		testModel.train();
		pm.calculateMeasures();
		pm.printMeasures();*/

		/*input.Run.main(args);

		double[][] inverseData = inverse(input.Run.volumes);


		double[][] HoltAddbounds = initBounds("beta");
		double[][] HoltWintersBounds = initBounds("gamma");

		double[] td = initData("training",inverseData[0]);
		double[] vd = initData("validation",inverseData[0]);

		optimization.Initialize.initializeOptimization("SES","Grid Search",SESbounds,1,0,td,vd);
		double[] SESestimate = optimization.Initialize.estVals;
		double[] SESBestPars = optimization.Initialize.optPars;

		optimization.Initialize.initializeOptimization("Holt additive","Grid Search",HoltAddbounds,1,0,td,vd);
		double[] DESestimate = optimization.Initialize.estVals;
		double[] DESBestPars = optimization.Initialize.optPars;

		optimization.Initialize.initializeOptimization("Holt-Winters additive","Grid Search",HoltWintersBounds,1,0,td,vd);
		double[] TESestimate1 = optimization.Initialize.estVals;
		double[] TESBestPars1 = optimization.Initialize.optPars;

		optimization.Initialize.initializeOptimization("Holt-Winters multiplicative","Grid Search",HoltWintersBounds,1,0,td,vd);
		double[] TESestimate2 = optimization.Initialize.estVals;
		double[] TESBestPars2 = optimization.Initialize.optPars;

		double[][] allData = mergeColVectors(vd,SESestimate);
		allData = mergeColVectors(allData,DESestimate);
		allData = mergeColVectors(allData,TESestimate1);
		allData = mergeColVectors(allData,TESestimate2);

		String[] mode = new String[1];
		mode[0] = "pivot";

		String[] dates = new String[vd.length];

		for(int idx=0;idx<vd.length;++idx)
			dates[idx] = input.Run.dates[td.length+idx];

		String[] header = new String[5];
		header[0] = "Real values";
		header[1] = "Estimated by SES";
		header[2] = "Estimated by Holt additive";
		header[3] = "Estimated by Holt-Winters additive";
		header[4] = "Estimated by Holt-Winters multiplicative";

		String[] methods = new String[4];

		methods[0] = "SES";
		methods[1] = "Holt additive";
		methods[2] = "Holt-Winters additive";
		methods[3] = "Holt-Winters multiplicative";

		double[] RMSE = new double[4];
		double[] MAPE = new double[4];
		double[] MAE = new double[4];
		double[] ME = new double[4];
		double[][] inverseAllData = inverse(allData);

		for(int idx=0;idx<4;++idx)
		{
			RMSE[idx] = Performance.validate(0,inverseAllData[0],inverseAllData[idx+1]);
			MAPE[idx] = Performance.validate(1,inverseAllData[0],inverseAllData[idx+1]);
			MAE[idx] = Performance.validate(2,inverseAllData[0],inverseAllData[idx+1]);
			ME[idx] = Performance.validate(3,inverseAllData[0],inverseAllData[idx+1]);
		}

		printBestPars("SES",SESBestPars);
		Performance.printMeasures(methods,RMSE,MAPE,MAE,ME);

		graph.Plot.initialize(mode, allData, dates, header, input.Run.labels);*/
	}

	private static double[] initData(String set,double[] data)
	{
		switch(set)
		{
			case "training":
			{
				double[] output = new double[(int) (propTraining*data.length)];

				for(int idx=0;idx<output.length;++idx)
					output[idx] = data[idx];

				return output;
			}
			case "validation":
			{
				double[] output = new double[(int) (propValidation*data.length)];

				for(int idx=0;idx<output.length;++idx)
					output[idx] = data[(int) (propTraining*data.length) + idx];

				return output;
			}
			case "testing":
			{
				double[] output = new double[data.length - (int) (propTraining*data.length) - (int) (propValidation*data.length)];

				for(int idx=0;idx<output.length;++idx)
					output[idx] = data[(int) (propTraining*data.length) + (int) (propValidation*data.length) + idx];

				return output;
			}
			default:
			{
				System.out.println("Error (initSet): default case reached");
				return null;
			}
		}
	}

	private static double[][] initBounds(int no)
	{
		double[][] output = new double[no][2];

		for(int idx=0;idx<no;++idx)
		{
			output[idx][0] = lowerBound;
			output[idx][1] = upperBound;
		}

		return output;
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

		System.out.print("Estimates:\tReal:\n");

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
