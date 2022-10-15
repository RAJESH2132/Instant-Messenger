import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame{
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;
    
    public Server(){
        super("Instant Messenger");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent event){
                        sendMessage(event.getActionCommand());
                        userText.setText("");
                    }
                });
        add(userText,BorderLayout.NORTH);
        chatWindow = new JTextArea(400,300);
        add(new JScrollPane(chatWindow));
        setSize(400,300);
        setVisible(true);
    }
    //setup and the server
    public void startRunning(){
        try{
            server = new ServerSocket(4567,100);
            while(true){
                try{
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                }catch(EOFException eofException){
                    showMessage("\n Server ended the connection!");
                }finally{
                    closeCrap();
                }
            }
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    //wait for connection,then display connection information
    private void waitForConnection() throws IOException{
        showMessage("Waiting for someone to connect....\n");
        connection = server.accept();
        showMessage("Now connected to "+ connection.getInetAddress().getHostName());
    }
    private void setupStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input =new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are now setup! \n");
    }
    // During the chat conversation
    private void whileChatting() throws IOException {
        String message = "You are now connected";
        sendMessage(message);
        ableToType(true);
        do{
            try{
                message = (String) input.readObject();
                showMessage("\n" + message);
            }catch(ClassNotFoundException classNotFoundException){
                showMessage("\n idk what user send!");
            }
        }while(!message.equals("CLIENT - END"));
    }
    //close streams and sockets after chatting
    private void closeCrap(){
        showMessage("\n Closing connections...\n");
        ableToType(false);
        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    // send message to client
    private void sendMessage(String message){
        try{
            output.writeObject("Server - "+message);
            output.flush();
            showMessage("\nSERVER - "+ message);
        }catch(IOException ioException){
            chatWindow.append("\n ERROR:CANT SEND MESSAGE");
        }
    }
    //updates chatWindow
    private void showMessage(final String text){
        SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                        chatWindow.append(text);
                    }
                }
        );
    }
    //let user type message
    private void ableToType(final boolean able){
         SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                       userText.setEditable(able);
                    }
                }
        );
    }
    public static void main(String[] args){
        Server x = new Server();
        x.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        x.startRunning();
    }
}
