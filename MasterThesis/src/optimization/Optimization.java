package optimization;

import performance.PerformanceMeasures;

public abstract class Optimization {
	protected PerformanceMeasures measures;
	protected double[][] bounds;
	protected String name;
	
	protected boolean startOptimization;
	protected double[] optPars;
	protected double performance;

	public Optimization(PerformanceMeasures pm, double[][] parBounds)
	{
		if(parBounds.length != pm.getModel().getNoParameters() )
		{
			optimizationError("optimization","No parameter bounds not equal to no parameters");
		}
		else
		{
			measures = pm;
			bounds = parBounds;
			startOptimization = true;
		}
	}
	
	public void printBest()
	{
		if(!startOptimization)
		{
			System.out.printf("RMSE\t= %s\n",performance);
			System.out.print("Best parameters\t= ");
			
			for(int idx=0;idx<optPars.length;++idx)
				System.out.printf("%s\t",optPars[idx]);
			
			System.out.println();
		}
		else
		{
			System.out.println("Error (printBest): Optimization should be started first");
		}
	}
	
	public PerformanceMeasures getPerformanceMeasures()
	{
		return measures;
	}
	
	public double[][] getBounds()
	{
		return bounds;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean optimizationStarted()
	{
		return !startOptimization;
	}
	
	public double[] getOptimalParameters()
	{
		return optPars;
	}
	
	public double getPerformance()
	{
		return performance;
	}
	
	public abstract void optimizeAll();
	public abstract void optimize(String cat);
	
	protected void updateBest(String cat)
	{		
		if( startOptimization || (performance > measures.getRMSE() ) )
		{
			performance = measures.getRMSE();
			optPars = measures.getModel().getParameters();
			startOptimization = false;
		}
	}
	
	protected void optimizationError(String method, String msg)
	{
		System.out.printf("Error (%s): %s",method,msg);
	}
}
