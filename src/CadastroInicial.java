import crypto.CertUtils;
import crypto.KeyUtils;
import crypto.SenhaUtils;
import crypto.TokenUtils;
import db.ChaveiroDAO;
import db.Database;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.Usuario;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class CadastroInicial {

    public static void main(String[] args) {
        try {
            Database.init();

            if (UsuarioDAO.contarUsuarios() > 0) {
                System.out.println("Já existe usuário cadastrado.");
                return;
            }

            Scanner sc = new Scanner(System.in);

            System.out.println("Cadastro inicial do administrador");

            System.out.print("Caminho do certificado: ");
            String caminhoCert = sc.nextLine().trim();

            System.out.print("Caminho da chave privada: ");
            String caminhoChave = sc.nextLine().trim();

            System.out.print("Frase secreta da chave privada: ");
            String frase = sc.nextLine();

            System.out.print("Senha pessoal numerica: ");
            String senha = sc.nextLine();

            System.out.print("Confirmacao da senha pessoal: ");
            String confirmacao = sc.nextLine();

            if (!senha.equals(confirmacao)) {
                System.out.println("As senhas nao conferem.");
                return;
            }

            if (!SenhaUtils.senhaPessoalValida(senha)) {
                RegistroDAO.registrar(6003, null, null);
                System.out.println("Senha pessoal invalida.");
                return;
            }

            X509Certificate certificado;
            try {
                certificado = CertUtils.carregarCertificado(caminhoCert);
            } catch (Exception e) {
                RegistroDAO.registrar(6004, null, null);
                System.out.println("Certificado invalido.");
                return;
            }

            PrivateKey chavePrivada;
            try {
                chavePrivada = KeyUtils.carregarChavePrivada(caminhoChave, frase);
            } catch (Exception e) {
                RegistroDAO.registrar(6006, null, null);
                System.out.println("Nao foi possivel abrir a chave privada.");
                return;
            }

            boolean chaveOk = KeyUtils.validarChaveComCertificado(
                    chavePrivada,
                    certificado.getPublicKey(),
                    9216
            );

            if (!chaveOk) {
                RegistroDAO.registrar(6007, null, null);
                System.out.println("A chave privada nao combina com o certificado.");
                return;
            }

            String email = CertUtils.obterEmail(certificado);
            String nome = CertUtils.obterNome(certificado);

            if (email == null || email.trim().isEmpty()) {
                System.out.println("Nao foi possivel extrair o e-mail do certificado.");
                return;
            }

            System.out.println();
            System.out.println("Dados do certificado:");
            System.out.println(CertUtils.resumoCertificado(certificado));

            System.out.print("Confirmar cadastro? (s/n): ");
            String opcao = sc.nextLine().trim();

            if (!opcao.equalsIgnoreCase("s")) {
                RegistroDAO.registrar(6009, null, null);
                System.out.println("Cadastro cancelado.");
                return;
            }

            RegistroDAO.registrar(6008, null, null);

            String hashSenha = SenhaUtils.gerarHashBcrypt(senha);
            String segredoBase32 = TokenUtils.gerarSegredoBase32();
            byte[] segredoCriptografado = TokenUtils.criptografarSegredo(segredoBase32, senha);

            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setGid(1);
            usuario.setSenhaHash(hashSenha);
            usuario.setTokenKey(segredoCriptografado);

            int uid = UsuarioDAO.inserir(usuario);

            String certPem = CertUtils.lerCertificadoComoTexto(caminhoCert);
            byte[] chaveCriptografada = Files.readAllBytes(Paths.get(caminhoChave));

            int kid = ChaveiroDAO.inserir(uid, certPem, chaveCriptografada);
            UsuarioDAO.atualizarKid(uid, kid);

            System.out.println();
            System.out.println("Administrador cadastrado com sucesso.");
            System.out.println("E-mail: " + email);
            System.out.println("Nome: " + nome);
            System.out.println();
            System.out.println("Cadastre este segredo no Google Authenticator:");
            System.out.println(segredoBase32);
            System.out.println();
            System.out.println("URI opcional:");
            System.out.println("otpauth://totp/Cofre%20Digital:" + email + "?secret=" + segredoBase32);

        } catch (Exception e) {
            System.out.println("Erro no cadastro inicial.");
            e.printStackTrace();
        }
    }
}