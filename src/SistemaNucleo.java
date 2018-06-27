import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SistemaNucleo implements Serializable{
	private static final long serialVersionUID = 6840821732808468010L;
	//String user_name;
	private int contadorGlobalDeItens;
	List<Usuario> usuarios;
	List<Item> itens;
	//List<Item> itensEmLeilao;
	List<Sala> salas;
	List<String> leiloesResultados;
	List<String> historico;
	 
	
	
	
	public SistemaNucleo() {
		this.contadorGlobalDeItens = 0;
		//this.user_name = System.getProperty("user.name", "n/a");
		this.itens = new ArrayList<>();
		this.salas = new ArrayList<>();
		this.historico = new ArrayList<>(); 
	}
	
	public int gerarIdItem(){
		return this.contadorGlobalDeItens+1;
	}
	
	public int gerarIdSala(){
		return this.salas.size()+1;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Usuario getUsuario(int i) {
		return usuarios.get(i);
	}
	
	public List<Usuario> getUsuarios() {
		return usuarios;
	}

	public void criarnovoUsuario(Usuario usuario) {
		this.usuarios.add(usuario);
	}
	
	public void criarNovoItem(String line){
			line = line.split(":")[1];
			int id = Integer.parseInt(line.split(";")[0]);
			String item = line.split(";")[1];
			String dono = line.split(";")[2];
			long data = Long.parseLong(line.split(";")[3]);
			
			this.contadorGlobalDeItens = id;
			this.itens.add(new Item(id, item, dono, data));
			System.out.println("Item adicionado: "+item+" (ID:"+this.contadorGlobalDeItens+")");
	}
	
	public void criarNovaSala(String line){
			line = line.split(":")[1];
			//System.out.println(line.split(";")[1]);
			int id = this.salas.size()+1;
			int itemId = Integer.parseInt(line.split(";")[1]);
			Item item = this.itens.remove(itemId-1);
			//double lanceInicial = Integer.parseInt(line.split(";")[2]);
			//double valorLance = Integer.parseInt(line.split(";")[3]);
			
			Sala sala = new Sala("sala "+id, item);
			this.salas.add(sala);
			
			//this.itensEmLeilao.add(item);
			System.out.println("Leilão do item "+item.getNome()+" iniciado na sala "+itemId);
	}
	
	public String entrarSala(String line){
		int id = Integer.parseInt(line.split(":")[1]);
		Sala sala = this.salas.get(id-1);
		SalaInterface si = new SalaInterface(sala);
		
		si.start();
		
		return si.vencedor();
	}
	
	public void listarItens(){
		int i = 1;
		for(Item item: this.itens){
			System.out.println("Item "+i+": "+item.getNome());
			i++;
		}
	}
	
	public void listarSalas(){
		int i = 1;
		for(Sala sala: this.salas){
			System.out.println("Sala "+i+": "+sala.getItem().getNome());
			i++;
		}
	}
	
	public String FinalizarLeilao(int idItem, String vencedor){
		//retira a sala da lista de salas
		int idSala = 0;
		this.salas.remove(idSala-1);
		
		//transmite mensagem para o cluster indicando quem ganhou o leilão
		
		return vencedor;
	}
	
	public void listarLeiloesFinalizados(){
		int i = 1;
		for(Sala sala: this.salas){
			System.out.println("Sala "+i+": "+sala.getItem().getNome());
			i++;
		}
	}
	
	public void historico(String msg){
		if(!this.historico.contains(msg)){
			this.historico.add(msg);
		}
	}
	
	public void creditos(){
		System.out.println("Sistema Leyla\n Casa de Leilão versão 0.0.1");
		System.out.println("\nColaboradores:");
		System.out.println("	Rafaela Martins");
		System.out.println("	Rodrigo Cezar Silveira");
		System.out.println("	Saulo Carvalho");
	}
		
	
	public void help(){
		System.out.println("Para realizar uma operação digite uma das seguintes opções:");
		System.out.println(".novo item:\"nome do item\"");
		System.out.println(".nova sala:\"id do item\"");
		System.out.println(".entrar na sala:\"id|nome da sala\"");
		System.out.println(".listar itens");
		System.out.println(".listar salas");
		System.out.println(".creditos\n\n");
	}
	
	
	public void setItens(List<Item> itens){
		this.itens = itens;
	}
	
	public List<Item> getItens(){
		return this.itens;
	}

	public int getContadorGlobalDeItens() {
		return contadorGlobalDeItens;
	}

	public void setContadorGlobalDeItens(int contadorGlobalDeItens) {
		this.contadorGlobalDeItens = contadorGlobalDeItens;
	}

	public List<Sala> getSalas() {
		return salas;
	}

	public void setSalas(List<Sala> salas) {
		this.salas = salas;
	}

	public List<String> getHistorico() {
		return historico;
	}

	public void setHistorico(List<String> historico) {
		this.historico = historico;
	}
}
