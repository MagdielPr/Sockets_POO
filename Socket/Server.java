package Socket;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    public static final int PORT = 4000;
    private ServerSocket serverSocket;
    private Connection dbConnection;
    private volatile boolean running = true;
    private ArrayList<String[]> cache;

    public Server() {
        this.cache = new ArrayList<>();
    }

    public void start() throws IOException {
        System.out.println("Servidor iniciando na porta: " + PORT);
        serverSocket = new ServerSocket(PORT);
        this.dbConnection = Banco.ConnectionDatabase.conectar();

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
            fecharConexaoBanco();
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
                    System.out.println("Recebido do cliente: " + inputLine);

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
                    System.out.println("Dados obtidos do banco de dados.");
                } else {
                    return "Aluno não encontrado.";
                }
            } else {
                System.out.println("Dados obtidos do cache.");
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
        String sql = "SELECT * FROM tabledatabase WHERE ID = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("ID"),
                        rs.getString("Nome"),
                        rs.getString("Email"),
                        rs.getString("DataNascimento")
                    };
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar aluno no banco: " + e.getMessage());
        }
        return null;
    }

    private void fecharConexaoBanco() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar conexão com o banco: " + e.getMessage());
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