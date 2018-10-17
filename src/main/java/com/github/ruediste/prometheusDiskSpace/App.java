package com.github.ruediste.prometheusDiskSpace;

import java.io.File;
import java.time.Duration;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.exporter.PushGateway;

public class App {
	static Gauge totalSpace = Gauge.build("fs_total_space", "Total space of the file system in bytes")
			.labelNames("name").register();

	static Gauge freeSpace = Gauge.build("fs_free_space", "Total space of the file system in bytes").labelNames("name")
			.register();

	public static void main(String[] args) throws Exception {
		File dataDir = new File("/data");
		if (!dataDir.exists()) {
			System.err.println("Directory /data does not exist. Mount file systems to monitor there.");
			System.exit(1);
		}
		for (File root : dataDir.listFiles()) {
			if (!root.isDirectory())
				continue;

			System.out.println("Monitoring " + root.getAbsolutePath() + " as " + root.getName());
			totalSpace.setChild(new Child() {
				@Override
				public double get() {
					return root.getTotalSpace();
				}
			}, root.getName());
			freeSpace.setChild(new Child() {
				@Override
				public double get() {
					return root.getUsableSpace();
				}
			}, root.getName());
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
}
