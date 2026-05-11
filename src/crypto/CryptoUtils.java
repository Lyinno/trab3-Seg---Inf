package crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import javax.crypto.SecretKey;

public class CryptoUtils {

    public static SecretKey gerarChaveAESPorSenha(String senha) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(senha.getBytes("UTF-8"));
        keyGen.init(256, random);
        return keyGen.generateKey();
    }

    public static SecretKey gerarChaveAESPorSemente(byte[] semente) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(semente);
        keyGen.init(256, random);
        return keyGen.generateKey();
    }

    public static byte[] criptografarAES(byte[] dados, SecretKey chave) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        return cipher.doFinal(dados);
    }

    public static byte[] decriptarAES(byte[] dados, SecretKey chave) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chave);
        return cipher.doFinal(dados);
    }

    public static byte[] criptografarRSA(byte[] dados, PublicKey chavePublica) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, chavePublica);
        return cipher.doFinal(dados);
    }

    public static byte[] decriptarRSA(byte[] dados, PrivateKey chavePrivada) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, chavePrivada);
        return cipher.doFinal(dados);
    }

    public static byte[] assinar(byte[] dados, PrivateKey chavePrivada) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(chavePrivada);
        sig.update(dados);
        return sig.sign();
    }

    public static boolean verificarAssinatura(byte[] dados, byte[] assinatura, PublicKey chavePublica) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(chavePublica);
        sig.update(dados);
        return sig.verify(assinatura);
    }

    public static byte[] gerarBytesAleatorios(int tamanho) throws Exception {
        byte[] dados = new byte[tamanho];
        SecureRandom random = new SecureRandom();
        random.nextBytes(dados);
        return dados;
    }
}