import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame {
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message ="";
    private String serverIP;
    private Socket connection;
    
    //constructor
    public Client(String host){
        super("Clients!");
        serverIP = host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent event){
                        sendMessage(event.getActionCommand());
                        userText.setText("");
                    }
                }
        );
        add(userText,BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow),BorderLayout.CENTER);
        setSize(400,300);
        setVisible(true);
    }
    public void startRunning(){
        try{
            connectToServer();
            setupStreams();
            whileChatting();
        }catch(EOFException eofException){
            showMessage("\n Client terminated connection");
        }catch(IOException ioException){
            ioException.printStackTrace();
        }finally{
            closeCrap();
        }
    }
    //connect to server
    private void connectToServer() throws IOException{
        showMessage("Attempting connection...\n");
        connection=new Socket(InetAddress.getByName(serverIP),4567);
        showMessage("Connected to: "+connection.getInetAddress().getHostName());
        
    }
    //setup streams to send and receive messages
    private void setupStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are good to go \n");
    }
    //while chatting with server 
    private void whileChatting()throws IOException{
        ableToType(true);
        do{
            try{
                message=(String) input.readObject();
                showMessage("\n"+message);
            }catch(ClassNotFoundException clasNotFoundException){
                showMessage("\n dont know object type");
            }
        }while(!message.equals("Server - END"));
    }
    //close the streams and sockets 
    private void closeCrap(){
        showMessage("\n closing crap down..");
        ableToType(false);
        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException ioException){
        ioException.printStackTrace();
       }
    }
    //send messages to server 
    private void sendMessage(String message){
        try{
            output.writeObject("Client - " +message);
            output.flush();
            showMessage("\nClient - "+message);
        }catch(IOException ioException){
            chatWindow.append("\n somthing went wrong!");
        }
    }
    //update chatwindow
    private void showMessage(final String m){
        SwingUtilities.invokeLater(
        new Runnable(){
            public void run(){
                chatWindow.append(m);
            }
           }
          );
    }
    //gives user permission to type crap into the text box
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
        Client msg;
        msg=new Client("127.0.0.1");
        msg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        msg.startRunning();
    }
    
}

