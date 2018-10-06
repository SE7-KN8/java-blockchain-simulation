package com.github.se7kn8.blockchain_simulation;

import com.github.se7kn8.blockchain_simulation.web.BlockchainWebServer;

public class BlockchainSimulation {
	public static void main(String[] args) {
		BlockchainWebServer webServer = new BlockchainWebServer();
		webServer.start();
	}
}