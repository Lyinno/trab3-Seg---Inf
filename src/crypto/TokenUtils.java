package crypto;

import javax.crypto.SecretKey;
import java.security.SecureRandom;

public class TokenUtils {

    public static String gerarSegredoBase32() {
        byte[] segredo = new byte[20];
        new SecureRandom().nextBytes(segredo);

        Base32 base32 = new Base32();
        return base32.toString(segredo);
    }

    public static byte[] criptografarSegredo(String segredoBase32, String senhaPessoal) throws Exception {
        SecretKey chaveAES = CryptoUtils.gerarChaveAESPorSenha(senhaPessoal);
        return CryptoUtils.criptografarAES(segredoBase32.getBytes("UTF-8"), chaveAES);
    }

    public static String decriptarSegredo(byte[] segredoCriptografado, String senhaPessoal) throws Exception {
        SecretKey chaveAES = CryptoUtils.gerarChaveAESPorSenha(senhaPessoal);
        byte[] aberto = CryptoUtils.decriptarAES(segredoCriptografado, chaveAES);
        return new String(aberto, "UTF-8");
    }
}