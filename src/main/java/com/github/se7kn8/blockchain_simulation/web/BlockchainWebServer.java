package com.github.se7kn8.blockchain_simulation.web;

import com.github.se7kn8.blockchain_simulation.blockchain.Block;
import com.github.se7kn8.blockchain_simulation.blockchain.Blockchain;
import com.github.se7kn8.blockchain_simulation.command.CommandHandler;
import io.javalin.Context;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Collections;

public class BlockchainWebServer {

	private Javalin app;
	private Blockchain blockchain;

	private static class Data {
		public String data;
	}

	public BlockchainWebServer(int wsPort, Blockchain blockchain) {
		this.app = Javalin.create();
		this.app.port(wsPort);
		this.blockchain = blockchain;
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
		Data head = new Data();
		Data header = new Data();
		Data content = new Data();
		Data footer = new Data();

		processResource("templates/head.html", path -> head.data = readFile(path));
		processResource("templates/header.html", path -> header.data = readFile(path));
		processResource("templates/footer.html", path -> footer.data = readFile(path));
		processResource(contentPath, path -> content.data = readFile(path));

		return content.data
				.replace("<!-- __footer__ -->", footer.data)
				.replace("<!-- __header__ -->", header.data)
				.replace("<!-- __head__ -->", head.data)
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
		Data rowsHtml = new Data();

		processResource("templates/block_row.html", path -> rowsHtml.data = readFile(path));

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			stringBuilder.append(rowsHtml.data);
		}
		rowsHtml.data = stringBuilder.toString();

		Data blocksHtml = new Data();
		processResource("templates/block.html", path -> blocksHtml.data = readFile(path));

		int block = 0;
		for (int r = 0; r < rows; r++) {
			StringBuilder blocksBuilder = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				if (block >= blockchain.getBlocks().size()) {
					break;
				}
				blocksBuilder.append(replaceBlockInfo(blocksHtml.data, block));

				block++;
			}
			rowsHtml.data = rowsHtml.data.replaceFirst("<!-- __blocks__ -->", blocksBuilder.toString());
		}

		ctx.html(html.replace("<!-- __rows__ -->", rowsHtml.data));
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
				.replace("<!-- __block_id__ -->", String.valueOf(blockID))
				.replace("<!-- __block_data__ -->", block.getBlockData().toString());
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

	private static String readFile(Path path) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, Charset.forName("UTF-8"));
	}

}
