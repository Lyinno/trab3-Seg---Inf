package ui;

import db.RegistroDAO;
import db.UsuarioDAO;
import model.Usuario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {
    private JTextField campoEmail;

    public LoginFrame() {
        setTitle("Cofre Digital - Autenticação");
        setSize(420, 160);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        campoEmail = new JTextField();

        JButton botaoOk = new JButton("OK");
        JButton botaoLimpar = new JButton("LIMPAR");

        setLayout(new BorderLayout());

        GridLayout grid = new GridLayout(2, 1, 5, 5);
        javax.swing.JPanel painelCentro = new javax.swing.JPanel(grid);

        painelCentro.add(new JLabel("Login name:"));
        painelCentro.add(campoEmail);

        javax.swing.JPanel painelBotoes = new javax.swing.JPanel();
        painelBotoes.add(botaoOk);
        painelBotoes.add(botaoLimpar);

        add(painelCentro, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        botaoOk.addActionListener(e -> validarLogin());
        botaoLimpar.addActionListener(e -> campoEmail.setText(""));
    }

    private void validarLogin() {
        try {
            RegistroDAO.registrar(2001, null, null);

            String email = campoEmail.getText().trim();

            Usuario usuario = UsuarioDAO.buscarPorEmail(email);

            if (usuario == null) {
                RegistroDAO.registrar(2005, null, null);
                JOptionPane.showMessageDialog(this, "Login name não identificado.");
                return;
            }

            UsuarioDAO.desbloquearSeExpirou(usuario);
            usuario = UsuarioDAO.buscarPorEmail(email);

            if (usuario.estaBloqueado()) {
                RegistroDAO.registrar(2004, usuario.getUid(), null);
                long faltam = (usuario.getBloqueadoAte() - System.currentTimeMillis()) / 1000;
                JOptionPane.showMessageDialog(this, "Usuário bloqueado. Tente novamente em " + faltam + " segundos.");
                return;
            }

            RegistroDAO.registrar(2003, usuario.getUid(), null);
            RegistroDAO.registrar(2002, usuario.getUid(), null);

            new SenhaFrame(usuario).setVisible(true);
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro na autenticação: " + e.getMessage());
        }
    }
}