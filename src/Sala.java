import java.io.Serializable;

public class Sala implements Serializable{
    private static final long serialVersionUID = 5828282449691544280L;
	private String nome;
    private Item item;
    private double lanceInicial;
    private double valorLance;	//mínimo valor de um lance
    private double lanceAtual;

    public Sala(String nome, Item item, double lanceInicial, double valorLance){
        this.nome = nome;
        this.item = item;
        this.lanceInicial = lanceInicial;
        this.valorLance = valorLance;
        this.lanceAtual = this.lanceInicial; 
    }

    public Item getItem() {
        return item;
    }

    public String getNome() {
        return nome;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

	public double getLanceInicial() {
		return lanceInicial;
	}

	public void setLanceInicial(double lanceInicial) {
		this.lanceInicial = lanceInicial;
	}

	public double getValorLance() {
		return valorLance;
	}

	public void setValorLance(double incrementoMinimo) {
		this.valorLance = incrementoMinimo;
	}

	public double getLanceAtual() {
		return lanceAtual;
	}

	public void setLanceAtual(double lanceAtual) {
		this.lanceAtual = lanceAtual;
	};
	
	public void novoLance(double lance){
		this.lanceAtual = this.lanceAtual+lance; 
	}
}
