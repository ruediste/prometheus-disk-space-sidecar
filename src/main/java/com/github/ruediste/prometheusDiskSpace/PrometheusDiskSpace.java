package com.github.ruediste.prometheusDiskSpace;

import java.io.File;
import java.time.Duration;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.exporter.PushGateway;

public class PrometheusDiskSpace {
	static Gauge totalSpace = Gauge.build("fs_total_space", "Total space of the file system in bytes")
			.labelNames("name").register();

	static Gauge freeSpace = Gauge.build("fs_free_space", "Total space of the file system in bytes").labelNames("name")
			.register();

	public static void main(String[] args) throws Exception {
		File dataDir = new File("/data");
		boolean anyRegistered = false;
		if (dataDir.exists())
			for (File root : dataDir.listFiles()) {
				if (!root.isDirectory())
					continue;
				register(root, root.getName());
				anyRegistered = true;
			}

		File hostnameDir = new File("/hostname");
		if (hostnameDir.exists())
			for (File root : hostnameDir.listFiles()) {
				if (!root.isDirectory())
					continue;
				register(root, System.getenv("HOSTNAME") + "-" + root.getName());
				anyRegistered = true;
			}

		if (!anyRegistered) {
			System.err.println("No file systems found to monitor. Mount them under /data or /hostname");
			System.exit(1);
		}

		String serverPort = System.getenv("SERVER_PORT");
		if (serverPort != null) {
			int port = Integer.parseInt(serverPort);
			System.out.println("Listening on port " + port);
			new HTTPServer(port);
		} else {
			String pushGateway = System.getenv("PUSH_GATEWAY");
			String jobName = System.getenv("JOB_NAME");
			if (pushGateway == null) {
				System.err.println("You must either specify SERVER_PORT or PUSH_GATEWAY");
				System.exit(1);
			}
			if (jobName == null) {
				System.err.println("You must either specify JOB_NAME");
				System.exit(1);
			}
			PushGateway pg = new PushGateway(pushGateway);
			while (true) {
				pg.push(CollectorRegistry.defaultRegistry, jobName);
				Thread.sleep(Duration.ofSeconds(10).toMillis());
			}
		}
	}

	private static void register(File root, String name) {
		System.out.println("Monitoring " + root.getAbsolutePath() + " as " + name);
		totalSpace.setChild(new Child() {
			@Override
			public double get() {
				return root.getTotalSpace();
			}
		}, name);
		freeSpace.setChild(new Child() {
			@Override
			public double get() {
				return root.getUsableSpace();
			}
		}, name);
	}
}
