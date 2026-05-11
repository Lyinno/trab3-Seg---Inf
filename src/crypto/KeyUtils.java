package crypto;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.util.Base64;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyUtils {

    public static PrivateKey carregarChavePrivada(String caminhoArquivo, String fraseSecreta) throws Exception {
        byte[] arquivoCriptografado = Files.readAllBytes(Paths.get(caminhoArquivo));
        return carregarChavePrivadaDeBytes(arquivoCriptografado, fraseSecreta);
    }

    public static PrivateKey carregarChavePrivadaDeBytes(byte[] arquivoCriptografado, String fraseSecreta) throws Exception {
        SecretKey chaveAES = CryptoUtils.gerarChaveAESPorSenha(fraseSecreta);
        byte[] dadosDecriptados = CryptoUtils.decriptarAES(arquivoCriptografado, chaveAES);

        String texto = new String(dadosDecriptados, "UTF-8");
        texto = texto.replace("-----BEGIN PRIVATE KEY-----", "");
        texto = texto.replace("-----END PRIVATE KEY-----", "");
        texto = texto.replaceAll("\\s", "");

        byte[] chaveBytes = Base64.getDecoder().decode(texto);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(chaveBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(spec);
    }

    public static boolean validarChaveComCertificado(PrivateKey chavePrivada, java.security.PublicKey chavePublica, int tamanhoTeste) {
        try {
            byte[] teste = CryptoUtils.gerarBytesAleatorios(tamanhoTeste);
            byte[] assinatura = CryptoUtils.assinar(teste, chavePrivada);
            return CryptoUtils.verificarAssinatura(teste, assinatura, chavePublica);
        } catch (Exception e) {
            return false;
        }
    }
}