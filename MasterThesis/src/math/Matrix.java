package math;

public class Matrix {

	public static double innerProduct(double[] array1, double[] array2)
	{
		if(array1.length != array2.length)
		{
			System.out.println("Error (innerProduct): arrays have unequal length");
			return 0;
		}

		double output = 0;

		for(int idx=0;idx<array1.length;++idx)
		{
			output += (array1[idx] * array2[idx]);
		}

		return output;
	}

	public static double[] innerProduct(double[] array, double[][] matrix)
	{
		if( (array.length != matrix.length) || (matrix.length == 0) )
		{
			System.out.println("Error (innerProduct): array and matrix have unequal length or 0 length");
			return null;
		}

		double[] output = new double[matrix[0].length];

		for(int idx1=0;idx1<matrix[0].length;++idx1)
		{
			for(int idx2=0;idx2<array.length;++idx2)
			{
				output[idx1] += (array[idx2] * matrix[idx2][idx1]);
			}
		}

		return output;
	}

	public static double[] innerProduct(double[][] matrix, double[] array)
	{
		if(matrix.length == 0)
		{
			System.out.println("Error (innerProduct): matrix has 0 length");
			return null;
		}

		if( (array.length != matrix[0].length) || (array.length == 0) )
		{
			System.out.println("Error (innerProduct): array and matrix have unequal length or 0 length");
			return null;
		}

		double[] output = new double[matrix.length];

		for(int idx1=0;idx1<matrix.length;++idx1)
		{
			for(int idx2=0;idx2<array.length;++idx2)
			{
				output[idx1] += (array[idx2] * matrix[idx1][idx2]);
			}
		}

		return output;
	}

	public static double[][] outerProduct(double[] array1, double[] array2)
	{
		double[][] output = new double[array1.length][array2.length];

		for(int idx1=0;idx1<array1.length;++idx1)
		{
			for(int idx2=0;idx2<array2.length;++idx2)
			{
				output[idx1][idx2] = array1[idx1] * array2[idx2];
			}
		}

		return output;
	}

	public static double[] difference(double[] array1, double[] array2)
	{
		if(array1.length != array2.length)
		{
			System.out.println("Error (difference): arrays have unequal length");
			return null;
		}

		double[] output = new double[array1.length];

		for(int idx=0;idx<array1.length;++idx)
		{
			output[idx] = array1[idx] - array2[idx];
		}

		return output;
	}

	public static double[][] difference(double[][] matrix1, double[][] matrix2)
	{
		if( (matrix1.length != matrix2.length) || (matrix1.length == 0) )
		{
			System.out.println("Error (difference): matrixs have unequal length or 0 length");
			return null;
		}

		double[][] output = new double[matrix1.length][matrix1[0].length];

		for(int idx1=0;idx1<matrix1.length;++idx1)
		{
			for(int idx2=0;idx2<matrix1[0].length;++idx2)
			{
				output[idx1][idx2] = matrix1[idx1][idx2] - matrix2[idx1][idx2];
			}
		}

		return output;
	}
	
	public static double infinityNorm(double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (infinityNorm): array has zero length");
			return 0;
		}
		
		double output = Math.abs(array[0]);
		
		for(int idx=1;idx<array.length;++idx)
		{
			double temp = Math.abs(array[idx]);
			if(temp > output)
				output = temp;
		}
		
		return output;
	}

	public static double[] scalarMultiplication(double scalar, double[] array)
	{
		double[] output = new double[array.length];

		for(int idx=0;idx<array.length;++idx)
			output[idx] = scalar * array[idx];

		return output;
	}

	public static double[][] scalarMultiplication(double scalar, double[][] matrix)
	{
		if(matrix.length == 0)
		{
			System.out.println("Error (scalarMultiplication): matrix has 0 length");
			return null;
		}

		double[][] output = new double[matrix.length][matrix[0].length];

		for(int idx1=0;idx1<matrix.length;++idx1)
		{
			for(int idx2=0;idx2<matrix[0].length;++idx2)
			{
				output[idx1][idx2] = scalar * matrix[idx1][idx2];
			}
		}

		return output;
	}
	
	public static void print(double[][] matrix)
	{
		for(int idx1=0;idx1<matrix.length;++idx1)
		{
			for(int idx2=0;idx2<matrix[idx1].length;++idx2)
				System.out.printf("%f\t", matrix[idx1][idx2]);
			
			System.out.println();
		}
	}
	
	public static void print(double[] array)
	{
		for(int idx=0;idx<array.length;++idx)
			System.out.printf("%f\t",array[idx]);

		System.out.println();
	}
}
