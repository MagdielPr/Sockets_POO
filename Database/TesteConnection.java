package Database;

import java.sql.Connection;
import java.sql.DriverManager;

public class TesteConnection {
//CLASSE CRIADA SEGUINDO VIDEO, USAR PARA TESTAR CONEXÃO, FUNCIONA SEM PRECISAR ATIVAR O XAMP '-'
	public static void main(String[] args) {
		Connection con = null;
		try {
			con = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlconnection","root","1234");
			if(con!= null) {
				System.out.println("Conectado guri!");
			}
		}catch (Exception e) {
			System.out.println("Erro");
		}
	}

}