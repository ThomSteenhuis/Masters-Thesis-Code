package graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class LatexTable {
	
	private static int[] config;
	private static ArrayList<String[]> input;

	public static void main(String[] args) 
	{
		if(args.length != 3) {System.out.println("Error (LatexTable): not enough arguments provided"); return;}
		
		String inputLoc = args[0];
		String outputLoc = args[1];
		String configLoc = args[2];

		if(!readConfig(configLoc)) return;
		readInput(inputLoc);
		writeLatexTable(outputLoc);
	}
	
	private static void writeLatexTable(String loc)
	{
		try 
		{
			PrintWriter p = new PrintWriter(new File(loc));
			
			p.printf("\\begin{tabular}{"); 
			for(int idx=0;idx<config.length;++idx) p.printf("l");
			p.printf("}\n"); 
			for(int idx=0;idx<(config.length-1);++idx) p.printf("%s &", input.get(0)[config[idx]]);
			p.printf("%s\\\\ \\hline\n", input.get(0)[config[config.length-1]]);
			
			for(int idx1=1;idx1<input.size();idx1+=2)
			{
				for(int idx2=0;idx2<(config.length-1);++idx2) p.printf("%s &", input.get(idx1)[config[idx2]]);
				p.printf("%s\\\\ \\hline\n", input.get(idx1)[config[config.length-1]]);
			}
			
			p.printf("\\end{tabular}"); 
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
		catch(FileNotFoundException e) {System.out.println("Error (readConfig): file not found"); return;}
	}
	
	private static boolean readConfig(String loc)
	{
		try{
			Scanner s = new Scanner(new File(loc));
			String line = s.nextLine();
			String[] lineSplt = line.split("\t");
			
			config = new int[lineSplt.length];
			for(int idx=0;idx<lineSplt.length;++idx) config[idx] = Integer.parseInt(lineSplt[idx].trim());
			
			s.close();
			return true;
		}
		catch(FileNotFoundException e) {System.out.println("Error (readConfig): file not found"); return false;}
		catch(NumberFormatException e) {System.out.println("Error (readConfig): file invalid"); return false;}
	}

}
