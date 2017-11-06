package optimization;

import models.Model;

public class GridSearch extends Optimization{

	private int noSteps;
	
	private double[][] grid;
	
	public GridSearch(Model mdl,double[][] parBounds,int steps)
	{
		super(mdl,parBounds);
		name = "GridSearch";
		
		if(steps <= 1)
		{
			optimizationError("GridSearch","no steps should be larger than 1");
		}
		else
		{
			noSteps = steps;
			grid = setupGrid();
		}
	}
	
	public void optimize()
	{		
		for(String idx1:model.getData().getCategories())
		{
			for(int idx2=0;idx2<grid.length;++idx2)
			{
				model.setParameters(grid[idx2]);
				model.train(idx1);
				updateBest(idx1);
			}
		}
	}
	
	public int getNoSteps()
	{
		return noSteps;
	}
	
	private double[][] setupGrid()
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
		
		return findCombinations(grid);
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
