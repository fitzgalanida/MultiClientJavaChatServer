package multithreadchatserver;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
public class MultiThreadChatServer 
{
  public static void main(String args[]) 
  {
    new Server(8080);
  }
}
class Server
{
  private ServerSocket serverSocket = null;
  private Socket clientSocket = null;
  public static final int MAX_CLIENT_COUNT = 10;
  private ArrayList<ClientThread> threads = new ArrayList<ClientThread>();
  private int portNumber = 8080;
  private int connectedUsers = 0;
  Server(int port)
  {
    this.portNumber = port;
    try 
    {
      System.out.println("Setting-up server " + portNumber);
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    while (true) {
      try {
        clientSocket = serverSocket.accept();
        ClientThread thread = new ClientThread(clientSocket, threads, this);
        thread.start();
        threads.add(thread);
        increaseConnectedUsers();
        if (getConnectedUsers() == MAX_CLIENT_COUNT) 
        {          
          MyConnection mc = new MyConnection(clientSocket);
          mc.sendMessage("***failed to connect***");
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
  
  public void increaseConnectedUsers()
  {
    connectedUsers++;
  }
  
  public void decreaseConnectedUsers()
  {
    connectedUsers--;
  }
  
  public int getConnectedUsers()
  {
    return connectedUsers;
  }
}
class ClientThread extends Thread {
  private MyConnection conn = null;
  private Socket clientSocket = null;
  private ArrayList<ClientThread> threads;
  private final Server server;
  private String name = "";

  public ClientThread(Socket clientSocket, ArrayList<ClientThread> threads, Server svr) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    this.server = svr;
  }

  public void run() {
    try {
      conn = new MyConnection(clientSocket);
      this.name = conn.getMessage();
      System.out.println(name + " has entered (" + this.server.getConnectedUsers() 
        + "/" + this.server.MAX_CLIENT_COUNT + ")");
      for (ClientThread thread: threads)
      {
        thread.conn.sendMessage("***" + name + " has entered***");
      }
      while (true) {        
        String line = conn.getMessage();
        if (line.startsWith("/quit")) 
        {
          break;
        }
        else if(line.startsWith("/time"))
        {
          for (ClientThread thread: threads)
          {
            if(this.name == thread.getUserName())
            {
              thread.conn.sendMessage("SERVER: " + (new Date()).toString()); 
            }
          }
        }
        else
        {
          for (ClientThread thread: threads)
          {
            thread.conn.sendMessage("<" + name + "> " + line); 
          }
        }
      }
      for (ClientThread thread: threads)
      {
        if( this != thread) 
        {
          thread.conn.sendMessage("*** " + name + " has left***"); 
        }
      }
      this.server.decreaseConnectedUsers();
      conn.sendMessage("Bye " + name + " ");     
      System.out.println(name + " has left (" + this.server.getConnectedUsers() 
        + "/" + this.server.MAX_CLIENT_COUNT + ")");
      threads.remove(this);
      clientSocket.close();      
    } catch (IOException e) 
    {
    }
  }
  public String getUserName()
  {
    return this.name;
  }
}