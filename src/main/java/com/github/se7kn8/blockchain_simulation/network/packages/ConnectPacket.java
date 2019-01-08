package com.github.se7kn8.blockchain_simulation.network.packages;

public class ConnectPacket extends Packet {
	private static final long serialVersionUID = 1;

	private final int STATE;
	private final String MESSAGE;

	public transient static final int TRY_TO_CONNECT = 0;
	public transient static final int SUCCESSFUL_CONNECTED = 1;
	public transient static final int CONNECTION_TERMINATED = 2;


	public ConnectPacket(int state) {
		this.STATE = state;
		this.MESSAGE = "";
	}

	public ConnectPacket(int state, String message) {
		this.STATE = state;
		this.MESSAGE = message;
	}

	public int getState() {
		return STATE;
	}

	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public String toString() {
		return "Packet[name=CONNECT_PACKET state=" + getState() + " message=" + getMessage() + "]";
	}
}
