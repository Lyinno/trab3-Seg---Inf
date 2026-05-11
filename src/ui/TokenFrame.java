package ui;

import crypto.TOTP;
import crypto.TokenUtils;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.Sessao;
import model.Usuario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class TokenFrame extends JFrame {
    private Usuario usuario;
    private String senhaCorreta;
    private JTextField campoToken;

    public TokenFrame(Usuario usuario, String senhaCorreta) {
        this.usuario = usuario;
        this.senhaCorreta = senhaCorreta;

        setTitle("Cofre Digital - Token");
        setSize(420, 160);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        campoToken = new JTextField();

        JButton botaoOk = new JButton("OK");
        JButton botaoLimpar = new JButton("LIMPAR");

        javax.swing.JPanel painelCentro = new javax.swing.JPanel(new GridLayout(2, 1, 5, 5));
        painelCentro.add(new JLabel("TOTP:"));
        painelCentro.add(campoToken);

        javax.swing.JPanel painelBotoes = new javax.swing.JPanel();
        painelBotoes.add(botaoOk);
        painelBotoes.add(botaoLimpar);

        add(painelCentro, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        botaoOk.addActionListener(e -> validarToken());
        botaoLimpar.addActionListener(e -> campoToken.setText(""));

        try {
            RegistroDAO.registrar(4001, usuario.getUid(), null);
        } catch (Exception e) {
            // ignora erro de log na tela
        }
    }

    private void validarToken() {
        try {
            String codigo = campoToken.getText().trim();

            String segredo = TokenUtils.decriptarSegredo(usuario.getTokenKey(), senhaCorreta);
            TOTP totp = new TOTP(segredo, 30);

            if (totp.validateCode(codigo)) {
                UsuarioDAO.limparErrosToken(usuario.getUid());
                UsuarioDAO.incrementarAcesso(usuario.getUid());

                usuario = UsuarioDAO.buscarPorUid(usuario.getUid());

                Sessao.setUsuarioAtual(usuario);
                Sessao.setSenhaPessoalAtual(senhaCorreta);

                RegistroDAO.registrar(4003, usuario.getUid(), null);
                RegistroDAO.registrar(4002, usuario.getUid(), null);
                RegistroDAO.registrar(1003, usuario.getUid(), null);

                new MainFrame(usuario).setVisible(true);
                dispose();
                return;
            }

            int erros = UsuarioDAO.incrementarErroToken(usuario.getUid());

            if (erros == 1) {
                RegistroDAO.registrar(4004, usuario.getUid(), null);
            } else if (erros == 2) {
                RegistroDAO.registrar(4005, usuario.getUid(), null);
            } else {
                RegistroDAO.registrar(4006, usuario.getUid(), null);
                UsuarioDAO.bloquearPorDoisMinutos(usuario.getUid());
                RegistroDAO.registrar(4007, usuario.getUid(), null);
                RegistroDAO.registrar(4002, usuario.getUid(), null);

                JOptionPane.showMessageDialog(this, "Terceiro erro de token. Usuário bloqueado por 2 minutos.");
                new LoginFrame().setVisible(true);
                dispose();
                return;
            }

            JOptionPane.showMessageDialog(this, "Token incorreto.");
            campoToken.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao validar token: " + e.getMessage());
        }
    }
}