public class Fibonacci {
	public static int fib(int n) {
		System.out.println(n);
		if (n==3) {
			System.out.println("fib3");
			return 2;
		} else if (n==2) {
			System.out.println("fib2");
			return 1;
		} else if (n<2) {
			System.out.println("fib1");
			return n;
		} else {
			System.out.println("fib recursive");
			return (fib(n-1) + fib(n-2));
		}
	}

	public static void main(String[] args) {
		int n = Integer.parseInt(args[0]);
		int result = fib(n);
		System.out.println("The result is "+result);
	}
}