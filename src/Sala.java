import java.io.Serializable;

public class Sala implements Serializable{
    private static final long serialVersionUID = 5828282449691544280L;
	private String nome;
    private Item item;
    private double lanceInicial;
    private double valorLance;	//mínimo valor de um lance
    private double lanceAtual;
    private boolean leilaoFinalizado;
    private String donoDoUltimoLance;

    public Sala(String nome, Item item, double lanceInicial, double valorLance){
        this.nome = nome;
        this.item = item;
        this.lanceInicial = lanceInicial;
        this.valorLance = valorLance;
        this.lanceAtual = this.lanceInicial;
        this.donoDoUltimoLance = "";
        this.leilaoFinalizado = false;
    }
    
    public Sala(String nome, Item item){
        this.nome = nome;
        this.item = item;
        this.lanceInicial = 0;
        this.valorLance = 0;
        this.lanceAtual = 0;
        this.leilaoFinalizado = false;
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
	
	public void novoLance(double lance, String dono){
		this.lanceAtual = lance; 
		this.donoDoUltimoLance = dono;
	}

	public String getDonoDoUltimoLance() {
		return donoDoUltimoLance;
	}

	public void setDonoDoUltimoLance(String donoDoUltimoLance) {
		this.donoDoUltimoLance = donoDoUltimoLance;
	}
	
	public boolean isLeilaoFinalizado() {
		return leilaoFinalizado;
	}

	public void setLeilaoFinalizado(boolean leilaoFinalizado) {
		this.leilaoFinalizado = leilaoFinalizado;
	}


	
	
	
}
