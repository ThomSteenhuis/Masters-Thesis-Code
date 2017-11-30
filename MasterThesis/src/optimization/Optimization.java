package optimization;

import performance.PerformanceMeasures;

public abstract class Optimization {
	protected PerformanceMeasures measures;
	protected double[][] bounds;
	protected String name;
	
	protected boolean optimized;
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
			optimized = false;
		}
	}
	
	public void printBest()
	{
		if(optimized)
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
	
	public boolean isOptimized()
	{
		return optimized;
	}
	
	public double[] getOptimalParameters()
	{
		return optPars;
	}
	
	public double getPerformance()
	{
		return performance;
	}
	
	public abstract boolean optimize(boolean silent);
	
	protected void updateBest()
	{		
		if(measures.getModel().getName().equals("ARIMA"))
		{
			if( !optimized || (performance > measures.getModel().getAIC() ) )
			{
				performance = measures.getModel().getAIC();
				optPars = measures.getModel().getParameters();
				optimized = true;
			}
		}
		else
		{
			double currentMeasure = 0;
			for(int idx=0;idx<measures.getModel().getNoOutputs();++idx) currentMeasure += measures.getRMSE()[idx];
			currentMeasure = currentMeasure / measures.getModel().getNoOutputs();
			
			if( !optimized || (performance > currentMeasure ) )
			{
				performance = currentMeasure;
				optPars = measures.getModel().getParameters();
				optimized = true;
			}
		}
	}
	
	protected void optimizationError(String method, String msg)
	{
		System.out.printf("Error (%s): %s",method,msg);
	}
}
