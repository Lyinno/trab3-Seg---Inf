import crypto.CertUtils;
import crypto.KeyUtils;
import db.ChaveiroDAO;
import db.Database;
import db.UsuarioDAO;
import model.Usuario;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LogViewApp extends JFrame {
    private JTextField campoChave;
    private JPasswordField campoFrase;
    private JTable tabela;
    private DefaultTableModel modelo;

    public LogViewApp() {
        setTitle("LogView - Auditoria do Cofre Digital");
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        montarTela();
    }

    private void montarTela() {
        JPanel topo = new JPanel(new GridLayout(2, 3, 5, 5));

        campoChave = new JTextField();
        campoFrase = new JPasswordField();

        JButton buscar = new JButton("Buscar");
        JButton autenticar = new JButton("Autenticar e Carregar Logs");

        topo.add(new JLabel("Chave privada do administrador:"));
        topo.add(campoChave);
        topo.add(buscar);

        topo.add(new JLabel("Frase secreta:"));
        topo.add(campoFrase);
        topo.add(autenticar);

        modelo = new DefaultTableModel();
        modelo.addColumn("Data/Hora");
        modelo.addColumn("Código");
        modelo.addColumn("Mensagem");
        modelo.addColumn("Usuário");
        modelo.addColumn("Arquivo");

        tabela = new JTable(modelo);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        buscar.addActionListener(e -> escolherChave());
        autenticar.addActionListener(e -> autenticarECarregar());
    }

    private void escolherChave() {
        JFileChooser chooser = new JFileChooser();

        int opcao = chooser.showOpenDialog(this);

        if (opcao == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();
            campoChave.setText(arquivo.getAbsolutePath());
        }
    }

    private void autenticarECarregar() {
        try {
            Database.init();

            String caminhoChave = campoChave.getText().trim();
            String frase = new String(campoFrase.getPassword());

            if (caminhoChave.isEmpty() || frase.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe a chave privada e a frase secreta.");
                return;
            }

            Usuario admin = UsuarioDAO.buscarPrimeiroAdministrador();

            if (admin == null) {
                JOptionPane.showMessageDialog(this, "Administrador não encontrado.");
                return;
            }

            String certPem = ChaveiroDAO.buscarCertificadoPorKid(admin.getKid());
            X509Certificate certificadoAdmin = CertUtils.carregarCertificadoDeTexto(certPem);

            PrivateKey chavePrivada;

            try {
                chavePrivada = KeyUtils.carregarChavePrivada(caminhoChave, frase);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir chave privada.");
                return;
            }

            boolean ok = KeyUtils.validarChaveComCertificado(
                    chavePrivada,
                    certificadoAdmin.getPublicKey(),
                    2048
            );

            if (!ok) {
                JOptionPane.showMessageDialog(this, "Falha na validação da chave privada.");
                return;
            }

            carregarLogs();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro no LogView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarLogs() throws Exception {
        modelo.setRowCount(0);

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

                modelo.addRow(new Object[]{
                        dataHora,
                        mid,
                        texto,
                        email == null ? "" : email,
                        arquivo == null ? "" : arquivo
                });
            }
        }

        JOptionPane.showMessageDialog(this, "Logs carregados com sucesso.");
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LogViewApp().setVisible(true);
            }
        });
    }
}