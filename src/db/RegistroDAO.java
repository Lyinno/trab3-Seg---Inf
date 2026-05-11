package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroDAO {

    public static void registrar(int mid, Integer uid, String arquivo) {
        String sql = "INSERT INTO Registros (data_hora, mid, uid, arquivo) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String agora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ps.setString(1, agora);
            ps.setInt(2, mid);

            if (uid == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, uid);
            }

            if (arquivo == null) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, arquivo);
            }

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Erro ao registrar log: " + e.getMessage());
        }
    }

    public static void imprimirRegistros() throws Exception {
        String sql =
                "SELECT r.data_hora, r.mid, m.texto, u.email, r.arquivo " +
                "FROM Registros r " +
                "JOIN Mensagens m ON r.mid = m.mid " +
                "LEFT JOIN Usuarios u ON r.uid = u.uid " +
                "ORDER BY r.data_hora ASC, r.rid ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String dataHora = rs.getString("data_hora");
                int mid = rs.getInt("mid");
                String texto = rs.getString("texto");
                String email = rs.getString("email");
                String arquivo = rs.getString("arquivo");

                if (email != null) {
                    texto = texto.replace("<login_name>", email);
                }

                if (arquivo != null) {
                    texto = texto.replace("<arq_name>", arquivo);
                }

                System.out.println(dataHora + " [" + mid + "] " + texto);
            }
        }
    }
}