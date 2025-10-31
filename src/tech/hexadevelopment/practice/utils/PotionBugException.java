package tech.hexadevelopment.practice.utils;

public class PotionBugException extends Exception{

	
    /**
	 * 
	 */
	private static long serialVersionUID = 1L;

	public PotionBugException(String message) {
        super(message);
    }

    public PotionBugException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
