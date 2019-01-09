package com.github.se7kn8.blockchain_simulation.network.packages;

public class ConnectPacket extends Packet {
	private static final long serialVersionUID = 1;

	private final int state;
	private final String message;

	public transient static final int TRY_TO_CONNECT = 0;
	public transient static final int SUCCESSFUL_CONNECTED = 1;
	public transient static final int CONNECTION_TERMINATED = 2;


	public ConnectPacket(int state) {
		this.state = state;
		this.message = "";
	}

	public ConnectPacket(int state, String message) {
		this.state = state;
		this.message = message;
	}

	public int getState() {
		return state;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "Packet[name=CONNECT_PACKET state=" + getState() + " message=" + getMessage() + "]";
	}
}
