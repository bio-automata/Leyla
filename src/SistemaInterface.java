import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;


import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

public class SistemaInterface extends ReceiverAdapter implements RequestHandler{
	
	private SistemaNucleo sistema;
	
	private String user_name = System.getProperty("user.name", "n/a");
	private Scanner teclado = new Scanner(System.in);
	private String line;
	boolean executando = true;
	
	private LockService lockservice;
	private Lock salasLock;
	private Lock itensLock;
	
	private JChannel channel;
	//private MessageDispatcher dispachante;
	
	
	private View old_view;
	
	public SistemaInterface() {
		this.sistema = new SistemaNucleo();
		this.user_name = System.getProperty("user.name", "n/a");
		this.teclado = new Scanner(System.in);
		this.executando = true;
	}
 
	
	private void start() {
		try{
			this.channel=new JChannel("../meu.xml");		//usa a configuração default
			//this.channel=new JChannel();
			this.channel.setDiscardOwnMessages(true);
			this.channel.setReceiver(this);	//quem irá lidar com as mensagens recebidas
			System.out.println("entrou C");
			this.channel.connect("GLOBAL");
			System.out.println("entrou D");
			
			try{
				this.channel.getState(this.channel.getView().getCreator(), 1000000);
			}
			catch(Exception e){
				System.out.println("Falha ao recuperar estado");
			}
			
			
			System.out.println("entrou E");
			this.lockservice = new LockService(this.channel);
			this.salasLock = lockservice.getLock("salas");
			this.itensLock = lockservice.getLock("itens");
			this.old_view = null;
			
			eventLoop();
			this.channel.close();
		}
		catch(Exception e){
			System.out.println("Falha ao estabelecer conexão com o sistema");
		}
	}
		
	public void viewAccepted(View new_view) {
		//System.out.println("[GLOBAL BOT] ** Usuários xativos: "+this.channel.getView().getMembers());
		System.out.println("[GLOBAL BOT] ** Usuários ativos: "+new_view.getMembers());
		//System.out.println("[GLOBAL BOT] ** Usuários ativos: "+new_view.getMembers().get(new_view.size()-1));
		
		//System.out.println("[GLOBAL BOT] ** Lider: "+new_view.getCreator());
		//System.out.println("[GLOBAL BOT] ** Novato: "+new_view.getMembers().get(new_view.size()-1));
		//View old_view = this.channel.getView();
		
		// A FAZER: invocar o settate (receber) caso minha "versao"
		// do modelo de dados estiver desatualizada
		
		
		// diferencie quem saiu e voltou de quem ainda esta no grupo
		//compare quem e' o ultimo, se for o seu Address, voce e'o novato
		//senao, voce e'o veterano
		
		
				
		//System.out.println("id: "+this.channel.getAddress()+" >"+new_view.getMembers().get(new_view.size()-1));
		
		//verifica se ele é o último membro à entrar no canal
		
		System.out.println("É novato "+this.channel.getAddress()+" "+new_view.getMembers().get(new_view.getMembers().size()-1));
		
		View view = this.channel.getView();
		if(this.channel.getAddress().equals(view.getMembers().get(new_view.getMembers().size()-1))){
        	try {
				//this.channel.getState(null, 10000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
        /*
		System.out.println("id: "+old_view+" >"+new_view);
		if(this.old_view==null||this.channel.getAddress()==new_view.getMembers().get(new_view.size()-1)){
		//if(this.old_view==null){
			try {
				this.channel.getState(null, 10000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//new_view.
		this.old_view = new_view;*/
	}
	
	public void receive(Message msgrcvd) {
		synchronized(sistema){
			String msg = msgrcvd.getObject().toString();
			if(msg.startsWith(".novo item:")){
				this.sistema.criarNovoItem(msg);
				this.sistema.historico(msg);
			}
		    else if(msg.startsWith(".nova sala:")){
		    	this.sistema.criarNovaSala(msg);
		    	this.sistema.historico(msg);
		    }
		    else if(msg.startsWith("[")){
		    	
		    }
		}
	}
	
	public void getState(OutputStream output) throws Exception {
		//Util.objectToStream(new String("teste"), new DataOutputStream(output));
		synchronized(this.sistema) {
			Util.objectToStream(this.sistema, new DataOutputStream(output));
		}
	}
	
	public void setState(InputStream input) throws Exception {
		//SistemaNucleo system = ;
		//this.sss = (String)Util.objectFromStream(new DataInputStream(input));
		synchronized(this.sistema) {
			this.sistema = (SistemaNucleo)Util.objectFromStream(new DataInputStream(input));
			//this.sistema.setItens((ArrayList<Item>)Util.objectFromStream(new DataInputStream(input)));
		}
	}
	
	private void eventLoop() throws Exception {
		//this.channel.getState(null, 10000);
		System.out.println("[GLOBAL BOT]: Bem vindo ao Sitema de Leilões");
		System.out.println("[GLOBAL BOT]: Se você é novato no sistema digite o comando [help] para acessar a lista de comandos");
		
		while( executando ) {
			try {
				this.menu();
			}catch(Exception e) {
				System.out.println("Falha: sistema não pode completar uma ação");
			}
		}
	}
	
	public void notificarCluster(Message msg) throws Exception {
		System.out.println("Notificando Cluster");
        RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, 5000);
      
        MessageDispatcher dispachante = new MessageDispatcher(this.channel, null, null, this);
        RspList<String> rsp_list = dispachante.castMessage(null, msg,opts);
        dispachante.stop();
        System.out.println(rsp_list.getResults());
    }
	
	public void menu() throws Exception{
		System.out.print("> ");
        line = teclado.nextLine().toLowerCase();
        
        
        
        
        
        
	    if(line.startsWith(".novo item:")){
	    	String msg = "";
			String item = line.split(":")[1];
			String user = this.user_name;
			long data = System.currentTimeMillis();
			
			//itensLock.lock();
			try{
				int id = sistema.gerarIdItem();
				msg = ".novo item:"+id+";"+item+";"+user+";"+data;
				this.notificarCluster(new Message(null, null, msg));
			}
			finally{
				//itensLock.unlock();
				
			}
			sistema.criarNovoItem(msg);
			sistema.historico(msg);
			
		}
	    else if(line.startsWith(".nova sala:")){
	    	
			
	    	int id = 0;
	    	String msg = "";
	    	line = line.split(":")[1];
			String item = line.split(";")[0];
			String user = this.user_name;
			long data = System.currentTimeMillis();
	    	
			salasLock.lock();
			try{
				id = sistema.gerarIdSala();
				msg = ".nova sala:"+id+";"+item+";"+user+";"+data;
				//this.channel.send(new Message(null, null, msg));
				this.notificarCluster(new Message(null, null, msg));
			}
			finally{
				salasLock.unlock();
			}
			
			sistema.criarNovaSala(msg);
			sistema.historico(msg);
			
			if(id>0){
				this.channel.disconnect();
		    	sistema.entrarSala(".entrar sala:"+id);
		    	channel.connect("GLOBAL");
				//channel.getState(null, 10000);
			}
		}
	    else if(line.startsWith(".entrar sala:")){
	    	this.channel.disconnect();
	    	sistema.entrarSala(line);
	    	
	    	channel.connect("GLOBAL");
			//channel.getState(null, 10000);
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
		else if(line.startsWith(".atualizar")){
			this.channel.getState();
		}
		else if(line.startsWith("quit") || line.startsWith("exit") || line.startsWith("sair")){
	        this.executando = false;
	    }
		else if(line.startsWith(":")){
			String user = this.user_name;
			String msg = "["+user+"]"+line.replace(":", ": ");
			this.channel.send(new Message(null, null, msg));
		}
		else if(line.startsWith("view")){
			System.out.println(this.channel.getView());
		}
		else{
			System.out.println("[GLOBAL BOT]: Ooops... operação inválida!");
			sistema.help();
		}
    }
	
	@Override
	public Object handle(Message msgrcvd) throws Exception {
		synchronized(sistema){
			String msg = msgrcvd.getObject().toString();
			if(msg.startsWith(".novo item:")){
				this.sistema.criarNovoItem(msg);
				this.sistema.historico(msg);
				return "+1 Item";
			}
		    else if(msg.startsWith(".nova sala:")){
		    	this.sistema.criarNovaSala(msg);
		    	this.sistema.historico(msg);
		    	return "+1 Sala";
		    }
		    else if(msg.startsWith("[LEILOEIRO]: atualizar")){
		    	this.channel.getState(null, 10000);
		    }
			if(msg.startsWith("Sala finalizada:")){
				msg = msg.split(":")[1];
				
				this.sistema.historico("Leilão:"+msg);
				return "finalizado";
			}
		    else {
		    	System.out.println("[DEBBUG]: "+msg);
		    }
		}
        
		return "teste";
	}
	
	
	public static void main(String[] args) {
		new SistemaInterface().start();
	}
}
