public class Sala {
    private Item item;
    private String nome;

    public Sala(String nome, Item item){
        this.nome = nome;
        this.item = item;
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
    };
}
