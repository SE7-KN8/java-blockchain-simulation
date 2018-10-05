package com.github.se7kn8.blockchain_simulation;

import com.github.se7kn8.blockchain_simulation.blockchain.Block;
import com.github.se7kn8.blockchain_simulation.blockchain.Blockchain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class BlockchainSimulationTest {

	@Test
	void testBlockValidation() {
		for (int i = 0; i < 5; i++) {
			var block = new Block("test_block_validation", List.of());
			block.mineBlock(3);
			System.out.println("Hash is: " + block.getHash());
			assertTrue(block.getHash().startsWith("000"));
		}
	}

	@Test
	void testBlockchain() {
		var blockchain = new Blockchain(2, "test_data_1", "test_data_2", "test_data_3");
		assertEquals(2, blockchain.getDifficulty());
		assertEquals(1, blockchain.getBlocks().size());
		assertEquals(3, blockchain.getBlocks().get(0).getBlockData().size());
		assertEquals("test_data_1", blockchain.getBlocks().get(0).getBlockData().get(0).toString());
		assertEquals("test_data_2", blockchain.getBlocks().get(0).getBlockData().get(1).toString());
		assertEquals("test_data_3", blockchain.getBlocks().get(0).getBlockData().get(2).toString());

		System.out.println("Try to add block 1");
		var block1 = new Block(blockchain.getBlocks().get(blockchain.getBlocks().size() - 1).getHash(), List.of());
		blockchain.addBlock(block1, false);
		assertEquals(2, blockchain.getBlocks().size());

		System.out.println("Try to add block 2");
		var block2 = new Block(blockchain.getBlocks().get(blockchain.getBlocks().size() - 1).getHash(), List.of());
		block2.mineBlock(blockchain.getDifficulty());
		blockchain.addBlock(block2, true);
		assertEquals(3, blockchain.getBlocks().size());

		System.out.println("Try to add block 3");
		var block3 = new Block("", List.of());
		assertThrows(Blockchain.WrongHashException.class, () -> blockchain.addBlock(block3, false));
		assertEquals(3, blockchain.getBlocks().size());

		System.out.println("Try to add block 4");
		var block4 = new Block(blockchain.getBlocks().get(blockchain.getBlocks().size() - 1).getHash(), List.of());
		assertThrows(Blockchain.WrongHashException.class, () -> blockchain.addBlock(block4, true));
		assertEquals(3, blockchain.getBlocks().size());

		assertTrue(blockchain.isValid());
		System.out.println("Blockchain is valid!");
		System.out.println("Blockchain size: " + blockchain.getBlocks().size());
	}

}
