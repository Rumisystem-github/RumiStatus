package su.rumishistem.rumistatus.TYPE;

public class SERVER_DATA {
	private String ID;
	private String NAME;
	private String DESC;
	private String EP;
	private SERVER_STATUS STATUS = SERVER_STATUS.NG;

	public SERVER_DATA(String ID, String NAME, String DESC, String EP) {
		this.ID = ID;
		this.NAME = NAME;
		this.DESC = DESC;
		this.EP = EP;
	}

	public String getID() {
		return ID;
	}

	public String getNAME() {
		return NAME;
	}

	public String getDESC() {
		return DESC;
	}

	public String getEP() {
		return EP;
	}

	public PROTOCOL getPROTOCOL() {
		if (EP.startsWith("http://") || EP.startsWith("https://")) {
			return PROTOCOL.HTTP;
		} else {
			return PROTOCOL.HTTP;
		}
	}

	public SERVER_STATUS getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(SERVER_STATUS STATUS) {
		this.STATUS = STATUS;
	}
}
