// TCP IO Client (Port 3000) - https://github.com/foxjony/nioserver

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/*
import java.awt.*;
import java.awt.event.*;
import javax.swing.JApplet;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
*/

public class Client {
	//String msg;

	public static void main(String[] args) {
		String host = "localhost";
		int port = 3000;
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		} else if (args.length == 2) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}

		try (
			Socket socket = new Socket(host, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		) {
			String inputLine = "";
			String word = "";

			System.out.println("Press id:");
			word = reader.readLine();    		// wait press text
			//out.println(word);         		// send no server
			//out.write(word+"\n");         	// send no server
			out.write("REG~1~"+word);         	// send no server
            out.flush();
			System.out.println("Send: REG~1~"+word);

			while ((inputLine = in.readLine()) != null) {
				System.out.println("Read: "+inputLine);

				//word = reader.readLine();    	// wait press text
				//out.println(word);         	// send no server
				//out.write(word+"\n");         // send no server
				//out.write(word);         		// send no server
            	//out.flush();
				//System.out.println("Send: "+word);

				//int number = Integer.valueOf(inputLine);
				//if (number >= 10) break;
				//number++;
				//out.println(number);
				//System.out.println("Send: "+number);
				//Thread.sleep(2000);
			}
			System.out.println("Disconnected");
		} catch (Throwable e) {
			System.out.println("Error: "+e.getMessage());
		}
	}

	/*
	// реализация всех трех методов интерфейса KeyListener
	// http://www.fandroid.info/sobytiya-i-slushateli-v-java/
	private class AppletKeyListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
			//showStatus("Key Down");				// отображение в строке состояния
			msg += e.getKeyText(e.getKeyCode());
        }
 
        public void keyReleased(KeyEvent e) {
            //showStatus("Key Up");				// отображение в строке состояния
        }
 
        public void keyTyped(KeyEvent e) {
            msg += e.getKeyChar();
            //repaint(); 							// перерисовать
			System.out.println(msg);
        }
	}
	*/
}
