import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;

import org.jgroups.Address;
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

public class SistemaComunicacao extends ReceiverAdapter implements RequestHandler{
	//protected String ADMINISTRADOR = "ADMINISTRADOR"; 
	protected SistemaNucleo sistema;
	//private String user_name = System.getProperty("user.name", "n/a");
	
	//informacoes relativas ao usuario
	protected Usuario usuario;
	protected Scanner teclado = new Scanner(System.in);
	protected String line;
	boolean executando = true;
	
	//servicos de loock
	protected LockService lockservice;
	protected Lock usuariosLock;
	protected Lock itensLock;
	protected Lock salasLock;
	
	//canal e suas configuracoes
	protected JChannel channel;
	protected MessageDispatcher dispachante;
	protected RequestOptions opcoesDispachante;
	protected List<Address> coordenadores;
	protected int minimoCordenadores;
	protected double porcentagemCordenadores;
	
	
	public SistemaComunicacao() {
		this.sistema = new SistemaNucleo();
		//this.user_name = System.getProperty("user.name", "n/a");
		this.usuario = null;
		this.teclado = new Scanner(System.in);
		this.executando = true;
		this.minimoCordenadores =  10;
		this.porcentagemCordenadores = 0.05;
		this.opcoesDispachante = new RequestOptions(ResponseMode.GET_ALL, 5000);
	}
 
	
	protected void inicializar() {
		try{
			//controi o canal
			this.channel=new JChannel("../meu.xml");		//usa a configuracao default;
			this.channel.setReceiver(this);	//quem ira lidar com as mensagens recebidas
			this.channel.setDiscardOwnMessages(true);
			this.dispachante = new MessageDispatcher(this.channel, null, null, this);			
			this.channel.connect("GLOBAL");
			
			//inicializa os servicos de lock
			this.lockservice = new LockService(this.channel);
			this.usuariosLock = lockservice.getLock("usuarios");
			this.itensLock = lockservice.getLock("itens");
			this.salasLock = lockservice.getLock("salas");
		}
		catch(Exception e){
			System.out.println("Falha ao estabelecer conexao com o sistema");
		}
	}
	
	public void checarMembership(){
		View new_view = this.channel.getView();
		System.out.println(new_view);
		if(new_view.getMembers().size()==1){
			//this.usuario = new Usuario(this.ADMINISTRADOR, "-1");
			this.restaurar();
			
		}
		else {
			this.atualizarCoordenadores();
			if(this.channel.getAddress().equals(this.coordenadores.size()-1)){
				try {
					this.channel.getState(null, 10000);
				} catch (Exception e) {}
			}
        }
	}
	
	protected void atualizarCoordenadores(){
		System.out.println(this.channel);
		View new_view = this.channel.getView();
		//atualiza os coordenadores
		if(new_view.getMembers().size()<=this.minimoCordenadores){
			this.coordenadores = new_view.getMembers();
		}
		else{
			int x = Math.max(this.minimoCordenadores, (int)(this.porcentagemCordenadores*new_view.getMembers().size()));
			this.coordenadores = new_view.getMembers().subList(0, x);
		}
	}
	
	public void logar() {
		String msg;
		String cpf;
		String senha;
		
		while(this.usuario==null) {
			System.out.print("Digite seu CPF: ");
			cpf = this.teclado.nextLine();
			System.out.print("Digite a senha: ");
			senha = this.teclado.nextLine();

			//confirma dados
			this.usuariosLock.lock();
			try{
				//notifica cluster
				msg = "..login:"+cpf+";"+senha;
				//List<String> resp = this.notificarCluster(new Message(null, null, msg));
				List<String> resp = this.broadcastCluster(msg);
				System.out.println("[DEB.]:: respostas login: "+resp);
				
				//se a varios membros respondem logado e nenhum !logado, eh porque o usuario existe e se autenticou com sucesso 
				if(resp.contains("logado") && !resp.contains("!logado")){
					//System.out.println("Atualizando sistema\n");
					this.usuario = new Usuario(cpf, senha);
					this.atualizar();
					//System.out.println(this.sistema.usuariosToString());
					System.out.println("[DEB.]: Sucesso login\n\n");
				}
				//ninguem responde nada, eh porque ele eh o primeiro
				else if(!resp.contains("logado") && !resp.contains("!logado")){
					this.sistema.criarUsuario(msg);
					this.usuario = new Usuario(cpf, senha);
				}
				//se alguem responde !logado, eh porque o usuario existe mas nao se autenticou com sucesso
				else{
					System.out.println("N��o foi poss��vel realizar login!\nTente Novamente...\n");
					//System.out.println(this.notificarCluster(new Message(null, null, "..usuarios")));
					System.out.println(this.broadcastCluster("..usuarios"));
				}
			}catch(Exception e ){
				System.out.println("[DEB.]: Erro durante login");
			}
			finally{
				this.usuariosLock.unlock();
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
		synchronized(this.sistema) {
			this.sistema = (SistemaNucleo)Util.objectFromStream(new DataInputStream(input));
		}
	}
	
	public List<String> multicastCluster(String msg) throws Exception {
		System.out.println("Notificando Cluster");
		List<Address> cluster = new ArrayList<>();
		int i=0;
		
		this.atualizarCoordenadores();
		while(i<5 && i<this.coordenadores.size()){
			cluster.add(this.coordenadores.get(new Random().nextInt(this.coordenadores.size())));
			i++;
		}
		
		//Message msgm =
        //RspList<String> rsp_list = this.dispachante.castMessage(null, new Message(null, null, msg), this.opcoesDispachante);
		RspList<String> rsp_list = this.dispachante.castMessage(this.coordenadores, new Message(null, null, msg), this.opcoesDispachante);
        this.dispachante.stop();
        System.out.println(rsp_list.getResults());
        //System.out.println(rsp_list.getResults());
        //System.out.println();
        return rsp_list.getResults();
    }
	
	public List<String> broadcastCluster(String msg) throws Exception {
		System.out.println("deb. Notificando Cluster");
		this.atualizarCoordenadores();
		RspList<String> rsp_list = this.dispachante.castMessage(this.coordenadores, new Message(null, null, msg), this.opcoesDispachante);
        this.dispachante.stop();
        System.out.println(rsp_list.getResults());
        return rsp_list.getResults();
    }	
	
	public void atualizar(){
		try {
			//for(Object obj: this.notificarCluster(new Message(null, null, "..atualizar"))){
			for(Object obj: this.broadcastCluster("..atualizar")){
				System.out.println(obj);
				if(obj!=null){
					System.out.println("Atualizando sistema");
					System.out.println(obj);
					this.sistema = (SistemaNucleo)obj;
					System.out.println("2bb");
					System.out.println(this.sistema.usuariosToString());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Object handle(Message msgrcvd) throws Exception {
		//System.out.println("deb. handle");
		synchronized(sistema){
			String msg = msgrcvd.getObject().toString();
			System.out.println(msg);
			
			
			if(msg.startsWith("..login:")){
				if(this.usuario!=null){
					String login = this.sistema.loginUsuario(msg);
					
					if(login.equals("!cadastrado")){
						login = this.sistema.criarUsuario(msg);
					}
					
					return login;
					//System.out.println("deb. ffsfad");
				}
				else{
					return "";
				}
			}
			else if(msg.startsWith(".novo item:")){
				return this.sistema.criarNovoItem(msg);
			}
		    else if(msg.startsWith(".nova sala:")){
		    	this.sistema.criarNovaSala(msg);
		    	//this.sistema.historico(msg);
		    	return "+1 Sala";
		    }
		    else if(msg.startsWith("[LEILOEIRO]: atualizar")){
		    	this.channel.getState(null, 5000);
		    }
			if(msg.startsWith("Vencedor:")){
				//System.out.println(msg);
				//this.sistema.historico(msg);
				//falta desmembrar a string 
				int x = 0;
				String vencedor = "";
				this.sistema.FinalizarLeilao(x, vencedor);
				return "finalizado";
			}
		    else {
		    	System.out.println("[DEBBUG]: "+msg);
		    }
			
			if(msg.startsWith("..atualizar")){
				if(this.usuario!=null){
					System.out.println("coordenadores respondendo a atualiza����o");					
					return this.sistema;
					
				}
				
				return null;
			}
			else if(msg.startsWith("..usuarios")){
				//return this.sistema.getUsuarios().toString();
				return this.sistema.usuariosToString();
			}
			
			if(msg.startsWith("Sala finalizada")){
				msg = msg.split(":")[1];
				//int salaId = Integer.parseInt(msg.split(";")[0]);
				System.out.println("Leilão finalizado:: item:"+msg.split(";")[1]+", vencedor: "+msg.split(";")[2]+", lance: "+msg.split(";")[3]);
				/*
				try{
					this.sistema.salas.remove(salaId-1);
				}
				finally{}*/
				
				return("Notificado fim leilão");
			}
		}
        
		return "indefinido";
	}
	
	
	

	public void restaurar(){
		ObjectInputStream iis = null;
		FileInputStream fin = null;
		try{
		    fin = new FileInputStream("./.temp");
		    iis = new ObjectInputStream(fin);
		    //
		    
		    this.sistema = (SistemaNucleo)iis.readObject();
		    iis.close();
		} catch (Exception ex) {
		    ex.printStackTrace();
		} finally {
		    if(iis != null){
		        
		    } 
		}
	}
	
	public void salvar(){
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
		    fout = new FileOutputStream("./.temp", true);
		    oos = new ObjectOutputStream(fout);
		    oos.writeObject(this.sistema);
		    oos.close();
		} catch (Exception ex) {
		    ex.printStackTrace();
		} finally {
		    if(oos != null){} 
		}
	}
}
