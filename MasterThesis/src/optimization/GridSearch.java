package optimization;

public class GridSearch {
	private static final int noSteps = 100;
	
	public static void optimize(int modelClass,int modelNo,double[][] bounds, int periods,int pm, double[] td, double[] vd)
	{
		double[][] grid = setupGrid(bounds);
		
		for(int idx=0;idx<grid.length;++idx)
		{
			models.Initialize.trainAndValidate(modelClass,modelNo,grid[idx],periods,pm,td,vd);
		}
	}
	
	private static double[][] setupGrid(double[][] bounds)
	{
		double[][] grid = new double[bounds.length][noSteps];
		
		for(int idx1=0;idx1<bounds.length;++idx1)
		{
			double stepLength = (bounds[idx1][1]-bounds[idx1][0])/(noSteps-1);
			
			for(int idx2=0;idx2<noSteps;++idx2)
			{
				grid[idx1][idx2] = bounds[idx1][0] + idx2*stepLength;
			}
		}
		
		double[][] combinations = findCombinations(grid);
		
		return combinations;
	}
	
	private static double[][] findCombinations(double[][] grid)
	{
		int noPars = grid.length;
		
		if(noPars == 0)
			return null;
		
		int noPos = grid[0].length;		
		int noCombinations = (int) Math.pow(noPos,noPars);

		double[][] output = new double[noCombinations][3];
		
		for(int idx1=0;idx1<noCombinations;++idx1)
		{
			for(int idx2=0;idx2<noPars;++idx2)
			{
				output[idx1][idx2] = grid[idx2][ (idx1 / ( (int) ( Math.pow(noPos,noPars-1-idx2) ) ) ) % noPos];
			}
		}
		
		return output;
	}
}
