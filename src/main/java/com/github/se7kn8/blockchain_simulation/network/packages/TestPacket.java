package com.github.se7kn8.blockchain_simulation.network.packages;

public class TestPacket implements Packet {

	private static final long serialVersionUID = 1;
	private final double testData = Math.random();

	public double getTestData() {
		return testData;
	}

	@Override
	public String toString() {
		return "Packet[name=TEST_PACKET test_data=" + getTestData() + "]";
	}
}
