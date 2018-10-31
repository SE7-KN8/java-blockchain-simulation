package com.github.se7kn8.blockchain_simulation.network.client;

import com.github.se7kn8.blockchain_simulation.network.packages.ConnectPacket;
import com.github.se7kn8.blockchain_simulation.network.packages.Packet;
import com.github.se7kn8.blockchain_simulation.util.SocketWrapper;

public class NetworkClientSocketWrapper extends SocketWrapper {

	private boolean connected = false;
	private Runnable disconnectHandler;

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

		System.out.println("Received packet: " + packet);

		return true;
	}

	@Override
	protected void closeConnection() {
		disconnectHandler.run();
		super.closeConnection();
	}
}
