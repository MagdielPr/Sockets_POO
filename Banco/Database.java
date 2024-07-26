package Banco;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Database {
    public static final int PORT = 5000;
    private ServerSocket serverSocket;
    private Connection dbConnection;
    private volatile boolean running = true;

    public void start() {
        try {
            System.out.println("Serviço de banco de dados iniciando na porta: " + PORT);
            serverSocket = new ServerSocket(PORT);
            dbConnection = ConnectionDatabase.conectar();

            while (running) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o serviço de banco de dados: " + e.getMessage());
        } finally {
            fecharConexaoBanco();
        }
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
                while ((inputLine = in.readLine()) != null) {
                    int id = Integer.parseInt(inputLine);
                    String[] dadosAluno = buscarNoBanco(id);
                    if (dadosAluno != null) {
                        out.println(String.join(", ", dadosAluno));
                    } else {
                        out.println("Aluno não encontrado");
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
    }

    public static void main(String[] args) {
        Database databaseService = new Database();
        databaseService.start();
    }
}
