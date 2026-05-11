import javax.swing.JOptionPane;
import java.io.File;

public class ResetDatabase {
    public static void main(String[] args) {
        int opcao = JOptionPane.showConfirmDialog(
                null,
                "Isso vai apagar o banco data/cofre.db.\nDeseja continuar?",
                "Reset do Banco",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }

        File banco = new File("data/cofre.db");

        if (banco.exists()) {
            if (banco.delete()) {
                JOptionPane.showMessageDialog(null, "Banco apagado com sucesso.");
            } else {
                JOptionPane.showMessageDialog(null, "Não foi possível apagar o banco.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Banco ainda não existe.");
        }
    }
}