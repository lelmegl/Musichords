
/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Interpretação musical de um vértice do grafo (classe VerticeGrafo do diagrama de
 * classes): a partir do rótulo "Tonalidade_Grau" (ex.: "G_IV") obtém a tonalidade, o grau, a função
 * tonal e o acorde físico correspondente.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe.
 */
public class VerticeMusical {
    private final int id;
    private final String rotulo;
    private final CampoHarmonico campo;
    private final int grau;
    private final Acorde acorde;

    private VerticeMusical(int id, String rotulo, CampoHarmonico campo, int grau) {
        this.id = id;
        this.rotulo = rotulo;
        this.campo = campo;
        this.grau = grau;
        this.acorde = campo.acorde(grau);
    }

    /** Interpreta o rótulo do vértice; retorna null se ele não seguir o padrão Tonalidade_Grau. */
    public static VerticeMusical interpretar(Vertice v) {
        String rotulo = v.getRotulo().replace("\\", "").trim();
        int sep = rotulo.lastIndexOf('_');
        if (sep <= 0 || sep == rotulo.length() - 1) return null;
        int grau = CampoHarmonico.indiceGrau(rotulo.substring(sep + 1));
        if (grau < 0) return null;
        try {
            return new VerticeMusical(v.getId(), rotulo, new CampoHarmonico(rotulo.substring(0, sep)), grau);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getRotulo() {
        return rotulo;
    }

    public CampoHarmonico getCampo() {
        return campo;
    }

    public int getGrau() {
        return grau;
    }

    public String getGrauRomano() {
        return CampoHarmonico.GRAUS[grau];
    }

    public CampoHarmonico.FuncaoTonal getFuncao() {
        return CampoHarmonico.funcao(grau);
    }

    public Acorde getAcorde() {
        return acorde;
    }

    /** "Grau IV de Sol Maior" */
    public String descricao() {
        return "Grau " + getGrauRomano() + " de " + campo.getNomeExtenso();
    }
}
