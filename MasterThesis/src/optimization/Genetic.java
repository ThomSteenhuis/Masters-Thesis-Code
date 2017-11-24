package optimization;

import math.Matrix;
import performance.PerformanceMeasures;

public class Genetic extends Optimization {
	
	private final int popSizeMultiplyer = 20;
	private final int noOffspringMultiplyer = 2;
	private final int noEpochs = 1000;
	private final int neighborhoodSize = 20;
	private final double mutationProb = 0.2;
	private final double randomParentProb = 0.2;
	private final double mixingGeneProb = 0.5;
	
	private int populationSize;	
	private int noOffspring;
	
	private boolean[] integerType;
	private int noParameters;
	private double[][] parameterBounds;
	private boolean[] exponentialSteps;
	private double[] exponentialBase;
	
	private Member[] population;
	private boolean initialized;
	
	public Genetic(PerformanceMeasures pm, double[][] bounds,boolean[] intType,boolean[] expSteps,double[] expBase)
	{
		super(pm,bounds);
		
		if( (bounds.length != intType.length) || (bounds.length != expSteps.length) )
		{
			optimizationError("Genetic","illegal input variables");
		}
		
		name = "Genetic";
		noParameters = intType.length;
		populationSize = noParameters * popSizeMultiplyer;
		noOffspring = noParameters * noOffspringMultiplyer;
		integerType = intType;
		exponentialSteps = expSteps;
		exponentialBase = expBase;
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
			
			if(!silent)
			{
				if( ( ( (100*idx) % noEpochs ) < 100) && ( (100*idx) >= noEpochs ) && ( ( (100*idx) / noEpochs) < 100 ) )
				{
					System.out.printf("Completed %d%% of %d epochs\n",(100*idx) / noEpochs,noEpochs);
				}
			}	
		}
		
		return true;
	}
	
	public void printPopulationFitness()
	{
		for(int idx=0;idx<population.length;++idx)
			System.out.println(population[idx].fitness);
	}
	
	public boolean[] getIntegerType()
	{
		return integerType;
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
		
		if(exponentialSteps[idx])
			output = Math.pow(exponentialBase[idx],output);
			
		if(integerType[idx])
			return (int) output;
		else 
			return output;
	}
	
	private double crossover(Member parent1,Member parent2,int idx)
	{
		double v = main.Run.r.nextDouble();
		double w = main.Run.r.nextDouble();
		
		if(w < mixingGeneProb)
		{
			if(exponentialSteps[idx])
				return Math.pow(exponentialBase[idx],v*Math.log(parent1.getChromosome()[idx].getValue())/Math.log(exponentialBase[idx]) +(1-v)*Math.log(parent2.getChromosome()[idx].getValue())/Math.log(exponentialBase[idx]) );
			else
				return v*parent1.getChromosome()[idx].getValue() +(1-v)*parent2.getChromosome()[idx].getValue();
		}
		else if(w < 0.5 + 0.5*mixingGeneProb )
			return parent1.getChromosome()[idx].getValue();
		else
			return parent2.getChromosome()[idx].getValue();
	}
	
	private boolean epoch()
	{
		Member[] offsprings = new Member[noOffspring];
		
		for(int idx=0;idx<noOffspring;++idx)
		{
			int[] parents = selectParents();
			offsprings[idx] = createOffspring(population[parents[0]],population[parents[1]]);
			
			updateBest();			
			compare(offsprings[idx]);
		}
		
		return true;
	}
	
	private void compare(Member m)
	{
		int idx1=0;
		double f = m.getFitness();
		
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
			
			offspring.getChromosome()[idx] = new Gene(integerType[idx],geneValue);
		}
		
		offspring.evaluateFitness();
		offspring.localSearch();
		
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
			
			if(!population[idx].initialize(idx))
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
		
		public boolean initialize(int index)
		{			
			for(int idx=0;idx<chromosome.length;++idx)
			{
				chromosome[idx] = new Gene(integerType[idx]);
				
				if(!chromosome[idx].initialize(idx))
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
			
			return true;
		}
		
		public void setGenevalues(double[] genevals)
		{
			for(int idx=0;idx<noParameters;++idx)
				chromosome[idx].setValue(genevals[idx]);
		}
		
		public double[] getGenevalues()
		{
			double[] output = new double[noParameters];
			
			for(int idx=0;idx<noParameters;++idx)
				output[idx] = chromosome[idx].getValue();
			
			return output;
		}
		
		public Gene[] getChromosome()
		{
			return chromosome;
		}
		
		public double getFitness()
		{
			return fitness;
		}
		
		private void localSearch()
		{
			double best = fitness;
			double[] bestGenevalues = getGenevalues();			
			
			for(int idx1=0;idx1<noParameters;++idx1)
			{				
				for(int idx2=0;idx2<neighborhoodSize;++idx2)
				{
					double neighbor = main.Run.r.nextDouble()*(parameterBounds[idx1][1]-parameterBounds[idx1][0]) + parameterBounds[idx1][0];
					
					if(exponentialSteps[idx1])
						neighbor = Math.pow(exponentialBase[idx1],neighbor);
					
					if(integerType[idx1])
						neighbor = (int)neighbor;

					chromosome[idx1].setValue(neighbor);
					evaluateFitness();
					
					if(fitness > best)
					{
						best = fitness;
						bestGenevalues = getGenevalues();
					}
				}					
			}
			
			setGenevalues(bestGenevalues);
			evaluateFitness();
		}
	}
	
	private class Gene
	{
		private boolean integerType;
		private double value;
		
		public Gene(boolean t)
		{
			integerType = t;
		}
		
		public Gene(boolean t,double v)
		{
			integerType = t;
			value = v;
		}
		
		public boolean initialize(int idx)
		{
			value = main.Run.r.nextDouble()*(parameterBounds[idx][1]-parameterBounds[idx][0]) + parameterBounds[idx][0];

			if(exponentialSteps[idx])
				value = Math.pow(exponentialBase[idx],value);
			
			if(integerType)
				value = (int)value;
			
			return true;
		}
		
		public void setValue(double val)
		{
			value = val;
		}
		
		public double getValue()
		{
			return value;
		}
	}
}
