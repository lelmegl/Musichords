import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Ponto de entrada do Musichords. Sobe um servidor HTTP usando apenas o JDK
 * (com.sun.net.httpserver) e entrega a interface (HTML/CSS/JS) e a API em JSON. Assim o
 * programa roda em qualquer computador ou no GitHub Codespaces só com o Java, sem bibliotecas.
 *   java -jar Musichords.jar            -> interface no navegador (http://localhost:8080)
 *   java -jar Musichords.jar console    -> menu de texto da Parte 1 (classe Main)
 *   java -jar Musichords.jar 9090       -> usa outra porta
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação do servidor.
 */
public class Servidor {

    private static final Map<String, String> TIPOS = new HashMap<>();

    static {
        TIPOS.put("html", "text/html; charset=utf-8");
        TIPOS.put("css", "text/css; charset=utf-8");
        TIPOS.put("js", "application/javascript; charset=utf-8");
        TIPOS.put("svg", "image/svg+xml");
        TIPOS.put("png", "image/png");
    }

    private final ApiMusichords api;

    public Servidor(ApiMusichords api) {
        this.api = api;
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            Main.main(new String[0]);
            return;
        }
        int porta = 8080;
        if (args.length > 0) {
            try {
                porta = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignorado) {
                // mantém a porta padrão
            }
        }

        File arquivo = localizarGrafo();
        ApiMusichords api = new ApiMusichords(arquivo.getPath());
        if (arquivo.exists()) {
            System.out.println(api.carregar(arquivo.getPath()) + "  (" + arquivo.getAbsolutePath() + ")");
        } else {
            System.out.println("Aviso: grafo.txt não encontrado. Coloque-o na mesma pasta do Musichords.jar "
                    + "ou leia pelo menu da aba \"Grafo\".");
        }

        boolean codespace = System.getenv("CODESPACES") != null;
        HttpServer servidor = null;
        for (int tentativa = 0; tentativa < 10 && servidor == null; tentativa++) {
            try {
                InetSocketAddress endereco = codespace
                        ? new InetSocketAddress(porta)
                        : new InetSocketAddress(InetAddress.getLoopbackAddress(), porta);
                servidor = HttpServer.create(endereco, 0);
            } catch (BindException ocupada) {
                porta++;
            }
        }
        if (servidor == null) {
            System.out.println("Não foi possível abrir uma porta entre 8080 e 8089.");
            return;
        }

        Servidor app = new Servidor(api);
        servidor.createContext("/", app::tratar);
        servidor.setExecutor(Executors.newSingleThreadExecutor()); // acesso ao grafo em sequência
        servidor.start();

        String url = "http://localhost:" + porta;
        System.out.println();
        System.out.println("=====================================================");
        System.out.println("  MUSICHORDS rodando em " + url);
        if (codespace) {
            String nome = System.getenv("CODESPACE_NAME");
            String dominio = System.getenv("GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN");
            if (nome != null && dominio != null) {
                System.out.println("  No Codespace: https://" + nome + "-" + porta + "." + dominio);
            }
            System.out.println("  (ou aba PORTAS -> porta " + porta + " -> abrir no navegador)");
        }
        System.out.println("  Para encerrar: Ctrl+C");
        System.out.println("=====================================================");
        abrirNavegador(url, codespace);
    }

    /** Procura o grafo.txt na pasta atual e, depois, na pasta onde está o .jar. */
    private static File localizarGrafo() {
        File atual = new File("grafo.txt");
        if (atual.exists()) return atual;
        try {
            File local = new File(Servidor.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File pasta = local.isFile() ? local.getParentFile() : local;
            for (int i = 0; i < 3 && pasta != null; i++) {
                File candidato = new File(pasta, "grafo.txt");
                if (candidato.exists()) return candidato;
                pasta = pasta.getParentFile();
            }
        } catch (Exception ignorado) {
            // segue com o caminho padrão
        }
        return atual;
    }

    private static void abrirNavegador(String url, boolean codespace) {
        if (codespace || GraphicsEnvironment.isHeadless()) return;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ignorado) {
            // o usuário pode abrir o endereço manualmente
        }
    }

    // ================================================================== roteamento

    private void tratar(HttpExchange ex) throws IOException {
        try {
            String caminho = ex.getRequestURI().getPath();
            if (caminho.startsWith("/api/")) {
                Map<String, String> p = parametros(ex);
                responder(ex, 200, "application/json; charset=utf-8", rota(caminho.substring(5), p).getBytes(StandardCharsets.UTF_8));
            } else {
                arquivoEstatico(ex, caminho.equals("/") ? "/index.html" : caminho);
            }
        } catch (Exception e) {
            String msg = Json.obj().put("ok", false).put("mensagem", "Erro: " + e).toString();
            responder(ex, 500, "application/json; charset=utf-8", msg.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String rota(String nome, Map<String, String> p) {
        switch (nome) {
            case "estado":
                return api.estado().toString();
            case "grafo":
                return api.grafo().toString();
            case "identificar": {
                String[] partes = p.getOrDefault("casas", "-1,-1,-1,-1,-1,-1").split(",");
                int[] casas = new int[6];
                for (int i = 0; i < 6; i++) casas[i] = i < partes.length ? Integer.parseInt(partes[i].trim()) : -1;
                return api.identificar(casas).toString();
            }
            case "progressoes":
                return api.progressoes(ApiMusichords.acorde(inteiro(p, "raiz"), p.get("tipo")),
                        inteiro(p, "tamanho"), "true".equals(p.get("modulacao"))).toString();
            case "caminho":
                return api.caminho(ApiMusichords.acorde(inteiro(p, "raiz"), p.get("tipo")),
                        ApiMusichords.acorde(inteiro(p, "destRaiz"), p.get("destTipo"))).toString();
            case "acordes":
                return api.acordes().toString();
            case "exemplos":
                return api.exemplos().toString();
            case "texto":
                return api.texto(p.getOrDefault("tipo", "")).toString();
            case "operacao":
                return api.operacao(p).toString();
            case "dijkstra":
                try {
                    return api.dijkstra(inteiro(p, "origem"), inteiro(p, "destino")).toString();
                } catch (NumberFormatException e) {
                    return Json.obj().put("ok", false).put("mensagem", "Informe origem e destino numéricos.").toString();
                }
            default:
                return Json.obj().put("ok", false).put("mensagem", "Rota desconhecida: " + nome).toString();
        }
    }

    private void arquivoEstatico(HttpExchange ex, String caminho) throws IOException {
        if (caminho.contains("..")) {
            responder(ex, 404, "text/plain", "não encontrado".getBytes(StandardCharsets.UTF_8));
            return;
        }
        byte[] dados = null;
        // 1) dentro do .jar (ou da pasta de classes compiladas)
        try (InputStream in = Servidor.class.getResourceAsStream("/static" + caminho)) {
            if (in != null) dados = ler(in);
        }
        // 2) direto da pasta src/web/static (útil ao editar o HTML sem recompilar)
        if (dados == null) {
            File f = new File("src/web/static" + caminho);
            if (f.isFile()) dados = Files.readAllBytes(f.toPath());
        }
        if (dados == null) {
            responder(ex, 404, "text/plain", "não encontrado".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String ext = caminho.substring(caminho.lastIndexOf('.') + 1);
        responder(ex, 200, TIPOS.getOrDefault(ext, "application/octet-stream"), dados);
    }

    private static void responder(HttpExchange ex, int status, String tipo, byte[] corpo) throws IOException {
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, corpo.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(corpo);
        }
    }

    private static byte[] ler(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    /** Junta os parâmetros da URL e do corpo (formulário) em um mapa. */
    private static Map<String, String> parametros(HttpExchange ex) throws IOException {
        Map<String, String> p = new HashMap<>();
        decodificar(ex.getRequestURI().getRawQuery(), p);
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            decodificar(new String(ler(ex.getRequestBody()), StandardCharsets.UTF_8), p);
        }
        return p;
    }

    private static void decodificar(String texto, Map<String, String> p) throws UnsupportedEncodingException {
        if (texto == null || texto.isEmpty()) return;
        for (String par : texto.split("&")) {
            int i = par.indexOf('=');
            String k = i < 0 ? par : par.substring(0, i);
            String v = i < 0 ? "" : par.substring(i + 1);
            p.put(URLDecoder.decode(k, "UTF-8"), URLDecoder.decode(v, "UTF-8"));
        }
    }

    private static int inteiro(Map<String, String> p, String chave) {
        return Integer.parseInt(p.getOrDefault(chave, "").trim());
    }
}
