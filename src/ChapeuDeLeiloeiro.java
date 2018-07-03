

public class ChapeuDeLeiloeiro implements Runnable{
	private SalaComunicacao salaInterface;
	private long timer;
	private int contador;
	
	public ChapeuDeLeiloeiro(SalaComunicacao salaInterface) {
		this.salaInterface = salaInterface;
		this.timer = (long) 0;
		this.contador = 0;
	}

	
	public void restart(){
		this.timer = System.currentTimeMillis();
		this.contador = 0;
	}
	
	public long getTimer(){
		return this.timer;
	}
	
	@Override
	public void run() {
		boolean rodando = true;
		try {
			while(rodando){
				//System.out.println("Running A");
				if(this.timer==0){
					//System.out.println("Running B");
					this.timer = System.currentTimeMillis();
				}
				if(System.currentTimeMillis()>this.timer+5000 && this.salaInterface.temMaisAlguemNaSala() && !this.salaInterface.isLanceNulo()){
					//System.out.println("Running C");
					String msg = "[LEILOEIRO]: ";
					this.timer = System.currentTimeMillis();
					if(this.contador>2){
						//msg = msg+"finalizar";
						this.salaInterface.broadcastCluster("..finalizar");
						rodando = false;
						this.salaInterface.executando = false;
						this.salaInterface.desconectar();
					}
					else{
						this.contador++;
						msg = msg+"Quem dá mais? Dou-lhe "+this.contador;
					}
					
					System.out.println(msg);
					this.salaInterface.broadcastCluster(msg);
				}
				
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			//this.salaInterface.desconectar();
		}
	}
}
