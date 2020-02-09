// TCP NIO Chat Server (Port 3000) - https://github.com/foxjony/nioserver

// REG~1~2
// SMS~1~2~text

import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.Scanner;
import java.util.Iterator;

public class NioChatServer implements Runnable {
	private final int port;
	private int clients;
	private ServerSocketChannel ssc;
	private Selector selector;
	private ByteBuffer buf = ByteBuffer.allocate(256);
	//private long tim_start = System.currentTimeMillis();

	NioChatServer(int port) throws IOException {
		this.port = port;
		this.ssc = ServerSocketChannel.open();
		this.ssc.socket().bind(new InetSocketAddress(port));
		this.ssc.configureBlocking(false);
		this.selector = Selector.open();
		this.ssc.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override public void run() {
		try {
			byte[] ip = new byte[4];
			ip = InetAddress.getLocalHost().getAddress();
			String localAddress = (0xff&(int)ip[0])+"."+(0xff&(int)ip[1])+"."+(0xff&(int)ip[2])+"."+(0xff&(int)ip[3]);
			System.out.println("Started NIO Chat Server "+localAddress+" port "+this.port);
			Iterator<SelectionKey> iter;
			SelectionKey key;

			while (this.ssc.isOpen()) {
				selector.select();
				iter = this.selector.selectedKeys().iterator();

				while (iter.hasNext()) {
					key = iter.next();
					iter.remove();
					if (key.isAcceptable()) this.handleAccept(key);
					if (key.isReadable()) this.handleRead(key);
				}
			}
		} catch (IOException e) {
			System.out.println("Port "+this.port+", Error ");
			e.printStackTrace();
		}
	}

	public boolean isNum(String str) {
		if (str == null || str.length() == 0) return false;
		int i = 0;
		//if (str.charAt(0) == '-') {
		//  if (str.length() == 1) return false;
		//  i = 1;
		//}
		char c;
		for (; i<str.length(); i++) {
			c = str.charAt(i);
			if (!(c >= '0' && c <= '9')) return false;
		}
		return true;
	}

	//private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome!\n".getBytes());

	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		String address = (new StringBuilder(sc.socket().getInetAddress().toString()))
			.append(":")
			.append(sc.socket().getPort()).toString();
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ, address);
		//sc.write(welcomeBuf);
		//welcomeBuf.rewind();
		clients++;
		long tim = System.currentTimeMillis();
		System.out.println(tim+address+" Connected ("+clients+")");
		broadcast(tim+address+" Connected ("+clients+")\n");
	}

	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel ch = (SocketChannel) key.channel();
		StringBuilder sb = new StringBuilder();
		String msg;
		int read = 0;
		buf.clear();

		try {
			while((read = ch.read(buf)) > 0) {
				buf.flip();
				byte[] bytes = new byte[buf.limit()];	// limit 32
				buf.get(bytes);
				sb.append(new String(bytes));
				buf.clear();
			}
		} catch (Exception e) {
			key.cancel();
			read = -1;
		}

		if ((sb.length() == 0) || (read == -1)) {
			if (clients > 0) clients--;
			msg = key.attachment()+" Disconnected ("+clients+")";
			ch.close();
		} else {
			msg = key.attachment()+" => "+sb.toString();
			if (msg.indexOf("REG~") == 0) {
				// Register Client
				String[] subStr = msg.split("~", 3);	// "REG~1~2"
				if (subStr.length == 3) {
					// city, st, uid, order
					int s = 0;
					if (isNum(subStr[1])) s++;
					if (isNum(subStr[2])) s++;
					if (s == 2) {
						/*
						list[clients] = new Id(key.attachment(), 
							Integer.parseInt(subStr[1]), 
							Integer.parseInt(subStr[2]));
						*/
						msg = key.attachment()+" => REG OK";
					} else {
						msg = key.attachment()+" ==> "+msg;
					}
				} else {
					msg = key.attachment()+" => "+msg;
				}
			} if (msg.indexOf("SMS~") == 0) {
				// Message to Client
				String[] subStr = msg.split("~", 4);	// "REG~1~2~text"
				if (subStr.length == 4) {
					// city, st, uid, order
					int s = 0;
					if (isNum(subStr[1])) s++;
					if (isNum(subStr[2])) s++;
					if (s == 2) {
						/*
						list[clients] = new Id(key.attachment(), 
							Integer.parseInt(subStr[1]), 
							Integer.parseInt(subStr[2]));
						*/
						msg = key.attachment()+" => SMS OK";
					} else {
						msg = key.attachment()+" ==> "+msg;
					}
				} else {
					msg = key.attachment()+" => "+msg;
				}
			} else {
				msg = key.attachment()+" => "+msg;
			}
		}

		long tim = System.currentTimeMillis();
		System.out.println(tim+msg);
		broadcast(tim+msg+"\n");
	}

	private void broadcast(String msg) throws IOException {
		ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
		int i = 0;
		for (SelectionKey key : selector.keys()) {
			if (key.isValid() && key.channel() instanceof SocketChannel) {
				SocketChannel sch = (SocketChannel) key.channel();
				sch.write(msgBuf);
				msgBuf.rewind();
				i++;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		int localPort = 3000;
		if (args.length == 1) localPort = Integer.parseInt(args[0]);
		NioChatServer server = new NioChatServer(localPort);
		(new Thread(server)).start();
	}
}

class Id {
	public Object addr;
	public int city;
	public int st;
	public int uid;
	public int order;

	/*
	public Id (int city, int st, int uid, int order) {
		//this.addr = addr;
		this.city = city;
		this.st = st;
		this.uid = uid;
		this.order = order;
	}
	*/
}
