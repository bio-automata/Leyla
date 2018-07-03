

public class MonitorDeSistema implements Runnable{
	private SistemaComunicacao sistemaComunicacao;
	private long timer;
	
	public MonitorDeSistema(SistemaComunicacao sistemaComunicacao) {
		this.sistemaComunicacao = sistemaComunicacao;
		this.timer = (long) 0;
	}
	
	public long getTimer(){
		return this.timer;
	}
	
	@Override
	public void run() {
		boolean rodando = true;
		try {
			while(rodando){
				//System.out.println("Monitor Running saving");
				if(this.timer==0){
					this.timer = System.currentTimeMillis();
				}
				if(System.currentTimeMillis()>this.timer+5000){
					//System.out.println("Sistema Salvo");
					this.sistemaComunicacao.salvar();
				}
				
				Thread.sleep(20000);
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
