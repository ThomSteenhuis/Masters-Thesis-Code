package math;

public class TestFunction extends Function {

	public TestFunction()
	{
		super(3);
	}

	public double evaluate(double[] input) throws ArrayIndexOutOfBoundsException 
	{
		return Math.pow(input[0]+1, 2) + Math.pow(input[1]-1, 2) + Math.pow(input[2]-2, 2);
	}

	public static void main(String[] args) 
	{
		TestFunction f = new TestFunction();
		double[] vec = {1,1,2};
		System.out.println(f.evaluate(vec) );
		NelderMead  nm = new NelderMead(f);
		nm.optimize();
		nm.printSummary();
	}

}
