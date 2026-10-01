/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Utilitário de notas musicais. Cada nota é representada pela sua classe de altura
 * (0 = Dó, 1 = Dó#, ..., 11 = Si), o que permite tratar enarmonias (C# = Db) de forma única.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe para a identificação de acordes na interface.
 */
public final class Nota {

    public static final String[] SUSTENIDOS = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final String[] BEMOIS = {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
    private static final String[] NOMES_PT_SUST = {"Dó", "Dó#", "Ré", "Ré#", "Mi", "Fá", "Fá#", "Sol", "Sol#", "Lá", "Lá#", "Si"};
    private static final String[] NOMES_PT_BEM = {"Dó", "Ré♭", "Ré", "Mi♭", "Mi", "Fá", "Sol♭", "Sol", "Lá♭", "Lá", "Si♭", "Si"};

    /** Afinação padrão, da 6ª corda (Mi grave) para a 1ª (Mi agudo). */
    public static final int[] AFINACAO_PADRAO = {4, 9, 2, 7, 11, 4};
    /** Nota MIDI de cada corda solta (E2 A2 D3 G3 B3 E4). */
    public static final int[] MIDI_CORDAS = {40, 45, 50, 55, 59, 64};
    public static final String[] NOMES_CORDAS = {"E", "A", "D", "G", "B", "e"};

    private Nota() {
    }

    public static int normalizar(int classe) {
        return ((classe % 12) + 12) % 12;
    }

    public static String nome(int classe, boolean bemol) {
        return (bemol ? BEMOIS : SUSTENIDOS)[normalizar(classe)];
    }

    public static String nomePt(int classe, boolean bemol) {
        return (bemol ? NOMES_PT_BEM : NOMES_PT_SUST)[normalizar(classe)];
    }

    /** Nota produzida pela corda (0 = Mi grave ... 5 = Mi agudo) pressionada na casa indicada. */
    public static int notaNaCorda(int corda, int casa) {
        return normalizar(AFINACAO_PADRAO[corda] + casa);
    }

    /** Converte nomes como "C", "F#", "Db", "Bb" na classe de altura; -1 se inválido. */
    public static int parse(String nome) {
        if (nome == null || nome.isEmpty()) return -1;
        String n = nome.trim();
        int base;
        switch (Character.toUpperCase(n.charAt(0))) {
            case 'C': base = 0; break;
            case 'D': base = 2; break;
            case 'E': base = 4; break;
            case 'F': base = 5; break;
            case 'G': base = 7; break;
            case 'A': base = 9; break;
            case 'B': base = 11; break;
            default: return -1;
        }
        for (int i = 1; i < n.length(); i++) {
            char c = n.charAt(i);
            if (c == '#' || c == '♯') base++;
            else if (c == 'b' || c == '♭') base--;
            else return -1;
        }
        return normalizar(base);
    }
}
