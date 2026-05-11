package db;

import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDAO {

    public static int contarUsuarios() throws Exception {
        String sql = "SELECT COUNT(*) FROM Usuarios";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static Usuario buscarPorEmail(String email) throws Exception {
        String sql = "SELECT * FROM Usuarios WHERE email = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return montarUsuario(rs);
            }
        }
    }

    public static Usuario buscarPorUid(int uid) throws Exception {
        String sql = "SELECT * FROM Usuarios WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uid);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return montarUsuario(rs);
            }
        }
    }

    public static int inserir(Usuario usuario) throws Exception {
        String sql =
                "INSERT INTO Usuarios " +
                "(nome, email, gid, senha_hash, token_key, kid, total_acessos, total_consultas, erros_senha, erros_token, bloqueado_ate) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, 0, 0)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setInt(3, usuario.getGid());
            ps.setString(4, usuario.getSenhaHash());
            ps.setBytes(5, usuario.getTokenKey());

            if (usuario.getKid() == 0) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, usuario.getKid());
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public static void atualizarKid(int uid, int kid) throws Exception {
        String sql = "UPDATE Usuarios SET kid = ? WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kid);
            ps.setInt(2, uid);
            ps.executeUpdate();
        }
    }

    public static void incrementarAcesso(int uid) throws Exception {
        String sql = "UPDATE Usuarios SET total_acessos = total_acessos + 1 WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uid);
            ps.executeUpdate();
        }
    }

    public static void incrementarConsulta(int uid) throws Exception {
        String sql = "UPDATE Usuarios SET total_consultas = total_consultas + 1 WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uid);
            ps.executeUpdate();
        }
    }

    public static void limparErrosSenha(int uid) throws Exception {
        String sql = "UPDATE Usuarios SET erros_senha = 0 WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uid);
            ps.executeUpdate();
        }
    }

    public static int incrementarErroSenha(int uid) throws Exception {
        String sql = "UPDATE Usuarios SET erros_senha = erros_senha + 1 WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uid);
            ps.executeUpdate();
        }

        Usuario usuario = buscarPorUid(uid);
        return usuario.getErrosSenha();
    }

    public static void limparErrosToken(int uid) throws Exception {
        String sql = "UPDATE Usuarios SET erros_token = 0 WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uid);
            ps.executeUpdate();
        }
    }

    public static int incrementarErroToken(int uid) throws Exception {
        String sql = "UPDATE Usuarios SET erros_token = erros_token + 1 WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uid);
            ps.executeUpdate();
        }

        Usuario usuario = buscarPorUid(uid);
        return usuario.getErrosToken();
    }

    public static void bloquearPorDoisMinutos(int uid) throws Exception {
        long ate = System.currentTimeMillis() + 2 * 60 * 1000;

        String sql =
                "UPDATE Usuarios SET bloqueado_ate = ?, erros_senha = 0, erros_token = 0 WHERE uid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ate);
            ps.setInt(2, uid);
            ps.executeUpdate();
        }
    }

    public static void desbloquearSeExpirou(Usuario usuario) throws Exception {
        if (usuario.getBloqueadoAte() > 0 && usuario.getBloqueadoAte() <= System.currentTimeMillis()) {
            String sql = "UPDATE Usuarios SET bloqueado_ate = 0 WHERE uid = ?";

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, usuario.getUid());
                ps.executeUpdate();
            }
        }
    }

    private static Usuario montarUsuario(ResultSet rs) throws Exception {
        Usuario usuario = new Usuario();

        usuario.setUid(rs.getInt("uid"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setGid(rs.getInt("gid"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setTokenKey(rs.getBytes("token_key"));
        usuario.setKid(rs.getInt("kid"));
        usuario.setTotalAcessos(rs.getInt("total_acessos"));
        usuario.setTotalConsultas(rs.getInt("total_consultas"));
        usuario.setErrosSenha(rs.getInt("erros_senha"));
        usuario.setErrosToken(rs.getInt("erros_token"));
        usuario.setBloqueadoAte(rs.getLong("bloqueado_ate"));

        return usuario;
    }

    public static Usuario buscarPrimeiroAdministrador() throws Exception {
        String sql = "SELECT * FROM Usuarios WHERE gid = 1 ORDER BY uid ASC LIMIT 1";

        try (Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            return montarUsuario(rs);
        }
    }
}