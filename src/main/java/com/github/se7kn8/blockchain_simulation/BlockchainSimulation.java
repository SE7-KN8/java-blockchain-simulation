package com.github.se7kn8.blockchain_simulation;

import com.github.se7kn8.blockchain_simulation.blockchain.Blockchain;
import com.github.se7kn8.blockchain_simulation.command.ConsoleReader;
import com.github.se7kn8.blockchain_simulation.network.NetworkHandler;
import com.github.se7kn8.blockchain_simulation.network.client.NetworkClient;
import com.github.se7kn8.blockchain_simulation.network.server.NetworkServer;
import com.github.se7kn8.blockchain_simulation.util.IDHandler;
import com.github.se7kn8.blockchain_simulation.web.BlockchainWebServer;

public class BlockchainSimulation {
	//start with 'java <filename>.jar <Network-Port> <WebServer-Port>'
	public static void main(String[] args) {
		int port;
		int wsPort;
		if (args.length >= 1) {
			port = checkIfPort(args[0]);
			System.out.println("Using port " + port);
		} else {
			port = 7890;
			System.out.println("Using default port 7890");
		}
		if (args.length >= 2) {
			wsPort = checkIfPort(args[1]);
			System.out.println("Using web server port " + wsPort);
		} else {
			wsPort = 7000;
			System.out.println("Using default web server port 7000");
		}


		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		ConsoleReader reader = new ConsoleReader(System.in);
		reader.start();

		System.out.println("[Info] Current random id is: " + IDHandler.PROGRAM_ID.toString());

		Blockchain blockchain = new Blockchain(4);
		// TODO just for tests
		//blockchain.addBlock(new Block("", new ArrayList<>()), false);
		//for (int i = 0; i < 39; i++) {
		//	blockchain.addBlock(new Block(blockchain.getBlocks().get(blockchain.getBlocks().size() - 1).getHash(), TextBlockData.createFromValues("Block ", "data", "" + i)), false);
		//}

		BlockchainWebServer webServer = new BlockchainWebServer(wsPort, blockchain);
		webServer.start();
		NetworkServer localServer = new NetworkServer();
		NetworkClient localClient = new NetworkClient(localServer, blockchain);

		NetworkHandler handler = new NetworkHandler(blockchain, packet -> {
			if (localClient.isConnected()) {
				localClient.getWrapper().broadcastPacketToNetwork(packet, true);
			} else {
				localServer.broadcastPacket(packet);
			}
		});
		localClient.setHandler(handler);
		localServer.setHandler(handler);

		localClient.registerCommands();
		localServer.registerCommands();

		localServer.start(port);
	}

	private static int checkIfPort(String arg) {
		try {
			int port = Integer.valueOf(arg);
			if (port < 1 || port > 65535) {
				throw new RuntimeException();
			}
			return port;
		} catch (Exception e) {
			throw new IllegalArgumentException("Port must in range between 1 and 65535", e);
		}
	}

}