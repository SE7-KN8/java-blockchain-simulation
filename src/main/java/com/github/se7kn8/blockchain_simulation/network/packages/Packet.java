package com.github.se7kn8.blockchain_simulation.network.packages;

import com.github.se7kn8.blockchain_simulation.util.IDHandler;

import java.io.Serializable;
import java.util.UUID;

public abstract class Packet implements Serializable {
	private UUID PROGRAM_UUID = IDHandler.PROGRAMM_ID;

	public UUID getSender() {
		return PROGRAM_UUID;
	}
}
