package com.github.se7kn8.blockchain_simulation.network.packages;

import com.github.se7kn8.blockchain_simulation.blockchain.Block;

public class AddBlockPacket extends Packet {

	private static final long serialVersionUID = 1;

	private final Block block;

	public AddBlockPacket(Block block) {
		this.block = block;
	}

	public Block getBlock() {
		return block;
	}
}
