package math;

public class LUfactorization {

	private double[][] A;
	private double[] b;
	
	private double[][] L;
	private double[][] U;
	private int[] P;
	private double[] x;
	
	public LUfactorization(double[][] matrix, double[] array)
	{
		if(!checkInputValidity(matrix,array))
		{	System.out.println("Error (LUdecomposition): invalid input"); return;	}
		
		A = matrix;
		b = array;
	}
	
	public void solve()
	{
		calculateLUP();
		
		int N = A.length;		
		
		double[] Pb = new double[N];
		double[] y = new double[N];
		x = new double[N];
		
		for(int idx=0;idx<N;++idx) Pb[idx] = b[P[idx]];
		
		for(int idx1=0;idx1<N;++idx1)
		{	y[idx1] = Pb[idx1]; for(int idx2=0;idx2<idx1;++idx2) y[idx1] -= (L[idx1][idx2]*y[idx2]);	}
	
		for(int idx1=N-1;idx1>=0;--idx1)
		{	x[idx1] = y[idx1]/U[idx1][idx1]; for(int idx2=(idx1+1);idx2<N;++idx2) x[idx1] -= (U[idx1][idx2]*x[idx2])/U[idx1][idx1];	}
	}
	
	public double[] getSolution()
	{
		return x;
	}
	
	private void calculateLUP()
	{
		int N = A.length;
		
		double[][] A_working = new double[N][N];
		L = new double[N][N];
		U = new double[N][N];
		P = new int[N];
		
		for(int idx1=0;idx1<N;++idx1) 
		{	P[idx1] = idx1; for(int idx2=0;idx2<N;++idx2) A_working[idx1][idx2] = A[idx1][idx2];	}
		
		for(int idx1=0;idx1<(N-1);++idx1)
		{	
			int maxIdx = findMax(A_working,idx1);
			exchangeRows(P,idx1,maxIdx);
			
			for(int idx2=(idx1+1);idx2<N;++idx2)
			{
				A_working[P[idx2]][idx1] = A_working[P[idx2]][idx1] / A_working[P[idx1]][idx1];
				
				for(int idx3=(idx1+1);idx3<N;++idx3) A_working[P[idx2]][idx3] -= A_working[P[idx2]][idx1] * A_working[P[idx1]][idx3];
			}
		}
		
		for(int idx1=0;idx1<N;idx1++)
		{	for(int idx2=idx1;idx2<N;idx2++) U[idx1][idx2] = A_working[P[idx1]][idx2]; for(int idx2=0;idx2<idx1;idx2++) L[idx1][idx2] = A_working[P[idx1]][idx2];	}
	}
	
	private void exchangeRows(int[] array, int idx1, int idx2)
	{
		int temp = array[idx1];
		array[idx1] = array[idx2];
		array[idx2] = temp;
	}
	
	private int findMax(double[][] matrix,int start)
	{
		double max = Math.abs(matrix[start][start]);
		int maxIdx = start;
		
		for(int idx = start+1;idx<matrix.length;++idx)
		{	if(Math.abs(matrix[idx][start]) > max) {	maxIdx = idx; max = Math.abs(matrix[idx][start]);	}	}
		
		return maxIdx;
	}
	
	private boolean checkInputValidity(double[][] matrix,double[] array)
	{
		int N = matrix.length;
		
		if(N==0 || array.length != N) return false;
		
		for(int idx=0;idx<N;++idx)
		{	if(matrix[idx].length != N) return false;	}
		
		return true;
	}
	
	private void printPA()
	{
		int N = A.length;
		
		for(int idx1=0;idx1<N;++idx1)
		{	for(int idx2=0;idx2<N;++idx2) System.out.printf("%f\t",A[P[idx1]][idx2]); System.out.print("\n");	}
	}
}
