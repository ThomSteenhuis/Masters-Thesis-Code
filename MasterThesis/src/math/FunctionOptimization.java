package math;

public abstract class FunctionOptimization {

	protected Function function;
	
	protected int maxNoIterations;
	
	protected double[] optimalInput;
	protected double optimalValue;
	protected int noIterations;
	protected boolean converged;
	
	public FunctionOptimization(Function f)
	{
		function = f;
	}
	
	public abstract void optimize(int maxNoIters);
	
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
