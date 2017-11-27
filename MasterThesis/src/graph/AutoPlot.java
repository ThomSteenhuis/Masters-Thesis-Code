package graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import math.Matrix;

public class AutoPlot {
	
	private static final int[] split = {0,2};
	private static final int[] categories = {1};
	private static final int[] filter = {3};
	private static final String[] filterValue = {"GridSearch"};
	
	private static String iLoc1;
	private static String iLoc2;
	private static String oLoc;
	
	private static ArrayList<String> machines;
	private static ArrayList<String> models;
	private static ArrayList<String> periods;
	private static ArrayList<String> optimizations;
	
	private static String[] machine;
	private static String[] model;
	private static String[] optimization;
	private static int[] noPersAhead;
	private static Hashtable<String,Integer> hash;
	private static boolean[] success;
	
	private static double[][] realTrainingData;
	private static double[][] realValidationData;
	private static double[][] realTestingData;
	
	private static String[][] realTrainingDates;
	private static String[][] realValidationDates;
	private static String[][] realTestingDates;
	
	private static double[][] forecastTrainingData;
	private static double[][] forecastValidationData;
	private static double[][] forecastTestingData;
	
	private static double[][][] volumes;
	private static String[][] dates;
	private static String[][] header;
	private static String[][] labels;

	public static void main(String[] args) 
	{
		iLoc1 = args[0];
		iLoc2 = args[1];
		oLoc = args[2];
		
		try{
			machines = main.Run.readFile(args[3]);
			models = main.Run.readFile(args[4]);
			periods = main.Run.readFile(args[5]);
			optimizations = main.Run.readFile(args[6]);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}
		
		readInput();
		deriveData("testing");
		
		LineGraph lg = new LineGraph(volumes,dates,header,labels);
		lg.autoplot(oLoc);
	}
	
	private static boolean deriveData(String set)
	{
		if(!validInput())
		{
			System.out.println("Error (deriveData): invalid input");
			return false;
		}
		
		String[][] splittedList = deriveList(split,false); 
		String[][] categoriesList = deriveList(categories,true);
				
		volumes = new double[splittedList.length][][];
		dates = new String[splittedList.length][];
		header = new String[splittedList.length][categoriesList.length];
		labels = new String[splittedList.length][2];
		
		for(int idx1=0;idx1<splittedList.length;idx1++)
		{
			double[][] tmp = new double[categoriesList.length][];
			labels[idx1][0] = "Time"; labels[idx1][1] = "Volume";
			
			for(int idx2=0;idx2<categoriesList.length;idx2++)
			{
				String key = makeKey(idx1,idx2,splittedList,categoriesList);
				int index = hash.get(key);
				
				header[idx1][idx2] = "";
				for(int idx3=0;idx3<categoriesList[idx2].length;++idx3) 
					header[idx1][idx2] = header[idx1][idx2] + categoriesList[idx2][idx3];

				switch(set)
				{
				case "training":
				{
					tmp[idx2] = new double[realTrainingDates[index].length];
					
					if(idx2 == 0)
					{
						dates[idx1] = new String[realTrainingDates[index].length];
						
						for(int idx3=0;idx3<tmp[idx2].length;++idx3) 
						{
							dates[idx1][idx3] = realTrainingDates[index][idx3];
							tmp[idx2][idx3] = realTrainingData[index][idx3];
						}
					}
						
					else
						for(int idx3=0;idx3<tmp[idx2].length;++idx3) tmp[idx2][idx3] = forecastTrainingData[index][idx3];
					break;
				}
				case "validation":
				{
					tmp[idx2] = new double[realValidationDates[index].length];
					
					if(idx2 == 0)
					{
						dates[idx1] = new String[realValidationDates[index].length];
						
						for(int idx3=0;idx3<tmp[idx2].length;++idx3) 
						{
							dates[idx1][idx3] = realValidationDates[index][idx3];
							tmp[idx2][idx3] = realValidationData[index][idx3];
						}
					}						
					else
						for(int idx3=0;idx3<tmp[idx2].length;++idx3) tmp[idx2][idx3] = forecastValidationData[index][idx3];
					break;
				}
				case "testing":
				{
					tmp[idx2] = new double[realTestingDates[index].length];
					
					if(idx2 == 0)
					{
						dates[idx1] = new String[realTestingDates[index].length];
						
						for(int idx3=0;idx3<tmp[idx2].length;++idx3) 
						{
							dates[idx1][idx3] = realTestingDates[index][idx3];
							tmp[idx2][idx3] = realTestingData[index][idx3];
						}
					}
					else
						for(int idx3=0;idx3<tmp[idx2].length;++idx3) tmp[idx2][idx3] = forecastTestingData[index][idx3];
					break;
				}
				default: System.out.println("Error (deriveData): default case reached");
				}
			}
			
			volumes[idx1] = Matrix.inverse(tmp);
		}
		
		return true;
	}
	
	private static String makeAlternativeKey(int index)
	{
		ArrayList<Integer> cat = new ArrayList<Integer>();
		for(int idx=0;idx<categories.length;++idx) cat.add(categories[idx]);
		
		String output = "";
		if(cat.get(0) == 0) output = output + "Actual data"; else if(!cat.contains(0)) output = output + machine[index];
		if(cat.get(0) == 1) output = output + "Actual data"; else if(!cat.contains(1)) output = output + model[index];
		if(cat.get(0) == 2) output = output + "Actual data"; else if(!cat.contains(2)) output = output + noPersAhead[index];
		if(cat.get(0) == 3) output = output + "Actual data"; else if(!cat.contains(3)) output = output + optimization[index];

		return output;
	}
	
	private static String makeKey(int index1,int index2,String[][] list1,String[][] list2)
	{
		int[] translate = new int[4];
		
		for(int idx1=0;idx1<translate.length;++idx1)
		{
			for(int idx2:split)
				translate[idx2] = 0;
			
			for(int idx2:categories)
				translate[idx2] = 1;
			
			for(int idx2:filter)
				translate[idx2] = 2;
		}
		
		String key = "";
		
		for(int idx1=0;idx1<translate.length;++idx1)
		{
			switch(translate[idx1])
			{
			case 0: 
			{
				int lstIdx = 0;
				while(!(split[lstIdx] == idx1)) lstIdx ++;
				key = key + list1[index1][lstIdx];
				break;
			}
			case 1: 
			{
				int lstIdx = 0;
				while(!(categories[lstIdx] == idx1)) lstIdx ++;
				key = key + list2[index2][lstIdx];
				break;
			}
			case 2: 
			{
				int lstIdx = 0;
				while(filter[lstIdx] != idx1) lstIdx ++;
				key = key + filterValue[lstIdx];
				break;
			}
			default: System.out.println("Error (makeKey): default case reached");
			}
		}
		
		return key;
	}
	
	private static boolean readInput()
	{
		try{
			Scanner s1 = new Scanner(new File(iLoc1));
			Scanner s2 = new Scanner(new File(iLoc2));
			
			ArrayList<String> lines = new ArrayList<String>();
			
			while(s1.hasNextLine())
				lines.add(s1.nextLine());
			
			s1.close();
			
			model = new String[(int)(0.5*lines.size())];
			machine = new String[model.length];
			optimization = new String[model.length];
			noPersAhead = new int[model.length];
			hash = new Hashtable<String,Integer>();
			success = new boolean[model.length];
			
			for(int idx=1;idx<lines.size();idx+=2)
			{
				String[] l = lines.get(idx).split("\t");
				model[(int)(0.5*(idx-1))] = l[0].trim();
				machine[(int)(0.5*(idx-1))] = l[1].trim();
				optimization[(int)(0.5*(idx-1))] = l[2].trim();
				noPersAhead[(int)(0.5*(idx-1))] = Integer.parseInt(l[3].trim());
				hash.put(machine[(int)(0.5*(idx-1))]+model[(int)(0.5*(idx-1))]+Integer.toString(noPersAhead[(int)(0.5*(idx-1))])+optimization[(int)(0.5*(idx-1))],(int)(0.5*(idx-1)));
				hash.put(makeAlternativeKey((int)(0.5*(idx-1))),(int)(0.5*(idx-1)));
				
				if(l[4].equals("Instance failed"))
					success[(int)(0.5*(idx-1))] = false;
				else
					success[(int)(0.5*(idx-1))] = true;
			}			
			
			lines = new ArrayList<String>();
			
			while(s2.hasNextLine())
				lines.add(s2.nextLine());
			
			s2.close();
			
			realTrainingDates = new String[model.length][];
			realValidationDates = new String[model.length][];
			realTestingDates = new String[model.length][];
			realTrainingData = new double[model.length][];
			realValidationData = new double[model.length][];
			realTestingData = new double[model.length][];
			forecastTrainingData = new double[model.length][];
			forecastValidationData = new double[model.length][];
			forecastTestingData = new double[model.length][];
			
			boolean data = false;
			int instance = 0;
			
			for(int idx1=0;idx1<lines.size();++idx1)
			{
				if(instance >= model.length)
					break;
				if(data) 
				{
					for(int idx2=idx1;idx2<(idx1+9);++idx2)
					{
						String[] l = lines.get(idx2).split("\t");

						switch (idx2-idx1)
						{
						case 0: realTrainingDates[instance] = l;break;
						case 1: realTrainingData[instance] = new double[l.length]; for(int idx3=0;idx3<l.length;++idx3) realTrainingData[instance][idx3] = Double.parseDouble(l[idx3].trim());break;
						case 2: forecastTrainingData[instance] = new double[l.length]; for(int idx3=0;idx3<l.length;++idx3) forecastTrainingData[instance][idx3] = Double.parseDouble(l[idx3].trim());break;
						case 3: realValidationDates[instance] = l;break;
						case 4: realValidationData[instance] = new double[l.length]; for(int idx3=0;idx3<l.length;++idx3) realValidationData[instance][idx3] = Double.parseDouble(l[idx3].trim());break;
						case 5: forecastValidationData[instance] = new double[l.length]; for(int idx3=0;idx3<l.length;++idx3) forecastValidationData[instance][idx3] = Double.parseDouble(l[idx3].trim());break;
						case 6: realTestingDates[instance] = l;break;
						case 7: realTestingData[instance] = new double[l.length]; for(int idx3=0;idx3<l.length;++idx3) realTestingData[instance][idx3] = Double.parseDouble(l[idx3].trim());break;
						case 8: forecastTestingData[instance] = new double[l.length]; for(int idx3=0;idx3<l.length;++idx3) forecastTestingData[instance][idx3] = Double.parseDouble(l[idx3].trim());break;
						default: System.out.println("Error (readInput): default case reached");
						}
					}
					
					idx1 += 8;
					data = false;
					instance ++;
				}
				else
				{
					if(lines.get(idx1).equals("Instance "+instance+" was successful"))
						data = true;
					else
						instance ++;
				}
			}
			
			return true;
		}
		catch(IOException | NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private static String[][] deriveList(int[] input,boolean actualData)
	{
		String[][] table = new String[4][];
		int l = 1;
		
		for(int idx1:input)
		{
			ArrayList<String> list = new ArrayList<String>();
			
			switch(idx1)
			{
			case 0: for(int idx2=0;idx2<machine.length;++idx2) if(!list.contains(machine[idx2])) list.add(machine[idx2]);break;
			case 1: for(int idx2=0;idx2<model.length;++idx2) if(!list.contains(model[idx2])) list.add(model[idx2]);break;
			case 2: for(int idx2=0;idx2<noPersAhead.length;++idx2) if(!list.contains(Integer.toString(noPersAhead[idx2]))) list.add(Integer.toString(noPersAhead[idx2]));break;
			case 3: for(int idx2=0;idx2<optimization.length;++idx2) if(!list.contains(optimization[idx2])) list.add(optimization[idx2]);break;
			default:  System.out.println("Error (deriveList): default case reached");
			}
			
			table[idx1] = new String[list.size()];
			int cnt = 0;
			for(String idx2:list) {table[idx1][cnt] = idx2; cnt++;}
			l = l * cnt;
		}
		
		if(actualData) l++;
		String[][] output = new String[l][input.length];
		
		int first = 0;
		if(actualData) {output[0][0] = "Actual data"; first = 1;}
		
		for(int idx2=0;idx2<input.length;++idx2)
		{
			int perm = 1; for(int idx3=idx2+1;idx3<input.length;++idx3) perm = perm * table[input[idx3]].length;
			
			for(int idx1=first;idx1<l;++idx1)
				output[idx1][idx2] = table[input[idx2]][( (idx1-first) / perm ) % table[input[idx2]].length]; 
		}
		
		return output;
	}
	
	private static boolean validInput()
	{
		int[] count = new int[4];
		
		for(int idx:split)
		{
			if( (idx<0) || (idx>3) )
				return false;
			else
				count[idx]++;
		}
			
		for(int idx:categories)
		{
			if( (idx<0) || (idx>3) )
				return false;
			else
				count[idx]++;
		}
		
		for(int idx:filter)
		{
			if( (idx<0) || (idx>3) )
				return false;
			else
				count[idx]++;
		}
		
		for(int idx:count)
		{
			if(idx != 1)
				return false;
		}

		return validFilterValue();
	}
	
	private static boolean validFilterValue()
	{
		if(filter.length != filterValue.length)
			return false;
		
		for(int idx=0;idx<filter.length;++idx)
		{
			switch(filter[idx])
			{
			case 0: if(!machines.contains(filterValue[idx])) return false; break;
			case 1: if(!models.contains(filterValue[idx])) return false; break;
			case 2: if(!periods.contains(filterValue[idx])) return false; break;
			case 3: if(!optimizations.contains(filterValue[idx])) return false; break;
			default: System.out.println("Error (validFilterValues): default case reached");
			}
		}
		
		return true;
	}

}
