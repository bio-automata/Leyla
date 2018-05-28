import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SistemaNucleo implements Serializable{
	private static final long serialVersionUID = 2895936811947863654L;
	//String user_name;
	List<Item> itens;
	List<Item> itensEmLeilao;
	List<String> historico;
	
	
	public SistemaNucleo() {
		//this.user_name = System.getProperty("user.name", "n/a");
		this.itens = new ArrayList<>();
		itensEmLeilao = new ArrayList<>();
		historico = new ArrayList<>();
	}
	
	public int gerarIdItem(){
		return this.itens.size()+this.itensEmLeilao.size();
	}
	
	public int gerarIdSala(){
		return this.itensEmLeilao.size();
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
			System.out.println(line.split(";")[1]);
			int id = Integer.parseInt(line.split(";")[1])-1;
			Item item = this.itens.remove(id);
			this.itensEmLeilao.add(item);
			System.out.println("Leilão do item "+item.getNome()+" iniciado na sala "+id);
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
		for(Item item: this.itensEmLeilao){
			System.out.println("Sala "+i+": "+item.getNome());
			i++;
		}
	}
	
	public void historico(String msg){
		if(!this.historico.contains(msg)){
			this.historico.add(msg);
		}
	}
	
	public void creditos(){
		System.out.println("Sistema Casa de Leilão versão 0.0.1");
		System.out.println("Colaboradores:");
		System.out.println("	Rafaela Martins");
		System.out.println("	Rodrigo Cezar");
		System.out.println("	Saulo Carvalho");
	}
		
	
	public void help(){
		System.out.println("Operação inválida!\n");
		System.out.println("Para realizar uma operação digite uma das seguintes opções:");
		System.out.println(".novo item:[nome do item]");
		System.out.println(".nova sala:[id do item]");
		System.out.println(".entrar na sala:[id|nome da sala]");
		System.out.println(".listar itens");
		System.out.println(".listar salas");
		System.out.println(".creditos\n\n");
	}
}
