package com.github.se7kn8.blockchain_simulation;

import com.github.se7kn8.blockchain_simulation.blockchain.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class BlockchainSimulationTest {

	@Test
	void test() {
		Block block = new Block("genesis", List.of());
		System.out.println(block.getHash());
		block.mineBlock(4);
		System.out.println(block.getHash());
	}

	@Test
	void testBlockValidation() {
		for(int i = 0; i< 5; i++){
			Block block = new Block("test_block_validation", List.of());
			block.mineBlock(3);
			System.out.println("Hash is: "+ block.getHash());
			assertTrue(block.getHash().startsWith("000"));
		}
	}

}
