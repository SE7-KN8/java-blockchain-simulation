package com.github.se7kn8.blockchain_simulation;

import com.github.se7kn8.blockchain_simulation.command.CommandHandler;
import com.github.se7kn8.blockchain_simulation.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

	private static class Data {
		private boolean data;
	}

	private class TestCommandSender implements CommandSender {

		private String lastMessage = "";

		@Override
		public void message(String message) {
			System.out.println("TestCommandSender: " + message);
			lastMessage = message;
		}
	}

	@Test
	void testStopHandler() throws Exception {
		Data test1 = new Data();
		Data test2 = new Data();
		Data test3 = new Data();
		Data test4 = new Data();



		CommandHandler.getInstance().addStopHandler("web", s -> test1.data = true);
		CommandHandler.getInstance().addStopHandler("data", s -> test2.data = true);
		CommandHandler.getInstance().addStopHandler("gui", s -> test3.data = true);
		CommandHandler.getInstance().addStopHandler("files", s -> test4.data = true);

		assertThrows(IllegalArgumentException.class, () -> {
			CommandHandler.getInstance().addStopHandler("web", null);
		});

		TestCommandSender sender = new TestCommandSender();

		CommandHandler.getInstance().parseCommand("stop web", sender, true);
		assertTrue(test1.data);
		assertFalse(test2.data);
		assertFalse(test3.data);
		assertFalse(test4.data);


		CommandHandler.getInstance().parseCommand("stop gui", sender, true);
		assertTrue(test1.data);
		assertFalse(test2.data);
		assertTrue(test3.data);
		assertFalse(test4.data);

		CommandHandler.getInstance().parseCommand("stop all", sender, true);
		assertTrue(test1.data);
		assertTrue(test2.data);
		assertTrue(test3.data);
		assertTrue(test4.data);

		CommandHandler.getInstance().parseCommand("stop", sender, true);
		assertTrue(sender.lastMessage.contains("web"));
		assertTrue(sender.lastMessage.contains("data"));
		assertTrue(sender.lastMessage.contains("gui"));
		assertTrue(sender.lastMessage.contains("files"));
		assertTrue(sender.lastMessage.contains("all"));

	}

}
