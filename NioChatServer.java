// TCP NIO Chat Server (Port 3000) (8.03.2020)
// https://github.com/foxjony/NioChatServer
// https://www.codeflow.site/ru/article/java-nio-selector

// M~13~3336~6~1~0~12~555~R 		(M, obl, city, st, typ,   0, id, order, msg) Register
// M~13~3336~6~1~13~12~555~text 	(M, obl, city, st, typ, uid, id, order, msg) Message

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
	//private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome!\n".getBytes());

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
		char c;
		for (; i<str.length(); i++) {
			c = str.charAt(i);
			if (!(c >= '0' && c <= '9')) return false;
		}
		return true;
	}

	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		//String address = (new StringBuilder(sc.socket().getInetAddress().toString())).append(":").append(sc.socket().getPort()).toString();
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ, "0~0~0~0~0");	// "obl~city~st~typ~id"
		//sc.write(welcomeBuf);
		//welcomeBuf.rewind();
		clients++;
		long tim = System.currentTimeMillis();
		//System.out.println(tim+": "+address+" Connected ("+clients+")");
		System.out.println(tim+": Connected ("+clients+")");
		//broadcast(0, 0, 0, 0, 0, 0, 0, tim+": "+address+" Connected ("+clients+")\n");
		//broadcast(0, tim+": "+address+" Connected ("+clients+")\n");
	}

	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel ch = (SocketChannel) key.channel();
		StringBuilder sb = new StringBuilder();
		long tim;
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
			tim = System.currentTimeMillis();
			System.out.println(tim+": Disconnected ("+clients+")");
			ch.close();
		} else {
			if (sb.toString().indexOf("M~") == 0) {
				// "M~13~3336~6~1~13~12~555~text" (M, obl, city, st, typ, uid, id, order, msg)
				String[] subStr = sb.toString().split("~", 9);
				if (subStr.length == 9) {
					int s = 0;
					if (isNum(subStr[1])) s++;
					if (isNum(subStr[2])) s++;
					if (isNum(subStr[3])) s++;
					if (isNum(subStr[4])) s++;
					if (isNum(subStr[5])) s++;
					if (isNum(subStr[6])) s++;
					if (isNum(subStr[7])) s++;
					if (s == 7) {
						int obl = Integer.parseInt(subStr[1]);
						int city = Integer.parseInt(subStr[2]);
						int st = Integer.parseInt(subStr[3]);
						int typ = Integer.parseInt(subStr[4]);
						int uid = Integer.parseInt(subStr[5]);
						int id = Integer.parseInt(subStr[6]);
						int order = Integer.parseInt(subStr[7]);
						//String msg = subStr[8];
						//key.attach(subStr[6]);
						if (order == 0) {
							// "obl~city~st~typ~id"
							key.attach(subStr[1]+"~"+subStr[2]+"~"+subStr[3]+"~"+subStr[4]+"~"+subStr[6]);
							broadcast(0, 0, id, "ok\n");
						} else {
							//broadcast(obl, typ, uid, msg+"\n");
							broadcast(obl, typ, uid, sb+"\n");
						}
						tim = System.currentTimeMillis();
						System.out.println(tim+": "+sb);
					}
				}
			}
		}
	}

	// broadcast(int obl, int city, int st, int typ, int uid, int id, int order, String msg)
	private void broadcast(int obl, int typ, int uid, String msg) throws IOException {
		ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
		String user;
		int obl2, typ2, id2;	// city2, st2
		//int id;
		for (SelectionKey key : selector.keys()) {
			if (key.isValid() && key.channel() instanceof SocketChannel) {
				SocketChannel sch = (SocketChannel) key.channel();
				//id = Integer.parseInt(key.attachment().toString());
				user = key.attachment().toString();
				String[] data = user.toString().split("~", 5);	// "obl~city~st~typ~id"
				if (data.length == 5) {
					obl2 = Integer.parseInt(data[0]);
					//city2 = Integer.parseInt(data[1]);
					//st2 = Integer.parseInt(data[2]);
					typ2 = Integer.parseInt(data[3]);
					id2 = Integer.parseInt(data[4]);
					if (uid != 0) {
						if (uid == id2) {
							sch.write(msgBuf);
							msgBuf.rewind();
							break;
						}
					} else {
						if ((obl == obl2) && (typ == typ2)) {
							sch.write(msgBuf);
							msgBuf.rewind();
						}
					}
				}
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
