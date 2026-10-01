import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Progressão sugerida = caminho no grafo de campos harmônicos. Guarda os vértices
 * percorridos (inclusive os de pivô) e os pesos das arestas, e oferece a visão "musical":
 * a sequência de acordes, os graus, o custo total e a dificuldade dos shapes.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe (RF02 / RF06).
 */
public class Progressao {

    /** Um acorde da progressão e os vértices (papéis) que ele ocupou no caminho. */
    public static class Etapa {
        private final Acorde acorde;
        private final List<VerticeMusical> vertices = new ArrayList<>();
        private int pesoEntrada;

        Etapa(Acorde acorde) {
            this.acorde = acorde;
        }

        public Acorde getAcorde() {
            return acorde;
        }

        public List<VerticeMusical> getVertices() {
            return Collections.unmodifiableList(vertices);
        }

        /** Peso da aresta que levou até este acorde (0 para o primeiro). */
        public int getPesoEntrada() {
            return pesoEntrada;
        }

        public boolean isPivo() {
            return vertices.size() > 1;
        }

        /** "IV" ou, se for acorde pivô, "I→IV". */
        public String getGrau() {
            List<String> g = new ArrayList<>();
            for (VerticeMusical v : vertices) g.add(v.getGrauRomano());
            return String.join("→", g);
        }

        public String getGrauOrdinal() {
            List<String> g = new ArrayList<>();
            for (VerticeMusical v : vertices) g.add(CampoHarmonico.grauOrdinal(v.getGrau()));
            return String.join("→", g);
        }
    }

    private final List<VerticeMusical> caminho;
    private final List<Integer> pesos;
    private final List<Etapa> etapas = new ArrayList<>();

    /**
     * @param caminho vértices na ordem percorrida
     * @param pesos   pesos das arestas (tamanho = caminho.size() - 1)
     */
    public Progressao(List<VerticeMusical> caminho, List<Integer> pesos) {
        this.caminho = new ArrayList<>(caminho);
        this.pesos = new ArrayList<>(pesos);
        Etapa atual = null;
        for (int i = 0; i < caminho.size(); i++) {
            VerticeMusical v = caminho.get(i);
            if (atual == null || !atual.acorde.equals(v.getAcorde())) {
                atual = new Etapa(v.getAcorde());
                atual.pesoEntrada = i == 0 ? 0 : pesos.get(i - 1);
                etapas.add(atual);
            }
            atual.vertices.add(v);
        }
    }

    public List<VerticeMusical> getCaminho() {
        return Collections.unmodifiableList(caminho);
    }

    public List<Integer> getPesos() {
        return Collections.unmodifiableList(pesos);
    }

    public List<Etapa> getEtapas() {
        return Collections.unmodifiableList(etapas);
    }

    public List<Acorde> getAcordes() {
        List<Acorde> lista = new ArrayList<>();
        for (Etapa e : etapas) lista.add(e.acorde);
        return lista;
    }

    public int getCustoTotal() {
        int total = 0;
        for (int p : pesos) total += p;
        return total;
    }

    public int getDificuldadeShapes() {
        int total = 0;
        for (Etapa e : etapas) total += BancoShapes.shape(e.acorde).getDificuldade();
        return total;
    }

    public int getModulacoes() {
        int n = 0;
        for (Etapa e : etapas) n += e.vertices.size() - 1;
        return n;
    }

    public Set<String> getTonalidades() {
        Set<String> t = new LinkedHashSet<>();
        for (VerticeMusical v : caminho) t.add(v.getCampo().getNomeExtenso());
        return t;
    }

    /** Campo harmônico principal (o da tonalidade de partida). */
    public CampoHarmonico getCampoPrincipal() {
        return caminho.get(0).getCampo();
    }

    /** "Mi menor – Dó – Sol – Ré" */
    public String getNomes() {
        List<String> n = new ArrayList<>();
        for (Etapa e : etapas) n.add(e.acorde.getNomeCurto());
        return String.join(" – ", n);
    }

    /** "Em – C – G – D" */
    public String getCifras() {
        List<String> n = new ArrayList<>();
        for (Etapa e : etapas) n.add(e.acorde.getCifra());
        return String.join(" – ", n);
    }

    /** "6º – 4º – 1º – 5º" */
    public String getGrausOrdinais() {
        List<String> n = new ArrayList<>();
        for (Etapa e : etapas) n.add(e.getGrauOrdinal());
        return String.join(" – ", n);
    }

    /** "vi–IV–I–V" (só faz sentido quando não há modulação). */
    public String getPadraoGraus() {
        List<String> n = new ArrayList<>();
        for (Etapa e : etapas) n.add(e.getGrau());
        return String.join("–", n);
    }

    public String getFluxoFuncional() {
        List<String> n = new ArrayList<>();
        for (Etapa e : etapas) {
            VerticeMusical v = e.vertices.get(e.vertices.size() - 1);
            n.add(v.getFuncao().getNome());
        }
        return String.join(" → ", n);
    }
}
