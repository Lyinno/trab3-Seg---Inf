package crypto;

import java.io.ByteArrayOutputStream;

public class Base32 {
    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public String toString(byte[] dados) {
        StringBuilder resultado = new StringBuilder();

        int buffer = 0;
        int bitsRestantes = 0;

        for (byte b : dados) {
            buffer = (buffer << 8) | (b & 0xff);
            bitsRestantes += 8;

            while (bitsRestantes >= 5) {
                int index = (buffer >> (bitsRestantes - 5)) & 31;
                bitsRestantes -= 5;
                resultado.append(ALFABETO.charAt(index));
            }
        }

        if (bitsRestantes > 0) {
            int index = (buffer << (5 - bitsRestantes)) & 31;
            resultado.append(ALFABETO.charAt(index));
        }

        return resultado.toString();
    }

    public byte[] fromString(String texto) {
        texto = texto.replace("=", "").replaceAll("\\s", "").toUpperCase();

        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        int buffer = 0;
        int bitsRestantes = 0;

        for (int i = 0; i < texto.length(); i++) {
            int valor = ALFABETO.indexOf(texto.charAt(i));

            if (valor < 0) {
                return null;
            }

            buffer = (buffer << 5) | valor;
            bitsRestantes += 5;

            if (bitsRestantes >= 8) {
                saida.write((buffer >> (bitsRestantes - 8)) & 0xff);
                bitsRestantes -= 8;
            }
        }

        return saida.toByteArray();
    }
}