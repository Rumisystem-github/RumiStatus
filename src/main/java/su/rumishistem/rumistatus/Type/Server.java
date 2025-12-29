package su.rumishistem.rumistatus.Type;

public class Server {
	public String id;
	public String name;
	public String description;
	public Protocol protocol;
	public String endpoint;

	public ServerStatus status = ServerStatus.IDK;
	public long ping = -1;
}
