# Musichords

> **Bridging the gap between abstract music theory and physical instrumental execution.**

---

## 🌍 Alinhamento com os Objetivos de Desenvolvimento Sustentável (ODS)

* **ODS 4 – Educação de Qualidade:** O Musichords democratiza o acesso ao aprendizado de teoria musical aplicada, traduzindo conceitos harmônicos abstratos (campos harmônicos, funções tonais) em uma representação visual e interativa diretamente ligada à posição física dos acordes no braço da guitarra, removendo uma das principais barreiras de entrada para estudantes do instrumento.
* **ODS 9 – Indústria, Inovação e Infraestrutura:** O projeto propõe uma inovação tecnológica de baixo custo (aplicativo) que amplia o acesso a ferramentas educacionais musicais, contribuindo para a democratização de recursos de aprendizagem baseados em software.

---

## 📋 1. Descrição da Proposta

### 1.1. Contextualização
Iniciantes de guitarra costumam aprender acordes isolados, mas têm dificuldade em conectar a teoria harmônica (quais acordes "combinam" entre si dentro de uma tonalidade) com a execução física no braço do instrumento. Essa lacuna entre teoria abstrata e prática instrumental é um dos principais obstáculos enfrentados por autodidatas. 

O **Musichords** se insere nesse cenário como um aplicativo inovador que identifica o acorde tocado pelo usuário no braço da guitarra e sugere progressões harmonicamente válidas e fisicamente executáveis, modelando os campos harmônicos das tonalidades como um grafo.

### 1.2. Objetivo
Desenvolver um aplicativo que, a partir das notas identificadas no braço da guitarra, **reconheça o acorde tocado**, **mapeie seus possíveis papéis funcionais** dentro de diferentes tonalidades (maiores e menores naturais) e **sugira**, por meio de busca em um grafo direcionado e ponderado, **progressões de acordes** harmonicamente válidas e fisicamente executáveis, de acordo com o sentimento (alegre/triste) desejado pelo usuário.

### 1.3. Motivação
A teoria musical é frequentemente ensinada de forma desconectada da prática instrumental, o que desmotiva iniciantes e prolonga a curva de aprendizado. 

Um sistema que traduz automaticamente teoria em posições físicas concretas no braço do instrumento possui:
* **Valor prático imediato:** Acelera o aprendizado e a autonomia de qualquer guitarrista ou violonista.
* **Valor acadêmico e tecnológico:** Aplica conceitos avançados de **Teoria dos Grafos** (busca de caminhos, ponderação de arestas, subgrafos) a um domínio real e de alto interesse cotidiano.

---

## 🚀 Funcionalidades Principais

1. **Reconhecimento de Acordes:** Identificação do acorde executado pelo usuário no instrumento.
2. **Mapeamento de Funções Tonais:** Tradução visual dos campos harmônicos (maior e menor natural) diretamente no braço da guitarra.
3. **Gerador de Progressões Baseado em Grafos:** Sugestão inteligente de caminhos harmônicos utilizando grafos direcionados e ponderados.
4. **Filtro por Sentimento:** Seleção de progressões orientada pela atmosfera desejada (ex: progressões alegres, melancólicas, tensas ou resolvidas).

---

## 🛠️ Tecnologias Envolvidas
* **Processamento de Sinal / Áudio:** Reconhecimento de notas e acordes no braço da guitarra.
* **Teoria dos Grafos:** Modelagem de campos harmônicos e transições de acordes.
* **Interface Interativa:** Visualização dinâmica do braço da guitarra (*fretboard*).

---
