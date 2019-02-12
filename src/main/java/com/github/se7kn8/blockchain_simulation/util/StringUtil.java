package com.github.se7kn8.blockchain_simulation.util;

public class StringUtil {

	public static String generateDifficultyString(int difficulty) {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < difficulty; i++) {
			stringBuilder.append(0);
		}

		return stringBuilder.toString();
	}

}
