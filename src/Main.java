import crypto.CertUtils;
import crypto.KeyUtils;
import db.ChaveiroDAO;
import db.Database;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.SistemaContexto;
import model.Usuario;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Database.init();

            RegistroDAO.registrar(1001, null, null);

            int totalUsuarios = UsuarioDAO.contarUsuarios();

            if (totalUsuarios == 0) {
                RegistroDAO.registrar(1005, null, null);
                System.out.println("Primeira execucao do sistema.");
                System.out.println("Execute CadastroInicial para cadastrar o administrador.");
                return;
            }

            RegistroDAO.registrar(1006, null, null);

            boolean adminOk = validarAdministradorNaPartida();

            if (!adminOk) {
                System.out.println("Validacao do administrador falhou. Sistema encerrado.");
                RegistroDAO.registrar(1002, null, null);
                return;
            }

        } catch (Exception e) {
            System.out.println("Erro ao iniciar o sistema.");
            e.printStackTrace();
        } finally {
            SistemaContexto.limpar();
        }
    }

    private static boolean validarAdministradorNaPartida() {
        try {
            Scanner sc = new Scanner(System.in);

            Usuario admin = UsuarioDAO.buscarPrimeiroAdministrador();

            if (admin == null) {
                System.out.println("Administrador nao encontrado.");
                return false;
            }

            String certPem = ChaveiroDAO.buscarCertificadoPorKid(admin.getKid());
            byte[] chaveBytes = ChaveiroDAO.buscarChavePrivadaPorKid(admin.getKid());

            if (certPem == null || chaveBytes == null) {
                System.out.println("Chave ou certificado do administrador nao encontrados.");
                return false;
            }

            X509Certificate certificado = CertUtils.carregarCertificadoDeTexto(certPem);

            System.out.print("Frase secreta da chave privada do administrador: ");
            String frase = sc.nextLine();

            PrivateKey chavePrivada;

            try {
                chavePrivada = KeyUtils.carregarChavePrivadaDeBytes(chaveBytes, frase);
            } catch (Exception e) {
                System.out.println("Nao foi possivel abrir a chave privada do administrador.");
                return false;
            }

            boolean ok = KeyUtils.validarChaveComCertificado(
                    chavePrivada,
                    certificado.getPublicKey(),
                    9216
            );

            if (!ok) {
                System.out.println("A chave privada do administrador nao combina com o certificado.");
                return false;
            }

            SistemaContexto.setFraseSecretaAdmin(frase);

            System.out.println("Administrador validado com sucesso.");
            return true;

        } catch (Exception e) {
            System.out.println("Erro ao validar administrador.");
            return false;
        }
    }
}