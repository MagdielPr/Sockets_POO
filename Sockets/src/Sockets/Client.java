package Sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final String serverEndereco = "127.0.0.1"; 
    private Socket clientSocket;
    private Scanner scanner;

    public Client() {
        scanner = new Scanner(System.in);
    }

    public void start() throws IOException {
        clientSocket = new Socket(serverEndereco, 4000);
        System.out.println("Cliente conectado ao servidor: " + serverEndereco + " // Porta:" + 4000);
        requisicaoLoop();
    }

    private void requisicaoLoop() {
        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String rqs;
            String resposta;
            do {
                System.out.println("Digite uma mensagem (ou 'sair' para finalizar):");
                rqs = scanner.nextLine();
                out.println(rqs);
                resposta = in.readLine();
                System.out.println("Resposta do servidor: " + resposta);
            } while (!rqs.equalsIgnoreCase("sair"));
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o servidor: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar socket do cliente: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.start();
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o cliente: " + e.getMessage());
        }
        System.out.println("Cliente finalizado!");
    }
}
