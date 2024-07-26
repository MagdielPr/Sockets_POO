package Socket;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    public static final int PORT = 4000;
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private ArrayList<String[]> cache;

    public Server() {
        this.cache = new ArrayList<>();
    }

    public void start() throws IOException {
        System.out.println("Servidor iniciando na porta: " + PORT);
        serverSocket = new ServerSocket(PORT);

        // Thread para aceitar conexões de clientes
        new Thread(this::clientConnectionLoop).start();

        // Thread para ler entrada do console
        new Thread(this::consoleInputLoop).start();
    }

    private void clientConnectionLoop() {
        int count = 0;
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                count++;
                System.out.println("Cliente " + count + " - " + clientSocket.getRemoteSocketAddress() + " conectou com sucesso!");

                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                if (running) {
                    System.out.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }

    private void consoleInputLoop() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if ("sair".equalsIgnoreCase(input)) {
                    running = false;
                    closeServer();
                }
            }
        }
    }

    private void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("\nServidor finalizado");
        } catch (IOException e) {
            System.out.println("Erro ao fechar o servidor: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null && running) {
                    String resposta = processarId(inputLine);
                    out.println(resposta);

                    if ("sair".equalsIgnoreCase(inputLine)) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro ao comunicar com o cliente: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar socket do cliente: " + e.getMessage());
                }
            }
        }
    }

    private String processarId(String idStr) {
        try {
            int id = Integer.parseInt(idStr);
            String[] dadosAluno = buscarNaCache(id);
            if (dadosAluno == null) {
                dadosAluno = buscarNoBanco(id);
                if (dadosAluno != null) {
                    cache.add(dadosAluno);
                    System.out.println("ID Consultado: " + id + " | Dados obtidos do Banco de Dados.");
                } else {
                    System.out.println("ID Consultado: " + id + " | Aluno não encontrado no banco.");
                    return "Aluno não encontrado.";
                }
            } else {
                System.out.println("ID Consultado: " + id + " | Dados obtidos do Cache.");
            }
            return String.join(", ", dadosAluno);
        } catch (NumberFormatException e) {
            return "ID inválido.";
        }
    }

    private String[] buscarNaCache(int id) {
        for (String[] aluno : cache) {
            if (Integer.parseInt(aluno[0]) == id) {
                return aluno;
            }
        }
        return null;
    }

    private String[] buscarNoBanco(int id) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println(id);
            String resposta = in.readLine();
            if ("Aluno não encontrado".equals(resposta)) {
                return null;
            } else {
                return resposta.split(", ");
            }
        } catch (IOException e) {
            System.out.println("Erro ao se comunicar com o serviço de banco de dados: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
            System.out.println("Servidor está ativo!");
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor! Erro: " + e.getMessage());
        }
    }
}
