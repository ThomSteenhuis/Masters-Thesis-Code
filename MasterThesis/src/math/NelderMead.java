package math;

import java.util.Random;

public class NelderMead extends FunctionOptimization {

	public NelderMead(Function f)
	{
		super(f);
	}
	
	public void optimize(int maxNoIters) 
	{
		maxNoIterations = maxNoIters;
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
	
	private class Simplex{
		
		private int dimension;
		
		private double[][] xVectors;
		
		private int bestVector;
		private double bestValue;
		private int secondBestVector;
		private double secondBestValue;
		private int worstVector;
		private double worstValue;
		
		private final double alpha = -1;
		private final double beta = 0.5;
		private final double gamma = -2;
		private final double error = 0.001;
		
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
			
			double[] newVector = new double[dimension];
			
			for(int idx=0;idx<dimension;++idx)
				newVector[idx] = (1-alpha)*centroid[idx] + alpha*xVectors[worstVector][idx];
			
			double tempValue = function.evaluate(newVector);
			int mode = compareVector(tempValue);
			
			switch (mode)
			{
				case 0:
				{
					double[] newVector2 = new double[dimension];
					
					for(int idx=0;idx<dimension;++idx)
						newVector2[idx] = (1-gamma)*centroid[idx] + gamma*xVectors[worstVector][idx];
					
					tempValue = function.evaluate(newVector2);
					
					if(tempValue < bestValue)
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector2[idx];
					}
					else
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector[idx];
					}
					
					secondBestValue = bestValue;
					secondBestVector = bestVector;
					bestValue = tempValue;
					bestVector = worstVector;
					
					determineWorstVector();
					
					return checkStoppingCriterium();
				}
				case 1:
				{										
					for(int idx=0;idx<dimension;++idx)
						xVectors[worstVector][idx] = newVector[idx];
				
					secondBestValue = tempValue;
					secondBestVector = worstVector;
					
					determineWorstVector();
					
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
						
						determineOrder();
					}
					else
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector2[idx];
						
						determineWorstVector();
					}
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
						
						determineOrder();
					}
					else
					{
						for(int idx=0;idx<dimension;++idx)
							xVectors[worstVector][idx] = newVector2[idx];
						
						determineWorstVector();
					}
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
			bestVector = 0;
			bestValue = function.evaluate(xVectors[0]);
			
			double tempValue =  function.evaluate(xVectors[1]);
			
			if(tempValue < bestValue)
			{
				secondBestValue = bestValue;
				secondBestVector = 0;
				bestValue = tempValue;
				bestVector = 1;
			}
			else
			{
				secondBestValue = tempValue;
				secondBestVector = 1;
			}
			
			tempValue = function.evaluate(xVectors[2]);
			
			if(tempValue < bestValue)
			{
				worstValue = secondBestValue;
				worstVector = secondBestVector;
				secondBestValue = bestValue;
				secondBestVector = bestVector;
				bestValue = tempValue;
				bestVector = 2;
			}
			else if(tempValue < secondBestValue)
			{
				worstValue = secondBestValue;
				worstVector = secondBestVector;
				secondBestValue = tempValue;
				secondBestVector = 2;
			}
			else
			{
				worstValue = tempValue;
				worstVector = 2;
			}
			
			for(int idx=3;idx<=dimension;++idx)
				insertVector(idx,function.evaluate(xVectors[idx]) );
		}
		
		private void insertVector(int vectorIdx, double vectorVal)
		{
			if(vectorVal < bestValue)
			{
				worstValue = secondBestValue;
				worstVector = secondBestVector;
				secondBestValue = bestValue;
				secondBestVector = bestVector;
				bestValue = vectorVal;
				bestVector = vectorIdx;
			}
			else if(vectorVal < secondBestValue)
			{
				worstValue = secondBestValue;
				worstVector = secondBestVector;
				secondBestValue = vectorVal;
				secondBestVector = vectorIdx;
			}
			else if(vectorVal > worstValue)
			{
				worstValue = vectorVal;
				worstVector = vectorIdx;
			}
		}
		
		private int compareVector(double vectorValue)
		{
			if(vectorValue < bestValue)
				return 0;
			else if(vectorValue < secondBestValue)
				return 1;
			else if(vectorValue > worstValue)
				return 2;
			else
				return 3;
		}
		
		private boolean checkStoppingCriterium()
		{
			double stop = Matrix.infinityNorm(Matrix.difference(xVectors[0],xVectors[bestVector]));
			
			for(int idx=1;idx<=dimension;++idx)
			{
				double temp = Matrix.infinityNorm(Matrix.difference(xVectors[idx],xVectors[bestVector]));
				
				if(temp > stop)
					temp = stop;
			}
			
			if(stop <= error)
				return true;
			else 
				return false;
		}
	}
}
