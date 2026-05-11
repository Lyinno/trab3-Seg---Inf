package crypto;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.SecureRandom;

public class SenhaUtils {

    public static boolean senhaPessoalValida(String senha) {
        if (senha == null) {
            return false;
        }

        if (!senha.matches("[0-9]{8,10}")) {
            return false;
        }

        boolean todosIguais = true;
        for (int i = 1; i < senha.length(); i++) {
            if (senha.charAt(i) != senha.charAt(0)) {
                todosIguais = false;
                break;
            }
        }

        return !todosIguais;
    }

    public static String gerarHashBcrypt(String senha) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        return OpenBSDBCrypt.generate(senha.toCharArray(), salt, 8);
    }

    public static boolean verificarSenha(String senha, String hash) {
        try {
            return OpenBSDBCrypt.checkPassword(hash, senha.toCharArray());
        } catch (Exception e) {
            return false;
        }
    }
}