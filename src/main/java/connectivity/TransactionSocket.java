package connectivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionSocket implements Runnable {

    @Override
    public void run() {
        try {
            ServerSocket Server = new ServerSocket(5566);

            while (true) {
                new Thread(new SocketHandler(Server.accept())).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(TransactionSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
