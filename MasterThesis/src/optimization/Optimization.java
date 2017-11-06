package optimization;

import models.Model;

public abstract class Optimization {
	protected Model model;
	protected double[][] bounds;
	protected String name;
	
	protected boolean[] startOptimization;
	protected double[][] optPars;
	protected double[][] estVals;
	protected double[] performance;

	public Optimization(Model mdl, double[][] parBounds)
	{
		if(parBounds.length != mdl.getParameters().length)
		{
			optimizationError("optimization","No parameter bounds not equal to no parameters");
		}
		else
		{
			model = mdl;
			bounds = parBounds;
			startOptimization = new boolean[model.getData().getNoCats()];
			
			for(int idx=0;idx<model.getData().getNoCats();++idx)
				startOptimization[idx] = true;
		}
	}
	
	public Model getModel()
	{
		return model;
	}
	
	public double[][] getBounds()
	{
		return bounds;
	}
	
	public String getName()
	{
		return name;
	}
	
	public abstract void optimize();
	
	protected void updateBest(String cat)
	{
		int index = model.getData().getIndexFromCat(cat);
		
		if( startOptimization || (performance[index] > model.) )
		{
			performance = perf;
			estVals = estVals;
			optPars = model.getParameters();
		}
	}
	
	protected void optimizationError(String method, String msg)
	{
		System.out.printf("Error (%s): %s",method,msg);
	}
}
