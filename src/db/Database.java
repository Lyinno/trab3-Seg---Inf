package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:data/cofre.db";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void init() throws Exception {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS Grupos (" +
                    "gid INTEGER PRIMARY KEY, " +
                    "nome TEXT NOT NULL UNIQUE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Usuarios (" +
                    "uid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "gid INTEGER NOT NULL, " +
                    "senha_hash TEXT NOT NULL, " +
                    "token_key BLOB NOT NULL, " +
                    "kid INTEGER, " +
                    "total_acessos INTEGER DEFAULT 0, " +
                    "total_consultas INTEGER DEFAULT 0, " +
                    "erros_senha INTEGER DEFAULT 0, " +
                    "erros_token INTEGER DEFAULT 0, " +
                    "bloqueado_ate INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (gid) REFERENCES Grupos(gid)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Chaveiro (" +
                    "kid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uid INTEGER NOT NULL, " +
                    "certificado_pem TEXT NOT NULL, " +
                    "chave_privada BLOB NOT NULL, " +
                    "FOREIGN KEY (uid) REFERENCES Usuarios(uid)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Mensagens (" +
                    "mid INTEGER PRIMARY KEY, " +
                    "texto TEXT NOT NULL" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Registros (" +
                    "rid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "data_hora TEXT NOT NULL, " +
                    "mid INTEGER NOT NULL, " +
                    "uid INTEGER, " +
                    "arquivo TEXT, " +
                    "FOREIGN KEY (mid) REFERENCES Mensagens(mid), " +
                    "FOREIGN KEY (uid) REFERENCES Usuarios(uid)" +
                    ")");

            stmt.execute("INSERT OR IGNORE INTO Grupos (gid, nome) VALUES (1, 'Administrador')");
            stmt.execute("INSERT OR IGNORE INTO Grupos (gid, nome) VALUES (2, 'Usuário')");
        }

        MensagemDAO.inserirMensagensPadrao();
    }
}