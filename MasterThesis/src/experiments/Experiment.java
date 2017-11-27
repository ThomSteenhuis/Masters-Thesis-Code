package experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import input.Data;
import models.ANN;
import models.ARIMA;
import models.ExponentialSmoothing;
import models.Model;
import optimization.Genetic;
import optimization.GridSearch;
import optimization.Optimization;
import performance.PerformanceMeasures;

public class Experiment {

	private static final int seed = 34304903;
	private ArrayList<Optimization> instances;
	
	private ArrayList<String> machines;
	
	private boolean[] success;
	private double[] runTimes;
	private String[][] outcomes;
	private String[][][] forecasts;
	
	public Experiment(String machineFile)
	{
		instances = new ArrayList<Optimization>();
		
		try{
			machines = main.Run.readFile(machineFile);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Experiment(Data data,String expFile,String machineFile)
	{
		instances = new ArrayList<Optimization>();
		
		try{
			Scanner s1 = new Scanner(new File(expFile) );
			ArrayList<String[]> experiments = new ArrayList<String[]>();
			
			while(s1.hasNextLine())
			{
				String line = s1.nextLine();
				String[] splittedLine = line.split("\t");
				
				if(splittedLine.length < 4)
				{
					System.out.println("Error (Experiment): fewer than 4 arguments");
					continue;
				}
				
				experiments.add(splittedLine);
			}
			
			s1.close();
			
			machines = main.Run.readFile(machineFile);
			
			for(int idx1=0;idx1<experiments.size();++idx1)
			{
				Model model;
				ArrayList<String> categories = getCategories(experiments.get(idx1)[1]);
				
				for(String idx2:categories)
				{
					if(isExponentialSmoothing(experiments.get(idx1)[0]))
						model = initializeES(experiments.get(idx1),idx2,data);
					else if(isARIMA(experiments.get(idx1)[0]))
						model = initializeARIMA(experiments.get(idx1),idx2,data);
					else if(isANN(experiments.get(idx1)[0]))
						model = initializeANN(experiments.get(idx1),idx2,data);
					else
					{
						System.out.println("Error (Experiment): model not recognized");
						continue;
					}

					PerformanceMeasures pm = new PerformanceMeasures(model);
					
					if(experiments.get(idx1)[2].equals("GridSearch") )
					{	
						Optimization opt = initializeGS(experiments.get(idx1),pm);
						instances.add(opt);
					}
					else if(experiments.get(idx1)[2].equals("Genetic") )
					{
						Optimization opt = initializeGA(experiments.get(idx1),pm);
						instances.add(opt);
					}
					else
					{
						System.out.println("Error (Experiment): optimization algorithm not recognized");
						continue;
					}
				}
			}
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
	
	public void run(boolean silent,PrintWriter p1,PrintWriter p2)
	{
		runTimes = new double[instances.size()];
		success = new boolean[instances.size()];
		
		for(int idx=0;idx<instances.size();++idx)
		{
			long startTime = System.currentTimeMillis();

			success[idx] = instances.get(idx).optimize(false);
				
			long stopTime = System.currentTimeMillis();
			
			runTimes[idx] = (double) (stopTime - startTime) / 1000;
			
			if(success[idx])
			{
				instances.get(idx).getPerformanceMeasures().getModel().setParameters(instances.get(idx).getOptimalParameters());
				instances.get(idx).getPerformanceMeasures().getModel().train();
				instances.get(idx).getPerformanceMeasures().calculateMeasures("testing");
				createOutcomes(idx);
				createForecasts(idx);
				writeOutcome(p1);
				writeForecast(idx,p2);
			}

			if(!silent)
				System.out.printf("Completed experiment %d of %d\n",idx+1,instances.size());
		}
		

	}
	
	public void writeOutcome(PrintWriter p)
	{
		for(int idx1=0;idx1<outcomes.length;++idx1)
		{
			for(int idx2=0;idx2<outcomes[idx1].length;++idx2)
			{
				p.printf("%s\t",outcomes[idx1][idx2]);
			}
			p.println();
		}
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
	
	public void writeForecast(int index,PrintWriter p)
	{
		if(success[index])
		{
			p.printf("Instance %d was successful\n",index);
			
			for(int idx2=0;idx2<forecasts[0].length;++idx2)
			{
				for(int idx3=0;idx3<forecasts[0][idx2].length;++idx3)
				{
					p.printf("%s\t",forecasts[0][idx2][idx3]);
				}
				p.println();
			}
		}
		else
		{
			p.printf("Instance %d was not successful\n",index);
		}
		
	}
	
	public void writeForecasts(PrintWriter p)
	{
		for(int idx1=0;idx1<forecasts.length;++idx1)
		{
			if(success[idx1])
			{
				p.printf("Instance %d was successful\n",idx1);
				
				for(int idx2=0;idx2<forecasts[idx1].length;++idx2)
				{
					for(int idx3=0;idx3<forecasts[idx1][idx2].length;++idx3)
					{
						p.printf("%s\t",forecasts[idx1][idx2][idx3]);
					}
					p.println();
				}
			}
			else
			{
				p.printf("Instance %d was not successful\n",idx1);
			}
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
	
	private static Model initializeES(String[] line,String cat,Data data) throws NumberFormatException
	{		
		ExponentialSmoothing es = new ExponentialSmoothing(line[0],Integer.parseInt(line[3]),data);
		es.setCategory(cat);
		
		if(line[0].contains("TES") || line[0].equals("four") )
		{
			double[] constants = {Double.parseDouble(line[4])};
			es.setConstants(constants);
		}
			
		return es;
	}
	
	private static Model initializeARIMA(String[] line,String cat,Data data) throws NumberFormatException
	{		
		ARIMA arma = new ARIMA(data,Integer.parseInt(line[3]),seed);
		arma.setCategory(cat);
			
		return arma;
	}
	
	private static Model initializeANN(String[] line,String cat,Data data) throws NumberFormatException
	{		
		ANN ann = new ANN(data,Integer.parseInt(line[3]));
		ann.setCategory(cat);
			
		double[] constants = {Double.parseDouble(line[4]),Double.parseDouble(line[5])};
		ann.setConstants(constants);
		
		return ann;
	}
	
	private static Optimization initializeGA(String[] line,PerformanceMeasures pm) throws FileNotFoundException,NumberFormatException
	{
		if( ( ( (line.length-4-pm.getModel().getNoConstants()) % 5) != 0) || ( ( (line.length-4-pm.getModel().getNoConstants()) / 5) != pm.getModel().getNoParameters() ) )
			throw new FileNotFoundException("number of inputs not correct");
		
		double[][] parBounds = new double[pm.getModel().getNoParameters()][2];
		boolean[] expSteps = new boolean[pm.getModel().getNoParameters()];
		boolean[] integer = new boolean[pm.getModel().getNoParameters()];
		double[] expBase = new double[pm.getModel().getNoParameters()];
		
		for(int idx=0;idx<pm.getModel().getNoParameters();++idx)
		{
			parBounds[idx][0] = Double.parseDouble(line[4+5*idx+pm.getModel().getNoConstants()]);
			parBounds[idx][1] = Double.parseDouble(line[4+5*idx+pm.getModel().getNoConstants()+1]);
			
			if(line[4+5*idx+pm.getModel().getNoConstants()+2].equals("integer"))
				integer[idx] = true;
			else
				integer[idx] = false;
			
			if(line[4+5*idx+pm.getModel().getNoConstants()+3].equals("exponential"))
				expSteps[idx] = true;
			else
				expSteps[idx] = false;
			
			expBase[idx] = Double.parseDouble(line[4+5*idx+pm.getModel().getNoConstants()+4]);
		}
		
		return new Genetic(pm,parBounds,integer,expSteps,expBase);
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
			case "mDESd": return true;
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
	
	private static boolean isANN(String modelName)
	{
		if(modelName.equals("ANN"))
			return true;
		else
			return false;
	}
	
	private ArrayList<String> getCategories(String input)
	{
		if(input.equals("all"))
			return machines;
		else
		{
			ArrayList<String> output = new ArrayList<String>();
			output.add(input);
			return output;
		}
	}
	
	private void createOutcomes(int index)
	{
		outcomes = new String[instances.size()+1][];
		outcomes[0] = new String[10];
		outcomes[0][0] = "Model name";
		outcomes[0][1] = "Machine name";
		outcomes[0][2] = "Optimization name";
		outcomes[0][3] = "No periods ahead";
		outcomes[0][4] = "Runtime (seconds)";
		outcomes[0][5] = "RMSE";
		outcomes[0][6] = "MAPE";
		outcomes[0][7] = "MAE";
		outcomes[0][8] = "ME";
		outcomes[0][9] = "Best parameters";
		
		int noPars = instances.get(index).getPerformanceMeasures().getModel().getNoParameters();
		outcomes[index+1] = new String[9+noPars];
		outcomes[index+1][0] = instances.get(index).getPerformanceMeasures().getModel().getName();
		outcomes[index+1][1] = instances.get(index).getPerformanceMeasures().getModel().getCategory();
		outcomes[index+1][2] = instances.get(index).getName();
		outcomes[+1][3] = Integer.toString(instances.get(index).getPerformanceMeasures().getModel().getNoPeriodsAhead());
		
		if(success[index])
		{
			outcomes[index+1][4] = Double.toString(runTimes[index]);
			outcomes[index+1][5] = Double.toString(instances.get(index).getPerformanceMeasures().getRMSE());
			outcomes[index+1][6] = Double.toString(instances.get(index).getPerformanceMeasures().getMAPE());
			outcomes[index+1][7] = Double.toString(instances.get(index).getPerformanceMeasures().getMAE());
			outcomes[index+1][8] = Double.toString(instances.get(index).getPerformanceMeasures().getME());
			
			for(int idx2=0;idx2<noPars;++idx2)
			{
				outcomes[index+1][9+idx2] = Double.toString(instances.get(index).getOptimalParameters()[idx2]);
			}
		}
		else
		{
			outcomes[index+1][4] = "Instance failed";
		}
	}
	
	private void createOutcomes()
	{
		outcomes = new String[instances.size()+1][];
		outcomes[0] = new String[10];
		outcomes[0][0] = "Model name";
		outcomes[0][1] = "Machine name";
		outcomes[0][2] = "Optimization name";
		outcomes[0][3] = "No periods ahead";
		outcomes[0][4] = "Runtime (seconds)";
		outcomes[0][5] = "RMSE";
		outcomes[0][6] = "MAPE";
		outcomes[0][7] = "MAE";
		outcomes[0][8] = "ME";
		outcomes[0][9] = "Best parameters";
		
		for(int idx1=0;idx1<instances.size();++idx1)
		{
			int noPars = instances.get(idx1).getPerformanceMeasures().getModel().getNoParameters();
			outcomes[idx1+1] = new String[9+noPars];
			outcomes[idx1+1][0] = instances.get(idx1).getPerformanceMeasures().getModel().getName();
			outcomes[idx1+1][1] = instances.get(idx1).getPerformanceMeasures().getModel().getCategory();
			outcomes[idx1+1][2] = instances.get(idx1).getName();
			outcomes[idx1+1][3] = Integer.toString(instances.get(idx1).getPerformanceMeasures().getModel().getNoPeriodsAhead());
			
			if(success[idx1])
			{
				outcomes[idx1+1][4] = Double.toString(runTimes[idx1]);
				outcomes[idx1+1][5] = Double.toString(instances.get(idx1).getPerformanceMeasures().getRMSE());
				outcomes[idx1+1][6] = Double.toString(instances.get(idx1).getPerformanceMeasures().getMAPE());
				outcomes[idx1+1][7] = Double.toString(instances.get(idx1).getPerformanceMeasures().getMAE());
				outcomes[idx1+1][8] = Double.toString(instances.get(idx1).getPerformanceMeasures().getME());
				
				for(int idx2=0;idx2<noPars;++idx2)
				{
					outcomes[idx1+1][9+idx2] = Double.toString(instances.get(idx1).getOptimalParameters()[idx2]);
				}
			}
			else
			{
				outcomes[idx1+1][4] = "Instance failed";
			}
		}
	}
	
	
	private void createForecasts(int index)
	{
		forecasts = new String[1][9][];
		
		if(success[index])
		{
			int trainingLength = instances.get(index).getPerformanceMeasures().getModel().getTrainingReal().length;
			forecasts[0][0] = new String[trainingLength];
			forecasts[0][1] = new String[trainingLength];
			forecasts[0][2] = new String[trainingLength];
			
			for(int idx2=0;idx2<trainingLength;idx2++)
			{
				forecasts[0][0][idx2] = instances.get(index).getPerformanceMeasures().getModel().getTrainingDates()[idx2];
				forecasts[0][1][idx2] = Double.toString(instances.get(index).getPerformanceMeasures().getModel().getTrainingReal()[idx2]);
				forecasts[0][2][idx2] = Double.toString(instances.get(index).getPerformanceMeasures().getModel().getTrainingForecast()[idx2]);
			}
			
			int validationLength = instances.get(index).getPerformanceMeasures().getModel().getValidationReal().length;
			forecasts[0][3] = new String[validationLength];
			forecasts[0][4] = new String[validationLength];
			forecasts[0][5] = new String[validationLength];
			
			for(int idx2=0;idx2<validationLength;idx2++)
			{
				forecasts[0][3][idx2] = instances.get(index).getPerformanceMeasures().getModel().getValidationDates()[idx2];
				forecasts[0][4][idx2] = Double.toString(instances.get(index).getPerformanceMeasures().getModel().getValidationReal()[idx2]);
				forecasts[0][5][idx2] = Double.toString(instances.get(index).getPerformanceMeasures().getModel().getValidationForecast()[idx2]);
			}
			
			int testingLength = instances.get(index).getPerformanceMeasures().getModel().getTestingReal().length;
			forecasts[0][6] = new String[testingLength];
			forecasts[0][7] = new String[testingLength];
			forecasts[0][8] = new String[testingLength];
			
			for(int idx2=0;idx2<testingLength;idx2++)
			{
				forecasts[0][6][idx2] = instances.get(index).getPerformanceMeasures().getModel().getTestingDates()[idx2];
				forecasts[0][7][idx2] = Double.toString(instances.get(index).getPerformanceMeasures().getModel().getTestingReal()[idx2]);
				forecasts[0][8][idx2] = Double.toString(instances.get(index).getPerformanceMeasures().getModel().getTestingForecast()[idx2]);
			}
		}	
	}
	
	private void createForecasts()
	{
		forecasts = new String[instances.size()][9][];
		
		for(int idx1=0;idx1<instances.size();idx1++)
		{
			if(success[idx1])
			{
				int trainingLength = instances.get(idx1).getPerformanceMeasures().getModel().getTrainingReal().length;
				forecasts[idx1][0] = new String[trainingLength];
				forecasts[idx1][1] = new String[trainingLength];
				forecasts[idx1][2] = new String[trainingLength];
				
				for(int idx2=0;idx2<trainingLength;idx2++)
				{
					forecasts[idx1][0][idx2] = instances.get(idx1).getPerformanceMeasures().getModel().getTrainingDates()[idx2];
					forecasts[idx1][1][idx2] = Double.toString(instances.get(idx1).getPerformanceMeasures().getModel().getTrainingReal()[idx2]);
					forecasts[idx1][2][idx2] = Double.toString(instances.get(idx1).getPerformanceMeasures().getModel().getTrainingForecast()[idx2]);
				}
				
				int validationLength = instances.get(idx1).getPerformanceMeasures().getModel().getValidationReal().length;
				forecasts[idx1][3] = new String[validationLength];
				forecasts[idx1][4] = new String[validationLength];
				forecasts[idx1][5] = new String[validationLength];
				
				for(int idx2=0;idx2<validationLength;idx2++)
				{
					forecasts[idx1][3][idx2] = instances.get(idx1).getPerformanceMeasures().getModel().getValidationDates()[idx2];
					forecasts[idx1][4][idx2] = Double.toString(instances.get(idx1).getPerformanceMeasures().getModel().getValidationReal()[idx2]);
					forecasts[idx1][5][idx2] = Double.toString(instances.get(idx1).getPerformanceMeasures().getModel().getValidationForecast()[idx2]);
				}
				
				int testingLength = instances.get(idx1).getPerformanceMeasures().getModel().getTestingReal().length;
				forecasts[idx1][6] = new String[testingLength];
				forecasts[idx1][7] = new String[testingLength];
				forecasts[idx1][8] = new String[testingLength];
				
				for(int idx2=0;idx2<testingLength;idx2++)
				{
					forecasts[idx1][6][idx2] = instances.get(idx1).getPerformanceMeasures().getModel().getTestingDates()[idx2];
					forecasts[idx1][7][idx2] = Double.toString(instances.get(idx1).getPerformanceMeasures().getModel().getTestingReal()[idx2]);
					forecasts[idx1][8][idx2] = Double.toString(instances.get(idx1).getPerformanceMeasures().getModel().getTestingForecast()[idx2]);
				}
			}			
		}
	}
}
