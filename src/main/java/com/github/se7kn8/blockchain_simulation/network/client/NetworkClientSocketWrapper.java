package com.github.se7kn8.blockchain_simulation.network.client;

import com.github.se7kn8.blockchain_simulation.network.SharedNetworkHandler;
import com.github.se7kn8.blockchain_simulation.network.packages.ConnectPacket;
import com.github.se7kn8.blockchain_simulation.network.packages.Packet;
import com.github.se7kn8.blockchain_simulation.network.server.NetworkServer;
import com.github.se7kn8.blockchain_simulation.util.IDHandler;
import com.github.se7kn8.blockchain_simulation.util.SocketWrapper;

public class NetworkClientSocketWrapper extends SocketWrapper {

	private boolean connected = false;
	private Runnable disconnectHandler;
	private NetworkServer server;
	private SharedNetworkHandler handler;

	public NetworkClientSocketWrapper(String host, int ip, Runnable disconnectHandler) {
		super(host, ip);
		this.disconnectHandler = disconnectHandler;
		sendPacket(new ConnectPacket(ConnectPacket.TRY_TO_CONNECT));
		this.registerCustomPacketHandler(ConnectPacket.class, this::handleConnectPacket);
		this.start();
	}

	private boolean handleConnectPacket(Packet p) {
		if (!connected && ((ConnectPacket) p).getState() == ConnectPacket.SUCCESSFUL_CONNECTED) {
			connected = true;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handlePacket(Packet packet) {
		if (!connected) {
			return false;
		}

		System.out.println("[ClientWrapper] Received packet: " + packet);
		handler.handlePacket(packet);
		System.out.println("[ClientWrapper] Broadcast packet to local server");
		broadcastPacketToNetwork(packet, false);

		return true;
	}

	@Override
	protected void closeConnection() {
		disconnectHandler.run();
		super.closeConnection();
	}

	public void setLocalServer(NetworkServer server) {
		this.server = server;
	}

	public NetworkServer getServer() {
		return server;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setHandler(SharedNetworkHandler handler) {
		this.handler = handler;
	}

	public void broadcastPacketToNetwork(Packet packet, boolean currentConnected) {
		if (currentConnected && this.connected) {
			sendPacket(packet);
		}
		if (!packet.getSender().equals(IDHandler.PROGRAM_ID) && server != null) {
			server.broadcastPacket(packet);
		} else {
			System.out.println("[ClientWrapper] Skipped packet to prevent packet loop");
		}
	}
}
