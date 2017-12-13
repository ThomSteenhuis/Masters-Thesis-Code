package math;

public abstract class FunctionOptimization {

	protected Function function;
	
	protected double[] optimalInput;
	protected double optimalValue;
	protected int noIterations;
	protected boolean converged;
	
	protected int maxNoIterations;
	
	public FunctionOptimization(Function f,int maxIters)
	{
		function = f;
		maxNoIterations = maxIters;
	}
	
	public abstract boolean optimize();
	
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
