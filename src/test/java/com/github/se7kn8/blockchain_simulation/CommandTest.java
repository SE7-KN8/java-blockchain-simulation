package com.github.se7kn8.blockchain_simulation;

import com.github.se7kn8.blockchain_simulation.command.CommandHandler;
import com.github.se7kn8.blockchain_simulation.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

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

		var test1 = new Object() {
			boolean stopped = false;
		};

		var test2 = new Object() {
			boolean stopped = false;
		};

		var test3 = new Object() {
			boolean stopped = false;
		};

		var test4 = new Object() {
			boolean stopped = false;
		};


		CommandHandler.getInstance().addStopHandler("web", s -> test1.stopped = true);
		CommandHandler.getInstance().addStopHandler("data", s -> test2.stopped = true);
		CommandHandler.getInstance().addStopHandler("gui", s -> test3.stopped = true);
		CommandHandler.getInstance().addStopHandler("files", s -> test4.stopped = true);

		assertThrows(IllegalArgumentException.class, () -> {
			CommandHandler.getInstance().addStopHandler("web", null);
		});

		TestCommandSender sender = new TestCommandSender();

		CommandHandler.getInstance().parseCommand("stop web", sender, true);
		assertTrue(test1.stopped);
		assertFalse(test2.stopped);
		assertFalse(test3.stopped);
		assertFalse(test4.stopped);


		CommandHandler.getInstance().parseCommand("stop gui", sender, true);
		assertTrue(test1.stopped);
		assertFalse(test2.stopped);
		assertTrue(test3.stopped);
		assertFalse(test4.stopped);

		CommandHandler.getInstance().parseCommand("stop all", sender, true);
		assertTrue(test1.stopped);
		assertTrue(test2.stopped);
		assertTrue(test3.stopped);
		assertTrue(test4.stopped);

		CommandHandler.getInstance().parseCommand("stop", sender, true);
		assertTrue(sender.lastMessage.contains("web"));
		assertTrue(sender.lastMessage.contains("data"));
		assertTrue(sender.lastMessage.contains("gui"));
		assertTrue(sender.lastMessage.contains("files"));
		assertTrue(sender.lastMessage.contains("all"));

	}

}
