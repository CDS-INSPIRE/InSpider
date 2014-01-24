package nl.ipo.cds.etl.test.protocol.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
	
	private static long counter = 0;
	
	private static synchronized void incrementCounter() {
		counter++;
	}
	
	public static synchronized void resetCounter() {
		counter = 0;
	}
	
	public static synchronized long getCounter() {
		return counter;
	}

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		
		incrementCounter();
		
		return new URLConnection(u) {

			@Override
			public void connect() throws IOException {
				if(url.getFile().toLowerCase().contains("404")) {
					throw new IOException();
				}
			}

			@Override
			public InputStream getInputStream() throws IOException {
				connect();
				
				if(url.getFile().toLowerCase().contains("empty")) {
					return new ByteArrayInputStream("".getBytes("utf-8"));
				}

				return new ByteArrayInputStream(url.toExternalForm().getBytes("utf-8"));
			}
		};
	}
}
