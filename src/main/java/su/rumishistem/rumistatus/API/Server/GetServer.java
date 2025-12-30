package su.rumishistem.rumistatus.API.Server;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.SmartHTTP.HTTP_REQUEST;
import su.rumishistem.rumi_java_lib.SmartHTTP.HTTP_RESULT;
import su.rumishistem.rumi_java_lib.SmartHTTP.Type.EndpointFunction;
import su.rumishistem.rumistatus.Main;
import su.rumishistem.rumistatus.Type.Server;

public class GetServer implements EndpointFunction{
	@Override
	public HTTP_RESULT Run(HTTP_REQUEST r) throws Exception {
		if (r.GetEVENT().getURI_PARAM().get("ID") == null) {
			return get_list();
		} else {
			//TODO:やれよ
			return null;
		}
	}

	private HTTP_RESULT get_list() throws JsonProcessingException {
		List<Object> list = new ArrayList<Object>();

		for (Server server:Main.server_list) {
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
			put("UPDATE_AT", Main.last_sync_date.atOffset(ZoneOffset.ofHours(9)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
			put("LIST", list);
		}}).getBytes(), "application/json");
	}
}
