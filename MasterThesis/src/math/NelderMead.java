package math;

import java.util.Random;

public class NelderMead extends FunctionOptimization {

	private Random r;
	
	public NelderMead(Function f, Random R)
	{
		super(f);
		r = R;
	}
	
	public boolean optimize() 
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
		
		noIterations = iter;
		optimalValue = S.getBestValue();
		optimalInput =  S.getBestVector();
		
		if(stop){ converged = true; return true;}
		else{ converged = false; return false;}
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
		private double[] xValues;
		
		/*private int bestVector;
		private double bestValue;
		private int secondWorstVector;
		private double secondWorstValue;
		private int worstVector;
		private double worstValue;*/
		
		private final double alpha = -1;
		private final double beta = 0.5;
		private final double gamma = -2;
		private final double error = 0.0001;
		
		public Simplex(int dim)
		{
			dimension = dim;
		}
		
		public void initialize()
		{
			double[] xBasis = new double[dimension];
			xVectors = new double[dimension+1][dimension];
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
			
			xValues = new double[dimension+1];
			
			for(int idx=0;idx<xValues.length;++idx)
				xValues[idx] = function.evaluate(xVectors[idx]);
			
			sort(0,dimension);
		}
		
		public boolean iterate()
		{
			double[] centroid = new double[dimension];
			
			for(int idx1=0;idx1<dimension;++idx1)
			{
				for(int idx2=0;idx2<dimension;++idx2) centroid[idx1] = centroid[idx1] + xVectors[idx2][idx1];
								
				centroid[idx1] = centroid[idx1] / dimension;
			}
			
			double[] newVector = new double[dimension];
			
			for(int idx=0;idx<dimension;++idx)
				newVector[idx] = (1-alpha)*centroid[idx] + alpha*xVectors[dimension][idx];
			
			double tempValue = function.evaluate(newVector);
			int mode = compareVector(tempValue);
			
			switch (mode)
			{
				case 0:
				{
					double[] newVector2 = new double[dimension];
					
					for(int idx=0;idx<dimension;++idx)
						newVector2[idx] = (1-gamma)*centroid[idx] + gamma*xVectors[dimension][idx];
					
					double tempValue2 = function.evaluate(newVector2);
					
					if(tempValue2 < xValues[0]) insertNewBestVector(newVector2,tempValue2);
					else insertNewBestVector(newVector,tempValue);
					
					return checkStoppingCriterium();
				}
				case 1:
				{										
					insertVector(newVector,tempValue);
					
					return checkStoppingCriterium();
				}
				case 2:
				{								
					double[] newVector2 = new double[dimension];
					
					for(int idx=0;idx<dimension;++idx)
						newVector2[idx] = (1-beta)*centroid[idx] + beta*xVectors[dimension][idx];
					
					tempValue = function.evaluate(newVector2);
					
					if(tempValue > xValues[dimension])
					{
						for(int idx1=1;idx1<=dimension;++idx1)
						{
							for(int idx2=0;idx2<dimension;++idx2)
								xVectors[idx1][idx2] = 0.5*(xVectors[idx1][idx2] + xVectors[0][idx2]);
							
							xValues[idx1] = function.evaluate(xVectors[idx1]);
						}
						sort(0,dimension);
					}
					else insertVector(newVector2,tempValue);
					
					return checkStoppingCriterium();
				}
				case 3:
				{								
					double[] newVector2 = new double[dimension];
					
					for(int idx=0;idx<dimension;++idx)
						newVector2[idx] = (1-beta)*centroid[idx] + beta*newVector[idx];
					
					tempValue = function.evaluate(newVector2);
					
					if(tempValue > xValues[dimension])
					{
						for(int idx1=1;idx1<=dimension;++idx1)
						{
							for(int idx2=0;idx2<dimension;++idx2)
								xVectors[idx1][idx2] = 0.5*(xVectors[idx1][idx2] + xVectors[0][idx2]);
							
							xValues[idx1] = function.evaluate(xVectors[idx1]);
						}
						sort(0,dimension);
					}
					else insertVector(newVector2,tempValue);
					
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
			return xValues[0];
		}
		
		public double[] getBestVector()
		{
			return xVectors[0];
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
			double pivotValue = xValues[pivot];
			double tmpValue;
			swap(pivot,indexR);
			
			int[] indices = new int[2];
			int i = indexL;
			int k = indexL;
			int p = indexR;
			
			while(i < p)
			{
				tmpValue = xValues[i];			
				
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
			double[] tmpVec = new double[dimension];
			
			for(int idx=0;idx<dimension;++idx) tmpVec[idx] = xVectors[idx1][idx];
			double tmpVal = xValues[idx1];
			
			for(int idx=0;idx<dimension;++idx) xVectors[idx1][idx] = xVectors[idx2][idx];
			xValues[idx1] = xValues[idx2];
			
			for(int idx=0;idx<dimension;++idx) xVectors[idx2][idx] = tmpVec[idx];
			xValues[idx2] = tmpVal;
		}
		
		/*private void determineOrder()
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
		}*/
		
		private void insertNewBestVector(double[] vector,double value)
		{
			for(int idx1=dimension;idx1>0;--idx1)
			{
				for(int idx2=0;idx2<dimension;++idx2) xVectors[idx1][idx2] = xVectors[idx1-1][idx2];
				xValues[idx1] = xValues[idx1-1];
			}
			
			for(int idx2=0;idx2<dimension;++idx2) xVectors[0][idx2] = vector[idx2];
			xValues[0] = value;
		}
		
		private void insertVector(double[] vector, double value)
		{
			int idx = 0;
			
			while( (value < xValues[idx]) && (idx < dimension) ) idx++;
			
			if( (idx < dimension) || (value < xValues[idx]) )
			{
				for(int idx1=dimension;idx1>idx;--idx1)
				{
					for(int idx2=0;idx2<dimension;++idx2) xVectors[idx1][idx2] = xVectors[idx1-1][idx2];
					xValues[idx1] = xValues[idx1-1];
				}
				
				for(int idx2=0;idx2<dimension;++idx2) xVectors[idx][idx2] = vector[idx2];
				xValues[idx] = value;
			}
		}
		
		private int compareVector(double vectorValue)
		{
			if(vectorValue < xValues[0])
				return 0;
			else if(vectorValue >= xValues[dimension])
				return 2;
			else if(vectorValue > xValues[dimension-1])
				return 3;
			else
				return 1;
		}
		
		private boolean checkStoppingCriterium()
		{
			double stop = 0;
			
			for(int idx=1;idx<=dimension;++idx)
			{
				double temp = Matrix.infinityNorm(Matrix.difference(xVectors[idx],xVectors[0]));
				
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
