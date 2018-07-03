import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class SistemaNucleo implements Serializable{
	private static final long serialVersionUID = 6840821732808468010L;
	//String user_name;
	private int contadorGlobalDeItens;
	List<Usuario> usuarios;
	List<Item> itens;
	List<Sala> salas;
	List<String> leiloesResultados;
	
	public SistemaNucleo() {
		this.contadorGlobalDeItens = 0;
		//this.user_name = System.getProperty("user.name", "n/a");
		this.usuarios = new ArrayList<>();
		this.itens = new ArrayList<>();
		this.salas = new ArrayList<>();
		//this.historico = new ArrayList<>(); 
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

	public String loginUsuario(String msg) {
		//msg = msg.split(":")[1];
		System.out.println(msg);
		String cpf = msg.split(";")[0];
		String senha = msg.split(";")[1];
		//System.out.println(cpf);
		//System.out.println(senha);
		
		
		Usuario usuario = new Usuario(cpf,senha);
		for(Usuario auxuser : this.usuarios){
			System.out.println(auxuser.getCpf()+" "+auxuser.getSenha());
			if(auxuser.getCpf().equals(cpf)){
				if(auxuser.getSenha().equals(senha)){
					return "logado";
				}
				else{
					return "!logado";   //não está logado
				}
			}
		}
		
		return "!cadastrado";
	}
	
	public String criarUsuario(String msg) {
		//msg = msg.split(":")[1];
		System.out.println(msg);
		String cpf = msg.split(";")[0];
		String senha = msg.split(";")[1];
		//System.out.println(cpf);
		//System.out.println(senha);
		Usuario usuario = new Usuario(cpf,senha);
		this.usuarios.add(usuario);
		return "logado";
	}
	
	
	public String criarNovoItem(String line){
			line = line.split(":")[1];
			int id = Integer.parseInt(line.split(";")[0]);
			String item = line.split(";")[1];
			String dono = line.split(";")[2];
			long data = Long.parseLong(line.split(";")[3]);
			
			this.contadorGlobalDeItens = id;
			this.itens.add(new Item(id, item, dono, data));
			System.out.println("Item adicionado: "+item+" (ID:"+this.contadorGlobalDeItens+")");
			return "Item adicionado: "+item+" (ID:"+this.contadorGlobalDeItens+")";
	}
	
	public String criarNovaSala(String line){
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
			return "Leilão do item "+item.getNome()+" iniciado na sala "+itemId;
	}
	
	public String entrarSala(String line){
		line = line.split(":")[1];
		int id = Integer.parseInt(line.split(";")[0]);
		String cpf = line.split(";")[1];
		Sala sala = this.salas.get(id-1);
		
		if(!sala.isLeilaoFinalizado()){
			SalaInterface si = new SalaInterface(cpf, sala);
			si.start();
			sala.setLeilaoFinalizado(true);
		}
		else{
			//this.salas.set(id-1, null);
			System.out.println("[GLOBAL BOT]: Sala fechada...");
			//this.salas.remove(id-1);
		}
		
		
		return ""; //si.vencedor();
	}
	
	public void listarUsuarios(){
		int i = 1;
		for(Usuario usuario: this.usuarios){
			System.out.println("Usuario "+i+": "+usuario.getCpf());
			i++;
		}
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
	
	public String FinalizarLeilao(int idSala, String vencedor){
		//retira a sala da lista de salas
		//int idSala = 0;
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
		System.out.println(".entrar na sala:\"id\"");
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
	
	
	public String usuariosToString(){
		String s = "";
		
		for(Usuario u : this.usuarios){
			s = s+u.getCpf()+","+u.getSenha()+";";
		}
		
		return s;
	}
}
