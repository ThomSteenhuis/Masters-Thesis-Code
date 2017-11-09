package experiments;

import java.util.ArrayList;

import optimization.Optimization;

public class Experiment {

	private ArrayList<Optimization> instances;
	private double[] runTimes;
	
	public Experiment()
	{
		instances = new ArrayList<Optimization>();
	}
	
	public void addInstance(Optimization instance)
	{
		instances.add(instance);
	}
	
	public void run(boolean silent)
	{
		runTimes = new double[instances.size()];
		
		for(int idx=0;idx<instances.size();++idx)
		{
			instances.get(idx).optimize(silent);
						
			if(!silent)
				System.out.printf("Completed experiment %d of %d\n",idx,instances.size());
		}
	}
	
	public ArrayList<Optimization> getInstances()
	{
		return instances;
	}
	
	public double[] getRunTimes()
	{
		return runTimes;
	}
}
