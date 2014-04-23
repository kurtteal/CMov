package com.example.bomberman.csclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.AsyncTask;

import android.util.Log;

public class ClientAsyncTask extends AsyncTask<String, Void, PrintWriter> {
	private static PrintWriter out;
	private static Socket clientSocket;
    private static BufferedReader in;
    
	int serverPort = 4442;
	String serverAddr = "10.0.2.2";
	String mode;
	
	ClientService service; 
  
	public ClientAsyncTask(String mode) {
        super();
        this.mode = mode;
    }
	
	public ClientAsyncTask(String mode, ClientService service) {
        super();
        this.mode = mode;
        this.service = service;
    }
	
//	public ClientAsyncTask(String mode, PrintWriter out, Activity activity) {
//        super();
//        this.mode = mode;
//        out = out;
//        this.activity = activity;
//    }
	
	//Metodo que o execute() vai correr: se for com ordem de connect,
	//vai abrir o socket com o servidor e devolve o printWriter ah activity
	//com o qual consegue enviar updates ao servidor. Se for send, envia o
	//update para o servidor no printWriter que lhe eh passado pelo construtor.
	//(Atençao: AsyncTasks so podem correr 1 vez, têm de ser instanciadas sempre
	//que correm, portanto eh preciso enviar o printWriter para a activity no connect
	//para que esta possa usá-lo quando faz send!)
	protected PrintWriter doInBackground(String... strings) {
		
		// validate input parameters
		if (strings.length <= 0) {
			return null;
		}
		//  connect to the server
		if(mode.equals("connect")){
			// connect to the server and send the message
			try {
				clientSocket = new Socket(serverAddr, serverPort);
			
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));

				Runnable r = new ClientListener(in, service);
				new Thread(r).start();
				Log.d("CONNECT", "IM IN CONNECT");
			} catch (UnknownHostException e) {
				System.out.println("Error connecting to server - UnknownHostException");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Error connecting to server - IOException");
				e.printStackTrace();
			}
			return out;
		}
		//Send the message
		else if(mode.equals("send")){
			out.println(strings[0]);
			return out;
		}
		else {
			return null;
		}
	}

	protected void onPostExecute(Long result) {
		return;
	}
	
	public void closeSocket(){
		try {
			out.close();
			in.close();
			clientSocket.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	//Listener de cada cliente, que fica ah escuta no bufferedReader
	//associado ao socket do servidor, por actualizacoes e envia-as para o servico
	public class ClientListener implements Runnable {

		BufferedReader in;
		ClientService service;

		public ClientListener(BufferedReader bfr, ClientService service) {
			in = bfr;
			this.service = service;
		}

		public void run() {
			while (true) {
				try {
					String message = in.readLine();

					//Se o servidor fechou o socket, o cliente faz o mesmo
					if(message == null){
						closeSocket();
						service.socketWasClosed();
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