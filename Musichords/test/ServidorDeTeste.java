import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Sobe o servidor real do Musichords (Servidor.criarServidor) numa porta livre, com
 * uma CÓPIA do grafo.txt numa pasta temporária, e faz requisições HTTP de verdade a ele.
 * Assim cada teste começa com o grafo original e nenhum teste altera o grafo.txt do projeto.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 20/10/2026 | Pedro | Criação da classe.
 */
final class ServidorDeTeste implements AutoCloseable {

    /** Resposta HTTP recebida pelo teste. */
    static final class Resposta {
        final int status;
        final String tipo;
        final String corpo;

        Resposta(int status, String tipo, String corpo) {
            this.status = status;
            this.tipo = tipo;
            this.corpo = corpo;
        }

        NoJson json() {
            return NoJson.ler(corpo);
        }

        @Override
        public String toString() {
            return "HTTP " + status + " " + (corpo.length() > 300 ? corpo.substring(0, 300) + "..." : corpo);
        }
    }

    final File grafo;
    final ApiMusichords api;
    private final HttpServer servidor;
    private final ExecutorService executor;
    private final String base;
    private final HttpClient http;

    private ServidorDeTeste(File grafo, ApiMusichords api, HttpServer servidor, ExecutorService executor) {
        this.grafo = grafo;
        this.api = api;
        this.servidor = servidor;
        this.executor = executor;
        this.base = "http://127.0.0.1:" + servidor.getAddress().getPort();
        this.http = HttpClient.newBuilder()
                .proxy(HttpClient.Builder.NO_PROXY)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /** Copia o grafo.txt do projeto para a pasta temporária e sobe o servidor numa porta livre. */
    static ServidorDeTeste iniciar(File pastaTemporaria) throws IOException {
        File original = new File("grafo.txt");
        if (!original.isFile()) {
            throw new IllegalStateException("grafo.txt não encontrado em " + original.getAbsolutePath()
                    + ". Rode os testes a partir da pasta Musichords (./testar.sh).");
        }
        File copia = new File(pastaTemporaria, "grafo.txt");
        Files.copy(original.toPath(), copia.toPath(), StandardCopyOption.REPLACE_EXISTING);

        ApiMusichords api = new ApiMusichords(copia.getPath());
        api.carregar(copia.getPath());

        // Porta 0 = o sistema escolhe uma porta livre (vários testes/CI em paralelo sem conflito)
        HttpServer servidor = Servidor.criarServidor(api, new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        // Mesmo modelo do servidor real (uma thread), mas com um executor que o teste consegue encerrar
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "musichords-teste");
            t.setDaemon(true);
            return t;
        });
        servidor.setExecutor(executor);
        servidor.start();
        return new ServidorDeTeste(copia, api, servidor, executor);
    }

    Resposta get(String caminho) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + caminho))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        return enviar(req);
    }

    /** POST de formulário (application/x-www-form-urlencoded), como o app.js faz. Ex.: post("/api/operacao", "op", "ler"). */
    Resposta post(String caminho, String... chavesEValores) throws IOException, InterruptedException {
        StringBuilder corpo = new StringBuilder();
        for (int i = 0; i + 1 < chavesEValores.length; i += 2) {
            if (corpo.length() > 0) corpo.append('&');
            corpo.append(URLEncoder.encode(chavesEValores[i], "UTF-8")).append('=')
                 .append(URLEncoder.encode(chavesEValores[i + 1], "UTF-8"));
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + caminho))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(corpo.toString(), StandardCharsets.UTF_8))
                .build();
        return enviar(req);
    }

    private Resposta enviar(HttpRequest req) throws IOException, InterruptedException {
        HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Resposta(r.statusCode(), r.headers().firstValue("Content-Type").orElse(""), r.body());
    }

    @Override
    public void close() {
        servidor.stop(0);
        executor.shutdownNow();
    }
}
