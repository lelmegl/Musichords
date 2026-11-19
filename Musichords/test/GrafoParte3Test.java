import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Testes das opções da Parte 3 (k a o do menu): caminho de menor esforço (Dijkstra),
 * graus e caminho euleriano, ciclo hamiltoniano, Floyd e Caixeiro Viajante, coloração por classe
 * (Welsh e Powell) e árvore geradora mínima (Kruskal e Prim). Cada técnica é testada primeiro em
 * grafos pequenos com resultado calculado à mão e depois no grafo do projeto (grafo.txt).
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 13/11/2026 | Pedro | Criação dos testes da Parte 3.
 * 13/11/2026 | Pedro | Testes dos percursos em profundidade e largura com o grafo G10 do slide;
 *                    | verificação do K4 no lugar da busca de clique.
 */
public class GrafoParte3Test {

    // ================================================================== grafos de apoio

    /** Monta um grafo com vértices 1..n (rótulos V1..Vn) e arestas {origem, destino, peso}. */
    private static Grafo grafo(int n, int[]... arestas) {
        Grafo g = new Grafo();
        ApiMusichords.capturar(() -> {
            for (int id = 1; id <= n; id++) g.inserirVertice(id, "V" + id);
            for (int[] a : arestas) g.inserirAresta(a[0], a[1], a[2]);
        });
        return g;
    }

    /** Mesmo grafo, mas com cada aresta nos dois sentidos (grafo "não direcionado"). */
    private static Grafo grafoNaoDirecionado(int n, int[]... arestas) {
        int[][] dobradas = new int[arestas.length * 2][];
        for (int i = 0; i < arestas.length; i++) {
            dobradas[2 * i] = arestas[i];
            dobradas[2 * i + 1] = new int[]{arestas[i][1], arestas[i][0], arestas[i][2]};
        }
        return grafo(n, dobradas);
    }

    private static Grafo projeto() {
        Grafo g = new Grafo();
        ApiMusichords.capturar(() -> g.lerArquivo("grafo.txt"));
        return g;
    }

    private static int id(Grafo g, String rotulo) {
        Vertice v = g.getVerticeByRotulo(rotulo);
        assertNotNull("vértice " + rotulo, v);
        return v.getId();
    }

    // ================================================================== percursos (slides de busca)

    /** Grafo G10 do slide (a..h = 1..8), com a matriz de adjacência A10. */
    private static java.util.Map<Integer, List<Integer>> g10() {
        java.util.Map<Integer, List<Integer>> adj = new java.util.LinkedHashMap<>();
        adj.put(1, Arrays.asList(2, 3, 5));   // a -> b, c, e
        adj.put(2, Arrays.asList(4, 5));      // b -> d, e
        adj.put(3, Arrays.asList(6, 7));      // c -> f, g
        adj.put(4, Arrays.asList(8));         // d -> h
        adj.put(5, Arrays.asList(8));         // e -> h
        adj.put(6, Arrays.asList(5, 7));      // f -> e, g
        adj.put(7, Arrays.asList(8));         // g -> h
        adj.put(8, Arrays.<Integer>asList()); // h
        return adj;
    }

    @Test
    public void percursoEmProfundidadeDoSlide() {
        // P10 = (a, b, d, h, e, c, f, g)
        assertEquals(Arrays.asList(1, 2, 4, 8, 5, 3, 6, 7), Grafo.percursoProfundidade(1, g10()));
    }

    @Test
    public void percursoEmLarguraDoSlide() {
        // P10 = (a, b, c, e, d, f, g, h)
        assertEquals(Arrays.asList(1, 2, 3, 5, 4, 6, 7, 8), Grafo.percursoLargura(1, g10()));
    }

    // ================================================================== k) Dijkstra

    @Test
    public void menorEsforcoMostraCaminhoPassosECusto() {
        // 1 -> 2 custa 5 direto, mas 1 -> 3 -> 2 custa 2; até 4: 1 -> 3 -> 2 -> 4 = 4
        Grafo g = grafo(4, new int[]{1, 2, 5}, new int[]{1, 3, 1}, new int[]{3, 2, 1}, new int[]{2, 4, 2});
        String saida = ApiMusichords.capturar(() -> g.mostrarMenorEsforco(1, 4));
        assertTrue(saida, saida.contains("Caminho: V1 -> V3 -> V2 -> V4"));
        assertTrue(saida, saida.contains("Custo total: 4"));
    }

    @Test
    public void menorEsforcoSemCaminhoOuVerticeInexistente() {
        Grafo g = grafo(3, new int[]{1, 2, 1});
        assertTrue(ApiMusichords.capturar(() -> g.mostrarMenorEsforco(2, 1)).contains("Não existe caminho"));
        assertTrue(ApiMusichords.capturar(() -> g.mostrarMenorEsforco(1, 99)).startsWith("Erro"));
    }

    @Test
    public void menorEsforcoDeSolParaDoNoProjeto() {
        Grafo g = projeto();
        int sol = id(g, "G_I"), dor = id(g, "C_I");
        assertEquals(5, g.dijkstra(sol).distancia(dor));
        String saida = ApiMusichords.capturar(() -> g.mostrarMenorEsforco(sol, dor));
        assertTrue(saida, saida.contains("G_I -> C_V -> C_I"));
        assertTrue(saida, saida.contains("pivô"));
    }

    @Test
    public void buscaDeVerticePeloRotuloIgnoraMaiusculas() {
        Grafo g = projeto();
        assertEquals(0, g.getVerticeByRotulo("c_i").getId());
        assertEquals(null, g.getVerticeByRotulo("X_9"));
    }

    // ================================================================== l) graus e Euler

    @Test
    public void cicloDirecionadoEhEuleriano() {
        // 1 -> 2 -> 3 -> 4 -> 1: todo vértice tem grau 2 (par)
        Grafo g = grafo(4, new int[]{1, 2, 1}, new int[]{2, 3, 1}, new int[]{3, 4, 1}, new int[]{4, 1, 1});
        assertEquals(0, g.contarVerticesGrauImpar());
        assertTrue(g.temCaminhoEuleriano());
    }

    @Test
    public void doisVerticesImparesDaoCaminhoEulerianoAberto() {
        // ciclo + corda 1 -> 3: os vértices 1 e 3 passam a ter grau 3
        Grafo g = grafo(4, new int[]{1, 2, 1}, new int[]{2, 3, 1}, new int[]{3, 4, 1}, new int[]{4, 1, 1}, new int[]{1, 3, 1});
        assertEquals(2, g.contarVerticesGrauImpar());
        assertTrue(g.temCaminhoEuleriano());
    }

    @Test
    public void grafoDesconexoNaoTemCaminhoEuleriano() {
        // dois vértices ímpares, mas o vértice 3 está isolado
        Grafo g = grafo(3, new int[]{1, 2, 1});
        assertEquals(2, g.contarVerticesGrauImpar());
        assertFalse(g.temCaminhoEuleriano());
    }

    @Test
    public void projetoNaoTemCaminhoEuleriano() {
        Grafo g = projeto();
        assertEquals(48, g.contarVerticesGrauImpar());
        assertFalse(g.temCaminhoEuleriano());
        assertEquals(14, g.grauTotal(id(g, "C_I")));   // tônica: 7 entradas e 7 saídas
        assertEquals(2, g.grauTotal(id(g, "C_vii")));  // diminuto: 1 entrada e 1 saída
        String saida = ApiMusichords.capturar(g::analisarGrausEEuler);
        assertTrue(saida, saida.contains("NÃO possui caminho euleriano"));
    }

    // ================================================================== m) Hamilton, Floyd e Caixeiro Viajante

    @Test
    public void cicloDeQuatroVerticesEhHamiltoniano() {
        Grafo g = grafo(4, new int[]{1, 2, 1}, new int[]{2, 3, 1}, new int[]{3, 4, 1}, new int[]{4, 1, 1}, new int[]{1, 3, 1});
        List<Integer> ciclo = g.cicloHamiltoniano();
        assertEquals(Arrays.asList(1, 2, 3, 4), ciclo);
        assertTrue(g.ehCicloHamiltoniano(ciclo));
    }

    @Test
    public void estrelaNaoTemCicloHamiltoniano() {
        // centro 1 ligado a 2, 3 e 4 nos dois sentidos: para voltar ao centro é preciso repeti-lo
        Grafo g = grafoNaoDirecionado(4, new int[]{1, 2, 1}, new int[]{1, 3, 1}, new int[]{1, 4, 1});
        assertTrue(g.cicloHamiltoniano().isEmpty());
        assertFalse(g.ehCicloHamiltoniano(Arrays.asList(1, 2, 3, 4)));
    }

    @Test
    public void projetoTemCicloHamiltonianoVerificado() {
        Grafo g = projeto();
        List<Integer> ciclo = g.cicloHamiltoniano();
        assertEquals(84, ciclo.size());
        assertTrue(g.ehCicloHamiltoniano(ciclo));
    }

    @Test
    public void floydCalculaMenoresDistanciasEntreTodosOsPares() {
        Grafo g = grafo(4, new int[]{1, 2, 5}, new int[]{1, 3, 1}, new int[]{3, 2, 1}, new int[]{2, 4, 2});
        int[][] d = g.floyd();               // índices na ordem dos vértices: V1=0 ... V4=3
        assertEquals(0, d[0][0]);
        assertEquals(2, d[0][1]);            // 1 -> 3 -> 2
        assertEquals(4, d[0][3]);            // 1 -> 3 -> 2 -> 4
        assertEquals(Grafo.INF, d[3][0]);    // do 4 não se volta ao 1
    }

    @Test
    public void caixeiroViajanteVizinhoMaisProximo() {
        // quadrado com diagonais caras: a rota gulosa é 1-2-3-4-1, custo 4
        Grafo g = grafoNaoDirecionado(4, new int[]{1, 2, 1}, new int[]{2, 3, 1}, new int[]{3, 4, 1},
                new int[]{4, 1, 1}, new int[]{1, 3, 9}, new int[]{2, 4, 9});
        List<Integer> rota = g.rotaVizinhoMaisProximo(g.floyd());
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), rota);   // índices 0..3 e o custo 4 no fim
    }

    @Test
    public void projetoTeoremasInconclusivosERotaDoCaixeiroViajante() {
        Grafo g = projeto();
        List<Integer> rota = g.rotaVizinhoMaisProximo(g.floyd());
        assertEquals(85, rota.size());                 // 84 vértices + custo
        assertEquals(248, (int) rota.get(84));
        String saida = ApiMusichords.capturar(g::analisarHamilton);
        assertTrue(saida, saida.contains("Dirac): grau mínimo = 2"));
        assertTrue(saida, saida.contains("não adjacentes = 28"));
        assertTrue(saida, saida.contains("verificado: sim"));
    }

    // ================================================================== n) coloração

    @Test
    public void trianguloPrecisaDeTresCores() {
        Grafo g = grafo(3, new int[]{1, 2, 1}, new int[]{2, 3, 1}, new int[]{3, 1, 1});
        assertEquals(3, g.coloracaoPorClasse(true).size());
        assertTrue(g.ehClique(Arrays.asList(1, 2, 3)));
        assertFalse(g.ehBipartido());
    }

    @Test
    public void caminhoEhBipartidoComDuasCores() {
        Grafo g = grafo(4, new int[]{1, 2, 1}, new int[]{2, 3, 1}, new int[]{3, 4, 1});
        assertEquals(2, g.coloracaoPorClasse(true).size());
        assertTrue(g.ehBipartido());
    }

    @Test
    public void projetoTemNumeroCromaticoQuatro() {
        Grafo g = projeto();
        List<List<Integer>> classes = g.coloracaoPorClasse(true);
        assertEquals(4, classes.size());
        assertEquals(5, g.coloracaoPorClasse(false).size());   // efeito da rotulação
        List<Integer> k4 = g.cliqueDaProgressao();
        assertEquals(Arrays.asList(id(g, "C_I"), id(g, "C_ii"), id(g, "C_V"), id(g, "C_vi")), k4);
        assertTrue(g.ehClique(k4));
        assertFalse("ii e iii não são ligados", g.ehClique(Arrays.asList(id(g, "C_ii"), id(g, "C_iii"))));
        assertFalse(g.ehBipartido());
        // nenhuma aresta liga dois vértices da mesma cor
        for (List<Integer> classe : classes) {
            Set<Integer> mesmaCor = new HashSet<>(classe);
            for (int v : classe) {
                for (Aresta a : g.getVerticeById(v).getAdjacencias()) {
                    assertFalse("aresta dentro da mesma cor", mesmaCor.contains(a.getDestinoId()));
                }
            }
        }
    }

    // ================================================================== o) árvore geradora mínima

    @Test
    public void kruskalEPrimNoTriangulo() {
        // triângulo com pesos 1, 2 e 3: a árvore usa as arestas 1 e 2
        Grafo g = grafo(3, new int[]{1, 2, 1}, new int[]{2, 3, 2}, new int[]{3, 1, 3});
        List<int[]> arvore = g.kruskal();
        assertEquals(2, arvore.size());
        assertEquals(3, arvore.get(0)[2] + arvore.get(1)[2]);
        assertEquals(3, g.custoPrim());
    }

    @Test
    public void grafoDesconexoGeraFlorestaEPrimInforma() {
        Grafo g = grafo(4, new int[]{1, 2, 1}, new int[]{3, 4, 1});
        assertEquals(2, g.kruskal().size());
        assertEquals(-1, g.custoPrim());
    }

    @Test
    public void projetoArvoreGeradoraMinimaCusto149() {
        Grafo g = projeto();
        List<int[]> arvore = g.kruskal();
        int custo = 0, pivos = 0;
        for (int[] a : arvore) {
            custo += a[2];
            if (a[2] == 0) pivos++;
        }
        assertEquals(83, arvore.size());
        assertEquals(149, custo);
        assertEquals(48, pivos);
        assertEquals(149, g.custoPrim());
    }
}
