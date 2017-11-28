package models;

import java.util.ArrayList;
import java.util.Random;

import input.Data;
import math.Matrix;

public class SVR extends Model {
	
	private Random r;

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
	private double[] alpha;
	private double[] alpha_ast;
	private double error;
	private int noIters;
	private double bias;
	
	private final double stoppingError = 0.001;

	public SVR(Data dataset, int periods, Random R)
	{
		super(dataset, periods);
		name = "SVR";
		noParameters = 4;
		noConstants = 0;
		r = R;
	}

	public boolean train()
	{
		initializeXY();
		initializeKernel();
		SMO smo = new SMO(N_train);
		
		if(smo.optimize())
		{
			bias = smo.getBias();
			alpha = smo.getAlpha();
			alpha_ast = smo.getAlphaAst();
			forecast();
			return true;
		}
		else return false;		
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

	public double[] getAlpha()
	{
		return alpha;
	}
	
	public double[] getAlphaAst()
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
	
	public double getBias()
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
		
		for(int idx1=0;idx1<Kernel.length;++idx1)
		{
			for(int idx2=0;idx2<idx1;++idx2) {Kernel[idx1][idx2] = kernel(x_train[idx1],x_train[idx2]); Kernel[idx2][idx1] = Kernel[idx1][idx2];}
			Kernel[idx1][idx1] = 1;
		}
	}

	private void initializeXY()
	{
		int index = data.getIndexFromCat(category);
		
		N_train = data.getValidationFirstIndex()[index] - data.getTrainingFirstIndex()[index] - (int) (parameters[3]) - noPersAhead;
		x_train = new double[N_train][(int) (parameters[3])];
		y_train = new double[N_train][1];

		for(int idx1=0;idx1<N_train;++idx1)
		{
			y_train[idx1][0] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead - 1][index];

			for(int idx2=0;idx2<x_train[idx1].length;++idx2)
			{
				x_train[idx1][idx2] =  data.getVolumes()[idx1 + idx2][index];
			}
		}
		
		N_validate = data.getTestingFirstIndex()[index] - data.getValidationFirstIndex()[index];
		x_validate = new double[N_validate][(int) (parameters[3])];
		y_validate = new double[N_validate][1];

		for(int idx1=0;idx1<N_validate;++idx1)
		{
			y_validate[idx1][0] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead - 1 + N_train][index];

			for(int idx2=0;idx2<x_validate[idx1].length;++idx2)
			{
				x_validate[idx1][idx2] =  data.getVolumes()[idx1 + idx2 + N_train][index];
			}
		}
		
		N_test = data.getNoObs() - data.getTestingFirstIndex()[index];
		x_test = new double[N_test][(int) (parameters[3])];
		y_test = new double[N_test][1];

		for(int idx1=0;idx1<N_test;++idx1)
		{
			y_test[idx1][0] = data.getVolumes()[idx1 + (int) (parameters[3]) + noPersAhead - 1 + N_train + N_validate][index];

			for(int idx2=0;idx2<x_test[idx1].length;++idx2)
			{
				x_test[idx1][idx2] =  data.getVolumes()[idx1 + idx2 + N_train + N_validate][index];
			}
		}
	}
	
	private void forecast()
	{
		initializeSets();
		
		for(int idx=0;idx<N_train;++idx)
		{
			trainingForecast[idx] = bias;
			
			for(int idx2=0;idx2<x_train.length;++idx2)
				trainingForecast[idx] += (alpha[idx2] - alpha_ast[idx2]) * kernel(x_train[idx2],x_train[idx]);
		}
		
		for(int idx=0;idx<N_validate;++idx)
		{
			validationForecast[idx] = bias;
			
			for(int idx2=0;idx2<x_train.length;++idx2)
				validationForecast[idx] += (alpha[idx2] - alpha_ast[idx2]) * kernel(x_train[idx2],x_validate[idx]);
		}
		
		for(int idx=0;idx<N_test;++idx)
		{
			testingForecast[idx] = bias;
			
			for(int idx2=0;idx2<x_train.length;++idx2)
				testingForecast[idx] += (alpha[idx2] - alpha_ast[idx2]) * kernel(x_train[idx2],x_test[idx]);
		}
		
		trainingForecasted = true;
		validationForecasted = true;
		testingForecasted = true;
	}
	
	private void calculateBias()
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

	/*private boolean ellipsoidMethod()
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
		private double[] alph;
		private double[] alph_ast;
		private double bias;
		private final double tol1 = 0.001;
		private final double tol2 = 0.00000001;
		
		private ArrayList<Integer> nonBound;
		private ArrayList<Integer> nonBound_ast;
		
		public SMO(int noObs)
		{
			initializeAlpha(noObs);
			bias = 0;
			nonBound = new ArrayList<Integer>();
			nonBound_ast = new ArrayList<Integer>();
		}
		
		public boolean optimize()
		{
			int numChanged = 0;
			boolean examineAll = true;
			int cnt=0;
			
			while(numChanged > 0 || examineAll)
			{
				/*System.out.println(cnt);
				System.out.println(numChanged);
				System.out.println(parameters[0]);
	
				cnt++;*/
				/*if(Double.isNaN(alph[0]))
						{
					Matrix.print(Kernel);
					
					return false;
						}*/
				System.out.println(evaluateObjective(alph,alph_ast));
				Matrix.print(alph);
				Matrix.print(alph_ast);
				
				numChanged = 0;
				
				if(examineAll)
				{
					for(int idx=0;idx<alph.length;++idx) numChanged += examineExample(idx,false);
					for(int idx=0;idx<alph.length;++idx) numChanged += examineExample(idx,true);
				}
				else
				{
					for(int idx:nonBound) numChanged += examineExample(idx,false);  
					for(int idx:nonBound_ast) numChanged += examineExample(idx,true);
				}
				
				if(examineAll) examineAll = false;
				else if(numChanged == 0) examineAll = true;
				updateNonBound();
			}
			
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
		
		private int examineExample(int exampleNo,boolean ast)
		{
			//System.out.printf("%d\t%b\n",exampleNo,ast);
			double alpha2;
			if(ast) alpha2 = alph_ast[exampleNo]; else alpha2 = alph[exampleNo];
			
			double E2 = calculateOutput(exampleNo) - y_train[exampleNo][0];
			double err = parameters[1] + E2;
			if ( (err < -tol1 && alpha2 < parameters[1]) || (err > tol1 && alpha2 > 0) )
			{
				int[] permuteNonBound = getPermutation(nonBound.size()+nonBound_ast.size());
				for(int idx:permuteNonBound) 
				{
					if(idx >= nonBound.size()) {if(takeStep(nonBound_ast.get(idx-nonBound.size()),true,exampleNo,ast,alpha2,E2) ) return 1;} 
					else {if(takeStep(nonBound.get(idx),false,exampleNo,ast,alpha2,E2) ) return 1;} 
				}
				
				int[] permuteAll = getPermutation(2*alph.length);
				for(int idx:permuteAll) 
				{
					if(idx >= alph.length) {if(takeStep(idx-alph.length,true,exampleNo,ast,alpha2,E2) ) return 1;} 
					else {if(takeStep(idx,false,exampleNo,ast,alpha2,E2) ) return 1;} 
				}
			}
			return 0;
		}
		
		private boolean takeStep(int exampleNo1, boolean ast1,int exampleNo2, boolean ast2,double alpha2,double E2)
		{
			if(exampleNo1 == exampleNo2) return false;
			
			double alpha1,L,H,aux;
			if(ast1) alpha1 = alph_ast[exampleNo1]; else alpha1 = alph[exampleNo1];
			
			if(ast1==ast2) {aux = 1;L = Math.max(0,alpha1+alpha2-parameters[0]); H = Math.min(parameters[0],alpha1+alpha2);} 
			else {aux = -1;L = Math.max(0,alpha2-alpha1); H = Math.min(parameters[0],parameters[0]+alpha2-alpha1);}
			
			if(L==H) return false;
			
			double eta = 2*Kernel[exampleNo1][exampleNo2] - Kernel[exampleNo1][exampleNo1] - Kernel[exampleNo2][exampleNo2];
			double E1 = calculateOutput(exampleNo1) - y_train[exampleNo1][0];
			
			//System.out.printf("%f\t%f\n",E1,E2);
			
			double a2;
			if(eta<-tol2) a2 = alpha2 - y_train[exampleNo2][0]*(E1-E2)/eta;
			else 
			{
				double[] LVec = new double[alph.length];
				double[] LVec_ast = new double[LVec.length];
				double[] HVec = new double[LVec.length];
				double[] HVec_ast = new double[LVec.length];
				
				for(int idx=0;idx<LVec.length;++idx)
				{
					LVec[idx] = alph[idx];LVec_ast[idx] = alph_ast[idx];HVec[idx] = alph[idx];HVec_ast[idx] = alph_ast[idx];
				}
				
				if(ast2) {LVec_ast[exampleNo2] = L; HVec_ast[exampleNo2] = H;} 
				else {LVec[exampleNo2] = L;HVec[exampleNo2] = H;} 
				double LObj = evaluateObjective(LVec,LVec_ast); double HObj = evaluateObjective(HVec,HVec_ast);
				
				if(LObj > HObj + tol2) a2 = L;
				else if(HObj > LObj + tol2) a2 = H;
				else a2 = alpha2;
			}
			//System.out.println(bias);
			/*if(Double.isNaN(a2)) 
			{
				System.out.printf("%f\t%f\t%f\t%f\n",eta,E1,E2,bias);
			}*/
			
			if (a2 < L) a2 = L;	else if (a2 > H) a2 = H;
			if (a2 < tol2) a2 = 0; else if (a2 > (parameters[0]-tol2)) a2 = parameters[0];
			if (Math.abs(a2-alpha2) < tol2*(a2+alpha2+tol2)) return false;
			double a1;
			a1 = alpha1 + aux * (alpha2 - a2);
			
			bias = calculateNewBias(exampleNo1,exampleNo2,a1,alpha1,a2,alpha2,E1,E2);

			if(ast1) alph_ast[exampleNo1] = a1; else alph[exampleNo1] = a1;
			if(ast2) alph_ast[exampleNo2] = a2; else alph[exampleNo2] = a2;
			//System.out.printf("%d\t%b\t%d\t%b\t%f\t%f\t%f\t%f\t%f\t\n",exampleNo1,ast1,exampleNo2,ast2,alpha1,alpha2,a1,a2,eta);
			//Matrix.print(Kernel);
			return true;
		}
		
		private void updateNonBound()
		{
			nonBound.clear();
			nonBound_ast.clear();
			
			for(int idx=0;idx<alph.length;++idx) 
			{
				if( (alph[idx] > tol2) && (alph[idx] < (parameters[0] - tol2) ) ) nonBound.add(idx); 
				if( (alph_ast[idx] > tol2) && (alph_ast[idx] < (parameters[0] - tol2) ) ) nonBound_ast.add(idx); 
			}
		}
		
		private double calculateNewBias(int ex1,int ex2,double newAlpha1,double newAlpha2,double oldAlpha1,double oldAlpha2, double E1,double E2)
		{
			if( (newAlpha1 > tol2) && (newAlpha1 < (parameters[0]-tol2) ) ) 
				return E1 + y_train[ex1][0]*(newAlpha1-oldAlpha1)*Kernel[ex1][ex1] + y_train[ex2][0]*(newAlpha2-oldAlpha2)*Kernel[ex1][ex2] + bias; 
			else if( (newAlpha2 > tol2) && (newAlpha2 < (parameters[0]-tol2) ) ) 
				return E2 + y_train[ex1][0]*(newAlpha1-oldAlpha1)*Kernel[ex1][ex2] + y_train[ex2][0]*(newAlpha2-oldAlpha2)*Kernel[ex2][ex2] + bias; 

			double b1 = E1 + y_train[ex1][0]*(newAlpha1-oldAlpha1)*Kernel[ex1][ex1] + y_train[ex2][0]*(newAlpha2-oldAlpha2)*Kernel[ex1][ex2] + bias; 
			double b2 = E2 + y_train[ex1][0]*(newAlpha1-oldAlpha1)*Kernel[ex1][ex2] + y_train[ex2][0]*(newAlpha2-oldAlpha2)*Kernel[ex2][ex2] + bias; 
			return 0.5*(b1+b2);
		}
		
		private double calculateOutput(int exampleNo)
		{
			double output = -bias;
			for(int idx=0;idx<N_train;++idx) output += (alph[idx] - alph_ast[idx]) * Kernel[idx][exampleNo];
			return output;
		}
		
		private double evaluateObjective(double[] Alpha,double[] Alpha_ast)
		{
			double objective = 0;
			
			for(int idx1=0;idx1<Alpha.length;++idx1)
			{
				for(int idx2=0;idx2<Alpha.length;++idx2) objective -= 0.5*(Alpha[idx1]-Alpha_ast[idx1])*(Alpha[idx2]-Alpha_ast[idx2])*Kernel[idx1][idx2];
				objective -= parameters[1]*(Alpha[idx1]+Alpha_ast[idx1]);
				objective += y_train[idx1][0]*(Alpha[idx1]-Alpha_ast[idx1]);
			}
			
			return objective;			
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
	
	public static void main(String[] args)
	{
		Random R = new Random(3493);
		double[] A = new double[10];
		for(int idx=0;idx<10;idx++)
			A[idx] = idx;
		
	}
}
