package rawdataprep;

import java.util.ArrayList;

public class Print {

	public static void printList(ArrayList<String> txt) 
	{
		for(int idx=0;idx<txt.size();++idx)
			System.out.println(txt.get(idx));	
	}
	
	public static void print(int[][] txt) 
	{
		for(int idx1=0;idx1<txt.length;++idx1)
		{
			for(int idx2=0;idx2<txt[idx1].length;++idx2)
			{
				System.out.print(txt[idx1][idx2] + " ");
			}
			System.out.println();
		}
	}
	
	public static void print(ArrayList<Integer[]>[] txt)
	{
		for(int idx1=0;idx1<txt.length;++idx1)
		{
			System.out.println("["+idx1+"]");
			
			for(int idx2=0;idx2<txt[idx1].size();++idx2)
			{
				for(int idx3=0;idx3<txt[idx1].get(idx2).length;++idx3)
					System.out.print(txt[idx1].get(idx2)[idx3]+" ");
				
				System.out.println();
			}
			
			System.out.println();
		}
	}
	
	public static void printTable(ArrayList<int[]> txt) 
	{
		for(int idx1=0;idx1<txt.size();++idx1)
		{
			for(int idx2=0;idx2<txt.get(idx1).length;++idx2)
			{
				System.out.print(txt.get(idx1)[idx2] + " ");
			}
			System.out.println();
		}	
	}

	public static void printHead(ArrayList<String> txt,int noLines)
	{
		for(int idx=0;idx<Math.min(txt.size(),noLines);++idx)
			System.out.println(txt.get(idx));
	}

	public static void printTail(ArrayList<String> txt,int noLines)
	{
		for(int idx=Math.max(0, txt.size() - noLines);idx<txt.size();++idx)
			System.out.println(txt.get(idx));
	}
}
