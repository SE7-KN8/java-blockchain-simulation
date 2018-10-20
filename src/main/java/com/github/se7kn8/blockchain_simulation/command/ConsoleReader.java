package com.github.se7kn8.blockchain_simulation.command;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConsoleReader implements Runnable, CommandSender {

	private BufferedReader reader;
	private Thread consoleReaderThread;

	public ConsoleReader(InputStream consoleStream) {
		reader = new BufferedReader(new InputStreamReader(consoleStream));
		consoleReaderThread = new Thread(this);
		consoleReaderThread.setName("Console reader");
		consoleReaderThread.setDaemon(true);
	}

	public void start() {
		consoleReaderThread.start();
	}

	@Override
	public void run() {
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				CommandHandler.getInstance().parseCommand(line, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void message(String message) {
		System.out.println(message);
	}
}
