package exception;

public class WalletConnectionException extends Exception {

    public WalletConnectionException() {
    }
    
    public WalletConnectionException(String message) {
        super(message);
    }
}
