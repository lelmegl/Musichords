import java.util.ArrayList;
import java.util.List;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Campo harmônico de uma tonalidade maior, gerado pelas regras da teoria musical
 * (fórmula da escala maior T-T-st-T-T-T-st e empilhamento de terças).
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe (base do RF01 e do RF05).
 */
public class CampoHarmonico {

    public static final String[] GRAUS = {"I", "ii", "iii", "IV", "V", "vi", "vii"};
    public static final int[] ESCALA_MAIOR = {0, 2, 4, 5, 7, 9, 11};
    private static final TipoAcorde[] QUALIDADES = {
        TipoAcorde.MAIOR, TipoAcorde.MENOR, TipoAcorde.MENOR, TipoAcorde.MAIOR,
        TipoAcorde.MAIOR, TipoAcorde.MENOR, TipoAcorde.DIMINUTO
    };
    private static final FuncaoTonal[] FUNCOES = {
        FuncaoTonal.TONICA, FuncaoTonal.SUBDOMINANTE, FuncaoTonal.TONICA, FuncaoTonal.SUBDOMINANTE,
        FuncaoTonal.DOMINANTE, FuncaoTonal.TONICA, FuncaoTonal.DOMINANTE
    };

    /** Função tonal de cada grau. */
    public enum FuncaoTonal {
        TONICA("Tônica"), SUBDOMINANTE("Subdominante"), DOMINANTE("Dominante");

        private final String nome;

        FuncaoTonal(String nome) {
            this.nome = nome;
        }

        public String getNome() {
            return nome;
        }
    }

    private final int tonica;
    private final String nome;
    private final boolean bemol;

    public CampoHarmonico(String nomeTonalidade) {
        int t = Nota.parse(nomeTonalidade);
        if (t < 0) throw new IllegalArgumentException("Tonalidade inválida: " + nomeTonalidade);
        this.tonica = t;
        this.nome = nomeTonalidade.trim();
        this.bemol = nome.length() > 1 && (nome.charAt(1) == 'b' || nome.charAt(1) == '♭') || t == 5;
    }

    public int getTonica() {
        return tonica;
    }

    /** Nome como aparece no grafo.txt: "C", "F#", "Db"... */
    public String getNome() {
        return nome;
    }

    public boolean isBemol() {
        return bemol;
    }

    public String getNomeExtenso() {
        return Nota.nomePt(tonica, bemol) + " Maior";
    }

    public Acorde acorde(int grau) {
        return new Acorde(tonica + ESCALA_MAIOR[grau], QUALIDADES[grau], bemol);
    }

    public List<Acorde> acordes() {
        List<Acorde> lista = new ArrayList<>();
        for (int g = 0; g < 7; g++) lista.add(acorde(g));
        return lista;
    }

    public static FuncaoTonal funcao(int grau) {
        return FUNCOES[grau];
    }

    /** Índice do grau a partir do numeral romano ("I", "ii", "IV"...), ou -1. */
    public static int indiceGrau(String grau) {
        for (int i = 0; i < GRAUS.length; i++) {
            if (GRAUS[i].equalsIgnoreCase(grau.trim())) return i;
        }
        return -1;
    }

    /** "1º", "4º"... como no documento de simulação. */
    public static String grauOrdinal(int grau) {
        return (grau + 1) + "º";
    }

    public List<Integer> escalaMaior() {
        List<Integer> notas = new ArrayList<>();
        for (int i : ESCALA_MAIOR) notas.add(Nota.normalizar(tonica + i));
        return notas;
    }

    /** Pentatônica menor da relativa (vi grau): 1, b3, 4, 5, b7. */
    public List<Integer> pentatonicaRelativa() {
        int relativa = tonica + 9;
        List<Integer> notas = new ArrayList<>();
        for (int i : new int[]{0, 3, 5, 7, 10}) notas.add(Nota.normalizar(relativa + i));
        return notas;
    }

    public String formatarNotas(List<Integer> notas) {
        List<String> nomes = new ArrayList<>();
        for (int n : notas) nomes.add(Nota.nome(n, bemol));
        return String.join(" – ", nomes);
    }

    @Override
    public String toString() {
        return getNomeExtenso();
    }
}
