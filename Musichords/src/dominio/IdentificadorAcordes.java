import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Identifica o acorde formado pelas notas selecionadas no braço da guitarra (RF04).
 * Compara o conjunto de classes de altura com as fórmulas de cada TipoAcorde, testando cada nota
 * como possível raiz e dando preferência à nota mais grave (acorde em estado fundamental).
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação do identificador de acordes.
 */
public final class IdentificadorAcordes {

    private IdentificadorAcordes() {
    }

    /** Resultado da identificação. */
    public static class Resultado {
        private final List<Integer> notas;
        private final Acorde acorde;
        private final int baixo;

        Resultado(List<Integer> notas, Acorde acorde, int baixo) {
            this.notas = notas;
            this.acorde = acorde;
            this.baixo = baixo;
        }

        /** Notas distintas, na ordem em que soam (do grave para o agudo). */
        public List<Integer> getNotas() {
            return notas;
        }

        /** Acorde identificado ou null se as notas não formam um acorde conhecido. */
        public Acorde getAcorde() {
            return acorde;
        }

        public boolean isIdentificado() {
            return acorde != null;
        }

        /** Nota mais grave, ou -1 se nenhuma corda soa. */
        public int getBaixo() {
            return baixo;
        }

        public boolean isInvertido() {
            return acorde != null && baixo >= 0 && baixo != acorde.getRaiz();
        }

        public String notasFormatadas() {
            boolean bemol = acorde != null && acorde.isGrafiaBemol();
            List<String> nomes = new ArrayList<>();
            for (int n : notas) nomes.add(Nota.nome(n, bemol));
            return "{" + String.join(", ", nomes) + "}";
        }
    }

    /**
     * @param casas casa pressionada em cada corda (índice 0 = Mi grave, 5 = Mi agudo);
     *              -1 = corda não tocada, 0 = corda solta.
     */
    public static Resultado identificar(int[] casas) {
        Set<Integer> notas = new LinkedHashSet<>();
        int baixo = -1;
        for (int corda = 0; corda < 6; corda++) {
            if (casas[corda] < 0) continue;
            int nota = Nota.notaNaCorda(corda, casas[corda]);
            if (baixo < 0) baixo = nota;
            notas.add(nota);
        }
        return new Resultado(new ArrayList<>(notas), identificarConjunto(notas, baixo), baixo);
    }

    /** Identifica um acorde a partir de um conjunto de classes de altura (baixo = -1 se indiferente). */
    public static Acorde identificarConjunto(Collection<Integer> notasEntrada, int baixo) {
        Set<Integer> notas = new LinkedHashSet<>();
        for (int n : notasEntrada) notas.add(Nota.normalizar(n));
        if (notas.size() < 3) return null;

        Acorde melhor = null;
        int melhorPontuacao = Integer.MIN_VALUE;
        for (int raiz : notas) {
            for (TipoAcorde tipo : TipoAcorde.values()) {
                Set<Integer> formula = new LinkedHashSet<>();
                for (int i : tipo.getIntervalos()) formula.add(Nota.normalizar(raiz + i));
                if (!formula.equals(notas)) continue;
                int pontuacao = 0;
                if (raiz == baixo) pontuacao += 10;      // estado fundamental
                if (tipo.isTriade()) pontuacao += 2;     // tríade é a leitura mais simples
                if (pontuacao > melhorPontuacao) {
                    melhorPontuacao = pontuacao;
                    melhor = new Acorde(raiz, tipo, grafiaPadrao(raiz, tipo));
                }
            }
        }
        return melhor;
    }

    /** Grafia mais usual na guitarra: Bb, Eb, Ab, Db maiores; F#m, C#m, G#m, Ebm, Bbm menores. */
    public static boolean grafiaPadrao(int raiz, TipoAcorde tipo) {
        int r = Nota.normalizar(raiz);
        if (tipo.triadeBase() == TipoAcorde.MAIOR) return r == 1 || r == 3 || r == 8 || r == 10;
        if (tipo.triadeBase() == TipoAcorde.MENOR) return r == 3 || r == 10;
        return false;
    }
}
