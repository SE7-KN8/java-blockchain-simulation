package com.github.se7kn8.blockchain_simulation.network;

import com.github.se7kn8.blockchain_simulation.command.CommandHandler;
import com.github.se7kn8.blockchain_simulation.command.CommandSender;
import com.github.se7kn8.blockchain_simulation.network.packages.ConnectPacket;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {

	private boolean isConnected;

	public void registerCommands() {
		CommandHandler.getInstance().registerCommand(LiteralArgumentBuilder.<CommandSender>literal("connect")
				.executes(c -> {
					c.getSource().message("connect <hostname/ip> [port]");
					return 1;
				})
				.requires(s -> !isConnected)
				.then(RequiredArgumentBuilder.<CommandSender, String>argument("hostname", StringArgumentType.string())
						.executes(c -> {
							//With default port
							System.out.println("Connect to " + StringArgumentType.getString(c, "hostname") + " with default port");
							try {
								connectToServer(StringArgumentType.getString(c, "hostname"), 7823);
							} catch (Exception e) {
								c.getSource().message("Can't connect to server: " + e.getMessage());
								e.printStackTrace();
							}
							return 1;
						})
						.then(RequiredArgumentBuilder.<CommandSender, Integer>argument("port", IntegerArgumentType.integer(0, 65535))
								.executes(c -> {
									//With port as parameter
									System.out.println("Connect to " + StringArgumentType.getString(c, "hostname") + " with port " + IntegerArgumentType.getInteger(c, "port"));
									try {
										connectToServer(StringArgumentType.getString(c, "hostname"), IntegerArgumentType.getInteger(c, "port"));
									} catch (Exception e) {
										c.getSource().message("Can't connect to server: " + e.getMessage());
										e.printStackTrace();
									}
									return 1;
								}))));

	}

	private void connectToServer(String hostname, int port) throws Exception {
		Socket socket = new Socket(hostname, port);
		System.out.println("Client connect!");
		isConnected = true;
		ObjectOutputStream stream = new ObjectOutputStream((socket.getOutputStream()));
		ObjectInputStream input = new ObjectInputStream((socket.getInputStream()));
		stream.writeObject(new ConnectPacket(ConnectPacket.TRY_TO_CONNECT));
		stream.flush();
		System.out.println("Object was written");

		while (true) {
			Object packet = input.readObject();
			System.out.println("Received packet: " + packet);

			if (false) {
				break;
			}
		}


		isConnected = false;
		System.out.println("Client");
		socket.close();
	}

}
