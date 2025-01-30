package su.rumishistem.rumistatus;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;
import static su.rumishistem.rumistatus.Main.RSC_PORT;
import static su.rumishistem.rumistatus.Main.VERBOSE;

import java.io.IOException;
import java.net.Socket;

import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;
import su.rumishistem.rumi_java_lib.Socket.Server.SocketServer;
import su.rumishistem.rumi_java_lib.Socket.Server.CONNECT_EVENT.CONNECT_EVENT;
import su.rumishistem.rumi_java_lib.Socket.Server.CONNECT_EVENT.CONNECT_EVENT_LISTENER;
import su.rumishistem.rumi_java_lib.Socket.Server.EVENT.CloseEvent;
import su.rumishistem.rumi_java_lib.Socket.Server.EVENT.EVENT_LISTENER;
import su.rumishistem.rumi_java_lib.Socket.Server.EVENT.MessageEvent;
import su.rumishistem.rumi_java_lib.Socket.Server.EVENT.ReceiveEvent;
import su.rumishistem.rumistatus.MODULE.EX_PRINTER;

public class RSC_SERVER {
	public static void Main() throws IOException {
		SocketServer SS = new SocketServer();
		SS.setEventListener(new CONNECT_EVENT_LISTENER() {
			@Override
			public void CONNECT(CONNECT_EVENT SESSION) {
				if (VERBOSE) {
					LOG(LOG_TYPE.INFO, "New SESSION");
				}
				/*
				try {
					SESSION.sendMessage("200 Welcome\r\n");
				} catch (Exception EX) {
					if (VERBOSE) {
						EX_PRINTER.PRINT(EX);
					}
				}*/

				SESSION.setEventListener(new EVENT_LISTENER() {
					@Override
					public void Receive(ReceiveEvent e) {
					}
					
					@Override
					public void Message(MessageEvent e) {
						try {
							String[] CMD = e.getString().split(" ");
							switch (CMD[0]) {
								case "PING":{
									long START = System.currentTimeMillis();
									boolean STATUS = false;

									switch (CMD[1]) {
										case "MYSQL": {
											STATUS = PING_MYSQL(CMD[2]);
										}
									}

									long END = System.currentTimeMillis();
									long PING = END - START;

									if (STATUS) {
										SESSION.sendMessage("PONG " + PING + "\r\n");
									} else {
										SESSION.sendMessage("FUCK\r\n");
									}

									SESSION.close();
									break;
								}

								default: {
									SESSION.sendMessage("400 Command ga nai\r\n");
									SESSION.close();
								}
							}
						} catch (Exception EX) {
							if (VERBOSE) {
								EX_PRINTER.PRINT(EX);
							}
						}
					}
					
					@Override
					public void Close(CloseEvent e) {
					}
				});
			}
		});
		SS.START(RSC_PORT);
	}

	private static boolean PING_MYSQL(String HOST) {
		//MySQLのTCPポートが合いているかをチェックするだけ
		try {
			Socket SOCKET = new Socket(HOST, 3306);

			return true;
		} catch (Exception EX) {
			return false;
		}
	}
}
