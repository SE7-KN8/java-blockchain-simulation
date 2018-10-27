package com.github.se7kn8.blockchain_simulation.util;

import com.github.se7kn8.blockchain_simulation.network.packages.ConnectPacket;
import com.github.se7kn8.blockchain_simulation.network.packages.Packet;

import java.io.Closeable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

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
			e.printStackTrace();
			closeConnection();
		}
	}


	private class InputThread extends IOThread {

		@Override
		public void runWithExceptions() throws Exception {
			while (!this.isInterrupted()) {
				Object o = input.readObject();
				if (o instanceof Packet) {
					if (!customPacketHandlers.getOrDefault(((Packet) o).getClass(), SocketWrapper.this::handlePacket).apply((Packet) o)) {
						SocketWrapper.this.sendPacket(new ConnectPacket(ConnectPacket.CONNECTION_TERMINATED, "Invalid packet or invalid packet state!"));

						while (!SocketWrapper.this.getSocket().isClosed() && SocketWrapper.this.getPacketsToSend().peek() == null && !SocketWrapper.this.outputThread.isInterrupted()) {
							Thread.onSpinWait();
						}
						closeConnection();

					}

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

	private Map<Class<? extends Packet>, Function<Packet, Boolean>> customPacketHandlers = new ConcurrentHashMap<>();

	private static AtomicInteger counter = new AtomicInteger(0);

	public SocketWrapper(Socket socket) {
		this.socket = socket;
	}

	public void start(){
		int number = counter.getAndIncrement();

		try {
			SocketWrapper.this.output = new ObjectOutputStream(SocketWrapper.this.socket.getOutputStream());
			SocketWrapper.this.input = new ObjectInputStream(SocketWrapper.this.socket.getInputStream());
		} catch (Exception e) {
			close();
			throw new IllegalStateException("Failed to create streams", e);
		}

		outputThread = new Thread(new OutputThread());
		inputThread = new Thread(new InputThread());

		outputThread.setName("socket-wrapper-" + number + "-output");
		inputThread.setName("socket-wrapper-" + number + "-input");

		outputThread.start();
		inputThread.start();
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

	public abstract boolean handlePacket(Packet packet);

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

	public void registerCustomPacketHandler(Class<? extends Packet> packetClass, Function<Packet, Boolean> handler) {
		if (customPacketHandlers.keySet().contains(packetClass)) {
			throw new IllegalArgumentException("Custom packet handle for packet '" + packetClass.getSimpleName() + "' is already defined!");
		}
		customPacketHandlers.put(packetClass, handler);
	}
}
