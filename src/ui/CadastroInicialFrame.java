package ui;

import crypto.CertUtils;
import crypto.KeyUtils;
import crypto.SenhaUtils;
import crypto.TokenUtils;
import db.ChaveiroDAO;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.Usuario;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class CadastroInicialFrame extends JFrame {
    private JTextField campoCertificado;
    private JTextField campoChave;
    private JPasswordField campoFrase;
    private JPasswordField campoSenha;
    private JPasswordField campoConfirmacao;

    public CadastroInicialFrame() {
        setTitle("Cofre Digital - Cadastro Inicial do Administrador");
        setSize(700, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        montarTela();

        try {
            RegistroDAO.registrar(1005, null, null);
        } catch (Exception e) {
            // segue
        }
    }

    private void montarTela() {
        JLabel titulo = new JLabel("Primeira execução: cadastre o administrador do sistema.");

        JPanel formulario = new JPanel(new GridLayout(5, 3, 5, 5));

        campoCertificado = new JTextField();
        campoChave = new JTextField();
        campoFrase = new JPasswordField();
        campoSenha = new JPasswordField();
        campoConfirmacao = new JPasswordField();

        JButton buscarCert = new JButton("Buscar");
        JButton buscarChave = new JButton("Buscar");

        formulario.add(new JLabel("Caminho do certificado digital:"));
        formulario.add(campoCertificado);
        formulario.add(buscarCert);

        formulario.add(new JLabel("Caminho da chave privada:"));
        formulario.add(campoChave);
        formulario.add(buscarChave);

        formulario.add(new JLabel("Frase secreta da chave privada:"));
        formulario.add(campoFrase);
        formulario.add(new JLabel(""));

        formulario.add(new JLabel("Senha pessoal:"));
        formulario.add(campoSenha);
        formulario.add(new JLabel(""));

        formulario.add(new JLabel("Confirmação da senha pessoal:"));
        formulario.add(campoConfirmacao);
        formulario.add(new JLabel(""));

        JPanel botoes = new JPanel();

        JButton cadastrar = new JButton("Cadastrar Administrador");
        JButton sair = new JButton("Sair");

        botoes.add(cadastrar);
        botoes.add(sair);

        add(titulo, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        buscarCert.addActionListener(e -> escolherArquivo(campoCertificado));
        buscarChave.addActionListener(e -> escolherArquivo(campoChave));
        cadastrar.addActionListener(e -> cadastrarAdministrador());
        sair.addActionListener(e -> System.exit(0));
    }

    private void escolherArquivo(JTextField campo) {
        JFileChooser chooser = new JFileChooser();

        int opcao = chooser.showOpenDialog(this);

        if (opcao == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();
            campo.setText(arquivo.getAbsolutePath());
        }
    }

    private void cadastrarAdministrador() {
        try {
            String caminhoCert = campoCertificado.getText().trim();
            String caminhoChave = campoChave.getText().trim();
            String frase = new String(campoFrase.getPassword());
            String senha = new String(campoSenha.getPassword());
            String confirmacao = new String(campoConfirmacao.getPassword());

            if (caminhoCert.isEmpty() || caminhoChave.isEmpty() || frase.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha certificado, chave privada e frase secreta.");
                return;
            }

            if (!senha.equals(confirmacao)) {
                JOptionPane.showMessageDialog(this, "As senhas pessoais não conferem.");
                return;
            }

            if (!SenhaUtils.senhaPessoalValida(senha)) {
                RegistroDAO.registrar(6003, null, null);
                JOptionPane.showMessageDialog(this, "Senha pessoal inválida. Use 8, 9 ou 10 dígitos numéricos.");
                return;
            }

            X509Certificate certificado;

            try {
                certificado = CertUtils.carregarCertificado(caminhoCert);
            } catch (Exception e) {
                RegistroDAO.registrar(6004, null, null);
                JOptionPane.showMessageDialog(this, "Certificado digital inválido.");
                return;
            }

            PrivateKey chavePrivada;

            try {
                chavePrivada = KeyUtils.carregarChavePrivada(caminhoChave, frase);
            } catch (Exception e) {
                RegistroDAO.registrar(6006, null, null);
                JOptionPane.showMessageDialog(this, "Não foi possível abrir a chave privada.");
                return;
            }

            boolean chaveOk = KeyUtils.validarChaveComCertificado(
                    chavePrivada,
                    certificado.getPublicKey(),
                    9216
            );

            if (!chaveOk) {
                RegistroDAO.registrar(6007, null, null);
                JOptionPane.showMessageDialog(this, "A chave privada não combina com o certificado.");
                return;
            }

            String email = CertUtils.obterEmail(certificado);
            String nome = CertUtils.obterNome(certificado);

            if (email == null || email.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não foi possível extrair o e-mail do certificado.");
                return;
            }

            String resumo = CertUtils.resumoCertificado(certificado);

            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    resumo + "\nConfirmar cadastro do administrador?",
                    "Confirmação do Certificado",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmar != JOptionPane.YES_OPTION) {
                RegistroDAO.registrar(6009, null, null);
                return;
            }

            RegistroDAO.registrar(6008, null, null);

            String hashSenha = SenhaUtils.gerarHashBcrypt(senha);
            String segredoBase32 = TokenUtils.gerarSegredoBase32();
            byte[] segredoCriptografado = TokenUtils.criptografarSegredo(segredoBase32, senha);

            Usuario admin = new Usuario();
            admin.setNome(nome);
            admin.setEmail(email);
            admin.setGid(1);
            admin.setSenhaHash(hashSenha);
            admin.setTokenKey(segredoCriptografado);

            int uid = UsuarioDAO.inserir(admin);

            String certPem = CertUtils.lerCertificadoComoTexto(caminhoCert);
            byte[] chaveCriptografada = Files.readAllBytes(new File(caminhoChave).toPath());

            int kid = ChaveiroDAO.inserir(uid, certPem, chaveCriptografada);
            UsuarioDAO.atualizarKid(uid, kid);

            JOptionPane.showMessageDialog(
                    this,
                    "Administrador cadastrado com sucesso.\n\n" +
                            "Nome: " + nome + "\n" +
                            "E-mail: " + email + "\n\n" +
                            "Cadastre este segredo no Google Authenticator:\n" +
                            segredoBase32 + "\n\n" +
                            "URI opcional:\n" +
                            "otpauth://totp/Cofre%20Digital:" + email + "?secret=" + segredoBase32
            );

            JOptionPane.showMessageDialog(this, "Reinicie o sistema para iniciar a operação normal.");
            System.exit(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro no cadastro inicial: " + e.getMessage());
            e.printStackTrace();
        }
    }
}