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
	
	public static double[][] innerProduct(double[][] matrix1, double[][] matrix2)
	{
		if(matrix1.length == 0 || matrix2.length == 0)
		{
			System.out.println("Error (innerProduct): matrix has 0 length");
			return null;
		}

		if( (matrix2.length != matrix1[0].length) )
		{
			System.out.println("Error (innerProduct): matrices not compatible");
			return null;
		}

		double[][] output = new double[matrix1.length][matrix2[0].length];

		for(int idx1=0;idx1<output.length;++idx1)
		{
			for(int idx2=0;idx2<output[idx1].length;++idx2)
			{
				for(int idx3=0;idx3<matrix1[idx1].length;++idx3) output[idx1][idx2] += (matrix1[idx1][idx3] * matrix2[idx3][idx2]);
			}
		}

		return output;
	}

	public static double[][] transpose(double[][] matrix)
	{
		if(matrix.length == 0)
		{
			System.out.println("Error (tranpose): matrix has 0 length");
			return null;
		}

		double[][] output = new double[matrix[0].length][matrix.length];

		for(int idx1=0;idx1<matrix.length;++idx1)
		{
			for(int idx2=0;idx2<matrix[idx1].length;++idx2)
				output[idx2][idx1] = matrix[idx1][idx2];
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

	public static double sum(double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (sum): array has 0 length");
			return 0;
		}

		double output = 0;

		for(int idx=0;idx<array.length;++idx)
			output += array[idx];

		return output;
	}
	
	public static double sum(double[][] matrix)
	{
		if(matrix.length == 0)
		{
			System.out.println("Error (sum): array has 0 length");
			return 0;
		}

		double output = 0;

		for(int idx1=0;idx1<matrix.length;++idx1)
		{
			for(int idx2=0;idx2<matrix[idx1].length;++idx2) output += matrix[idx1][idx2];
		}

		return output;
	}

	public static double[] addition(double[] array1, double[] array2)
	{
		if(array1.length != array2.length)
		{
			System.out.println("Error (difference): arrays have unequal length");
			return null;
		}

		double[] output = new double[array1.length];

		for(int idx=0;idx<array1.length;++idx)
		{
			output[idx] = array1[idx] + array2[idx];
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
			System.out.println("Error (difference): matrices have unequal length or 0 length");
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

	public static double average(double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (average): array has 0 length");
			return 0;
		}

		double output = 0;

		for(int idx=0;idx<array.length;++idx)
			output += array[idx];

		return output/array.length;
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

	public static double twoNorm(double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (twoNorm): array has zero length");
			return 0;
		}

		double output = 0;

		for(int idx=0;idx<array.length;++idx)
		{
			output += array[idx] * array[idx];
		}

		return Math.sqrt(output);
	}
	
	public static double[][] inverse(double[][] input)
	{
		if(input.length == 0)
		{
			System.out.println("Error (inverse): input has length 0");
			return null;
		}

		int noLines = input.length;
		int noColumns = input[0].length;

		double[][] output = new double[noColumns][noLines];

		for(int idx1=0;idx1<noLines;++idx1)
		{
			if(input[idx1].length != noColumns)
			{
				System.out.println("Error (inverse): input does not have the same no of columns everywhere");
				return null;
			}

			for(int idx2=0;idx2<noColumns;++idx2)
				output[idx2][idx1] = input[idx1][idx2];
		}

		return output;
	}

	public static double[] tanh(double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (tanh): array has 0 length");
			return null;
		}

		double[] output = new double[array.length];

		for(int idx=0;idx<array.length;++idx)
			output[idx] = Math.tanh(array[idx]);

		return output;
	}

	public static double[] elementwiseMultiplication(double[] array1,double[] array2)
	{
		if(array1.length != array2.length)
		{
			System.out.println("Error (elementwiseMultiplication): arrays have unequal length");
			return null;
		}

		double[] output = new double[array1.length];

		for(int idx=0;idx<output.length;++idx)
			output[idx] = array1[idx]*array2[idx];

		return output;
	}

	public static double[] elementwiseMultiplication(double[] array1,double[] array2,double[] array3)
	{
		if( (array1.length != array2.length) || (array1.length != array3.length) )
		{
			System.out.println("Error (elementwiseMultiplication): arrays have unequal length");
			return null;
		}

		double[] output = new double[array1.length];

		for(int idx=0;idx<output.length;++idx)
			output[idx] = array1[idx]*array2[idx]*array3[idx];

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

	public static double[] scalarSubtraction(double scalar,double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (scalarSubtraction): array has 0 length");
			return null;
		}

		double[] output = new double[array.length];

		for(int idx=0;idx<output.length;++idx)
			output[idx] = scalar - array[idx];

		return output;
	}
	
	public static double mean(double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (mean): array has 0 length");
			return 0;
		}
		
		double output = 0;
		
		for(int idx=0;idx<array.length;++idx)
			output += array[idx];
		
		return (output / array.length);
	}
	
	public static double max(double[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (mean): array has 0 length");
			return 0;
		}
		
		double output = array[0];
		
		for(int idx=1;idx<array.length;++idx)
		{
			if(array[idx] > output)
				output = array[idx];
		}			
		
		return output;
	}
	
	public static int max(int[] array)
	{
		if(array.length == 0)
		{
			System.out.println("Error (mean): array has 0 length");
			return 0;
		}
		
		int output = array[0];
		
		for(int idx=1;idx<array.length;++idx)
		{
			if(array[idx] > output)
				output = array[idx];
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
	
	public static void print(int[][] matrix)
	{
		for(int idx1=0;idx1<matrix.length;++idx1)
		{
			for(int idx2=0;idx2<matrix[idx1].length;++idx2)
				System.out.printf("%d\t", matrix[idx1][idx2]);

			System.out.println();
		}
	}

	public static void print(double[] array)
	{
		for(int idx=0;idx<array.length;++idx)
			System.out.printf("%f\t",array[idx]);

		System.out.println();
	}
	
	public static void print(int[] array)
	{
		for(int idx=0;idx<array.length;++idx)
			System.out.printf("%d\t",array[idx]);

		System.out.println();
	}
	
	public static void print(String[] array)
	{
		for(int idx=0;idx<array.length;++idx)
			System.out.printf("%s\t",array[idx]);

		System.out.println();
	}
	
	public static void print(String[][] matrix)
	{
		for(int idx1=0;idx1<matrix.length;++idx1)
		{
			for(int idx2=0;idx2<matrix[idx1].length;++idx2)
				System.out.printf("%s\t", matrix[idx1][idx2]);

			System.out.println();
		}
	}
}
