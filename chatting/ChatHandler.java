//쓰레드 핸들러 : ChatHandler클래스
package chatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatHandler extends Thread {
	Socket client; //소켓 객체
	BufferedReader input; //입력스트림
	PrintWriter output; //출력스트림
	ChatServer server; //서버 객체
	
	//생성자, 소켓객체로부터 입출력 스트림을 얻어냄
	public ChatHandler(ChatServer server, Socket client) throws IOException {
		this.server = server;
		this.client = client;
		
		input = new BufferedReader(
				new InputStreamReader(client.getInputStream()));
		output = new PrintWriter(
				new OutputStreamWriter(client.getOutputStream()));
	}
	
	//클라이언트가 보낸 메시지를 읽는 메소드
	public void run() {
		String name = "";
		try {
			//클라이언트가 보낸메시지 읽음
			name = input.readLine();
			//클라이언트가 보낸메시지를 모든 클라이언트에게 중계(보냄)
			//모든 사용자에게 메시지를 중계하는 broadcast()메소드 호출
			broadcast (name + " 님 입장.");
			
			//무한히 클라이언트가 보낸 메시지를 받을 수 있도록 무한루프로 처리
			while (true) {
				//클라이언트가 보낸 메시지 읽음
				String msg = input.readLine();				
				//모든 사용자에게 메시지를 중계하는 broadcast()메소드 호출
				broadcast(name + " : " + msg);
			}
		} catch (Exception ex) {
			System.out.println (name + " 님 퇴장, "
					+ "IP: " + client.getInetAddress());
					
		} finally {//채팅창의 닫기단추를 클릭하면 수행
			server.handlers.removeElement(this);
			broadcast (name + " 님 퇴장");
			try {//리소스 해제
				input.close();
				output.close();
				client.close();
			} catch (IOException ex) {  
				ex.printStackTrace();
			}
		}
	}
	
	//현재 접속한 모든 클라이언트에게 메시지를 보냄
	protected void broadcast(String message) {
		//모든 사용자들에게 메시지를 중계하는 동안 
		//벡터에서 클라이언트의 추가와 제거가 안됨
		synchronized (server.handlers) {
			//현재 벡터안에 있는 클라이언트의 수를 얻어냄
			int n = server.handlers.size();
			
			//접속한 모든 사용자에게 메시지를 보내기 위해 사용자의 수만큼 반복
			for(int i=0; i < n; i++) {
				//클라이언트 하나를 얻어냄
				ChatHandler handler = server.handlers.elementAt(i);
				try{
					//메시지를 보내는 동안 출력스트림을 다른곳에서 사용못하게 함
					synchronized (handler.output) {
						//클라이언트에게 메시지 보냄
						handler.output.println(message);
					}
					handler.output.flush ();
				}catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
