package Server;
/* ServerThread.java
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Xiangxing Liu
 * xl5587
 * 76175
 * Zi Zhou Wang
 * zw3948
 * 76175
 * Slip days used: <1>
 * Git URL: https://github.com/xxuil/Chat
 * Summer 2017
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerThread implements Observer {
    private boolean Debug = true;
    private boolean isOpen;
    private int count = 0;
    private int port = 9001;
    protected ArrayList<Observable> observerList;
    protected ArrayList<PrintWriter> oos;
    protected ArrayList<String> userNameList;
    protected HashMap<String, ClientThread> userMap;




    public ServerThread() {
        this.isOpen = true;
        this.userMap = new HashMap<>();
        this.observerList = new ArrayList<>();
        this.userNameList = new ArrayList<>();
        this.oos = new ArrayList<>();
    }

    public void setOpen(boolean isOpen){
        this.isOpen = isOpen;
    }

    private void setUpNetworking() throws Exception {
        @SuppressWarnings("resource")
        ServerSocket serverSock = new ServerSocket(port);
        System.out.println("Server established");
        while (isOpen) {
            //Accepting clients
            Socket clientSocket = serverSock.accept();
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            oos.add(writer);

            //Starting new client thread
            ClientHandler client = new ClientHandler(clientSocket, writer);
            observerList.add(client);
            Thread t = new Thread(client);
            t.start();

            count ++;
            System.out.println("Got a new connection");
            System.out.println("Client #" + count);
        }

    }

    private void notifyClients(String message) {
        for (PrintWriter writer : oos) {
            writer.println(message);
            writer.flush();
        }
    }

    class ClientHandler extends Observable implements Runnable {
        private String Username;
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;

        public ClientHandler(Socket clientSocket, PrintWriter writer) throws IOException {
            this.Username = "";
            this.socket = clientSocket;
            this.writer = writer;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            System.out.println("ClientHandler for " + Username + " has started");

            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    synchronized(oos){
                        String[] parse = message.split(" ");

                        if(Debug)
                            System.out.println(message);

                        if(parse[0].equals("/new")) {
                            synchronized(userNameList) {
                                String name = parse[1];

                                if(userNameList.contains(name)){
                                    System.out.println("Same user name already created");
                                    oos.get(oos.size() - 1).println("/remove");
                                    oos.get(oos.size() - 1).flush();
                                    oos.remove(oos.size() - 1);

                                    if(Debug)
                                        System.out.println(userNameList);
                                    continue;
                                }

                                userNameList.add(name);

                                if(Debug)
                                    System.out.println(userNameList);

                                notifyClients("/new "+ name);
                            }
                        }

                        else if(parse[0].equals("/private")) {
                            String name = parse[1];

                            if(userNameList.contains(name)){
                                //message.replaceFirst("/private ","");
                                for(int clientIndex = 0; clientIndex < userNameList.size(); clientIndex ++){
                                    oos.get(clientIndex).println(message);
                                    oos.get(clientIndex).flush();
                                }
                            }
                        }

                        else if(parse[0].equals("/logoff")) {
                            String name = parse[1];
                            int index = userNameList.indexOf(name);
                            oos.remove(index);
                            userNameList.remove(name);
                            observerList.remove(index);
                            if(Debug) {
                                System.out.println(userNameList);
                                System.out.println(oos);
                                count --;
                            }
                        }

                        else{
                            notifyClients(message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(Observable observable, Object obj1) {
        observerList.remove(observable);
    }

    public static void main(String[] args) throws Exception{
        ServerThread serverThread = new ServerThread();
        serverThread.setUpNetworking();
    }
}