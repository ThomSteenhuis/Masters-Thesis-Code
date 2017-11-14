package experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import input.Data;
import models.ARIMA;
import models.ExponentialSmoothing;
import models.Model;
import optimization.GridSearch;
import optimization.Optimization;
import performance.PerformanceMeasures;

public class Experiment {

	private ArrayList<Optimization> instances;
	
	private double[] runTimes;
	private String[][] outcomes;
	
	public Experiment()
	{
		instances = new ArrayList<Optimization>();
	}
	
	public Experiment(Data data,String expFile)
	{
		instances = new ArrayList<Optimization>();
		
		try{
			Scanner s = new Scanner(new File(expFile) );
			
			while(s.hasNextLine())
			{
				String line = s.nextLine();
				String[] splittedLine = line.split("\t");
				Model model;
				
				if(splittedLine.length < 4)
				{
					System.out.println("Error (Experiment): fewer than 4 arguments");
					continue;
				}
				
				if(isExponentialSmoothing(splittedLine[0]))
					model = initializeES(splittedLine,data);
				else if(isARIMA(splittedLine[0]))
					model = initializeARIMA(splittedLine,data);
				else
				{
					System.out.println("Error (Experiment): model not recognized");
					continue;
				}
				
				if(splittedLine[2].equals("GridSearch") )
				{
					PerformanceMeasures pm = new PerformanceMeasures(model);
					Optimization opt = initializeGS(splittedLine,pm);
					instances.add(opt);
				}
				else
				{
					System.out.println("Error (Experiment): optimization algorithm not recognized");
					continue;
				}
			}
			
			s.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Error (Experiment): file not found or incorrect");
			e.printStackTrace();
			return;
		}
		catch(NumberFormatException e)
		{
			System.out.println("Error (Experiment): input cannot be parsed to integer");
			e.printStackTrace();
			return;
		}
	}
	
	public void addInstance(Optimization instance)
	{
		instances.add(instance);
	}
	
	public void run(boolean silent)
	{
		runTimes = new double[instances.size()];
		
		for(int idx=0;idx<instances.size();++idx)
		{
			long startTime = System.currentTimeMillis();
			
			instances.get(idx).optimize(silent);
				
			long stopTime = System.currentTimeMillis();
			
			runTimes[idx] = (double) (stopTime - startTime) / 1000;
			
			instances.get(idx).getPerformanceMeasures().getModel().setParameters(instances.get(idx).getOptimalParameters());
			instances.get(idx).getPerformanceMeasures().getModel().train();
			instances.get(idx).getPerformanceMeasures().calculateMeasures("testing");
			
			if(!silent)
				System.out.printf("Completed experiment %d of %d\n",idx+1,instances.size());
		}
		
		createOutcomes();
	}
	
	public void writeOutcomes(PrintWriter p)
	{
		for(int idx1=0;idx1<outcomes.length;++idx1)
		{
			for(int idx2=0;idx2<outcomes[idx1].length;++idx2)
			{
				p.printf("%s\t",outcomes[idx1][idx2]);
			}
			p.println();
		}
		p.close();
		
	}
	
	public ArrayList<Optimization> getInstances()
	{
		return instances;
	}
	
	public double[] getRunTimes()
	{
		return runTimes;
	}
	
	public String[][] getOutcomes()
	{
		return outcomes;
	}
	
	private static Model initializeES(String[] line,Data data) throws NumberFormatException
	{		
		ExponentialSmoothing es = new ExponentialSmoothing(line[0],Integer.parseInt(line[3]),data);
		es.setCategory(line[1]);
		
		if(line[0].contains("TES") || line[0].equals("four") )
		{
			double[] constants = new double[1];
			constants[0] = Double.parseDouble(line[4]);
			es.setConstants(constants);
		}
			
		return es;
	}
	
	private static Model initializeARIMA(String[] line,Data data) throws NumberFormatException
	{		
		ARIMA arma = new ARIMA(data,Integer.parseInt(line[3]));
		arma.setCategory(line[1]);
			
		return arma;
	}
	
	private static Optimization initializeGS(String[] line,PerformanceMeasures pm) throws FileNotFoundException,NumberFormatException
	{
		if( ( ( (line.length-4-pm.getModel().getNoConstants()) % 5) != 0) || ( ( (line.length-4-pm.getModel().getNoConstants()) / 5) != pm.getModel().getNoParameters() ) )
			throw new FileNotFoundException("number of inputs not correct");
		
		double[][] parBounds = new double[pm.getModel().getNoParameters()][2];
		boolean[] expBounds = new boolean[pm.getModel().getNoParameters()];
		double[] expBase = new double[pm.getModel().getNoParameters()];
		int[] noSteps = new int[pm.getModel().getNoParameters()];
		
		for(int idx=0;idx<pm.getModel().getNoParameters();++idx)
		{
			parBounds[idx][0] = Double.parseDouble(line[4+5*idx+pm.getModel().getNoConstants()]);
			parBounds[idx][1] = Double.parseDouble(line[4+5*idx+pm.getModel().getNoConstants()+1]);
			
			if(line[4+5*idx+pm.getModel().getNoConstants()+3].equals("exponential"))
				expBounds[idx] = true;
			else
				expBounds[idx] = false;
			
			expBase[idx] = Double.parseDouble(line[4+5*idx+pm.getModel().getNoConstants()+4]);
			noSteps[idx] = Integer.parseInt(line[4+5*idx+pm.getModel().getNoConstants()+2]);
		}
		
		return new GridSearch(pm,parBounds,expBounds,expBase,noSteps);
	}
	
	private static boolean isExponentialSmoothing(String modelName)
	{
		switch (modelName)
		{
			case "SES": return true;
			case "aDES": return true;
			case "mDES": return true;
			case "aDESd": return true;
			case "aDESm": return true;
			case "aTES": return true;
			case "mTES": return true;
			case "aTESd": return true;
			case "mTESd": return true;
			case "four": return true;
			default: return false;
		}
	}
	
	private static boolean isARIMA(String modelName)
	{
		if(modelName.equals("ARMA"))
			return true;
		else
			return false;
	}
	
	private void createOutcomes()
	{
		outcomes = new String[instances.size()+1][];
		outcomes[0] = new String[9];
		outcomes[0][0] = "Model name";
		outcomes[0][1] = "Machine name";
		outcomes[0][2] = "Optimization name";
		outcomes[0][3] = "Runtime (seconds)";
		outcomes[0][4] = "RMSE";
		outcomes[0][5] = "MAPE";
		outcomes[0][6] = "MAE";
		outcomes[0][7] = "ME";
		outcomes[0][8] = "Best parameters";
		
		for(int idx1=0;idx1<instances.size();++idx1)
		{
			int noPars = instances.get(idx1).getPerformanceMeasures().getModel().getNoParameters();
			outcomes[idx1+1] = new String[8+noPars];
			outcomes[idx1+1][0] = instances.get(idx1).getPerformanceMeasures().getModel().getName();
			outcomes[idx1+1][1] = instances.get(idx1).getPerformanceMeasures().getModel().getCategory();
			outcomes[idx1+1][2] = instances.get(idx1).getName();
			outcomes[idx1+1][3] = Double.toString(runTimes[idx1]);
			outcomes[idx1+1][4] = Double.toString(instances.get(idx1).getPerformanceMeasures().getRMSE());
			outcomes[idx1+1][5] = Double.toString(instances.get(idx1).getPerformanceMeasures().getMAPE());
			outcomes[idx1+1][6] = Double.toString(instances.get(idx1).getPerformanceMeasures().getMAE());
			outcomes[idx1+1][7] = Double.toString(instances.get(idx1).getPerformanceMeasures().getME());
			
			for(int idx2=0;idx2<noPars;++idx2)
			{
				outcomes[idx1+1][8+idx2] = Double.toString(instances.get(idx1).getOptimalParameters()[idx2]);
			}
		}
	}
}
