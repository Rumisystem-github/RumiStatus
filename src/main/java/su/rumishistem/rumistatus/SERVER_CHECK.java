package su.rumishistem.rumistatus;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import static su.rumishistem.rumistatus.Main.UPDATE_TIME;
import static su.rumishistem.rumistatus.Main.CONFIG_DATA;
import static su.rumishistem.rumistatus.Main.SERVER_LIST;
import static su.rumishistem.rumistatus.Main.VERBOSE;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import su.rumishistem.rumi_java_lib.FETCH;
import su.rumishistem.rumi_java_lib.FETCH_RESULT;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;
import su.rumishistem.rumistatus.TYPE.SERVER_DATA;
import su.rumishistem.rumistatus.TYPE.SERVER_STATUS;

public class SERVER_CHECK {
	public static void Main() {
		ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
		Runnable TASK = new Runnable() {
			@Override
			public void run() {
				if (VERBOSE) {
					LOG(LOG_TYPE.PROCESS, "Check...");
				}

				for (SERVER_DATA SERVER:SERVER_LIST) {
					try {
						//ヘッダーならスキップ
						if (SERVER.getID().equals("-_HR_-")) {
							continue;
						}

						switch (SERVER.getPROTOCOL()) {
							case HTTP: {
								FETCH AJAX = new FETCH(SERVER.getEP());
								FETCH_RESULT RESULT = AJAX.GET();
								if (RESULT.GetSTATUS_CODE() == 200) {
									SERVER.setSTATUS(SERVER_STATUS.OK);
								} else {
									SERVER.setSTATUS(SERVER_STATUS.NG);
								}
								break;
							}

							default: {
								if (VERBOSE) {
									LOG(LOG_TYPE.OK, "Skip:" + SERVER.getID());
									LOG(LOG_TYPE.INFO, "Reason:Protcol ga wakaran");
								}
								continue;
							}
						}

						//成功
						if (VERBOSE) {
							LOG(LOG_TYPE.OK, "Get:" + SERVER.getID());
						}
					} catch (Exception EX) {
						//エラーが発生したらステータスをNGにする
						SERVER.setSTATUS(SERVER_STATUS.NG);

						//失敗
						if (VERBOSE) {
							LOG(LOG_TYPE.FAILED, "Get:" + SERVER.getID());
						}
					}
				}

				//最終更新日時を入れる
				UPDATE_TIME = LocalDateTime.now();

				//完了
				if (VERBOSE) {
					LOG(LOG_TYPE.OK, "All server checked!");
				}
			}
		};

		SCHEDULER.scheduleAtFixedRate(TASK, 0, CONFIG_DATA.get("STATUS").asInt("INTERVAL"), TimeUnit.SECONDS);
	}
}
