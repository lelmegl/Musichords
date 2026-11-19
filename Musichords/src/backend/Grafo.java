import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Implementação do grafo direcionado com peso nas arestas usando Lista de Adjacências,
 * com as operações das Partes 1 e 2 e as técnicas de análise e solução da Parte 3.
 * Histórico de Alterações:
 * Data       | Autor   | Descrição
 * 22/09/2026 | Pedro   | Estruturação inicial da classe, atributos e método lerArquivo.
 * 23/09/2026 | Gustavo | Implementação da manipulação do grafo (inserir/remover vértices e arestas).
 * 27/09/2026 | Felipe  | Criação dos métodos mostrarGrafo, mostrarConteudoArquivo e gravarArquivo.
 * 28/09/2026 | Pedro   | Implementação do algoritmo DFS para análise de conexidade (FCONEX).
 * 30/09/2026 | Pedro   | Acréscimos para a interface gráfica: getVertices(), getVerticeById() público
 *                        e caminho mínimo por Dijkstra (nenhum método existente foi alterado).
 * 11/11/2026 | Pedro   | Validações nas opções c..f: ID de vértice repetido, aresta com vértice
 *                        inexistente, peso negativo e aresta repetida são recusados; os métodos
 *                        passam a devolver true/false para o menu informar o resultado correto.
 * 13/11/2026 | Pedro   | Parte 3: opção k (progressão de menor esforço com Dijkstra) e opções l..o
 *                        (graus e caminho euleriano; Teoremas de Ore e Dirac, ciclo hamiltoniano
 *                        por backtracking e Caixeiro Viajante com Floyd; coloração por classe de
 *                        Welsh e Powell; árvore geradora mínima de Kruskal conferida com Prim).
 * 13/11/2026 | Pedro   | Parte 3 conforme os slides de Algoritmos de Busca em Grafos: percursos em
 *                        profundidade (pilha) e em largura (fila) como no material da aula; o
 *                        Kruskal detecta ciclos com o percurso em profundidade (sem união-busca),
 *                        o Prim escolhe a menor aresta que sai da árvore, o bipartido e a
 *                        conexidade do subjacente usam o percurso em largura, o ciclo hamiltoniano
 *                        é um percurso em profundidade com retorno e o limite inferior da coloração
 *                        vem da verificação de um K4 (sem busca de clique).
 */
public class Grafo {

    private final int TIPO_GRAFO = 6;
    private List<Vertice> vertices;

    public Grafo() {
        this.vertices = new ArrayList<>();
    }

    // Acesso somente leitura aos vértices (usado pela interface gráfica)
    public List<Vertice> getVertices() {
        return java.util.Collections.unmodifiableList(vertices);
    }

    // a) Ler dados do arquivo grafo.txt
    public void lerArquivo(String caminhoArquivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            // Limpa a memória atual antes de carregar o novo ficheiro
            this.vertices.clear();
            String linha = br.readLine();

            if (linha == null) {
                System.out.println("O arquivo está vazio");
                return;
            }

            int tipo = Integer.parseInt(linha.trim());
            if (tipo != TIPO_GRAFO) {
                System.out.println("O tipo de grafo no arquivo é diferente da modelagem");
            }

            int numVertices = Integer.parseInt(br.readLine().trim());

            for (int i = 0; i < numVertices; i++) {
                linha = br.readLine().trim();
                // Separa o ID do rótulo baseado no primeiro espaço em branco
                int firstSpace = linha.indexOf(' ');
                int id = Integer.parseInt(linha.substring(0, firstSpace));
                // Remove as aspas do rótulo para armazenar limpamente na memória
                String rotulo = linha.substring(firstSpace + 1).replace("\"", "");

                this.vertices.add(new Vertice(id, rotulo));
            }

            int numArestas = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < numArestas; i++) {
                linha = br.readLine().trim();
                String[] partes = linha.split("\\s+");
                int origem = Integer.parseInt(partes[0]);
                int destino = Integer.parseInt(partes[1]);
                int peso = Integer.parseInt(partes[2]);

                // Associa a aresta ao vértice de origem correspondente
                for (Vertice v : this.vertices) {
                    if (v.getId() == origem) {
                        v.adicionarAresta(new Aresta(destino, peso));
                        break;
                    }
                }
            }

            System.out.println("\nArquivo lido: " + numVertices + " vértices e " + numArestas + " arestas");

        } catch (IOException e) {
            System.out.println("\nNão foi possível ler o arquivo: " + e.getMessage());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("\nFormatação inválida: " + e.getMessage());
        }
    }

    // b) Gravar dados no arquivo grafo.txt
    public void gravarArquivo(String caminhoArquivo) {
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(caminhoArquivo))) {
            bw.write(TIPO_GRAFO + "\n");
            bw.write(vertices.size() + "\n");
            
            int totalArestas = 0;
            // Grava os vértices no formato: ID "Rótulo"
            for (Vertice v : vertices) {
                bw.write(v.getId() + " \"" + v.getRotulo() + "\"\n");
                totalArestas += v.getAdjacencias().size();
            }
            
            // Grava o número de arestas e, de seguida, as ligações
            bw.write(totalArestas + "\n");
            for (Vertice v : vertices) {
                for (Aresta a : v.getAdjacencias()) {
                    bw.write(v.getId() + " " + a.getDestinoId() + " " + a.getPeso() + "\n");
                }
            }
            System.out.println("Arquivo gravado com sucesso em: " + caminhoArquivo);
        } catch (IOException e) {
            System.out.println("Erro ao gravar arquivo: " + e.getMessage());
        }
    }

    // c) Inserir vértice
    /**
     * Insere um vértice novo. Recusa ID repetido, pois o ID identifica o vértice nas arestas.
     * @return true se o vértice foi inserido
     */
    public boolean inserirVertice(int id, String rotulo) {
        if (getVerticeById(id) != null) {
            System.out.println("Erro: já existe um vértice com o ID " + id + ".");
            return false;
        }
        vertices.add(new Vertice(id, rotulo));
        return true;
    }

    // d) Inserir aresta
    /**
     * Insere a aresta origem -> destino. Os dois vértices precisam existir, o peso não pode ser
     * negativo (o Dijkstra exige pesos >= 0) e a mesma aresta não pode ser inserida duas vezes.
     * @return true se a aresta foi inserida
     */
    public boolean inserirAresta(int origemId, int destinoId, int peso) {
        Vertice origem = getVerticeById(origemId);
        if (origem == null || getVerticeById(destinoId) == null) {
            System.out.println("Erro: os vértices " + origemId + " e " + destinoId + " precisam existir no grafo.");
            return false;
        }
        if (peso < 0) {
            System.out.println("Erro: o peso deve ser maior ou igual a zero.");
            return false;
        }
        for (Aresta a : origem.getAdjacencias()) {
            if (a.getDestinoId() == destinoId) {
                System.out.println("Erro: a aresta " + origemId + " -> " + destinoId + " já existe.");
                return false;
            }
        }
        origem.adicionarAresta(new Aresta(destinoId, peso));
        return true;
    }

    // e) Remover vértice
    /** @return true se o vértice existia e foi removido (com as arestas que chegavam nele) */
    public boolean removerVertice(int id) {
        boolean verticeRemovido = false;

        // remover o vértice
        for (int i = vertices.size() - 1; i >= 0; i--) {
            if (vertices.get(i).getId() == id) {
                vertices.remove(i);
                verticeRemovido = true;
                System.out.println("Vértice com ID " + id + " foi removido.");
                break;
            }
        }

        if (!verticeRemovido) {
            System.out.println("Vértice com ID " + id + " não existe no grafo.");
            return false;
        }

        // Limpar arestas que apontavam para o vértice removido
        for (int i = 0; i < vertices.size(); i++) {
            Vertice v = vertices.get(i);
            for (int j = v.getAdjacencias().size() - 1; j >= 0; j--) {
                if (v.getAdjacencias().get(j).getDestinoId() == id) {
                    v.getAdjacencias().remove(j);
                }
            }
        }
        return true;
    }

    // f) Remover aresta
    /** @return true se a aresta existia e foi removida */
    public boolean removerAresta(int origemId, int destinoId) {
        boolean arestaRemovida = false;

        for (Vertice v : vertices) {
            if (v.getId() == origemId) {
                for (int i = v.getAdjacencias().size() - 1; i >= 0; i--) {
                    if (v.getAdjacencias().get(i).getDestinoId() == destinoId) {
                        v.getAdjacencias().remove(i);
                        arestaRemovida = true;
                        System.out.println("Aresta do acorde " + origemId + " para " + destinoId + " removida.");
                        break;
                    }
                }
                break;
            }
        }

        if (!arestaRemovida) {
            System.out.println("Erro: A transição informada não existe no grafo.");
        }
        return arestaRemovida;
    }

    // g) Mostrar conteúdo do arquivo
    public void mostrarConteudoArquivo() {
        System.out.println("\n=== CONTEÚDO DO ARQUIVO ===");
        System.out.println("Tipo do Grafo: " + TIPO_GRAFO + " (Direcionado com peso nas arestas)");
        System.out.println("Número de Vértices: " + vertices.size());
        for (Vertice v : vertices) {
            System.out.println("  ID: " + v.getId() + " | Rótulo: \"" + v.getRotulo() + "\"");
        }
        
        int totalArestas = 0;
        for (Vertice v : vertices) {
            totalArestas += v.getAdjacencias().size();
        }
        
        System.out.println("Número de Arestas: " + totalArestas);
        for (Vertice v : vertices) {
            for (Aresta a : v.getAdjacencias()) {
                System.out.println("  Origem: " + v.getId() + " -> Destino: " + a.getDestinoId() + " (Peso: " + a.getPeso() + ")");
            }
        }
        System.out.println("===========================\n");
    }

    // h) Mostrar grafo
    public void mostrarGrafo() {
        System.out.println("\n=== ESTRUTURA DO GRAFO (LISTA DE ADJACÊNCIAS) ===");
        for (Vertice v : vertices) {
            System.out.print("[" + v.getId() + " - " + v.getRotulo() + "] -> ");
            if (v.getAdjacencias().isEmpty()) {
                System.out.print("Nenhuma transição");
            } else {
                for (int i = 0; i < v.getAdjacencias().size(); i++) {
                    Aresta a = v.getAdjacencias().get(i);
                    System.out.print("(" + a.getDestinoId() + ", peso:" + a.getPeso() + ")");
                    if (i < v.getAdjacencias().size() - 1) System.out.print(" -> ");
                }
            }
            System.out.println();
        }
        System.out.println("=================================================\n");
    }

    // i) Apresentar a conexidade do grafo e o reduzido
    public void analisarConexidade() {
        if (vertices.isEmpty()) {
            System.out.println("O grafo está vazio.");
            return;
        }

        // --- 1. ALGORITMO FCONEX (Kosaraju para Componentes Fortemente Conexas) ---
        java.util.Stack<Integer> pilha = new java.util.Stack<>();
        java.util.Set<Integer> visitados = new java.util.HashSet<>();

        // Passo 1.1: Preencher a pilha com base nos tempos de término via DFS
        for (Vertice v : vertices) {
            if (!visitados.contains(v.getId())) {
                preencherPilhaDFS(v, visitados, pilha);
            }
        }

        // Passo 1.2: Criar o grafo transposto (inverter a direção de todas as arestas)
        java.util.Map<Integer, List<Integer>> grafoTransposto = new java.util.HashMap<>();
        for (Vertice v : vertices) {
            grafoTransposto.putIfAbsent(v.getId(), new ArrayList<>());
            for (Aresta a : v.getAdjacencias()) {
                grafoTransposto.putIfAbsent(a.getDestinoId(), new ArrayList<>());
                grafoTransposto.get(a.getDestinoId()).add(v.getId());
            }
        }

        // Passo 1.3: Encontrar as componentes fortemente conexas usando a pilha
        visitados.clear();
        List<List<Integer>> sccs = new ArrayList<>();
        while (!pilha.isEmpty()) {
            int id = pilha.pop();
            if (!visitados.contains(id)) {
                List<Integer> componente = new ArrayList<>();
                dfsTransposto(id, grafoTransposto, visitados, componente);
                sccs.add(componente);
            }
        }

        // --- 2. ANÁLISE DE CONEXIDADE (C0, C1, C2, C3) ---
        System.out.println("\n=== ANÁLISE DE CONEXIDADE ===");
        if (sccs.size() == 1) {
            System.out.println("Categoria: C3 (Fortemente Conexo) - Qualquer acorde alcança qualquer outro acorde.");
        } else {
            // Verificar C0 vs C1/C2 através do grafo subjacente não direcionado
            if (isSubjacenteConexo()) {
                if (isC2(sccs)) {
                    System.out.println("Categoria: C2 (Unilateralmente Conexo / Semi-conexo).");
                } else {
                    System.out.println("Categoria: C1 (Fracamente Conexo) - O grafo só é conexo se ignorarmos a direção das transições.");
                }
            } else {
                System.out.println("Categoria: C0 (Desconexo) - O campo harmônico possui acordes isolados que não se interligam de forma alguma.");
            }
        }

        // Exibir Componentes Fortemente Conexas (FCONEX)
        System.out.println("\nComponentes Fortemente Conexas (FCONEX):");
        for (int i = 0; i < sccs.size(); i++) {
            System.out.println("Componente " + (i + 1) + ": " + sccs.get(i));
        }

        // --- 3. GERAR E EXIBIR GRAFO REDUZIDO ---
        System.out.println("\nGrafo Reduzido (Baseado nas Componentes):");
        mostrarGrafoReduzido(sccs);
        System.out.println("=============================\n");
    }

    // ==========================================
    // MÉTODOS AUXILIARES PARA A CONEXIDADE
    // ==========================================

    private void preencherPilhaDFS(Vertice v, java.util.Set<Integer> visitados, java.util.Stack<Integer> pilha) {
        visitados.add(v.getId());
        for (Aresta a : v.getAdjacencias()) {
            if (!visitados.contains(a.getDestinoId())) {
                Vertice vizinho = getVerticeById(a.getDestinoId());
                if (vizinho != null) preencherPilhaDFS(vizinho, visitados, pilha);
            }
        }
        pilha.push(v.getId());
    }

    private void dfsTransposto(int vId, java.util.Map<Integer, List<Integer>> transposto, java.util.Set<Integer> visitados, List<Integer> componente) {
        visitados.add(vId);
        componente.add(vId);
        if (transposto.containsKey(vId)) {
            for (int vizinho : transposto.get(vId)) {
                if (!visitados.contains(vizinho)) {
                    dfsTransposto(vizinho, transposto, visitados, componente);
                }
            }
        }
    }

    public Vertice getVerticeById(int id) {
        for (Vertice v : vertices) {
            if (v.getId() == id) return v;
        }
        return null;
    }

    /**
     * Algoritmo "desconexo - Solução 1" do slide: torna a matriz simétrica (grafo subjacente),
     * faz o percurso em largura a partir de um vértice qualquer e compara a quantidade de
     * vértices visitados com |V|.
     */
    private boolean isSubjacenteConexo() {
        if (vertices.isEmpty()) return true;
        return percursoLargura(vertices.get(0).getId(), vizinhosSubjacente()).size() == vertices.size();
    }

    private boolean isC2(List<List<Integer>> sccs) {
        List<List<Integer>> topoOrder = new ArrayList<>(sccs);
        java.util.Collections.reverse(topoOrder);
        
        for (int i = 0; i < topoOrder.size() - 1; i++) {
            List<Integer> sccAtual = topoOrder.get(i);
            List<Integer> sccProx = topoOrder.get(i + 1);
            
            boolean temAresta = false;
            for (int u : sccAtual) {
                Vertice v = getVerticeById(u);
                if (v != null) {
                    for (Aresta a : v.getAdjacencias()) {
                        if (sccProx.contains(a.getDestinoId())) {
                            temAresta = true;
                            break;
                        }
                    }
                }
                if (temAresta) break;
            }
            if (!temAresta) return false;
        }
        return true;
    }

    private void mostrarGrafoReduzido(List<List<Integer>> sccs) {
        java.util.Map<Integer, Integer> mapVerticeComponente = new java.util.HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            for (int vId : sccs.get(i)) {
                mapVerticeComponente.put(vId, i + 1);
            }
        }

        java.util.Set<String> arestasReduzido = new java.util.HashSet<>();
        for (Vertice v : vertices) {
            int compOrigem = mapVerticeComponente.get(v.getId());
            for (Aresta a : v.getAdjacencias()) {
                Integer compDestino = mapVerticeComponente.get(a.getDestinoId());
                if (compDestino != null && compOrigem != compDestino) {
                    arestasReduzido.add("Componente " + compOrigem + " -> Componente " + compDestino);
                }
            }
        }

        if (arestasReduzido.isEmpty()) {
            System.out.println("  Grafo reduzido não possui arestas (apenas um vértice ou vértices isolados).");
        } else {
            for (String ar : arestasReduzido) {
                System.out.println("  " + ar);
            }
        }
    }

    // ==========================================
    // CAMINHO MÍNIMO - DIJKSTRA (pesos não negativos)
    // ==========================================

    /**
     * Dijkstra com múltiplas origens (todas começam com distância 0). É usado quando o mesmo
     * acorde físico ocupa vários vértices do grafo (um em cada tonalidade).
     */
    public ResultadoDijkstra dijkstra(java.util.Collection<Integer> origens) {
        java.util.Map<Integer, Integer> dist = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> anterior = new java.util.HashMap<>();
        java.util.PriorityQueue<int[]> fila = new java.util.PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        for (int o : origens) {
            if (getVerticeById(o) == null) continue;
            dist.put(o, 0);
            fila.add(new int[]{o, 0});
        }
        java.util.Set<Integer> fechados = new java.util.HashSet<>();
        while (!fila.isEmpty()) {
            int[] atual = fila.poll();
            int u = atual[0];
            if (!fechados.add(u)) continue;
            Vertice vu = getVerticeById(u);
            if (vu == null) continue;
            for (Aresta a : vu.getAdjacencias()) {
                int v = a.getDestinoId();
                if (getVerticeById(v) == null) continue;
                int nd = atual[1] + a.getPeso();
                if (nd < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, nd);
                    anterior.put(v, u);
                    fila.add(new int[]{v, nd});
                }
            }
        }
        return new ResultadoDijkstra(dist, anterior);
    }

    public ResultadoDijkstra dijkstra(int origem) {
        return dijkstra(java.util.Collections.singletonList(origem));
    }

    /** Distâncias e predecessores calculados pelo Dijkstra. */
    public static class ResultadoDijkstra {
        private final java.util.Map<Integer, Integer> distancias;
        private final java.util.Map<Integer, Integer> anteriores;

        ResultadoDijkstra(java.util.Map<Integer, Integer> distancias, java.util.Map<Integer, Integer> anteriores) {
            this.distancias = distancias;
            this.anteriores = anteriores;
        }

        public boolean alcanca(int destino) {
            return distancias.containsKey(destino);
        }

        public int distancia(int destino) {
            return distancias.getOrDefault(destino, Integer.MAX_VALUE);
        }

        /** Caminho (IDs) da origem até o destino; vazio se inalcançável. */
        public List<Integer> caminhoAte(int destino) {
            List<Integer> caminho = new ArrayList<>();
            if (!alcanca(destino)) return caminho;
            Integer atual = destino;
            while (atual != null) {
                caminho.add(0, atual);
                atual = anteriores.get(atual);
            }
            return caminho;
        }
    }

    // ==========================================================================================
    // PARTE 3 - SOLUÇÃO DO PROBLEMA (item 1) E CARACTERÍSTICAS DO GRAFO (item 2)
    // ==========================================================================================

    /** Procura um vértice pelo rótulo (ex.: "C_I"), sem diferenciar maiúsculas de minúsculas. */
    public Vertice getVerticeByRotulo(String rotulo) {
        for (Vertice v : vertices) {
            if (v.getRotulo().equalsIgnoreCase(rotulo.trim())) return v;
        }
        return null;
    }

    /** Rótulo de um vértice pelo ID (ou o próprio ID, se o vértice não existir). */
    private String rotulo(int id) {
        Vertice v = getVerticeById(id);
        return v == null ? String.valueOf(id) : v.getRotulo();
    }

    /** Peso da aresta origem -> destino, ou -1 se ela não existir. */
    private int pesoAresta(int origemId, int destinoId) {
        Vertice v = getVerticeById(origemId);
        if (v == null) return -1;
        for (Aresta a : v.getAdjacencias()) {
            if (a.getDestinoId() == destinoId) return a.getPeso();
        }
        return -1;
    }

    /** Grau de entrada de cada vértice (quantas arestas chegam nele), indexado pelo ID. */
    public java.util.Map<Integer, Integer> grausDeEntrada() {
        java.util.Map<Integer, Integer> entrada = new java.util.HashMap<>();
        for (Vertice v : vertices) entrada.put(v.getId(), 0);
        for (Vertice v : vertices) {
            for (Aresta a : v.getAdjacencias()) {
                entrada.computeIfPresent(a.getDestinoId(), (id, g) -> g + 1);
            }
        }
        return entrada;
    }

    /** Grau total (entrada + saída) de um vértice, como definido em aula para grafos direcionados. */
    public int grauTotal(int id) {
        Vertice v = getVerticeById(id);
        if (v == null) return 0;
        return v.getAdjacencias().size() + grausDeEntrada().get(id);
    }

    /**
     * Vizinhos de cada vértice no grafo subjacente (ignorando o sentido das arestas).
     * Usado pela coloração e pelos teoremas de Hamilton, que tratam grafos não orientados.
     */
    private java.util.Map<Integer, java.util.Set<Integer>> vizinhosSubjacente() {
        java.util.Map<Integer, java.util.Set<Integer>> viz = new java.util.LinkedHashMap<>();
        for (Vertice v : vertices) viz.put(v.getId(), new java.util.TreeSet<>());
        for (Vertice v : vertices) {
            for (Aresta a : v.getAdjacencias()) {
                if (!viz.containsKey(a.getDestinoId()) || a.getDestinoId() == v.getId()) continue;
                viz.get(v.getId()).add(a.getDestinoId());
                viz.get(a.getDestinoId()).add(v.getId());
            }
        }
        return viz;
    }

    // ------------------------------------------------------------------------------------------
    // Percursos em profundidade e em largura (slides "Algoritmos de Busca em Grafos - parte 1")
    // ------------------------------------------------------------------------------------------

    /** Primeiro nó adjacente a n ainda não marcado, ou -1 se não houver (noAdjacente do slide). */
    private static int noAdjacente(int n, java.util.Map<Integer, ? extends java.util.Collection<Integer>> adj,
                                   java.util.Set<Integer> nosMarcados) {
        for (int m : adj.get(n)) if (!nosMarcados.contains(m)) return m;
        return -1;
    }

    /**
     * Percurso em profundidade, como no slide:
     * 1. visita o nó n selecionado; 2. marca n; 3. empilha n na pilha P;
     * 4. enquanto P não estiver vazia: 4.1 n <- pop(P);
     *    4.2 para cada nó m não marcado e adjacente a n: visita m, empilha n, marca m e faz n <- m.
     * "Visitar" aqui é acrescentar o nó à lista devolvida (a ordem do percurso).
     * @param adj lista de adjacência a percorrer (permite percorrer o grafo subjacente ou uma floresta)
     */
    public static List<Integer> percursoProfundidade(int vInicio, java.util.Map<Integer, ? extends java.util.Collection<Integer>> adj) {
        List<Integer> visitados = new ArrayList<>();
        if (!adj.containsKey(vInicio)) return visitados;
        java.util.Set<Integer> nosMarcados = new java.util.HashSet<>();
        java.util.Deque<Integer> pilha = new java.util.ArrayDeque<>();
        visitados.add(vInicio);                       // 1. visita n
        nosMarcados.add(vInicio);                     // 2. marca n
        pilha.push(vInicio);                          // 3. empilha n
        while (!pilha.isEmpty()) {                    // 4.
            int n = pilha.pop();                      // 4.1
            int m;
            while ((m = noAdjacente(n, adj, nosMarcados)) != -1) {   // 4.2
                visitados.add(m);                     // 4.2.1 visita m
                pilha.push(n);                        // 4.2.2 empilha n
                nosMarcados.add(m);                   // 4.2.3 marca m
                n = m;                                // 4.2.4 n <- m
            }
        }
        return visitados;
    }

    /**
     * Percurso em largura, como no slide:
     * 1. visita o nó n selecionado; 2. marca n; 3. insere n na fila F;
     * 4. enquanto F não estiver vazia: 4.1 retira um elemento de F e atribui a n;
     *    4.2 para cada nó m não marcado e adjacente a n: visita m, insere m em F e marca m.
     */
    public static List<Integer> percursoLargura(int vInicio, java.util.Map<Integer, ? extends java.util.Collection<Integer>> adj) {
        List<Integer> visitados = new ArrayList<>();
        if (!adj.containsKey(vInicio)) return visitados;
        java.util.Set<Integer> nosMarcados = new java.util.HashSet<>();
        java.util.Deque<Integer> fila = new java.util.ArrayDeque<>();
        visitados.add(vInicio);                       // 1.
        nosMarcados.add(vInicio);                     // 2.
        fila.add(vInicio);                            // 3.
        while (!fila.isEmpty()) {                     // 4.
            int n = fila.poll();                      // 4.1
            int m;
            while ((m = noAdjacente(n, adj, nosMarcados)) != -1) {   // 4.2
                visitados.add(m);                     // 4.2.1
                fila.add(m);                          // 4.2.2
                nosMarcados.add(m);                   // 4.2.3
            }
        }
        return visitados;
    }

    // ------------------------------------------------------------------------------------------
    // k) ITEM 1 - Progressão de menor esforço entre dois acordes (Dijkstra)
    // ------------------------------------------------------------------------------------------

    /**
     * Mostra o caminho de menor custo (algoritmo de Dijkstra) entre dois vértices: cada passo com
     * o seu peso (número de cordas que mudam de posição) e o custo total. Passos de peso 0 entre
     * tonalidades diferentes são pivôs: o mesmo acorde, visto como grau de outra tonalidade.
     */
    public void mostrarMenorEsforco(int origemId, int destinoId) {
        if (getVerticeById(origemId) == null || getVerticeById(destinoId) == null) {
            System.out.println("Erro: origem e destino precisam ser vértices do grafo.");
            return;
        }
        ResultadoDijkstra r = dijkstra(origemId);
        System.out.println("\n=== PROGRESSÃO DE MENOR ESFORÇO (DIJKSTRA) ===");
        System.out.println("Origem: " + rotulo(origemId) + "  ->  Destino: " + rotulo(destinoId));
        if (!r.alcanca(destinoId)) {
            System.out.println("Não existe caminho entre esses vértices.");
            return;
        }
        List<Integer> caminho = r.caminhoAte(destinoId);
        StringBuilder linha = new StringBuilder(rotulo(caminho.get(0)));
        for (int i = 1; i < caminho.size(); i++) linha.append(" -> ").append(rotulo(caminho.get(i)));
        System.out.println("Caminho: " + linha);
        System.out.println("Passos:");
        for (int i = 1; i < caminho.size(); i++) {
            int peso = pesoAresta(caminho.get(i - 1), caminho.get(i));
            String tipo = peso == 0 ? "pivô: mesmo acorde em outra tonalidade" : peso + " corda(s) mudam de posição";
            System.out.println("  " + rotulo(caminho.get(i - 1)) + " -> " + rotulo(caminho.get(i)) + "  (peso " + peso + ", " + tipo + ")");
        }
        System.out.println("Custo total: " + r.distancia(destinoId));
        System.out.println("==============================================\n");
    }

    // ------------------------------------------------------------------------------------------
    // l) ITEM 2 - Graus dos vértices e caminho euleriano
    // ------------------------------------------------------------------------------------------

    /** Quantidade de vértices com grau total (entrada + saída) ímpar. */
    public int contarVerticesGrauImpar() {
        java.util.Map<Integer, Integer> entrada = grausDeEntrada();
        int impares = 0;
        for (Vertice v : vertices) {
            if ((v.getAdjacencias().size() + entrada.get(v.getId())) % 2 == 1) impares++;
        }
        return impares;
    }

    /**
     * Teorema visto em aula: um grafo conexo tem caminho euleriano se e somente se tiver nenhum
     * (caminho fechado) ou exatamente dois (caminho aberto) vértices de grau ímpar.
     */
    public boolean temCaminhoEuleriano() {
        int impares = contarVerticesGrauImpar();
        return !vertices.isEmpty() && isSubjacenteConexo() && (impares == 0 || impares == 2);
    }

    public void analisarGrausEEuler() {
        if (vertices.isEmpty()) {
            System.out.println("O grafo está vazio.");
            return;
        }
        java.util.Map<Integer, Integer> entrada = grausDeEntrada();
        System.out.println("\n=== GRAUS DOS VÉRTICES E CAMINHO EULERIANO ===");
        System.out.println("Vértice      Entrada  Saída  Total");
        int min = Integer.MAX_VALUE, max = 0, soma = 0, desbalanceados = 0;
        String verticeMin = "", verticeMax = "";
        for (Vertice v : vertices) {
            int e = entrada.get(v.getId());
            int s = v.getAdjacencias().size();
            int t = e + s;
            System.out.printf("%-12s %7d %6d %6d%s%n", v.getRotulo(), e, s, t, t % 2 == 1 ? "  (ímpar)" : "");
            if (t < min) { min = t; verticeMin = v.getRotulo(); }
            if (t > max) { max = t; verticeMax = v.getRotulo(); }
            soma += t;
            if (e != s) desbalanceados++;
        }
        int impares = contarVerticesGrauImpar();
        System.out.println("\nGrau mínimo: " + min + " (" + verticeMin + ")  |  Grau máximo: " + max + " (" + verticeMax + ")");
        System.out.printf("Grau médio: %.2f%n", (double) soma / vertices.size());
        System.out.println("Vértices com grau ímpar: " + impares);
        if (!isSubjacenteConexo()) {
            System.out.println("Conclusão: o grafo não é conexo, logo não possui caminho euleriano.");
        } else if (impares == 0) {
            System.out.println("Conclusão: nenhum vértice de grau ímpar -> possui caminho euleriano fechado (grafo euleriano).");
        } else if (impares == 2) {
            System.out.println("Conclusão: exatamente dois vértices de grau ímpar -> possui caminho euleriano aberto, mas não é euleriano.");
        } else {
            System.out.println("Conclusão: mais de dois vértices de grau ímpar -> NÃO possui caminho euleriano e não é euleriano.");
        }
        System.out.println("Verificação para grafos direcionados (entrada = saída em todo vértice): "
                + (desbalanceados == 0 ? "atendida" : "não atendida em " + desbalanceados + " vértices"));
        System.out.println("Interpretação: não há uma sequência que pratique cada troca de acorde exatamente uma vez.");
        System.out.println("==============================================\n");
    }

    // ------------------------------------------------------------------------------------------
    // m) ITEM 2 - Hamilton: Teoremas 1 (Ore) e 2 (Dirac), ciclo hamiltoniano e Caixeiro Viajante
    // ------------------------------------------------------------------------------------------

    /** Limite de passos do percurso com retorno (evita travar em grafos onde a busca seria muito longa). */
    private static final long LIMITE_BACKTRACKING = 20_000_000L;

    /**
     * Procura um ciclo hamiltoniano com um percurso em profundidade com retorno (backtracking),
     * começando no primeiro vértice do grafo. É o percurso em profundidade da aula, com uma
     * diferença: quando o caminho chega a um nó sem adjacentes livres e "retorna", o nó é
     * desmarcado, para poder ser usado em outro caminho. A busca termina quando o caminho tem
     * todos os vértices e existe a aresta do último de volta ao primeiro.
     * Podas (material complementar, fora dos slides) que tornam a busca viável em 84 vértices:
     *  - todo vértice ainda não visitado precisa de pelo menos um predecessor e um sucessor livres;
     *  - se um vértice só pode ser alcançado a partir do vértice atual, ele é o próximo obrigatório;
     *  - os candidatos são testados do que tem menos saídas livres para o que tem mais (Warnsdorff).
     * @return o ciclo (sem repetir o vértice inicial no fim), ou lista vazia se não for encontrado
     */
    public List<Integer> cicloHamiltoniano() {
        List<Integer> caminho = new ArrayList<>();
        if (vertices.isEmpty()) return caminho;
        java.util.Map<Integer, List<Integer>> predecessores = new java.util.HashMap<>();
        for (Vertice v : vertices) predecessores.put(v.getId(), new ArrayList<>());
        for (Vertice v : vertices) {
            for (Aresta a : v.getAdjacencias()) {
                if (predecessores.containsKey(a.getDestinoId())) predecessores.get(a.getDestinoId()).add(v.getId());
            }
        }
        int inicio = vertices.get(0).getId();
        java.util.Set<Integer> visitados = new java.util.HashSet<>();
        caminho.add(inicio);
        visitados.add(inicio);
        long[] chamadas = {0};
        boolean achou = backtrackingHamilton(inicio, inicio, caminho, visitados, predecessores, chamadas);
        if (!achou) caminho.clear();
        return caminho;
    }

    private boolean backtrackingHamilton(int atual, int inicio, List<Integer> caminho, java.util.Set<Integer> visitados,
                                         java.util.Map<Integer, List<Integer>> predecessores, long[] chamadas) {
        if (++chamadas[0] > LIMITE_BACKTRACKING) return false;
        if (caminho.size() == vertices.size()) return pesoAresta(atual, inicio) >= 0;  // fecha o ciclo?

        Integer obrigatorio = null;
        for (Vertice v : vertices) {
            int id = v.getId();
            if (visitados.contains(id)) continue;
            // predecessores livres: não visitados ou o vértice atual
            int livres = 0;
            boolean soPeloAtual = true;
            for (int p : predecessores.get(id)) {
                if (!visitados.contains(p) || p == atual) {
                    livres++;
                    if (p != atual) soPeloAtual = false;
                }
            }
            if (livres == 0) return false;                       // ninguém mais consegue chegar em v
            if (soPeloAtual) {                                     // v só é alcançável pelo atual
                if (obrigatorio != null && obrigatorio != id) return false;
                obrigatorio = id;
            }
            boolean temSaida = false;
            for (Aresta a : v.getAdjacencias()) {
                if (!visitados.contains(a.getDestinoId()) || a.getDestinoId() == inicio) { temSaida = true; break; }
            }
            if (!temSaida) return false;                         // v não teria para onde ir
        }

        List<Integer> candidatos = new ArrayList<>();
        if (obrigatorio != null) {
            if (pesoAresta(atual, obrigatorio) < 0) return false;
            candidatos.add(obrigatorio);
        } else {
            for (Aresta a : getVerticeById(atual).getAdjacencias()) {
                if (!visitados.contains(a.getDestinoId()) && getVerticeById(a.getDestinoId()) != null) candidatos.add(a.getDestinoId());
            }
            candidatos.sort(java.util.Comparator.comparingInt((Integer c) -> {
                int saidasLivres = 0;
                for (Aresta a : getVerticeById(c).getAdjacencias()) if (!visitados.contains(a.getDestinoId())) saidasLivres++;
                return saidasLivres;
            }).thenComparingInt(c -> c));
        }
        for (int proximo : candidatos) {
            visitados.add(proximo);
            caminho.add(proximo);
            if (backtrackingHamilton(proximo, inicio, caminho, visitados, predecessores, chamadas)) return true;
            caminho.remove(caminho.size() - 1);
            visitados.remove(proximo);
        }
        return false;
    }

    /** Confere se a lista é um ciclo hamiltoniano: todos os vértices uma vez e arestas entre consecutivos. */
    public boolean ehCicloHamiltoniano(List<Integer> ciclo) {
        if (ciclo.size() != vertices.size() || new java.util.HashSet<>(ciclo).size() != ciclo.size()) return false;
        for (int i = 0; i < ciclo.size(); i++) {
            if (pesoAresta(ciclo.get(i), ciclo.get((i + 1) % ciclo.size())) < 0) return false;
        }
        return true;
    }

    /**
     * Algoritmo de Floyd: menor custo entre todos os pares de vértices.
     * As linhas/colunas seguem a ordem da lista de vértices; INF indica que não há caminho.
     */
    public static final int INF = Integer.MAX_VALUE / 4;

    public int[][] floyd() {
        int n = vertices.size();
        java.util.Map<Integer, Integer> indice = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) indice.put(vertices.get(i).getId(), i);
        int[][] d = new int[n][n];
        for (int i = 0; i < n; i++) {
            java.util.Arrays.fill(d[i], INF);
            d[i][i] = 0;
            for (Aresta a : vertices.get(i).getAdjacencias()) {
                Integer j = indice.get(a.getDestinoId());
                if (j != null) d[i][j] = Math.min(d[i][j], a.getPeso());
            }
        }
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];
        return d;
    }

    /**
     * Heurística do vizinho mais próximo para o Caixeiro Viajante (vista em aula): parte do primeiro
     * vértice e sempre vai ao vértice ainda não visitado mais próximo, usando a tabela de menores
     * distâncias do Floyd; no fim, volta ao início. Em empate, escolhe o vértice que aparece antes.
     * @return a rota (índices na lista de vértices) seguida do custo total na última posição;
     *         vazia se algum trecho for inalcançável
     */
    public List<Integer> rotaVizinhoMaisProximo(int[][] dist) {
        int n = vertices.size();
        List<Integer> rota = new ArrayList<>();
        if (n == 0) return rota;
        boolean[] visitado = new boolean[n];
        int atual = 0, custo = 0;
        rota.add(0);
        visitado[0] = true;
        for (int passo = 1; passo < n; passo++) {
            int melhor = -1;
            for (int j = 0; j < n; j++) {
                if (!visitado[j] && dist[atual][j] < INF && (melhor == -1 || dist[atual][j] < dist[atual][melhor])) melhor = j;
            }
            if (melhor == -1) return new ArrayList<>();
            custo += dist[atual][melhor];
            visitado[melhor] = true;
            rota.add(melhor);
            atual = melhor;
        }
        if (dist[atual][0] >= INF) return new ArrayList<>();
        custo += dist[atual][0];
        rota.add(custo);
        return rota;
    }

    /** Mesma heurística usando só as arestas diretas: devolve quantos vértices visita antes de travar. */
    private int vizinhoMaisProximoSoArestasDiretas() {
        if (vertices.isEmpty()) return 0;
        java.util.Set<Integer> visitados = new java.util.HashSet<>();
        int atual = vertices.get(0).getId();
        visitados.add(atual);
        while (true) {
            Aresta melhor = null;
            for (Aresta a : getVerticeById(atual).getAdjacencias()) {
                if (visitados.contains(a.getDestinoId()) || getVerticeById(a.getDestinoId()) == null) continue;
                if (melhor == null || a.getPeso() < melhor.getPeso()
                        || (a.getPeso() == melhor.getPeso() && a.getDestinoId() < melhor.getDestinoId())) melhor = a;
            }
            if (melhor == null) return visitados.size();
            atual = melhor.getDestinoId();
            visitados.add(atual);
        }
    }

    public void analisarHamilton() {
        int n = vertices.size();
        if (n < 3) {
            System.out.println("São necessários pelo menos 3 vértices para a análise hamiltoniana.");
            return;
        }
        System.out.println("\n=== HAMILTON: TEOREMAS, CICLO HAMILTONIANO E CAIXEIRO VIAJANTE ===");
        java.util.Map<Integer, java.util.Set<Integer>> viz = vizinhosSubjacente();

        // Teorema 2 (Dirac): grau de cada vértice >= |V|/2
        int grauMinimo = Integer.MAX_VALUE;
        for (Vertice v : vertices) grauMinimo = Math.min(grauMinimo, grauTotal(v.getId()));
        System.out.printf("Teorema 2 (Dirac): grau mínimo = %d; exigido >= %.1f (|V|/2) -> %s%n", grauMinimo, n / 2.0,
                grauMinimo >= n / 2.0 ? "atendido: o grafo possui ciclo hamiltoniano" : "não atendido (inconclusivo)");

        // Teorema 1 (Ore): soma dos graus de cada par não adjacente >= |V|
        int maiorSoma = -1;
        boolean oreAtendido = true;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int u = vertices.get(i).getId(), w = vertices.get(j).getId();
                if (viz.get(u).contains(w)) continue;
                int soma = grauTotal(u) + grauTotal(w);
                maiorSoma = Math.max(maiorSoma, soma);
                if (soma < n) oreAtendido = false;
            }
        }
        System.out.println("Teorema 1 (Ore): maior soma de graus entre vértices não adjacentes = " + maiorSoma
                + "; exigido >= " + n + " para todos os pares -> " + (oreAtendido ? "atendido" : "não atendido (inconclusivo)"));
        System.out.println("Os dois teoremas são condições suficientes, mas não necessárias: é preciso procurar o ciclo.");

        // Ciclo hamiltoniano por backtracking
        long t0 = System.currentTimeMillis();
        List<Integer> ciclo = cicloHamiltoniano();
        long ms = System.currentTimeMillis() - t0;
        if (ciclo.isEmpty()) {
            System.out.println("\nCiclo hamiltoniano: não encontrado (busca esgotada ou limite de tentativas atingido).");
        } else {
            System.out.println("\nCiclo hamiltoniano encontrado por percurso em profundidade com retorno em " + ms + " ms (verificado: "
                    + (ehCicloHamiltoniano(ciclo) ? "sim" : "não") + "):");
            imprimirSequencia(ciclo, true);
        }

        // Caixeiro Viajante: vizinho mais próximo com a tabela de distâncias do Floyd
        int[][] dist = floyd();
        List<Integer> rota = rotaVizinhoMaisProximo(dist);
        if (rota.isEmpty()) {
            System.out.println("\nCaixeiro Viajante: não foi possível montar a rota (há vértices inalcançáveis).");
        } else {
            int custo = rota.remove(rota.size() - 1);
            List<Integer> ids = new ArrayList<>();
            for (int idx : rota) ids.add(vertices.get(idx).getId());
            System.out.println("\nCaixeiro Viajante - heurística do vizinho mais próximo (distâncias pelo algoritmo de Floyd):");
            imprimirSequencia(ids, true);
            System.out.println("Custo total da rota: " + custo);
        }
        System.out.println("Usando só as arestas diretas (sem a tabela do Floyd), a heurística trava após "
                + vizinhoMaisProximoSoArestasDiretas() + " vértices.");
        System.out.println("=================================================================\n");
    }

    /** Imprime uma sequência de vértices pelos rótulos, 8 por linha; se fechar, repete o primeiro no fim. */
    private void imprimirSequencia(List<Integer> ids, boolean fechar) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(rotulo(ids.get(i)));
            if (i < ids.size() - 1 || fechar) sb.append(" -> ");
            if ((i + 1) % 8 == 0) sb.append("\n  ");
        }
        if (fechar && !ids.isEmpty()) sb.append(rotulo(ids.get(0)));
        System.out.println(sb);
    }

    // ------------------------------------------------------------------------------------------
    // n) ITEM 2 - Coloração por classe (Welsh e Powell) e k-partição
    // ------------------------------------------------------------------------------------------

    /**
     * Algoritmo de coloração por classe (Welsh e Powell), como visto em aula: cada classe de cor é
     * formada de uma vez, percorrendo os vértices que ainda não têm cor e colocando na classe atual
     * todo vértice sem vizinho nela. Trabalha no grafo subjacente (sem o sentido das arestas).
     * @param ordenarPorGrau true para considerar os vértices em ordem não crescente de grau
     *                       (a ordem recomendada); false para a ordem do arquivo
     */
    public List<List<Integer>> coloracaoPorClasse(boolean ordenarPorGrau) {
        java.util.Map<Integer, java.util.Set<Integer>> viz = vizinhosSubjacente();
        List<Integer> restantes = new ArrayList<>(viz.keySet());
        if (ordenarPorGrau) {
            restantes.sort(java.util.Comparator.comparingInt((Integer v) -> -viz.get(v).size()).thenComparingInt(v -> v));
        }
        List<List<Integer>> classes = new ArrayList<>();
        while (!restantes.isEmpty()) {
            List<Integer> classe = new ArrayList<>();
            List<Integer> recusados = new ArrayList<>();
            for (int v : restantes) {
                boolean temVizinhoNaClasse = false;
                for (int c : classe) if (viz.get(v).contains(c)) { temVizinhoNaClasse = true; break; }
                if (temVizinhoNaClasse) recusados.add(v);
                else classe.add(v);
            }
            classes.add(classe);
            restantes = recusados;
        }
        return classes;
    }

    /**
     * Verifica se os vértices formam um subgrafo completo (todos ligados entre si) no grafo
     * subjacente. Se k vértices formam um K_k, cada um precisa de uma cor diferente: χ(G) >= k.
     */
    public boolean ehClique(List<Integer> ids) {
        java.util.Map<Integer, java.util.Set<Integer>> viz = vizinhosSubjacente();
        for (int i = 0; i < ids.size(); i++) {
            if (!viz.containsKey(ids.get(i))) return false;
            for (int j = i + 1; j < ids.size(); j++) {
                if (!viz.get(ids.get(i)).contains(ids.get(j))) return false;
            }
        }
        return true;
    }

    /**
     * K4 do grafo do projeto: I, ii, V e vi da mesma tonalidade (a progressão I-vi-ii-V) estão
     * todos ligados entre si. Procura a primeira tonalidade em que isso acontece; vazio se nenhuma.
     */
    public List<Integer> cliqueDaProgressao() {
        for (Vertice v : vertices) {
            String r = v.getRotulo();
            if (r == null || !r.endsWith("_I")) continue;
            String tom = r.substring(0, r.length() - 2);
            List<Integer> ids = new ArrayList<>();
            for (String grau : new String[]{"I", "ii", "V", "vi"}) {
                Vertice w = getVerticeByRotulo(tom + "_" + grau);
                if (w != null) ids.add(w.getId());
            }
            if (ids.size() == 4 && ehClique(ids)) return ids;
        }
        return new ArrayList<>();
    }

    /**
     * O grafo subjacente é bipartido se der para pintá-lo com 2 cores. Usa o percurso em largura
     * do slide: a partir de cada vértice ainda não visitado, cada nó m visitado a partir de n recebe
     * a cor oposta à de n; no fim, nenhuma aresta pode ligar dois vértices da mesma cor.
     */
    public boolean ehBipartido() {
        java.util.Map<Integer, java.util.Set<Integer>> viz = vizinhosSubjacente();
        java.util.Map<Integer, Integer> cor = new java.util.HashMap<>();
        for (int vInicio : viz.keySet()) {
            if (cor.containsKey(vInicio)) continue;
            java.util.Deque<Integer> fila = new java.util.ArrayDeque<>();
            cor.put(vInicio, 0);                                  // visita e marca o nó inicial
            fila.add(vInicio);
            while (!fila.isEmpty()) {
                int n = fila.poll();
                int m;
                while ((m = noAdjacente(n, viz, cor.keySet())) != -1) {
                    cor.put(m, 1 - cor.get(n));                   // visitar m = pintar com a outra cor
                    fila.add(m);
                }
            }
        }
        for (int u : viz.keySet()) {
            for (int w : viz.get(u)) if (cor.get(u).equals(cor.get(w))) return false;
        }
        return true;
    }

    public void mostrarColoracao() {
        if (vertices.isEmpty()) {
            System.out.println("O grafo está vazio.");
            return;
        }
        System.out.println("\n=== COLORAÇÃO POR CLASSE (WELSH E POWELL) ===");
        List<List<Integer>> ordemArquivo = coloracaoPorClasse(false);
        List<List<Integer>> classes = coloracaoPorClasse(true);
        System.out.println("Vértices na ordem do arquivo: " + ordemArquivo.size() + " cores");
        System.out.println("Vértices em ordem não crescente de grau: " + classes.size() + " cores (efeito da rotulação)");
        for (int i = 0; i < classes.size(); i++) {
            System.out.println("\nCor " + (i + 1) + " (" + classes.get(i).size() + " vértices):");
            imprimirSequenciaSimples(classes.get(i));
        }
        List<Integer> clique = cliqueDaProgressao();
        if (clique.isEmpty()) {
            System.out.println("\nNúmero cromático: χ(G) <= " + classes.size() + " (não há o K4 da progressão I-vi-ii-V para comparar).");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int v : clique) sb.append(rotulo(v)).append(" ");
            System.out.println("\nOs acordes {" + sb.toString().trim() + "} (progressão I-vi-ii-V) estão todos ligados entre si:");
            System.out.println("formam um K4, então são necessárias pelo menos 4 cores.");
            if (classes.size() == 4) {
                System.out.println("Como a coloração usou 4 cores, o número cromático é χ(G) = 4.");
            } else {
                System.out.println("Número cromático: 4 <= χ(G) <= " + classes.size() + ".");
            }
        }
        System.out.println("O grafo é " + classes.size() + "-partido e " + (ehBipartido() ? "é" : "NÃO é") + " bipartido.");
        System.out.println("==============================================\n");
    }

    private void imprimirSequenciaSimples(List<Integer> ids) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(rotulo(ids.get(i)));
            if (i < ids.size() - 1) sb.append(", ");
            if ((i + 1) % 10 == 0 && i < ids.size() - 1) sb.append("\n  ");
        }
        System.out.println(sb);
    }

    // ------------------------------------------------------------------------------------------
    // o) ITEM 2 - Árvore geradora de custo mínimo (Kruskal, conferida com Prim)
    // ------------------------------------------------------------------------------------------

    /**
     * Arestas do grafo subjacente (sem sentido), cada par uma vez, com o menor peso entre os dois
     * sentidos. Cada aresta é {u, v, peso} com u < v.
     */
    private List<int[]> arestasSubjacentes() {
        java.util.Map<String, int[]> pares = new java.util.LinkedHashMap<>();
        for (Vertice v : vertices) {
            for (Aresta a : v.getAdjacencias()) {
                if (getVerticeById(a.getDestinoId()) == null || a.getDestinoId() == v.getId()) continue;
                int u = Math.min(v.getId(), a.getDestinoId()), w = Math.max(v.getId(), a.getDestinoId());
                String chave = u + "-" + w;
                int[] atual = pares.get(chave);
                if (atual == null || a.getPeso() < atual[2]) pares.put(chave, new int[]{u, w, a.getPeso()});
            }
        }
        return new ArrayList<>(pares.values());
    }

    /**
     * Algoritmo de Kruskal: ordena as arestas pelo peso (em empate, pelos vértices) e acrescenta à
     * árvore cada aresta u-v que não forme ciclo. Para saber se formaria ciclo, faz o percurso em
     * profundidade do slide a partir de u na árvore montada até o momento: se v for visitado, já
     * existe caminho entre u e v e a aresta é descartada. Se o grafo não for conexo, devolve uma floresta.
     */
    public List<int[]> kruskal() {
        List<int[]> arestas = arestasSubjacentes();
        arestas.sort((x, y) -> x[2] != y[2] ? Integer.compare(x[2], y[2])
                : x[0] != y[0] ? Integer.compare(x[0], y[0]) : Integer.compare(x[1], y[1]));
        java.util.Map<Integer, List<Integer>> floresta = new java.util.LinkedHashMap<>();
        for (Vertice v : vertices) floresta.put(v.getId(), new ArrayList<>());
        List<int[]> arvore = new ArrayList<>();
        for (int[] a : arestas) {
            if (arvore.size() == vertices.size() - 1) break;
            if (percursoProfundidade(a[0], floresta).contains(a[1])) continue;   // formaria ciclo
            floresta.get(a[0]).add(a[1]);
            floresta.get(a[1]).add(a[0]);
            arvore.add(a);
        }
        return arvore;
    }

    /**
     * Algoritmo de Prim (usado para conferir o custo do Kruskal): começa com um vértice na árvore e,
     * a cada passo, acrescenta a aresta de menor peso que liga um vértice da árvore a um vértice fora
     * dela. Devolve o custo total, ou -1 se o grafo não for conexo.
     */
    public int custoPrim() {
        if (vertices.isEmpty()) return 0;
        List<int[]> arestas = arestasSubjacentes();
        java.util.Set<Integer> naArvore = new java.util.HashSet<>();
        naArvore.add(vertices.get(0).getId());
        int custo = 0;
        while (naArvore.size() < vertices.size()) {
            int[] menor = null;
            for (int[] a : arestas) {
                boolean cruza = naArvore.contains(a[0]) != naArvore.contains(a[1]);
                if (cruza && (menor == null || a[2] < menor[2])) menor = a;
            }
            if (menor == null) return -1;                        // sobrou vértice inalcançável
            naArvore.add(menor[0]);
            naArvore.add(menor[1]);
            custo += menor[2];
        }
        return custo;
    }

    public void mostrarArvoreGeradoraMinima() {
        if (vertices.isEmpty()) {
            System.out.println("O grafo está vazio.");
            return;
        }
        System.out.println("\n=== ÁRVORE GERADORA DE CUSTO MÍNIMO (KRUSKAL) ===");
        System.out.println("Calculada no grafo subjacente (sem o sentido das arestas).");
        List<int[]> arvore = kruskal();
        int custo = 0, pivos = 0;
        for (int[] a : arvore) {
            custo += a[2];
            if (a[2] == 0) pivos++;
        }
        System.out.println("Trocas de acorde (arestas de peso > 0):");
        for (int[] a : arvore) {
            if (a[2] > 0) System.out.println("  " + rotulo(a[0]) + " - " + rotulo(a[1]) + "  (peso " + a[2] + ")");
        }
        System.out.println("\nArestas na árvore: " + arvore.size() + " (" + pivos + " pivôs de peso 0 e " + (arvore.size() - pivos) + " trocas de acorde)");
        if (arvore.size() < vertices.size() - 1) {
            System.out.println("O grafo não é conexo: o resultado é uma floresta geradora mínima.");
        }
        System.out.println("Custo total (Kruskal): " + custo);
        System.out.println("Verificação com o algoritmo de Prim: custo " + custoPrim());
        System.out.println("=================================================\n");
    }
}
