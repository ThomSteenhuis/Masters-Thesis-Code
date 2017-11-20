package rawdataprep;

import java.util.ArrayList;

import input.Read;

public class Run {
	private static ArrayList<String> wholeTxt;
	public static int wholeTxtLineCnt = 0;

	private static final String inputLocation = "src/data/raw_data_ZOTD.txt";
	private static final String outputLocation = "src/data/machines_ZOTD.txt";
	private static final String dmLocation = "src/data/distinctMachines_ZOTD.txt";
	private static final String pivotLocation = "src/data/prepared_data.txt";

	private static ArrayList<String> distinctMachines;
	private static ArrayList<String>[] rawDates;
	private static ArrayList<Integer[]>[] monthYears;
	public static ArrayList<int[]> pivotTable;

	public static ArrayList<String> machineNames;

	public static void main(String[] args)
	{
		wholeTxt = Read.readTxt(inputLocation);
		System.out.println("Number of lines: "+wholeTxtLineCnt);
		Print.printHead(wholeTxt,100);

		ArrayList<String>[] machines = Preparation.filterMachines(wholeTxt);

		Print.printHead(machines[0],100);
		Print.printTail(machines[0],100);

		Write.writeTxt(machines,outputLocation);

		rawDates = Preparation.getDates(machines);
		monthYears = Preparation.getMonthYears(rawDates);

		createPivotTable(monthYears);
		Print.printTable(pivotTable);

		String[] header = new String[2+machineNames.size()];
		header[0] = "Month";
		header[1] = "Year";

		for(int idx=0;idx<machineNames.size();++idx)
			header[idx+2] = machineNames.get(idx);

		Write.writeArray(pivotTable,header,pivotLocation);
	}

	private static void createPivotTable(ArrayList<Integer[]>[] input)
	{
		pivotTable = new ArrayList<int[]>();
		int[] entry = new int[2+input.length];
		boolean added;

		for(int idx0=0;idx0<input.length;++idx0)
		{
			for(int idx1=0;idx1<input[idx0].size();++idx1)
			{
				added = false;

				for(int idx2=0;idx2<pivotTable.size();++idx2)
				{
					if( (input[idx0].get(idx1)[1] == pivotTable.get(idx2)[1]) && (input[idx0].get(idx1)[0] == pivotTable.get(idx2)[0]) )
					{
						pivotTable.get(idx2)[2+idx0]++;
						added = true;
						break;
					}
					else if( (input[idx0].get(idx1)[1] < pivotTable.get(idx2)[1]) || ( (input[idx0].get(idx1)[1] == pivotTable.get(idx2)[1]) && (input[idx0].get(idx1)[0] < pivotTable.get(idx2)[0]) ) )
					{
						entry = new int[2+input.length];
						entry[0] = input[idx0].get(idx1)[0];
						entry[1] = input[idx0].get(idx1)[1];

						for(int idx3=2;idx3<(2+input.length);++idx3)
							entry[idx3] = 0;

						entry[2+idx0] = 1;

						pivotTable.add(idx2, entry);
						added = true;
						break;
					}
				}

				if(!added)
				{
					entry = new int[2+input.length];
					entry[0] = input[idx0].get(idx1)[0];
					entry[1] = input[idx0].get(idx1)[1];

					for(int idx3=2;idx3<(2+input.length);++idx3)
						entry[idx3] = 0;

					entry[2+idx0] = 1;

					pivotTable.add(entry);
				}
			}
		}

		for(int idx=(pivotTable.size()-2);idx>=0;--idx)
		{
			if( (pivotTable.get(idx+1)[1] == pivotTable.get(idx)[1]) && ( (pivotTable.get(idx+1)[0] - pivotTable.get(idx)[0]) > 1) )
			{
				entry = new int[3];
				entry[0] = pivotTable.get(idx+1)[0] - 1;
				entry[1] = pivotTable.get(idx+1)[1];
				entry[2] = 0;

				pivotTable.add(idx+1,entry);
			}
			else if( (pivotTable.get(idx+1)[1] > pivotTable.get(idx)[1]) )
			{
				if(pivotTable.get(idx+1)[0] > 1)
				{
					entry = new int[3];
					entry[0] = pivotTable.get(idx+1)[0] - 1;
					entry[1] = pivotTable.get(idx+1)[1];
					entry[2] = 0;

					pivotTable.add(idx+1,entry);
				}
				else if(pivotTable.get(idx)[0] < 12)
				{
					entry = new int[3];
					entry[0] = 12;
					entry[1] = pivotTable.get(idx+1)[1] - 1;
					entry[2] = 0;

					pivotTable.add(idx+1,entry);
				}
			}
		}
	}
}

