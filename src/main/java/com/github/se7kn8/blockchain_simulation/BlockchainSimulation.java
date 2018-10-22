package com.github.se7kn8.blockchain_simulation;

import com.github.se7kn8.blockchain_simulation.command.ConsoleReader;
import com.github.se7kn8.blockchain_simulation.network.NetworkClient;
import com.github.se7kn8.blockchain_simulation.network.NetworkServer;
import com.github.se7kn8.blockchain_simulation.web.BlockchainWebServer;

public class BlockchainSimulation {
	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		ConsoleReader reader = new ConsoleReader(System.in);
		reader.start();
		BlockchainWebServer webServer = new BlockchainWebServer();
		webServer.start();
		new NetworkClient().registerCommands();
		NetworkServer server = new NetworkServer();
		server.start(7890);
	}
}