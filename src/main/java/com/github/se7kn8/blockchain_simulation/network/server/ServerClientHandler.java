package com.github.se7kn8.blockchain_simulation.network.server;

import com.github.se7kn8.blockchain_simulation.network.packages.ConnectPacket;
import com.github.se7kn8.blockchain_simulation.network.packages.Packet;
import com.github.se7kn8.blockchain_simulation.util.SocketWrapper;

import java.net.Socket;


public class ServerClientHandler extends SocketWrapper {

	private NetworkServer server;
	private boolean connected = false;

	public ServerClientHandler(Socket client, NetworkServer server) {
		super(client);
		System.out.println("Connected to client with remote ip " + client.getInetAddress().getCanonicalHostName() + " and port " + client.getPort());
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
