package com.github.se7kn8.blockchain_simulation.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CommandHandler {

	private static final CommandHandler handler = new CommandHandler();
	private Map<String, Consumer<CommandSender>> registeredStopHandlers = new HashMap<>();

	public static CommandHandler getInstance() {
		return handler;
	}

	private CommandDispatcher<CommandSender> dispatcher;
	private Executor commandExecutor = Executors.newCachedThreadPool();

	public CommandHandler() {
		dispatcher = new CommandDispatcher<>();
		dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("info").executes(c -> {
			c.getSource().message("Project name: java-blockchain-simulation");
			return 1;
		}));
		dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("stop").executes(c -> {
			c.getSource().message("stop <module>");
			c.getSource().message("Available modules are: " + registeredStopHandlers.keySet().toString());
			return 1;
		}));
		dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("stop").then(LiteralArgumentBuilder.<CommandSender>literal("all").executes(c -> {
			c.getSource().message("Stopping all modules!");
			registeredStopHandlers.values().forEach(v -> {
				if (v != null) {
					v.accept(c.getSource());
				}
			});
			return 1;
		})));
		dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("exit").executes(c -> {
			c.getSource().message("Stopping the program");
			return 1;
		}));
		registeredStopHandlers.put("all", null);
	}

	public void registerCommand(LiteralArgumentBuilder<CommandSender> builder) {
		dispatcher.register(builder);
	}

	public void addStopHandler(String name, Consumer<CommandSender> stopHandler) {
		if (!registeredStopHandlers.keySet().contains(name)) {
			dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("stop").then(LiteralArgumentBuilder.<CommandSender>literal(name).executes(c -> {
				if (stopHandler != null) {
					stopHandler.accept(c.getSource());
				}

				return 1;
			})));
			registeredStopHandlers.put(name, stopHandler);
		} else {
			throw new IllegalArgumentException("Stop-handler '" + name + "' has already been registered!");
		}
	}

	public void parseCommand(String command, CommandSender sender) {
		parseCommand(command, sender, false);
	}

	public void parseCommand(String command, CommandSender sender, boolean executeOnCurrentThread) {

		Runnable commandRunnable = () -> {
			try {
				dispatcher.execute(command, sender);
			} catch (CommandSyntaxException e) {
				sender.message("Error while executing command: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		if (executeOnCurrentThread) {
			commandRunnable.run();
		} else {
			commandExecutor.execute(commandRunnable);
		}
	}
}
