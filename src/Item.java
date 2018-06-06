import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hera
 */
public class Item  implements Serializable{
	
	private static final long serialVersionUID = 5948447758362484465L;
	private int id;
    private String nome;
    private String dono;
    private long data;
    private boolean emUso = false;
    
    public Item(String nome, int id) {
        this.nome = nome;
        this.id = id;
    }
    
    public Item(int id, String nome, String dono, long data) {
    	this.id = id;
    	this.nome = nome;
    	this.dono = dono;
    	this.data = data;
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getDono() {
        return dono;
    }

    public void setDono(String dono) {
        this.dono = dono;
    }

    
    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    

    public boolean isEmUso() {
        return emUso;
    }
    
    public void setEmUso(boolean emUso) {
        this.emUso = emUso;
    }
    
    public void bloqueia() {
        this.emUso = true;
    }
    
    public void desbloqueia() {
        this.emUso = false;
    }
}
