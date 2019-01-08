package com.github.se7kn8.blockchain_simulation.network.server;

import com.github.se7kn8.blockchain_simulation.network.packages.ConnectPacket;
import com.github.se7kn8.blockchain_simulation.network.packages.Packet;
import com.github.se7kn8.blockchain_simulation.util.IDHandler;
import com.github.se7kn8.blockchain_simulation.util.SocketWrapper;

import java.net.Socket;


public class ServerClientHandler extends SocketWrapper {

	private NetworkServer server;
	private boolean connected = false;

	public ServerClientHandler(Socket client, NetworkServer server) {
		super(client);
		System.out.println("[ServerClientHandler] Connected to client with remote ip " + client.getInetAddress().getCanonicalHostName() + " and port " + client.getPort());
		this.server = server;
		this.server.addClient(this);
		this.registerCustomPacketHandler(ConnectPacket.class, this::handleConnectPacket);
		this.start();
	}

	@Override
	public boolean handlePacket(Packet packet) {
		if (!connected) {
			return false;
		}
		//TODO just for tests
		System.out.println("[ServerClientHandler] Received packet: " + packet);
		System.out.println("[ServerClientHandler] Broadcast packet " + packet.getClass() + " to all clients");
		server.getConnectedClients().forEach(serverClientHandler -> {
			if (serverClientHandler != this) {
				serverClientHandler.sendPacket(packet);
			}
		});
		if (!packet.getSender().equals(IDHandler.PROGRAMM_ID)) {
			if (server.getLocalClient() != null) {
				if (server.getLocalClient().isConnected()) {
					System.out.println("[ServerClientHandler] Broadcast packet to local client");
					server.getLocalClient().sendPacket(packet);
				}
			} else {
				System.err.println("[ServerClientHandler] Can't broadcast packet to local client, because the client is not connect");
			}
		} else {
			System.out.println("[ServerClientHandler] Skipped packet to prevent packet loop");
		}

		return true;
	}

	private boolean handleConnectPacket(Packet p) {
		if (connected) {
			return false;
		} else {
			ConnectPacket packet = (ConnectPacket) p;

			if (packet.getState() == ConnectPacket.TRY_TO_CONNECT) {
				connected = true;
				sendPacket(new ConnectPacket(ConnectPacket.SUCCESSFUL_CONNECTED));
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void closeConnection() {
		super.closeConnection();
		this.server.removeClient(this);
	}
}
