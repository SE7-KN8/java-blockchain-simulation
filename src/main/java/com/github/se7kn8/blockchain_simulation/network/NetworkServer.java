package com.github.se7kn8.blockchain_simulation.network;

import com.github.se7kn8.blockchain_simulation.network.packages.Packet;
import com.github.se7kn8.blockchain_simulation.network.packages.TestPacket;
import com.github.se7kn8.blockchain_simulation.util.IOThread;

import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NetworkServer extends IOThread {

	private boolean started;
	private ServerSocket socket;
	private Thread serverThread;
	private Set<ServerClientHandler> connectedClients = new HashSet<>();

	private Executor executor = Executors.newCachedThreadPool();

	public void start(int port) {
		try {
			socket = new ServerSocket(port);
			serverThread = new Thread(this);
			serverThread.setName("Server thread");
			serverThread.start();

			new Thread(() -> {

				try {
					while(true){
						NetworkServer.this.broadcastPacket(new TestPacket());
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void runWithExceptions() throws Exception {
		while (!this.isInterrupted()) {
			new ServerClientHandler(socket.accept(), this);
		}
	}

	@Override
	public void handleException(Exception e) {
		e.printStackTrace();
	}

	public void stopServer() {
		try {
			socket.close();
			serverThread.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void addClient(ServerClientHandler client) {
		this.connectedClients.add(client);
	}

	public synchronized void removeClient(ServerClientHandler client) {
		this.connectedClients.remove(client);
	}

	public void broadcastPacket(Packet packet) {
		this.connectedClients.stream().filter(client -> !client.getSocket().isClosed()).forEach(client -> client.sendPacket(packet));
	}

}
