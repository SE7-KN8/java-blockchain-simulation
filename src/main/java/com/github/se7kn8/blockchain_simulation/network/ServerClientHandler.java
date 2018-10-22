package com.github.se7kn8.blockchain_simulation.network;

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
	}

	@Override
	public void handlePacket(Packet packet) {
		if (!connected) {
			if (packet instanceof ConnectPacket && ((ConnectPacket) packet).getState() == ConnectPacket.TRY_TO_CONNECT) {
				connected = true;
				sendPacket(new ConnectPacket(ConnectPacket.SUCCESSFUL_CONNECTED));
			} else {
				sendPacket(new ConnectPacket(ConnectPacket.CONNECTION_TERMINATED, "Invalid packet or invalid packet state!"));

				while (!getSocket().isClosed() && getPacketsToSend().peek() == null) {
					Thread.onSpinWait();
				}
				close();
			}
		}
	}

	@Override
	protected void closeConnection() {
		super.closeConnection();
		this.server.removeClient(this);
	}
}
