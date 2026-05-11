import crypto.CertUtils;
import crypto.KeyUtils;
import db.ChaveiroDAO;
import db.Database;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.Usuario;

import java.io.Console;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class LogView {

    public static void main(String[] args) {
        try {
            Database.init();

            if (args.length != 1) {
                System.out.println("Uso:");
                System.out.println("java LogView caminho/da/chave/privada.key");
                return;
            }

            String caminhoChave = args[0];

            Usuario admin = UsuarioDAO.buscarPrimeiroAdministrador();

            if (admin == null) {
                System.out.println("Administrador nao encontrado no banco.");
                return;
            }

            String certPem = ChaveiroDAO.buscarCertificadoPorKid(admin.getKid());

            if (certPem == null) {
                System.out.println("Certificado do administrador nao encontrado.");
                return;
            }

            X509Certificate certificadoAdmin = CertUtils.carregarCertificadoDeTexto(certPem);

            String fraseSecreta = lerFraseSecreta();

            PrivateKey chavePrivada;

            try {
                chavePrivada = KeyUtils.carregarChavePrivada(caminhoChave, fraseSecreta);
            } catch (Exception e) {
                System.out.println("Erro ao abrir chave privada.");
                return;
            }

            boolean ok = KeyUtils.validarChaveComCertificado(
                    chavePrivada,
                    certificadoAdmin.getPublicKey(),
                    2048
            );

            if (!ok) {
                System.out.println("Falha na validacao da chave privada.");
                return;
            }

            System.out.println();
            System.out.println("Autenticacao do administrador realizada com sucesso.");
            System.out.println();
            System.out.println("=== Registros do sistema ===");
            System.out.println();

            RegistroDAO.imprimirRegistros();

        } catch (Exception e) {
            System.out.println("Erro ao executar logView.");
            e.printStackTrace();
        }
    }

    private static String lerFraseSecreta() {
        Console console = System.console();

        if (console != null) {
            char[] senha = console.readPassword("Frase secreta da chave privada: ");
            return new String(senha);
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("Frase secreta da chave privada: ");
        return sc.nextLine();
    }
}