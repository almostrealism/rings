package com.almostrealism.photon.network;

import com.almostrealism.network.RayTracingOutputHandler;
import io.almostrealism.db.DatabaseConnection;
import io.almostrealism.db.Query;
import io.flowtree.fs.OutputServer;
import io.flowtree.msg.Message;
import io.flowtree.node.Client;
import org.almostrealism.io.JobOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class ExtractImageClient {
	public static void main(String args[]) throws IOException, InterruptedException {
		if (args.length < 2) {
			throw new IllegalArgumentException("ExtractImageClient: Please supply configuration file and target task ID");
		}

		Properties p = new Properties();

		try {
			p.load(new FileInputStream(args[0]));
		} catch (FileNotFoundException fnf) {
			System.out.println("ExtractImageClient: Config file not found: " + args[0]);
			System.exit(2);
		} catch (IOException ioe) {
			System.out.println("ExtractImageClient: IO error loading config file: " + args[0]);
			System.exit(3);
		}

		if (Client.getCurrentClient() == null) {
			System.out.println("ExtractImageClient: Starting network client...");

			String user = p.getProperty("render.host.user", "");
			String passwd = p.getProperty("render.host.passwd", "");

			try {
				Client.setCurrentClient(new Client(p, user, passwd, null));
			} catch (IOException ioe) {
				System.out.println("ExtractImageClient: IO error starting network client: " + ioe.getMessage());
			}
		}

		List<String> imageIds = new ArrayList<>();

		File imageListFile = new File(args[1]);

		if (imageListFile.exists()) {
			try (FileInputStream in = new FileInputStream(imageListFile)) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));

				String line = null;

				while ((line = reader.readLine()) != null) {
					imageIds.add(line);
				}
			}
		} else {
			imageIds.add(args[1]);
		}

		try {
			imageIds.forEach(id -> {
				try {
					Query q = new Query(OutputServer.getOutputTable(), DatabaseConnection.tidColumn + " = '" + id + "'");
					Message m = OutputServer.getCurrentServer().getNodeServer().executeQuery(q);
					Hashtable<String, String> h = Query.fromString(m.getData());

					System.out.println("ExtractImageClient: There are " + h.size() + " records from query for task " + id);

					System.out.println("ExtractImageClient: Decoding JobOutput...");
					Hashtable<Long, JobOutput> decoded = new Hashtable<>();
					// h.forEach((toa, data) -> System.out.println(data));

					h.forEach((toa, data) -> decoded.put(Long.parseLong(toa), JobOutput.decode(data)));
					System.out.println("ExtractImageClient: Decoded " + decoded.size() + " JobOutputs");

					System.out.println("ExtractImageClient: Creating output handler and processing records...");
					RayTracingOutputHandler handler = new RayTracingOutputHandler(id);
					decoded.forEach((toa, data) -> handler.storeOutput(toa, -1, data));
					System.out.println("ExtractImageClient: Done processing records");

					System.out.println("ExtractImageClient: Writing image...");
					handler.writeImage();
					System.out.println("ExtractImageClient: Done writing image");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} finally {
			Thread.sleep(1000);
			System.exit(0);
		}
	}
}
