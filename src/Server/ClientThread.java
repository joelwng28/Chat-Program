package Server;

import java.io.*;
import java.net.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;


public class ClientThread extends Application{
    private String name;
    private String hostIP;
    private int port;
    private BufferedReader reader;
    private PrintWriter writer;
    private GridPane grid;
    private TextArea textInput;
    private TextFlow messageBox;
    public MenuButton onlineClients = new MenuButton();
    private ListView<String> userList;
    private static ObservableList<String> users = FXCollections.observableArrayList();
    private String message;


    private boolean Debug = true;

    public void showLogin(Stage primaryStage){
        primaryStage.close();
        grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));


        //Add Username
        int row = 1;
        Label username = new Label("Username:");
        grid.add(username, 0, row);

        TextField nameField = new TextField();
        grid.add(nameField, 1, row);

        //Add host options
        row += 2;
        Label host = new Label("Host:");
        grid.add(host, 0, row);

        TextField hostAddress = new TextField();
        grid.add(hostAddress, 1, row);

        row += 2;
        Label port = new Label("Port:");
        grid.add(port, 0, row);

        TextField portNumber = new TextField();
        grid.add(portNumber, 1, row);

        //Add login button
        row += 2;
        Button login = new Button("Login");
        login.setPrefWidth(90);
        HBox loginBox = new HBox(10);
        loginBox.setAlignment(Pos.BOTTOM_LEFT);
        loginBox.getChildren().add(login);
        grid.add(loginBox, 1, row);

        Button quit = new Button("Quit");
        quit.setPrefWidth(90);
        loginBox.getChildren().add(quit);


        Stage stage = new Stage();
        stage.setTitle("Horn Chat Login");
        stage.setScene(new Scene(grid, 320, 180));
        stage.setAlwaysOnTop(true);
        stage.show();

        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                if(!portNumber.getText().isEmpty()){
                    String username = nameField.getText();
                    String hostIP = hostAddress.getText();
                    int port = Integer.parseInt(portNumber.getText());
                    setLogin(username, hostIP, port);
                }
                connect();
                sendUsername();
                stage.close();
                primaryStage.show();
            }
        });

        quit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                System.exit(0);
            }
        });
    }


    public void initPrivate(String name){
        GridPane window = new GridPane();
        window.setHgap(5);
        window.setVgap(5);
        window.setPadding(new Insets(10, 10, 10, 10));

        TextFlow privateMessage = new TextFlow();
        privateMessage.setPrefSize(400,200);
        privateMessage.setPadding(new Insets(10));
        privateMessage.setLineSpacing(10);

        window.add(privateMessage,0,0);

        TextArea privateInput = new TextArea();
        privateInput.setPrefSize(460,90);
        privateInput.setWrapText(true);
        window.add(privateInput, 0, 6);

        Button send = new Button("Send");
        send.setPrefWidth(90);
        HBox sendBox = new HBox(10);
        sendBox.setAlignment(Pos.BOTTOM_RIGHT);
        sendBox.getChildren().add(send);
        window.add(sendBox, 0, 7);

        Button quitPrivate = new Button("Quit");
        quitPrivate.setPrefWidth(90);
        sendBox.getChildren().add(quitPrivate);


        Stage stage = new Stage();
        stage.setTitle(name + " Private Chat");
        stage.setScene(new Scene(window, 480, 400));
        stage.setAlwaysOnTop(true);
        stage.show();
    }


    public void start(Stage primaryStage){
        try{
            showLogin(primaryStage);

            primaryStage.setTitle("Project 7 Horns Chat");
            grid = new GridPane();
            grid.setHgap(5);
            grid.setVgap(5);
            grid.setPadding(new Insets(30, 30, 30, 30));

            int row = 15;
            textInput = new TextArea();
            textInput.setPrefSize(460,90);
            textInput.setWrapText(true);
            grid.add(textInput, 0, row);

            Button send = new Button("Send");
            send.setPrefWidth(90);
            HBox sendBox = new HBox(10);
            sendBox.setAlignment(Pos.TOP_LEFT);
            sendBox.getChildren().add(send);
            grid.add(sendBox, 1, row);

            Button logoff = new Button("Logoff");
            logoff.setPrefWidth(90);


            Button quitAfter = new Button("Quit");
            quitAfter.setPrefWidth(90);
            HBox quitAbox = new HBox(10);
            quitAbox.setAlignment(Pos.TOP_RIGHT);
            quitAbox.getChildren().add(logoff);
            quitAbox.getChildren().add(quitAfter);
            grid.add(quitAbox, 1, row + 1);

            Label usersLabel = new Label("Online Users");
            grid.add(usersLabel, 1, 0);

            userList = new ListView<>();
            userList.setItems(users);
            userList.setPrefSize(10,360);
            userList.setOrientation(Orientation.VERTICAL);
            grid.add(userList, 1, 1);

            Label messageLable = new Label("Message: ");
            grid.add(messageLable, 0, 0);
            messageBox = new TextFlow();
            messageBox.setPrefSize(460,360);
            messageBox.setPadding(new Insets(10));
            messageBox.setLineSpacing(10);

            grid.add(messageBox,0,1);



            Scene scene = new Scene(grid, 720, 640);
            primaryStage.setScene(scene);
            primaryStage.show();

            send.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event){

                    if (reader != null && writer != null) {
                        String message = "/public " + name + " " + textInput.getText();

                        if (Debug)
                            System.out.println(message);

                        writer.println(message);
                        writer.flush();
                    }
                }
            });

            logoff.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event){
                    sendLogoff();
                    primaryStage.close();
                    showLogin(primaryStage);
                }
            });

            quitAfter.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event){
                    sendLogoff();
                    System.exit(0);
                }
            });

            textInput.setOnKeyPressed(e -> {
                if(e.getCode() == KeyCode.ENTER)
                    send.fire();
            });

            userList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(event.getClickCount() == 2){
                        String name = userList.getSelectionModel().getSelectedItem();
                        if(Debug)
                            System.out.println("Private chat selected with user: " + name);

                        initPrivate(name);
                    }
                }
            });



        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setLogin(String name, String hostIP, int port){
        this.port = port;
        this.hostIP = hostIP;
        this.name = name;
        //this.users.add(name);
    }

    private void connect(){
        try{
            @SuppressWarnings("resource")
            Socket sock = new Socket(hostIP, port);
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("networking established");
            Thread readerThread = new Thread(new IncomingReader(this.name));
            readerThread.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUsername(){
        writer.println("/new " + name);
        writer.flush();
    }

    public void sendLogoff(){
        writer.println("/logoff " + name);
        writer.flush();
    }

    public String getClientName(){
        return this.name;
    }

    public static void main(String[] args) {
        launch(args);
    }

    class IncomingReader implements Runnable {
        private String name;
        public IncomingReader(String name) {
            this.name = name;
        }

        public void run() {
            try {
                while ((message = reader.readLine()) != null) {
                    String[] parse = message.split(" ");

                    //Print new member notification
                    if(parse[0].equals("/new")){
                        synchronized (users){
                            Platform.runLater(() ->{
                                String newName = parse[1];
                                users.add(newName);
                                userList.setItems(users);
                                System.out.println("message recieved: " + message);
                            });
                        }
                    }

                    //Handle private chat
                    else if(parse[0].equals("/private")){

                    }

                    else if(parse[0].equals("/remove")){
                        System.exit(0);
                    }
                    //Print message
                    else{
                        Platform.runLater(() ->{
                            String finalString = message;
                            String outputName = parse[1];

                            Text text = new Text(finalString);

                            String reg = "/public " + outputName;
                            String replacement = outputName + ": ";

                            finalString = finalString.replaceAll(reg, replacement);

                            // Add new line if not the first child
                            text = new Text("\n" + finalString);

                            messageBox.getChildren().add(text);
                        });
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
