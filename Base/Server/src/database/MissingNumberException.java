package database;

public class MissingNumberException extends Exception{
    public MissingNumberException(){} 
	public MissingNumberException(String msg) {
        super(msg);
    }
}
