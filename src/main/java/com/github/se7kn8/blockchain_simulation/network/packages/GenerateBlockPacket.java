package com.github.se7kn8.blockchain_simulation.network.packages;

import com.github.se7kn8.blockchain_simulation.blockchain.BlockData;

import java.util.List;

public class GenerateBlockPacket extends Packet {
	private static final long serialVersionUID = 1;

	private final int difficulty;
	private final List<? extends BlockData> blockData;

	public GenerateBlockPacket(int difficulty, List<? extends BlockData> blockData) {
		this.difficulty = difficulty;
		this.blockData = blockData;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public List<? extends BlockData> getBlockData() {
		return blockData;
	}
}
