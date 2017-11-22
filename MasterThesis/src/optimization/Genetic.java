package optimization;

import performance.PerformanceMeasures;

public class Genetic extends Optimization {
	
	public Genetic(PerformanceMeasures pm, double[][] bounds)
	{
		super(pm,bounds);
	}

	public boolean optimizeAll(boolean silent) 
	{
		return false;
	}

	@Override
	public boolean optimize(boolean silent) 
	{
		return false;
	}

}
