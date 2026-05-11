package ui;

import db.RegistroDAO;
import model.Sessao;
import model.SistemaContexto;
import model.Usuario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class SaidaFrame extends JFrame {
    private Usuario usuario;

    public SaidaFrame(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Cofre Digital - Saída");
        setSize(520, 240);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel cabecalho = new JPanel(new GridLayout(3, 1));
        cabecalho.add(new JLabel("Login: " + usuario.getEmail()));
        cabecalho.add(new JLabel("Grupo: " + usuario.getGrupoNome()));
        cabecalho.add(new JLabel("Nome: " + usuario.getNome()));

        JLabel mensagem = new JLabel("Pressione o botão Encerrar Sessão ou o botão Encerrar Sistema para confirmar.");

        JButton encerrarSessao = new JButton("Encerrar Sessão");
        JButton encerrarSistema = new JButton("Encerrar Sistema");
        JButton voltar = new JButton("Voltar para o Menu Principal");

        JPanel botoes = new JPanel();
        botoes.add(encerrarSessao);
        botoes.add(encerrarSistema);
        botoes.add(voltar);

        add(cabecalho, BorderLayout.NORTH);
        add(mensagem, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        encerrarSessao.addActionListener(e -> encerrarSessao());
        encerrarSistema.addActionListener(e -> encerrarSistema());
        voltar.addActionListener(e -> voltar());
    }

    private void encerrarSessao() {
        try {
            RegistroDAO.registrar(8002, usuario.getUid(), null);
            RegistroDAO.registrar(1004, usuario.getUid(), null);
        } catch (Exception e) {
            // ignora
        }

        Sessao.limpar();
        new LoginFrame().setVisible(true);
        dispose();
    }

    private void encerrarSistema() {
        try {
            RegistroDAO.registrar(8003, usuario.getUid(), null);
            RegistroDAO.registrar(1002, null, null);
        } catch (Exception e) {
            // ignora
        }

        Sessao.limpar();
        SistemaContexto.limpar();
        System.exit(0);
    }

    private void voltar() {
        try {
            RegistroDAO.registrar(8004, usuario.getUid(), null);
        } catch (Exception e) {
            // ignora
        }

        new MainFrame(usuario).setVisible(true);
        dispose();
    }
}