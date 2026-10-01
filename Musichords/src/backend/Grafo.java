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
 * Síntese: Implementação do grafo direcionado com peso nas arestas usando Lista de Adjacências.
 * Histórico de Alterações:
 * Data       | Autor   | Descrição
 * 22/09/2026 | Pedro   | Estruturação inicial da classe, atributos e método lerArquivo.
 * 23/09/2026 | Gustavo | Implementação da manipulação do grafo (inserir/remover vértices e arestas).
 * 27/09/2026 | Felipe  | Criação dos métodos mostrarGrafo, mostrarConteudoArquivo e gravarArquivo.
 * 28/09/2026 | Pedro   | Implementação do algoritmo DFS para análise de conexidade (FCONEX).
 * 30/09/2026 | Pedro   | Acréscimos para a interface gráfica: getVertices(), getVerticeById() público
 *                        e caminho mínimo por Dijkstra (nenhum método existente foi alterado).
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
    public void inserirVertice(int id, String rotulo) {
        Vertice novoVertice = new Vertice(id, rotulo);
        vertices.add(novoVertice);
    }

    // d) Inserir aresta
    public void inserirAresta(int origemId, int destinoId, int peso) {
        Aresta novaAresta = new Aresta(destinoId, peso);
        for(int i = 0; i < vertices.size(); i++) {
            if(vertices.get(i).getId() == origemId) {
                vertices.get(i).adicionarAresta(novaAresta);
            }
        }
    }

    // e) Remover vértice
    public void removerVertice(int id) {
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
            return;
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
    }

    // f) Remover aresta
    public void removerAresta(int origemId, int destinoId) {
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

    private boolean isSubjacenteConexo() {
        if (vertices.isEmpty()) return true;
        java.util.Set<Integer> visitados = new java.util.HashSet<>();
        java.util.Queue<Integer> fila = new java.util.LinkedList<>();
        
        java.util.Map<Integer, List<Integer>> adjNaoDirecionada = new java.util.HashMap<>();
        for (Vertice v : vertices) {
            adjNaoDirecionada.putIfAbsent(v.getId(), new ArrayList<>());
            for (Aresta a : v.getAdjacencias()) {
                adjNaoDirecionada.putIfAbsent(a.getDestinoId(), new ArrayList<>());
                adjNaoDirecionada.get(v.getId()).add(a.getDestinoId());
                adjNaoDirecionada.get(a.getDestinoId()).add(v.getId());
            }
        }
        
        fila.add(vertices.get(0).getId());
        visitados.add(vertices.get(0).getId());
        
        while (!fila.isEmpty()) {
            int atual = fila.poll();
            if (adjNaoDirecionada.containsKey(atual)) {
                for (int vizinho : adjNaoDirecionada.get(atual)) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        fila.add(vizinho);
                    }
                }
            }
        }
        return visitados.size() == vertices.size();
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
}
