package optimization;

import performance.PerformanceMeasures;

public class GridSearch extends Optimization{

	private int[] noSteps;
	private boolean[] exponentialSteps;
	private double[] exponentialBasenumber;
	private double[][] grid;
	
	public GridSearch(PerformanceMeasures pm,double[][] parBounds,boolean[] exp,double[] expBase,int[] steps)
	{
		super(pm,parBounds);
		name = "GridSearch";
		boolean stepsNOK = false;
		
		if( (steps.length != exp.length) || (steps.length != expBase.length) )
			optimizationError("GridSearch","input lengths not equal");
		else
		{
			for(int idx=0;idx<steps.length;++idx)
			{
				if(steps[idx] < 1)
					stepsNOK = true;
			}
			
			if(stepsNOK)
			{
				optimizationError("GridSearch","no steps should be larger than 1");
			}
			else
			{
				noSteps = steps;
				exponentialSteps = exp;
				exponentialBasenumber = expBase;
				grid = setupGrid();
			}
		}
	}
	
	public void optimizeAll(boolean silent)
	{		
		for(String cat:measures.getModel().getData().getCategories())
		{
			measures.getModel().setCategory(cat);
			
			for(int idx2=0;idx2<grid.length;++idx2)
			{
				measures.getModel().setParameters(grid[idx2]);
				measures.getModel().train();
				measures.getModel().validate();
				measures.calculateMeasures();
				updateBest();
			}
		}
	}
	
	public void optimize(boolean silent)
	{				
		if(measures.getModel().getCategory().equals(""))
		{
			optimizationError("optimize","set category first");
			return;
		}
		
		for(int idx=0;idx<grid.length;++idx)
		{
			measures.getModel().setParameters(grid[idx]);
			measures.getModel().train();
			measures.getModel().validate();
			measures.calculateMeasures();
			updateBest();
			
			if(!silent)
			{
				if( ( (idx % (grid.length/100) ) == 0) && (idx >= (grid.length/100) ) && ((idx / (grid.length/100) < 100 ) ) )
				{
					System.out.printf("Completed %d%% of %d models\n",idx / (grid.length/100),grid.length);
				}
			}
				
		}
	}
	
	public int[] getNoSteps()
	{
		return noSteps;
	}
	
	public boolean[] getExponentialSteps()
	{
		return exponentialSteps;
	}
	
	public double[] getExponentialBasenumber()
	{
		return exponentialBasenumber;
	}
	
	private double[][] setupGrid()
	{
		double[][] grid = new double[bounds.length][];
		
		for(int idx1=0;idx1<bounds.length;++idx1)
		{
			grid[idx1] = new double[noSteps[idx1]+1];
			double stepLength = (bounds[idx1][1]-bounds[idx1][0])/noSteps[idx1];
			
			if(exponentialSteps[idx1])
			{
				for(int idx2=0;idx2<=noSteps[idx1];++idx2)
					grid[idx1][idx2] = Math.pow(2,bounds[idx1][0] + idx2*stepLength);
			}
			else
			{
				for(int idx2=0;idx2<=noSteps[idx1];++idx2)
					grid[idx1][idx2] = bounds[idx1][0] + idx2*stepLength;
			}			
		}
		
		return findCombinations(grid);
	}
	
	private static double[][] findCombinations(double[][] grid)
	{
		int noPars = grid.length;
		
		if(noPars == 0)
			return null;
			
		int noCombinations = 1;
		
		for(int idx=0;idx<noPars;++idx)
			noCombinations = noCombinations * grid[idx].length;

		double[][] output = new double[noCombinations][noPars];
		
		for(int idx1=0;idx1<noCombinations;++idx1)
		{
			for(int idx2=0;idx2<noPars;++idx2)
			{
				output[idx1][idx2] = grid[idx2][ (idx1 / ( (int) ( Math.pow(grid[idx2].length,noPars-1-idx2) ) ) ) % grid[idx2].length];
			}
		}
		
		return output;
	}
}
