// TCP Client Test (Port 4040)

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
	public static void main(String[] args) {
		try (
			Socket socket = new Socket("192.168.0.5", 4040);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		) {
			String inputLine;
			out.println(1);
			System.out.println("Send: 1");

			while ((inputLine = in.readLine()) != null) {
				System.out.println("Read: "+inputLine);
				int number = Integer.valueOf(inputLine);
				if (number >= 10) break;
				number++;
				out.println(number);
				System.out.println("Send: "+number);
				//Thread.sleep(2000);
			}
			System.out.println("Disconnected");
		} catch (Throwable e) {
			System.out.println("Error: "+e.getMessage());
		}
	}
}
