import java.io.*;

import java.net.*;

public class Client {

	public static void main(String[] args) {

		// TODO

		try {

			Socket s = new Socket("localhost", 50000);

			BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());

			//Handshake with server
			
			
			
			//Gets command to find the largest server
			
			
			
			//Scheduale jobs to server




			dout.close();
			s.close();

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}