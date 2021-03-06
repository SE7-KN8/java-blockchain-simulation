package com.github.se7kn8.blockchain_simulation.network.server;

import com.github.se7kn8.blockchain_simulation.command.CommandHandler;
import com.github.se7kn8.blockchain_simulation.command.CommandSender;
import com.github.se7kn8.blockchain_simulation.network.SharedNetworkHandler;
import com.github.se7kn8.blockchain_simulation.network.client.NetworkClientSocketWrapper;
import com.github.se7kn8.blockchain_simulation.network.packages.Packet;
import com.github.se7kn8.blockchain_simulation.network.packages.TestPacket;
import com.github.se7kn8.blockchain_simulation.util.IOThread;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NetworkServer extends IOThread {

	private boolean started;
	private ServerSocket socket;
	private Thread serverThread;
	private NetworkClientSocketWrapper localClient;
	private SharedNetworkHandler handler;
	private Set<ServerClientHandler> connectedClients = new HashSet<>();

	private Executor executor = Executors.newCachedThreadPool();

	public void start(int port) {
		try {
			socket = new ServerSocket(port);
			serverThread = new Thread(this);
			serverThread.setName("Server thread");
			serverThread.start();
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
		throw new RuntimeException("Error while server run!", e);
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

	public synchronized void broadcastPacket(Packet packet) {
		this.connectedClients.stream().filter(client -> !client.getSocket().isClosed()).forEach(client -> client.sendPacket(packet));
	}

	public Set<ServerClientHandler> getConnectedClients() {
		return connectedClients;
	}

	public void registerCommands() {
		CommandHandler.getInstance().addStopHandler("network-server", s -> {
			s.message("Stopping server");
			stopServer();
		});
		CommandHandler.getInstance().registerCommand(LiteralArgumentBuilder.<CommandSender>literal("send-test-packet-server").executes(c -> {
			broadcastPacket(new TestPacket());
			return 1;
		}));
	}

	public void setLocalClient(NetworkClientSocketWrapper localClient) {
		this.localClient = localClient;
	}

	public NetworkClientSocketWrapper getLocalClient() {
		return localClient;
	}

	public void setHandler(SharedNetworkHandler handler) {
		this.handler = handler;
	}

	public SharedNetworkHandler getHandler() {
		return handler;
	}
}
