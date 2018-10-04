package com.github.se7kn8.blockchain_simulation.blockchain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {

	public static class WrongHashException extends IllegalArgumentException {
		public WrongHashException() {
			super();
		}
		public WrongHashException(String message) {
			super(message);
		}
	}

	public static class BlockNotMinedException extends IllegalArgumentException {
		public BlockNotMinedException() {
			super();
		}

		public BlockNotMinedException(String message) {
			super(message);
		}
	}

	private List<Block> blocks;
	private int difficulty;

	public Blockchain(int startDifficulty, String... genesisBlockData) {
		this.blocks = new ArrayList<>();
		this.difficulty = startDifficulty;

		Block block = new Block("", TextBlockData.createFromValues(genesisBlockData));
		block.mineBlock(startDifficulty);
		blocks.add(block);
	}

	public void addBlock(Block block, boolean isMined) {
		if (!block.getPrevHash().equals(blocks.get(blocks.size() - 1).getHash())) {
			throw new WrongHashException("The block must contain the hash of the previous block");
		}
		if (!isMined) {
			block.mineBlock(difficulty);
		}

		if (block.getHash().startsWith("0".repeat(difficulty))) {
			blocks.add(block);
		} else {
			throw new BlockNotMinedException();
		}
	}

	public List<Block> getBlocks() {
		return List.copyOf(blocks);
	}

	public int getDifficulty() {
		return difficulty;
	}
}
