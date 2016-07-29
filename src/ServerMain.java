import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
	
	ServerSocket serverSocket;
	Socket socket;
	public static List<Client> clientList;
	

	public static void main(String[] args) {
		
		ServerMain sm = new ServerMain();
		try {
			ServerMain.clientList = new ArrayList<Client>();
			sm.startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	private void startServer() throws IOException {
		serverSocket = new ServerSocket(5001);
		while(true){
			socket = serverSocket.accept();
			System.out.println("클라이언트 접속");
			Anonymous anonymous = new Anonymous(socket);
		}
	}
	

}
