package com.github.se7kn8.blockchain_simulation.network;

import com.github.se7kn8.blockchain_simulation.blockchain.Block;
import com.github.se7kn8.blockchain_simulation.blockchain.Blockchain;
import com.github.se7kn8.blockchain_simulation.network.packages.AddBlockPacket;
import com.github.se7kn8.blockchain_simulation.network.packages.GenerateBlockPacket;
import com.github.se7kn8.blockchain_simulation.network.packages.Packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class NetworkHandler {

	private Map<Class<? extends Packet>, Consumer<Packet>> customPacketHandlers = new HashMap<>();
	private Blockchain blockchain;
	private Consumer<Packet> packetSender;

	public NetworkHandler(Blockchain blockchain, Consumer<Packet> packetSender) {
		customPacketHandlers.put(GenerateBlockPacket.class, this::handleGenerateBlockPacket);
		customPacketHandlers.put(AddBlockPacket.class, this::handleAddBlockPacket);
		this.blockchain = blockchain;
		this.packetSender = packetSender;
	}

	public void handlePacket(Packet packet) {

		System.out.println("[NetworkHandler] Handle packet: " + packet.getClass().getSimpleName());
		customPacketHandlers.getOrDefault(packet.getClass(), p -> System.out.println("[NetworkHandler] No handler found for packet: " + p.getClass().getSimpleName())).accept(packet);
	}

	public void handleGenerateBlockPacket(Packet p) {

		if (p instanceof GenerateBlockPacket /*Should always be true*/) {
			GenerateBlockPacket packet = ((GenerateBlockPacket) p);
			new Thread(() -> {
				Block block = new Block(blockchain.getLastHash(), packet.getBlockData());
				System.out.println("[NetworkHandler] Generating block on thread: " + Thread.currentThread().getName());
				try {
					Thread.sleep(new Random().nextInt(8000) + 2000); //Just to save the cpu
				} catch (Exception e) {
					e.printStackTrace();
				}
				block.mineBlock(packet.getDifficulty());
				handleAddBlockPacket(new AddBlockPacket(block));//Simulate: blockchain.addBlock(block, true); equal to local broadcast
				sendPacketSynced(new AddBlockPacket(block));
			}).start();
		}
	}

	public void handleAddBlockPacket(Packet p) {
		if (p instanceof AddBlockPacket /*Should always be true*/) {
			AddBlockPacket packet = ((AddBlockPacket) p);
			new Thread(() -> {
				try {
					for (Block block : blockchain.getBlocks()) {
						if (block.getPrevHash().equals(packet.getBlock().getPrevHash())) {
							System.out.println("[NetworkHandler] Skipped already existing block!");
							return;
						}
					}
					blockchain.addBlock(packet.getBlock(), true);
					System.out.println("Sender: " + p.getSender());
					System.out.println("[NetworkHandler] Successful added block to blockchain!");
				} catch (Blockchain.WrongHashException e) {
					throw new RuntimeException("[NetworkHandler] Can't add block, hash is invalid:", e);
				}
			}).start();
		}
	}


	private synchronized void sendPacketSynced(Packet packet) {
		packetSender.accept(packet);
	}

}
