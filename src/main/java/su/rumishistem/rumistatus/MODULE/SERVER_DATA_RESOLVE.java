package su.rumishistem.rumistatus.MODULE;

import static su.rumishistem.rumistatus.Main.SERVER_LIST;
import su.rumishistem.rumistatus.TYPE.SERVER_DATA;

public class SERVER_DATA_RESOLVE {
	public static SERVER_DATA getID(String ID) {
		for (SERVER_DATA SERVER:SERVER_LIST) {
			if (SERVER.getID().equals(ID)) {
				return SERVER;
			}
		}

		return null;
	}
}
