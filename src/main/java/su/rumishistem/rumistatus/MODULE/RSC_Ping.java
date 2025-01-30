package su.rumishistem.rumistatus.MODULE;

import static su.rumishistem.rumistatus.Main.RSC_PORT;
import static su.rumishistem.rumistatus.Main.VERBOSE;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;
import su.rumishistem.rumistatus.TYPE.PROTOCOL;
import su.rumishistem.rumistatus.TYPE.SERVER_STATUS;

public class RSC_Ping {
	private String RSC_HOST;
	private PROTOCOL CONNECT_PRT;
	private String CONNECT_HOST;
	private SERVER_STATUS STATUS = SERVER_STATUS.WHAT;
	private int PING = 0;

	public RSC_Ping(String EP) {
		//接頭辞を破壊
		EP = EP.replaceFirst("rsc\\:\\/\\/", "");
		String[] EPS = EP.split("\\/");

		//RSC;
		if (EPS[0] != null) {
			RSC_HOST = EPS[0];
		}

		//プロトコル
		if (EPS[1] != null) {
			switch (EPS[1]) {
				case "https":
				case "http": {
					CONNECT_PRT = PROTOCOL.HTTP;
					break;
				}

				case "mysql": {
					CONNECT_PRT = PROTOCOL.MYSQL;
					break;
				}

				default: {
					throw new Error("Protocol ga aho");
				}
			}
		}

		//ホスト部分を割り出す(HTTPだと/index.htmlみたいなのも混ざるが問題ない)
		CONNECT_HOST = EP.replaceFirst(EPS[0] + "/", "").replaceFirst(EPS[1] + "/", "");

		//ログ
		if (VERBOSE) {
			LOG(LOG_TYPE.INFO, "RSC Host        :" + RSC_HOST);
			LOG(LOG_TYPE.INFO, "Connect Protocol:" + CONNECT_PRT.name());
			LOG(LOG_TYPE.INFO, "Connect         :" + CONNECT_HOST);
		}
	}

	public SERVER_STATUS getSTATUS() {
		return STATUS;
	}

	public int getPING() {
		return PING;
	}

	public void Ping() {
		try {
			Socket SOCKET = new Socket(RSC_HOST, RSC_PORT);
			BufferedReader BR = new BufferedReader(new InputStreamReader(SOCKET.getInputStream()));
			PrintWriter BW = new PrintWriter(SOCKET.getOutputStream(), true);

			//コマンド送信
			BW.write("PING " + CONNECT_PRT.name() + " " + CONNECT_HOST + "\r\n");
			BW.flush();

			String MSG;
			while ((MSG = BR.readLine()) != null) {
				String[] RES = MSG.split(" ");
				//PONGならヨシ
				if (RES[0].equals("PONG")) {
					STATUS = SERVER_STATUS.OK;
					PING = Integer.parseInt(RES[1]);
				} else {
					STATUS = SERVER_STATUS.NG;
				}
			}

			//後始末
			BR.close();
			BW.close();
			SOCKET.close();
		} catch (Exception EX) {
			if (VERBOSE) {
				EX_PRINTER.PRINT(EX);
			}
		}
	}
}
