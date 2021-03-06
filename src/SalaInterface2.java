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
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

public class SalaInterface2 extends ReceiverAdapter implements RequestHandler{
	private Sala sala;
	private double lance;
	
	private String user_name;
	private Scanner teclado = new Scanner(System.in);
	private String line;
	
	private JChannel channel;
	private ChapeuDeLeiloeiro leiloeiro;
	
	boolean executando = true;
	private List<Address> usuariosAtivos;
	//private String props;
	
	public SalaInterface2(Sala sala) {
		this.sala = sala;
		this.lance = 0;
		
		this.user_name = System.getProperty("user.name", "n/a");
		this.teclado = new Scanner(System.in);
		this.executando = true;
		
		//this.start();
	}
 
	
	public void start() {
		try{
			//System.out.println("teste B");
			this.channel=new JChannel("../meu.xml");		//usa a configuração default
			//this.channel=new JChannel();		//usa a configuração default
			this.channel.setDiscardOwnMessages(true);
			this.channel.setReceiver(this);	//quem irá lidar com as mensagens recebidas
			this.channel.connect(this.sala.getNome());
			//this.channel.getState(null, 10000);
		
			if(this.channel.getAddress()==this.channel.getView().getCreator()){
				Thread thread = new Thread(this.leiloeiro);
				thread.start();
			}
			
			try{
				this.channel.getState(this.channel.getView().getCreator(), 1000000);
			}
			catch(Exception e){
				System.out.println("Falha ao recuperar estado");
			}
			
			//tenta entrar no leilão
			if(this.entraSala()) {
				eventLoop();
			}
			else {
				System.out.println("Sala Fechada");
			}
			
			channel.close();
		}
		catch(Exception e){
			System.out.println("Falha ao estabelecer conexão com a "+this.sala.getNome());
		}
	}
	
	

	public boolean entraSala() {
		String msg = "participar";
		
		try{
			//msg = ".novo usuario:"+";"+cpf+";"+senha;
			//logado = this.notificarCluster(new Message(null, null, msg)).get(0);
			if(this.notificarCluster(msg).equals("1")) {
				return true;
			}
			//return this.sistema.loginUsuario(msg);
		}catch(Exception e ){}
		finally{
			//this.usuariosLock.unlock();
		}
		
		return false;
	}
	
	public void viewAccepted(View new_view) {
		//System.out.println("[ÁRBITRO DA SALA] \n	Usuários Ativos:"+new_view);
		//usuariosAtivos = new_view.getMembers();
		this.usuariosAtivos = new_view.getMembers();
		//System.out.println("[ÁRBITRO DA SALA] ** Usuários ativos: "+new_view.getMembers());
		//System.out.println("[ÁRBITRO DA SALA] ** Lider: "+new_view.getCreator());
		//System.out.println("[ÁRBITRO DA SALA] ** Novato: "+new_view.getMembers().get(new_view.size()-1));
		
		
		try {
			this.notificarCluster("bem vindo");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		if(this.user_name.equals(new_view.getMembers().get(0))){
			this.leiloeiro = new ChapeuDeLeiloeiro(this);
			new Thread(this.leiloeiro).start();
		}
		else if(this.user_name.equals(new_view.getMembers().get(new_view.size()-1))){
			try {
				this.leiloeiro = null;
				this.channel.getState(null, 10000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
	
	public void receive(Message msgrcvd) {
		
	}
	
	public void getState(OutputStream output) throws Exception {
		synchronized(sala) {
			String msg = sala.getDonoDoUltimoLance()+":"+sala.getLanceAtual();
			Util.objectToStream(msg, new DataOutputStream(output));
			Util.objectToStream(sala.getLanceAtual(), new DataOutputStream(output));
		}
	}
	
	public void setState(InputStream input) throws Exception {
		synchronized(sala) {
			String msg = (String)Util.objectFromStream(new DataInputStream(input));
			
			this.sala.setDonoDoUltimoLance(msg.split(":")[0]);
			this.sala.setLanceAtual(Double.parseDouble(msg.split(":")[1]));
			//this.sala=(Sala)Util.objectFromStream(new DataInputStream(input));
			//this.sala.setLanceAtual((double)Util.objectFromStream(new DataInputStream(input)));
		}
	}
	
	private void eventLoop() throws Exception {
		this.channel.getState(null, 10000);
		
		while( executando ) {
			try {
				if(!this.sala.isLeilaoFinalizado()){
					this.menu();
				}
				else{
					System.out.println("Sala não está mais aberta para lances");
				}
			}catch(Exception e) {
				System.out.println("Falha: sala não pode completar uma ação");
			}
		}
	}

	public List<String> notificarCluster(String text) throws Exception {
		System.out.println("Notificando Cluster...");
		Message msg  = new Message(null, null, text);
        RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, 5000);
        
        MessageDispatcher dispachante = new MessageDispatcher(this.channel, null, null, this);
        RspList<String> rsp_list = dispachante.castMessage(null, msg, opts);
        dispachante.stop();
        return rsp_list.getResults();
    }
	
	public void menu() throws Exception{
		System.out.println(this.sala.getNome()+" ("+this.sala.getItem()+")\nMembros: "+this.usuariosAtivos+"\nLance Atual: "+this.sala.getLanceAtual());
		System.out.print("> ");
        line = teclado.nextLine().toLowerCase();
        //System.out.println("Digitou: "+line);
        //this.channel.send(new Message(null, null, line));
        
	    if(line.startsWith("quit") || line.startsWith("exit") || line.startsWith("sair")){
	        this.executando = false;
	    }
		else{
			double lance = Double.parseDouble(this.line);
			if(lance>this.sala.getLanceAtual()){
				//this.leiloeiro.restart();
				String msg = ".lance:"+user_name+";"+lance;
				this.lance = lance;
				this.sala.novoLance(lance, this.user_name);
		    	//this.channel.send(new Message(null, null, msg));
		    	//this.notificarCluster(new Message(null, null, msg));
				this.notificarCluster(msg);
				
				if(this.leiloeiro!=null){
					this.leiloeiro.restart();
				}
			}
			else{
				System.out.println("[Árbitro da sala]: Lance inválido!\nDigite um valor maior do que o lance atual ");
			}
		}
    }


	@Override
	public Object handle(Message msgrcvd) throws Exception {
		synchronized(sala){
			try{
				String msg = msgrcvd.getObject().toString();
				if(msg.startsWith("[LEILOEIRO]: finalizar")){
					System.out.println("finalizando");
					this.executando = false;
					this.sala.setLeilaoFinalizado(true);
					
					this.desconectar();
					
					return "finalizado";
				}
				else if(msg.startsWith("[LEILOEIRO]:")){
					System.out.println(msg);
				}
				else{
					msg = msg.split(":")[1];
					double lance = Double.parseDouble(msg.split(";")[1]);
					if(lance>this.sala.getLanceAtual()){
						//this.sala.setValorLance(lance);
						this.sala.novoLance(lance, msg.split(";")[0]);
						//msg = "[Árbitro da sala]: Usuário "+msgrcvd.src()+" deu um lance de "+lance;
						System.out.println("[Árbitro da sala]: Usuário "+msgrcvd.src()+" deu um lance de "+lance);
						
						if(this.leiloeiro!=null){
							this.leiloeiro.restart();
						}
					}
				}
			}
			catch(Exception e){
				
			}
		}
		
		return null;
	}
		
	public boolean temMaisAlguemNaSala(){
		return this.channel.getView().size()!=1;
	}
	
	public boolean isLanceNulo(){
		return this.sala.getLanceAtual()==0;
	}
	
	public void desconectar(){
		try {
			System.out.println("Vencedor: "+this.sala.getItem().getNome()+", lance: "+this.sala.getDonoDoUltimoLance()+";"+this.lance);
			this.channel.disconnect();
			this.channel.connect("GLOBAL");
			this.notificarCluster("Sala finalizada:"+this.sala.getItem().getNome()+";"+this.sala.getDonoDoUltimoLance()+";"+this.lance);
		} catch (Exception e) {
			System.out.println("Falha ao anunciar vencedor");
			e.printStackTrace();
		}
	}
	
	public String vencedor(){
		return "Vencedor: "+this.sala.getDonoDoUltimoLance()+", item: "+this.sala.getItem().getNome()+", lance: "+this.sala.getLanceAtual();
	}
}
