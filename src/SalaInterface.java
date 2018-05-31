import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

public class SalaInterface extends ReceiverAdapter {
	private Sala sala;
	private double lance;
	
	private Scanner teclado = new Scanner(System.in);
	private String line;
	
	private JChannel channel;
	private String user_name;
	boolean executando = true;
	private List<Address> usuariosAtivos;
	
	public SalaInterface(Sala sala) {
		this.sala = sala;
		this.lance = sala.getLanceInicial();
		
		this.user_name = System.getProperty("user.name", "n/a");
		this.teclado = new Scanner(System.in);
		this.executando = true;
		
		System.out.println("teste A");
		this.start();
	}
 
	
	public void start() {
		try{
			System.out.println("teste B");
			channel=new JChannel();		//usa a configuração default
			channel.setDiscardOwnMessages(true);
			channel.setReceiver(this);	//quem irá lidar com as mensagens recebidas
			channel.connect(this.sala.getNome());
			System.out.println("teste C");
			
			
			channel.getState(null, 10000);
			eventLoop();
			channel.close();
		}
		catch(Exception e){
			System.out.println("Falha ao estabelecer conexão com a "+this.sala.getNome());
		}
	}
	
	public void viewAccepted(View new_view) {
		//System.out.println("[ÁRBITRO DA SALA] \n	Usuários Ativos:"+new_view);
		usuariosAtivos = new_view.getMembers();
	}
	
	public void receive(Message msgrcvd) {
		synchronized(sala){
			
			try{
				double lance = this.sala.getValorLance()*Double.parseDouble(msgrcvd.getObject().toString());
				this.sala.novoLance(lance);
				
				String msg = "[Árbitro da sala]: Usuário "+msgrcvd.src()+" deu um lance de "+lance;
		    	this.channel.send(new Message(null, null, msg));
			}
			catch(Exception e){
				
			}
		}
	}
	
	public void getState(OutputStream output) throws Exception {
		synchronized(sala) {
			Util.objectToStream(sala.getLanceAtual(), new DataOutputStream(output));
		}
	}
	
	public void setState(InputStream input) throws Exception {
		synchronized(sala) {
			//this.sala=(Sala)Util.objectFromStream(new DataInputStream(input));
			this.sala.setLanceAtual((double)Util.objectFromStream(new DataInputStream(input)));
		}
	}
	
	private void eventLoop() throws Exception {
		while( executando ) {
			try {
				this.menu();
			}catch(Exception e) {
				System.out.println("Falha: sala não pode completar uma ação");
			}
		}
	}
	
	public void menu() throws Exception{
		System.out.println(this.sala.getNome()+" ("+this.sala.getItem()+")Membros: "+this.usuariosAtivos+"\nLance Atual:"+this.sala.getLanceAtual()+"\nValor do Lance"+this.sala.getValorLance());
		System.out.print("> ");
        line = teclado.nextLine().toLowerCase();
        //System.out.println("Digitou: "+line);
        this.channel.send(new Message(null, null, line));
        
	    if(line.startsWith("quit") || line.startsWith("exit") || line.startsWith("sair")){
	        this.executando = false;
	    }
		else{
			double lance = this.sala.getValorLance()*Double.parseDouble(this.line);
			String msg = ".lance:"+user_name+";"+lance;
			this.sala.novoLance(lance);
	    	this.channel.send(new Message(null, null, msg));
		}
    }
}
