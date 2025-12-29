package su.rumishistem.rumistatus;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import java.io.File;
import java.io.IOException;

import su.rumishistem.rumi_java_lib.ArrayNode;
import su.rumishistem.rumi_java_lib.CONFIG;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;

public class Main {
	public static ArrayNode config;

	public static void main(String[] args) throws IOException {
		//設定ファイルを読み込む
		if (new File("Config.ini").exists()) {
			config = new CONFIG().DATA;
			LOG(LOG_TYPE.PROCESS_END_OK, "");
		} else {
			LOG(LOG_TYPE.PROCESS_END_FAILED, "");
			LOG(LOG_TYPE.FAILED, "ERR! Config.ini ga NAI!!!!!!!!!!!!!!");
			System.exit(1);
			return;
		}

		
	}
}
