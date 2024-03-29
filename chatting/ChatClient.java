//클라이언트 : ChatClient클래스
package chatting;

import java.awt.*;
import java.awt.event.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient extends JFrame implements Runnable, ActionListener{
	private static final long serialVersionUID = 1L;
	
	BufferedReader input; //입력스트림
	PrintWriter output; //출력스트림
	Thread handler; //ChatHandler와 메시지를 주고 받기 위한 쓰레드
	Container c;
	JTextArea display; //채팅창에서 대화를 표시
	JTextField id; //사용자 id
	JTextField inData; //사용자가 메시지를 입력하는 필드
	JLabel displayId; //채팅창에 id표시하는 레이블
	JButton	send; //[보내기]버튼
	CardLayout window;
	
	//생성자로 채팅창의 UI를 구성
	public ChatClient(){
		super("채팅 클라이언트");
		
		//컨테이너에 레이아웃 매니저 지정
		c = getContentPane();
		window = new CardLayout();
		c.setLayout(window);
		
		//로그인 창을 구성
		JPanel login = new JPanel(new BorderLayout());
		JPanel bottom = new JPanel();
		JLabel idLable = new JLabel("로그인:");
		//아이디 입력필드의 생성과 리스너 등록
		id = new JTextField(15);
		id.addActionListener(this);
		//로그인창의 컴포넌트배치
		bottom.add(idLable);
		bottom.add(id);
		login.add("South",bottom);
		c.add("login", login);

		
		//채팅창을 구성
		JPanel chat = new JPanel(new BorderLayout());
		//채팅창의 대화 표시 텍스트에리어 생성 및 스크롤바 추가, 배치
		display = new JTextArea (10,30);
		JScrollPane s = new JScrollPane(display);
		chat.add ("Center", s);//패널에 추가
		display.setEditable(false); //대화표시 화면에 임의로 입력 금지
		//채팅창의 메시지입력과[보내기]버튼 생성및 배치
		JPanel mess = new JPanel();
		mess.add(new JLabel("메시지"));
		//메시지 입력필드의 생성과 리스너 등록, 배치
		inData = new JTextField(20);
		mess.add(inData);
		inData.addActionListener(this);
		//[보내기]버튼의 생성과 리스너 등록, 배치
		send = new JButton("보내기");
		mess.add(send);
		send.addActionListener(this);
		//채팅창의 컴포넌트 배치
		chat.add("South",mess);
		displayId = new JLabel();
        chat.add("North",displayId);
        c.add("chat", chat);
        window.show(c, "login");
        
        setSize(400, 400);
        setVisible(true);	
	}
	
	//ChatHandler와 메시지를 주고받은 일을하는 쓰레드 생성 후 실행시킴
	public void clientStart() {
		handler = new Thread(this);
		handler.start ();
	}	
	
	//쓰레드를 실행시 자동으로 실행
	public void run() {
		try {
			//소켓객체 생성
			Socket s = new Socket ("127.0.0.1", 5000);
		    //입출력스트림 얻어냄
			input = new BufferedReader(
					  new InputStreamReader(s.getInputStream()));
			output = new PrintWriter(
					  new OutputStreamWriter(s.getOutputStream()));
			//쓰레드 핸들러가 보낸 메시지를  받는 execute()메소드
			execute(); 
		} catch (IOException ex) {
			ex.printStackTrace (System.out);
		}
	}

	//쓰레드 핸들러인 ChatHandler가 보낸 메시지를 받아서 대화화면에 표시
	public void execute() {
		try {
			while(true) {//무한히 메시지를 받음
				//받은 메시지를 대화화면에 표시
				String line = input.readLine();
				display.append (line + "\n");
			}
		}catch (IOException ex) {
			ex.printStackTrace (System.out);
		}finally{//메시지 받는 것이 중단될때 수행
			stop ();
		}
	}
	
	//서버와의 연결을 중단시 사용하는 메소드
	public void stop () {
		if (handler != null) {
			try {
				if(output != null) {
				   input.close();
				   output.close();
				}
			}catch (IOException ioe) { 
				ioe.printStackTrace();
			}
		}
		//사용자 쓰레드 제거
		handler = null;
    }   
	
	//사용자의 아이디 처리와 쓰레드 핸들러로 메시지를 보내는 것을 처리
	public void actionPerformed (ActionEvent e) {
		//이벤트가 발생한 컴포넌트 얻어냄
		Component event = (Component) e.getSource();
		
		//이벤트가 발생한 컴포넌트가 아이디 입력필드이면 수행
		if(event == id) {
			//아이디를 얻어내 채팅창의 제일위에 표시
			String name = id.getText().trim();
	        displayId.setText(name);
	        //아이디를 입력하지 않으면 실행중단.
	        if(name == null || name.length() == 0) {
	       		return;
	       	}
	        
	        //쓰레드 핸들러인 ChatHandler로 메시지 보냄
	       	output.println(name);
	       	output.flush();
	       	//채팅창이 표시되도록함
	       	window.show(c, "chat");
	       	inData.requestFocus();
		
		//이벤트의 발생이 메시지 입력 필드나 [보내기]버튼이면 수행
		}else if(event == inData || event == send) {
			//쓰레드 핸들러인 ChatHandler로 메시지 보냄
			output.println(inData.getText());
			output.flush();
			inData.setText("");
		}
	}
	
	public static void main(String[] args) {
		ChatClient cc = new ChatClient();
		cc.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		cc.clientStart();
	}
}