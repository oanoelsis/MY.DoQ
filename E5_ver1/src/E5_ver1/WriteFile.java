package E5_ver1;

import java.io.*;

public class WriteFile {
	private BufferedWriter bw;

	public void write(String data) throws IOException{
		String location = "C:\\Users\\user\\Documents\\MY.DoQ\\out.txt";
		FileWriter fw = new FileWriter(location);
		bw = new BufferedWriter(fw);
		//System.out.println("it shold write : " + data);
		bw.write(data);
		bw.close();
		
	}
}
