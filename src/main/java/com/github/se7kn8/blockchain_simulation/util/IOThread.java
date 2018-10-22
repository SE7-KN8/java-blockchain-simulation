package com.github.se7kn8.blockchain_simulation.util;

public abstract class IOThread extends Thread {

	@Override
	public final void run() {
		try {
			runWithExceptions();
		} catch (Exception e) {
			handleException(e);
		}

	}

	public abstract void runWithExceptions() throws Exception;

	public abstract void handleException(Exception e);

}
