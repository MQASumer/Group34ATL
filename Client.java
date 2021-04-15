import java.io.*;

import java.net.*;

public class Client {

	public static class Server implements Comparable<Server> { // Server class to hold server info
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
			// sort by core then type acending order.
			if (this.core - o.core == 0) {
				return o.Type.compareTo(this.Type);
			}
			return this.core - o.core;
		}

	}

	public static String[] parsing(String data) {
		String delims = "[ ]+"; // set the space as the splitting element for parsing messages.
		String[] splitData = data.split(delims);
		return splitData;
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

	public static void main(String[] args) {

		// TODO
		/*
		 * - handshake
		 */

		try {

			Socket s = new Socket("localhost", 50000);

			BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());

			// Handshake with server

			
			//hold first job for later
			rcvd = readMSG(din);
			String firstjob = rcvd;
			
			// Gets command to find the largest server

			sendMSG("GETS All\n", dout); // get server DATA
			rcvd = readMSG(din);
			String[] Data = parsing(rcvd); // parse DATA to find the amount of servers
			sendMSG("OK\n", dout);
			// Initialise variable for server DATA
			int numServer = Integer.parseInt(Data[1]); // Number of servers on system.
			Server[] serverList = new Server[numServer]; // Create server array.

			// Loop through all servers to create server list
			for (int i = 0; i < numServer; i++) {
				rcvd = readMSG(din);
				String[] stringList = parsing(rcvd);
				serverList[i] = new Server(stringList[0], stringList[4]);
			}

			Arrays.sort(serverList); // Sort Servers

			// find first largest server
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

			sendMSG("OK\n", dout); // catch the "." at end of data stream.
			rcvd = readMSG(din);

			// Scheduale jobs to server
			rcvd = firstjob; // start with first job recived.
			/*
			int i = 0;// Used to track number of jobs
			sendMSG("REDY\n", dout);// Inital REDY to get job from server
			*/
			
			while (!rcvd.equals("NONE")) {
				// String[] job = parsing(rcvd);  job[2] is job id
				while (!rcvd.equals("OK")) {
					if (rcvd.contains("JCPL")) { // Breaks loop if no more jobs to schedule and if jobs are waiting to
													// finish
						break;
					}
					sendMSG("SCHD " + i + " " + serverList[highestCoreIndex].Type + " " + serverList[highestCoreIndex].ID + "\n", dout); // Schedules
					// serverList[highestCoreIndex].ID may need to hardcoded to 0. needs testing.
																											// Job
																				// Server ID
					rcvd = readMSG(din);
					i++;
				}
				sendMSG("REDY\n", dout);
				rcvd = readMSG(din);
			}

			sendMSG("QUIT\n", dout);

			dout.close();
			s.close();

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
