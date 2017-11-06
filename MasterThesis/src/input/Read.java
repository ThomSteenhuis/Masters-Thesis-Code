package input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Read {

	public static ArrayList<String> readTxt(String location)
	{
		ArrayList<String> output = new ArrayList<String> ();
		
		try{
			Scanner scanner = new Scanner(new File(location));

			while (scanner.hasNextLine()) 
			{
				output.add(scanner.nextLine());
			}
		
			scanner.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return output;
	}
}
