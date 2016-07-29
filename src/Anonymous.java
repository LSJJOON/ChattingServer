
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.Authenticator.RequestorType;
import java.util.StringTokenizer;

public class Anonymous {
	private final char LOGIN_TYPE = 'l';
	private final char REGIST_TYPE = 'r';
	InputStreamReader inputStreamReader;
	OutputStreamWriter outputStreamWriter;
	Socket socket;
	boolean flag;
	Client client;
	
	
	public Anonymous(Socket socket) {
		try {
			inputStreamReader = new InputStreamReader(socket.getInputStream());
			outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
			this.socket=socket;
			flag = true;
			receive();
		} catch (IOException e) {
			closeConnection();
			e.printStackTrace();
		}

	}
	private void receive() throws IOException {
		Runnable runnable =()->{
			try{
				while(flag){
					System.out.println("receive복귀");
					char[] requestType = new char[1];
					inputStreamReader.read(requestType);
					switch (requestType[0]) {
					case LOGIN_TYPE:
						sendReadyToClient();
						if(checkLogin()){
							client.setSocket(socket);
							client.setOnline(true);
							client.receive();

							flag=false;
						}
						break;
					case REGIST_TYPE:
						sendReadyToClient();
						registClient();
						break;
					default:
						System.out.println("이상발생");
						break;
					}
				}
			}catch (Exception e) {
				closeConnection();
			}

		};
		Thread thread = new Thread(runnable);
		thread.start();

		
	}
	private void registClient() throws IOException {
		System.out.println("registClient함수");
		char[] readBytes = new char[100];
		inputStreamReader.read(readBytes);
		String data = new String(readBytes);
		StringTokenizer st= new StringTokenizer(data, "/");
		String id = st.nextToken();
		String pass = st.nextToken();
		String name = st.nextToken();
		String msg = null;
		for(Client client : ServerMain.clientList){
			if(id.equals(client.getId())){
				msg = "already";
				break;
			}
		}
		if(msg==null){
			ServerMain.clientList.add(new Client(id, pass, name));
			msg = "clear";
		}
		
		outputStreamWriter.write(msg);
		outputStreamWriter.flush();
		
	}
	
	private void sendReadyToClient() throws IOException {
		outputStreamWriter.write("ready");
		outputStreamWriter.flush();
		
	}
	
	private void sendMsg(String msg) throws IOException {
		outputStreamWriter.write(msg);
		outputStreamWriter.flush();
		
	}
	private boolean checkLogin() throws IOException {
		boolean isLoginClear = false;
		
		System.out.println("checkLogin함수");
		char[] readBytes = new char[100];
		inputStreamReader.read(readBytes);
		String data = new String(readBytes).trim();
		StringTokenizer st = new StringTokenizer(data, "/");
		String id = st.nextToken();
		String pass = st.nextToken();
		String msg = null;
		for(Client clientTemp:ServerMain.clientList){
			if(clientTemp.getId().equals(id)){
				if(clientTemp.getPass().equals(pass)){
					msg = "success";
					client = clientTemp;
					isLoginClear = true;
				}else{
					msg = "passFail";
				}
				break;
			}
		}
		if(msg==null){
			msg = "noClient";
		}
		sendMsg(msg);
		System.out.println("로그인함수 종료");
		return isLoginClear;
		
	}
	
	public void closeConnection(){
		try{
			System.out.println("클라이언트와 연결이 끊깁니다");
			inputStreamReader.close();
			outputStreamWriter.close();
			socket.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
