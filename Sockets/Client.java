package Sockets;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import Sockets.Server;

public class Client {
	private final String serverEndereco = "127.0.0.1";
	private Socket clientSocket;
	private Scanner scanner;
	
	public Client() {
		scanner = new Scanner(System.in);
	}
	
	public void start() throws IOException {
		clientSocket = new Socket(serverEndereco, Server.PORT);
		clientSocket.getOutputStream();//para poder pegar a mensagem e enviar para server, video explicou melhor rever 
		System.out.println("Client conectado ao Server: "+serverEndereco+":"+Server.PORT);
		requisicaoLoop();
	}
	
	private void requisicaoLoop() {
		String rqs;
		do{
			System.out.println("Aguardando requisição!");
			rqs = scanner.nextLine();
		}while(!rqs.equalsIgnoreCase("sair"));
	}
	public static void main(String[] args) {
		try {
			Client client  = new Client();
			client.start();
		} catch (IOException e) {
			System.out.println("Erro ao inicial Cliente: " + e.getMessage());
		}
		System.out.println("Client finalizado!");
	}

}
