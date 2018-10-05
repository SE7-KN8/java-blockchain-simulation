package com.github.se7kn8.blockchain_simulation.blockchain;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
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

	private static final HashFunction sha256 = Hashing.sha256();

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

		validateBlockHash(block);

		blocks.add(block);
	}

	public List<Block> getBlocks() {
		return List.copyOf(blocks);
	}

	public int getDifficulty() {
		return difficulty;
	}

	public boolean isValid() {
		for (int i = 0; i < blocks.size(); i++) {
			if (i == 0) {
				continue;
			}
			var block = blocks.get(i);
			try {
				validateBlockHash(block);
			} catch (WrongHashException e) {
				return false;
			}
			if (!block.getPrevHash().equals(blocks.get(i - 1).getHash())) {
				return false;
			}
		}
		return true;
	}

	private void validateBlockHash(Block block) {
		if (!block.getHash().startsWith("0".repeat(difficulty))) {
			throw new WrongHashException("Hash has to start with: " + "0".repeat(difficulty));
		} else if (!block.getHash().equals(sha256.hashString(block.getPrevHash() + block.getTimestamp() + block.getNonce() + block.getDataRootHash(), StandardCharsets.UTF_8).toString())) { //TODO validate data root hash
			throw new WrongHashException("Hash does not match block values");
		}
	}

}
