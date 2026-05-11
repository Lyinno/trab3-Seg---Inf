package crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

public class TOTP {
    private byte[] key = null;
    private long timeStepInSeconds = 30;

    public TOTP(String base32EncodedSecret, long timeStepInSeconds) throws Exception {
        Base32 base32 = new Base32();
        byte[] decoded = base32.fromString(base32EncodedSecret);

        if (decoded == null) {
            throw new Exception("Chave Base32 inválida.");
        }

        this.key = decoded;
        this.timeStepInSeconds = timeStepInSeconds;
    }

    private String getTOTPCodeFromHash(byte[] hash) {
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int otp = binary % 1000000;

        return String.format("%06d", otp);
    }

    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        try {
            SecretKeySpec signKey = new SecretKeySpec(keyByteArray, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            return mac.doFinal(counter);
        } catch (Exception e) {
            return null;
        }
    }

    private String TOTPCode(long timeInterval) {
        byte[] counter = new byte[8];

        for (int i = 7; i >= 0; i--) {
            counter[i] = (byte) (timeInterval & 0xff);
            timeInterval >>= 8;
        }

        byte[] hash = HMAC_SHA1(counter, key);

        if (hash == null) {
            return null;
        }

        return getTOTPCodeFromHash(hash);
    }

    public String generateCode() {
        long agora = new Date().getTime() / 1000;
        long intervalo = agora / timeStepInSeconds;

        return TOTPCode(intervalo);
    }

    public boolean validateCode(String inputTOTP) {
        if (inputTOTP == null || !inputTOTP.matches("[0-9]{6}")) {
            return false;
        }

        long agora = new Date().getTime() / 1000;
        long intervalo = agora / timeStepInSeconds;

        String anterior = TOTPCode(intervalo - 1);
        String atual = TOTPCode(intervalo);
        String proximo = TOTPCode(intervalo + 1);

        return inputTOTP.equals(anterior) ||
               inputTOTP.equals(atual) ||
               inputTOTP.equals(proximo);
    }
}