import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.FlowLayout;

import java.util.Date;

public class MultiThreadChatClient extends JFrame
{
  public static void main(String[] args) 
  {
    String name = "anonymous";
    name = JOptionPane.showInputDialog("name ");
    
    
    MultiThreadChatClient client = new MultiThreadChatClient(name);
    client.initialise("localhost", 8080);
    
    client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    client.setSize(375, 395);
    client.setVisible(true);
    
    client.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e)
          {
            client.closeClient();
          }
        });
    
  }
  
  private JTextField textField;
  private JTextArea textArea;
  private JButton sendButton;
  private JScrollPane frameScroll;
  private String name;
  private Client client;
  
  public MultiThreadChatClient(String n)
  {
    super("Client: " + n);
    this.name = n;
  }
  
  public void initialise(String host, int port)
  {
    setLayout(new FlowLayout());
    textArea = new JTextArea(null, (new Date()).toString(), 20, 30);
    textArea.setEditable(false);
    textField = new JTextField("", 22);
    
    frameScroll = new JScrollPane(textArea);
    frameScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    frameScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    sendButton = new JButton("send");
    
    //Events
    textField.addActionListener(
        new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            client.sendClientMessage(textField.getText());
          }
        }
        );
    
    sendButton.addActionListener(
        new ActionListener()
       {
         public void actionPerformed(ActionEvent e)
         {
           client.sendClientMessage(textField.getText());
         }
       }
        );
    
    setResizable(false);
    add(textArea);
    add(textField);
    add(sendButton);
    
    client = new Client(host, port, name, this); 
  }
  
  public void updateTextField(String msg)
  {
    textArea.append("\n" + msg);
    textField.setText("");
  }
  
  public void closeClient()
  {
    client.closeConnection();
  }
}

class Client
{
  private Socket clientSocket = null;
  private MyConnection conn = null;

  private int portNumber = 8080; 
  private String sourceHost = "localhost";
  private String name = "";
  
  private final MultiThreadChatClient ptrMtc; 
  
  private ClientWorker clientThread;
  
  Client(String host, int port, String n, MultiThreadChatClient mtc)
  {
    this.portNumber = port;
    this.sourceHost = host;
    this.name = n;
    this.ptrMtc = mtc;
    System.out.println("Setting-up client " + portNumber); 
    try 
    {
      clientSocket = new Socket(sourceHost, portNumber);
      conn = new MyConnection(clientSocket);     
    } 
    catch (UnknownHostException e) 
    {
      System.err.println("Don't know about host " + host);
    } 
    catch (IOException e) 
    {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    if (clientSocket != null && conn != null) 
    { 
      {
        clientThread = new ClientWorker(clientSocket, this);
        (new Thread(clientThread)).start();
        sendClientMessage(name);
      } 
    }    
  }
  
  public void sendClientMessage(String msg)
  {
    try
    {
      System.out.println(msg);
      conn.sendMessage(msg);
    } 
    catch (IOException e) 
    {
      e.printStackTrace();
    }
  }
  
  public void getClientMessage(String msg)
  {
    ptrMtc.updateTextField(msg);
  }

  public void closeConnection()
  {
    try
    {
      sendClientMessage("/quit"); 
      clientSocket.close();
      clientThread.toggleConnection(false);
    }
    catch (IOException e) 
    {
      e.printStackTrace();
    }
  }
  
}

class ClientWorker implements Runnable
{
  private MyConnection conn = null;
  private final Socket socket;
  final Client mtcClient;
  private boolean isConnected = true;
  
  public ClientWorker(Socket s, Client mtcc)
  {
    this.socket = s;
    this.mtcClient = mtcc;    
  }
  public void run() 
  {
    conn = new MyConnection(socket);
    String responseLine;
    try {
      while (isConnected) 
      {
        responseLine = conn.getMessage();
        mtcClient.getClientMessage(responseLine);
      }
    } catch (IOException e) 
    {
      e.printStackTrace();
    }
  }
  public void toggleConnection(boolean conn)
  {
    isConnected = conn;
  }
  public boolean getConnectionState()
  {
    return isConnected;
  }
}