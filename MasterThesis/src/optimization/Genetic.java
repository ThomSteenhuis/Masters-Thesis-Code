package optimization;

import java.util.ArrayList;
import java.util.Random;

import math.Matrix;
import performance.PerformanceMeasures;

public class Genetic extends Optimization {

	private final int popSizeMultiplyer = 20;
	private final int noOffspringMultiplyer = 1;
	private final int maxNoEpochs = 1000;
	private final int maxNoEpochsNoImprovement = 50;
	private final int neighborhoodSize = 2;
	private final double mutationProb = 0.2;
	private final double randomParentProb = 0.2;
	private final double mixingGeneProb = 0.5;
	private final boolean localSearch = true;

	private int populationSize;
	private int noOffspring;

	private boolean[] integerType;
	private int noParameters;
	private double[][] parameterBounds;
	private boolean[] exponentialSteps;
	private double[] exponentialBase;
	private int noEpochsNoImprovement;

	private Member[] population;
	private boolean initialized;

	public Genetic(int s,PerformanceMeasures pm, double[][] bounds,boolean[] intType,boolean[] expSteps,double[] expBase)
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
		r = new Random(s);
	}

	public boolean optimize(boolean silent)
	{
		initialize();

		initialized = true;

		if(!silent) System.out.println("Genetic population initialized");

		for(int idx=0;idx<maxNoEpochs;++idx)
		{			
			printPopulationFitness();
			if(!epoch())
				return false;

			if(checkStoppingCriterium())
				break;

			if(!silent)
			{
				if( ( ( (100*idx) % maxNoEpochs ) < 100) && ( (100*idx) >= maxNoEpochs ) && ( ( (100*idx) / maxNoEpochs) < 100 ) )
				{
					System.out.printf("Completed %d%% of %d epochs\n",(100*idx) / maxNoEpochs,maxNoEpochs);
				}
			}
		}

		return true;
	}

	public void printPopulationFitness()
	{
		for(int idx=0;idx<population.length;++idx)
			System.out.println(population[idx].fitness);
		System.out.println();
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
		double output = r.nextDouble()*(parameterBounds[idx][1]-parameterBounds[idx][0])+parameterBounds[idx][0];

		if(exponentialSteps[idx])
			output = Math.pow(exponentialBase[idx],output);

		if(integerType[idx])
			return (int) output;
		else
			return output;
	}

	private double crossover(Member parent1,Member parent2,int idx)
	{
		double v = r.nextDouble();
		double w = r.nextDouble();

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
		boolean improvement = false;

		for(int idx=0;idx<noOffspring;++idx)
		{
			int[] parents = selectParents();
			offsprings[idx] = createOffspring(population[parents[0]],population[parents[1]]);

			if(compare(offsprings[idx])) {updateBest(); improvement = true;}
		}

		if(!improvement) noEpochsNoImprovement++; else noEpochsNoImprovement = 0;

		return true;
	}

	private boolean compare(Member m)
	{
		int idx1=0;
		double f = m.getFitness();

		while(f < population[idx1].fitness)
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

		if(idx1 == 0) return true; else return false;
	}

	private Member createOffspring(Member parent1,Member parent2)
	{
		Member offspring = new Member(noParameters);

		for(int idx=0;idx<noParameters;++idx)
		{
			double v = r.nextDouble();
			double geneValue;

			if(v < mutationProb)
				geneValue = mutation(idx);
			else
				geneValue = crossover(parent1,parent2,idx);

			offspring.getChromosome()[idx] = new Gene(integerType[idx],geneValue);
		}

		if(offspring.evaluateFitness())
		{
			if(localSearch) offspring.localSearch();
			return offspring;
		}
		else
			return createOffspring(parent1,parent2);
	}

	/*private int[] selectParents()
	{
		int[] parents = new int[2];
		double v = r.nextDouble();
		double w = r.nextDouble();
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
	}*/
	
	private int[] selectParents()
	{
		int[] parents = new int[2];
		ArrayList<Double> fitness = new ArrayList<Double>();
		for(int idx=0;idx<populationSize;++idx) fitness.add(population[idx].fitness);
		parents[0] = drawRandom(fitness);
		fitness.remove(parents[0]);
		parents[1] = drawRandom(fitness);
		if(parents[1] >= parents[0]) parents[1]++;
		return parents;
	}
	
	private int drawRandom(ArrayList<Double> list)
	{
		double sum = 0;
		for(double idx:list) sum+=idx;
		double w = r.nextDouble()*sum;
		boolean cont = true;
		sum = 0;
		int cnt = 0;
		while(cont) {sum += list.get(cnt); if(w <= sum) break; cnt++;}
		return cnt;
	}

	private void initialize()
	{
		population = new Member[populationSize];

		for(int idx=0;idx<populationSize;++idx)
		{
			population[idx] = new Member(noParameters);

			population[idx].initialize(idx);
		}

		sort(0,populationSize-1);
		updateBest();
	}

	private boolean checkStoppingCriterium()
	{
		if(noEpochsNoImprovement > maxNoEpochsNoImprovement) return true;
		else return false;
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
		int pivot = r.nextInt(indexR-indexL+1)+indexL;
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

		public void initialize(int index)
		{
			for(int idx=0;idx<chromosome.length;++idx)
			{
				chromosome[idx] = new Gene(integerType[idx]);
				chromosome[idx].initialize(idx);
			}

			if(!evaluateFitness())
				initialize(index);
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

			double currentMeasure = 0.001;
			for(int idx=0;idx<measures.getModel().getNoOutputs();++idx) currentMeasure += measures.getRMSE()[idx];

			fitness = 1/currentMeasure;

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
					double neighbor = r.nextDouble()*(parameterBounds[idx1][1]-parameterBounds[idx1][0]) + parameterBounds[idx1][0];

					if(exponentialSteps[idx1])
						neighbor = Math.pow(exponentialBase[idx1],neighbor);

					if(integerType[idx1])
						neighbor = (int)neighbor;

					chromosome[idx1].setValue(neighbor);

					if( evaluateFitness() && (fitness > best) )
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

		public void initialize(int idx)
		{
			value = r.nextDouble()*(parameterBounds[idx][1]-parameterBounds[idx][0]) + parameterBounds[idx][0];

			if(exponentialSteps[idx])
				value = Math.pow(exponentialBase[idx],value);

			if(integerType)
				value = (int)value;
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
