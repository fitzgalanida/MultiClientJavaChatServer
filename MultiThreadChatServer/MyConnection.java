package multithreadchatserver;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MyConnection 
{
  public BufferedReader is = null;
	public PrintWriter os = null;
	
	MyConnection(Socket socket) 
	{
	  try 
	  {
	    os = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	    is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	  }
	  catch(IOException e)
	  {
	    e.printStackTrace();
	  }
	}
	String getMessage() throws IOException
	{
	  return is.readLine();
	}
	void sendMessage(String msg) throws IOException
	{
	  os.println(msg);
	  os.flush();
	}
}
