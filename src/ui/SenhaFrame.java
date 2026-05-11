package ui;

import crypto.SenhaUtils;
import crypto.TecladoVirtual;
import db.RegistroDAO;
import db.UsuarioDAO;
import model.Usuario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class SenhaFrame extends JFrame {
    private Usuario usuario;
    private TecladoVirtual teclado;
    private List<String> botoesPressionados;
    private JLabel labelSenha;
    private JPanel painelTeclado;

    public SenhaFrame(Usuario usuario) {
        this.usuario = usuario;
        this.botoesPressionados = new ArrayList<String>();

        setTitle("Cofre Digital - Senha Pessoal");
        setSize(500, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        labelSenha = new JLabel("Senha pessoal: ");

        painelTeclado = new JPanel(new GridLayout(1, 5, 5, 5));

        JButton botaoOk = new JButton("OK");
        JButton botaoLimpar = new JButton("LIMPAR");

        JPanel painelSul = new JPanel();
        painelSul.add(botaoOk);
        painelSul.add(botaoLimpar);

        add(labelSenha, BorderLayout.NORTH);
        add(painelTeclado, BorderLayout.CENTER);
        add(painelSul, BorderLayout.SOUTH);

        botaoOk.addActionListener(e -> validarSenha());
        botaoLimpar.addActionListener(e -> limparSenha());

        try {
            RegistroDAO.registrar(3001, usuario.getUid(), null);
        } catch (Exception e) {
            // ignora erro de log na tela
        }

        montarTeclado();
    }

    private void montarTeclado() {
        teclado = new TecladoVirtual();
        painelTeclado.removeAll();

        for (int i = 1; i <= 5; i++) {
            final int indice = i;
            String textoBotao = teclado.getBotao(i);
            JButton botao = new JButton(textoBotao.charAt(0) + "   " + textoBotao.charAt(1));

            botao.addActionListener(e -> pressionarBotao(indice));
            painelTeclado.add(botao);
        }

        painelTeclado.revalidate();
        painelTeclado.repaint();
    }

    private void pressionarBotao(int indice) {
        if (botoesPressionados.size() >= 10) {
            JOptionPane.showMessageDialog(this, "A senha pode ter no máximo 10 dígitos.");
            return;
        }

        String botao = teclado.getBotao(indice);
        botoesPressionados.add(botao);

        atualizarMascara();
        montarTeclado();
    }

    private void atualizarMascara() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < botoesPressionados.size(); i++) {
            sb.append("*");
        }

        labelSenha.setText("Senha pessoal: " + sb.toString());
    }

    private void limparSenha() {
        botoesPressionados.clear();
        atualizarMascara();
        montarTeclado();
    }

    private void validarSenha() {
        try {
            if (botoesPressionados.size() < 8) {
                JOptionPane.showMessageDialog(this, "A senha precisa ter pelo menos 8 dígitos.");
                return;
            }

            List<String> possibilidades = TecladoVirtual.gerarPossibilidades(botoesPressionados);

            for (String tentativa : possibilidades) {
                if (SenhaUtils.verificarSenha(tentativa, usuario.getSenhaHash())) {
                    UsuarioDAO.limparErrosSenha(usuario.getUid());
                    RegistroDAO.registrar(3003, usuario.getUid(), null);
                    RegistroDAO.registrar(3002, usuario.getUid(), null);

                    new TokenFrame(usuario, tentativa).setVisible(true);
                    dispose();
                    return;
                }
            }

            int erros = UsuarioDAO.incrementarErroSenha(usuario.getUid());

            if (erros == 1) {
                RegistroDAO.registrar(3004, usuario.getUid(), null);
            } else if (erros == 2) {
                RegistroDAO.registrar(3005, usuario.getUid(), null);
            } else {
                RegistroDAO.registrar(3006, usuario.getUid(), null);
                UsuarioDAO.bloquearPorDoisMinutos(usuario.getUid());
                RegistroDAO.registrar(3007, usuario.getUid(), null);
                RegistroDAO.registrar(3002, usuario.getUid(), null);

                JOptionPane.showMessageDialog(this, "Terceiro erro. Usuário bloqueado por 2 minutos.");
                new LoginFrame().setVisible(true);
                dispose();
                return;
            }

            RegistroDAO.registrar(3002, usuario.getUid(), null);
            JOptionPane.showMessageDialog(this, "Senha incorreta.");
            limparSenha();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao validar senha: " + e.getMessage());
        }
    }
}