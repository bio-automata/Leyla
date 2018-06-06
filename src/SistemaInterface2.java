import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

public class SistemaInterface2 extends ReceiverAdapter implements RequestHandler{
	private SistemaNucleo sistema;
	
	private Scanner teclado = new Scanner(System.in);
	private String line;
	
	private JChannel channel;
	private String user_name = System.getProperty("user.name", "n/a");
	boolean executando = true;
	
	public SistemaInterface2() {
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
		
		System.out.println("[GLOBAL BOT] ** Usuários ativos: "+new_view.getMembers());
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
		    	
		    }
			
			System.out.println(msg);
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
	
	public boolean confirmaNovoItem() throws Exception {
		RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, 5000);
        //Channel channel = new JChannel();
        MessageDispatcher disp = new MessageDispatcher(this.channel, null, null, this);
        //channel.connect("confirma novo item");
        
        System.out.println("Casting message");
        RspList<String> rsp_list = disp.castMessage(null, new Message(null, null, new String("[Msg teste]")),opts);
        //System.out.println();
        
        //channel.close();
        disp.stop();
        System.out.println(rsp_list.getResults());
        
        /*
        if(rsp_list.getResults().get(0).equals("Success!")){
        	return true;
        }*/
        
        return false;
    }
	
	public void menu() throws Exception{
		System.out.print("> ");
        line = teclado.nextLine().toLowerCase();
        
	    if(line.startsWith(".novo item:")){
			
			 
			//RpcDispatcher disp=new RpcDispatcher(this.channel, this);

			//RspList rsp_list=disp.callRemoteMethods(null,"print", new Class[]{int.class}, opts);
			
			//channel=new JChannel();
			//MessageDispatcher qdisp = new MessageDispatcher(channel, null, null, this);
	        //RequestOptions opts = new RequestOptions(ResponseMode.GET_MAJORITY, 5000);
	        //RspList<Item> rsp_list = disp.castMessage(null, new Message(null, null, new String("msg teste")), opts);
	        //RspList<Item> rsp_list = disp.sendMessage(new Message(null, null, new String("msg teste")), opts);
	        //channel.connect("51234312Msg");
	        //System.out.println("Responses:\n" +rsp_list.getResults());
	    	
	    	//MessageDispatcher  disp=new MessageDispatcher(channel, null, null, this);
	        //channel.connect("MessageDispatcherTestGroup");

	        confirmaNovoItem();
	    	
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
	    	line = line.split(":")[1];
	    	int id = sistema.gerarIdSala();
			String item = line.split(";")[0];
			String lanceInicial = line.split(";")[1];
			String valorLance = line.split(";")[2];
			String user = this.user_name;
			long data = System.currentTimeMillis();
	    	
			//String msg = ".nova sala:"+id+";"+item+";"+user+";"+data;
			String msg = ".nova sala:"+id+";"+item+";"+lanceInicial+";"+valorLance+";"+user+";"+data;
			sistema.criarNovaSala(msg);
			sistema.historico(msg);
	    	this.channel.send(new Message(null, null, msg));
		}
	    else if(line.startsWith(".entrar sala:")){
	    	this.channel.disconnect();
	    	sistema.entrarSala(line);
	    	
	    	channel.connect("GLOBAL");
			channel.getState(null, 10000);
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
		else if(line.startsWith("quit") || line.startsWith("exit") || line.startsWith("sair")){
	        this.executando = false;
	    }
		else if(line.startsWith(":")){
			String user = this.user_name;
			String msg = "["+user+"]"+line.replace(":", ": ");
			this.channel.send(new Message(null, null, msg));
		}
		else{
			System.out.println("[GLOBAL BOT]: Ooops... operação inválida!");
			sistema.help();
		}
    }
	
	public static void main(String[] args) {
		new SistemaInterface2().start();
	}


	@Override
	public Object handle(Message msg) throws Exception {
		//System.out.println("handle(): " + msg);
        return "Success!";
	}
}
