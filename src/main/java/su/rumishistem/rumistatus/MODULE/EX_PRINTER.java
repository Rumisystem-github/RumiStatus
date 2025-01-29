package su.rumishistem.rumistatus.MODULE;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import su.rumishistem.rumi_java_lib.EXCEPTION_READER;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;

public class EX_PRINTER {
	public static void PRINT(Exception EX) {
		String EX_TEXT = EXCEPTION_READER.READ(EX);
		for (String LINE:EX_TEXT.split("\n")) {
			LOG(LOG_TYPE.FAILED, LINE);
		}
	}
}
