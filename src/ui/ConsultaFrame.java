package ui;

import crypto.CertUtils;
import crypto.KeyUtils;
import db.ChaveiroDAO;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.SistemaContexto;
import model.Usuario;
import vault.ArquivoProtegido;
import vault.PastaSeguraService;

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
import java.util.ArrayList;
import java.util.List;

public class ConsultaFrame extends JFrame {
    private Usuario usuarioLogado;

    private JTextField campoPasta;
    private JPasswordField campoFrase;
    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private List<ArquivoProtegido> arquivosVisiveis;
    private File pastaAtual;
    private PrivateKey chavePrivadaUsuario;
    private X509Certificate certUsuario;

    public ConsultaFrame(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.arquivosVisiveis = new ArrayList<ArquivoProtegido>();

        setTitle("Cofre Digital - Consulta da Pasta Segura");
        setSize(850, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        montarTela();

        try {
            RegistroDAO.registrar(7001, usuarioLogado.getUid(), null);
        } catch (Exception e) {
            // segue sem travar a tela
        }
    }

    private void montarTela() {
        JPanel cabecalho = new JPanel(new GridLayout(4, 1));
        cabecalho.add(new JLabel("Login: " + usuarioLogado.getEmail()));
        cabecalho.add(new JLabel("Grupo: " + usuarioLogado.getGrupoNome()));
        cabecalho.add(new JLabel("Nome: " + usuarioLogado.getNome()));
        cabecalho.add(new JLabel("Total de consultas do usuário: " + usuarioLogado.getTotalConsultas()));

        JPanel formulario = new JPanel(new GridLayout(2, 3, 5, 5));

        campoPasta = new JTextField();
        campoFrase = new JPasswordField();

        JButton buscarPasta = new JButton("Buscar");
        JButton listar = new JButton("Listar");

        formulario.add(new JLabel("Caminho da pasta:"));
        formulario.add(campoPasta);
        formulario.add(buscarPasta);

        formulario.add(new JLabel("Frase secreta da sua chave privada:"));
        formulario.add(campoFrase);
        formulario.add(listar);

        modeloTabela = new DefaultTableModel();
        modeloTabela.addColumn("Nome código");
        modeloTabela.addColumn("Nome secreto");
        modeloTabela.addColumn("Dono");
        modeloTabela.addColumn("Grupo");

        tabela = new JTable(modeloTabela);
        JScrollPane scroll = new JScrollPane(tabela);

        JButton abrirArquivo = new JButton("Decriptar arquivo selecionado");
        JButton voltar = new JButton("Voltar para o Menu Principal");

        JPanel botoes = new JPanel();
        botoes.add(abrirArquivo);
        botoes.add(voltar);

        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.add(cabecalho, BorderLayout.NORTH);
        painelTopo.add(formulario, BorderLayout.CENTER);

        add(painelTopo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        buscarPasta.addActionListener(e -> escolherPasta());
        listar.addActionListener(e -> listarArquivos());
        abrirArquivo.addActionListener(e -> abrirSelecionado());
        voltar.addActionListener(e -> voltarMenu());
    }

    private void escolherPasta() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int opcao = chooser.showOpenDialog(this);

        if (opcao == JFileChooser.APPROVE_OPTION) {
            File pasta = chooser.getSelectedFile();
            campoPasta.setText(pasta.getAbsolutePath());
        }
    }

    private void listarArquivos() {
        try {
            RegistroDAO.registrar(7003, usuarioLogado.getUid(), null);

            String caminho = campoPasta.getText().trim();
            String fraseUsuario = new String(campoFrase.getPassword());

            if (caminho.isEmpty() || fraseUsuario.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe o caminho da pasta e a frase secreta.");
                return;
            }

            pastaAtual = new File(caminho);

            if (!pastaAtual.exists() || !pastaAtual.isDirectory()) {
                RegistroDAO.registrar(7004, usuarioLogado.getUid(), null);
                JOptionPane.showMessageDialog(this, "Caminho da pasta inválido.");
                return;
            }

            String fraseAdmin = SistemaContexto.getFraseSecretaAdmin();

            if (fraseAdmin == null) {
                JOptionPane.showMessageDialog(this, "Frase secreta do administrador não está carregada.");
                return;
            }

            Usuario admin = UsuarioDAO.buscarPrimeiroAdministrador();

            if (admin == null) {
                JOptionPane.showMessageDialog(this, "Administrador não encontrado.");
                return;
            }

            String certAdminPem = ChaveiroDAO.buscarCertificadoPorKid(admin.getKid());
            byte[] chaveAdminBytes = ChaveiroDAO.buscarChavePrivadaPorKid(admin.getKid());

            X509Certificate certAdmin = CertUtils.carregarCertificadoDeTexto(certAdminPem);

            PrivateKey chavePrivadaAdmin;

            try {
                chavePrivadaAdmin = KeyUtils.carregarChavePrivadaDeBytes(chaveAdminBytes, fraseAdmin);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir chave privada do administrador.");
                return;
            }

            boolean chaveAdminOk = KeyUtils.validarChaveComCertificado(
                    chavePrivadaAdmin,
                    certAdmin.getPublicKey(),
                    9216
            );

            if (!chaveAdminOk) {
                JOptionPane.showMessageDialog(this, "Chave privada do administrador inválida.");
                return;
            }

            String certUsuarioPem = ChaveiroDAO.buscarCertificadoPorKid(usuarioLogado.getKid());
            byte[] chaveUsuarioBytes = ChaveiroDAO.buscarChavePrivadaPorKid(usuarioLogado.getKid());

            certUsuario = CertUtils.carregarCertificadoDeTexto(certUsuarioPem);

            try {
                chavePrivadaUsuario = KeyUtils.carregarChavePrivadaDeBytes(chaveUsuarioBytes, fraseUsuario);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Frase secreta do usuário inválida.");
                return;
            }

            boolean chaveUsuarioOk = KeyUtils.validarChaveComCertificado(
                    chavePrivadaUsuario,
                    certUsuario.getPublicKey(),
                    9216
            );

            if (!chaveUsuarioOk) {
                JOptionPane.showMessageDialog(this, "Chave privada do usuário não combina com o certificado.");
                return;
            }

            List<ArquivoProtegido> todos;

            try {
                todos = PastaSeguraService.lerIndice(
                        pastaAtual,
                        chavePrivadaAdmin,
                        certAdmin.getPublicKey()
                );

                RegistroDAO.registrar(7005, usuarioLogado.getUid(), null);
                RegistroDAO.registrar(7006, usuarioLogado.getUid(), null);

            } catch (Exception e) {
                RegistroDAO.registrar(7007, usuarioLogado.getUid(), null);
                RegistroDAO.registrar(7008, usuarioLogado.getUid(), null);
                JOptionPane.showMessageDialog(this, "Erro ao abrir ou verificar índice: " + e.getMessage());
                return;
            }

            arquivosVisiveis.clear();
            modeloTabela.setRowCount(0);

            String grupoUsuario = usuarioLogado.getGrupoNome();

            for (ArquivoProtegido arq : todos) {
                if (arq.podeListar(usuarioLogado.getEmail(), grupoUsuario)) {
                    arquivosVisiveis.add(arq);

                    modeloTabela.addRow(new Object[]{
                            arq.getNomeCodigo(),
                            arq.getNomeSecreto(),
                            arq.getDono(),
                            arq.getGrupo()
                    });
                }
            }

            RegistroDAO.registrar(7009, usuarioLogado.getUid(), null);
            UsuarioDAO.incrementarConsulta(usuarioLogado.getUid());

            if (arquivosVisiveis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum arquivo disponível para este usuário.");
            } else {
                JOptionPane.showMessageDialog(this, "Arquivos listados com sucesso.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro na consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirSelecionado() {
        try {
            int linha = tabela.getSelectedRow();

            if (linha < 0) {
                JOptionPane.showMessageDialog(this, "Selecione um arquivo na tabela.");
                return;
            }

            if (pastaAtual == null || chavePrivadaUsuario == null || certUsuario == null) {
                JOptionPane.showMessageDialog(this, "Liste os arquivos antes de tentar decriptar.");
                return;
            }

            ArquivoProtegido escolhido = arquivosVisiveis.get(linha);

            RegistroDAO.registrar(7010, usuarioLogado.getUid(), escolhido.getNomeSecreto());

            if (!escolhido.podeAbrir(usuarioLogado.getEmail())) {
                RegistroDAO.registrar(7012, usuarioLogado.getUid(), escolhido.getNomeSecreto());
                JOptionPane.showMessageDialog(this, "Acesso negado. Você não é o dono deste arquivo.");
                return;
            }

            RegistroDAO.registrar(7011, usuarioLogado.getUid(), escolhido.getNomeSecreto());

            try {
                byte[] dados = PastaSeguraService.abrirArquivoProtegido(
                        pastaAtual,
                        escolhido.getNomeCodigo(),
                        chavePrivadaUsuario,
                        certUsuario.getPublicKey()
                );

                PastaSeguraService.salvarArquivoAberto(
                        pastaAtual,
                        escolhido.getNomeSecreto(),
                        dados
                );

                RegistroDAO.registrar(7013, usuarioLogado.getUid(), escolhido.getNomeSecreto());
                RegistroDAO.registrar(7014, usuarioLogado.getUid(), escolhido.getNomeSecreto());

                JOptionPane.showMessageDialog(
                        this,
                        "Arquivo decriptado com sucesso:\n" +
                                new File(pastaAtual, escolhido.getNomeSecreto()).getAbsolutePath()
                );

            } catch (Exception e) {
                RegistroDAO.registrar(7015, usuarioLogado.getUid(), escolhido.getNomeSecreto());
                RegistroDAO.registrar(7016, usuarioLogado.getUid(), escolhido.getNomeSecreto());

                JOptionPane.showMessageDialog(
                        this,
                        "Erro ao decriptar ou verificar arquivo:\n" + e.getMessage()
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void voltarMenu() {
        try {
            RegistroDAO.registrar(7002, usuarioLogado.getUid(), null);
        } catch (Exception e) {
            // segue
        }

        new MainFrame(usuarioLogado).setVisible(true);
        dispose();
    }
}