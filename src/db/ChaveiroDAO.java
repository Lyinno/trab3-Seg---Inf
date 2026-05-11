package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChaveiroDAO {

    public static int inserir(int uid, String certificadoPem, byte[] chavePrivadaCriptografada) throws Exception {
        String sql = "INSERT INTO Chaveiro (uid, certificado_pem, chave_privada) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, uid);
            ps.setString(2, certificadoPem);
            ps.setBytes(3, chavePrivadaCriptografada);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public static String buscarCertificadoPorKid(int kid) throws Exception {
        String sql = "SELECT certificado_pem FROM Chaveiro WHERE kid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("certificado_pem");
                }
            }
        }

        return null;
    }

    public static byte[] buscarChavePrivadaPorKid(int kid) throws Exception {
        String sql = "SELECT chave_privada FROM Chaveiro WHERE kid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("chave_privada");
                }
            }
        }

        return null;
    }
}