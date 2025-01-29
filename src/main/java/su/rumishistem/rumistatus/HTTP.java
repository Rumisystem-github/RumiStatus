package su.rumishistem.rumistatus;

import static su.rumishistem.rumistatus.Main.UPDATE_TIME;
import static su.rumishistem.rumistatus.Main.CONFIG_DATA;
import static su.rumishistem.rumistatus.Main.SERVER_LIST;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import su.rumishistem.rumi_java_lib.HTTP_SERVER.HTTP_EVENT;
import su.rumishistem.rumi_java_lib.HTTP_SERVER.HTTP_EVENT_LISTENER;
import su.rumishistem.rumi_java_lib.HTTP_SERVER.HTTP_SERVER;
import su.rumishistem.rumi_java_lib.RESOURCE.RESOURCE_MANAGER;
import su.rumishistem.rumistatus.MODULE.EX_PRINTER;
import su.rumishistem.rumistatus.TYPE.SERVER_DATA;

public class HTTP {
	public void Main() throws IOException {
		HTTP_SERVER HS = new HTTP_SERVER(CONFIG_DATA.get("HTTP").asInt("PORT"));
		HS.SET_EVENT_VOID(new HTTP_EVENT_LISTENER() {
			@Override
			public void REQUEST_EVENT(HTTP_EVENT e) {
				try {
					if (e.getEXCHANGE().getRequestURI().getPath().startsWith("/Asset")) {
						switch (e.getEXCHANGE().getRequestURI().getPath()) {
							case "/Asset/OK.png": {
								e.setHEADER("Content-Type", "image/png");
								e.REPLY_BYTE(200, new RESOURCE_MANAGER().getResourceData("/Asset/OK.png"));
								break;
							}

							case "/Asset/NG.png": {
								e.setHEADER("Content-Type", "image/png");
								e.REPLY_BYTE(200, new RESOURCE_MANAGER().getResourceData("/Asset/NG.png"));
								break;
							}

							default: {
								e.REPLY_String(404, "404");
							}
						}
					} else if (e.getEXCHANGE().getRequestURI().getPath().startsWith("/favicon.ico")) {
						e.REPLY_BYTE(200, new RESOURCE_MANAGER().getResourceData("/Asset/favicon.ico"));
					} else {
						//基盤をロード
						String BASE_HTML = new String(new RESOURCE_MANAGER().getResourceData("/HTML/index.html"));
						int HTTP_STATUS_CODE = 200;

						switch (e.getEXCHANGE().getRequestURI().getPath()) {
							//トップページ
							case "/": {
								String SERVER_ITEM_HTML = new String(new RESOURCE_MANAGER().getResourceData("/HTML/SERVER_ITEM.html"));
								String SERVER_LIST_HTML = "";

								//更新日時をセット
								SERVER_LIST_HTML += "<H1>"+UPDATE_TIME.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分ss秒"))+"時点</H1>";

								//サーバー一覧を生成
								SERVER_LIST_HTML += "<DIV CLASS=\"SERVER_LIST\">";
								for (SERVER_DATA SERVER:SERVER_LIST) {
									SERVER_LIST_HTML += SERVER_ITEM_HTML
										.replace("${NAME}", SERVER.getNAME())
										.replace("${EP}", SERVER.getEP())
										.replace("${STATUS}", SERVER.getSTATUS().name());
								}
								SERVER_LIST_HTML += "</DIV>";

								//コンテンツを基盤にセット
								BASE_HTML = BASE_HTML.replace("${CONTENTS}", SERVER_LIST_HTML);
								break;
							}

							//ページがない
							default: {
								BASE_HTML = BASE_HTML.replace("${CONTENTS}", "ページがない");
								HTTP_STATUS_CODE = 404;
							}
						}

						//応答
						e.setHEADER("Content-Type", "text/html; charset=utf-8");
						e.REPLY_String(HTTP_STATUS_CODE, BASE_HTML);
					}
				} catch (Exception EX) {
					EX_PRINTER.PRINT(EX);
				}
			}
		});
		HS.START_HTTPSERVER();
	}
}
