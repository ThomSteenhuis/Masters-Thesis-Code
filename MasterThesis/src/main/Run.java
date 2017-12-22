package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import experiments.Experiment;
import input.Data;
import math.GatingErrorFunction;
import math.Matrix;
import models.ANN;
import models.ARIMA;
import models.ExponentialSmoothing;
import models.Gating;
import models.Naive;
import models.SVR;
import optimization.Genetic;
import optimization.GridSearch;
import performance.PerformanceMeasures;

public class Run {

	private static final double propTraining = 0.6;
	private static final double propValidation = 0.2;

	private static int seed;
	private static String dataLocation;
	private static String outcomeLocation;
	private static String forecastLocation;
	private static String experimentLocation;
	private static String machineLocation;

	public static void main(String[] args)
	{
		if(!argumentStore(args)) return;

		Data data = new Data(dataLocation);
		data.setDataIndices(propTraining, propValidation);
		//data.plot();

	/*	int[] p = {1};
		String[] c = {"2200EVO"};
		Gating g = new Gating(data,p,c,seed);
		
		double[] consts = {3,0,0};
		g.setConstants(consts);
		
		g.train();
		g.plotForecast("testing",0);		
		
	*/
		
		Experiment e = new Experiment(data,seed,experimentLocation,machineLocation);

		try
		{
			PrintWriter p1 = new PrintWriter(outcomeLocation);
			PrintWriter p2 = new PrintWriter(forecastLocation);
			e.run(false,p1,p2);
			p1.close();
			p2.close();
			e.getInstances().get(0).getPerformanceMeasures().getModel().plotForecast("testing",0);
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}

		/*double[] x = new double[4];
		x[0] = test.standardize(data.getVolumes()[60][0],0);
		x[1] = test.standardize(data.getVolumes()[49][0],0);
		x[2] = test.standardize(data.getVolumes()[60][1],1);
		x[3] = test.standardize(data.getVolumes()[49][1],1);
		Matrix.print(x);
		double[] y_hat = test.predict(x);
		for(int idx=0;idx<y_hat.length;++idx) y_hat[idx] = test.destandardize(y_hat[idx],idx/3);
		Matrix.print(y_hat);
		System.out.printf("%f\t%f\t%f\n",test.getTestingForecast()[0][61-data.getTestingFirstIndex()[0]],test.getTestingForecast()[1][63-data.getTestingFirstIndex()[0]],test.getTestingForecast()[2][66-data.getTestingFirstIndex()[0]]);
		*/
		/*ExponentialSmoothing des = new ExponentialSmoothing("mTESd",1,data);
		des.setCategory("2200EVO");
		double[] cons = {12};
		des.setConstants(cons);
		PerformanceMeasures pm = new PerformanceMeasures(des);
		double[][] bounds = {{0,1},{0,1},{0,1},{0,1}};
		boolean[] type = {false,false,false,false};
		boolean[] exp = {false,false,false,false};
		double[] base = {2,2,2,2};
		Genetic g = new Genetic(pm,bounds,type,exp,base);
		if(g.optimize(false))
		{
			Matrix.print(g.getOptimalParameters());
			des.setParameters(g.getOptimalParameters());
			des.train();
			pm.calculateMeasures("validation");
			pm.printMeasures();
		}

		ExponentialSmoothing des2 = new ExponentialSmoothing("mTESd",1,data);
		des2.setCategory("2200EVO");
		des2.setConstants(cons);
		PerformanceMeasures pm2 = new PerformanceMeasures(des2);
		double[][] bounds2 = {{0,1},{0,1},{0,1},{0,1}};
		boolean[] type2 = {false,false,false,false};
		double[] expb = {2,2,2,2};
		int[] stps = {40,40,40,40};
		GridSearch g2 = new GridSearch(pm2,bounds2,type2,expb,stps);
		if(g2.optimize(false))
		{
			Matrix.print(g2.getOptimalParameters());
			des2.setParameters(g2.getOptimalParameters());
			des2.train();
			pm2.calculateMeasures("validation");
			pm2.printMeasures();
			des2.plotForecast("validation");
		}*/

		/*ARIMA arma = new ARIMA(data,1,seed);
		arma.setCategory("2200EVO");
		double[] pars = {2,2};
		arma.setParameters(pars);
		arma.train();
		PerformanceMeasures pm = new PerformanceMeasures(arma);
		pm.calculateMeasures("training");
		pm.printMeasures();
		Matrix.print(arma.getCoefficients());
		System.out.println(arma.getConstants()[0]);
		System.out.println(arma.getLogLikelihood());
		System.out.println(arma.getAIC());
		Matrix.print(arma.getTrainingForecast());	*/

		/*ExponentialSmoothing ses = new ExponentialSmoothing("SES",1,data);
		ses.setCategory("2200EVO");
		double[] pars = new double[1];
		pars[0] = 0.1;
		ses.setParameters(pars);
		ses.train();
		Matrix.print(ses.getValidationDates());
		Matrix.print(ses.getValidationReal());
		Matrix.print(ses.getValidationForecast());
		ses.plotForecast("testing");*/



		/*System.out.println(data.getTrainingFirstIndex()[data.getIndexFromCat("2200EVO")]);
		System.out.println(data.getValidationFirstIndex()[data.getIndexFromCat("2200EVO")]);
		ANN ann = new ANN(data,1);
		ann.setCategory("2200EVO");
		double[] cons = {0.00001,0.95};
		ann.setConstants(cons);

		int[] steps = {1,1,1,1};
		double[][] bounds = {{1,3},{1,10},{0.001,0.003},{1,50}};
		boolean[] exp = {false,false,false,false};
		double[] expBase = {2,2,2,2};

		PerformanceMeasures pm = new PerformanceMeasures(ann);
		GridSearch gs = new GridSearch(pm,bounds,exp,expBase,steps);
		gs.optimize(false);
		gs.printBest();
		ann.setParameters(gs.getOptimalParameters());
		ann.plotForecast("validation");*/

	/*	ann.noTrainingEpochs = (int)ann.getParameters()[3];
		ann.alr = ann.getParameters()[2];

		ann.X = new double[3][2];
		ann.X[0][0] = 1;
		ann.X[1][0] = 1;
		ann.X[2][0] = 1;
		ann.X[0][1] = 2;
		ann.X[1][1] = 2;
		ann.X[2][1] = 2;
		ann.Y = new double[2];
		ann.Y[0] = 1;
		ann.Y[1] = 2;
		ann.N = 2;

		Matrix.print(ann.Y);
		Matrix.print(ann.X);

		ann.initializeWeights();

		Matrix.print(ann.getLowerWeights());
		Matrix.print(ann.getLowerBias());
		Matrix.print(ann.getUpperWeights());
		System.out.println(ann.getUpperBias());

		ann.train();

		Matrix.print(ann.getWTX());
		Matrix.print(ann.getZ());
		Matrix.print(ann.getLowerWeights());
		Matrix.print(ann.getLowerBias());
		Matrix.print(ann.getUpperWeights());
		System.out.println(ann.getUpperBias());*/


		/*double[][] gsbounds = {{0,3},{0,3}};
		boolean[] exp = {false,false};
		double[] expbase = {2,2};
		int[] nosteps = {3,3};
		GridSearch gs = new GridSearch(pm,gsbounds,exp,expbase,nosteps);
		gs.optimize(false);
		gs.printBest();
		arma.plotForecast("training");
		pm.printMeasures();*/

		/*double[] timeseries = new double[data.getNoObs()-data.getTrainingFirstIndex()[data.getIndexFromCat("TCB & Chameo")]];

		for(int idx=0;idx<timeseries.length;++idx)
			timeseries[idx] = data.getVolumes()[data.getTrainingFirstIndex()[data.getIndexFromCat("TCB & Chameo")]+idx][data.getIndexFromCat("TCB & Chameo")];

		LLARMAFunction ll = new LLARMAFunction(timeseries,1,1);
		double[] vec = {3.244834,-0.830291,1.578291,4.179848};
		System.out.println(ll.evaluate(vec));
		Matrix.print(ll.derivative(vec));*/

		/*SVR testModel = new SVR(data,1);
		PerformanceMeasures pm = new PerformanceMeasures(testModel);
		testModel.setCategory("2200EVO");
		double[][] SVRBounds = {{-10,10},{0.1,50},{-10,10},{1,3}};
		boolean[] SVRExp = {true,false,true,false};
		double[] SVRExpBase = {2,2,2,2};
		int[] SVRsteps = {2,5,2,2};
		GridSearch gs = new GridSearch(pm,SVRBounds,SVRExp,SVRExpBase,SVRsteps);
		gs.optimize(false);
		gs.printBest();
		testModel.setParameters(gs.getOptimalParameters());
		testModel.train();
		pm.calculateMeasures();
		pm.printMeasures();
		testModel.plotForecast("validation");*/

		/*ExponentialSmoothing testModel = new ExponentialSmoothing("TES",false,true,1,data);
		testModel.setCategory("2200EVO");
		double[] cons = {12};
		testModel.setConstants(cons);
		PerformanceMeasures pm = new PerformanceMeasures(testModel);
		double[][] SESbounds = {{0.01,1},{0.01,1},{0.01,1},{0.01,1}};
		boolean[] SESExp = {false,false,false,false};
		double[] SESExpbase = {2,2,2,2};
		int[] SESnoSteps = {40,40,40,40};
		GridSearch gs = new GridSearch(pm,SESbounds,SESExp,SESExpbase,SESnoSteps);
		Experiment ex = new Experiment();
		ex.addInstance(gs);
		ex.run(false);
		Matrix.print(ex.getRunTimes());*/

		/*gs.printBest();
		testModel.setParameters(gs.getOptimalParameters());
		testModel.train();
		pm.calculateMeasures();
		pm.printMeasures();
		testModel.plotForecast("testing");*/


		/*double[] pars = {0.01,0.21,0.01,0.01};
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

	public static ArrayList<String> readFile(String location) throws FileNotFoundException
	{
		Scanner s = new Scanner(new File(location));

		ArrayList<String> output = new ArrayList<String>();


		while(s.hasNextLine())
			output.add(s.nextLine().trim());

		s.close();

		return output;
	}

	private static boolean argumentStore(String[] args)
	{
		if(!argumentCheck(args)) {System.out.println("Error: illegal input"); return false;}

		seed = Integer.parseInt(args[0]);
		dataLocation = args[1];
		outcomeLocation = args[2];
		forecastLocation = args[3];
		experimentLocation = args[4];
		machineLocation = args[5];

		return true;
	}

	private static boolean argumentCheck(String[] args)
	{
		if(args.length != 6) return false;

		try{Integer.parseInt(args[0]);if(Integer.parseInt(args[0])<1) return false;}
		catch(NumberFormatException e) {return false;}

		return true;
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
