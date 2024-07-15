package Sockets;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    public static final int PORT = 4000;
    private ServerSocket serverSocket;
    private List<String> comandos;
    private Connection dbConnection;
    private volatile boolean running = true;

    public Server() {
        this.comandos = new ArrayList<>();
    }

    public void start() throws IOException {
        System.out.println("Servidor iniciando na porta: " + PORT);
        serverSocket = new ServerSocket(PORT);
        this.dbConnection = Database.ConnectionDatabase.conectar();
        
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
            
            // Certifique-se de que a conexão está aberta antes de usá-la
            if (dbConnection == null || dbConnection.isClosed()) {
                dbConnection = Database.ConnectionDatabase.conectar();
            }
            
            System.out.println("\nComandos no banco de dados:");
            mostrarComandosDoBanco();
            
            System.out.println("\nComandos no array:");
            for (String comando : comandos) {
                System.out.println(comando);
            }
            
            // Feche a conexão depois de usar
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
            fecharConexaoBanco();
            System.out.println("\nServidor finalizado");
        } catch (IOException | SQLException e) {
            System.out.println("Erro ao fechar o servidor: " + e.getMessage());
        }
    }

    private void mostrarComandosDoBanco() {
    	String sql = "SELECT ID, Comando FROM tabledatabase ORDER BY ID;";
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getString("Comando"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar comandos do banco: " + e.getMessage());
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
                    
                    String resposta = processarComando(inputLine);
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

    private String processarComando(String comando) {
    	try {
            if (dbConnection == null || dbConnection.isClosed()) {
                dbConnection = Database.ConnectionDatabase.conectar();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao reabrir conexão com o banco: " + e.getMessage());
            return "Erro ao processar o comando.";
        }
    	
        if (comandos.contains(comando)) {
            return "Comando já existe no cache do servidor.";
        }

        if (verificarComandoNoBanco(comando)) {
            comandos.add(comando);
            return "Comando encontrado no banco de dados.";
        }

        if (inserirComandoNoBanco(comando)) {
            comandos.add(comando);
            return "Novo comando inserido no banco de dados.";
        }

        return "Erro ao processar o comando.";
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
    
    private boolean verificarComandoNoBanco(String comando) {
        String sql = "SELECT * FROM tabledatabase WHERE Comando = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, comando);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar comando no banco: " + e.getMessage());
            return false;
        }
    }

    private boolean inserirComandoNoBanco(String comando) {
        String sql = "INSERT INTO tabledatabase (Comando) VALUES (?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, comando);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir comando no banco: " + e.getMessage());
            return false;
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
