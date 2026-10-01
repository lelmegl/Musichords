# Musichords

Modelagem de progressões harmônicas para guitarra via grafos de campos harmônicos
(Teoria dos Grafos / Engenharia de Software — Universidade Presbiteriana Mackenzie).

**Integrantes:** Gustavo Kiyoshi Ikeda (10439179) · Pedro Montarroyos de Pinho (10440213) · Felipe Marques Leite Martha (10437877)

## Como executar
## Engenharia de Software

Único requisito: **Java 11 ou mais novo** (qualquer JDK/JRE; o GitHub Codespaces já vem com Java).
Não há Maven, bibliotecas nem downloads: o projeto já vem com o `Musichords.jar` pronto.

```bash
cd Musichords
java -jar Musichords.jar            # interface no navegador -> http://localhost:8080
```
## Parte 2 Projeto de Grafos
```
cd Musichords
java -jar Musichords.jar console    # menu de texto da Parte 1 (opções a..j)
```

Ou use os atalhos: **Windows** → dois cliques em `executar.bat` · **Linux/macOS** → `./executar.sh`.

No computador o navegador abre sozinho. Se não abrir, acesse `http://localhost:8080`.
Para encerrar, pressione `Ctrl+C` no terminal.

### GitHub Codespaces

1. Abra o repositório em um Codespace.
2. No terminal: `java -jar Musichords.jar`
3. O VS Code mostra o aviso "Your application running on port 8080 is available" → **Open in Browser**
   (ou aba **PORTAS** → porta 8080 → ícone do globo). O próprio programa também imprime o endereço.

Com a pasta `.devcontainer/` incluída, o Codespace já inicia o Musichords e abre o navegador sozinho.

### IntelliJ / VS Code

- **IntelliJ**: *File → Open* na pasta `Musichords` e rode a configuração **"Musichords (navegador)"** (ou "console").
- **VS Code**: abra a pasta e rode **"Musichords (navegador)"** em *Run and Debug*.

Depois de editar o código-fonte, rode `compilar.bat` / `./compilar.sh` para gerar o `Musichords.jar` de novo
(precisa do JDK). Os arquivos da interface (`src/web/static`) também são lidos direto da pasta, então mudanças
no HTML/CSS/JS aparecem só recarregando a página quando o programa é executado pela IDE.

## Como funciona

```
navegador (HTML/CSS/JS)  ⇄  Servidor.java (HTTP do próprio JDK)  ⇄  ApiMusichords  ⇄  Grafo / domínio  ⇄  grafo.txt
```

- O `Servidor` usa `com.sun.net.httpserver`, que faz parte do JDK — nenhuma biblioteca externa.
- **Toda informação vem do back-end da Parte 1** (`Grafo`, `Vertice`, `Aresta`) carregado do **`grafo.txt`**:
  - as opções **a) ler, b) gravar, e) remover vértice, f) remover aresta, g) conteúdo, h) lista de adjacência e
    i) conexidade** chamam os próprios métodos do `Grafo`, e a tela mostra exatamente o texto que eles imprimem;
  - os acordes, graus e funções vêm do rótulo `Tonalidade_Grau` de cada vértice (ex.: `G_IV` = grau IV de Sol maior = C);
  - o custo das progressões é a soma dos **pesos das arestas**; as arestas de **peso 0** são os pivôs entre tonalidades;
  - inserir/remover pela aba do grafo altera o grafo em memória e o fluxo passa a usar os novos dados
    (use **b) Gravar** para salvar no `grafo.txt`).
- A interface só apresenta os dados; o único processamento no navegador é o som dos acordes (Web Audio).

## Interface

**Fluxo do aplicativo** — os seis passos do wireframe:

| Passo | Tela | Requisito |
|---|---|---|
| 1 | Toque no braço da guitarra (12 casas, uma nota por corda, exemplos) | RF03 |
| 2 | Identificação do acorde (tríades, tétrades, inversões, shape de referência) | RF04 |
| 3 | Mapeamento nos vértices do grafo (Caso 1 — Tônica, Caso 2 — Subdominante...) | RF01 |
| 4 | Progressões ordenadas pelo custo total + caminho mínimo (Dijkstra) até um acorde | RF02, RF06 |
| 5 | Shapes da progressão escolhida, com o peso de cada transição | RF07 |
| 6 | Extras: músicas de referência, escalas no braço, campo harmônico | RF05 |

**Grafo de campos harmônicos** — o menu a..i, Dijkstra entre dois vértices e o desenho do grafo
(tonalidades no círculo das quintas, pivôs em laranja; clique nos vértices para preencher origem/destino).

## Estrutura

```
Musichords.jar            programa pronto para rodar
grafo.txt                 dados do modelo (lidos pelo Grafo)
executar.bat / .sh        atalhos para rodar
compilar.bat / .sh        recompilam o .jar a partir de src/
src/
├── backend/   Grafo, Vertice, Aresta, Main          ← código da Parte 1
├── dominio/   Nota, Acorde, TipoAcorde, IdentificadorAcordes, CampoHarmonico, VerticeMusical,
│              ShapeAcorde, BancoShapes, Progressao, BuscaProgressoes, ConteudoExtra
└── web/       Servidor, ApiMusichords, Json
    └── static/   index.html, estilo.css, app.js     ← interface
```

### Acréscimos ao back-end

Nenhum método existente do `Grafo` foi alterado. Foram **acrescentados** `getVertices()` (somente leitura),
`getVerticeById()` passou de `private` para `public`, e `dijkstra(...)` (caminho mínimo com uma ou várias origens).
