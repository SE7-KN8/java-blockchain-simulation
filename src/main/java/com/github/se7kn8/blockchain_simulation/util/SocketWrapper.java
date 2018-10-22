package com.github.se7kn8.blockchain_simulation.util;

import com.github.se7kn8.blockchain_simulation.network.packages.Packet;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class SocketWrapper implements Closeable {

	private class OutputThread extends IOThread {

		@Override
		public void runWithExceptions() throws Exception {
			while (!this.isInterrupted()) {
				if (SocketWrapper.this.packetsToSend.peek() == null) {
					Thread.sleep(1); //Lower CPU usage
				} else {
					Packet packet = SocketWrapper.this.packetsToSend.poll();

					output.writeObject(packet);
				}
			}
		}

		@Override
		public void handleException(Exception e) {
			closeConnection();
		}
	}


	private class InputThread extends IOThread {

		@Override
		public void runWithExceptions() throws Exception {
			while (!this.isInterrupted()) {
				Object o = input.readObject();
				if (o instanceof Packet) {
					handlePacket(((Packet) o));
				} else {
					throw new IllegalStateException("Received packet is not an instance of " + Packet.class.getSimpleName());
				}
			}
		}

		@Override
		public void handleException(Exception e) {
			closeConnection();
		}
	}


	private Socket socket;
	private Queue<Packet> packetsToSend = new ConcurrentLinkedQueue<>();

	private Thread outputThread;
	private Thread inputThread;

	private ObjectOutputStream output;
	private ObjectInputStream input;

	private static int counter = 0;

	public SocketWrapper(Socket socket) {
		this.socket = socket;

		try {
			SocketWrapper.this.output = new ObjectOutputStream(SocketWrapper.this.socket.getOutputStream());
			SocketWrapper.this.input = new ObjectInputStream(SocketWrapper.this.socket.getInputStream());
		} catch (Exception e) {
			close();
			throw new IllegalStateException("Failed to create streams", e);
		}

		outputThread = new Thread(new OutputThread());
		inputThread = new Thread(new InputThread());

		outputThread.setName("socket-wrapper-" + counter + "-output");
		inputThread.setName("socket-wrapper-" + counter + "-input");

		outputThread.start();
		inputThread.start();
		counter++;
	}

	public void sendPacket(Packet packet) {
		this.packetsToSend.add(packet);
	}

	protected void closeConnection() {
		try {
			socket.close();
			outputThread.interrupt();
			inputThread.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void handlePacket(Packet packet);

	@Override
	public void close() {
		closeConnection();
	}

	public Queue<Packet> getPacketsToSend() {
		return packetsToSend;
	}

	public Socket getSocket() {
		return socket;
	}
}
