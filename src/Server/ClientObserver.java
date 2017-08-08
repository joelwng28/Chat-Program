package Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class ClientObserver extends PrintWriter implements Observer {
    public ClientObserver(OutputStream out) {
        super(out);
    }

    public ClientObserver(Socket s) throws IOException {
        this(s.getOutputStream());
    }

    @Override
    public void update(Observable o, Object arg) {
        this.println(arg); //writer.println(arg);
        this.flush(); //writer.flush();
    }

}
