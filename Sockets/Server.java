package Sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static final int PORT = 4000;
	private ServerSocket serverSocket;
	
	public void start() throws IOException {
		System.out.println("Servidor iniciando na porta: " + PORT);
		serverSocket = new ServerSocket(PORT);
		clientConnectionLoop();
	}
	
	private void clientConnectionLoop() throws IOException {
		int count = 0;
		while(true) {
			Socket clientSocket = serverSocket.accept(); //fica aguardando o cliente conectar e quanto o cliente conectar ele cria um socket local para que o servidor possa se comunicar com o client por meio daquele socket local
			count++;
			System.out.println("Client "+count+" sadad- "+ clientSocket.getRemoteSocketAddress()+" conectou com sucesso!");
			
		}
		
	}
	public static void main(String[] args) {
		try {
			Server  server = new Server();
			server.start();
			System.out.println("O pai ta on!");
		} catch (IOException e) {
			System.out.println("Erro ao startar server! Erro: " + e.getMessage());
		}
		System.out.println("Server off");
	}
}
