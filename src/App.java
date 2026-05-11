import crypto.CertUtils;
import crypto.KeyUtils;
import db.ChaveiroDAO;
import db.Database;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.SistemaContexto;
import model.Usuario;
import ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class App {
    public static void main(String[] args) {
        try {
            Database.init();
            RegistroDAO.registrar(1001, null, null);

            int totalUsuarios = UsuarioDAO.contarUsuarios();

            if (totalUsuarios == 0) {
                RegistroDAO.registrar(1005, null, null);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        new ui.CadastroInicialFrame().setVisible(true);
                    }
                });

                return;
            }

            RegistroDAO.registrar(1006, null, null);

            if (!validarAdministradorNaPartida()) {
                RegistroDAO.registrar(1002, null, null);
                JOptionPane.showMessageDialog(null, "Validação do administrador falhou. Sistema encerrado.");
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new LoginFrame().setVisible(true);
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao iniciar sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean validarAdministradorNaPartida() {
        try {
            Usuario admin = UsuarioDAO.buscarPrimeiroAdministrador();

            if (admin == null) {
                return false;
            }

            String frase = JOptionPane.showInputDialog(
                    null,
                    "Frase secreta da chave privada do administrador:"
            );

            if (frase == null || frase.isEmpty()) {
                return false;
            }

            String certPem = ChaveiroDAO.buscarCertificadoPorKid(admin.getKid());
            byte[] chaveBytes = ChaveiroDAO.buscarChavePrivadaPorKid(admin.getKid());

            X509Certificate certificado = CertUtils.carregarCertificadoDeTexto(certPem);
            PrivateKey chavePrivada = KeyUtils.carregarChavePrivadaDeBytes(chaveBytes, frase);

            boolean ok = KeyUtils.validarChaveComCertificado(
                    chavePrivada,
                    certificado.getPublicKey(),
                    9216
            );

            if (!ok) {
                return false;
            }

            SistemaContexto.setFraseSecretaAdmin(frase);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}