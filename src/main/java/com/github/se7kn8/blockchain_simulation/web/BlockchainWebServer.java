package com.github.se7kn8.blockchain_simulation.web;

import com.github.se7kn8.blockchain_simulation.blockchain.Block;
import com.github.se7kn8.blockchain_simulation.blockchain.Blockchain;
import com.github.se7kn8.blockchain_simulation.command.CommandHandler;
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
		for (int i = 0; i < 40; i++) {
			blockchain.addBlock(new Block(blockchain.getBlocks().get(blockchain.getBlocks().size() - 1).getHash(), List.of()), false);
		}
		CommandHandler.getInstance().addStopHandler("webserver", c -> {
			c.message("Stopping webserver");
			this.app.stop();
		});
	}

	public void start() {
		this.app.enableStaticFiles("static-files");
		this.app.start();
		this.app.get("blockchain", this::blockchainEndpoint);
		this.app.get("block-info/:block_id", this::blockInfoEndpoint);
		this.app.error(404, this::handle404);
		this.app.error(500, this::handle500);
	}

	private String getSite(String title, String contentPath) throws IOException, URISyntaxException {
		var head = new Object() {
			String head;
		};
		var header = new Object() {
			String header;
		};
		var content = new Object() {
			String content;
		};
		var footer = new Object() {
			String footer;
		};

		processResource("templates/head.html", path -> head.head = Files.readString(path));
		processResource("templates/header.html", path -> header.header = Files.readString(path));
		processResource("templates/footer.html", path -> footer.footer = Files.readString(path));
		processResource(contentPath, path -> content.content = Files.readString(path));

		return content.content
				.replace("<!-- __footer__ -->", footer.footer)
				.replace("<!-- __header__ -->", header.header)
				.replace("<!-- __head__ -->", head.head)
				.replace("<!-- __title__ -->", title);
	}

	private String getErrorSite(String title, String errorTitle, String errorDesc) throws IOException, URISyntaxException {
		String site = getSite(title, "sites/error.html");


		return site
				.replace("<!-- __error_title__ -->", errorTitle)
				.replace("<!-- __error_desc__ -->", errorDesc);
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
				blocksBuilder.append(replaceBlockInfo(blocksHtml.blocks, block));

				block++;
			}
			rowsHtml.rows = rowsHtml.rows.replaceFirst("<!-- __blocks__ -->", blocksBuilder.toString());
		}

		ctx.html(html.replace("<!-- __rows__ -->", rowsHtml.rows));
	}

	private void blockInfoEndpoint(Context ctx) throws IOException, URISyntaxException {
		int block = Integer.valueOf(ctx.pathParam("block_id"));
		String blockName = block == 0 ? "Genesis block" : "Block " + block;

		String site = getSite(blockName, "sites/block-info.html")
				.replace("<!-- __previous_block_link__ -->", "/block-info/" + (block - 1))
				.replace("<!-- __next_block_link__ -->", "/block-info/" + (block + 1));

		ctx.html(replaceBlockInfo(site, block));
	}

	private String replaceBlockInfo(String html, int blockID) {
		if (blockID >= this.blockchain.getBlocks().size() || blockID < 0) {
			return html.replace("<!-- __block_name__ -->", "The requested block doesn't exists.")
					.replace("<!-- __block_hash__ -->", "---")
					.replace("<!-- __block_previous_hash__ -->", "---")
					.replace("<!-- __block_nonce__ -->", "---")
					.replace("<!-- __block_timestamp__ -->", "---")
					.replace("<!-- __block_data_root_hash__ -->", "---")
					.replace("<!-- __block_id__ -->", String.valueOf(blockID));
		}
		Block block = this.blockchain.getBlocks().get(blockID);
		return html.replace("<!-- __block_name__ -->", blockID == 0 ? "Genesis block" : "Block " + blockID)
				.replace("<!-- __block_hash__ -->", block.getHash().toUpperCase())
				.replace("<!-- __block_previous_hash__ -->", block.getPrevHash().toUpperCase())
				.replace("<!-- __block_nonce__ -->", String.valueOf(block.getNonce()))
				.replace("<!-- __block_timestamp__ -->", block.getTimestamp().toUpperCase())
				.replace("<!-- __block_data_root_hash__ -->", block.getDataRootHash().toUpperCase())
				.replace("<!-- __block_id__ -->", String.valueOf(blockID));
	}

	private void handle404(Context ctx) {
		try {
			ctx.html(getErrorSite("404 Error", "HTTP Error 404", "The requested page doesn't exists"));
		} catch (Exception e) {
			throw new IllegalStateException("Error while creating 404 page", e);
		}
	}

	private void handle500(Context ctx) {
		try {
			ctx.html(getErrorSite("500 Error", "HTTP Error 500", "There was an error while loading the requested site"));
		} catch (Exception e) {
			throw new IllegalStateException("Error while creating 500 page", e);
		}
	}

}
