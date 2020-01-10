// NIO Server Test

import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.Scanner;
import java.util.Iterator;

public class NioServerTest implements Runnable {
	private final int port;
	private int clients;
	private ServerSocketChannel ssc;
	private Selector selector;
	private ByteBuffer buf = ByteBuffer.allocate(32);	// (256)

	NioServerTest(int port) throws IOException {
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
			System.out.println("Run NIO Server "+localAddress+":"+this.port);
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
		//broadcast(tim+address+" Connected ("+clients+")\n");
	}

	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel ch = (SocketChannel) key.channel();
		StringBuilder sb = new StringBuilder();
		//String msg;
		int i = 0;
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
			//msg = key.attachment()+" Disconnected ("+clients+")";
			ch.close();
		} else {
			//msg = key.attachment()+" => "+sb.toString();
			i = Integer.parseInt(sb.toString())+1;
		}

		//long tim = System.currentTimeMillis();
		//System.out.println(tim+msg);
		//broadcast(tim+msg+"\n");

		System.out.println(i);
		broadcast(i+"\n");
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
		int localPort = Integer.parseInt(args[0]);
		NioServerTest server = new NioServerTest(localPort);
		(new Thread(server)).start();
	}
}