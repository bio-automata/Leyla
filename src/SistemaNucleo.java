import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SistemaNucleo implements Serializable{
	private static final long serialVersionUID = 2895936811947863654L;
	//String user_name;
	List<Item> itens;
	List<Item> itensEmLeilao;
	List<Sala> salas;
	List<String> historico;
	
	
	public SistemaNucleo() {
		//this.user_name = System.getProperty("user.name", "n/a");
		this.itens = new ArrayList<>();
		this.salas = new ArrayList<>();
		this.itensEmLeilao = new ArrayList<>();
		this.historico = new ArrayList<>();
	}
	
	public int gerarIdItem(){
		return this.itens.size()+this.itensEmLeilao.size();
	}
	
	public int gerarIdSala(){
		return this.salas.size();
	}
	
	public void criarNovoItem(String line){
			line = line.split(":")[1];
			int id = Integer.parseInt(line.split(";")[0]);
			String item = line.split(";")[1];
			String dono = line.split(";")[2];
			long data = Long.parseLong(line.split(";")[3]);
			this.itens.add(new Item(id, item, dono, data));
			System.out.println("Item adicionado: "+item);
	}
	
	public void criarNovaSala(String line){
			line = line.split(":")[1];
			//System.out.println(line.split(";")[1]);
			int id = this.salas.size()+1;
			int itemId = Integer.parseInt(line.split(";")[1]);
			Item item = this.itens.remove(itemId-1);
			double lanceInicial = Integer.parseInt(line.split(";")[2]);
			double valorLance = Integer.parseInt(line.split(";")[3]);
			
			Sala sala = new Sala("sala "+id, item, lanceInicial, valorLance);
			this.salas.add(sala);
			
			//this.itensEmLeilao.add(item);
			System.out.println("Leilão do item "+item.getNome()+" iniciado na sala "+itemId);
	}
	
	public void entrarSala(String line){
		int id = Integer.parseInt(line.split(":")[1]);
		Sala sala = this.salas.get(id-1);
		
		new SalaInterface(sala);
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
}
