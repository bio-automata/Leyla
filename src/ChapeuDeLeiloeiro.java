import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

public class ChapeuDeLeiloeiro implements Runnable{
	private SalaInterface salaInterface;
	private Long timer;
	private int contador;
	
	public ChapeuDeLeiloeiro(SalaInterface salaInterface) {
		this.salaInterface = salaInterface;
		this.timer = (long) 0;
		this.contador = 0;
	}

	
	public void restart(){
		this.timer = System.currentTimeMillis();
		this.contador = 0;
	}
	
	public Long getTimer(){
		return this.timer;
	}
	
	@Override
	public void run() {
		try {
			if(this.timer==0){
				this.timer = System.currentTimeMillis();
			}
			if(System.currentTimeMillis()>this.timer+10000){
				String msg = "[LEILOEIRO]: ";
				this.timer = System.currentTimeMillis();
				if(this.contador>3){
					msg = msg+"finalizar";
				}
				else{
					this.contador++;
					msg = msg+"Quem dá mais? Dou-lhe "+this.contador;
				}
				this.salaInterface.notificarCluster(msg);
				Thread.sleep(1000);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
