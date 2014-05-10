package com.example.bomberman.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocket;
import android.os.AsyncTask;
import android.util.Log;

public class ClientAsyncTask extends AsyncTask<String, Void, PrintWriter> {
	
	private static PrintWriter out;
	private static Socket clientSocket;
	private static SimWifiP2pSocket clientWDSocket;
    private static BufferedReader in;
	private int serverPort = 10001;
	private ArrayList<String> addresses;
	private String mode;
	private NetworkService service;
	
	public ClientAsyncTask(String mode) {
        super();
        this.mode = mode;
    }
	
	public ClientAsyncTask(String mode, NetworkService service, ArrayList<String> addresses) {
        super();
        this.mode = mode;
        this.service = service;
        this.addresses = addresses;
    }
	
	//Metodo que o execute() vai correr: se for com ordem de connect,
	//vai abrir o socket com o servidor e devolve o printWriter ah activity
	//com o qual consegue enviar updates ao servidor. Se for send, envia o
	//update para o servidor no printWriter que lhe eh passado pelo construtor.
	//(Atencao: AsyncTasks so podem correr 1 vez, tem de ser instanciadas sempre
	//que correm, portanto eh preciso enviar o printWriter para a activity no connect
	//para que esta possa usa-lo quando faz send!)
	protected PrintWriter doInBackground(String... strings) {
		
		// validate input parameters
		if (strings.length <= 0) {
			return null;
		}
		
		if(mode.equals("connect")) {
			if(addresses != null) {
				for(String address : addresses) {
					Log.d("TRYING", "Trying " + "192.168.0.1");
					try {
						if(!service.usingWDSim()) {
							clientSocket = new Socket(address, serverPort);
							out = new PrintWriter(clientSocket.getOutputStream(), true);
							in = new BufferedReader(new InputStreamReader(
									clientSocket.getInputStream()));
						}
						else {
							clientWDSocket = new SimWifiP2pSocket("192.168.0.1", serverPort);
							out = new PrintWriter(clientWDSocket.getOutputStream(), true);
							in = new BufferedReader(new InputStreamReader(
									clientWDSocket.getInputStream()));
						}
						Runnable r = new ClientListener(in, service);
						new Thread(r).start();
						Log.d("Connected", "Connected to " + "192.168.0.1");
						break;
					} catch (UnknownHostException e) {
						System.out.println("Error connecting to server - UnknownHostException");
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("Error connecting to server - IOException");
						e.printStackTrace();
					}
				}
			}
			return out;
		}
		else if(mode.equals("send")) {
			out.println(strings[0]);
			return out;
		}
		else {
			return null;
		}
	}
	
	public void closeSocket() {
		try {
			out.close();
			in.close();
			if(!service.usingWDSim())
				clientSocket.close();
			else
				clientWDSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Listener de cada cliente, que fica ah escuta no bufferedReader
	// associado ao socket do servidor, por actualizacoes e envia-as para o servico
	public class ClientListener implements Runnable {
		BufferedReader in;
		NetworkService service;

		public ClientListener(BufferedReader bfr, NetworkService service) {
			this.in = bfr;
			this.service = service;
		}

		public void run() {
			while (true) {
				try {
					String message = in.readLine();
					if(message == null){
						closeSocket();
						service.closeConnection();
						break;
					}
					service.processMessage(message);
					Log.d("Client recebeu: ", message);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
