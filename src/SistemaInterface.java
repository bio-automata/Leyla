import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

public class SistemaInterface extends ReceiverAdapter {
	private SistemaNucleo sistema;
	private Scanner teclado = new Scanner(System.in);
	private String line;
	
	private JChannel channel;
	private String user_name = System.getProperty("user.name", "n/a");
	boolean executando = true;
	
	public SistemaInterface() {
		this.sistema = new SistemaNucleo();
		this.user_name = System.getProperty("user.name", "n/a");
		this.teclado = new Scanner(System.in);
		this.executando = true;
	}
 
	
	private void start() {
		try{
			channel=new JChannel();		//usa a configuração default
			channel.setDiscardOwnMessages(true);
			channel.setReceiver(this);	//quem irá lidar com as mensagens recebidas
			channel.connect("GLOBAL");
			channel.getState(null, 10000);
			eventLoop();
			channel.close();
		}
		catch(Exception e){
			System.out.println("Falha ao estabelecer conexão com o sistema");
		}
	}
		
	public void viewAccepted(View new_view) {
		System.out.println("[DEBBUG] ** View: "+new_view);
	}
	
	public void receive(Message msgrcvd) {
		synchronized(sistema){
			String msg = msgrcvd.getObject().toString();
			if(msg.startsWith(".novo item:")){
				sistema.criarNovoItem(msg);
				sistema.historico(msg);
			}
		    else if(msg.startsWith(".nova sala:")){
		    	sistema.criarNovaSala(msg);
		    	sistema.historico(msg);
		    }
		    else if(msg.startsWith("[")){
		    	System.out.println(msg);
		    }
		}
	}
	
	public void getState(OutputStream output) throws Exception {
		synchronized(sistema) {
			Util.objectToStream(sistema, new DataOutputStream(output));
		}
	}
	
	public void setState(InputStream input) throws Exception {
		synchronized(sistema) {
			this.sistema=(SistemaNucleo)Util.objectFromStream(new DataInputStream(input));
		}
	}
	
	private void eventLoop() throws Exception {
		while( executando ) {
			try {
				this.menu();
			}catch(Exception e) {
				System.out.println("Falha: sistema não pode completar uma ação");
			}
		}
	}
	
	public void menu() throws Exception{
		System.out.print("> ");
        line = teclado.nextLine().toLowerCase();
                        
	    if(line.startsWith("quit") || line.startsWith("exit") || line.startsWith("sair")){
	        this.executando = false;
	    }
	    else if(line.startsWith(".novo item:")){
			int id = sistema.gerarIdItem();
			String item = line.split(":")[1];
			String user = this.user_name;
			long data = System.currentTimeMillis();
			
			String msg = ".novo item:"+id+";"+item+";"+user+";"+data;
			sistema.criarNovoItem(msg);
			sistema.historico(msg);
	    	this.channel.send(new Message(null, null, msg));
		}
	    else if(line.startsWith(".nova sala:")){
	    	int id = sistema.gerarIdSala();
			String item = line.split(":")[1];
			String user = this.user_name;
			long data = System.currentTimeMillis();
	    	
			String msg = ".nova sala:"+id+";"+item+";"+user+";"+data;
			sistema.criarNovaSala(msg);
			sistema.historico(msg);
	    	this.channel.send(new Message(null, null, msg));
		}
	    else if(line.startsWith(".entrar sala:")){
	    	line = line.split(":")[1];
	    	System.out.println(this.user_name+" entrou na sala "+line);
	    }
		else if(line.startsWith(".listar itens")){
			sistema.listarItens();
		}
		else if(line.startsWith(".listar salas")){
			sistema.listarSalas();
		}
		else if(line.startsWith(".creditos")){
			sistema.creditos();
		}
		else if(line.startsWith(":")){
			String user = this.user_name;
			String msg = "["+user+"]"+line.replace(":", ": ");
			this.channel.send(new Message(null, null, msg));
		}
		else{
			sistema.help();
		}
    }
		
	public static void main(String[] args) {
		new SistemaInterface().start();
	}
}
