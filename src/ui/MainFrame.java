package ui;

import db.RegistroDAO;
import db.UsuarioDAO;
import model.Sessao;
import model.SistemaContexto;
import model.Usuario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class MainFrame extends JFrame {
    private Usuario usuario;

    public MainFrame(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Cofre Digital - Menu Principal");
        setSize(520, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        montarTela();
    }

    private void montarTela() {
        try {
            usuario = UsuarioDAO.buscarPorUid(usuario.getUid());
            Sessao.setUsuarioAtual(usuario);
            RegistroDAO.registrar(5001, usuario.getUid(), null);
        } catch (Exception e) {
            // continua mesmo se falhar log
        }

        JPanel painelCabecalho = new JPanel(new GridLayout(4, 1));
        painelCabecalho.add(new JLabel("Login: " + usuario.getEmail()));
        painelCabecalho.add(new JLabel("Grupo: " + usuario.getGrupoNome()));
        painelCabecalho.add(new JLabel("Nome: " + usuario.getNome()));
        painelCabecalho.add(new JLabel("Total de acessos do usuário: " + usuario.getTotalAcessos()));

        JPanel painelMenu = new JPanel();

        if (usuario.getGid() == 1) {
            painelMenu.setLayout(new GridLayout(3, 1, 5, 5));

            JButton botaoCadastrar = new JButton("Cadastrar um novo usuário");
            JButton botaoConsultar = new JButton("Consultar pasta de arquivos secretos");
            JButton botaoSair = new JButton("Sair do sistema");

            painelMenu.add(botaoCadastrar);
            painelMenu.add(botaoConsultar);
            painelMenu.add(botaoSair);

            botaoCadastrar.addActionListener(e -> abrirCadastro());
            botaoConsultar.addActionListener(e -> abrirConsulta());
            botaoSair.addActionListener(e -> sair());

        } else {
            painelMenu.setLayout(new GridLayout(2, 1, 5, 5));

            JButton botaoConsultar = new JButton("Consultar pasta de arquivos secretos");
            JButton botaoSair = new JButton("Sair do sistema");

            painelMenu.add(botaoConsultar);
            painelMenu.add(botaoSair);

            botaoConsultar.addActionListener(e -> abrirConsulta());
            botaoSair.addActionListener(e -> sair());
        }

        add(painelCabecalho, BorderLayout.NORTH);
        add(painelMenu, BorderLayout.CENTER);
    }

    private void abrirCadastro() {
        try {
            RegistroDAO.registrar(5002, usuario.getUid(), null);
        } catch (Exception e) {
            // ignora
        }

        new CadastroFrame(usuario).setVisible(true);
        dispose();
    }

    private void abrirConsulta() {
        try {
            RegistroDAO.registrar(5003, usuario.getUid(), null);
        } catch (Exception e) {
            // ignora
        }

        new ConsultaFrame(usuario).setVisible(true);
        dispose();
    }

    private void sair() {
        try {
            RegistroDAO.registrar(5004, usuario.getUid(), null);
            RegistroDAO.registrar(8001, usuario.getUid(), null);
        } catch (Exception e) {
            // ignora
        }

        new SaidaFrame(usuario).setVisible(true);
        dispose();
    }
}