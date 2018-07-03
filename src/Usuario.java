import java.io.Serializable;
import java.util.List;

public class Usuario implements Serializable{
    private static final long serialVersionUID = 5828282449691544280L;
	private String cpf;
    private String senha;
    private List<String> premios;
    

    public Usuario(String cpf, String senha){
        this.cpf = cpf;
        this.senha = senha;
    }

	public String getCpf() {
		return cpf;
	}


	public void setCpf(String cpf) {
		this.cpf = cpf;
	}


	public String getSenha() {
		return senha;
	}


	public void setSenha(String senha) {
		this.senha = senha;
	}

	public List<String> getPremios() {
		return premios;
	}

	public void setPremios(List<String> premios) {
		this.premios = premios;
	}
	
	
}
