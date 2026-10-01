/*
 * Musichords — interface no navegador.
 * Toda informação musical e do grafo vem do servidor Java (back-end + grafo.txt) pela API /api/...;
 * aqui ficam apenas a apresentação, a navegação entre os passos e o som (Web Audio).
 */
"use strict";

// ======================================================================= utilitários

async function api(rota, params = {}, metodo = "GET") {
  const corpo = new URLSearchParams(params).toString();
  const url = "/api/" + rota + (metodo === "GET" && corpo ? "?" + corpo : "");
  const resp = await fetch(url, metodo === "POST"
    ? { method: "POST", headers: { "Content-Type": "application/x-www-form-urlencoded" }, body: corpo }
    : {});
  return resp.json();
}

/** Cria elementos: h("div", {class: "x", onclick: f}, filho1, "texto", ...) */
function h(tag, attrs, ...filhos) {
  const el = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs || {})) {
    if (v === null || v === undefined || v === false) continue;
    if (k.startsWith("on")) el.addEventListener(k.slice(2), v);
    else if (k === "class") el.className = v;
    else if (k === "html") el.innerHTML = v;
    else el.setAttribute(k, v === true ? "" : v);
  }
  for (const f of achatar(filhos)) {
    if (f === null || f === undefined || f === false) continue;
    el.append(f instanceof Node ? f : document.createTextNode(String(f)));
  }
  return el;
}

const $ = (id) => document.getElementById(id);

/** Achata listas aninhadas de filhos (equivalente a Array.flat, para navegadores antigos). */
function achatar(lista) {
  const r = [];
  for (const x of lista) {
    if (Array.isArray(x)) r.push(...achatar(x));
    else r.push(x);
  }
  return r;
}

/** Substitui os filhos de um elemento (equivalente a replaceChildren). */
function trocar(el, ...filhos) {
  while (el.firstChild) el.removeChild(el.firstChild);
  for (const f of achatar(filhos)) if (f) el.append(f);
}
const SVGNS = "http://www.w3.org/2000/svg";
function s(tag, attrs, ...filhos) {
  const el = document.createElementNS(SVGNS, tag);
  for (const [k, v] of Object.entries(attrs || {})) {
    if (k.startsWith("on")) el.addEventListener(k.slice(2), v);
    else el.setAttribute(k, v);
  }
  for (const f of filhos) if (f !== null && f !== undefined) el.append(f instanceof Node ? f : document.createTextNode(String(f)));
  return el;
}

// ======================================================================= estado

const S = {
  estado: null,          // dados gerais do back-end (braço, cordas, contagens)
  passo: 0,
  casas: [-1, -1, -1, -1, -1, -1],
  ident: null,           // resultado de /api/identificar
  chaveBusca: null,
  tamanho: 4,
  modulacao: false,
  progressoes: [],
  escolhida: null,
  acordes: [],
  destino: "",
  caminhoAcorde: null,   // resultado do Dijkstra entre acordes
  exemplos: [],
  shapeIdx: 0,
  escala: "maior",
  grafo: null,
  caminhoGrafo: [],
  selecionados: [],
  cliqueOrigem: true,
};

// ======================================================================= som (Web Audio)

let audio = null;
function nota(freq, t) {
  const o = audio.createOscillator();
  const f = audio.createBiquadFilter();
  const g = audio.createGain();
  o.type = "triangle";
  o.frequency.value = freq;
  f.type = "lowpass";
  f.frequency.value = 2400;
  g.gain.setValueAtTime(0.0001, t);
  g.gain.exponentialRampToValueAtTime(0.22, t + 0.012);
  g.gain.exponentialRampToValueAtTime(0.0001, t + 1.8);
  o.connect(f).connect(g).connect(audio.destination);
  o.start(t);
  o.stop(t + 1.9);
}
function tocar(casas, atraso = 0) {
  try {
    audio = audio || new (window.AudioContext || window.webkitAudioContext)();
    const t0 = audio.currentTime + 0.05 + atraso;
    casas.forEach((c, i) => {
      if (c < 0) return;
      const midi = S.estado.midiCordas[i] + c;
      nota(440 * Math.pow(2, (midi - 69) / 12), t0 + i * 0.035);
    });
  } catch (e) { /* navegador sem áudio */ }
}
function tocarSequencia(lista) {
  lista.forEach((casas, i) => tocar(casas, i * 1.15));
}

// ======================================================================= componentes

/** Braço da guitarra (cordas 1ª a 6ª de cima para baixo, como no wireframe). */
function braco({ interativo = true, destaque = null, tonica = -1, bemol = false } = {}) {
  const nomes = bemol ? S.estado.bracoBemol : S.estado.braco;
  const tabela = h("table", { class: "braco" });
  const cab = h("tr", {}, h("th", {}));
  for (let c = 0; c <= 12; c++) cab.append(h("th", {}, c === 0 ? "solta" : c));
  tabela.append(cab);
  for (let linha = 0; linha < 6; linha++) {
    const corda = 5 - linha;
    const tr = h("tr", {}, h("td", { class: "corda-nome" }, S.estado.cordas[corda].toUpperCase()));
    for (let casa = 0; casa <= 12; casa++) {
      const classe = S.estado.bracoClasses[corda][casa];
      let estilo = "nota";
      if (interativo) {
        if (S.casas[corda] === casa) estilo += " selecionada";
      } else if (destaque && destaque.has(classe)) {
        estilo += classe === tonica ? " tonica" : " destaque";
      } else {
        estilo += " apagada";
      }
      const botao = h("button", {
        class: estilo,
        title: `Corda ${6 - corda} (${S.estado.cordas[corda]}) · ${casa === 0 ? "solta" : casa + "ª casa"}`,
        onclick: interativo ? () => alternarNota(corda, casa) : null,
      }, nomes[corda][casa]);
      const td = h("td", { class: casa === 0 ? "solta" : "casa" }, botao);
      td.style.setProperty("--fio", (1 + (corda < 3 ? (3 - corda) * 0.6 : 0)) + "px");
      tr.append(td);
    }
    tabela.append(tr);
  }
  const marc = h("tr", {}, h("td", {}));
  for (let c = 0; c <= 12; c++) marc.append(h("td", { class: "marcador" }, [3, 5, 7, 9].includes(c) ? "•" : c === 12 ? "••" : ""));
  tabela.append(marc);
  return h("div", { class: "caixa-braco" }, tabela);
}

/** Diagrama do shape (6 cordas verticais, 5 casas). */
function diagrama(shape, w = 200, alt = 220) {
  const svg = s("svg", { viewBox: `0 0 ${w} ${alt}`, width: w, height: alt });
  const mx = w * 0.18, topo = alt * 0.16, base = alt * 0.84;
  const larg = w - 2 * mx, pc = larg / 5, ph = (base - topo) / 5;
  const r = Math.min(pc, ph) * 0.34;
  const casas = shape ? shape.casas : [-1, -1, -1, -1, -1, -1];
  const max = Math.max(...casas), positivos = casas.filter((c) => c > 0);
  const inicio = shape && max > 5 ? Math.min(...positivos) : 1;
  for (let i = 0; i <= 5; i++) svg.append(s("line", { x1: mx, y1: topo + i * ph, x2: mx + larg, y2: topo + i * ph, stroke: "#B9BDCA", "stroke-width": 1.2 }));
  if (inicio === 1) svg.append(s("line", { x1: mx - 1, y1: topo, x2: mx + larg + 1, y2: topo, stroke: "#1F2430", "stroke-width": 5 }));
  for (let c = 0; c < 6; c++) {
    const x = mx + c * pc;
    svg.append(s("line", { x1: x, y1: topo, x2: x, y2: base, stroke: "#B9BDCA", "stroke-width": 1.2 }));
    svg.append(s("text", { x, y: base + alt * 0.08, "text-anchor": "middle", "font-size": 11, fill: "#6B7080" }, S.estado.cordas[c]));
  }
  if (inicio > 1) svg.append(s("text", { x: mx * 0.45, y: topo + ph / 2 + 4, "text-anchor": "middle", "font-size": 12, "font-weight": 700, fill: "#6B7080" }, inicio + "ª"));
  if (!shape) return svg;
  if (shape.pestana > 0) {
    const idx = casas.map((c, i) => (c === shape.pestana ? i : -1)).filter((i) => i >= 0);
    if (idx.length > 1) {
      const y = topo + (shape.pestana - inicio + 0.5) * ph;
      const x1 = mx + idx[0] * pc, x2 = mx + idx[idx.length - 1] * pc;
      svg.append(s("rect", { x: x1 - r, y: y - r, width: x2 - x1 + 2 * r, height: 2 * r, rx: r, fill: "#3355FF" }));
    }
  }
  casas.forEach((c, i) => {
    const x = mx + i * pc, yTopo = topo - alt * 0.065;
    if (c < 0) svg.append(s("text", { x, y: yTopo + 4, "text-anchor": "middle", "font-size": 13, "font-weight": 700, fill: "#D9473B" }, "x"));
    else if (c === 0) svg.append(s("circle", { cx: x, cy: yTopo, r: r * 0.55, fill: "none", stroke: "#1F2430", "stroke-width": 1.5 }));
    else svg.append(s("circle", { cx: x, cy: topo + (c - inicio + 0.5) * ph, r, fill: "#3355FF" }));
  });
  return svg;
}

function cartao(classe, ...filhos) {
  return h("div", { class: "cartao " + (classe || "") }, ...filhos);
}

// ======================================================================= passos

const PASSOS = [
  {
    curto: "Tocar no braço",
    titulo: "Toque no braço da guitarra",
    explicacao: "O usuário toca as notas de um acorde diretamente no braço interativo. Cada círculo corresponde a uma posição real no instrumento (corda × casa) — escolha no máximo uma casa por corda.",
    avancar: "Identificar acorde",
    pode: () => S.ident && S.ident.quantidade >= 3,
    render() {
      const q = S.ident ? S.ident.quantidade : 0;
      const aoVivo = q === 0 ? "Nenhuma nota selecionada"
        : "Notas: " + S.ident.notas + (q < 3 ? `  ·  faltam ${3 - q} nota(s) diferentes` : "");
      return [
        braco(),
        h("div", { class: "linha-ao-vivo" },
          h("strong", {}, aoVivo), h("span", { class: "esp" }),
          h("button", { class: "botao secundario", disabled: q < 3, onclick: () => tocar(S.casas) }, "▶ Ouvir"),
          h("button", { class: "botao tracejado", onclick: () => definirCasas([-1, -1, -1, -1, -1, -1]) }, "Limpar")),
        h("div", { class: "texto-dica" }, "Toque pelo menos 3 notas para formar um acorde. O sistema reconhece tríades maiores, menores e diminutas (e tétrades com sétima, reduzidas à tríade)."),
        h("div", { class: "chips" }, h("span", { class: "texto-suave" }, "Exemplos:"),
          S.exemplos.map((e) => h("button", { class: "chip-botao", onclick: () => definirCasas(e.casas) }, e.nome))),
      ];
    },
  },
  {
    curto: "Identificar acorde",
    titulo: "Identificação do acorde",
    explicacao: "O sistema identifica o acorde formado pelas notas selecionadas, comparando-as com as fórmulas de tríades e tétrades, e valida se ele existe no grafo de campos harmônicos.",
    pode: () => S.ident && S.ident.acorde && S.ident.origens.length > 0,
    render() {
      const r = S.ident;
      if (!r.acorde) {
        return [cartao("cartao-resultado",
          h("div", { class: "texto-suave" }, "Notas selecionadas: " + r.notas),
          h("div", { class: "acorde-grande erro" }, "Acorde não reconhecido"),
          h("div", { class: "texto-dica" }, "Essas notas não formam uma tríade (maior, menor ou diminuta) nem uma tétrade conhecida. Volte e ajuste as notas no braço."))];
      }
      const a = r.acorde;
      const obs = [];
      if (r.tetrade) obs.push(`Tétrade reduzida à tríade ${r.triade.cifra} para a busca no grafo.`);
      if (r.invertido) obs.push(`Baixo em ${r.baixo} (acorde invertido: ${a.cifra}/${r.baixo}).`);
      if (r.grafoVazio) obs.push("Nenhum grafo carregado: abra a aba \"Grafo de campos harmônicos\" e leia o grafo.txt.");
      else if (r.origens.length === 0) obs.push("Este acorde não aparece em nenhum vértice do grafo carregado.");
      else obs.push(`Encontrado em ${r.origens.length} ${r.origens.length === 1 ? "vértice" : "vértices"} do grafo.`);
      return [
        cartao("cartao-resultado",
          h("div", { class: "texto-suave" }, "Notas selecionadas: " + r.notas),
          h("div", { class: "acorde-grande" }, a.extenso.charAt(0).toUpperCase() + a.extenso.slice(1)),
          h("div", { class: "acorde-cifra" }, "Cifra: " + a.cifra),
          h("div", { class: "texto-dica" }, obs.join(" "))),
        h("div", { class: "linha-centro" },
          diagrama(r.shape, 170, 190),
          h("div", {},
            h("div", { class: "rotulo-secao" }, "SHAPE DE REFERÊNCIA"),
            h("div", { class: "texto-suave" }, `${r.triade.cifra}  ·  ${r.shape.tablatura}`),
            h("div", { class: "texto-suave" }, r.shape.descricao),
            h("button", { class: "botao secundario", style: "margin-top:8px", onclick: () => tocar(r.shape.casas) }, "▶ Ouvir este shape"))),
      ];
    },
  },
  {
    curto: "Mapear no grafo",
    titulo: "Mapeamento em grafos de campos harmônicos",
    explicacao: "O mesmo acorde físico pode ocupar mais de um vértice no grafo — cada um representando um papel funcional diferente (tônica, subdominante, dominante...) em uma tonalidade. Esses vértices são as origens da busca de progressões.",
    pode: () => S.ident && S.ident.origens && S.ident.origens.length > 0,
    render() {
      const origens = S.ident.origens;
      return [
        h("div", { class: "lista-rolavel" }, origens.map((o, i) => cartao("cartao-caso " + o.funcao,
          h("div", { class: "rotulo-caso" }, `CASO ${i + 1} — ${o.funcao.toUpperCase()}`),
          h("div", { class: "titulo-caso" }, o.descricao),
          h("div", { class: "texto-suave" }, `Vértice ${o.id} ("${o.rotulo}") · ${o.saidas} transições saindo deste vértice`),
          h("div", { class: "chips" }, o.campo.map((c) => h("span", { class: "chip" + (c.atual ? " ativo" : "") }, `${c.grau}  ${c.cifra}`)))))),
        h("div", { class: "texto-suave centro" }, `acorde físico ${S.ident.triade.cifra} → ${origens.length} vértices no grafo`),
      ];
    },
  },
  {
    curto: "Progressões por dificuldade",
    titulo: "Progressões sugeridas",
    explicacao: "O sistema percorre o grafo a partir dos vértices de origem e calcula, para cada progressão candidata, o custo total — a soma dos pesos das arestas, que representam o esforço de trocar os dedos entre um acorde e o próximo. As opções são ordenadas da mais fácil para a mais difícil. Selecione uma progressão para avançar.",
    pode: () => !!S.escolhida,
    async entrar() {
      const t = S.ident.triade;
      const chave = `${t.raiz}-${t.tipo}`;
      if (S.acordes.length === 0) S.acordes = (await api("acordes")).lista;
      if (chave !== S.chaveBusca) {
        S.chaveBusca = chave;
        S.caminhoAcorde = null;
        S.destino = "";
        await buscarProgressoes();
      }
    },
    render() {
      const t = S.ident.triade;
      const selTamanho = h("select", { onchange: async (e) => { S.tamanho = +e.target.value; await buscarProgressoes(); desenhar(); } },
        [3, 4, 5, 6].map((n) => h("option", { value: n, selected: n === S.tamanho }, n)));
      const mod = h("input", { type: "checkbox", style: "width:auto", checked: S.modulacao,
        onchange: async (e) => { S.modulacao = e.target.checked; await buscarProgressoes(); desenhar(); } });
      const lista = S.progressoes.length === 0
        ? [h("div", { class: "texto-dica" }, `Nenhuma progressão com ${S.tamanho} acordes parte deste acorde no grafo atual.`)]
        : S.progressoes.map((p, i) => cartaoProgressao(p, i === 0 ? "Mais fácil" : "custo " + p.custo, i === 0));

      const destinos = S.acordes.filter((a) => !(a.raiz === t.raiz && a.tipo === t.tipo));
      const selDestino = h("select", { onchange: (e) => { S.destino = e.target.value; desenhar(); } },
        h("option", { value: "" }, "Acorde de destino"),
        destinos.map((a) => h("option", { value: `${a.raiz}-${a.tipo}`, selected: S.destino === `${a.raiz}-${a.tipo}` }, `${a.cifra}  (${a.nome})`)));
      let resultado = null;
      if (S.caminhoAcorde) {
        resultado = S.caminhoAcorde.erro ? h("div", { class: "texto-dica" }, S.caminhoAcorde.erro)
          : cartaoProgressao(S.caminhoAcorde.progressao, "custo " + S.caminhoAcorde.progressao.custo, false);
      }
      return [
        h("div", { class: "controles" }, h("span", { class: "texto-suave" }, "Acordes:"), selTamanho,
          h("label", { class: "controles" }, mod, "Permitir modulação por acorde pivô")),
        h("div", { class: "texto-dica" }, "Da mais fácil para a mais difícil: menor custo de troca de dedos primeiro; empates desfeitos pela dificuldade dos shapes."),
        h("div", { class: "lista-rolavel" }, lista,
          cartao("cartao-dijkstra",
            h("div", { class: "rotulo-secao" }, "CHEGAR A UM ACORDE ESPECÍFICO (DIJKSTRA)"),
            h("div", { class: "texto-suave" }, "Encontra a sequência de menor custo do acorde tocado até o acorde escolhido, podendo atravessar tonalidades pelas arestas de pivô."),
            h("div", { class: "controles" }, selDestino,
              h("button", { class: "botao secundario", disabled: !S.destino, onclick: calcularCaminhoAcorde }, "Calcular caminho mínimo")),
            resultado)),
      ];
    },
  },
  {
    curto: "Ver shapes",
    titulo: "Shapes da progressão escolhida",
    explicacao: "A progressão escolhida é exibida acorde a acorde, com o shape físico correspondente no braço do instrumento e o peso da transição vinda do acorde anterior.",
    avancar: "Ver extras",
    pode: () => true,
    entrar() { S.shapeIdx = 0; },
    render() {
      const etapas = S.escolhida.etapas;
      const i = S.shapeIdx, e = etapas[i];
      const transicao = i === 0 ? "Acorde inicial da progressão"
        : `Transição de ${etapas[i - 1].acorde.nome} → ${e.acorde.nome}: peso ${e.peso}` + (e.pivo ? "  (acorde pivô: muda de tonalidade com custo 0)" : "");
      return [
        h("div", { class: "linha-centro" },
          h("button", { class: "botao redondo", disabled: i === 0, onclick: () => { S.shapeIdx--; desenhar(); } }, "‹"),
          diagrama(e.shape, 200, 220),
          h("button", { class: "botao redondo", disabled: i === etapas.length - 1, onclick: () => { S.shapeIdx++; desenhar(); } }, "›")),
        h("div", { class: "nome-shape" }, e.acorde.nome),
        h("div", { class: "texto-suave centro" }, `${i + 1} de ${etapas.length}`),
        h("div", { class: "chips", style: "justify-content:center" },
          etapas.map((et, k) => h("button", { class: "chip-botao" + (k === i ? " ativo" : ""), onclick: () => { S.shapeIdx = k; desenhar(); } }, et.acorde.nome))),
        h("div", { class: "texto-suave centro" }, `${e.acorde.cifra}  ·  ${e.shape.tablatura}  ·  ${e.shape.descricao}`),
        h("div", { class: "texto-suave centro" }, e.papeis.join("  →  ")),
        h("div", { class: "texto-custo centro" }, transicao),
        h("div", { class: "linha-centro", style: "gap:8px" },
          h("button", { class: "botao secundario", onclick: () => tocar(e.shape.casas) }, "▶ Ouvir acorde"),
          h("button", { class: "botao secundario", onclick: () => tocarSequencia(etapas.map((x) => x.shape.casas)) }, "▶ Ouvir progressão")),
      ];
    },
  },
  {
    curto: "Extras",
    titulo: "Continue treinando",
    explicacao: "Conteúdo complementar: músicas de referência que usam a mesma sequência de graus, escalas relacionadas à tonalidade e o campo harmônico completo, para o usuário continuar praticando.",
    avancar: "Recomeçar",
    pode: () => true,
    render() {
      const x = S.escolhida.extras;
      const esc = S.escala === "maior" ? x.escalaMaior : x.pentatonica;
      const musicas = x.musicas.length === 0
        ? [h("div", { class: "texto-suave" }, `Nenhuma música catalogada para a sequência ${S.escolhida.padrao}. Experimente tocá-la com metrônomo, começando em 60 bpm.`)]
        : [...x.musicas.map((m) => h("div", {}, "•  " + m)),
          h("div", { class: "texto-suave" }, `Sequência de graus: ${S.escolhida.padrao} — vale em qualquer tonalidade.`)];
      return [
        cartao("", h("div", { class: "rotulo-secao" }, "MÚSICAS COM ESSA PROGRESSÃO"), musicas),
        cartao("",
          h("div", { class: "rotulo-secao" }, "ESCALAS PARA TREINAR — " + x.tonalidade.toUpperCase()),
          h("div", { class: "chips" },
            h("button", { class: "chip-botao" + (S.escala === "maior" ? " ativo" : ""), onclick: () => { S.escala = "maior"; desenhar(); } }, x.escalaMaior.nome),
            h("button", { class: "chip-botao" + (S.escala === "penta" ? " ativo" : ""), onclick: () => { S.escala = "penta"; desenhar(); } }, x.pentatonica.nome)),
          h("div", {}, "Notas: " + esc.texto + "   (a tônica aparece em destaque no braço)"),
          braco({ interativo: false, destaque: new Set(esc.notas), tonica: esc.tonica, bemol: x.bemol })),
        cartao("",
          h("div", { class: "rotulo-secao" }, "CAMPO HARMÔNICO DE " + x.tonalidade.toUpperCase()),
          h("div", { class: "campo-harmonico" }, x.campo.map((c) => h("div", { class: `item-campo ${c.funcao}${c.usado ? " usado" : ""}` },
            h("div", { class: "g" }, c.grau), h("div", { class: "c" }, c.cifra), h("div", { class: "f" }, c.funcao))))),
      ];
    },
  },
];

function cartaoProgressao(p, selo, destaque) {
  const sel = S.escolhida && S.escolhida.cifras === p.cifras && S.escolhida.custo === p.custo;
  return h("div", { class: "cartao cartao-progressao" + (sel ? " selecionado" : ""), onclick: () => { S.escolhida = p; desenhar(); } },
    h("div", { class: "topo-progressao" }, h("span", { class: "titulo-progressao" }, p.nomes), h("span", { class: "selo" + (destaque ? " destaque" : "") }, selo)),
    h("div", { class: "texto-suave" }, `${p.cifras}  ·  ${p.graus}  ·  ${p.tonalidades}`),
    h("div", { class: "texto-custo" }, `custo físico total: ${p.custo}   ·   dificuldade dos shapes: ${p.dificuldade}`),
    h("div", {}, p.descricao));
}

// ======================================================================= ações do fluxo

async function definirCasas(casas) {
  S.casas = casas.slice();
  S.ident = await api("identificar", { casas: S.casas.join(",") });
  desenhar();
}

function alternarNota(corda, casa) {
  const novas = S.casas.slice();
  novas[corda] = novas[corda] === casa ? -1 : casa;
  definirCasas(novas);
}

async function buscarProgressoes() {
  const t = S.ident.triade;
  const r = await api("progressoes", { raiz: t.raiz, tipo: t.tipo, tamanho: S.tamanho, modulacao: S.modulacao });
  S.progressoes = r.lista;
  S.escolhida = null;
}

async function calcularCaminhoAcorde() {
  const t = S.ident.triade;
  const [raiz, tipo] = S.destino.split("-");
  S.caminhoAcorde = await api("caminho", { raiz: t.raiz, tipo: t.tipo, destRaiz: raiz, destTipo: tipo });
  desenhar();
}

async function irPara(i) {
  S.passo = Math.max(0, Math.min(i, PASSOS.length - 1));
  const p = PASSOS[S.passo];
  if (p.entrar) await p.entrar();
  desenhar();
  window.scrollTo({ top: 0 });
}

function reiniciarFluxo() {
  S.casas = [-1, -1, -1, -1, -1, -1];
  S.ident = { quantidade: 0, notas: "{}", acorde: null, origens: [] };
  S.chaveBusca = null;
  S.progressoes = [];
  S.escolhida = null;
  S.caminhoAcorde = null;
  S.acordes = [];
  irPara(0);
}

function desenhar() {
  const p = PASSOS[S.passo];
  $("numero-passo").textContent = S.passo === PASSOS.length - 1 ? "EXTRAS" : "PASSO " + (S.passo + 1);
  $("titulo-passo").textContent = p.titulo;
  trocar($("conteudo"), ...p.render());
  $("avancar").textContent = p.avancar || "Avançar";
  $("avancar").disabled = !p.pode();
  $("voltar").disabled = S.passo === 0;

  trocar($("pontos"), ...PASSOS.map((_, i) => h("span", { class: i === S.passo ? "atual" : i < S.passo ? "feito" : "" })));
  $("passo-lateral").textContent = `Passo ${S.passo + 1}/${PASSOS.length}`;
  $("explicacao").textContent = p.explicacao;
  trocar($("lista-passos"), ...PASSOS.map((x, i) => h("li", {
    class: i === S.passo ? "atual" : i < S.passo ? "feito" : "",
    onclick: i < S.passo ? () => irPara(i) : null,
  }, `${i + 1}. ${x.curto}`)));
  const a = S.passo >= 1 && S.ident && S.ident.acorde ? S.ident.triade : null;
  trocar($("resumo"), 
    h("div", {}, "Acorde: " + (a ? `${a.cifra} (${a.extenso})` : "—")),
    h("div", {}, "Progressão: " + (S.escolhida ? S.escolhida.cifras : "—")),
    S.escolhida ? h("div", {}, "Custo físico total: " + S.escolhida.custo) : null);
}

$("avancar").addEventListener("click", () => {
  if (S.passo === PASSOS.length - 1) reiniciarFluxo();
  else irPara(S.passo + 1);
});
$("voltar").addEventListener("click", () => irPara(S.passo - 1));

// ======================================================================= aba do grafo

function status(msg, ok = true) {
  $("status").textContent = msg;
  $("status").className = "status" + (ok ? "" : " erro");
}

function mostrarSub(nome) {
  document.querySelectorAll(".sub-aba").forEach((b) => b.classList.toggle("ativa", b.dataset.sub === nome));
  ["visual", "lista", "arquivo", "conexidade", "dijkstra"].forEach((n) => $("sub-" + n).classList.toggle("oculto", n !== nome));
}

async function recarregarGrafo() {
  S.estado = await api("estado");
  S.grafo = await api("grafo");
  $("contagem").textContent = `Tipo 6 (direcionado com peso nas arestas)  ·  ${S.estado.vertices} vértices  ·  ${S.estado.arestas} arestas`;
  if (!$("g-caminho").value) $("g-caminho").value = S.estado.arquivo;
  const [l, a, c] = await Promise.all([api("texto", { tipo: "lista" }), api("texto", { tipo: "arquivo" }), api("texto", { tipo: "conexidade" })]);
  $("sub-lista").textContent = l.texto;
  $("sub-arquivo").textContent = a.texto;
  $("sub-conexidade").textContent = c.texto;
  S.caminhoGrafo = [];
  S.selecionados = [];
  desenharGrafo();
}

async function operacao(op) {
  const campos = {
    ler: { caminho: $("g-caminho").value },
    gravar: { caminho: $("g-caminho").value },
    inserirVertice: { id: $("g-id").value, rotulo: $("g-rotulo").value },
    removerVertice: { id: $("g-id").value },
    inserirAresta: { origem: $("g-origem").value, destino: $("g-destino").value, peso: $("g-peso").value },
    removerAresta: { origem: $("g-origem").value, destino: $("g-destino").value },
  }[op];
  const r = await api("operacao", Object.assign({ op }, campos), "POST");
  status(r.mensagem, r.ok);
  if (r.ok && op !== "gravar") {
    await recarregarGrafo();
    reiniciarFluxo(); // o fluxo passa a usar o grafo alterado
  }
}

async function dijkstraGrafo() {
  const r = await api("dijkstra", { origem: $("g-d-origem").value, destino: $("g-d-destino").value });
  if (!r.ok) { status(r.mensagem, false); return; }
  $("sub-dijkstra").textContent = r.texto;
  S.caminhoGrafo = r.caminho;
  desenharGrafo();
  status(r.custo >= 0 ? `Caminho mínimo: custo ${r.custo} (destacado na visualização).` : "O destino não é alcançável a partir da origem.", r.custo >= 0);
  mostrarSub("dijkstra");
}

/** Desenho do grafo: tonalidades no círculo das quintas, 7 graus em cada "cacho". */
function desenharGrafo() {
  const W = 1000, H = 760, cx = W / 2, cy = H / 2;
  const R = 268, rc = 52, rn = 15;
  const cores = { "Tônica": "#5B7CFA", "Subdominante": "#3BA776", "Dominante": "#E0623B" };
  const pos = {};
  const soltos = [];
  for (const v of S.grafo.vertices) {
    if (v.tonica === undefined) { soltos.push(v); continue; }
    const ang = (-90 + ((v.tonica * 7) % 12) * 30) * Math.PI / 180;
    const kx = cx + R * Math.cos(ang), ky = cy + R * Math.sin(ang);
    const ag = (-90 + v.grau * 360 / 7) * Math.PI / 180;
    pos[v.id] = [kx + rc * Math.cos(ag), ky + rc * Math.sin(ag)];
  }
  soltos.forEach((v, i) => { pos[v.id] = [cx + (i - (soltos.length - 1) / 2) * rn * 3, cy]; });

  const svg = s("svg", { viewBox: `0 0 ${W} ${H}`, preserveAspectRatio: "xMidYMid meet" });
  const defs = s("defs");
  for (const [id, cor] of [["seta-p", "#E8862A"], ["seta-c", "#3355FF"]]) {
    defs.append(s("marker", { id, viewBox: "0 0 10 10", refX: 9, refY: 5, markerWidth: 6, markerHeight: 6, orient: "auto-start-reverse" },
      s("path", { d: "M0,0 L10,5 L0,10 z", fill: cor })));
  }
  svg.append(defs);

  const noCaminho = new Set();
  for (let i = 0; i + 1 < S.caminhoGrafo.length; i++) noCaminho.add(S.caminhoGrafo[i] + ">" + S.caminhoGrafo[i + 1]);
  const linha = (a, b, cor, larg, marcador) => {
    const [x1, y1] = pos[a], [x2, y2] = pos[b];
    const d = Math.hypot(x2 - x1, y2 - y1) || 1, ux = (x2 - x1) / d, uy = (y2 - y1) / d;
    const attrs = { x1: x1 + ux * rn, y1: y1 + uy * rn, x2: x2 - ux * (rn + 2), y2: y2 - uy * (rn + 2), stroke: cor, "stroke-width": larg };
    if (marcador) attrs["marker-end"] = `url(#${marcador})`;
    return s("line", attrs);
  };
  for (const v of S.grafo.vertices) {
    for (const a of v.adjacencias) {
      if (!pos[v.id] || !pos[a.destino] || noCaminho.has(v.id + ">" + a.destino)) continue;
      svg.append(a.peso === 0 ? linha(v.id, a.destino, "#E8862A", 2, "seta-p") : linha(v.id, a.destino, "rgba(154,160,176,.35)", 1));
    }
  }
  for (let i = 0; i + 1 < S.caminhoGrafo.length; i++) {
    if (pos[S.caminhoGrafo[i]] && pos[S.caminhoGrafo[i + 1]]) svg.append(linha(S.caminhoGrafo[i], S.caminhoGrafo[i + 1], "#3355FF", 4, "seta-c"));
  }

  const vistos = new Set();
  for (const v of S.grafo.vertices) {
    if (v.tonica === undefined || vistos.has(v.tonica)) continue;
    vistos.add(v.tonica);
    const ang = (-90 + ((v.tonica * 7) % 12) * 30) * Math.PI / 180;
    const rr = R + rc + 30;
    svg.append(s("text", { x: cx + rr * Math.cos(ang), y: cy + rr * Math.sin(ang) + 5, "text-anchor": "middle", "font-size": 17, "font-weight": 700, fill: "#1F2430" }, v.tonalidade));
  }

  const destaque = new Set([...S.caminhoGrafo, ...S.selecionados]);
  for (const v of S.grafo.vertices) {
    const p = pos[v.id];
    if (!p) continue;
    const forte = destaque.has(v.id);
    const r = forte ? rn * 1.25 : rn;
    const g = s("g", { class: "no", onclick: () => cliqueVertice(v) },
      s("title", {}, `Vértice ${v.id} · ${v.rotulo}` + (v.cifra ? ` · ${v.cifra} (${v.funcao}) · ${v.descricao}` : "") + ` · ${v.adjacencias.length} transições`),
      forte ? s("circle", { cx: p[0], cy: p[1], r: r + 4, fill: "none", stroke: "#3355FF", "stroke-width": 3 }) : null,
      s("circle", { cx: p[0], cy: p[1], r, fill: cores[v.funcao] || "#8A8F9C" }),
      s("text", { x: p[0], y: p[1] + 4, "text-anchor": "middle", "font-size": 11, "font-weight": 700, fill: "#fff" }, v.grauRomano || v.id));
    svg.append(g);
  }

  let ly = 24;
  for (const [nome, cor] of Object.entries(cores)) {
    svg.append(s("circle", { cx: 20, cy: ly, r: 7, fill: cor }), s("text", { x: 34, y: ly + 5, "font-size": 14, fill: "#1F2430" }, nome));
    ly += 24;
  }
  svg.append(s("line", { x1: 12, y1: ly, x2: 28, y2: ly, stroke: "#E8862A", "stroke-width": 3 }), s("text", { x: 34, y: ly + 5, "font-size": 14, fill: "#1F2430" }, "Aresta de pivô (peso 0)"));
  if (S.caminhoGrafo.length) {
    ly += 24;
    svg.append(s("line", { x1: 12, y1: ly, x2: 28, y2: ly, stroke: "#3355FF", "stroke-width": 4 }), s("text", { x: 34, y: ly + 5, "font-size": 14, fill: "#1F2430" }, "Caminho mínimo"));
  }
  trocar($("svg-grafo"), svg);
}

function cliqueVertice(v) {
  $("g-id").value = v.id;
  $("g-rotulo").value = v.rotulo;
  if (S.cliqueOrigem) {
    $("g-origem").value = v.id; $("g-destino").value = "";
    $("g-d-origem").value = v.id; $("g-d-destino").value = "";
    S.selecionados = [v.id];
    status(`Vértice ${v.id} (${v.rotulo}) como origem — clique em outro vértice para o destino.`);
  } else {
    $("g-destino").value = v.id;
    $("g-d-destino").value = v.id;
    S.selecionados.push(v.id);
    status(`Vértice ${v.id} (${v.rotulo}) como destino.`);
  }
  S.cliqueOrigem = !S.cliqueOrigem;
  desenharGrafo();
}

document.querySelectorAll("[data-op]").forEach((b) => b.addEventListener("click", () => operacao(b.dataset.op)));
document.querySelectorAll("[data-sub]").forEach((b) => b.addEventListener("click", () => mostrarSub(b.dataset.sub)));
$("g-dijkstra").addEventListener("click", dijkstraGrafo);
document.querySelectorAll(".aba").forEach((b) => b.addEventListener("click", () => {
  document.querySelectorAll(".aba").forEach((x) => x.classList.toggle("ativa", x === b));
  $("aba-fluxo").classList.toggle("oculto", b.dataset.aba !== "fluxo");
  $("aba-grafo").classList.toggle("oculto", b.dataset.aba !== "grafo");
}));

// ======================================================================= início

(async function iniciar() {
  try {
    await recarregarGrafo();
    S.exemplos = (await api("exemplos")).lista;
    status(S.estado.vertices > 0 ? `Grafo carregado de ${S.estado.arquivo}`
      : "Nenhum grafo carregado: informe o caminho do grafo.txt e clique em a) Ler.", S.estado.vertices > 0);
    reiniciarFluxo();
  } catch (e) {
    document.body.prepend(h("div", { class: "cartao erro", style: "margin:12px" },
      "Não foi possível falar com o servidor Java. Verifique se o Musichords.jar continua rodando no terminal."));
  }
})();
