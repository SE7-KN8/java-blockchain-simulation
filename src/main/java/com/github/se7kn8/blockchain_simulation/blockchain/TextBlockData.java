package com.github.se7kn8.blockchain_simulation.blockchain;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextBlockData implements BlockData {

	private String text;

	public TextBlockData(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public String getHash() {
		return Hashing.sha256().hashString(text, StandardCharsets.UTF_8).toString();
	}

	public static List<TextBlockData> createFromValues(String... text){
		return Arrays.stream(text).map(TextBlockData::new).collect(Collectors.toList());
	}
}
