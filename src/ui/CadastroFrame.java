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
import javax.swing.JComboBox;
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
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class CadastroFrame extends JFrame {
    private Usuario usuarioLogado;

    private JTextField campoCertificado;
    private JTextField campoChave;
    private JPasswordField campoFrase;
    private JComboBox<String> campoGrupo;
    private JPasswordField campoSenha;
    private JPasswordField campoConfirmacao;

    public CadastroFrame(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        setTitle("Cofre Digital - Cadastro de Usuário");
        setSize(700, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        montarTela();

        try {
            RegistroDAO.registrar(6001, usuarioLogado.getUid(), null);
        } catch (Exception e) {
            // segue sem travar a tela
        }
    }

    private void montarTela() {
        JPanel cabecalho = new JPanel(new GridLayout(3, 1));
        cabecalho.add(new JLabel("Login: " + usuarioLogado.getEmail()));
        cabecalho.add(new JLabel("Grupo: " + usuarioLogado.getGrupoNome()));
        cabecalho.add(new JLabel("Nome: " + usuarioLogado.getNome()));

        JPanel formulario = new JPanel(new GridLayout(6, 3, 5, 5));

        campoCertificado = new JTextField();
        campoChave = new JTextField();
        campoFrase = new JPasswordField();
        campoGrupo = new JComboBox<String>(new String[]{"Administrador", "Usuário"});
        campoSenha = new JPasswordField();
        campoConfirmacao = new JPasswordField();

        JButton buscarCertificado = new JButton("Buscar");
        JButton buscarChave = new JButton("Buscar");

        formulario.add(new JLabel("Caminho do certificado digital:"));
        formulario.add(campoCertificado);
        formulario.add(buscarCertificado);

        formulario.add(new JLabel("Caminho da chave privada:"));
        formulario.add(campoChave);
        formulario.add(buscarChave);

        formulario.add(new JLabel("Frase secreta:"));
        formulario.add(campoFrase);
        formulario.add(new JLabel(""));

        formulario.add(new JLabel("Grupo:"));
        formulario.add(campoGrupo);
        formulario.add(new JLabel(""));

        formulario.add(new JLabel("Senha pessoal:"));
        formulario.add(campoSenha);
        formulario.add(new JLabel(""));

        formulario.add(new JLabel("Confirmação senha pessoal:"));
        formulario.add(campoConfirmacao);
        formulario.add(new JLabel(""));

        JPanel botoes = new JPanel();

        JButton cadastrar = new JButton("Cadastrar");
        JButton voltar = new JButton("Voltar");

        botoes.add(cadastrar);
        botoes.add(voltar);

        add(cabecalho, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        buscarCertificado.addActionListener(e -> escolherArquivo(campoCertificado));
        buscarChave.addActionListener(e -> escolherArquivo(campoChave));

        cadastrar.addActionListener(e -> cadastrarUsuario());
        voltar.addActionListener(e -> voltarMenu());
    }

    private void escolherArquivo(JTextField campo) {
        JFileChooser chooser = new JFileChooser();

        int opcao = chooser.showOpenDialog(this);

        if (opcao == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();
            campo.setText(arquivo.getAbsolutePath());
        }
    }

    private void cadastrarUsuario() {
        try {
            RegistroDAO.registrar(6002, usuarioLogado.getUid(), null);

            String caminhoCert = campoCertificado.getText().trim();
            String caminhoChave = campoChave.getText().trim();
            String frase = new String(campoFrase.getPassword());
            String senha = new String(campoSenha.getPassword());
            String confirmacao = new String(campoConfirmacao.getPassword());

            if (caminhoCert.isEmpty() || caminhoChave.isEmpty() || frase.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha os caminhos e a frase secreta.");
                return;
            }

            if (!senha.equals(confirmacao)) {
                JOptionPane.showMessageDialog(this, "As senhas pessoais não conferem.");
                return;
            }

            if (!SenhaUtils.senhaPessoalValida(senha)) {
                RegistroDAO.registrar(6003, usuarioLogado.getUid(), null);
                JOptionPane.showMessageDialog(this, "Senha pessoal inválida. Use 8, 9 ou 10 dígitos numéricos.");
                return;
            }

            X509Certificate certificado;

            try {
                certificado = CertUtils.carregarCertificado(caminhoCert);
            } catch (Exception e) {
                RegistroDAO.registrar(6004, usuarioLogado.getUid(), null);
                JOptionPane.showMessageDialog(this, "Certificado digital inválido.");
                return;
            }

            PrivateKey chavePrivada;

            try {
                chavePrivada = KeyUtils.carregarChavePrivada(caminhoChave, frase);
            } catch (Exception e) {
                RegistroDAO.registrar(6006, usuarioLogado.getUid(), null);
                JOptionPane.showMessageDialog(this, "Não foi possível abrir a chave privada. Verifique a frase secreta.");
                return;
            }

            boolean chaveOk = KeyUtils.validarChaveComCertificado(
                    chavePrivada,
                    certificado.getPublicKey(),
                    9216
            );

            if (!chaveOk) {
                RegistroDAO.registrar(6007, usuarioLogado.getUid(), null);
                JOptionPane.showMessageDialog(this, "A chave privada não combina com o certificado.");
                return;
            }

            String email = CertUtils.obterEmail(certificado);
            String nome = CertUtils.obterNome(certificado);

            if (email == null || email.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não foi possível extrair o e-mail do certificado.");
                return;
            }

            Usuario existente = UsuarioDAO.buscarPorEmail(email);

            if (existente != null) {
                JOptionPane.showMessageDialog(this, "Já existe usuário cadastrado com esse e-mail.");
                return;
            }

            String resumo = CertUtils.resumoCertificado(certificado);

            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    resumo + "\nConfirmar cadastro?",
                    "Confirmação do Certificado",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmar != JOptionPane.YES_OPTION) {
                RegistroDAO.registrar(6009, usuarioLogado.getUid(), null);
                return;
            }

            RegistroDAO.registrar(6008, usuarioLogado.getUid(), null);

            int gid = campoGrupo.getSelectedItem().toString().equals("Administrador") ? 1 : 2;

            String hashSenha = SenhaUtils.gerarHashBcrypt(senha);
            String segredoBase32 = TokenUtils.gerarSegredoBase32();
            byte[] segredoCriptografado = TokenUtils.criptografarSegredo(segredoBase32, senha);

            Usuario novo = new Usuario();
            novo.setNome(nome);
            novo.setEmail(email);
            novo.setGid(gid);
            novo.setSenhaHash(hashSenha);
            novo.setTokenKey(segredoCriptografado);

            int uid = UsuarioDAO.inserir(novo);

            String certPem = CertUtils.lerCertificadoComoTexto(caminhoCert);
            byte[] chaveCriptografada = Files.readAllBytes(Paths.get(caminhoChave));

            int kid = ChaveiroDAO.inserir(uid, certPem, chaveCriptografada);
            UsuarioDAO.atualizarKid(uid, kid);

            JOptionPane.showMessageDialog(
                    this,
                    "Usuário cadastrado com sucesso.\n\n" +
                            "Nome: " + nome + "\n" +
                            "E-mail: " + email + "\n" +
                            "Grupo: " + campoGrupo.getSelectedItem() + "\n\n" +
                            "Cadastre este segredo no Google Authenticator:\n" +
                            segredoBase32 + "\n\n" +
                            "URI opcional:\n" +
                            "otpauth://totp/Cofre%20Digital:" + email + "?secret=" + segredoBase32
            );

            limparFormulario();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limparFormulario() {
        campoCertificado.setText("");
        campoChave.setText("");
        campoFrase.setText("");
        campoSenha.setText("");
        campoConfirmacao.setText("");
        campoGrupo.setSelectedIndex(1);
    }

    private void voltarMenu() {
        try {
            RegistroDAO.registrar(6010, usuarioLogado.getUid(), null);
        } catch (Exception e) {
            // segue
        }

        new MainFrame(usuarioLogado).setVisible(true);
        dispose();
    }
}