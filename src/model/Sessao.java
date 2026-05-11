package model;

public class Sessao {
    private static Usuario usuarioAtual;
    private static String senhaPessoalAtual;

    public static Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    public static void setUsuarioAtual(Usuario usuarioAtual) {
        Sessao.usuarioAtual = usuarioAtual;
    }

    public static String getSenhaPessoalAtual() {
        return senhaPessoalAtual;
    }

    public static void setSenhaPessoalAtual(String senhaPessoalAtual) {
        Sessao.senhaPessoalAtual = senhaPessoalAtual;
    }

    public static void limpar() {
        usuarioAtual = null;
        senhaPessoalAtual = null;
    }
}