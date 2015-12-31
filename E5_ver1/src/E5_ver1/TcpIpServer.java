package E5_ver1;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TcpIpServer {
	ServerSocket serverSocket = null;
	
	OutputStream out;
	DataOutputStream dos;
	InputStream in;
	DataInputStream dis;
	Socket socket;
	
	public void setup(int portNum) throws IOException{
		serverSocket = new ServerSocket(portNum);
		System.out.println(getTime() + "server socket open.");
		System.out.println(getTime() + "wait for connection...");
		//serverSocket.setSoTimeout(5 * 1000);		
	}
	
	public void accept() throws IOException{
		socket = serverSocket.accept();
		System.out.println(getTime() + " " + socket.getInetAddress() + "connection established.");
		out = socket.getOutputStream();
		dos = new DataOutputStream(out);
		in = socket.getInputStream();
		dis = new DataInputStream(in);
	}
	
	public void write(String data) throws IOException{
		dos.writeUTF(data);
		System.out.println(getTime() + "send data.");
	}
	
	
	
	public String read() throws IOException{
		String data = "";
		//System.out.println("TCPIPServer read operate...");
		//System.out.println("count: " + dis.available());
		//String data = dis.readUTF();
		
		while(true){
			if(dis.available()>0) break;
		}
		int count = dis.available();
		byte[] bytes = new byte[count];
		dis.read(bytes, 0, count);
		data = new String(bytes, Charset.forName("UTF-8"));
		//System.out.print("TCPIPServer read operate end...");

		return data;
	}
	
	public void close() throws IOException{
		dis.close();
		dos.close();
		socket.close();
		serverSocket.close();
	}
	
	static String getTime(){
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}
}
