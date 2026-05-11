package crypto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TecladoVirtual {
    private List<String> botoes;

    public TecladoVirtual() {
        embaralhar();
    }

    public void embaralhar() {
        List<Integer> numeros = new ArrayList<Integer>();

        for (int i = 0; i <= 9; i++) {
            numeros.add(i);
        }

        Collections.shuffle(numeros);

        botoes = new ArrayList<String>();

        for (int i = 0; i < 10; i += 2) {
            botoes.add("" + numeros.get(i) + numeros.get(i + 1));
        }
    }

    public void mostrar() {
        System.out.println();
        System.out.println("Teclado virtual:");

        for (int i = 0; i < botoes.size(); i++) {
            System.out.print((i + 1) + ":[" + botoes.get(i).charAt(0) + " " + botoes.get(i).charAt(1) + "]  ");
        }

        System.out.println();
        System.out.println("Digite o numero do botao escolhido. Ex: 1, 2, 3...");
    }

    public String getBotao(int indice) {
        if (indice < 1 || indice > 5) {
            return null;
        }

        return botoes.get(indice - 1);
    }

    public static List<String> gerarPossibilidades(List<String> botoesPressionados) {
        List<String> resultados = new ArrayList<String>();
        gerarRecursivo(botoesPressionados, 0, "", resultados);
        return resultados;
    }

    private static void gerarRecursivo(List<String> botoes, int pos, String atual, List<String> resultados) {
        if (pos == botoes.size()) {
            resultados.add(atual);
            return;
        }

        String botao = botoes.get(pos);

        gerarRecursivo(botoes, pos + 1, atual + botao.charAt(0), resultados);
        gerarRecursivo(botoes, pos + 1, atual + botao.charAt(1), resultados);
    }
}