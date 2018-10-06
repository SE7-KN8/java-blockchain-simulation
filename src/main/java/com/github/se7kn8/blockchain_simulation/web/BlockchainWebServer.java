package com.github.se7kn8.blockchain_simulation.web;

import com.github.se7kn8.blockchain_simulation.blockchain.Block;
import com.github.se7kn8.blockchain_simulation.blockchain.Blockchain;
import io.javalin.Context;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

public class BlockchainWebServer {

	private Javalin app;
	private Blockchain blockchain;

	public BlockchainWebServer() {
		this.app = Javalin.create();
		this.blockchain = new Blockchain(4, "genesis", "block", "data");
		//TODO just for tests
		for(int i = 0; i< 40; i++){
			blockchain.addBlock(new Block(blockchain.getBlocks().get(blockchain.getBlocks().size() - 1).getHash(), List.of()), false);
		}
	}

	public void start() {
		this.app.enableStaticFiles("static-files");
		this.app.start();
		this.app.get("blockchain", this::blockchainEndpoint);
	}

	private String getSite(String title, String contentPath) throws IOException, URISyntaxException {
		var header = new Object() {
			String header;
		};
		var content = new Object() {
			String content;
		};
		var footer = new Object() {
			String footer;
		};

		processResource("templates/header.html", path -> {
			header.header = Files.readString(path);
		});
		processResource(contentPath, path -> {
			content.content = Files.readString(path);
		});
		processResource("templates/footer.html", path -> {
			footer.footer = Files.readString(path);
		});

		return content.content
				.replace("<!-- __footer__ -->", footer.footer)
				.replace("<!-- __header__ -->", header.header)
				.replace("<!-- __title__ -->", title);
	}

	private interface IOConsumer<T> {
		void accept(T t) throws IOException;
	}

	private void processResource(String name, IOConsumer<Path> action) throws IOException, URISyntaxException { //https://stackoverflow.com/questions/15713119/java-nio-file-path-for-a-classpath-resource/36021165#36021165
		URI uri = ClassLoader.getSystemResource(name).toURI();
		try {
			Path p = Paths.get(uri);
			action.accept(p);
		} catch (FileSystemNotFoundException ex) {
			try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
				Path p = fs.provider().getPath(uri);
				action.accept(p);
			} catch (Exception e) {
				throw new IllegalStateException("Error while loading resource: " + name, e);
			}
		}
	}

	private void blockchainEndpoint(Context ctx) throws IOException, URISyntaxException {
		String html = getSite("Blockchain", "sites/blockchain.html");

		int rows = (blockchain.getBlocks().size() % 4) > 0 ? (blockchain.getBlocks().size() / 4) + 1 : (blockchain.getBlocks().size() / 4);
		var rowsHtml = new Object() {
			String rows;
		};

		processResource("templates/block_row.html", path -> rowsHtml.rows = Files.readString(path));

		rowsHtml.rows = rowsHtml.rows.repeat(rows);

		var blocksHtml = new Object() {
			String blocks;
		};
		processResource("templates/block.html", path -> blocksHtml.blocks = Files.readString(path));

		int block = 0;
		for (int r = 0; r < rows; r++) {
			StringBuilder blocksBuilder = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				if (block >= blockchain.getBlocks().size()) {
					break;
				}
				var currentBlock = blockchain.getBlocks().get(block);
				blocksBuilder.append(blocksHtml.blocks
						.replace("<!-- __block_name__ -->", "Block: " + (block + 1))
						.replace("<!-- __block_hash__ -->", currentBlock.getHash().toUpperCase())
						.replace("<!-- __block_previous_hash__ -->", currentBlock.getPrevHash().toUpperCase())
						.replace("<!-- __block_nonce__ -->", String.valueOf(currentBlock.getNonce()))
						.replace("<!-- __block_timestamp__ -->", currentBlock.getTimestamp().toUpperCase())
						.replace("<!-- __block_data_root_hash__ -->", currentBlock.getDataRootHash().toUpperCase()));
				block++;
			}
			rowsHtml.rows = rowsHtml.rows.replaceFirst("<!-- __blocks__ -->", blocksBuilder.toString());
		}

		ctx.html(html.replace("<!-- __rows__ -->", rowsHtml.rows));
	}

}
