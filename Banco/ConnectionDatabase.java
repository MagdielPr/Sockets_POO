package Banco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionDatabase {
    private static final String HOST = "localhost";
    private static final int PORTA = 3306;
    private static final String NOME_BANCO = "databasesocket";
    private static final String USER = "root";
    private static final String PASSWORD = "1234"; 

    public static Connection conectar() {
        Connection conexao = null;
        try {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", 
                HOST, PORTA, NOME_BANCO);
            conexao = DriverManager.getConnection(url, USER, PASSWORD);
            System.out.println("Conectado ao banco de dados: " + NOME_BANCO + " com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
        return conexao;
    }

    public static void fecharConexao(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
                System.out.println("Conexão fechada com sucesso!");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }

    public static void execute(String sql) {
        try (Connection conexao = conectar();
             Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("SQL executado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao executar a instrução SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getHost() {
        return HOST;
    }

    public static int getPorta() {
        return PORTA;
    }

    public static String getNomeBanco() {
        return NOME_BANCO;
    }

    public static String getUser() {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
