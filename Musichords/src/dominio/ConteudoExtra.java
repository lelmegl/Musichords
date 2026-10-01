import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Conteúdo complementar ("livro aberto"): descrição musical das progressões, músicas
 * de referência que usam a mesma sequência de graus e escalas para treinar (RF05).
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe (passo 6 - Extras).
 */
public final class ConteudoExtra {

    private static final List<String> POP = Arrays.asList("I", "V", "vi", "IV");

    private ConteudoExtra() {
    }

    /** Texto curto que explica o caráter da progressão (como nos cards do wireframe). */
    public static String descrever(Progressao p) {
        String padrao = p.getPadraoGraus();
        if (p.getModulacoes() > 0) {
            Progressao.Etapa pivo = null;
            for (Progressao.Etapa e : p.getEtapas()) if (e.isPivo()) pivo = e;
            String acorde = pivo == null ? "" : pivo.getAcorde().getNomeCurto();
            return "Modula usando " + acorde + " como acorde pivô: " + String.join(" → ", p.getTonalidades()) + ".";
        }
        switch (padrao) {
            case "I–IV–V–I":
                return "A progressão mais clássica e resolutiva possível. Utiliza puramente os pilares maiores do campo harmônico.";
            case "I–V–vi–IV":
                return "A famosa \"progressão pop\". Soa épica, otimista e triunfante.";
            case "vi–IV–I–V":
                return "A progressão pop rotacionada a partir do relativo menor. Ganha um tom introspectivo e dramático.";
            case "I–vi–IV–V":
                return "A progressão dos anos 50 (doo-wop): nostálgica e muito usada em baladas.";
            case "ii–V–I":
                return "A cadência ii–V–I, base da harmonia do jazz.";
            default:
                break;
        }
        int menores = 0;
        for (Acorde a : p.getAcordes()) if (a.getTipo() != TipoAcorde.MAIOR) menores++;
        String fim;
        List<Progressao.Etapa> etapas = p.getEtapas();
        Progressao.Etapa ultima = etapas.get(etapas.size() - 1);
        CampoHarmonico.FuncaoTonal funcaoFinal = ultima.getVertices().get(ultima.getVertices().size() - 1).getFuncao();
        if (funcaoFinal == CampoHarmonico.FuncaoTonal.TONICA) fim = "termina em repouso, resolvendo na tônica";
        else if (funcaoFinal == CampoHarmonico.FuncaoTonal.DOMINANTE) fim = "termina em suspensão na dominante, pedindo para repetir";
        else fim = "termina na subdominante, com sensação de afastamento";
        if (padrao.contains("ii–V–I")) return "Contém a cadência ii–V–I e " + fim + ".";
        if (menores >= p.getAcordes().size() - 1) {
            return "Um caminho denso e melancólico, que transita quase inteiramente pelos graus menores; " + fim + ".";
        }
        return "Segue o fluxo " + p.getFluxoFuncional().toLowerCase() + " e " + fim + ".";
    }

    /** Músicas conhecidas que usam a mesma sequência de graus (independente da tonalidade). */
    public static List<String> musicasDeReferencia(Progressao p) {
        if (p.getModulacoes() > 0) return Collections.emptyList();
        List<String> graus = new ArrayList<>();
        for (Progressao.Etapa e : p.getEtapas()) graus.add(e.getGrau());
        String padrao = String.join("–", graus);

        switch (padrao) {
            case "I–V–vi–IV":
                return Arrays.asList("Let It Be — The Beatles", "No Woman, No Cry — Bob Marley",
                        "With or Without You — U2", "Someone Like You — Adele");
            case "vi–IV–I–V":
                return Arrays.asList("Zombie — The Cranberries", "Numb — Linkin Park");
            case "I–vi–IV–V":
                return Arrays.asList("Stand by Me — Ben E. King", "Every Breath You Take — The Police");
            case "I–IV–V–I":
            case "I–IV–V–IV":
                return Arrays.asList("Twist and Shout — The Beatles", "La Bamba — Ritchie Valens",
                        "Wild Thing — The Troggs");
            default:
                break;
        }
        if (graus.size() == 4 && ehRotacao(graus, POP)) {
            return Arrays.asList("Variação da progressão pop (I–V–vi–IV), presente em Let It Be — The Beatles",
                    "e em With or Without You — U2, só que começando em outro ponto do ciclo");
        }
        if (padrao.contains("ii–V–I")) {
            return Arrays.asList("Autumn Leaves (standard de jazz)", "Fly Me to the Moon — Frank Sinatra");
        }
        return Collections.emptyList();
    }

    private static boolean ehRotacao(List<String> a, List<String> b) {
        for (int r = 0; r < b.size(); r++) {
            boolean igual = true;
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).equals(b.get((i + r) % b.size()))) {
                    igual = false;
                    break;
                }
            }
            if (igual) return true;
        }
        return false;
    }
}
