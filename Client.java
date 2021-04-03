import java.io.*;

import java.net.*;
import java.util.*;

public class Client {

	public static class Server implements Comparable<Server> {
		String Type = "";
		int ID;
		int core;

		public Server(String Type, String core) {
			this.Type = Type;
			this.core = Integer.parseInt(core);
		}

		public String getType() {
			return Type;
		}

		public String getCore() {
			return Integer.toString(core);
		}

		public void setType(String newType) {
			this.Type = newType;
		}

		public void setCore(String newCore) {
			this.core = Integer.parseInt(newCore);
		}

		@Override
		public int compareTo(Client.Server o) {
			// TODO Auto-generated method stub
			if (this.core - o.core == 0) {
				return o.Type.compareTo(this.Type);
			}
			return this.core - o.core;
		}

	}

	public static void sendMSG(String msg, DataOutputStream out) {
		try {
			out.write(msg.getBytes());
			out.flush();
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	public static String readMSG(BufferedReader in) throws IOException {
		String message = in.readLine();
		System.out.println("server says: " + message);
		return message;
	}

	public static void handShake(BufferedReader in, DataOutputStream out) {
		try {
			String message = "";
			sendMSG("HELO\n", out);
			message = readMSG(in);
			// System.out.println("server says: " + rcvd);
			if (message.equals("OK")) {
				sendMSG("AUTH aydin\n", out);
			} else {
				System.out.println("not equals ok");
			}
			message = readMSG(in);
			// System.out.println("server says: " + rcvd);
			if (message.equals("OK")) {
				sendMSG("REDY\n", out);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static String[] parsing(String data) {
		String delims = "[ ]+"; // set the space as the splitting element for parsing messages.
		String[] splitData = data.split(delims);
		return splitData;
	}

	public static void main(String[] args) {

		try {

			Socket s = new Socket("localhost", 50000);

			BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());

			// String delims = "[ ]+"; //set the space as the splitting element for parsing
			// messages.
			String rcvd = ""; // hold the received message from server

			// Initiated the Client server handshake
			handShake(din, dout);
			/*
			 * dout.write("HELO\n".getBytes()); dout.flush(); rcvd = din.readLine();
			 * System.out.println("server says: " + rcvd); if (rcvd.equals("OK")) {
			 * dout.write("AUTH aydin\n".getBytes()); dout.flush(); } rcvd = din.readLine();
			 * System.out.println("server says: " + rcvd);
			 * 
			 * if (rcvd.equals("OK")) { dout.write("REDY\n".getBytes()); dout.flush(); }
			 */
			
			rcvd = readMSG(din);
			/*
			 * rcvd = din.readLine(); System.out.println("server says: " + rcvd);
			 */
			String firstjob = rcvd; // hold first job

			// get server DATA
			sendMSG("GETS All\n", dout);
			rcvd = readMSG(din);

			/*
			 * dout.write("GETS All\n".getBytes()); dout.flush(); rcvd = din.readLine();
			 * System.out.println("server says: " + rcvd);
			 */

			String[] Data = parsing(rcvd);
			// String[] Data = rcvd.split(delims);

			sendMSG("OK\n", dout);

			// dout.write("OK\n".getBytes());
			// dout.flush();

			// Initialise variable for server DATA
			int numServer = Integer.parseInt(Data[1]); // Number of servers on system.
			Server[] serverList = new Server[numServer]; // Create server array.

			// Loop through all servers to create server list
			for (int i = 0; i < numServer; i++) {
				rcvd = readMSG(din);
				// rcvd = din.readLine();
				// System.out.println( "loop " + i + " server says: " + " " + rcvd);
				String[] stringList = parsing(rcvd);
				// String[] stringList = rcvd.split(delims);
				serverList[i] = new Server(stringList[0], stringList[4]);

			}

			Arrays.sort(serverList); // Sort Servers

			// first largest server
			String highestCore = serverList[numServer - 1].getCore();
			int highestCoreIndex = numServer - 1;

			for (int i = numServer - 1; i >= 0; i--) {
				if (serverList[i].getCore() == highestCore) {
					highestCore = serverList[i].getCore();
					highestCoreIndex = i;
				} else {
					break;
				}
			}
			System.out.println("Largest in list is " + serverList[highestCoreIndex].getType() + " with "
					+ serverList[highestCoreIndex].getCore() + " cores.");

			// catch the "." at end of data stream.
			sendMSG("OK\n", dout);
			rcvd = readMSG(din);

			/*
			 * dout.write("OK\n".getBytes()); dout.flush(); rcvd = din.readLine();
			 * System.out.println("server says: after loop " + rcvd);
			 */

			rcvd = firstjob;
			// Handle jobs sent from server
			while (!rcvd.equals("NONE")) {
				String[] tokens = parsing(rcvd);
				// String[] tokens = rcvd.split(delims);
				switch (tokens[0]) {
				case "JOBN":
					String job = "SCHD" + " " + tokens[2] + " " + serverList[highestCoreIndex].getType() + " " + "0"
							+ "\n"; // "0" is hard coded so that it goes to the first of the largest servers every
									// time
					sendMSG(job, dout);
					// dout.write(job.getBytes());
					// dout.flush();
					break;
				case "JCPL":
					sendMSG("REDY\n", dout);
					// dout.write("REDY\n".getBytes());
					// dout.flush();
					break;
				case "OK":
					sendMSG("REDY\n", dout);
					// dout.write("REDY\n".getBytes());
					// dout.flush();
					break;
				}
				rcvd = readMSG(din);
				// rcvd = din.readLine();
			}

			// tell server to quit after no more jobs
			sendMSG("QUIT\n", dout);
			rcvd = readMSG(din);
			/*
			 * dout.write("QUIT\n".getBytes()); dout.flush(); rcvd = din.readLine();
			 * System.out.println("server says: " + rcvd);
			 */

			dout.close();
			s.close();

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
