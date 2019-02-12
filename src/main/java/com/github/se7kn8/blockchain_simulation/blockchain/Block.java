package com.github.se7kn8.blockchain_simulation.blockchain;

import com.github.se7kn8.blockchain_simulation.util.StringUtil;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Block implements Serializable {

	public static boolean showGenerationInfo = false;
	private static final long serialVersionUID = 1;
	private transient static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
	private transient static final HashFunction sha256 = Hashing.sha256();
	private transient static final Random rand = new Random();

	//Header
	private String hash;
	private String prevHash;
	private String timestamp;
	private int nonce;

	//Body
	private String dataRootHash;
	private List<? extends BlockData> blockData;

	public Block(String prevHash, List<? extends BlockData> blockData) {
		this.prevHash = prevHash;
		this.timestamp = sdf.format(new Date());
		this.nonce = 0;
		this.hash = "";
		this.blockData = blockData;

		StringBuilder dataRootHashBuilder = new StringBuilder();
		for (BlockData data : blockData) {
			dataRootHashBuilder.append(data.getHash());
		}
		this.dataRootHash = sha256.hashString(dataRootHashBuilder.toString(), StandardCharsets.UTF_8).toString();
	}

	private String generateCurrentBlockHash(int difficulty) {
		String currentBlockHash;

		int hashCounter = 0;
		while (true) {
			String hash = sha256.hashString(prevHash + timestamp + nonce + dataRootHash, StandardCharsets.UTF_8).toString();
			if (showGenerationInfo) {
				System.out.println("[Block] Got hash: " + hash);
			}
			if (hash.startsWith(StringUtil.generateDifficultyString(difficulty))) {
				currentBlockHash = hash;
				if (showGenerationInfo) {
					System.out.println("[Block] " + hashCounter + " iterations to got current hash");
				}
				break;
			}
			nonce = rand.nextInt();
			hashCounter++;
		}
		return currentBlockHash;
	}

	public void mineBlock(int difficulty) {
		this.hash = generateCurrentBlockHash(difficulty);
	}

	public String getHash() {
		return hash;
	}

	public String getPrevHash() {
		return prevHash;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public int getNonce() {
		return nonce;
	}

	public String getDataRootHash() {
		return dataRootHash;
	}

	public List<? extends BlockData> getBlockData() {
		return Collections.unmodifiableList(blockData);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Block block = (Block) o;
		return nonce == block.nonce &&
				Objects.equals(hash, block.hash) &&
				Objects.equals(prevHash, block.prevHash) &&
				Objects.equals(timestamp, block.timestamp) &&
				Objects.equals(dataRootHash, block.dataRootHash) &&
				Objects.equals(blockData, block.blockData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hash, prevHash, timestamp, nonce, dataRootHash, blockData);
	}
}
