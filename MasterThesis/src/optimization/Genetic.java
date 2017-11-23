package optimization;

import performance.PerformanceMeasures;

public class Genetic extends Optimization {
	
	private final int popSizeMultiplyer = 5;
	private final int noEpochMultiplyer = 25;
	
	private int populationSize;
	private int noEpochs;
	
	private String[] parameterType;
	private int noParameters;
	private double[][] parameterBounds;
	
	private Member[] population;
	private boolean initialized;
	
	public Genetic(PerformanceMeasures pm, double[][] bounds,String[] parType)
	{
		super(pm,bounds);
		
		if(bounds.length != parType.length)
		{
			optimizationError("Genetic","illegal input variables");
		}
		
		noParameters = parType.length;
		populationSize = noParameters * popSizeMultiplyer;
		noEpochs = noParameters * noEpochMultiplyer;
		parameterType = parType;
		parameterBounds = bounds;
		initialized = initialize();
	}

	public boolean optimize(boolean silent) 
	{
		if(!initialized)
			return false;
		
		return true;
	}
	
	public void printPopulationFitness()
	{
		for(int idx=0;idx<population.length;++idx)
			System.out.println(population[idx].fitness);
	}
	
	public String[] getParameterTypes()
	{
		return parameterType;
	}
	
	public int getNoParameters()
	{
		return noParameters;
	}
	
	public double[][] getParameterBounds()
	{
		return parameterBounds;
	}

	public Member[] getPopulation()
	{
		return population;
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	private boolean initialize()
	{
		population = new Member[populationSize];
		
		for(int idx=0;idx<populationSize;++idx)
		{
			population[idx] = new Member(noParameters);
			
			if(!population[idx].initialize(parameterBounds,parameterType))
				return false;
		}
		
		sort(0,populationSize-1);
		return true;
	}
	
	private void sort(int indexL,int indexR)
	{
		if (indexR <= indexL) 
			return;

		int[] indices = partition(indexL,indexR);

		sort(indexL,indices[0]);
		sort(indices[1],indexR);		
	}
	
	private int[] partition(int indexL,int indexR)
	{
		int pivot = main.Run.r.nextInt(indexR-indexL+1)+indexL;
		double pivotValue = population[pivot].fitness;
		double tmpValue;
		swap(pivot,indexR);
		
		int[] indices = new int[2];
		int i = indexL;
		int k = indexL;
		int p = indexR;
		
		while(i < p)
		{
			tmpValue = population[i].fitness;			
			
			if(tmpValue < pivotValue)
			{
				swap(i,k);
				i++;
				k++;
			}
			else if(tmpValue == pivotValue)
			{
				p--;
				swap(i,p);
			}
			else
				i++;
		}
		
		indices[0] = k-1;
		indices[1] = indexR-(i-k)+1;
		int n = indexR;
		
		for(int idx = k;idx<p;idx++,n--)
			swap(idx,n);	
		
		return indices;
	}
	
	private void swap(int idx1,int idx2)
	{
		Member tmp = population[idx1];
		population[idx1] = population[idx2];
		population[idx2] = tmp;
	}
	
	private class Member
	{
		private Gene[] chromosome;
		private double fitness;
		
		public Member(int noGenes)
		{
			chromosome = new Gene[noGenes];
		}
		
		public boolean initialize(double[][] bounds,String[] types)
		{
			if( (bounds.length != chromosome.length) || (types.length != chromosome.length) )
			{
				optimizationError("Member","illegal input variables");
				return false;
			}
			
			for(int idx=0;idx<chromosome.length;++idx)
			{
				chromosome[idx] = new Gene(types[idx]);
				
				if(!chromosome[idx].initialize(bounds[idx]))
					return false;
			}
			
			return evaluateFitness();
		}
		
		public boolean evaluateFitness()
		{
			double[] parameters = new double[chromosome.length];
			
			for(int idx=0;idx<chromosome.length;++idx)
				parameters[idx] = chromosome[idx].getValue();
			
			measures.getModel().setParameters(parameters);
			
			if(!measures.getModel().train())
				return false;
			
			measures.calculateMeasures("validation");
			fitness = measures.getRMSE();
			
			return true;
		}
		
		public Gene[] getChromosome()
		{
			return chromosome;
		}
		
		public double getFitness()
		{
			return fitness;
		}
		
		private class Gene
		{
			private String type;
			private double value;
			
			public Gene(String t)
			{
				type = t;
			}
			
			public boolean initialize(double[] bounds)
			{
				if(type.equals("real"))
				{
					value = main.Run.r.nextDouble()*(bounds[1]-bounds[0]) + bounds[0];
					return true;
				}
				else if(type.equals("integer"))
				{
					value = main.Run.r.nextInt((int)bounds[1]-(int)bounds[0]) + (int)bounds[0];
					return true;
				}
				else
				{
					return false;
				}				
			}
			
			public String getType()
			{
				return type;
			}
			
			public double getValue()
			{
				return value;
			}
		}
	}
}
