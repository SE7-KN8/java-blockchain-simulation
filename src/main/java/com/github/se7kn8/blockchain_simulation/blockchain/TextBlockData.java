package com.github.se7kn8.blockchain_simulation.blockchain;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TextBlockData implements BlockData {

	private static final long serialVersionUID = 1;
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

	public static List<TextBlockData> createFromValues(String... text) {
		return Arrays.stream(text).map(TextBlockData::new).collect(Collectors.toList());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TextBlockData that = (TextBlockData) o;
		return Objects.equals(text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}
}
