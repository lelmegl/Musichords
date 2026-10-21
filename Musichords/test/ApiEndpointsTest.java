import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
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
 * Síntese: Testes de integração dos endpoints /api/* do servidor backend. Cada teste sobe o
 * servidor HTTP real numa porta livre, com uma cópia do grafo.txt, e confere status HTTP,
 * tipo do conteúdo e os campos do JSON devolvido.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 20/10/2026 | Pedro | Criação dos testes.
 */
public class ApiEndpointsTest {

    private static final int VERTICES_GRAFO = 84;
    private static final int ARESTAS_GRAFO = 252;

    @Rule
    public TemporaryFolder pasta = new TemporaryFolder();

    private ServidorDeTeste s;

    @Before
    public void subirServidor() throws Exception {
        s = ServidorDeTeste.iniciar(pasta.getRoot());
    }

    @After
    public void derrubarServidor() {
        if (s != null) s.close();
    }

    /** GET que exige HTTP 200 + JSON e devolve o corpo já lido. */
    private NoJson getOk(String caminho) throws Exception {
        ServidorDeTeste.Resposta r = s.get(caminho);
        assertEquals("status de " + caminho + " -> " + r, 200, r.status);
        assertTrue("Content-Type deveria ser JSON: " + r.tipo, r.tipo.startsWith("application/json"));
        return r.json();
    }

    private NoJson operacao(String... parametros) throws Exception {
        ServidorDeTeste.Resposta r = s.post("/api/operacao", parametros);
        assertEquals("status da operação -> " + r, 200, r.status);
        return r.json();
    }

    // ================================================================== GET /api/estado

    @Test
    public void estadoInformaTamanhoDoGrafoCarregado() throws Exception {
        NoJson r = getOk("/api/estado");
        assertEquals(VERTICES_GRAFO, r.get("vertices").inteiro());
        assertEquals(ARESTAS_GRAFO, r.get("arestas").inteiro());
        assertTrue(r.get("arquivo").texto().endsWith("grafo.txt"));
    }

    @Test
    public void estadoDevolveBracoDaGuitarraComSeisCordasETrezeCasas() throws Exception {
        NoJson r = getOk("/api/estado");
        NoJson braco = r.get("braco");
        assertEquals(6, braco.tamanho());
        for (NoJson corda : braco.itens()) assertEquals(13, corda.tamanho());
        assertEquals("E", braco.get(0).get(0).texto());   // Mi grave solta
        assertEquals("G", braco.get(0).get(3).texto());   // Mi grave, casa 3
        assertEquals("E", braco.get(0).get(12).texto());  // casa 12 = oitava
        assertEquals("Bb", r.get("bracoBemol").get(1).get(1).texto());
        assertEquals(Arrays.asList(40, 45, 50, 55, 59, 64), r.get("midiCordas").inteiros());
    }

    // ================================================================== GET /api/grafo

    @Test
    public void grafoDevolveVerticesComInterpretacaoMusical() throws Exception {
        NoJson r = getOk("/api/grafo");
        assertEquals(VERTICES_GRAFO, r.get("vertices").tamanho());
        assertEquals(ARESTAS_GRAFO, r.get("arestas").inteiro());

        NoJson cI = r.get("vertices").get(0);
        assertEquals(0, cI.get("id").inteiro());
        assertEquals("C_I", cI.get("rotulo").texto());
        assertEquals("C", cI.get("cifra").texto());
        assertEquals("Tônica", cI.get("funcao").texto());

        NoJson cV = r.get("vertices").get(4);
        assertEquals("C_V", cV.get("rotulo").texto());
        assertEquals("G", cV.get("cifra").texto());
        assertEquals("Dominante", cV.get("funcao").texto());
    }

    @Test
    public void somaDasAdjacenciasDoGrafoBateComTotalDeArestas() throws Exception {
        NoJson r = getOk("/api/grafo");
        int total = 0;
        for (NoJson v : r.get("vertices").itens()) total += v.get("adjacencias").tamanho();
        assertEquals(r.get("arestas").inteiro(), total);
    }

    // ================================================================== GET /api/identificar

    @Test
    public void identificaDoMaiorNoShapeAberto() throws Exception {
        NoJson r = getOk("/api/identificar?casas=-1,3,2,0,1,0");
        assertEquals("C", r.get("acorde").get("cifra").texto());
        assertEquals("MAIOR", r.get("acorde").get("tipo").texto());
        assertEquals(3, r.get("quantidade").inteiro());
        assertFalse(r.get("tetrade").logico());
        assertFalse(r.get("invertido").logico());
        assertEquals("x32010", r.get("shape").get("tablatura").texto());

        // Dó maior aparece como vértice em vários campos harmônicos; o C_I (id 0) é um deles
        boolean temCI = false;
        for (NoJson o : r.get("origens").itens()) temCI |= o.get("rotulo").texto().equals("C_I");
        assertTrue("C_I deveria estar entre as origens: " + r.get("origens"), temCI);
    }

    @Test
    public void identificaLaMenor() throws Exception {
        NoJson r = getOk("/api/identificar?casas=-1,0,2,2,1,0");
        assertEquals("Am", r.get("acorde").get("cifra").texto());
        assertEquals("MENOR", r.get("acorde").get("tipo").texto());
    }

    @Test
    public void identificaTetradeEReduzParaTriadeDoGrafo() throws Exception {
        NoJson r = getOk("/api/identificar?casas=3,2,0,0,0,1"); // G7
        assertEquals("G7", r.get("acorde").get("cifra").texto());
        assertTrue(r.get("tetrade").logico());
        assertEquals("G", r.get("triade").get("cifra").texto());
    }

    @Test
    public void identificaInversaoPeloBaixo() throws Exception {
        NoJson r = getOk("/api/identificar?casas=0,3,2,0,1,0"); // C/E
        assertEquals("C", r.get("acorde").get("cifra").texto());
        assertTrue(r.get("invertido").logico());
        assertEquals("E", r.get("baixo").texto());
    }

    @Test
    public void semNotasSuficientesNaoIdentificaAcorde() throws Exception {
        NoJson r = getOk("/api/identificar?casas=-1,-1,-1,-1,-1,-1");
        assertTrue(r.get("acorde").nulo());
        assertEquals(0, r.get("quantidade").inteiro());
    }

    @Test
    public void identificarComCasaNaoNumericaRetorna400() throws Exception {
        ServidorDeTeste.Resposta r = s.get("/api/identificar?casas=a,b");
        assertEquals(400, r.status);
        assertFalse(r.json().get("ok").logico());
    }

    // ================================================================== GET /api/progressoes

    @Test
    public void progressoesComecamNoAcordeEscolhidoEVemOrdenadasPeloCusto() throws Exception {
        NoJson r = getOk("/api/progressoes?raiz=0&tipo=MAIOR&tamanho=4&modulacao=false");
        List<NoJson> lista = r.get("lista").itens();
        assertFalse("deveria haver progressões a partir de C", lista.isEmpty());
        assertTrue("no máximo 10 progressões", lista.size() <= 10);

        int custoAnterior = Integer.MIN_VALUE;
        for (NoJson p : lista) {
            assertEquals(4, p.get("etapas").tamanho());
            assertEquals("C", p.get("etapas").get(0).get("acorde").get("cifra").texto());
            assertEquals("sem modulação pedida", 0, p.get("modulacoes").inteiro());
            int custo = p.get("custo").inteiro();
            assertTrue("lista deve vir em ordem crescente de custo", custo >= custoAnterior);
            custoAnterior = custo;
        }
    }

    @Test
    public void custoDaProgressaoEhASomaDosPesosDasArestas() throws Exception {
        NoJson r = getOk("/api/progressoes?raiz=9&tipo=MENOR&tamanho=3&modulacao=false");
        for (NoJson p : r.get("lista").itens()) {
            int soma = 0;
            for (NoJson etapa : p.get("etapas").itens()) soma += etapa.get("peso").inteiro();
            assertEquals("custo de " + p.get("cifras").texto(), soma, p.get("custo").inteiro());
        }
    }

    @Test
    public void progressoesComTipoDeAcordeInvalidoRetornam400() throws Exception {
        assertEquals(400, s.get("/api/progressoes?raiz=0&tipo=FOO&tamanho=3").status);
        assertEquals(400, s.get("/api/progressoes?raiz=0&tamanho=3").status);       // tipo ausente
        assertEquals(400, s.get("/api/progressoes?raiz=x&tipo=MAIOR&tamanho=3").status);
    }

    // ================================================================== GET /api/caminho

    @Test
    public void caminhoMinimoDeDoAteSol() throws Exception {
        NoJson p = getOk("/api/caminho?raiz=0&tipo=MAIOR&destRaiz=7&destTipo=MAIOR").get("progressao");
        assertEquals(4, p.get("custo").inteiro());
        assertEquals(Arrays.asList(0, 4), p.get("caminho").inteiros()); // C_I -> C_V
        assertEquals("C – G", p.get("cifras").texto());
    }

    @Test
    public void caminhoParaAcordeQueSaiuDoGrafoInformaErro() throws Exception {
        String bdim = "/api/caminho?raiz=0&tipo=MAIOR&destRaiz=11&destTipo=DIMINUTO";
        assertTrue(getOk(bdim).tem("progressao"));
        // Bdim só existe como vii de Dó maior (vértice 6, C_vii); sem ele não há caminho
        assertTrue(operacao("op", "removerVertice", "id", "6").get("ok").logico());
        NoJson r = getOk(bdim);
        assertTrue("deveria informar erro: " + r, r.tem("erro"));
        assertFalse(r.tem("progressao"));
    }

    // ================================================================== GET /api/acordes e /api/exemplos

    @Test
    public void acordesListaOsAcordesDoGrafoSemRepeticao() throws Exception {
        List<NoJson> lista = getOk("/api/acordes").get("lista").itens();
        assertFalse(lista.isEmpty());
        java.util.Set<String> cifras = new java.util.HashSet<>();
        for (NoJson a : lista) assertTrue("acorde repetido", cifras.add(a.get("cifra").texto()));
        assertTrue(cifras.containsAll(Arrays.asList("C", "G", "Am", "Em", "Bdim")));
    }

    @Test
    public void exemplosTrazemSeteShapesParaOBraco() throws Exception {
        List<NoJson> lista = getOk("/api/exemplos").get("lista").itens();
        assertEquals(7, lista.size());
        for (NoJson e : lista) assertEquals(6, e.get("casas").tamanho());
    }

    // ================================================================== GET /api/texto (opções g, h, i)

    @Test
    public void textoMostraConteudoListaEConexidade() throws Exception {
        assertTrue(getOk("/api/texto?tipo=arquivo").get("texto").texto().contains("Número de Vértices: " + VERTICES_GRAFO));
        assertTrue(getOk("/api/texto?tipo=lista").get("texto").texto().contains("LISTA DE ADJACÊNCIAS"));
        assertTrue(getOk("/api/texto?tipo=conexidade").get("texto").texto().contains("C3"));
        assertEquals("", getOk("/api/texto?tipo=outro").get("texto").texto());
    }

    // ================================================================== GET /api/dijkstra

    @Test
    public void dijkstraEntreDoisVertices() throws Exception {
        NoJson r = getOk("/api/dijkstra?origem=0&destino=4");
        assertTrue(r.get("ok").logico());
        assertEquals(4, r.get("custo").inteiro());
        assertEquals(Arrays.asList(0, 4), r.get("caminho").inteiros());
    }

    @Test
    public void dijkstraComVerticeInexistenteOuInvalido() throws Exception {
        assertFalse(getOk("/api/dijkstra?origem=0&destino=999").get("ok").logico());
        assertFalse(getOk("/api/dijkstra?origem=abc&destino=4").get("ok").logico());
    }

    // ================================================================== POST /api/operacao (opções a..f)

    @Test
    public void inserirVerticeAumentaOGrafoERecusaIdRepetido() throws Exception {
        assertTrue(operacao("op", "inserirVertice", "id", "100", "rotulo", "Teste").get("ok").logico());
        assertEquals(VERTICES_GRAFO + 1, getOk("/api/estado").get("vertices").inteiro());

        NoJson repetido = operacao("op", "inserirVertice", "id", "100", "rotulo", "Outro");
        assertFalse(repetido.get("ok").logico());
        assertFalse(operacao("op", "inserirVertice", "id", "101", "rotulo", "  ").get("ok").logico());
        assertFalse(operacao("op", "inserirVertice", "id", "abc", "rotulo", "X").get("ok").logico());
    }

    @Test
    public void inserirArestaValidaPesoEVertices() throws Exception {
        assertTrue(operacao("op", "inserirAresta", "origem", "0", "destino", "6", "peso", "1").get("ok").logico());
        assertEquals(ARESTAS_GRAFO + 1, getOk("/api/estado").get("arestas").inteiro());

        assertFalse("peso negativo", operacao("op", "inserirAresta", "origem", "0", "destino", "6", "peso", "-2").get("ok").logico());
        assertFalse("vértice inexistente", operacao("op", "inserirAresta", "origem", "0", "destino", "999", "peso", "1").get("ok").logico());
        assertEquals(ARESTAS_GRAFO + 1, getOk("/api/estado").get("arestas").inteiro());
    }

    @Test
    public void novaArestaMaisBarataMudaOCaminhoMinimo() throws Exception {
        // sem atalho, C_I(0) -> C_vii(6) custa mais que 1
        assertNotEquals(1, getOk("/api/dijkstra?origem=0&destino=6").get("custo").inteiro());
        operacao("op", "inserirAresta", "origem", "0", "destino", "6", "peso", "1");
        NoJson r = getOk("/api/dijkstra?origem=0&destino=6");
        assertEquals(1, r.get("custo").inteiro());
        assertEquals(Arrays.asList(0, 6), r.get("caminho").inteiros());
    }

    @Test
    public void removerArestaAlteraOCaminhoMinimo() throws Exception {
        assertTrue(operacao("op", "removerAresta", "origem", "0", "destino", "4").get("ok").logico());
        assertEquals(ARESTAS_GRAFO - 1, getOk("/api/estado").get("arestas").inteiro());

        NoJson r = getOk("/api/dijkstra?origem=0&destino=4");
        assertTrue("sem a aresta direta o caminho fica mais caro", r.get("custo").inteiro() > 4);
        assertFalse("remover de novo deve falhar", operacao("op", "removerAresta", "origem", "0", "destino", "4").get("ok").logico());
    }

    @Test
    public void removerVerticeApagaTambemAsArestasQueChegavamNele() throws Exception {
        assertTrue(operacao("op", "removerVertice", "id", "4").get("ok").logico());
        NoJson grafo = getOk("/api/grafo");
        assertEquals(VERTICES_GRAFO - 1, grafo.get("vertices").tamanho());
        for (NoJson v : grafo.get("vertices").itens()) {
            for (NoJson a : v.get("adjacencias").itens()) assertNotEquals(4, a.get("destino").inteiro());
        }
        assertFalse(operacao("op", "removerVertice", "id", "4").get("ok").logico());
    }

    @Test
    public void gravarERelerOArquivoPreservaOGrafo() throws Exception {
        operacao("op", "inserirVertice", "id", "100", "rotulo", "Novo");
        operacao("op", "inserirAresta", "origem", "100", "destino", "0", "peso", "2");
        File destino = new File(pasta.getRoot(), "gravado.txt");
        assertTrue(operacao("op", "gravar", "caminho", destino.getPath()).get("ok").logico());
        assertTrue(destino.isFile());

        // volta ao grafo original e depois lê o arquivo gravado
        assertTrue(operacao("op", "ler", "caminho", s.grafo.getPath()).get("ok").logico());
        assertEquals(VERTICES_GRAFO, getOk("/api/estado").get("vertices").inteiro());
        assertTrue(operacao("op", "ler", "caminho", destino.getPath()).get("ok").logico());
        NoJson estado = getOk("/api/estado");
        assertEquals(VERTICES_GRAFO + 1, estado.get("vertices").inteiro());
        assertEquals(ARESTAS_GRAFO + 1, estado.get("arestas").inteiro());
    }

    @Test
    public void lerArquivoInexistenteInformaErro() throws Exception {
        NoJson r = operacao("op", "ler", "caminho", new File(pasta.getRoot(), "nao-existe.txt").getPath());
        assertFalse(r.get("ok").logico());
        assertTrue(r.get("mensagem").texto().contains("não encontrado"));
    }

    @Test
    public void operacaoDesconhecidaInformaErro() throws Exception {
        NoJson r = operacao("op", "explodir");
        assertFalse(r.get("ok").logico());
        assertTrue(r.get("mensagem").texto().contains("Operação desconhecida"));
    }

    // ================================================================== rotas e arquivos estáticos

    @Test
    public void rotaDaApiInexistenteRetorna404() throws Exception {
        ServidorDeTeste.Resposta r = s.get("/api/nao-existe");
        assertEquals(404, r.status);
        assertFalse(r.json().get("ok").logico());
    }

    @Test
    public void raizEntregaAInterfaceHtml() throws Exception {
        ServidorDeTeste.Resposta r = s.get("/");
        assertEquals(200, r.status);
        assertTrue(r.tipo.startsWith("text/html"));
        assertTrue(r.corpo.toLowerCase().contains("<html"));
        assertEquals(200, s.get("/app.js").status);
        assertEquals(200, s.get("/estilo.css").status);
    }

    @Test
    public void arquivoEstaticoInexistenteRetorna404() throws Exception {
        assertEquals(404, s.get("/nao-existe.js").status);
    }
}
