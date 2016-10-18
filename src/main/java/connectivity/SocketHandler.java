package connectivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.TransactionService;
import utility.Mail;

public class SocketHandler implements Runnable {

    private Socket socket;
    private TransactionService transactionService;

    public SocketHandler(Socket socket) {
        this.socket = socket;
        this.transactionService = new TransactionService();
    }

    @Override
    public void run() {
        try {
            while (true) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputFromClient = inFromClient.readLine();    
                
                if (inputFromClient.startsWith("block")) {
                    transactionService.checkTransactionsForConfirmations();
                }
                if (inputFromClient.startsWith("wallet")) {
                    String[] split = inputFromClient.split(" ");
                    transactionService.createTransaction(split[1]);
                }
                break;
            }
            socket.close();
        } catch (Exception ex) {
            try {
                Mail mail = new Mail();
                mail.sendError(ex);
                this.socket.close();
            } catch (IOException ex1) {
                Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
}
