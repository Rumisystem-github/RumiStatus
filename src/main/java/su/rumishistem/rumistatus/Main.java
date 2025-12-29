package su.rumishistem.rumistatus;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.ArrayNode;
import su.rumishistem.rumi_java_lib.CONFIG;
import su.rumishistem.rumi_java_lib.Ajax.Ajax;
import su.rumishistem.rumi_java_lib.Ajax.AjaxResult;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;
import su.rumishistem.rumi_java_lib.SmartHTTP.HTTP_REQUEST;
import su.rumishistem.rumi_java_lib.SmartHTTP.HTTP_RESULT;
import su.rumishistem.rumi_java_lib.SmartHTTP.SmartHTTP;
import su.rumishistem.rumi_java_lib.SmartHTTP.Type.EndpointFunction;
import su.rumishistem.rumistatus.Type.Protocol;
import su.rumishistem.rumistatus.Type.Server;
import su.rumishistem.rumistatus.Type.ServerStatus;
import su.rumishistem.rumi_java_lib.SmartHTTP.Type.EndpointEntrie.Method;

public class Main {
	public static ArrayNode config;
	public static Server[] server_list;
	public static LocalDateTime last_sync_date = LocalDateTime.now();

	public static void main(String[] args) throws IOException, InterruptedException {
		//設定ファイルを読み込む
		if (new File("Config.ini").exists()) {
			config = new CONFIG().DATA;
			LOG(LOG_TYPE.PROCESS_END_OK, "");
		} else {
			LOG(LOG_TYPE.PROCESS_END_FAILED, "");
			LOG(LOG_TYPE.FAILED, "ERR! Config.ini ga NAI!!!!!!!!!!!!!!");
			System.exit(1);
		}

		//サーバーリストを読む
		if (Files.exists(Path.of("Server.json")) == false) {
			LOG(LOG_TYPE.FAILED, "Server.jsonが必要です。");
			System.exit(1);
		}

		JsonNode server_json = new ObjectMapper().readTree(new File("Server.json"));
		server_list = new Server[server_json.size()];
		for (int i = 0; i < server_json.size(); i++) {
			JsonNode row = server_json.get(i);
			server_list[i] = new Server();
			server_list[i].id = row.get("ID").asText();
			server_list[i].name = row.get("NAME").asText();
			server_list[i].description = row.get("DESCRIPTION").asText();
			server_list[i].endpoint = row.get("EP").asText();
			if (server_list[i].endpoint.startsWith("http://") || server_list[i].endpoint.startsWith("https://")) {
				server_list[i].protocol = Protocol.HTTP;
			} else {
				LOG(LOG_TYPE.FAILED, row.get("ID").asText() + "は非対応のプロトコルです。");
				System.exit(1);
			}
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				sync();

				ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
				long initial_delay = ChronoUnit.SECONDS.between(LocalDateTime.now(), LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(5 - (LocalDateTime.now().getMinute()) % 5));
				long period = TimeUnit.MINUTES.toSeconds(5);

				scheduler.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						sync();
					}
				}, initial_delay, period, TimeUnit.SECONDS);
			}
		}).start();

		SmartHTTP http = new SmartHTTP(config.get("HTTP").getData("PORT").asInt());

		http.SetRoute("/api/Server", Method.GET, new EndpointFunction() {
			@Override
			public HTTP_RESULT Run(HTTP_REQUEST r) throws Exception {
				List<Object> list = new ArrayList<Object>();

				for (Server server:server_list) {
					list.add(new HashMap<String, Object>(){{
						put("ID", server.id);
						put("NAME", server.name);
						put("DESCRIPTION", server.description);
						put("STATUS", server.status.name());
						put("PROTOCOL", server.protocol.name());
						put("ISTORIA", new Object[] {
							server.status.name()
						});
						put("PING", server.ping);
					}});
				}

				return new HTTP_RESULT(200, new ObjectMapper().writeValueAsString(new LinkedHashMap<String, Object>(){{
					put("STATUS", true);
					put("UPDATE_AT", last_sync_date.atOffset(ZoneOffset.ofHours(9)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
					put("LIST", list);
				}}).getBytes(), "application/json");
			}
		});

		http.Start();
	}

	private static void sync() {
		for (int i = 0; i < server_list.length; i++) {
			try {
				//PING値計測開始
				long start = System.nanoTime();

				switch (server_list[i].protocol) {
					case HTTP:
						Ajax ajax = new Ajax(server_list[i].endpoint);
						AjaxResult result = ajax.GET();
						if (result.get_code() == 200) {
							server_list[i].status = ServerStatus.OK;
						} else {
							server_list[i].status = ServerStatus.NG;
						}
						break;
				}

				long end = System.nanoTime();
				long ping = (end - start) / 1_000_000;
				server_list[i].ping = ping;
			} catch (Exception ex) {
				server_list[i].status = ServerStatus.NG;
				server_list[i].ping = -1;
			}
		}

		last_sync_date = LocalDateTime.now();
	}
}
