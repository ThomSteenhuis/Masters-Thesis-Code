package optimization;

import performance.PerformanceMeasures;

public class Genetic extends Optimization {
	
	private final int popSizeMultiplyer = 10;
	private final int noEpochMultiplyer = 100;
	private final int noOffspring = 4;
	private final double mutationProb = 0.2;
	private final double randomParentProb = 0.2;	
	
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
		
		for(int idx=0;idx<noEpochs;++idx)
		{
			if(!epoch())
				return false;
		}
		
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
	
	private double mutation(int idx)
	{
		double output = main.Run.r.nextDouble()*(parameterBounds[idx][1]-parameterBounds[idx][0])+parameterBounds[idx][0];
		
		if(parameterType[idx].equals("integer"))
			return (int) output;
		else 
			return output;
	}
	
	private double crossover(Member parent1,Member parent2,int idx)
	{
		double v = main.Run.r.nextDouble();
		
		return v*parent1.getChromosome()[idx].getValue() +(1-v)*parent2.getChromosome()[idx].getValue();
	}
	
	private boolean epoch()
	{
		Member[] offsprings = new Member[noOffspring];
		
		for(int idx=0;idx<noOffspring;++idx)
		{
			int[] parents = selectParents();
			offsprings[idx] = createOffspring(population[parents[0]],population[parents[1]]);
			
			if(!offsprings[idx].evaluateFitness())
				return false;
			
			compare(offsprings[idx]);
		}
		
		return true;
	}
	
	private void compare(Member m)
	{
		int idx1=0;
		double f = m.fitness;
		
		while(f > population[idx1].fitness)
		{
			idx1++;
			
			if(idx1 == populationSize)
			{
				idx1--;
				break;
			}
		}		
		
		for(int idx2=populationSize-1;idx2>idx1;idx2--)
			population[idx2] = population[idx2-1];
		
		population[idx1] = m;
	}
	
	private Member createOffspring(Member parent1,Member parent2)
	{
		Member offspring = new Member(noParameters);
		
		for(int idx=0;idx<noParameters;++idx)
		{
			double v = main.Run.r.nextDouble();
			double geneValue;
			
			if(v < mutationProb)
				geneValue = mutation(idx);
			else
				geneValue = crossover(parent1,parent2,idx);
			
			offspring.getChromosome()[idx] = new Gene(parameterType[idx],geneValue);
		}
		
		return offspring;
	}
	
	private int[] selectParents()
	{
		int[] parents = new int[2];
		double v = main.Run.r.nextDouble();
		double w = main.Run.r.nextDouble();
		double b = 0.5;
		int idx = 0;
		
		if(w < randomParentProb)
		{
			while(idx < (populationSize - 1) )
			{
				if(v < ( (idx+1) / populationSize) )
					break;
				else
					idx++;	
			}
		}
		else
		{
			while(idx < (populationSize - 2 ) )
			{
				if(v > b)
					break;
				else
				{
					idx ++;
					b = b / 2;
				}
			}
		}
		
		parents[0] = idx;
		
		if(parents[0] == 0)
			idx = 1;
		else
			idx = 0;
		
		if(w < randomParentProb)
		{
			while(idx < (populationSize - 2) )
			{
				if(v < ( (idx+1) / (populationSize - 1)) )
					break;
				else
				{
					idx++;	
					
					if(idx == parents[0])
						idx++;
				}
			}
		}
		else
		{
			while(idx < (populationSize - 2 ) )
			{
				if(v > b)
					break;
				else
				{
					idx ++;
					b = b / 2;
					
					if(idx == parents[0])
						idx++;
				}
			}
		}
		
		
		if(idx == populationSize)
			idx = (populationSize - 2);
		
		parents[1] = idx;
		return parents;
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
			
			if(tmpValue > pivotValue)
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
			fitness = 1/measures.getRMSE();
			updateBest();
			
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
	}
	
	private class Gene
	{
		private String type;
		private double value;
		
		public Gene(String t)
		{
			type = t;
		}
		
		public Gene(String t,double v)
		{
			type = t;
			value = v;
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
		
		public double getValue()
		{
			return value;
		}
	}
}
