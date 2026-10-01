import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Camada de aplicação entre a interface (navegador) e o back-end. Mantém a instância
 * de Grafo carregada do grafo.txt e transforma os resultados do back-end e do domínio musical
 * em JSON. As opções a..i do menu reutilizam os próprios métodos do Grafo, capturando o texto
 * que eles imprimem, para que a interface mostre a mesma saída do console.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe.
 */
public class ApiMusichords {

    private final Grafo grafo = new Grafo();
    private BuscaProgressoes busca = new BuscaProgressoes(grafo);
    private String arquivo;

    public ApiMusichords(String arquivo) {
        this.arquivo = arquivo;
    }

    // ================================================================== carga

    /** Carrega o grafo.txt usando Grafo.lerArquivo (opção a). */
    public String carregar(String caminho) {
        String saida = capturar(() -> grafo.lerArquivo(caminho));
        arquivo = caminho;
        busca = new BuscaProgressoes(grafo);
        return saida;
    }

    private void grafoAlterado() {
        busca = new BuscaProgressoes(grafo);
    }

    // ================================================================== estado geral

    public Json.Obj estado() {
        // Nome (com sustenido e com bemol) e classe de altura de cada posição do braço
        List<Object> braco = Json.lista();
        List<Object> bracoBemol = Json.lista();
        List<Object> classes = Json.lista();
        for (int corda = 0; corda < 6; corda++) {
            List<Object> nomes = Json.lista();
            List<Object> nomesBemol = Json.lista();
            List<Object> cl = Json.lista();
            for (int casa = 0; casa <= 12; casa++) {
                int nota = Nota.notaNaCorda(corda, casa);
                nomes.add(Nota.nome(nota, false));
                nomesBemol.add(Nota.nome(nota, true));
                cl.add(nota);
            }
            braco.add(nomes);
            bracoBemol.add(nomesBemol);
            classes.add(cl);
        }
        return Json.obj()
                .put("arquivo", arquivo == null ? "" : new File(arquivo).getAbsolutePath())
                .put("vertices", grafo.getVertices().size())
                .put("arestas", numeroArestas())
                .put("braco", braco)
                .put("bracoBemol", bracoBemol)
                .put("bracoClasses", classes)
                .put("cordas", Nota.NOMES_CORDAS)
                .put("midiCordas", Nota.MIDI_CORDAS);
    }

    /** Vértices e arestas com a interpretação musical de cada vértice (para o desenho). */
    public Json.Obj grafo() {
        List<Object> vs = Json.lista();
        for (Vertice v : grafo.getVertices()) {
            VerticeMusical vm = busca.musical(v.getId());
            List<Object> adj = Json.lista();
            for (Aresta a : v.getAdjacencias()) adj.add(Json.obj().put("destino", a.getDestinoId()).put("peso", a.getPeso()));
            Json.Obj o = Json.obj().put("id", v.getId()).put("rotulo", v.getRotulo()).put("adjacencias", adj);
            if (vm != null) {
                o.put("tonalidade", vm.getCampo().getNome())
                 .put("tonica", vm.getCampo().getTonica())
                 .put("grau", vm.getGrau())
                 .put("grauRomano", vm.getGrauRomano())
                 .put("funcao", vm.getFuncao().getNome())
                 .put("cifra", vm.getAcorde().getCifra())
                 .put("descricao", vm.descricao());
            }
            vs.add(o);
        }
        return Json.obj().put("vertices", vs).put("arestas", numeroArestas());
    }

    // ================================================================== fluxo

    public Json.Obj identificar(int[] casas) {
        IdentificadorAcordes.Resultado r = IdentificadorAcordes.identificar(casas);
        Json.Obj o = Json.obj()
                .put("notas", r.notasFormatadas())
                .put("quantidade", r.getNotas().size())
                .put("grafoVazio", grafo.getVertices().isEmpty());
        Acorde a = r.getAcorde();
        if (a == null) return o.put("acorde", null);

        o.put("acorde", acordeJson(a));
        o.put("triade", acordeJson(a.triade()));
        o.put("tetrade", !a.getTipo().isTriade());
        o.put("invertido", r.isInvertido());
        o.put("baixo", r.getBaixo() < 0 ? "" : Nota.nome(r.getBaixo(), a.isGrafiaBemol()));
        o.put("shape", shapeJson(BancoShapes.shape(a)));

        List<VerticeMusical> origens = busca.origens(a);
        origens.sort((x, y) -> x.getGrau() != y.getGrau() ? Integer.compare(x.getGrau(), y.getGrau())
                : Integer.compare((x.getCampo().getTonica() * 7) % 12, (y.getCampo().getTonica() * 7) % 12));
        List<Object> lista = Json.lista();
        for (VerticeMusical vm : origens) {
            Vertice v = grafo.getVerticeById(vm.getId());
            List<Object> campo = Json.lista();
            for (int g = 0; g < 7; g++) {
                campo.add(Json.obj().put("grau", CampoHarmonico.GRAUS[g]).put("cifra", vm.getCampo().acorde(g).getCifra())
                        .put("atual", g == vm.getGrau()));
            }
            lista.add(Json.obj()
                    .put("id", vm.getId())
                    .put("rotulo", vm.getRotulo())
                    .put("funcao", vm.getFuncao().getNome())
                    .put("descricao", vm.descricao())
                    .put("saidas", v == null ? 0 : v.getAdjacencias().size())
                    .put("campo", campo));
        }
        return o.put("origens", lista);
    }

    public Json.Obj progressoes(Acorde a, int tamanho, boolean modulacao) {
        List<Object> lista = Json.lista();
        for (Progressao p : busca.buscarProgressoes(a, tamanho, modulacao, 10)) lista.add(progressaoJson(p));
        return Json.obj().put("lista", lista);
    }

    public Json.Obj caminho(Acorde de, Acorde para) {
        Progressao p = busca.caminhoMinimo(de, para);
        if (p == null) return Json.obj().put("erro", "Não existe caminho de " + de.getCifra() + " até " + para.getCifra() + " no grafo atual.");
        return Json.obj().put("progressao", progressaoJson(p));
    }

    public Json.Obj acordes() {
        List<Object> lista = Json.lista();
        for (Acorde a : busca.acordesDoGrafo()) lista.add(acordeJson(a));
        return Json.obj().put("lista", lista);
    }

    public Json.Obj exemplos() {
        String[][] ex = {{"Sol", "7", "MAIOR"}, {"Dó", "0", "MAIOR"}, {"Ré", "2", "MAIOR"}, {"Mi menor", "4", "MENOR"},
                {"Lá menor", "9", "MENOR"}, {"Si menor", "11", "MENOR"}, {"Fá", "5", "MAIOR"}};
        List<Object> lista = Json.lista();
        for (String[] e : ex) {
            ShapeAcorde s = BancoShapes.shape(new Acorde(Integer.parseInt(e[1]), TipoAcorde.valueOf(e[2])));
            lista.add(Json.obj().put("nome", e[0]).put("casas", s.getPosicoesPorCorda()));
        }
        return Json.obj().put("lista", lista);
    }

    // ================================================================== menu a..i

    public Json.Obj texto(String tipo) {
        String t;
        switch (tipo) {
            case "arquivo": t = capturar(grafo::mostrarConteudoArquivo); break;          // g)
            case "lista": t = capturar(grafo::mostrarGrafo); break;                      // h)
            case "conexidade":                                                           // i)
                t = grafo.getVertices().isEmpty() ? "O grafo está vazio." : capturar(grafo::analisarConexidade);
                break;
            default: t = "";
        }
        return Json.obj().put("texto", t);
    }

    public Json.Obj operacao(Map<String, String> p) {
        String op = p.getOrDefault("op", "");
        try {
            switch (op) {
                case "ler": {                                                            // a)
                    String caminho = p.getOrDefault("caminho", arquivo);
                    if (!new File(caminho).exists()) return erro("Arquivo não encontrado: " + caminho);
                    String saida = carregar(caminho);
                    return saida.contains("Arquivo lido") ? ok(saida) : erro(saida);
                }
                case "gravar": {                                                         // b)
                    String caminho = p.getOrDefault("caminho", arquivo);
                    String saida = capturar(() -> grafo.gravarArquivo(caminho));
                    arquivo = caminho;
                    return saida.startsWith("Erro") ? erro(saida) : ok(saida);
                }
                case "inserirVertice": {                                                 // c)
                    int id = inteiro(p, "id");
                    String rotulo = p.getOrDefault("rotulo", "").trim();
                    if (rotulo.isEmpty()) return erro("Informe o rótulo do vértice.");
                    if (grafo.getVerticeById(id) != null) return erro("Já existe um vértice com o ID " + id + ".");
                    grafo.inserirVertice(id, rotulo);
                    grafoAlterado();
                    return ok("Vértice " + id + " (\"" + rotulo + "\") inserido com sucesso.");
                }
                case "removerVertice": {                                                 // e)
                    int id = inteiro(p, "id");
                    boolean existia = grafo.getVerticeById(id) != null;
                    String saida = capturar(() -> grafo.removerVertice(id));
                    grafoAlterado();
                    return existia ? ok(saida + " As arestas que apontavam para ele também foram removidas.") : erro(saida);
                }
                case "inserirAresta": {                                                  // d)
                    int o = inteiro(p, "origem");
                    int d = inteiro(p, "destino");
                    int peso = inteiro(p, "peso");
                    if (grafo.getVerticeById(o) == null || grafo.getVerticeById(d) == null) {
                        return erro("Não foi possível inserir: os vértices " + o + " e " + d + " precisam existir.");
                    }
                    if (peso < 0) return erro("O peso deve ser não negativo (o Dijkstra exige pesos ≥ 0).");
                    grafo.inserirAresta(o, d, peso);
                    grafoAlterado();
                    return ok("Aresta " + o + " → " + d + " (peso " + peso + ") inserida com sucesso.");
                }
                case "removerAresta": {                                                  // f)
                    int o = inteiro(p, "origem");
                    int d = inteiro(p, "destino");
                    String saida = capturar(() -> grafo.removerAresta(o, d));
                    grafoAlterado();
                    return saida.startsWith("Erro") ? erro(saida) : ok(saida);
                }
                default:
                    return erro("Operação desconhecida: " + op);
            }
        } catch (NumberFormatException e) {
            return erro("Valor inválido: digite apenas números.");
        }
    }

    public Json.Obj dijkstra(int origem, int destino) {
        if (grafo.getVerticeById(origem) == null || grafo.getVerticeById(destino) == null) {
            return erro("Origem ou destino inexistente.");
        }
        Grafo.ResultadoDijkstra r = grafo.dijkstra(origem);
        StringBuilder sb = new StringBuilder("Algoritmo de Dijkstra (pesos não negativos)\n");
        sb.append("Origem: ").append(descrever(origem)).append('\n');
        sb.append("Destino: ").append(descrever(destino)).append("\n\n");
        List<Integer> caminho = new ArrayList<>();
        if (!r.alcanca(destino)) {
            sb.append("O destino não é alcançável a partir da origem.");
        } else {
            caminho = r.caminhoAte(destino);
            sb.append("Custo mínimo: ").append(r.distancia(destino)).append("\n\nCaminho:\n");
            for (int i = 0; i < caminho.size(); i++) {
                if (i > 0) {
                    sb.append("     │ peso ").append(peso(caminho.get(i - 1), caminho.get(i))).append("\n     ▼\n");
                }
                sb.append("  ").append(descrever(caminho.get(i))).append('\n');
            }
        }
        return Json.obj().put("ok", true).put("texto", sb.toString()).put("caminho", caminho)
                .put("custo", r.alcanca(destino) ? r.distancia(destino) : -1);
    }

    // ================================================================== conversões

    static Acorde acorde(int raiz, String tipo) {
        TipoAcorde t = TipoAcorde.valueOf(tipo);
        return new Acorde(raiz, t, IdentificadorAcordes.grafiaPadrao(raiz, t));
    }

    private static Json.Obj acordeJson(Acorde a) {
        return Json.obj().put("raiz", a.getRaiz()).put("tipo", a.getTipo().name()).put("cifra", a.getCifra())
                .put("nome", a.getNomeCurto()).put("extenso", a.getNomeExtenso());
    }

    private static Json.Obj shapeJson(ShapeAcorde s) {
        return Json.obj().put("casas", s.getPosicoesPorCorda()).put("pestana", s.getPestana())
                .put("dificuldade", s.getDificuldade()).put("descricao", s.getDescricao()).put("tablatura", s.tablatura());
    }

    private Json.Obj progressaoJson(Progressao p) {
        List<Object> etapas = Json.lista();
        List<Progressao.Etapa> lista = p.getEtapas();
        for (int i = 0; i < lista.size(); i++) {
            Progressao.Etapa e = lista.get(i);
            List<Object> papeis = Json.lista();
            for (VerticeMusical vm : e.getVertices()) papeis.add(vm.descricao() + " (" + vm.getFuncao().getNome() + ")");
            etapas.add(Json.obj()
                    .put("acorde", acordeJson(e.getAcorde()))
                    .put("grau", e.getGrau())
                    .put("peso", e.getPesoEntrada())
                    .put("pivo", e.isPivo())
                    .put("papeis", papeis)
                    .put("shape", shapeJson(BancoShapes.shape(e.getAcorde()))));
        }
        List<Object> caminho = Json.lista();
        for (VerticeMusical vm : p.getCaminho()) caminho.add(vm.getId());

        // Extras (passo 6)
        CampoHarmonico ch = p.getCampoPrincipal();
        List<Object> campo = Json.lista();
        for (int g = 0; g < 7; g++) {
            campo.add(Json.obj().put("grau", CampoHarmonico.GRAUS[g]).put("cifra", ch.acorde(g).getCifra())
                    .put("funcao", CampoHarmonico.funcao(g).getNome()).put("usado", p.getAcordes().contains(ch.acorde(g))));
        }
        int relativa = Nota.normalizar(ch.getTonica() + 9);
        Json.Obj extras = Json.obj()
                .put("musicas", ConteudoExtra.musicasDeReferencia(p))
                .put("tonalidade", ch.getNomeExtenso())
                .put("bemol", ch.isBemol())
                .put("escalaMaior", Json.obj().put("nome", "Escala maior de " + Nota.nomePt(ch.getTonica(), ch.isBemol()))
                        .put("notas", ch.escalaMaior()).put("tonica", ch.getTonica()).put("texto", ch.formatarNotas(ch.escalaMaior())))
                .put("pentatonica", Json.obj().put("nome", "Pentatônica de " + Nota.nomePt(relativa, ch.isBemol()) + " menor (relativa)")
                        .put("notas", ch.pentatonicaRelativa()).put("tonica", relativa).put("texto", ch.formatarNotas(ch.pentatonicaRelativa())))
                .put("campo", campo);

        return Json.obj()
                .put("nomes", p.getNomes())
                .put("cifras", p.getCifras())
                .put("graus", p.getGrausOrdinais())
                .put("padrao", p.getPadraoGraus())
                .put("custo", p.getCustoTotal())
                .put("dificuldade", p.getDificuldadeShapes())
                .put("modulacoes", p.getModulacoes())
                .put("tonalidades", String.join(" → ", p.getTonalidades()))
                .put("descricao", ConteudoExtra.descrever(p))
                .put("caminho", caminho)
                .put("etapas", etapas)
                .put("extras", extras);
    }

    // ================================================================== utilitários

    private String descrever(int id) {
        Vertice v = grafo.getVerticeById(id);
        if (v == null) return String.valueOf(id);
        VerticeMusical vm = busca.musical(id);
        return "[" + id + " - " + v.getRotulo() + "]" + (vm == null ? "" : "  " + vm.getAcorde().getCifra() + " · " + vm.descricao());
    }

    private int numeroArestas() {
        int total = 0;
        for (Vertice v : grafo.getVertices()) total += v.getAdjacencias().size();
        return total;
    }

    private int peso(int origem, int destino) {
        Vertice v = grafo.getVerticeById(origem);
        if (v != null) for (Aresta a : v.getAdjacencias()) if (a.getDestinoId() == destino) return a.getPeso();
        return -1;
    }

    private static int inteiro(Map<String, String> p, String chave) {
        return Integer.parseInt(p.getOrDefault(chave, "").trim());
    }

    private static Json.Obj ok(String msg) {
        return Json.obj().put("ok", true).put("mensagem", umaLinha(msg));
    }

    private static Json.Obj erro(String msg) {
        return Json.obj().put("ok", false).put("mensagem", umaLinha(msg));
    }

    private static String umaLinha(String s) {
        return s.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    /** Executa um método de console do back-end e devolve o que ele imprimiu. */
    static synchronized String capturar(Runnable acao) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            PrintStream ps = new PrintStream(buffer, true, "UTF-8");
            System.setOut(ps);
            acao.run();
            ps.flush();
            return buffer.toString("UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            return "";
        } finally {
            System.setOut(original);
        }
    }
}
