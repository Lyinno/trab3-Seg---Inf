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
    
        if (todosIguais) {
            return false;
        }
    
        for (int tamanho = 1; tamanho <= senha.length() / 2; tamanho++) {
            if (senha.length() % tamanho != 0) {
                continue;
            }
    
            String bloco = senha.substring(0, tamanho);
            StringBuilder repetida = new StringBuilder();
    
            while (repetida.length() < senha.length()) {
                repetida.append(bloco);
            }
    
            if (senha.equals(repetida.toString())) {
                return false;
            }
        }
    
        return true;
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
