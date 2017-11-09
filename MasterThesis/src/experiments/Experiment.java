package experiments;

import java.util.ArrayList;

import optimization.Optimization;

public class Experiment {

	private ArrayList<Optimization> instances;
	private double[] runTimes;
	
	private String[][] outcomes;
	
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
			long startTime = System.currentTimeMillis();
			
			instances.get(idx).optimize(silent);
				
			long stopTime = System.currentTimeMillis();
			
			runTimes[idx] = (double) (stopTime - startTime) / 1000;
			
			instances.get(idx).getPerformanceMeasures().getModel().setParameters(instances.get(idx).getOptimalParameters());
			instances.get(idx).getPerformanceMeasures().getModel().train();
			instances.get(idx).getPerformanceMeasures().calculateMeasures();
			
			if(!silent)
				System.out.printf("Completed experiment %d of %d\n",idx+1,instances.size());
		}
		
		createOutcomes();
	}
	
	public ArrayList<Optimization> getInstances()
	{
		return instances;
	}
	
	public double[] getRunTimes()
	{
		return runTimes;
	}
	
	private void createOutcomes()
	{
		outcomes = new String[instances.size()+1][8];
		outcomes[0][0] = "Model name";
		outcomes[0][1] = "Machine name";
		outcomes[0][2] = "Optimization name";
		outcomes[0][3] = "Runtime (seconds)";
		outcomes[0][4] = "RMSE";
		outcomes[0][5] = "MAPE";
		outcomes[0][6] = "MAE";
		outcomes[0][7] = "ME";
		
		for(int idx=0;idx<instances.size();++idx)
		{
			outcomes[idx+1][0] = instances.get(idx).getPerformanceMeasures().getModel().getName();
			outcomes[idx+1][1] = instances.get(idx).getPerformanceMeasures().getModel().getCategory();
			outcomes[idx+1][2] = instances.get(idx).getName();
			outcomes[idx+1][3] = Double.toString(runTimes[idx]);
			outcomes[idx+1][4] = Double.toString(instances.get(idx).getPerformanceMeasures().getRMSE());
			outcomes[idx+1][5] = Double.toString(instances.get(idx).getPerformanceMeasures().getMAPE());
			outcomes[idx+1][6] = Double.toString(instances.get(idx).getPerformanceMeasures().getMEA());
			outcomes[idx+1][7] = Double.toString(instances.get(idx).getPerformanceMeasures().getME());
		}
	}
}
