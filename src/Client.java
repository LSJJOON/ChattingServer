import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Client {

	private String id;
	private String pass;
	private String name;
	private boolean isOnline;
	List<String> friendRequestList;
	Boolean flag;
	
	InputStream inputStream;
	OutputStream outputStream;
	InputStreamReader inputStreamReader;
	OutputStreamWriter outputStreamWriter;
	
	private final char FRIEND_ADD_TYPE = 'f';
	private final char REQUEST_FRIEND_ADD_TYPE = 'r';
	private final char REQUEST_RECEIVED_FRIEND_ADD_TYPE = 'a';
	
	private Socket socket;
	
	public Client(String id, String pass, String name) {
		this.id = id;
		this.pass = pass;
		this.name = name;
		this.isOnline = false;
		friendRequestList = new ArrayList<>();
	}
	
	public void receive() throws IOException {
		flag = true;
		Runnable runnable =()->{
		while(flag){
			
			char[] requestType = new char[1];
			try {
				inputStreamReader.read(requestType);
				switch (requestType[0]) {
				case FRIEND_ADD_TYPE:
					sendRequestType(FRIEND_ADD_TYPE);	//친구 검색
					findFriend();
					break;
				case REQUEST_FRIEND_ADD_TYPE:
					sendRequestType(REQUEST_FRIEND_ADD_TYPE);	//친구에게 친구 요청
					addRequestToFriend();
					break;
				case REQUEST_RECEIVED_FRIEND_ADD_TYPE:
					sendReceivedRequestFriend();
				}
			} catch (Exception e) {
				stopClient();
				e.printStackTrace();
			}
			
		}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	private void sendReceivedRequestFriend() {
		String msg="";
		for(String temp : friendRequestList){
			msg += temp+"/";
		}
		if(msg.equals("")){
			try {
				sendMsg(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addRequestToFriend() {
		char[] readBytes = new char[30];
		try {
			inputStreamReader.read(readBytes);
		} catch (Exception e) {
			stopClient();
			e.printStackTrace();
		}
		String data = new String(readBytes).trim();
		System.out.println(data);
		StringTokenizer st = new StringTokenizer(data, "/");
		String clientId = st.nextToken().trim();
		String targetId = st.nextToken().trim();
		System.out.println(clientId+"님이 "+targetId+"님에게 친구요청을 했습니다.");
		for(Client client : ServerMain.clientList){
			if(client.getId().equals(targetId)){
				if(client.isOnline){
					readBytes = new char[30];
					try {
						System.out.println("클라이언트("+client.getId()+")에 리퀘스트 보냅니다");
						client.sendRequestType(REQUEST_RECEIVED_FRIEND_ADD_TYPE);
						client.friendRequestList.add(clientId);

					} catch (Exception e) {
						stopClient();
						e.printStackTrace();
					}

				}
			}
		}
	}

	private void sendRequestType(char requestType) {
		try {
			outputStreamWriter.write(requestType);
			outputStreamWriter.flush();
		} catch (Exception e) {
			stopClient();
			e.printStackTrace();
		}
		
	}

	private void findFriend() throws IOException {
		char[] readBytes = new char[100];
		inputStreamReader.read(readBytes);
		String data = new String(readBytes).trim();
		System.out.println(data);
		if(isExistFriend(data)){
			sendMsg("find");
		}else{
			sendMsg("noClient");
		}
	}

	private boolean isExistFriend(String findId) {
		for(Client client : ServerMain.clientList){
			if(client.getId().equals(findId)){
				return true;
			}
		}
		return false;
	}

	public void sendMsg(String msg) throws IOException{
		outputStreamWriter.write(msg);
		outputStreamWriter.flush();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.inputStream = socket.getInputStream();
			this.outputStream = socket.getOutputStream();
		} catch (Exception e) {
			stopClient();
			e.printStackTrace();
		}
		
		inputStreamReader = 
				new InputStreamReader(inputStream);
		outputStreamWriter = new OutputStreamWriter(outputStream);
	}

	public void stopClient(){
		flag = false;
		System.out.println("클라이언트가 종료하였습니다.");
		this.isOnline= false;
		try{
			inputStreamReader.close();
			outputStreamWriter.close();
			socket.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
