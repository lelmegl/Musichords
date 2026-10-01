/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Qualidade (tipo) de um acorde, definida pelos intervalos em semitons a partir da raiz.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação do enum com tríades (usadas no grafo) e tétrades (reduzidas à tríade).
 */
public enum TipoAcorde {
    MAIOR("", "maior", new int[]{0, 4, 7}),
    MENOR("m", "menor", new int[]{0, 3, 7}),
    DIMINUTO("dim", "diminuto", new int[]{0, 3, 6}),
    // Tétrades: reconhecidas no braço e reduzidas à tríade correspondente para o grafo
    SETIMA("7", "com sétima", new int[]{0, 4, 7, 10}),
    SETIMA_MAIOR("7M", "com sétima maior", new int[]{0, 4, 7, 11}),
    MENOR_SETIMA("m7", "menor com sétima", new int[]{0, 3, 7, 10}),
    MEIO_DIMINUTO("m7(b5)", "meio-diminuto", new int[]{0, 3, 6, 10});

    private final String sufixo;
    private final String descricao;
    private final int[] intervalos;

    TipoAcorde(String sufixo, String descricao, int[] intervalos) {
        this.sufixo = sufixo;
        this.descricao = descricao;
        this.intervalos = intervalos;
    }

    public String getSufixo() {
        return sufixo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int[] getIntervalos() {
        return intervalos.clone();
    }

    public boolean isTriade() {
        return intervalos.length == 3;
    }

    /** Tríade que representa esta qualidade dentro do campo harmônico. */
    public TipoAcorde triadeBase() {
        switch (this) {
            case SETIMA:
            case SETIMA_MAIOR:
                return MAIOR;
            case MENOR_SETIMA:
                return MENOR;
            case MEIO_DIMINUTO:
                return DIMINUTO;
            default:
                return this;
        }
    }
}
