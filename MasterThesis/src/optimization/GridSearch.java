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
		double[][] combinations = new double[(int) (Math.pow(noSteps,bounds.length) )][bounds.length];
		
		for(int idx1=0;idx1<bounds.length;++idx1)
		{
			double stepLength = (bounds[idx1][1]-bounds[idx1][0])/(noSteps-1);
			
			for(int idx2=0;idx2<noSteps;++idx2)
			{
				grid[idx1][idx2] = bounds[idx1][0] + idx2*stepLength;
			}
		}
		
		return combinations;
	}
}
