package jp.colors.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer implements Runnable  {
	static Object _lockobj = new Object(), _lockobj2 = new Object();
	static int _counter = 0;
	Socket _sock = null;
	public TestServer(Socket sock) {
		_sock = sock;
		Thread thread = new Thread(this);
		thread.start();
	}

	void println(String s) {
		synchronized(_lockobj) {
			System.out.println(s);
		}
	}

	int getCount() {
		synchronized(_lockobj2) {
			return ++_counter;
		}
	}

	public void run() {
		doServer();
	}

	public void doServer() {
		try {
			OutputStream ost =  _sock.getOutputStream();
			InputStream ist = _sock.getInputStream();
			byte[] buffer = new byte[1024];
			int nread;
			while((nread = ist.read(buffer))> 0) {
				if(buffer[nread - 1] == 0)
					break;
			}

			buffer = "data is okay".getBytes();
			ost.write(buffer);
			ist.close();
			ost.close();
			String msg = String.valueOf(getCount() ) + " had done.";
			println(msg);

		} catch(Exception e) {
			println(e.getMessage());
		}finally {
			try {	_sock.close();	} catch (IOException e) {
				println("socket close failer");
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		if(args.length == 0) {
			ServerSocket sock = null;
			try {
				sock = new ServerSocket(5015, 2048);

				while(true) {
					Socket client =  sock.accept();
					new TestServer(client);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(sock != null)
					try {
						sock.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		} else {
			System.out.println("usage TestServer ipaddress [count=10] [size=100K]");
			int count = 10, size=100*1024;
			if(args.length > 1) {
				try { count = Integer.parseInt(args[1]); } catch(Exception e) {}
			}
			if(args.length > 2) {
				try { size = Integer.parseInt(args[2]); } catch(Exception e) {}
			}
			new TestServer(args[0], count, size);
		}
	}

	byte[] _buffer = null;


	public TestServer(String ipaddr, int count, int size) {
		_buffer = new byte[size + 1];
		for(int i = 0; i < size; i++) {
			_buffer[i] = (byte)('A' + (i % 26));
		}
		_buffer[size]  = 0;
		final String fipaddr= ipaddr;
		for(int i = 0; i < count; i++) {
			Runnable r = new Runnable() {
					public void run() {
						TestServer.this.doClient(fipaddr, 5015);
					}
				};
			(new Thread(r)).start();
		}
	}

	public void doClient(String ipaddr, int port) {
		Socket sock = null;
		OutputStream ost = null;
		InputStream ist = null;
		try {
			sock = new Socket(ipaddr, 5015);
			 ost = sock.getOutputStream();
			 ist = sock.getInputStream();
			ost.write(_buffer);
			byte[] ret = new byte[12];
			ist.read(ret);
			String sret = new String(ret);
			if("data is okay".equals(sret)) {
				println("send done");
			} else
				println("send error");

		} catch(Exception e) {
			println(e.getMessage());
		} finally {
			if(ost != null)
				try {
					ost.close();
				} catch (IOException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			if(ist != null)
				try {
					ist.close();
				} catch (IOException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			if(sock != null)
				try {	sock.close();		} catch (IOException e) {	e.printStackTrace();		}
		}
	}

}
