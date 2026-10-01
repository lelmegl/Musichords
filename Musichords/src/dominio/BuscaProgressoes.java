import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Mapeamento dos papéis funcionais de um acorde (RF01) e busca de progressões de menor
 * custo sobre o grafo de campos harmônicos (RF02 / RF06).
 *  - origens(acorde): todos os vértices que representam o acorde físico tocado;
 *  - buscarProgressoes: percorre o grafo a partir das origens e gera todos os caminhos com N
 *    acordes, ordenados pela soma dos pesos (custo físico). As arestas de pivô (peso 0) permitem
 *    trocar de tonalidade sem trocar de acorde;
 *  - caminhoMinimo: Dijkstra multi-origem até um acorde de destino escolhido.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe (classe GrafoCamposHarmonicos do diagrama de classes).
 */
public class BuscaProgressoes {

    private final Grafo grafo;
    private final Map<Integer, VerticeMusical> musicais = new LinkedHashMap<>();

    public static final Comparator<Progressao> ORDEM_FACILIDADE = Comparator
            .comparingInt(Progressao::getCustoTotal)
            .thenComparingInt(Progressao::getDificuldadeShapes)
            .thenComparingInt(Progressao::getModulacoes)
            .thenComparing(Progressao::getCifras);

    public BuscaProgressoes(Grafo grafo) {
        this.grafo = grafo;
        for (Vertice v : grafo.getVertices()) {
            VerticeMusical vm = VerticeMusical.interpretar(v);
            if (vm != null) musicais.put(v.getId(), vm);
        }
    }

    public VerticeMusical musical(int id) {
        return musicais.get(id);
    }

    /** Vértices do grafo que representam o acorde (o mesmo acorde físico em várias tonalidades). */
    public List<VerticeMusical> origens(Acorde acorde) {
        Acorde alvo = acorde.triade();
        List<VerticeMusical> lista = new ArrayList<>();
        for (VerticeMusical vm : musicais.values()) {
            if (vm.getAcorde().equals(alvo)) lista.add(vm);
        }
        return lista;
    }

    /** Acordes distintos presentes no grafo, ordenados pela cifra. */
    public List<Acorde> acordesDoGrafo() {
        Map<Acorde, Acorde> unicos = new LinkedHashMap<>();
        for (VerticeMusical vm : musicais.values()) unicos.putIfAbsent(vm.getAcorde(), vm.getAcorde());
        List<Acorde> lista = new ArrayList<>(unicos.values());
        lista.sort(Comparator.comparingInt((Acorde a) -> a.getTipo().ordinal()).thenComparingInt(Acorde::getRaiz));
        return lista;
    }

    /**
     * Gera as progressões com {@code tamanho} acordes que começam no acorde informado.
     *
     * @param permitirModulacao se true, aceita uma aresta de pivô (peso 0) no meio do caminho
     * @param limite            quantidade máxima de progressões retornadas
     */
    public List<Progressao> buscarProgressoes(Acorde inicial, int tamanho, boolean permitirModulacao, int limite) {
        Map<String, Progressao> melhores = new LinkedHashMap<>();
        for (VerticeMusical origem : origens(inicial)) {
            List<VerticeMusical> caminho = new ArrayList<>();
            List<Integer> pesos = new ArrayList<>();
            caminho.add(origem);
            Set<Acorde> usados = new HashSet<>();
            usados.add(origem.getAcorde());
            dfs(origem, caminho, pesos, usados, 1, 0, tamanho, permitirModulacao, melhores);
        }
        TreeSet<Progressao> ordenadas = new TreeSet<>(ORDEM_FACILIDADE);
        ordenadas.addAll(melhores.values());
        List<Progressao> resultado = new ArrayList<>();
        for (Progressao p : ordenadas) {
            if (resultado.size() >= limite) break;
            resultado.add(p);
        }
        return resultado;
    }

    private void dfs(VerticeMusical atual, List<VerticeMusical> caminho, List<Integer> pesos, Set<Acorde> usados,
                     int acordes, int pivos, int tamanho, boolean permitirModulacao, Map<String, Progressao> melhores) {
        if (acordes == tamanho) {
            Progressao p = new Progressao(caminho, pesos);
            String chave = p.getCifras();
            Progressao existente = melhores.get(chave);
            if (existente == null || ORDEM_FACILIDADE.compare(p, existente) < 0) melhores.put(chave, p);
            return;
        }
        Vertice v = grafo.getVerticeById(atual.getId());
        if (v == null) return;
        boolean ultimoFoiPivo = caminho.size() >= 2
                && caminho.get(caminho.size() - 2).getAcorde().equals(atual.getAcorde());

        for (Aresta a : v.getAdjacencias()) {
            VerticeMusical prox = musicais.get(a.getDestinoId());
            if (prox == null) continue;
            boolean pivo = prox.getAcorde().equals(atual.getAcorde());
            if (pivo) {
                // Pivô: mesmo acorde, outra tonalidade. Não conta como novo acorde.
                if (!permitirModulacao || pivos >= 1 || ultimoFoiPivo || caminho.size() == 1) continue;
                caminho.add(prox);
                pesos.add(a.getPeso());
                dfs(prox, caminho, pesos, usados, acordes, pivos + 1, tamanho, permitirModulacao, melhores);
                caminho.remove(caminho.size() - 1);
                pesos.remove(pesos.size() - 1);
                continue;
            }
            Acorde acorde = prox.getAcorde();
            boolean voltaAoInicio = acordes == tamanho - 1 && acorde.equals(caminho.get(0).getAcorde());
            if (usados.contains(acorde) && !voltaAoInicio) continue;
            caminho.add(prox);
            pesos.add(a.getPeso());
            boolean novo = usados.add(acorde);
            dfs(prox, caminho, pesos, usados, acordes + 1, pivos, tamanho, permitirModulacao, melhores);
            if (novo) usados.remove(acorde);
            caminho.remove(caminho.size() - 1);
            pesos.remove(pesos.size() - 1);
        }
    }

    /**
     * Caminho de menor custo (Dijkstra multi-origem) do acorde de partida até qualquer vértice que
     * represente o acorde de destino. Retorna null se o destino for inalcançável.
     */
    public Progressao caminhoMinimo(Acorde partida, Acorde destino) {
        List<Integer> ids = new ArrayList<>();
        for (VerticeMusical vm : origens(partida)) ids.add(vm.getId());
        if (ids.isEmpty()) return null;
        Grafo.ResultadoDijkstra r = grafo.dijkstra(ids);
        VerticeMusical melhor = null;
        for (VerticeMusical alvo : origens(destino)) {
            if (!r.alcanca(alvo.getId())) continue;
            if (melhor == null || r.distancia(alvo.getId()) < r.distancia(melhor.getId())) melhor = alvo;
        }
        if (melhor == null) return null;
        List<Integer> idsCaminho = r.caminhoAte(melhor.getId());
        List<VerticeMusical> caminho = new ArrayList<>();
        List<Integer> pesos = new ArrayList<>();
        for (int i = 0; i < idsCaminho.size(); i++) {
            VerticeMusical vm = musicais.get(idsCaminho.get(i));
            if (vm == null) return null;
            caminho.add(vm);
            if (i > 0) pesos.add(peso(idsCaminho.get(i - 1), idsCaminho.get(i)));
        }
        return new Progressao(caminho, pesos);
    }

    private int peso(int origem, int destino) {
        Vertice v = grafo.getVerticeById(origem);
        if (v != null) {
            for (Aresta a : v.getAdjacencias()) {
                if (a.getDestinoId() == destino) return a.getPeso();
            }
        }
        return 0;
    }
}
