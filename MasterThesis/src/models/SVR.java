package models;

import java.util.ArrayList;
import java.util.Random;

import input.Data;
import math.Matrix;

public class SVR extends Model {
	
	private double[][] x_train;
	private double[][] x_validate;
	private double[][] x_test;
	private double[][] y_train;
	private double[][] y_validate;
	private double[][] y_test;
	private int N_train;
	private int N_validate;
	private int N_test;
	
	private double[][] Kernel;
	private int[][] KernelOrdering;
	private double[][] alpha;
	private double[][] alpha_ast;
	private double error;
	private int noIters;
	private double[] bias;
	
	private final double stoppingError = 0.001;

	public SVR(Data dataset, int[] periods, String[] cats, int s)
	{
		super(dataset, periods, cats);
		name = "SVR";
		noParameters = 3;
		noConstants = 3;
		noOutputs = 1;
		bias = new double[noOutputs];
		alpha = new double[noOutputs][];
		alpha_ast = new double[noOutputs][];
		r = new Random(s);
	}

	public boolean train(boolean bootstrap)
	{
		initializeXY();
		if(bootstrap) bootstrap();
		initializeKernel();
		
		for(int idx=0;idx<noOutputs;++idx)
		{
			SMO smo = new SMO(N_train);
			
			if(smo.optimize(idx))
			{
				bias[idx] = smo.getBias();
				alpha[idx] = smo.getAlpha();
				alpha_ast[idx] = smo.getAlphaAst();
			}
			else return false;	
		}

		forecast();
		return true;
	}

	public double[][] getXtrain()
	{
		return x_train;
	}

	public double[][] getYtrain()
	{
		return y_train;
	}

	public int getNtrain()
	{
		return N_train;
	}
	
	public double[][] getXvalidate()
	{
		return x_validate;
	}

	public double[][] getYvalidate()
	{
		return y_validate;
	}

	public int getNvalidate()
	{
		return N_validate;
	}
	
	public double[][] getXtest()
	{
		return x_test;
	}

	public double[][] getYtest()
	{
		return y_test;
	}

	public int getNtest()
	{
		return N_test;
	}

	public double[][] getAlpha()
	{
		return alpha;
	}
	
	public double[][] getAlphaAst()
	{
		return alpha_ast;
	}
	
	public double getError()
	{
		return error;
	}
	
	public int getNoIters()
	{
		return noIters;
	}
	
	public double[] getBias()
	{
		return bias;
	}

	public void printAlpha()
	{
		for(int idx=0;idx<N_train;++idx)
			System.out.printf("%f\t",alpha[idx]);

		System.out.println();
	}
	
	public void printAlphaAst()
	{
		for(int idx=0;idx<N_train;++idx)
			System.out.printf("%f\t",alpha_ast[idx]);

		System.out.println();
	}
	
	private void initializeKernel()
	{
		Kernel = new double[N_train][N_train];
		KernelOrdering = new int[N_train][N_train-1];
		
		for(int idx1=1;idx1<Kernel.length;++idx1)
		{
			for(int idx2=0;idx2<idx1;++idx2) {Kernel[idx1][idx2] = kernel(x_train[idx1],x_train[idx2]); Kernel[idx2][idx1] = Kernel[idx1][idx2];}
			Kernel[idx1][idx1] = 1;
		}
		
		for(int idx1=0;idx1<Kernel.length;++idx1)
		{
			int cnt = 0;
			for(int idx2=0;idx2<Kernel[idx1].length;++idx2) {if(idx2 != idx1) {KernelOrdering[idx1][cnt] = idx2; cnt++; } }
			
			for(int idx2=1;idx2<KernelOrdering[idx1].length;++idx2)
			{
				for(int idx3=idx2-1;idx3>=0;--idx3)
				{
					if(Kernel[idx1][KernelOrdering[idx1][idx3+1]] > Kernel[idx1][KernelOrdering[idx1][idx3]])
					{
						int temp = KernelOrdering[idx1][idx3+1];
						KernelOrdering[idx1][idx3+1] = KernelOrdering[idx1][idx3];
						KernelOrdering[idx1][idx3] = temp;
					}
					else break;
				}
			}

		}
	}
	
	private void bootstrap()
	{
		int[] idcs = new int[N_train];
		for(int idx=0;idx<idcs.length;++idx) idcs[idx] = r.nextInt(N_train);
		
		double[][] x_bootstrap = new double[idcs.length][x_train[0].length];
		double[] y_bootstrap = new double[idcs.length];
		
		for(int idx1=0;idx1<idcs.length;++idx1)
		{
			y_bootstrap[idx1] = y_train[idcs[idx1]][0];
			for(int idx2=0;idx2<x_bootstrap[idx1].length;++idx2) x_bootstrap[idx1][idx2] = x_train[idcs[idx1]][idx2];
		}
		
		for(int idx1=0;idx1<x_train.length;++idx1)
		{
			y_train[idx1][0] = y_bootstrap[idx1];
			for(int idx2=0;idx2<x_bootstrap[idx1].length;++idx2) x_train[idx1][idx2] = x_bootstrap[idx1][idx2];
		}
	}

	private void initializeXY()
	{
		int index = data.getIndexFromCat(category[0]);
		
		N_train = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index] - Math.max((int) (constants[2]),(int) (constants[1])*12) - noPersAhead[0] + 1;
		x_train = new double[N_train][((int) (constants[2]) + (int) (constants[1])) * category.length];
		y_train = new double[N_train][1];
		
		N_validate = data.getTestingFirstIndex()[index] - data.getValidationFirstIndex()[index];
		x_validate = new double[N_validate][((int) (constants[2]) + (int) (constants[1])) * category.length];
		y_validate = new double[N_validate][1];
		
		N_test = data.getNoObs() - data.getTestingFirstIndex()[index];
		x_test = new double[N_test][((int) (constants[2]) + (int) (constants[1])) * category.length];
		y_test = new double[N_test][1];

		for(int idx1=0;idx1<N_train;++idx1)
		{
			index = data.getIndexFromCat(category[(int)constants[0]]);
			y_train[idx1][0] = data.getVolumes()[idx1 + data.getTrainingFirstIndex()[index] + Math.max((int) (constants[2]),(int) (constants[1])*12) + noPersAhead[0] - 1][index];
							
			for(int idx2=0;idx2<x_train[idx1].length;++idx2)
			{
				int lag;
				if( ( (int)(constants[1]) == 1) && (idx2 % ((int)(constants[2]) + (int)(constants[1]) ) == ((int)(constants[2]) + (int)(constants[1]) -1) ) )
					lag = 0;
				else
					lag = (int)(constants[1])*(12 - (int)(constants[2])) + idx2 % ((int)(constants[2]) + (int)(constants[1]) );
				
				index = data.getIndexFromCat(category[idx2 / ( (int)(constants[2]) + (int)(constants[1]) ) ] );
				x_train[idx1][idx2] = data.getVolumes()[data.getTrainingFirstIndex()[index] + idx1 + lag][index];
			}
		}

		for(int idx1=0;idx1<N_validate;++idx1) 
		{
			index = data.getIndexFromCat(category[(int)constants[0]]);
			y_validate[idx1][0] = data.getVolumes()[idx1 + data.getValidationFirstIndex()[index]][index];
						
			for(int idx2=0;idx2<x_validate[idx1].length;++idx2)
			{
				int lag;
				if( ( (int)(constants[1]) == 1) && (idx2 % ((int)(constants[2]) + (int)(constants[1]) ) == ((int)(constants[2]) + (int)(constants[1]) -1) ) )
					lag = 0;
				else
					lag = (int)(constants[1])*(12 - (int)(constants[2])) + idx2 % ((int)(constants[2]) + (int)(constants[1]) );
				
				index = data.getIndexFromCat(category[idx2 / ( (int)(constants[2]) + (int)(constants[1]) ) ] );
				x_validate[idx1][idx2] =  data.getVolumes()[idx1 + lag + N_train][index];
			}
		}
		
		for(int idx1=0;idx1<N_test;++idx1) 
		{
			index = data.getIndexFromCat(category[(int)constants[0]]);
			y_test[idx1][0] = data.getVolumes()[idx1 + data.getTestingFirstIndex()[index]][index];
			
			
			for(int idx2=0;idx2<x_test[idx1].length;++idx2)
			{
				int lag;
				if( ( (int)(constants[1]) == 1) && (idx2 % ((int)(constants[2]) + (int)(constants[1]) ) == ((int)(constants[2]) + (int)(constants[1]) -1) ) )
					lag = 0;
				else
					lag = (int)(constants[1])*(12 - (int)(constants[2])) + idx2 % ((int)(constants[2]) + (int)(constants[1]) );
				
				index = data.getIndexFromCat(category[idx2 / ( (int)(constants[2]) + (int)(constants[1]) ) ] );
				x_test[idx1][idx2] =  data.getVolumes()[idx1 + lag + N_train + N_validate][index];
			}
		}
	}
	
	private void forecast()
	{
		int index = data.getIndexFromCat(category[(int)constants[0]]);
		int firstIndex = Math.max(12*(int)constants[1],(int)constants[2])+noPersAhead[0]-1;
		initializeSet();
		
		for(int idx2=0;idx2<firstIndex;++idx2) trainingForecast[0][idx2] = data.getVolumes()[data.getTrainingFirstIndex()[index]+idx2][index];
		
		for(int idx2=0;idx2<N_train;++idx2)
		{
			trainingForecast[0][firstIndex+idx2] = bias[0];
			
			for(int idx3=0;idx3<x_train.length;++idx3)
				trainingForecast[0][firstIndex+idx2] += (alpha[0][idx3] - alpha_ast[0][idx3]) * kernel(x_train[idx2],x_train[idx3]);
		}
		
		for(int idx2=0;idx2<N_validate;++idx2)
		{
			validationForecast[0][idx2] = bias[0];
			
			for(int idx3=0;idx3<x_train.length;++idx3)
				validationForecast[0][idx2] += (alpha[0][idx3] - alpha_ast[0][idx3]) * kernel(x_train[idx3],x_validate[idx2]);
		}
		
		for(int idx2=0;idx2<N_test;++idx2)
		{
			testingForecast[0][idx2] = bias[0];
			
			for(int idx3=0;idx3<x_train.length;++idx3)
				testingForecast[0][idx2] += (alpha[0][idx3] - alpha_ast[0][idx3]) * kernel(x_train[idx3],x_test[idx2]);
		}
		
		forecasted[0] = true;
	}
	
	private void initializeSet()
	{
		trainingReal = new double[1][]; trainingForecast = new double[1][]; trainingDates = new String[1][];
		validationReal = new double[1][]; validationForecast = new double[1][]; validationDates = new String[1][];
		testingReal = new double[1][]; testingForecast = new double[1][]; testingDates = new String[1][];
		
		trainingReal[0] = data.getTrainingSet(category[(int)constants[0]]);
		trainingForecast[0] = new double[trainingReal[0].length];
		trainingDates[0] = data.getTrainingDates(category[(int)constants[0]]);

		validationReal[0] = data.getValidationSet(category[(int)constants[0]]);
		validationForecast[0] = new double[validationReal[0].length];
		validationDates[0] = data.getValidationDates(category[(int)constants[0]]);

		testingReal[0] = data.getTestingSet(category[(int)constants[0]]);
		testingForecast[0] = new double[testingReal[0].length];
		testingDates[0] = data.getTestingDates(category[(int)constants[0]]);
	}
	
	/*private void calculateBias()
	{
		double sumErrors = 0;
		
		for(int idx1=0;idx1<N_train;++idx1)
		{
			sumErrors += y_train[idx1][0];
			
			for(int idx2=0;idx2<N_train;++idx2)
			{
				sumErrors  -= (alpha[idx2] - alpha_ast[idx2]) * Kernel[idx2][idx1];
			}
		}
		
		bias = sumErrors / N_train;
	}

	private boolean ellipsoidMethod()
	{
		double[] center = initializeCenter();
		double[][] ellipsMatrix = initializeEllipsMatrix(center);
		double bestValue = 0;
		double[] bestCenter = new double[N_train];
		boolean firstValue = true;
		boolean stop = false;
		int iter = 0;
		double normCoeff = 0;

		while(!stop)
		{
			iter ++;
			double[] subDifferential;
			double alpha = 0;
			int infeasibleConstraint = checkFeasible(center);

			if(infeasibleConstraint == 0)
			{
				subDifferential = new double[N_train];

				for(int idx1=0;idx1<N_train;++idx1)
				{
					double sign = center[idx1] / Math.abs(center[idx1]);
					subDifferential[idx1] = parameters[1] * sign - y_train[idx1][0];

					for(int idx2=0;idx2<N_train;++idx2)
						subDifferential[idx1] += (center[idx2] * kernel(x_train[idx1],x_train[idx2]) );
				}

				normCoeff = Matrix.innerProduct(subDifferential,Matrix.innerProduct(ellipsMatrix,subDifferential) );
				normCoeff = 1 / Math.sqrt(normCoeff);
				subDifferential = Matrix.scalarMultiplication(normCoeff,subDifferential);

				double currentValue = objective(center);

				if( firstValue || (currentValue < bestValue) )
				{
					bestValue = currentValue;
					bestCenter = new double[N_train];

					for(int idx=0;idx<N_train;++idx)
						bestCenter[idx] = center[idx];

					firstValue = false;
				}

				alpha = (currentValue - bestValue) * normCoeff;
			}
			else
			{
				subDifferential = subDiffConstraint(infeasibleConstraint);
				normCoeff = Matrix.innerProduct(subDifferential,Matrix.innerProduct(ellipsMatrix,subDifferential) );
				normCoeff = 1 / Math.sqrt(normCoeff);
				subDifferential = Matrix.scalarMultiplication(normCoeff,subDifferential);
				alpha = currentConstraint(infeasibleConstraint,center) * normCoeff;
			}

			double scalar1 = (1 + N_train * alpha) / (N_train + 1);
			double scalar2 = Math.pow(N_train,2) / (Math.pow(N_train,2) - 1) * (1 - Math.pow(alpha,2) );
			double scalar3 = ( (2*(1+N_train*alpha) ) / ( (N_train+1) * (1+alpha) ) );

			double[] ellMat_subDiff = Matrix.innerProduct(ellipsMatrix,subDifferential);
			center = Matrix.difference(center,Matrix.scalarMultiplication(scalar1,ellMat_subDiff) );
			ellipsMatrix = Matrix.scalarMultiplication(scalar2,(Matrix.difference(ellipsMatrix,Matrix.scalarMultiplication(scalar3,Matrix.outerProduct(ellMat_subDiff,ellMat_subDiff) ) ) ) );

			if( ( (1/normCoeff) < stoppingError) && !firstValue || iter == maxIters )
				stop = true;
		}

		if(iter == maxIters)
		{
			System.out.println("Warning (ellipsoid method): not converged");
			return false;
		}

		error = 1/normCoeff;
		noIters = iter;
		lambda = bestCenter;
		
		return true;
	}*/

	private double objective(double[] input)
	{
		double output = 0;

		for(int idx1=0;idx1<N_train;++idx1)
		{
			output += parameters[1] * Math.abs(input[idx1]) - y_train[idx1][0] * input[idx1] + 0.5 * Math.pow(input[idx1] , 2);

			for(int idx2=0;idx2<idx1;++idx2)
				output += input[idx1] * input[idx2] * kernel(x_train[idx1],x_train[idx2]);
		}

		return output;
	}

	private double[] subDiffConstraint(int no)
	{
		double[] output = new double[N_train];

		if(no == (N_train+1) )
		{
			for(int idx=0;idx<N_train;++idx)
				output[idx] = 1;
		}
		else if(no == (-N_train-1) )
		{
			for(int idx=0;idx<N_train;++idx)
				output[idx] = -1;
		}
		else if(no>0)
			output[no-1] = 1;
		else
			output[-no-1] = -1;

		return output;
	}

	private double currentConstraint(int no,double[] center)
	{
		if(no == (N_train+1) )
		{
			double output = -stoppingError;

			for(int idx=0;idx<N_train;++idx)
				output += center[idx];

			return output;
		}
		else if(no == (-N_train-1))
		{
			double output = -stoppingError;

			for(int idx=0;idx<N_train;++idx)
				output -= center[idx];

			return output;
		}
		else if(no>0)
			return (center[no-1] - parameters[0]);
		else
			return (-center[-no-1] - parameters[0]);
	}

	private double kernel(double[] first,double[] second)
	{
		double[] diff = Matrix.difference(first,second);

		double k = Math.exp( -Matrix.innerProduct(diff, diff) / (2 * parameters[2] ) );

		if(Double.isInfinite(k))
			return Double.MAX_VALUE;
		else
			return k;
	}

	private int checkFeasible(double[] c)
	{
		double sum = 0;

		for(int idx=0;idx<N_train;++idx)
		{
			if(c[idx] > parameters[0])
				return (idx+1);

			if(c[idx] < (-parameters[0]) )
				return -(idx+1);

			sum += c[idx];
		}

		if(sum > stoppingError)
			return N_train+1;

		if(sum < (-stoppingError) )
			return -N_train-1;

		return 0;
	}

	private double[] initializeCenter()
	{
		double[] c = new double[N_train];
		double sum = 0;
		double boundary = parameters[0]/(N_train-1);
		Random r = new Random();

		for(int idx=0;idx<(N_train-1);++idx)
		{
			c[idx] = boundary * (2 * r.nextDouble() - 1);
			sum += c[idx];
		}

		c[N_train-1] = -sum;
		return c;
	}

	private double[][] initializeEllipsMatrix(double[] c)
	{
		double[][] P = new double[N_train][N_train];
		double maxRadius = N_train*Math.pow(2*parameters[0],2);

		for(int idx=0;idx<N_train;++idx)
		{
			P[idx][idx] = maxRadius;
		}

		return P;
	}
	
	private class SMO
	{
		private final int maxIters = 50000;
		private double[] alph;
		private double[] alph_ast;
		private double bias;
		private final double tol = 0.001;
		private final double eta_tol = 0.000001;
		
		private ArrayList<Integer> nonBound;
		private ArrayList<Integer> nonBound_ast;
		
		public SMO(int noObs)
		{
			initializeAlpha(noObs);
			bias = 0;
			nonBound = new ArrayList<Integer>();
			nonBound_ast = new ArrayList<Integer>();
		}
		
		public boolean optimize(int index)
		{
			int numChanged = 0;
			boolean examineAll = true;
			int lpCnt = 0;
			
			while((numChanged > 0) || examineAll)
			{
				lpCnt++;
				
				//if(lpCnt > 10000) {System.out.println(parameters[0]);System.out.println(evaluateObjective(index,alph,alph_ast));Matrix.print(alph);Matrix.print(alph_ast);}
				if(lpCnt > maxIters) return false;
				//System.out.println(lpCnt);
				
				
				//System.out.println(numChanged);
				//if(lpCnt >5626) break;
				/*System.out.println(numChanged);
				System.out.println(parameters[0]);*/
				/*if(Double.isNaN(alph[0]))
						{
					Matrix.print(Kernel);
					
					return false;
						}
				
				*/
				
				numChanged = 0;
				
				if(examineAll) {for(int idx=0;idx<alph.length;++idx) numChanged += examineExample(index,idx);}
				else {for(int idx:nonBound) numChanged += examineExample(index,idx);}
				if(examineAll) examineAll = false;
				else if(numChanged == 0) examineAll = true;
				updateNonBound();
			}
			/*Matrix.print(alph);
			Matrix.print(alph_ast);
			System.out.println(bias);
			System.out.println(evaluateObjective(alph,alph_ast));*/
			return true;
		}
		
		public double[] getAlpha()
		{
			return alph;
		}
		
		public double[] getAlphaAst()
		{
			return alph_ast;
		}
		
		public double getBias()
		{
			return bias;
		}
		
		private int examineExample(int cat,int exampleNo)
		{
			double alpha2 = alph[exampleNo]; double alpha2_ast = alph_ast[exampleNo];
			double phi2 = calculateOutput(exampleNo) - y_train[exampleNo][cat];

			if( (phi2 > (parameters[1] + tol) && alpha2_ast < parameters[0]) || (phi2 < (parameters[1] - tol) && alpha2_ast > 0) 
					|| (phi2 > (-parameters[1]+tol) && alpha2 > 0) || (phi2 < (-parameters[1] - tol) && alpha2 < parameters[0])  )
			{
				int[] permuteNonBound = getNonBoundPermutation();
				if(permuteNonBound.length > 0)
				{
					int ex2 = secondChoiceHeuristic(exampleNo);
					if(takeStep(cat,ex2,exampleNo,alpha2,alpha2_ast,phi2) ) return 1;
				}
				
				for(int idx:permuteNonBound) 
				{
					if(takeStep(cat,idx,exampleNo,alpha2,alpha2_ast,phi2) ) return 1;
				}
				
				int[] permuteAll = getPermutation(alph.length);
				for(int idx:permuteAll) 
				{
					if(takeStep(cat,idx,exampleNo,alpha2,alpha2_ast,phi2) ) return 1;
				}
			}
			
			return 0;
		}
		
		private boolean takeStep(int cat,int exampleNo1,int exampleNo2,double alpha2,double alpha2_ast,double phi2)
		{
			if(exampleNo1 == exampleNo2) return false;
			
			double alpha1 = alph[exampleNo1];
			double alpha1_ast = alph_ast[exampleNo1]; 			
			double phi1 = calculateOutput(exampleNo1) - y_train[exampleNo1][cat];
			double eta = 2*Kernel[exampleNo1][exampleNo2] - Kernel[exampleNo1][exampleNo1] - Kernel[exampleNo2][exampleNo2];
			
			boolean case1 = true; boolean case2 = true; boolean case3 = true; boolean case4 = true; boolean finished = false;
			double alpha1old = alpha1; double alpha1old_ast = alpha1_ast;
			double alpha2old = alpha2; double alpha2old_ast = alpha2_ast;
			double delta_phi = phi2 - phi1;
			
			//System.out.printf("%f\t%f\t%f\t%f\t%f\n",delta_phi,alpha1,alpha1_ast,alpha2,alpha2_ast);
			boolean etanotzero;
			if(eta<-eta_tol) etanotzero = true; else etanotzero = false;
			
			double a1,a2;
			while(!finished)
			{
				//System.out.printf("%f\t%f\t%f\t%f\t%f\n",delta_phi,alpha1,alpha1_ast,alpha2,alpha2_ast);
				if( case1 && (alpha1 > 0 || (alpha1_ast == 0 && delta_phi > 0) ) && (alpha2 > 0 || (alpha2_ast == 0 && delta_phi < 0) ) )
				{
					//System.out.println("case 1");
					double[] LH = computeLH(1,alpha1,alpha2);
					if( (LH[1] - LH[0]) > tol)
					{
						//System.out.println(delta_phi);
						if(etanotzero){a2 = alpha2 + delta_phi/eta; a2 = Math.min(a2,LH[1]); a2 = Math.max(a2,LH[0]);}
						else
						{
							double[] LVec = new double[alph.length];
							double[] HVec = new double[alph.length];
							double[] LVec_ast = new double[alph.length];
							double[] HVec_ast = new double[alph.length];
							for(int idx=0;idx<LVec.length;++idx){LVec[idx] = alph[idx];HVec[idx] = alph[idx];LVec_ast[idx] = alph_ast[idx];HVec_ast[idx] = alph_ast[idx];}
							LVec[exampleNo2] = LH[0]; HVec[exampleNo2] = LH[1]; LVec[exampleNo1] -= (LH[0]-alph[exampleNo2]); HVec[exampleNo1] -= (LH[1]-alph[exampleNo2]);
							LVec_ast[exampleNo2] = alpha2_ast; HVec_ast[exampleNo2] = alpha2_ast; LVec_ast[exampleNo1] = alpha1_ast; HVec_ast[exampleNo1] = alpha1_ast;
							double LObj = evaluateObjective(cat,LVec,LVec_ast); double HObj = evaluateObjective(cat,HVec,HVec_ast);
							if(LObj > HObj + tol) a2 = LH[0]; else if(HObj > LObj + tol) a2 = LH[1]; else a2 = alpha2;
						}
						a1 = alpha1 - (a2 - alpha2);
						//if( (a1 > parameters[0]) || (a2 > parameters[0]) )  System.out.printf("case 1 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,parameters[0],a1,a2,alpha1,alpha2,eta,delta_phi);
						/*double[] vec1 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec1[idx]=alph[idx];
						double[] vec2 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec2[idx]=alph_ast[idx];
						double[] vec3 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec3[idx]=alph[idx];
						double[] vec4 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec4[idx]=alph_ast[idx];
						double[] vec5 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec5[idx]=alph[idx];
						double[] vec6 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec6[idx]=alph_ast[idx];
						
						vec1[exampleNo2] = alpha2;
						vec1[exampleNo1] = alpha1;
						vec3[exampleNo2] = a2;
						vec3[exampleNo1] = a1;
						vec5[exampleNo2] = 0;
						vec5[exampleNo1] = alpha1 - (vec5[exampleNo2] - alpha2);
						if( (evaluateObjective(cat,vec1,vec2) - evaluateObjective(cat,vec3,vec4)) >tol){
							System.out.printf("case 1 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,a1,a2,alpha1,alpha2,eta,delta_phi);
							System.out.println((calculateOutput(exampleNo2,alpha2,alpha2_ast)-y_train[exampleNo2][cat])-(calculateOutput(exampleNo1,alpha1,alpha1_ast)-y_train[exampleNo1][cat]));
							System.out.printf("%f\t%f\t%f\n",evaluateObjective(cat,vec1,vec2),evaluateObjective(cat,vec3,vec4),evaluateObjective(cat,vec5,vec6));
						}*/
						if(Math.abs(a2-alpha2) > tol) {alpha2 = a2; alpha1 = a1;} 
					}
					else finished = true;
					case1 = false;
				}
				else if( case2 && (alpha1 > 0 || (alpha1_ast == 0 && delta_phi > (2*parameters[1]) ) ) && (alpha2_ast > 0 || (alpha2 == 0 && delta_phi > (2*parameters[1]) ) ) )
				{
					//System.out.println("case 2");
					double[] LH = computeLH(2,alpha1,alpha2_ast);
					if( (LH[1] - LH[0]) > tol)
					{
						if(etanotzero){a2 = alpha2_ast - (delta_phi-2*parameters[1])/eta;a2 = Math.min(a2,LH[1]);a2 = Math.max(a2,LH[0]);}
						else
						{
							double[] LVec = new double[alph.length];
							double[] HVec = new double[alph.length];
							double[] LVec_ast = new double[alph.length];
							double[] HVec_ast = new double[alph.length];
							for(int idx=0;idx<LVec.length;++idx){LVec[idx] = alph[idx];HVec[idx] = alph[idx];LVec_ast[idx] = alph_ast[idx];HVec_ast[idx] = alph_ast[idx];}
							LVec_ast[exampleNo2] = LH[0]; HVec_ast[exampleNo2] = LH[1]; LVec[exampleNo1] += (LH[0]-alph_ast[exampleNo2]); HVec[exampleNo1] += (LH[1]-alph_ast[exampleNo2]);
							LVec[exampleNo2] = alpha2; HVec[exampleNo2] = alpha2; LVec_ast[exampleNo1] = alpha1_ast; HVec_ast[exampleNo1] = alpha1_ast;
							double LObj = evaluateObjective(cat,LVec,LVec_ast);double HObj = evaluateObjective(cat,HVec,HVec_ast);
							if(LObj > HObj + tol) a2 = LH[0]; else if(HObj > LObj + tol) a2 = LH[1]; else a2 = alpha2_ast;
						}
						a1 = alpha1 + (a2 - alpha2_ast); 
						//if( (a1 > parameters[0]) || (a2 > parameters[0]) ) System.out.printf("case 2 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,parameters[0],a1,a2,alpha1,alpha2_ast,eta,delta_phi);
						/*double[] vec1 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec1[idx]=alph[idx];
						double[] vec2 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec2[idx]=alph_ast[idx];
						double[] vec3 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec3[idx]=alph[idx];
						double[] vec4 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec4[idx]=alph_ast[idx];
						vec2[exampleNo2] = alpha2_ast;
						vec1[exampleNo1] = alpha1;
						vec4[exampleNo2] = a2;
						vec3[exampleNo1] = a1;
						if( (evaluateObjective(cat,vec1,vec2) - evaluateObjective(cat,vec3,vec4)) >tol){
							System.out.printf("case 2 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,a1,a2,alpha1,alpha2_ast,eta,delta_phi);
							System.out.println((calculateOutput(exampleNo2,alpha2,alpha2_ast)-y_train[exampleNo2][cat])-(calculateOutput(exampleNo1,alpha1,alpha1_ast)-y_train[exampleNo1][cat]));
							
							System.out.printf("%f\t%f\n",evaluateObjective(cat,vec1,vec2),evaluateObjective(cat,vec3,vec4));
						}*/
						if(Math.abs(a2-alpha2_ast) > tol) {alpha2_ast = a2; alpha1 = a1;} 
						//System.out.printf("%f\t%f\n",evaluateObjective(vec1,vec2),evaluateObjective(vec3,vec4));
					}
					else finished = true;
					case2 = false;
				}
				else if( case3 && (alpha1_ast > 0 || (alpha1 == 0 && delta_phi < (-2*parameters[1]) ) ) && (alpha2 > 0 || (alpha2_ast == 0 && delta_phi < (-2*parameters[1]) ) ) )
				{
					//System.out.println("case 3");
					double[] LH = computeLH(2,alpha1_ast,alpha2);
					if( (LH[1] - LH[0]) > tol)
					{
						if(etanotzero){a2 = alpha2 + (delta_phi+2*parameters[1])/eta; a2 = Math.min(a2,LH[1]); a2 = Math.max(a2,LH[0]);}
						else
						{
							double[] LVec = new double[alph.length];
							double[] HVec = new double[alph.length];
							double[] LVec_ast = new double[alph.length];
							double[] HVec_ast = new double[alph.length];
							for(int idx=0;idx<LVec.length;++idx){LVec[idx] = alph[idx];HVec[idx] = alph[idx];LVec_ast[idx] = alph_ast[idx];HVec_ast[idx] = alph_ast[idx];}
							LVec[exampleNo2] = LH[0]; HVec[exampleNo2] = LH[1]; LVec_ast[exampleNo1] += (LH[0]-alph[exampleNo2]); HVec_ast[exampleNo1] += (LH[1]-alph[exampleNo2]);
							LVec_ast[exampleNo2] = alpha2_ast; HVec_ast[exampleNo2] = alpha2_ast; LVec[exampleNo1] = alpha1; HVec[exampleNo1] = alpha1;
							double LObj = evaluateObjective(cat,LVec,LVec_ast);double HObj = evaluateObjective(cat,HVec,HVec_ast);
							if(LObj > HObj + tol) a2 = LH[0]; else if(HObj > LObj + tol) a2 = LH[1]; else a2 = alpha2;
						}
						a1 = alpha1_ast + (a2 - alpha2); if(Math.abs(a2-alpha2) > tol) {delta_phi -= eta*(a1-alpha1_ast); alpha2 = a2; alpha1_ast = a1;} 
						//if( (a1 > parameters[0]) || (a2 > parameters[0]) ) System.out.printf("case 3 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,parameters[0],a1,a2,alpha1_ast,alpha2,eta,delta_phi);
						/*double[] vec1 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec1[idx]=alph[idx];
						double[] vec2 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec2[idx]=alph_ast[idx];
						double[] vec3 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec3[idx]=alph[idx];
						double[] vec4 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec4[idx]=alph_ast[idx];
						vec1[exampleNo2] = alpha2;
						vec2[exampleNo1] = alpha1_ast;
						vec3[exampleNo2] = a2;
						vec4[exampleNo1] = a1;
						if( (evaluateObjective(cat,vec1,vec2) - evaluateObjective(cat,vec3,vec4)) >tol){
							System.out.printf("case 3 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,a1,a2,alpha1_ast,alpha2,eta,delta_phi);
							
							System.out.printf("%f\t%f\n",evaluateObjective(cat,vec1,vec2),evaluateObjective(cat,vec3,vec4));
						}*/
						if(Math.abs(a2-alpha2) > tol) {alpha2 = a2; alpha1_ast = a1;} 
						//System.out.printf("%f\t%f\n",evaluateObjective(vec1,vec2),evaluateObjective(vec3,vec4));
					}
					else finished = true;
					case3 = false;
				}
				else if( case4 && (alpha1_ast > 0 || (alpha1 == 0 && delta_phi < 0 ) ) && (alpha2_ast > 0 || (alpha2 == 0 && delta_phi > 0 ) ) )
				{
					//System.out.printf("case 4 %f %f %f %f %f %f\n",alpha1,alpha2,alpha1_ast,alpha2_ast,delta_phi,eta);
					double[] LH = computeLH(1,alpha1_ast,alpha2_ast);
					if( (LH[1] - LH[0]) > tol)
					{
						if(etanotzero){a2 = alpha2_ast - delta_phi/eta; a2 = Math.min(a2,LH[1]); a2 = Math.max(a2,LH[0]);}
						else
						{
							double[] LVec = new double[alph.length];
							double[] HVec = new double[alph.length];
							double[] LVec_ast = new double[alph.length];
							double[] HVec_ast = new double[alph.length];
							for(int idx=0;idx<LVec.length;++idx){LVec[idx] = alph[idx];HVec[idx] = alph[idx];LVec_ast[idx] = alph_ast[idx];HVec_ast[idx] = alph_ast[idx];}
							LVec_ast[exampleNo2] = LH[0]; HVec_ast[exampleNo2] = LH[1]; LVec_ast[exampleNo1] -= (LH[0]-alph_ast[exampleNo2]); HVec_ast[exampleNo1] -= (LH[1]-alph_ast[exampleNo2]);
							LVec[exampleNo2] = alpha2; HVec[exampleNo2] = alpha2; LVec[exampleNo1] = alpha1; HVec[exampleNo1] = alpha1;
							double LObj = evaluateObjective(cat,LVec,LVec_ast);double HObj = evaluateObjective(cat,HVec,HVec_ast);
							//Matrix.print(LVec_ast); Matrix.print(HVec_ast); System.out.printf("%f %f\n",LH[0],LH[1]);
							if(LObj > HObj + tol) a2 = LH[0]; else if(HObj > LObj + tol) a2 = LH[1]; else a2 = alpha2_ast;
						}
						a1 = alpha1_ast - (a2 - alpha2_ast); 
						//if( (a1 > parameters[0]) || (a2 > parameters[0]) )  {System.out.printf("case 4 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,parameters[0],a1,a2,alpha1_ast,alpha2_ast,eta,delta_phi);  }
						
						/*double[] vec1 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec1[idx]=alph[idx];
						double[] vec2 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec2[idx]=alph_ast[idx];
						double[] vec3 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec3[idx]=alph[idx];
						double[] vec4 = new double[alph.length]; for(int idx=0;idx<alph.length;++idx) vec4[idx]=alph_ast[idx];
						vec2[exampleNo2] = alpha2_ast;
						vec2[exampleNo1] = alpha1_ast;
						vec4[exampleNo2] = a2;
						vec4[exampleNo1] = a1;
						if( (evaluateObjective(cat,vec1,vec2) - evaluateObjective(cat,vec3,vec4)) >tol){
							System.out.printf("case 4 %d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,a1,a2,alpha1_ast,alpha2_ast,eta,delta_phi);
							
							System.out.printf("%f\t%f\n",evaluateObjective(cat,vec1,vec2),evaluateObjective(cat,vec3,vec4));
						}*/
						if(Math.abs(a2-alpha2_ast) > tol) {alpha2_ast = a2; alpha1_ast = a1;} 
					}
					else finished = true;
					case4 = false;
					//delta_phi -= eta*( (alpha2-alpha2_ast) - (alph[exampleNo2] - alph_ast[exampleNo2]) );
				}
				else finished = true;
				delta_phi -= eta*( (alpha2-alpha2_ast) - (alph[exampleNo2] - alph_ast[exampleNo2]) );
			}
			
			//System.out.printf("%d\t%b\t%d\t%b\t%f\t%f\t%f\t%f\t%f\t\n",exampleNo1,ast1,exampleNo2,ast2,alpha1,alpha2,a1,a2,eta);
			//Matrix.print(Kernel);
			if( (Math.abs(alpha1-alpha1old)>tol) || (Math.abs(alpha1_ast-alpha1old_ast)>tol) || (Math.abs(alpha2-alpha2old)>tol) || (Math.abs(alpha2_ast-alpha2old_ast)>tol))
			{
				//System.out.printf("%d\t%d\t%f\t%f\t%f\t%f\n",exampleNo1,exampleNo2,alpha1,alpha1_ast,alpha2,alpha2_ast);
				/*double[] vec1 = alph;
				double[] vec2 = alph_ast;
				double[] vec3 = alph;
				double[] vec4 = alph_ast;
				vec3[exampleNo1] = alpha1;
				vec4[exampleNo1] = alpha1_ast;
				vec3[exampleNo2] = alpha2;
				vec4[exampleNo2] = alpha2_ast;
				if(evaluateObjective(vec1,vec2)>evaluateObjective(vec3,vec4)) System.out.printf("%f\t%f\n",evaluateObjective(vec1,vec2),evaluateObjective(vec3,vec4));
				*/
				
				bias = updateBias(cat,exampleNo1,exampleNo2,alpha1,alpha2,alpha1_ast,alpha2_ast);
				//if(evaluateObjective(vec1,vec2)>evaluateObjective(vec3,vec4)) System.out.printf("%f\t%f\n",evaluateObjective(vec1,vec2),evaluateObjective(vec3,vec4));
				
				alph_ast[exampleNo1] = alpha1_ast; alph[exampleNo1] = alpha1;
				alph_ast[exampleNo2] = alpha2_ast; alph[exampleNo2] = alpha2;
				return true;
			}
			else return false;
		}
		
		private double[] computeLH(int mode,double alpha1,double alpha2)
		{
			double[] output = new double[2];
			switch(mode)
			{
			case 1: output[0] = Math.max(0, alpha1 + alpha2 - parameters[0]); output[1] = Math.min(alpha1 + alpha2,parameters[0]); return output;
			case 2: output[0] = Math.max(0, alpha2 - alpha1); output[1] = Math.min(alpha2 - alpha1 + parameters[0],parameters[0]); return output;
			default: System.out.println("Error (computeLH): default case reached"); return null;
			}			
		}
		
		private int secondChoiceHeuristic(int ex1)
		{
			for(int idx=0;idx<KernelOrdering[ex1].length;++idx) {if(nonBound.contains(KernelOrdering[ex1][idx]) || nonBound_ast.contains(KernelOrdering[ex1][idx])) return idx; }
			return ex1;
		}
		
		private void updateNonBound()
		{
			nonBound.clear();
			nonBound_ast.clear();
			
			for(int idx=0;idx<alph.length;++idx) 
			{
				if( (alph[idx] > tol) && (alph[idx] < (parameters[0] - tol) ) ) nonBound.add(idx); 
				if( (alph_ast[idx] > tol) && (alph_ast[idx] < (parameters[0] - tol) ) ) nonBound_ast.add(idx); 
			}
		}
		
		private double updateBias(int cat,int ex1,int ex2,double newAlpha1,double newAlpha1_ast,double newAlpha2,double newAlpha2_ast)
		{
			if( (newAlpha1 > tol) && (newAlpha1 < (parameters[0]-tol) ) ) 
				return y_train[ex1][cat]-calculateOutput(ex1,newAlpha1,newAlpha1_ast)+bias-parameters[1]; 

			if( (newAlpha2 > tol) && (newAlpha2 < (parameters[0]-tol) ) ) 
				return y_train[ex2][cat]-calculateOutput(ex2,newAlpha2,newAlpha2_ast)+bias-parameters[1]; 
			
			if( (newAlpha1_ast > tol) && (newAlpha1_ast < (parameters[0]-tol) ) ) 
				return y_train[ex1][cat]-calculateOutput(ex1,newAlpha1,newAlpha1_ast)+bias+parameters[1]; 
			
			if( (newAlpha2_ast > tol) && (newAlpha2_ast < (parameters[0]-tol) ) ) 
				return y_train[ex2][cat]-calculateOutput(ex2,newAlpha2,newAlpha2_ast)+bias+parameters[1]; 

			double b1,b2;
			
			if( (newAlpha1 < tol) && (newAlpha2 < tol) )
			{
				b1 = y_train[ex1][cat]-calculateOutput(ex1,newAlpha1,newAlpha1_ast)+bias-parameters[1];
				b2 = y_train[ex2][cat]-calculateOutput(ex2,newAlpha2,newAlpha2_ast)+bias-parameters[1];
				return Math.max(b1,b2);
			}
			else if( (newAlpha1 < tol) && (newAlpha2_ast < tol) )
			{
				b1 = y_train[ex1][cat]-calculateOutput(ex1,newAlpha1,newAlpha1_ast)+bias-parameters[1];
				b2 = y_train[ex2][cat]-calculateOutput(ex2,newAlpha2,newAlpha2_ast)+bias-parameters[1];
				return 0.5*(b1+b2);
			}
			else if( (newAlpha1_ast < tol) && (newAlpha2 < tol) )
			{
				b1 = y_train[ex1][cat]-calculateOutput(ex1,newAlpha1,newAlpha1_ast)+bias-parameters[1];
				b2 = y_train[ex2][cat]-calculateOutput(ex2,newAlpha2,newAlpha2_ast)+bias-parameters[1];
				return 0.5*(b1+b2);
			}
			else if( (newAlpha1_ast < tol) && (newAlpha2_ast < tol) )
			{
				b1 = y_train[ex1][cat]-calculateOutput(ex1,newAlpha1,newAlpha1_ast)+bias-parameters[1];
				b2 = y_train[ex2][cat]-calculateOutput(ex2,newAlpha2,newAlpha2_ast)+bias-parameters[1];
				return Math.min(b1,b2);
			}
			else System.out.println("Error (updataBias): inconsistent alphas"); return 0;
		}
		
		private double calculateOutput(int exampleNo)
		{
			double output = bias;
			for(int idx=0;idx<N_train;++idx) output += (alph[idx] - alph_ast[idx]) * Kernel[idx][exampleNo];
			return output;
		}
		
		private double calculateOutput(int exampleNo,double alpha,double alpha_ast)
		{
			double output = bias + (alpha - alpha_ast);
			for(int idx=0;idx<N_train;++idx) {if(idx != exampleNo) output += (alph[idx] - alph_ast[idx]) * Kernel[idx][exampleNo];}
			return output;
		}
		
		private double evaluateObjective(int cat,double[] Alpha,double[] Alpha_ast)
		{
			double objective = 0;
			
			for(int idx1=0;idx1<Alpha.length;++idx1)
			{
				for(int idx2=0;idx2<Alpha.length;++idx2) objective -= 0.5*(Alpha[idx1]-Alpha_ast[idx1])*(Alpha[idx2]-Alpha_ast[idx2])*Kernel[idx1][idx2];
				objective -= parameters[1]*(Alpha[idx1]+Alpha_ast[idx1]);
				objective += y_train[idx1][cat]*(Alpha[idx1]-Alpha_ast[idx1]);
			}
			
			return objective;			
		}
		
		private int[] getNonBoundPermutation()
		{
			ArrayList<Integer> nb = new ArrayList<Integer>();
			
			for(int idx=0;idx<alph.length;++idx) {if(nonBound.contains(idx) || nonBound_ast.contains(idx)) nb.add(idx); }
			
			int[] output = new int[nb.size()];
			boolean[] used = new boolean[output.length];
			
			for(int idx1=0;idx1<output.length;++idx1)
			{
				int n = r.nextInt(output.length-idx1);
				int cnt = 0;
				int idx2 = 0;
				
				while(cnt<=n)
				{
					if(used[idx2]) idx2++; 
					else {cnt++;idx2++;}
				}
				
				--idx2;
				output[idx1] = nb.get(idx2);
				used[idx2] = true;
			}
			
			return output;
		}
		
		private int[] getPermutation(int max)
		{
			int[] output = new int[max];
			boolean[] used = new boolean[output.length];
			
			for(int idx1=0;idx1<output.length;++idx1)
			{
				int n = r.nextInt(output.length-idx1);
				int cnt = 0;
				int idx2 = 0;
				
				while(cnt<=n)
				{
					if(used[idx2]) idx2++; 
					else {cnt++;idx2++;}
				}
				
				--idx2;
				output[idx1] = idx2;
				used[idx2] = true;
			}
			
			return output;
		}
		
		private void initializeAlpha(int noObs)
		{
			alph = new double[noObs];
			alph_ast = new double[noObs];
		}
	}
}
