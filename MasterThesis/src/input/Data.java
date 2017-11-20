package input;

import java.util.ArrayList;
import java.util.Hashtable;

import graph.Plot;

public class Data {
	
	private String[] header;
	private String[] categories;
	private Hashtable<String,Integer> catHash; 
	private double[][] volumes;
	private String[] dates;
	private String[] labels;
	private int noObs;
	private int noCats;
	
	private boolean indicesSet;
	private int[] trainingFirstIndex;
	private int[] validationFirstIndex;
	private int[] testingFirstIndex;
	
	public Data(String location)
	{
		ArrayList<String> data = Read.readTxt(location);
		
		if(data.size() <2)
			mainError();

		header = data.get(0).split("\t");

		if(header.length < 3)
			mainError();

		noCats = header.length - 2;
		categories = new String[noCats];
		catHash = new Hashtable<String,Integer>();

		for(int idx=2;idx<header.length;++idx)
		{
			categories[idx-2] = header[idx];
			catHash.put(header[idx],idx-2);
		}

		noObs = data.size()-1;
		volumes = new double[noObs][];
		dates = new String[data.size()-1];
		String[] line = null;

		for(int idx1=1;idx1<data.size();++idx1)
		{
			line = data.get(idx1).split("\t");

			if(line.length < 3)
				mainError();

			volumes[idx1-1] = convertLine(line);

			if(volumes.equals(null))
				mainError();

			if(volumes[idx1-1].length == 0)
				mainError();

			String month = getMonth(line[0]);

			dates[idx1-1] = (month + " " + line[1]);
		}

		labels = new String[2];
		labels[0] = "Time";
		labels[1] = "Volume";
		
		indicesSet = false;
	}
	
	public void setDataIndices(double propTrain,double propVal)
	{
		if( (propTrain<0) || (propTrain>1) || (propVal<0) || (propVal>1) || ( (propTrain+propVal) > 1) )
			throw new IllegalArgumentException();
		else
		{
			trainingFirstIndex = new int[noCats];
			validationFirstIndex = new int[noCats];
			testingFirstIndex = new int[noCats];
			
			for(int idx1=0;idx1<noCats;++idx1)
			{
				int idx2 = 0;
				int firstIndex = -1;
				
				while( (volumes[idx2][idx1]==0) && (idx2<(noObs - 1) ) )
					idx2++;
				
				if(idx2 != (noObs - 1))
					firstIndex = idx2;
				
				if(firstIndex > -1)
				{
					trainingFirstIndex[idx1] = firstIndex;
					validationFirstIndex[idx1] = firstIndex + (int) (propTrain*(double)(noObs-firstIndex) );
					testingFirstIndex[idx1] = firstIndex + (int) ( (propTrain+propVal)*(double)(noObs-firstIndex) );
				}
				else
				{
					System.out.println("Error (calculateDataIndices): data contains 0 values only");
				}
			}
			
			indicesSet = true;
		}
	}
	
	public void plot()
	{
		String[] pars = new String[1];
		pars[0] = "pivot";

		Plot.initialize(pars,volumes,dates,categories,labels);
	}
	
	public void plotSplittedSet(String set,String category)
	{
		if(!indicesSet)
		{
			System.out.println("Error (plotTrainingSet): set data indices first");
			return;
		}
		
		int index = getIndexFromCat(category);
		
		String[] pars = new String[1];
		pars[0] = "pivot";
		
		String[] cats = new String[1];
		cats[0] = categories[index];
		
		switch (set)
		{
			case "training":
			{
				double[][] vols = new double[validationFirstIndex[index]-trainingFirstIndex[index]][1];
				String[] dats = new String[vols.length];
				
				for(int idx=trainingFirstIndex[index];idx<validationFirstIndex[index];++idx)
				{
					vols[idx-trainingFirstIndex[index]][0] = volumes[idx][index];
					dats[idx-trainingFirstIndex[index]] = dates[idx];
				}
				
				Plot.initialize(pars,vols,dats,cats,labels);
				break;
			}
			case "validation":
			{
				double[][] vols = new double[testingFirstIndex[index]-validationFirstIndex[index]][1];
				String[] dats = new String[vols.length];
				
				for(int idx=validationFirstIndex[index];idx<testingFirstIndex[index];++idx)
				{
					vols[idx-validationFirstIndex[index]][0] = volumes[idx][index];
					dats[idx-validationFirstIndex[index]] = dates[idx];
				}
				
				Plot.initialize(pars,vols,dats,cats,labels);
				break;
			}
			case "testing":
			{
				double[][] vols = new double[noObs-testingFirstIndex[index]][1];
				String[] dats = new String[vols.length];
				
				for(int idx=testingFirstIndex[index];idx<noObs;++idx)
				{
					vols[idx-testingFirstIndex[index]][0] = volumes[idx][index];
					dats[idx-testingFirstIndex[index]] = dates[idx];
				}
				
				Plot.initialize(pars,vols,dats,cats,labels);
				break;
			}
			default:
			{
				System.out.println("Error (plotSplittedSet): default case reached");
			}
		}
	}
	
	public double[] getTrainingSet(String cat)
	{
		int index = getIndexFromCat(cat);
		double[] output = new double[validationFirstIndex[index]-trainingFirstIndex[index]];
		
		for(int idx=0;idx<output.length;++idx)
			output[idx] = volumes[trainingFirstIndex[index]+idx][index];
		
		return output;
	}
	
	public double[] getValidationSet(String cat)
	{
		int index = getIndexFromCat(cat);
		double[] output = new double[testingFirstIndex[index]-validationFirstIndex[index]];
		
		for(int idx=0;idx<output.length;++idx)
			output[idx] = volumes[validationFirstIndex[index]+idx][index];
		
		return output;
	}
	
	public double[] getTestingSet(String cat)
	{
		int index = getIndexFromCat(cat);
		double[] output = new double[noObs-testingFirstIndex[index]-1];
		
		for(int idx=0;idx<output.length;++idx)
			output[idx] = volumes[testingFirstIndex[index]+idx][index];
		
		return output;
	}
	
	public String[] getTrainingDates(String cat)
	{
		int index = getIndexFromCat(cat);
		String[] output = new String[validationFirstIndex[index]-trainingFirstIndex[index]];
		
		for(int idx=0;idx<output.length;++idx)
			output[idx] = dates[trainingFirstIndex[index]+idx];
		
		return output;
	}
	
	public String[] getValidationDates(String cat)
	{
		int index = getIndexFromCat(cat);
		String[] output = new String[testingFirstIndex[index]-validationFirstIndex[index]];
		
		for(int idx=0;idx<output.length;++idx)
			output[idx] = dates[validationFirstIndex[index]+idx];
		
		return output;
	}
	
	public String[] getTestingDates(String cat)
	{
		int index = getIndexFromCat(cat);
		String[] output = new String[noObs-testingFirstIndex[index]-1];
		
		for(int idx=0;idx<output.length;++idx)
			output[idx] = dates[testingFirstIndex[index]+idx];
		
		return output;
	}
	
	public String[] getHeader()
	{
		return header;
	}
	
	public String[] getCategories()
	{
		return categories;
	}
	
	public double[][] getVolumes()
	{
		return volumes;
	}
	
	public String[] getDates()
	{
		return dates;
	}
	
	public String[] getLabels()
	{
		return labels;
	}
	
	public int getNoObs()
	{
		return noObs;
	}
	
	public int getNoCats()
	{
		return noCats;
	}
	
	public boolean indicesSet()
	{
		return indicesSet;
	}
	
	public int[] getTrainingFirstIndex()
	{
		return trainingFirstIndex;
	}
	
	public int[] getValidationFirstIndex()
	{
		return validationFirstIndex;
	}
	
	public int[] getTestingFirstIndex()
	{
		return testingFirstIndex;
	}
	
	private static void mainError()
	{
		System.out.println("Error (main): data not valid");
		System.exit(0);
	}
	
	private static double[] convertLine(String[] input)
	{
		double[] output = new double[input.length-2];

		for(int idx=2;idx<input.length;++idx)
		{
			try
			{
				output[idx-2] = Double.parseDouble(input[idx]);
			}
			catch(NumberFormatException e)
			{
				System.out.println("Error (convertTable): input not valid");
				return null;
			}
		}

		return output;
	}

	private static String getMonth(String monthno)
	{
		switch (monthno)
		{
		case "1":
		{
			return "jan";
		}
		case "2":
		{
			return "feb";
		}
		case "3":
		{
			return "mar";
		}
		case "4":
		{
			return "apr";
		}
		case "5":
		{
			return "may";
		}
		case "6":
		{
			return "jun";
		}
		case "7":
		{
			return "jul";
		}
		case "8":
		{
			return "aug";
		}
		case "9":
		{
			return "sep";
		}
		case "10":
		{
			return "oct";
		}
		case "11":
		{
			return "nov";
		}
		case "12":
		{
			return "dec";
		}
		}
		System.out.println("Warning (getMonth): default case reached");
		return null;
	}
	
	public int getIndexFromCat(String cat)
	{
		Integer output = catHash.get(cat);
		
		try{
			output.equals(null);
		}
		catch(NullPointerException e)
		{
			System.out.println("Error (getIndexFromCat): this category does not exist");
		}

		return (int) output;
	}

	public static void main(String[] args) 
	{
		
	}

}
