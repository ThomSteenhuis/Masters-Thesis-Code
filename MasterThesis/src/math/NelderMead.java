package math;

import java.util.Random;

public class NelderMead extends FunctionOptimization {

	public NelderMead(Function f)
	{
		super(f);
	}
	
	public void optimize() 
	{
		Simplex S = new Simplex(function.getNoInputs() );
		S.initialize();
		
		int iter = 0;
		boolean stop = false;
		
		while((!stop) && (iter < maxNoIterations) )
		{
			stop = S.iterate();
			iter ++;
		}
		
		if(stop)
			converged = true;
		else
			converged = false;
		
		noIterations = iter;
		optimalValue = S.getBestValue();
		optimalInput =  S.getBestVector();
	}
	
	public void printSummary()
	{
		System.out.printf("Converged: %b\n",converged);
		System.out.printf("Iterations: %d\n", noIterations);
		System.out.printf("Optimal value: %f\n", optimalValue);
		
		System.out.print("Optimal input: ");
		for(int idx=0;idx<function.getNoInputs();++idx)
			System.out.printf("%f\t", optimalInput[idx]);
		
		System.out.println();
	}
	
	private class Simplex{
		
		private int dimension;
		
		private double[][] xVectors;
		
		private int bestVector;
		private double bestValue;
		private int secondWorstVector;
		private double secondWorstValue;
		private int worstVector;
		private double worstValue;
		
		private final double alpha = -1;
		private final double beta = 0.5;
		private final double gamma = -2;
		private final double error = 0.000001;
		
		public Simplex(int dim)
		{
			dimension = dim;
		}
		
		public void initialize()
		{
			double[] xBasis = new double[dimension];
			xVectors = new double[dimension+1][dimension];
			Random r = new Random();
			double eta = r.nextDouble();
			
			for(int idx=0;idx<dimension;++idx)
			{
				xBasis[idx] = 2 * r.nextDouble() - 1;
				xVectors[0][idx] = xBasis[idx];
			}
			
			for(int idx1=0;idx1<dimension;++idx1)
			{
				for(int idx2=0;idx2<dimension;++idx2)
					xVectors[idx1+1][idx2] = xBasis[idx2];
				
				xVectors[idx1+1][idx1] = xVectors[idx1][idx1] + eta;
			}
			
			double[] outputs = new double[dimension+1];
			
			for(int idx=0;idx<outputs.length;++idx)
				outputs[idx] = function.evaluate(xVectors[idx]);
			
			determineOrder();
		}
		
		public boolean iterate()
		{
			double[] centroid = new double[dimension];
			
			for(int idx1=0;idx1<dimension;++idx1)
			{
				for(int idx2=0;idx2<=dimension;++idx2)
				{
					if(idx2 != worstVector)
						centroid[idx1] = centroid[idx1] + xVectors[idx2][idx1];
				}
				
				centroid[idx1] = centroid[idx1] / dimension;
			}
			
			Matrix.print(centroid);
			
			double[] newVector = new double[dimension];
			
			for(int idx=0;idx<dimension;++idx)
				newVector[idx] = (1-alpha)*centroid[idx] + alpha*xVectors[worstVector][idx];
			
			Matrix.print(newVector);
			
			double tempValue = function.evaluate(newVector);
			
			System.out.printf("Value = %f\n", tempValue);
			int mode = compareVector(tempValue);
			
			System.out.printf("Mode = %d\n", mode);
			
			switch (mode)
			{
				case 0:
				{
					double[] newVector2 = new double[dimension];
					
					for(int idx=0;idx<dimension;++idx)
						newVector2[idx] = (1-gamma)*centroid[idx] + gamma*xVectors[worstVector][idx];
					
					Matrix.print(newVector2);
					double tempValue2 = function.evaluate(newVector2);
					System.out.printf("Value = %f\n",tempValue2);
					
					if(tempValue2 < bestValue)
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector2[idx];
						
						bestValue = tempValue2;
						bestVector = worstVector;
					}
					else
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector[idx];
						
						bestValue = tempValue;
						bestVector = worstVector;
					}
					
					Matrix.print(xVectors);
					
					determineOrder();
					
					System.out.printf("%f\t%f\t%f\n", bestValue,secondWorstValue,worstValue);
					System.out.printf("%d\t%d\t%d\n", bestVector,secondWorstVector,worstVector);
					
					return checkStoppingCriterium();
				}
				case 1:
				{										
					for(int idx=0;idx<dimension;++idx)
						xVectors[worstVector][idx] = newVector[idx];
					
					determineOrder();
					
					Matrix.print(xVectors);
					System.out.printf("%f\t%f\t%f\n", bestValue,secondWorstValue,worstValue);
					System.out.printf("%d\t%d\t%d\n", bestVector,secondWorstVector,worstVector);
					
					return checkStoppingCriterium();
				}
				case 2:
				{								
					double[] newVector2 = new double[dimension];
					
					for(int idx=0;idx<dimension;++idx)
						newVector2[idx] = (1-beta)*centroid[idx] + beta*xVectors[worstVector][idx];
					
					tempValue = function.evaluate(newVector2);
					
					if(tempValue > worstValue)
					{
						for(int idx1=0;idx1<dimension;++idx1)
						{
							for(int idx2=0;idx2<=dimension;++idx2)
								xVectors[idx2][idx1] = 0.5*(xVectors[idx2][idx1] + xVectors[bestVector][idx1]);
						}
					}
					else
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector2[idx];
					}
					
					determineOrder();
					
					Matrix.print(newVector2);
					System.out.printf("Value = %f\n", tempValue);
					Matrix.print(xVectors);
					System.out.printf("%f\t%f\t%f\n", bestValue,secondWorstValue,worstValue);
					System.out.printf("%d\t%d\t%d\n", bestVector,secondWorstVector,worstVector);
					
					return checkStoppingCriterium();
				}
				case 3:
				{								
					double[] newVector2 = new double[dimension];
					
					for(int idx=0;idx<dimension;++idx)
						newVector2[idx] = (1-beta)*centroid[idx] + beta*newVector[idx];
					
					tempValue = function.evaluate(newVector2);
					
					if(tempValue > worstValue)
					{
						for(int idx1=0;idx1<dimension;++idx1)
						{
							for(int idx2=0;idx2<=dimension;++idx2)
								xVectors[idx2][idx1] = 0.5*(xVectors[idx2][idx1] + xVectors[bestVector][idx1]);
						}
					}
					else
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector2[idx];
					}
					
					determineOrder();
					
					Matrix.print(newVector2);
					System.out.printf("Value = %f\n", tempValue);
					Matrix.print(xVectors);
					System.out.printf("%f\t%f\t%f\n", bestValue,secondWorstValue,worstValue);
					System.out.printf("%d\t%d\t%d\n", bestVector,secondWorstVector,worstVector);
					
					return checkStoppingCriterium();
				}
				default:
				{
					System.out.println("Error (iterate): default case reached");
					return true;
				}
			}
		}
		
		public double getBestValue()
		{
			return bestValue;
		}
		
		public double[] getBestVector()
		{
			return xVectors[bestVector];
		}
		
		private void determineWorstVector()
		{
			double worst = function.evaluate( xVectors[0]);
			int worstIdx = 0;
			
			for(int idx=1;idx<=dimension;++idx)
			{
				double temp = function.evaluate( xVectors[idx]);
				
				if(temp>worst)
				{
					worst = temp;
					worstIdx = idx;
				}
			}
			
			worstValue = worst;
			worstVector = worstIdx;
		}
		
		private void determineOrder()
		{
			worstVector = 0;
			worstValue = function.evaluate(xVectors[0]);
			
			double tempValue =  function.evaluate(xVectors[1]);
			
			if(tempValue > worstValue)
			{
				secondWorstValue = worstValue;
				secondWorstVector = 0;
				worstValue = tempValue;
				worstVector = 1;
			}
			else
			{
				secondWorstValue = tempValue;
				secondWorstVector = 1;
			}
			
			tempValue = function.evaluate(xVectors[2]);
			
			if(tempValue > worstValue)
			{
				bestValue = secondWorstValue;
				bestVector = secondWorstVector;
				secondWorstValue = worstValue;
				secondWorstVector = worstVector;
				worstValue = tempValue;
				worstVector = 2;
			}
			else if(tempValue > secondWorstValue)
			{
				bestValue = secondWorstValue;
				bestVector = secondWorstVector;
				secondWorstValue = tempValue;
				secondWorstVector = 2;
			}
			else
			{
				bestValue = tempValue;
				bestVector = 2;
			}
			
			for(int idx=3;idx<=dimension;++idx)
				insertVector(idx,function.evaluate(xVectors[idx]) );
		}
		
		private void insertVector(int vectorIdx, double vectorVal)
		{
			if(vectorVal < bestValue)
			{
				bestValue = vectorVal;
				bestVector = vectorIdx;
			}
			else if(vectorVal > worstValue)
			{
				secondWorstValue = worstValue;
				secondWorstVector = worstVector;
				worstValue = vectorVal;
				worstVector = vectorIdx;
				
			}
			else if(vectorVal > secondWorstValue)
			{
				secondWorstValue = vectorVal;
				secondWorstVector = vectorIdx;
			}
		}
		
		private int compareVector(double vectorValue)
		{
			if(vectorValue < bestValue)
				return 0;
			else if(vectorValue > worstValue)
				return 2;
			else if(vectorValue > secondWorstValue)
				return 3;
			else
				return 1;
		}
		
		private boolean checkStoppingCriterium()
		{
			double stop = Matrix.infinityNorm(Matrix.difference(xVectors[0],xVectors[bestVector]));
			
			for(int idx=1;idx<=dimension;++idx)
			{
				double temp = Matrix.infinityNorm(Matrix.difference(xVectors[idx],xVectors[bestVector]));
				
				if(temp > stop)
					stop = temp;
			}
			
			if(stop <= error)
				return true;
			else 
				return false;
		}
	}
}
