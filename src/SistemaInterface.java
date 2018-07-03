import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jgroups.Message;


public class SistemaInterface extends SistemaComunicacao{
	protected MonitorDeSistema monitor;
	public SistemaInterface() {
		super();
	}
 	
	private void start(){
		this.inicializar();
		this.atualizarCoordenadores();
		this.checarMembership();
		this.monitor = new MonitorDeSistema(this);
		Thread tmonitor = new Thread(this.monitor);
		tmonitor.start();
		this.eventLoop();
	}
	
	
	private void eventLoop(){
		//this.channel.getState(null, 10000);
		if(this.usuario==null){
			this.logar();
			//this.usuario = new Usuario("ADMINISTRADOR", "1234");
		}
		
		
		System.out.println("[GLOBAL BOT]: Bem vindo ao Sitema de Leilões");
		System.out.println("[GLOBAL BOT]: Se você é novato no sistema digite o comando [help] para acessar a lista de comandos");
		while( executando ) {
			try {
				this.chamarMenu();
			}catch(Exception e) {
				System.out.println("Falha: sistema não pode completar uma ação");
			}
		}
	}
	
	public void chamarMenu() throws Exception{
		System.out.print("> ");
        line = teclado.nextLine().toLowerCase();
        
        //if(line.startsWith(".novo :")){
        if(line.startsWith(".novo item:")){
	    	String msg = "";
			String item = line.split(":")[1];
			String user = this.usuario.getCpf();
			long data = System.currentTimeMillis();
			//System.out.print("deb......3");
			
			itensLock.lock();
			try{
				int id = sistema.gerarIdItem();
				msg = ".novo item:"+id+";"+item+";"+user+";"+data;
				//this.notificarCluster(new Message(null, null, msg));
				this.broadcastCluster(msg);
				//this.coordenadores
			}
			finally{
				itensLock.unlock();
				
			}
			sistema.criarNovoItem(msg);
			//sistema.historico(msg);
		}
	    else if(line.startsWith(".nova sala:")){
	    	int id = 0;
	    	String msg = "";
	    	line = line.split(":")[1];
			String item = line.split(";")[0];
			String user = this.usuario.getCpf();
			long data = System.currentTimeMillis();
	    	
			salasLock.lock();
			try{
				id = sistema.gerarIdSala();
				msg = ".nova sala:"+id+";"+item+";"+user+";"+data;
				//this.notificarCluster(new Message(null, null, msg));
				this.broadcastCluster(msg);
			}
			finally{
				salasLock.unlock();
			}
			
			sistema.criarNovaSala(msg);
			
			if(id>0){
				this.channel.disconnect();
				msg = sistema.entrarSala(".entrar sala:"+id+";"+user);
		    	
				System.out.println(msg);
				this.broadcastCluster(msg);
			}
		}
	    else if(line.startsWith(".entrar sala:")){
	    	if(this.channel.getView().size()>1){
		    	sistema.entrarSala(line+";"+this.usuario.getCpf());
	    	}
	    	else{
	    		System.out.println("Você é o único usuário logado, precisa segurar o sistema");
	    	}
	    }
	    else if(line.startsWith(".listar usuarios")){
			this.atualizar();
			sistema.listarUsuarios();
		}
		else if(line.startsWith(".listar itens")){
			this.atualizar();
			sistema.listarItens();
		}
		else if(line.startsWith(".listar salas")){
			this.atualizar();
			sistema.listarSalas();
		}
		else if(line.startsWith(".creditos")){
			sistema.creditos();
		}
		else if(line.startsWith(".atualizar")){
			//this.channel.getState();
			this.atualizar();
		}
		else if(line.startsWith(".help")){
			this.sistema.help();
		}
		else if(line.startsWith("quit") || line.startsWith("exit") || line.startsWith("sair")){
	        this.executando = false;
	        this.channel.close();
	    }
		else if(line.startsWith(":")){
			String user = this.usuario.getCpf();
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
		
	public static void main(String[] args) {
		SistemaInterface sistema = new SistemaInterface();
		sistema.start();
		//sistema
	}
}
