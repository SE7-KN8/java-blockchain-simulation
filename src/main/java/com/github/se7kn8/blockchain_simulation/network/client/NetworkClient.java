package com.github.se7kn8.blockchain_simulation.network.client;

import com.github.se7kn8.blockchain_simulation.command.CommandHandler;
import com.github.se7kn8.blockchain_simulation.command.CommandSender;
import com.github.se7kn8.blockchain_simulation.network.packages.TestPacket;
import com.github.se7kn8.blockchain_simulation.network.server.NetworkServer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import java.net.InetSocketAddress;

public class NetworkClient {

	private NetworkServer localServer;
	private NetworkClientSocketWrapper wrapper;
	private boolean connected;

	public NetworkClient(NetworkServer localServer) {
		this.localServer = localServer;
	}

	public void registerCommands() {
		CommandHandler.getInstance().registerCommand(LiteralArgumentBuilder.<CommandSender>literal("connect")
				.executes(c -> {
					c.getSource().message("connect <hostname/ip> [port]");
					return 1;
				})
				.then(RequiredArgumentBuilder.<CommandSender, String>argument("hostname", StringArgumentType.string())
						.executes(c -> {
							//With default port
							c.getSource().message("Connect to " + StringArgumentType.getString(c, "hostname") + " with default port");
							try {
								connect(StringArgumentType.getString(c, "hostname"), 7823, c.getSource());
							} catch (Exception e) {
								c.getSource().message("Can't connect to server: " + e.getMessage());
								e.printStackTrace();
							}
							return 1;
						})
						.then(RequiredArgumentBuilder.<CommandSender, Integer>argument("port", IntegerArgumentType.integer(0, 65535))
								.executes(c -> {
									//With port as parameter
									c.getSource().message("Connect to " + StringArgumentType.getString(c, "hostname") + " with port " + IntegerArgumentType.getInteger(c, "port"));
									try {
										connect(StringArgumentType.getString(c, "hostname"), IntegerArgumentType.getInteger(c, "port"), c.getSource());
									} catch (Exception e) {
										c.getSource().message("Can't connect to server: " + e.getMessage());
										e.printStackTrace();
									}
									return 1;
								}))));
		CommandHandler.getInstance().registerCommand(LiteralArgumentBuilder.<CommandSender>literal("disconnect")
				.executes(c -> {
					disconnected(c.getSource());
					return 1;
				}));
		CommandHandler.getInstance().registerCommand(LiteralArgumentBuilder.<CommandSender>literal("send-test-packet-client").executes(c -> {
			if (connected) {
				wrapper.sendPacket(new TestPacket());
			} else {
				c.getSource().message("Not connect!");
			}
			return 1;
		}));

	}

	private synchronized void connect(String hostname, int port, CommandSender sender) throws Exception {
		if (!connected) {
			if (wrapper == null) {
				connected = true;
				wrapper = new NetworkClientSocketWrapper(hostname, port, () -> connected = false);
				wrapper.setLocalServer(localServer);
				localServer.setLocalClient(wrapper);
				System.out.println("[Client] Connected");
			} else {
				throw new IllegalStateException("Connected is 'false' but the wrapper is not null. Try to restart the program");
			}
		} else {
			sender.message("Already connected. Disconnect via 'disconnect'");
		}
	}

	private synchronized void disconnected(CommandSender sender) {
		if (!connected) {
			sender.message("Not connected! Connect to a node via 'connect'");
		} else {
			wrapper.close();
			wrapper = null;
			localServer.setLocalClient(null);
		}
	}

	public NetworkClientSocketWrapper getWrapper() {
		return wrapper;
	}
}
