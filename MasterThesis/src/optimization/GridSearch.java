package optimization;

import performance.PerformanceMeasures;

public class GridSearch extends Optimization{

	private int[] noSteps;
	private boolean[] exponentialSteps;
	private double[] exponentialBasenumber;
	
	private int noCombinations;
	private int noParameters;
	
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
				if(steps[idx] < 0)
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
				noParameters = noSteps.length;
				noCombinations = 1;
				
				for(int idx=0;idx<noSteps.length;++idx)
					noCombinations = noCombinations*(noSteps[idx]+1);
				
				setupGrid();
			}
		}
	}
	
	public boolean optimize(boolean silent)
	{				
		if(measures.getModel().getCategory().equals(""))
		{
			optimizationError("optimize","set category first");
			return false;
		}

		for(int idx=0;idx<noCombinations;++idx)
		{			
			measures.getModel().setParameters(getGridConfig(idx));

			if(measures.getModel().train(false))
			{
				measures.calculateMeasures("validation");
				updateBest();
			}
			else System.out.printf("Error (GridSearch): model %d did not converge\n",idx);
			
			if(!silent)
			{
				if( ( ( (100*idx) % noCombinations ) < 100) && ( (100*idx) >= noCombinations ) && ( ( (100*idx) / noCombinations) < 100 ) )
				{
					System.out.printf("Completed %d%% of %d models\n",(100*idx) / noCombinations,noCombinations);
				}
			}				
		}
		
		return true;
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
	
	private double[] getGridConfig(int idx)
	{
		double[] output = new double[noParameters];
		
		for(int idx2=0;idx2<noParameters;++idx2)
		{
			int perm = 1; for(int idx3=idx2+1;idx3<noParameters;++idx3) perm = perm * grid[idx3].length;
			output[idx2] = grid[idx2][ (idx / perm ) % grid[idx2].length];
		}
			
		return output;
	}
	
	private void setupGrid()
	{
		grid = new double[bounds.length][];
		
		for(int idx1=0;idx1<bounds.length;++idx1)
		{
			grid[idx1] = new double[noSteps[idx1]+1];
			double stepLength = (bounds[idx1][1]-bounds[idx1][0])/noSteps[idx1];
			if(noSteps[idx1] == 0) stepLength = 0;
			
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
	}
}
