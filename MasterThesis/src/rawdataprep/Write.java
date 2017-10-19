package rawdataprep;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Write {

	public static void writeTxt(ArrayList<String> txt, String location)
	{
		try
		{
		    PrintWriter writer = new PrintWriter(location);

		    for(int idx=0; idx<txt.size();++idx)
		    {
		    	writer.println(txt.get(idx));
		    }
		    
		    writer.close();
		} 
		catch (IOException e) 
		{
		   e.printStackTrace();
		}
	}
	
	public static void writeTxt(ArrayList<String>[] txt, String location)
	{
		try
		{
		    PrintWriter writer = new PrintWriter(location);

		    for(int idx1=0; idx1<txt.length;++idx1)
		    {
		    	for(int idx2=0;idx2<txt[idx1].size();++idx2)
		    		writer.println(txt[idx1].get(idx2));
		    }
		    
		    writer.close();
		} 
		catch (IOException e) 
		{
		   e.printStackTrace();
		}
	}
}
