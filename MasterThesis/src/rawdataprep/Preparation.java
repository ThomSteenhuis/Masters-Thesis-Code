package rawdataprep;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import input.Read;

public class Preparation {

	private static final String DALocation = "src/data/die_attach.txt";
	private static final String DAMLocation = "src/data/DA_machines.txt";
	private static final String machineList = "src/data/machine_list.txt";
	private static final String FCLLocation = "src/data/fcl.txt";

	public static ArrayList<String> findDistinctMachines(ArrayList<String> machines)
	{
		ArrayList<String> output = new ArrayList<String>();
		String[] split = null;

		for(int idx=1;idx<machines.size();++idx)
		{
			split = machines.get(idx).split("\t");

			if(split.length > 17)
			{
				if(!output.contains(split[17]))
					output.add(split[17]);
			}
		}

		return output;
	}

	public static ArrayList<String> cleanupDescription(ArrayList<String> machines)
	{
		ArrayList<String> output = new ArrayList<String>();

		if(machines.size() < 2)
		{
			System.out.println("Error (cleanupDescription): machines has not enough lines");
			return null;
		}

		output.add(machines.get(0));

		String[] split1 = null;
		String[] split2 = null;
		String text = null;

		for(int idx = 1; idx<machines.size(); ++idx)
		{
			split1 = machines.get(idx).split("\t");

			if(split1.length > 17)
			{
				split2 = split1[17].trim().split("\\(");
			}

			text = split1[0].trim();

			for(int idx2=1;idx2<17;++idx2)
				text = text + "\t" + split1[idx2].trim();

			text = text + "\t" + split2[0].trim();

			for(int idx2=18;idx2<split1.length;++idx2)
				text = text + "\t" + split1[idx2].trim();

			output.add(text);
		}

		return output;
	}

	public static ArrayList<String>[] filterMachines(ArrayList<String> txt)
	{
		Run.machineNames = new ArrayList<String>();

		try{
			Scanner scanner = new Scanner(new File(machineList));

			while (scanner.hasNextLine())
			{
				Run.machineNames.add(scanner.nextLine());
			}

			scanner.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}

		ArrayList<String>[] output = new ArrayList[Run.machineNames.size()];
		ArrayList<String> DAText = null;
		ArrayList<String> FCLText = null;
		String[][] DATable = null;

		if(txt.size() < 4)
		{
			System.out.println("Error (filterMachines): wholeTxt has not enough lines");
			return null;
		}

		for(int idx=0;idx<output.length;++idx)
			output[idx] = new ArrayList<String>();

		DAText = Read.readTxt(DALocation);
		FCLText = Read.readTxt(FCLLocation);

		DATable = new String[DAText.size()][2];
		String[] line = null;

		for(int idx=0;idx<DAText.size();++idx)
		{
			line = DAText.get(idx).split("\t");
			DATable[idx][0] = line[0].trim();
			DATable[idx][1] = line[1].trim();
		}

		String test1 = null;
		String test2 = null;
		String test3 = null;
		String[] split = null;

		for(int idx = 0; idx<txt.size(); ++idx)
		{
			split = txt.get(idx).split("\\|");

			if(split.length > 17)
			{
				test1 = split[16].trim();
				test2 = split[3].trim();
				test3 = split[17].trim();

				if(test1.startsWith("950") && (test1.length() > 7) && !test2.startsWith("45"))
					addDieAttach(txt.get(idx),test1,output,DATable);
				else if(checkFCL(test1,FCLText))
					output[6].add(txt.get(idx));
				else if(test1.startsWith("5") && !test2.startsWith("45") )
				{
					if( (test1.length() == 5) && (test3.contains("AMS") || test3.contains("MMS") ) )
						output[5].add(txt.get(idx));
					else if(test1.length() > 11)
					{
						if(test1.substring(5,11).equals(" SYSTEM"))
							output[5].add(txt.get(idx));
					}
				}
				else if(test1.startsWith("41") && !test2.startsWith("45") && test3.contains("FSL") && test3.contains("SML"))
					output[7].add(txt.get(idx));
				else if(test1.startsWith("801.000") && !test2.startsWith("45") )
					output[8].add(txt.get(idx));
			}
		}

		return output;
	}

	private static boolean checkFCL(String test,ArrayList<String> compare)
	{
		boolean output = false;

		for(int idx=0;idx<compare.size();++idx)
		{
			if(compare.get(idx).trim().equals(test))
			{
				output = true;
				break;
			}
		}

		return output;
	}

	private static void addDieAttach(String line, String materialNo,ArrayList<String>[] output,String[][] DATable)
	{
		ArrayList<String> DAMachines = null;
		DAMachines = Read.readTxt(DAMLocation);

		if(DAMachines.size() >= 5)
		{
			for(int idx=0;idx<DATable.length;++idx)
			{
				if(materialNo.startsWith(DATable[idx][0]))
				{
					if(DATable[idx][1].equals(DAMachines.get(0)))
						output[0].add(line);
					else if(DATable[idx][1].equals(DAMachines.get(1)))
						output[1].add(line);
					else if(DATable[idx][1].equals(DAMachines.get(2)))
						output[2].add(line);
					else if(DATable[idx][1].equals(DAMachines.get(3)))
						output[3].add(line);
					else if(DATable[idx][1].equals(DAMachines.get(4)))
						output[4].add(line);
					break;
				}
			}
		}
	}

	public static ArrayList<String>[] getDates(ArrayList<String>[] txt)
	{
		ArrayList<String>[] output = new ArrayList[Run.machineNames.size()];

		for(int idx=0;idx<output.length;++idx)
			output[idx] = new ArrayList<String>();

		for(int idx=0;idx<txt.length;++idx)
		{
			if(txt[idx].size() < 1)
			{
				System.out.println("Error (getDates): txt has not enough lines");
				return null;
			}
		}

		String[] split = null;

		for(int idx1=0;idx1<txt.length;++idx1)
		{
			for(int idx2=0;idx2<txt[idx1].size();++idx2)
			{
				split = txt[idx1].get(idx2).split("\\|");

				if(split.length > 21)
				{
					if(split[20].trim().equals("") )
						output[idx1].add(split[21].trim());
					else
						output[idx1].add(split[20].trim());
				}
			}
		}

		return output;
	}

	public static ArrayList<Integer[]>[] getMonthYears(ArrayList<String>[] txt)
	{
		ArrayList<Integer[]>[] output = new ArrayList[txt.length];

		for(int idx=0;idx<output.length;++idx)
			output[idx] = new ArrayList<Integer[]>();

		Integer[] tempInt = null;

		String[] split = null;

		for(int idx1=0;idx1<txt.length;++idx1)
		{
			for(int idx2=0;idx2<txt[idx1].size();++idx2)
			{
				split = txt[idx1].get(idx2).split("\\.");
				tempInt = new Integer[2];

				if(split[1].trim().startsWith("0"))
					tempInt[0] = Integer.parseInt(split[1].trim().substring(1));
				else
					tempInt[0] = Integer.parseInt(split[1].trim());

				tempInt[1] = Integer.parseInt(split[2].trim());
				output[idx1].add(tempInt);
			}
		}

		return output;
	}
}
