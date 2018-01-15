package graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class LatexTable {
	
	private static int[] config;
	private static ArrayList<String[]> input;
	private static ArrayList<Integer> filterVars;
	private static ArrayList<String> filterVals;

	public static void main(String[] args) 
	{
		if(args.length != 3) {System.out.println("Error (LatexTable): not enough arguments provided"); return;}
		
		String inputLoc = args[0];
		String outputLoc = args[1];
		String configLoc = args[2];

		if(!readConfig(configLoc)) return;
		readInput(inputLoc);
		adjustInput();
		writeLatexTable(outputLoc);
	}
	
	private static String whichModel()
	{
		for(int idx=0;idx<filterVars.size();++idx)
		{
			if(filterVars.get(idx) == 0) return filterVals.get(idx);
		}
		return null;
	}
	
	private static boolean contains(int[] vec, int val)
	{
		for(int idx=0;idx<vec.length;++idx)
		{
			if(vec[idx]==val) return true;
		}
		return false;
	}
	
	private static void adjustInput()
	{
		input.get(0)[1] = "Machine"; input.get(0)[2] = "Optimization method"; input.get(0)[3] = "Forecast interval";
		for(int idx1=1;idx1<input.size();idx1+=2)
		{
			if(input.get(idx1)[1].equals("2200EVO")) input.get(idx1)[1] = "2200 EVO";
			else if(input.get(idx1)[1].equals("8800FCQ, RFID")) input.get(idx1)[1] = "8800 FCQ";
			
			if(input.get(idx1)[2].equals("GridSearch")) input.get(idx1)[2] = "Grid search algorithm";
			else if(input.get(idx1)[2].equals("Genetic")) input.get(idx1)[2] = "Genetic algorithm";
			
			if(input.get(idx1)[0].equals("ANN"))
			{
				if(input.get(idx1)[9].equals("1.0")) input.get(idx1)[9] = "yes";
				else if(input.get(idx1)[9].equals("0.0")) input.get(idx1)[9] = "no";
			}
			else if(input.get(idx1)[0].equals("SVR"))
			{
				if(input.get(idx1)[10].equals("1.0")) input.get(idx1)[10] = "yes";
				else if(input.get(idx1)[10].equals("0.0")) input.get(idx1)[10] = "no";
			}
		}		
	}
	
	private static void writeLatexTable(String loc)
	{
		try 
		{
			PrintWriter p = new PrintWriter(new File(loc));
			
			int configLength = config.length;
			if(contains(config,9)) configLength++;
			
			p.printf("\\resizebox{\\textwidth}{!}{\\begin{tabular}{"); 
			for(int idx=0;idx<configLength;++idx) p.printf("l");
			p.printf("}\n"); 
			
			String model = whichModel();
			
			for(int idx=0;idx<(config.length-1);++idx) 
			{
				if(config[idx] == 9) p.printf("Number of lags &12th lag &");
				else if(config[idx] == 10) 
				{
					if(model.equals("ANN")) p.printf("\\{Best number of hidden units\\} &");
					else if(model.equals("Gating")) p.printf("\\{Best number of hidden units,C,\\epsilon,\\sigma^2\\} &");
					else if(model.equals("SVR")) p.printf("\\{C,\\epsilon,\\sigma^2\\} &");
					else if(model.equals("SES")) p.printf("\\{\\alpha\\} &");
					else if(model.equals("aDES") || model.equals("mDES")) p.printf("\\{\\alpha,\\beta\\} &");
					else if(model.equals("aDESd") || model.equals("mDESd")) p.printf("\\{\\alpha,\\beta,\\phi\\} &");
					else if(model.equals("aTES") || model.equals("mTES")) p.printf("\\{\\alpha,\\beta,\\gamma\\} &");
					else if(model.equals("aTESd") || model.equals("mTESd")) p.printf("\\{\\alpha,\\beta,\\gamma,\\phi\\} &");
					else if(model.equals("four")) p.printf("\\{\\alpha,\\beta,\\gamma,\\delta\\} &");
				}
				else p.printf("%s &", input.get(0)[config[idx]]);
			}
			if(config[config.length-1] == 9) p.printf("Number of lags &12th lag \\\\ \\hline \n");
			else if(config[config.length-1] == 10) 
			{
				
				if(model.equals("ANN")) p.printf("\\{Best number of hidden units\\} \\\\ \\hline \n");
				else if(model.equals("SVR")) p.printf("\\{C,\\epsilon,\\sigma^2\\} \\\\ \\hline \n");
				else if(model.equals("Gating")) p.printf("\\{Best number of hidden units,C,\\epsilon,\\sigma^2\\} \\\\ \\hline \n");
				else if(model.equals("SES")) p.printf("\\{\\alpha\\} \\\\ \\hline \n");
				else if(model.equals("aDES") || model.equals("mDES")) p.printf("\\{\\alpha,\\beta\\} \\\\ \\hline \n");
				else if(model.equals("aDESd") || model.equals("mDESd")) p.printf("\\{\\alpha,\\beta,\\phi\\} \\\\ \\hline \n");
				else if(model.equals("aTES") || model.equals("mTES")) p.printf("\\{\\alpha,\\beta,\\gamma\\} \\\\ \\hline \n");
				else if(model.equals("aTESd") || model.equals("mTESd")) p.printf("\\{\\alpha,\\beta,\\gamma,\\phi\\} \\\\ \\hline \n");
				else if(model.equals("four")) p.printf("\\{\\alpha,\\beta,\\gamma,\\delta\\} \\\\ \\hline \n");
			}
			else p.printf("%s\\\\ \\hline\n", input.get(0)[config[config.length-1]]);
			
			for(int idx1=1;idx1<input.size();idx1+=2)
			{
				boolean cnt = true;
				for(int idx2=0;idx2<filterVars.size();++idx2)
				{
					if(!input.get(idx1)[filterVars.get(idx2)].equals(filterVals.get(idx2))) {cnt = false; break;}
				}
				
				if(cnt)
				{
					for(int idx2=0;idx2<(config.length-1);++idx2) 
					{
						if( (config[idx2] >= 4) && (config[idx2] <= 8) ) p.printf("%.3f &", Double.parseDouble(input.get(idx1)[config[idx2]]));
						else if(config[idx2] == 9)
						{
							if(input.get(idx1)[0].equals("ANN"))
							{
								p.printf("%.0f &", Double.parseDouble(input.get(idx1)[config[idx2]+1])); p.printf("%s &", input.get(idx1)[config[idx2]]);
							}
							else if(input.get(idx1)[0].equals("SVR"))
							{
								p.printf("%.0f &", Double.parseDouble(input.get(idx1)[config[idx2]+2])); p.printf("%s &", input.get(idx1)[config[idx2]+1]);
							}
						}
						else if(config[idx2] == 10)
						{
							if(input.get(idx1)[0].equals("ANN"))
							{
								p.printf("\\{%.0f\\} &", Double.parseDouble(input.get(idx1)[config[idx2]+1])); 
							}
							else if(input.get(idx1)[0].equals("SVR")) p.printf("\\{%.3f,%.3f,%.3f\\} &", Double.parseDouble(input.get(idx1)[config[idx2]+2]), Double.parseDouble(input.get(idx1)[config[idx2]+3]), Double.parseDouble(input.get(idx1)[config[idx2]+4])) ;
							else if(input.get(idx1)[0].equals("Gating")) p.printf("\\{%.0f,%.3f,%.3f,%.3f\\} &", Double.parseDouble(input.get(idx1)[config[idx2]+1]), Double.parseDouble(input.get(idx1)[config[idx2]+2]), Double.parseDouble(input.get(idx1)[config[idx2]+3]), Double.parseDouble(input.get(idx1)[config[idx2]+4])) ;
							else if(input.get(idx1)[0].equals("SES")) p.printf("\\{%.3f\\} &",Double.parseDouble(input.get(idx1)[config[idx2]-1]));
							else if(input.get(idx1)[0].equals("aDES") || input.get(idx1)[0].equals("mDES")) p.printf("\\{%.3f,%.3f\\} &",Double.parseDouble(input.get(idx1)[config[idx2]-1]),Double.parseDouble(input.get(idx1)[config[idx2]]));
							else if(input.get(idx1)[0].equals("aDESd") || input.get(idx1)[0].equals("mDESd")) p.printf("\\{%.3f,%.3f,%.3f\\} &",Double.parseDouble(input.get(idx1)[config[idx2]-1]),Double.parseDouble(input.get(idx1)[config[idx2]]),Double.parseDouble(input.get(idx1)[config[idx2]+1]));
							else if(input.get(idx1)[0].equals("aTES") || input.get(idx1)[0].equals("mTES")) p.printf("\\{%.3f,%.3f,%.3f\\} &",Double.parseDouble(input.get(idx1)[config[idx2]]),Double.parseDouble(input.get(idx1)[config[idx2]+1]),Double.parseDouble(input.get(idx1)[config[idx2]+2]));
							else if(input.get(idx1)[0].equals("aTESd") || input.get(idx1)[0].equals("mTESd")) p.printf("\\{%.3f,%.3f,%.3f,%.3f\\} &",Double.parseDouble(input.get(idx1)[config[idx2]]),Double.parseDouble(input.get(idx1)[config[idx2]+1]),Double.parseDouble(input.get(idx1)[config[idx2]+2]),Double.parseDouble(input.get(idx1)[config[idx2]+3]));
							else if(input.get(idx1)[0].equals("four")) p.printf("\\{%.3f,%.3f,%.3f,%.3f\\} &",Double.parseDouble(input.get(idx1)[config[idx2]]),Double.parseDouble(input.get(idx1)[config[idx2]+1]),Double.parseDouble(input.get(idx1)[config[idx2]+2]),Double.parseDouble(input.get(idx1)[config[idx2]+3]));
						}
						else p.printf("%s &", input.get(idx1)[config[idx2]]);					
					}
					
					if( (config[config.length-1] >= 4) && (config[config.length-1] <= 8) ) p.printf("%.3f \\\\ \n", Double.parseDouble(input.get(idx1)[config[config.length-1]]));
					else if(config[config.length-1] == 9)
					{
						if(input.get(idx1)[0].equals("ANN"))
						{
							p.printf("%.0f \\\\ \n", Double.parseDouble(input.get(idx1)[config[config.length-1]+1])); p.printf("%s \\\\ \n", input.get(idx1)[config[config.length-1]]);
						}
						else if(input.get(idx1)[0].equals("SVR")) {p.printf("%.0f \\\\ \n", Double.parseDouble(input.get(idx1)[config[config.length-1]+2])); p.printf("%s &", input.get(idx1)[config[config.length-1]+1]);}
					}
					else if(config[config.length-1] == 10)
					{
						if(input.get(idx1)[0].equals("ANN"))
						{
							p.printf("\\{%.0f\\} \\\\ \n", Double.parseDouble(input.get(idx1)[config[config.length-1]+1])); 
						}
						else if(input.get(idx1)[0].equals("SVR")) p.printf("\\{%.3f,%.3f,%.3f\\} \\\\ \n", Double.parseDouble(input.get(idx1)[config[config.length-1]+2]), Double.parseDouble(input.get(idx1)[config[config.length-1]+3]), Double.parseDouble(input.get(idx1)[config[config.length-1]+4])) ;
						else if(input.get(idx1)[0].equals("Gating")) p.printf("\\{%.0f,%.3f,%.3f,%.3f\\} \\\\ \n", Double.parseDouble(input.get(idx1)[config[config.length-1]+1]), Double.parseDouble(input.get(idx1)[config[config.length-1]+2]), Double.parseDouble(input.get(idx1)[config[config.length-1]+3]), Double.parseDouble(input.get(idx1)[config[config.length-1]+4])) ;
						else if(input.get(idx1)[0].equals("SES")) p.printf("\\{%.3f\\} \\\\ \n",Double.parseDouble(input.get(idx1)[config[config.length-1]-1]));
						else if(input.get(idx1)[0].equals("aDES") || input.get(idx1)[0].equals("mDES")) p.printf("\\{%.3f,%.3f\\} \\\\ \n",Double.parseDouble(input.get(idx1)[config[config.length-1]-1]),Double.parseDouble(input.get(idx1)[config[config.length-1]]));
						else if(input.get(idx1)[0].equals("aDESd") || input.get(idx1)[0].equals("mDESd")) p.printf("\\{%.3f,%.3f,%.3f\\} \\\\ \n",Double.parseDouble(input.get(idx1)[config[config.length-1]-1]),Double.parseDouble(input.get(idx1)[config[config.length-1]]),Double.parseDouble(input.get(idx1)[config[config.length-1]+1]));
						else if(input.get(idx1)[0].equals("aTES") || input.get(idx1)[0].equals("mTES")) p.printf("\\{%.3f,%.3f,%.3f\\} \\\\ \n",Double.parseDouble(input.get(idx1)[config[config.length-1]]),Double.parseDouble(input.get(idx1)[config[config.length-1]+1]),Double.parseDouble(input.get(idx1)[config[config.length-1]+2]));
						else if(input.get(idx1)[0].equals("aTESd") || input.get(idx1)[0].equals("mTESd")) p.printf("\\{%.3f,%.3f,%.3f,%.3f\\} \\\\ \n",Double.parseDouble(input.get(idx1)[config[config.length-1]]),Double.parseDouble(input.get(idx1)[config[config.length-1]+1]),Double.parseDouble(input.get(idx1)[config[config.length-1]+2]),Double.parseDouble(input.get(idx1)[config[config.length-1]+3]));
						else if(input.get(idx1)[0].equals("four")) p.printf("\\{%.3f,%.3f,%.3f,%.3f\\} \\\\ \n",Double.parseDouble(input.get(idx1)[config[config.length-1]]),Double.parseDouble(input.get(idx1)[config[config.length-1]+1]),Double.parseDouble(input.get(idx1)[config[config.length-1]+2]),Double.parseDouble(input.get(idx1)[config[config.length-1]+3]));
					}
					else p.printf("%s\\\\ \n", input.get(idx1)[config[config.length-1]]);	
				}				
			}
			
			p.printf("\\end{tabular}}"); 
			p.close();
		} 
		catch (FileNotFoundException e) {System.out.println("Error (writeLatexTable): output location not found"); return;}
	}
	
	private static void readInput(String loc)
	{
		try{
			Scanner s = new Scanner(new File(loc));
			input = new ArrayList<String[]>();
			
			while(s.hasNextLine())
			{
				String line = s.nextLine();
				input.add(line.split("\t"));
			}
		}
		catch(FileNotFoundException e) {System.out.println("Error (readInput): file not found"); return;}
	}
	
	private static boolean readConfig(String loc)
	{
		try{
			Scanner s = new Scanner(new File(loc));
			String line = s.nextLine();
			String[] lineSplt = line.split("\t");
			
			config = new int[lineSplt.length];
			for(int idx=0;idx<lineSplt.length;++idx) config[idx] = Integer.parseInt(lineSplt[idx].trim());
			
			filterVars = new ArrayList<Integer>();
			filterVals = new ArrayList<String>();
			
			while(s.hasNextLine())
			{
				line = s.nextLine();
				lineSplt = line.split("=");
				filterVars.add(Integer.parseInt(lineSplt[0].trim()));
				filterVals.add(lineSplt[1].trim());
			}
			
			s.close();
			return true;
		}
		catch(FileNotFoundException e) {System.out.println("Error (readConfig): file not found"); return false;}
		catch(NumberFormatException e) {System.out.println("Error (readConfig): file invalid"); return false;}
	}

}
