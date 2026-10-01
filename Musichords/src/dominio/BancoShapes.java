import java.util.HashMap;
import java.util.Map;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Base de digitações (camada de dados). Os acordes abertos usam as digitações
 * padronizadas de dicionários de acordes; os demais são gerados pelos shapes móveis com pestana
 * (formas de Mi e de Lá), escolhendo sempre a posição mais próxima da pestana do instrumento.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da base de shapes.
 */
public final class BancoShapes {

    private static final Map<Acorde, ShapeAcorde> ABERTOS = new HashMap<>();
    private static final Map<Acorde, ShapeAcorde> CACHE = new HashMap<>();

    static {
        aberto(0, TipoAcorde.MAIOR, -1, 3, 2, 0, 1, 0);   // C
        aberto(7, TipoAcorde.MAIOR, 3, 2, 0, 0, 0, 3);    // G
        aberto(2, TipoAcorde.MAIOR, -1, -1, 0, 2, 3, 2);  // D
        aberto(9, TipoAcorde.MAIOR, -1, 0, 2, 2, 2, 0);   // A
        aberto(4, TipoAcorde.MAIOR, 0, 2, 2, 1, 0, 0);    // E
        aberto(9, TipoAcorde.MENOR, -1, 0, 2, 2, 1, 0);   // Am
        aberto(4, TipoAcorde.MENOR, 0, 2, 2, 0, 0, 0);    // Em
        aberto(2, TipoAcorde.MENOR, -1, -1, 0, 2, 3, 1);  // Dm
    }

    private BancoShapes() {
    }

    private static void aberto(int raiz, TipoAcorde tipo, int... casas) {
        Acorde a = new Acorde(raiz, tipo);
        ABERTOS.put(a, new ShapeAcorde(a, casas, 0, 1, "Shape aberto (sem pestana)"));
    }

    /** Shape mais simples conhecido para a tríade do acorde. */
    public static ShapeAcorde shape(Acorde acorde) {
        Acorde chave = new Acorde(acorde.getRaiz(), acorde.getTipo().triadeBase());
        return CACHE.computeIfAbsent(chave, BancoShapes::gerar);
    }

    private static ShapeAcorde gerar(Acorde a) {
        ShapeAcorde aberto = ABERTOS.get(a);
        if (aberto != null) return aberto;

        int casaFormaMi = Nota.normalizar(a.getRaiz() - 4);  // raiz na 6ª corda
        int casaFormaLa = Nota.normalizar(a.getRaiz() - 9);  // raiz na 5ª corda
        if (casaFormaMi == 0) casaFormaMi = 12;
        if (casaFormaLa == 0) casaFormaLa = 12;

        if (a.getTipo() == TipoAcorde.DIMINUTO) {
            // Formas sem pestana: raiz na 5ª corda (x f f+1 f+2 f+1 x) ou na 6ª (f f+1 f+2 f x x)
            int fLa = Nota.normalizar(a.getRaiz() - 9);
            int fMi = Nota.normalizar(a.getRaiz() - 4);
            int[] casas = fLa <= fMi
                    ? new int[]{-1, fLa, fLa + 1, fLa + 2, fLa + 1, -1}
                    : new int[]{fMi, fMi + 1, fMi + 2, fMi, -1, -1};
            int pos = Math.min(fLa, fMi);
            return new ShapeAcorde(a, casas, 0, pos > 7 ? 4 : 3, "Shape fechado de quatro cordas");
        }

        boolean maior = a.getTipo() == TipoAcorde.MAIOR;
        int[] casas;
        int pestana;
        String forma;
        if (casaFormaMi <= casaFormaLa) {
            int f = casaFormaMi;
            casas = maior ? new int[]{f, f + 2, f + 2, f + 1, f, f} : new int[]{f, f + 2, f + 2, f, f, f};
            pestana = f;
            forma = "forma de Mi";
        } else {
            int f = casaFormaLa;
            casas = maior ? new int[]{-1, f, f + 2, f + 2, f + 2, f} : new int[]{-1, f, f + 2, f + 2, f + 1, f};
            pestana = f;
            forma = "forma de Lá";
        }
        int dificuldade = pestana > 7 ? 4 : 3;
        return new ShapeAcorde(a, casas, pestana, dificuldade, "Pestana na " + pestana + "ª casa (" + forma + ")");
    }
}
