package optimization;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class Initialize {
	private static boolean filesNotRead = true;
	private static final String modelFileLocation = "src/data/models.txt";
	private static String[][] models;
	private static Hashtable<String,Integer> modelsHashtable;
	
	private static final String optFileLocation = "src/data/optimization.txt";
	private static Hashtable<String,Integer> optHashtable;
	
	public static double[] optPars;
	public static double[] estVals;
	public static double performance = -1;
	
	public static void initializeOptimization(String model,String optMethod,double[][] bounds,int periods, int performanceMeasure,double[] trainingData,double[] validationData)
	{
		if(filesNotRead)
			filesRead();
		
		try{
			int index = modelsHashtable.get(model);
			int modelClass = Integer.parseInt(models[index][1]);
			int modelNo = Integer.parseInt(models[index][2]);
			int noPars = Integer.parseInt(models[index][3]);
			
			int optNo = optHashtable.get(optMethod);
			
			if( (bounds.length != noPars) )
			{
				System.out.println("Error (initializeOptimization): number of parameters not correct");
			}
			
			optimize(optNo,modelClass,modelNo,bounds,periods,performanceMeasure,trainingData,validationData);
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void optimize(int optNo,int modelClass,int modelNo,double[][] bounds,int periods,int pm,double[] td, double[] vd)
	{
		switch(optNo){
		case 0 :
		{
			GridSearch.optimize(modelClass,modelNo,bounds,periods,pm,td,vd);
			break;
		}
		default:
		{
			System.out.println("Error (optimize): default case reached");
		}
		}
	}
	
	private static void filesRead()
	{
		ArrayList<String> rawTxt = new ArrayList<String>();
		
		try{
			Scanner scanner = new Scanner(new File(modelFileLocation));

			while (scanner.hasNextLine()) 
			{
				rawTxt.add(scanner.nextLine());
			}
		
			scanner.close();
			
			models = new String[rawTxt.size()][];
			
			for(int idx=0;idx<rawTxt.size();++idx)
			{
				models[idx] = rawTxt.get(idx).split("\t");
			}
			
			modelsHashtable = new Hashtable<String,Integer>();
			
			for(int idx=1;idx<models.length;++idx)
			{
				modelsHashtable.put(models[idx][0],idx);
			}
			
			rawTxt = new ArrayList<String>();
			
			scanner = new Scanner(new File(optFileLocation));

			while (scanner.hasNextLine()) 
			{
				rawTxt.add(scanner.nextLine());
			}
		
			scanner.close();
			
			optHashtable = new Hashtable<String,Integer>();
			
			for(int idx=0;idx<rawTxt.size();++idx)
			{
				optHashtable.put(rawTxt.get(idx),idx);
			}
			
			filesNotRead = false;
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}		
	}
}
