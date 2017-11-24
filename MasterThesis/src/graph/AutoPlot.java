package graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import math.Matrix;

public class AutoPlot {
	
	private static String iLoc1;
	private static String iLoc2;
	private static String oLoc;
	
	private static String[] categories;
	private static String[] model;
	private static String[] optimization;
	private static int[] noPersAhead;
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
	private static String[][] cats;
	private static String[][] labels;

	public static void main(String[] args) 
	{
		iLoc1 = args[0];
		iLoc2 = args[1];
		oLoc = args[2];
		
		readInput();
		deriveData();
		
		LineGraph lg = new LineGraph(volumes,dates,cats,labels);
		lg.plot();
		
		
	}
	
	private static void deriveData()
	{
		ArrayList<String> tmp1 = new ArrayList<String>(); 
		ArrayList<String> tmp2 = new ArrayList<String>(); 
		ArrayList<String> tmp3 = new ArrayList<String>(); 
		ArrayList<String> tmp4 = new ArrayList<String>(); 
		ArrayList<Integer> dataLength1 = new ArrayList<Integer>();
		ArrayList<Integer> dataLength2 = new ArrayList<Integer>();
		ArrayList<Integer> dataLength3 = new ArrayList<Integer>();
		
		for(int idx=0;idx<model.length;++idx)
		{
			if(success[idx])
			{
				if(!tmp1.contains(categories[idx]))
				{
					tmp1.add(categories[idx]);
					dataLength1.add(realTrainingData[idx].length);
					dataLength2.add(realValidationData[idx].length);
					dataLength3.add(realTestingData[idx].length);
				}
			}
		}
		
		for(int idx=0;idx<model.length;++idx)
		{
			if(success[idx])
			{
				if(!tmp2.contains(Integer.toString(noPersAhead[idx]) ) )
					tmp2.add(Integer.toString(noPersAhead[idx]));
			}
		}
		
		for(int idx=0;idx<model.length;++idx)
		{
			if(success[idx])
			{
				if(!tmp3.contains(optimization[idx]))
					tmp3.add(optimization[idx]);
			}
		}
		
		for(int idx=0;idx<model.length;++idx)
		{
			if(success[idx])
			{
				if(!tmp4.contains(model[idx]))
					tmp4.add(model[idx]);
			}
		}
		
		int noPlots = tmp1.size() * tmp2.size() * 3;
		int noCats = tmp3.size() * tmp4.size() + 1;
		
		volumes = new double[noPlots][][];
		dates = new String[noPlots][];
		cats = new String[noPlots][noCats];
		labels = new String[noPlots][2];
		
		for(int idx1=0;idx1<tmp1.size();idx1++)
		{
			for(int idx2=0;idx2<tmp2.size();idx2++)
			{
				volumes[idx1*tmp2.size()*3+idx2*3] = new double[dataLength1.get(idx1)][noCats];
				volumes[idx1*tmp2.size()*3+idx2*3+1] = new double[dataLength2.get(idx1)][noCats];
				volumes[idx1*tmp2.size()*3+idx2*3+2] = new double[dataLength3.get(idx1)][noCats];
				
				dates[idx1*tmp2.size()*3+idx2*3] = new String[dataLength1.get(idx1)];
				dates[idx1*tmp2.size()*3+idx2*3+1] = new String[dataLength2.get(idx1)];
				dates[idx1*tmp2.size()*3+idx2*3+2] = new String[dataLength3.get(idx1)];
				
				ArrayList<Integer> instances = new ArrayList<Integer>();
				
				for(int idx3=0;idx3<model.length;++idx3)
				{
					if(success[idx3])
					{
						if(tmp1.get(idx1).equals(categories[idx3]) && tmp2.get(idx2).equals(Integer.toString(noPersAhead[idx3]) ) )
							instances.add(idx3);
					}
				}
				
				labels[idx1*tmp2.size()+idx2][0] = "Time";
				labels[idx1*tmp2.size()+idx2][1] = "Volume";
				cats[idx1*tmp2.size()+idx2][0] = "Actual data";
				
				for(int idx3=0;idx3<tmp3.size();++idx3)
				{
					for(int idx4=0;idx4<tmp4.size();++idx4)
						cats[idx1*tmp2.size()+idx2][idx3*tmp4.size()+idx4+1] = tmp3.get(idx3) + " " + tmp4.get(idx4);
				}
				
				for(int idx3=0;idx3<dataLength1.get(idx1);++idx3)
				{
					volumes[idx1*tmp2.size()*3+idx2*3][idx3][0] = realTrainingData[instances.get(0)][idx3];
					dates[idx1*tmp2.size()*3+idx2*3][idx3]  = realTrainingDates[instances.get(0)][idx3];
					
					for(int idx4=0;idx4<instances.size();++idx4)
						volumes[idx1*tmp2.size()*3+idx2*3][idx3][idx4+1] = forecastTrainingData[instances.get(idx4)][idx3];						
				}
					
				for(int idx3=0;idx3<dataLength2.get(idx1);++idx3)
				{
					volumes[idx1*tmp2.size()*3+idx2*3+1][idx3][0] = realValidationData[instances.get(0)][idx3];
					dates[idx1*tmp2.size()*3+idx2*3+1][idx3]  = realValidationDates[instances.get(0)][idx3];
					
					for(int idx4=0;idx4<instances.size();++idx4)
						volumes[idx1*tmp2.size()*3+idx2*3+1][idx3][idx4+1] = forecastValidationData[instances.get(idx4)][idx3];
				}
				
				for(int idx3=0;idx3<dataLength3.get(idx1);++idx3)
				{
					volumes[idx1*tmp2.size()*3+idx2*3+2][idx3][0] = realTestingData[instances.get(0)][idx3];
					dates[idx1*tmp2.size()*3+idx2*3+2][idx3]  = realTestingDates[instances.get(0)][idx3];
					
					for(int idx4=0;idx4<instances.size();++idx4)
						volumes[idx1*tmp2.size()*3+idx2*3+2][idx3][idx4+1] = forecastTestingData[instances.get(idx4)][idx3];						
				}				
			}
		}
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
			
			model = new String[lines.size()-1];
			categories = new String[lines.size()-1];
			optimization = new String[lines.size()-1];
			noPersAhead = new int[lines.size()-1];
			success = new boolean[lines.size()-1];
			
			for(int idx=1;idx<lines.size();++idx)
			{
				String[] l = lines.get(idx).split("\t");
				model[idx-1] = l[0].trim();
				categories[idx-1] = l[1].trim();
				optimization[idx-1] = l[2].trim();
				noPersAhead[idx-1] = Integer.parseInt(l[3].trim());
				
				if(l[4].equals("Instance failed"))
					success[idx-1] = false;
				else
					success[idx-1] = true;
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

}
