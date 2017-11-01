package input;

import java.util.ArrayList;

import graph.Plot;

public class Run{

	private static final String dataLocation = "src/data/prepared_data.txt";
	
	public static String[] header;
	public static String[] categories;
	public static double[][] volumes;
	public static String[] dates;
	public static String[] labels;

	public static void main(String[] args)
	{
		ArrayList<String> data = Read.readTxt(dataLocation);

		if(data.size() <2)
			mainError();

		header = data.get(0).split("\t");

		if(header.length < 3)
			mainError();

		categories = new String[header.length - 2];

		for(int idx=2;idx<header.length;++idx)
			categories[idx-2] = header[idx];

		volumes = new double[data.size()-1][];
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

		String[] pars = new String[1];
		pars[0] = "pivot";

		Plot.initialize(pars,volumes,dates,categories,labels);
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
}
