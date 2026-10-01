/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Digitação (shape) de um acorde no braço: a casa de cada corda, da 6ª (Mi grave) à
 * 1ª (Mi agudo), com -1 para corda abafada e 0 para corda solta (atributo posicoesPorCorda[6]).
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe (RF07).
 */
public class ShapeAcorde {
    private final Acorde acorde;
    private final int[] posicoesPorCorda;
    private final int pestana;
    private final int dificuldade;
    private final String descricao;

    public ShapeAcorde(Acorde acorde, int[] posicoesPorCorda, int pestana, int dificuldade, String descricao) {
        if (posicoesPorCorda.length != 6) throw new IllegalArgumentException("Um shape precisa de 6 cordas");
        this.acorde = acorde;
        this.posicoesPorCorda = posicoesPorCorda.clone();
        this.pestana = pestana;
        this.dificuldade = dificuldade;
        this.descricao = descricao;
    }

    public Acorde getAcorde() {
        return acorde;
    }

    public int[] getPosicoesPorCorda() {
        return posicoesPorCorda.clone();
    }

    /** Casa da pestana, ou 0 quando o shape não usa pestana. */
    public int getPestana() {
        return pestana;
    }

    /** 1 (shape aberto simples) a 5 (pestana em posição alta). */
    public int getDificuldade() {
        return dificuldade;
    }

    public String getDescricao() {
        return descricao;
    }

    public int casaMinima() {
        int min = Integer.MAX_VALUE;
        for (int c : posicoesPorCorda) if (c > 0) min = Math.min(min, c);
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    public int casaMaxima() {
        int max = 0;
        for (int c : posicoesPorCorda) max = Math.max(max, c);
        return max;
    }

    /** Tablatura compacta do grave para o agudo, ex.: "x32010". */
    public String tablatura() {
        StringBuilder sb = new StringBuilder();
        for (int c : posicoesPorCorda) {
            if (c < 0) sb.append('x');
            else if (c > 9) sb.append('(').append(c).append(')');
            else sb.append(c);
        }
        return sb.toString();
    }
}
