import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Testes de unidade da classe Grafo (back-end da Parte 1): inserção e remoção de
 * vértices/arestas, leitura e gravação do arquivo e caminho mínimo (Dijkstra).
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 20/10/2026 | Pedro | Criação dos testes.
 */
public class GrafoTest {

    @Rule
    public TemporaryFolder pasta = new TemporaryFolder();

    private Grafo g;

    /**
     * Grafo pequeno, calculado à mão:
     *   1 -> 2 (5)    1 -> 3 (1)    3 -> 2 (1)    2 -> 4 (2)    4 (sem saída)    5 (isolado)
     * Caminho mínimo 1 -> 4 = 1 -> 3 -> 2 -> 4, custo 4 (a aresta direta 1 -> 2 custa 5).
     */
    @Before
    public void montarGrafo() {
        g = new Grafo();
        for (int id = 1; id <= 5; id++) g.inserirVertice(id, "V" + id);
        g.inserirAresta(1, 2, 5);
        g.inserirAresta(1, 3, 1);
        g.inserirAresta(3, 2, 1);
        g.inserirAresta(2, 4, 2);
    }

    private static int arestas(Grafo grafo) {
        int total = 0;
        for (Vertice v : grafo.getVertices()) total += v.getAdjacencias().size();
        return total;
    }

    @Test
    public void inserirVerticesEArestas() {
        assertEquals(5, g.getVertices().size());
        assertEquals(4, arestas(g));
        assertEquals("V3", g.getVerticeById(3).getRotulo());
        assertNull(g.getVerticeById(99));
    }

    @Test
    public void removerArestaExistenteEInexistente() {
        String saida = ApiMusichords.capturar(() -> g.removerAresta(1, 2));
        assertTrue(saida, saida.contains("removida"));
        assertEquals(3, arestas(g));

        String erro = ApiMusichords.capturar(() -> g.removerAresta(1, 2));
        assertTrue(erro, erro.startsWith("Erro"));
        assertEquals(3, arestas(g));
    }

    @Test
    public void removerVerticeApagaArestasQueApontavamParaEle() {
        ApiMusichords.capturar(() -> g.removerVertice(2));
        assertNull(g.getVerticeById(2));
        assertEquals(4, g.getVertices().size());
        assertEquals("sobra apenas 1 -> 3", 1, arestas(g));
    }

    @Test
    public void dijkstraEscolheOCaminhoMaisBaratoENaoOMaisCurto() {
        Grafo.ResultadoDijkstra r = g.dijkstra(1);
        assertEquals(0, r.distancia(1));
        assertEquals(2, r.distancia(2));
        assertEquals(4, r.distancia(4));
        assertEquals(Arrays.asList(1, 3, 2, 4), r.caminhoAte(4));
    }

    @Test
    public void dijkstraIdentificaVerticeInalcancavel() {
        Grafo.ResultadoDijkstra r = g.dijkstra(1);
        assertFalse(r.alcanca(5));
        assertEquals(Integer.MAX_VALUE, r.distancia(5));
        assertTrue(r.caminhoAte(5).isEmpty());
        assertFalse("o grafo é direcionado: 4 não volta para 1", g.dijkstra(4).alcanca(1));
    }

    @Test
    public void dijkstraComVariasOrigensUsaAMaisProxima() {
        Grafo.ResultadoDijkstra r = g.dijkstra(Arrays.asList(1, 2));
        assertEquals(0, r.distancia(2));
        assertEquals(2, r.distancia(4));
        assertEquals(Arrays.asList(2, 4), r.caminhoAte(4));
    }

    @Test
    public void dijkstraIgnoraOrigemInexistente() {
        assertFalse(g.dijkstra(Collections.singletonList(99)).alcanca(1));
    }

    @Test
    public void gravarELerArquivoPreservaVerticesArestasEPesos() throws Exception {
        File arquivo = new File(pasta.getRoot(), "grafo-teste.txt");
        String saida = ApiMusichords.capturar(() -> g.gravarArquivo(arquivo.getPath()));
        assertTrue(saida, saida.contains("sucesso"));

        Grafo lido = new Grafo();
        ApiMusichords.capturar(() -> lido.lerArquivo(arquivo.getPath()));
        assertEquals(5, lido.getVertices().size());
        assertEquals(4, arestas(lido));
        assertEquals("V4", lido.getVerticeById(4).getRotulo());
        assertEquals(4, lido.dijkstra(1).distancia(4));
    }

    @Test
    public void lerOGrafoDoProjeto() {
        Grafo projeto = new Grafo();
        String saida = ApiMusichords.capturar(() -> projeto.lerArquivo("grafo.txt"));
        assertTrue(saida, saida.contains("Arquivo lido"));
        assertEquals(84, projeto.getVertices().size());
        assertEquals(252, arestas(projeto));
        assertNotNull(projeto.getVerticeById(0));
        assertEquals("C_I", projeto.getVerticeById(0).getRotulo());
    }

    @Test
    public void grafoDoProjetoEhFortementeConexo() {
        Grafo projeto = new Grafo();
        ApiMusichords.capturar(() -> projeto.lerArquivo("grafo.txt"));
        String saida = ApiMusichords.capturar(projeto::analisarConexidade);
        assertTrue(saida, saida.contains("C3"));
        // consequência: de qualquer vértice se chega a todos os outros
        Grafo.ResultadoDijkstra r = projeto.dijkstra(0);
        for (Vertice v : projeto.getVertices()) assertTrue("0 alcança " + v.getId(), r.alcanca(v.getId()));
    }
}
