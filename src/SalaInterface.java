import java.util.List;


public class SalaInterface extends SalaComunicacao{
	public SalaInterface(String usuario, Sala sala) {
		super(usuario, sala);
	}
 	
	public void start(){
		this.inicializar();
		this.atualizarCoordenadores();
		this.checarMembership();
		this.logar();
		this.eventLoop();
	}
	
	
	private void eventLoop(){
		//this.channel.getState(null, 10000);
		//if(this.usuario==null){
		System.out.println("deb. no loop a");
		System.out.println(this.sala);
		//System.out.println(this.sala.getNome()+" ("+this.sala.getItem()+")\nMembros: "+this.channel.getView()+"\nLance Atual: "+this.sala.getLanceAtual());
		System.out.println("deb. no loop b");
		System.out.print("> ");
        
		while(this.executando) {
			System.out.println("deb. está no eventloop");
			try {
				this.chamarMenu();
			}catch(Exception e) {
				System.out.println("Falha: sala não pode completar uma ação");
			}
		}
		
		/*
		if(this.fechada) {
			try {
				List<String> resp = this.broadcastCluster("..finalizar");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		
		//this.desconectar();
	}
	
	public void chamarMenu() throws Exception{
		System.out.println(this.sala.getNome()+" ("+this.sala.getItem()+")\nMembros: "+this.channel.getViewAsString()+"\nLance Atual: "+this.sala.getLanceAtual());
		System.out.print("> ");
        line = teclado.nextLine().toLowerCase();
        //System.out.println("Digitou: "+line);
        //this.channel.send(new Message(null, null, line));
        
	    if(line.startsWith("quit") || line.startsWith("exit") || line.startsWith("sair")){
	        this.executando = false;
	    }
		else{
			double lance = Double.parseDouble(this.line);
			//this.leiloeiro.restart();
			String msg = ".lance:"+lance+";"+this.cpfUsuario;
			List<String> resp = this.broadcastCluster(msg);
			
			if(resp.contains("invalido")){
				System.out.println("[Árbitro da sala]: Lance inválido!\nDigite um valor maior do que o lance atual ");
			}
			else{
				//zera os contadores do leiloeiro
				if(this.leiloeiro!=null){
					this.leiloeiro.restart();
				}
				this.sala.novoLance(lance, this.cpfUsuario);
			}
		}
    }

	public String vencedor(){
		return "Vencedor: "+this.sala.getDonoDoUltimoLance()+", item: "+this.sala.getItem().getNome()+", lance: "+this.sala.getLanceAtual();
	}
}
