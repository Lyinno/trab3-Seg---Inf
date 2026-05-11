package model;

public class SistemaContexto {
    private static String fraseSecretaAdmin;

    public static String getFraseSecretaAdmin() {
        return fraseSecretaAdmin;
    }

    public static void setFraseSecretaAdmin(String fraseSecretaAdmin) {
        SistemaContexto.fraseSecretaAdmin = fraseSecretaAdmin;
    }

    public static void limpar() {
        fraseSecretaAdmin = null;
    }
}