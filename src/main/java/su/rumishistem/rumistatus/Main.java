package su.rumishistem.rumistatus;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.ArrayNode;
import su.rumishistem.rumi_java_lib.CONFIG;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;
import su.rumishistem.rumistatus.MODULE.EX_PRINTER;
import su.rumishistem.rumistatus.TYPE.SERVER_DATA;

public class Main {
	public static final int RSC_PORT = 3322;

	public static LocalDateTime UPDATE_TIME = LocalDateTime.now();
	public static ArrayNode CONFIG_DATA = null;
	public static List<SERVER_DATA> SERVER_LIST = new ArrayList<SERVER_DATA>();
	public static boolean VERBOSE = false;

	public static void main(String[] args) {
		try {
			LOG(LOG_TYPE.INFO, "--------------------<Rumi Status>--------------------");
			LOG(LOG_TYPE.INFO, "うんこ");

			for (String arg:args) {
				if (arg.equals("--verbose")) {
					VERBOSE = true;
				}

				if (arg.equals("--connector")) {
					LOG(LOG_TYPE.INFO, "コネクターモードで起動します");
					LOG(LOG_TYPE.INFO, "ポート番号は既定値の" + RSC_PORT + "です");
					RSC_SERVER.Main();
					return;
				}
			}

			//設定ファイルをロード
			LOG(LOG_TYPE.PROCESS, "Loading Config.ini");
			if (Files.exists(Paths.get("Config.ini")) ) {
				CONFIG_DATA = new CONFIG().DATA;

				LOG(LOG_TYPE.PROCESS_END_OK, "");
			} else {
				LOG(LOG_TYPE.PROCESS_END_FAILED, "");
				LOG(LOG_TYPE.PROCESS_END_FAILED, "Config.ini ga nai!");
				System.exit(1);
			}

			//サーバーリストをロード
			LOG(LOG_TYPE.PROCESS, "Loading Server.json");
			if (Files.exists(Paths.get("Server.json"))) {
				//JSONを解析して型に変換する堕偽
				JsonNode SERVER_JSON = new ObjectMapper().readTree(new File("Server.json"));
				for (int I = 0; I < SERVER_JSON.size(); I++) {
					JsonNode SERVER = SERVER_JSON.get(I);
					SERVER_LIST.add(new SERVER_DATA(
						SERVER.get("ID").asText(),
						SERVER.get("NAME").asText(),
						SERVER.get("DESC") != null ? SERVER.get("DESC").asText() : null,
						SERVER.get("EP") != null ? SERVER.get("EP").asText() : null
					));
				}

				LOG(LOG_TYPE.PROCESS_END_OK, "");
			} else {
				LOG(LOG_TYPE.PROCESS_END_FAILED, "");
				LOG(LOG_TYPE.PROCESS_END_FAILED, "Server.json ga nai!");
				System.exit(1);
			}

			//HTTPサーバー起動
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						new HTTP().Main();
					} catch (Exception EX) {
						EX_PRINTER.PRINT(EX);
					}
				}
			}).start();

			//実際にステータスを取得するやつ
			SERVER_CHECK.Main();
		} catch (Exception EX) {
			EX_PRINTER.PRINT(EX);
		}
	}
}
