package math;

public abstract class FunctionOptimization {

	protected Function function;
	
	protected double[] optimalInput;
	protected double optimalValue;
	protected int noIterations;
	protected boolean converged;
	
	protected final int maxNoIterations = 10000;
	
	public FunctionOptimization(Function f)
	{
		function = f;
	}
	
	public abstract void optimize();
	
	public Function getFunction()
	{
		return function;
	}
	
	public int getMaxNoIters()
	{
		return maxNoIterations;
	}
	
	public double[] getOptimalIntput()
	{
		return optimalInput;
	}
	
	public double getOptimalValue()
	{
		return optimalValue;
	}
	
	public int getNoIterations()
	{
		return noIterations;
	}
	
	public boolean isConverged()
	{
		return converged;
	}
}
