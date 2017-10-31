package models;

public class Initialize {
	public static void trainAndValidate(int modelClass, int modelNo, double[] pars,int periods,int pm, double[] td,double[] vd)
	{
		switch(modelClass){
		case 0:{
			double[] data = new double[td.length+vd.length];
			
			for(int idx=0;idx<td.length;++idx)
				data[idx] = td[idx];
			
			for(int idx=0;idx<vd.length;++idx)
				data[td.length+idx] = td[idx];
			
			double[] estimates = models.ExponentialSmoothing.runES(modelNo,pars,periods,data);
			double[] estimatesVal = new double[vd.length];
			
			for(int idx=0;idx<vd.length;++idx)
				estimatesVal[idx] = estimates[td.length+idx];
			
			double performance = models.Performance.validate(pm,vd,estimatesVal);
			
			updateBest(performance,estimatesVal,pars);
			
			break;
		}
		default:{
			System.out.println("Error (trainAndValidate): default case reached");
		}
		}
	}
	
	private static void updateBest(double perf,double[] estVals, double[] pars)
	{
		if( ( optimization.Initialize.performance < 0) || (optimization.Initialize.performance > perf) )
		{
			optimization.Initialize.performance = perf;
			optimization.Initialize.estVals = estVals;
			optimization.Initialize.optPars = pars;
		}
	}
}
