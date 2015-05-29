package publisher;

public class SimpleLogger {
	
	public static void log(String logMsg){
		System.out.println("Thread: " + Thread.currentThread().getName()+ " " + logMsg);
	}

}
