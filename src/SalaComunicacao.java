import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
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

public class SalaComunicacao extends ReceiverAdapter implements RequestHandler{
	//protected String ADMINISTRADOR = "ADMINISTRADOR"; 
	protected SistemaNucleo sistema;
	//private String user_name = System.getProperty("user.name", "n/a");
	
	//informacoes relativas ao usuario e sala
	protected Sala sala;
	protected String cpfUsuario;
	//protected String cpfUltimoLance;
	//protected double lance;
	protected Scanner teclado = new Scanner(System.in);
	protected String line;
	protected boolean executando;
	
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
	protected ChapeuDeLeiloeiro leiloeiro;
	
	public SalaComunicacao(String usuario, Sala sala) {
		this.sistema = new SistemaNucleo();
		//this.user_name = System.getProperty("user.name", "n/a");
		this.cpfUsuario = usuario;
		this.sala = sala;
		this.teclado = new Scanner(System.in);
		this.executando = true;
		this.minimoCordenadores =  10;
		this.porcentagemCordenadores = 0.05;
		
		this.opcoesDispachante = new RequestOptions(ResponseMode.GET_ALL, 5000);
	}
 
	
	protected void inicializar() {
		try{
			//controi o canal
			System.out.println("deb. init A");
			this.channel=new JChannel("../meu.xml");		//usa a configuracao default;
			System.out.println("deb. init B");
			this.channel.setReceiver(this);	//quem ira lidar com as mensagens recebidas
			System.out.println("deb. init B");
			this.channel.setDiscardOwnMessages(true);
			
			System.out.println("deb. init B");
			//System.out.println("deb.: BF");
			this.dispachante = new MessageDispatcher(this.channel, null, null, this);			
			//System.out.println("entrou C");
			System.out.println("deb. init C");
			this.channel.connect(this.sala.getNome());
			
			System.out.println("deb. init C2");
			//inicializa os servicos de lock
			this.lockservice = new LockService(this.channel);
			this.usuariosLock = lockservice.getLock("usuarios");
			this.itensLock = lockservice.getLock("itens");
			this.salasLock = lockservice.getLock("salas");
			
			System.out.println("deb. sala init D");
			
			
			//this.channel.close();
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
		}
		else {
			this.atualizarCoordenadores();
			if(this.channel.getAddress().equals(this.coordenadores.size()-1)){
				try {
					this.channel.getState(null, 10000);
				} catch (Exception e) {}
			}
        }
		
		System.out.println("deb. checou membros");
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
		
		System.out.println("Atualizou coordenadores");
	}
	
	
	
	public void logar() {
		try{
			List<String> resp = this.broadcastCluster("..entrar sala");
			System.out.println("[DEB.]:: respostas login: "+resp);
			
			//se a varios membros respondem logado e nenhum !logado, eh porque o usuario existe e se autenticou com sucesso 
			if(resp.contains("sala fechada")){
				//System.out.println("Atualizando sistema\n");
				this.atualizar();
				//System.out.println(this.sistema.usuariosToString());
				System.out.println("[DEB.]: Sucesso sala fechada\n\n");
			}
			/*
			else if(!resp.contains("sala aberta") && !resp.contains("sala fechada")){
				System.out.println("Sou o leiloeiro!");
				this.leiloeiro = new ChapeuDeLeiloeiro(this);
				Thread thread = new Thread(this.leiloeiro);
				thread.start();
			}*/
			else if(resp.size()==0){
				System.out.println("Sou o leiloeiro!");
				this.leiloeiro = new ChapeuDeLeiloeiro(this);
				Thread thread = new Thread(this.leiloeiro);
				thread.start();
			}
		}catch(Exception e ){
			System.out.println("[DEB.]: Erro durante entrar sala");
		}
		finally{
			System.out.println("[DEB.]: Sucesso em entra na sala\n\n");
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
	
	public List<String> multicastCluster2(String msg) throws Exception {
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
		System.out.println("Notificando Cluster");
		
		this.atualizarCoordenadores();
		//Message msgm =
        //RspList<String> rsp_list = this.dispachante.castMessage(null, new Message(null, null, msg), this.opcoesDispachante);
		RspList<String> rsp_list = this.dispachante.castMessage(this.coordenadores, new Message(null, null, msg), this.opcoesDispachante);
        this.dispachante.stop();
        System.out.println(rsp_list.getResults());
        //System.out.println(rsp_list.getResults());
        //System.out.println();
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
		synchronized(this.sala){
			try{
				String msg = msgrcvd.getObject().toString();
				System.out.println("deb. sala msg handle"+msg);
				if(msg.startsWith("..entrar sala")){
					if(this.executando){
						return "sala aberta";
					}
					else{
						return "sala fechada";
					}
				}
				else if(msg.startsWith("..finalizar")){
					System.out.println("deb. finalizando");
					this.executando = false;
					this.sala.setLeilaoFinalizado(true);
					
					//this.desconectar();
					this.executando = false;
					//this.desconectar();
					
					return "finalizado";
				}
				else if(msg.startsWith("[LEILOEIRO]:")){
					System.out.println(msg);
				}
				else{
					msg = msg.split(":")[1];
					double lance = Double.parseDouble(msg.split(";")[0]);
					String cpfLance = msg.split(";")[1];
					System.out.println("deb. lance status: "+lance+" "+cpfLance);
					
					if(lance>this.sala.getLanceAtual()){
						System.out.println("[Árbitro da sala]: Usuário "+cpfLance+" deu um lance de "+lance);
						this.sala.novoLance(lance, cpfLance);
						
						//zera os contadores do leiloeiro
						if(this.leiloeiro!=null){
							this.leiloeiro.restart();
						}
						
						return "valido";
					}
					else{
						return "invalido";
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
			System.out.println("Vencedor: "+this.sala.getDonoDoUltimoLance()+", lance: "+this.sala.getLanceAtual()+", sala: "+this.sala.getNome()+", item: "+this.sala.getItem().getNome());
			this.channel.disconnect();
			this.channel.connect("GLOBAL");
			this.broadcastCluster("Sala finalizada:"+this.sala.getItem().getNome()+";"+this.sala.getDonoDoUltimoLance()+";"+this.sala.getLanceAtual());
		} catch (Exception e) {
			System.out.println("Falha ao anunciar vencedor");
			e.printStackTrace();
		}
	}
}
