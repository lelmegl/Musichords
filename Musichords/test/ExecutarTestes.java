import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Executa todas as classes de teste com o JUnit, mostra cada teste no terminal e,
 * quando roda no GitHub Actions, escreve um resumo em tabela na página da execução
 * (variável GITHUB_STEP_SUMMARY). Sai com código 1 se algum teste falhar, o que marca o
 * workflow como reprovado.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 20/10/2026 | Pedro | Criação da classe.
 */
public class ExecutarTestes {

    private static final Class<?>[] CLASSES = {DominioTest.class, GrafoTest.class, ApiEndpointsTest.class};

    public static void main(String[] args) throws Exception {
        Relatorio relatorio = new Relatorio();
        JUnitCore junit = new JUnitCore();
        junit.addListener(relatorio);
        Result resultado = junit.run(CLASSES);

        System.out.println();
        System.out.println("=====================================================");
        System.out.printf("  %d testes  |  %d passaram  |  %d falharam  |  %.1f s%n",
                resultado.getRunCount(), resultado.getRunCount() - resultado.getFailureCount(),
                resultado.getFailureCount(), resultado.getRunTime() / 1000.0);
        System.out.println("=====================================================");
        for (Failure f : resultado.getFailures()) {
            System.out.println();
            System.out.println("FALHA: " + f.getTestHeader());
            System.out.println(f.getTrace());
        }

        String resumo = System.getenv("GITHUB_STEP_SUMMARY");
        if (resumo != null && !resumo.isEmpty()) relatorio.escreverResumo(resumo, resultado);

        System.exit(resultado.wasSuccessful() ? 0 : 1);
    }

    /** Guarda o resultado de cada teste para o terminal e para o resumo do GitHub Actions. */
    private static final class Relatorio extends RunListener {
        private final List<String[]> linhas = new ArrayList<>(); // classe, teste, situação, ms
        private String classeAtual = "";
        private long inicio;
        private boolean falhou;

        @Override
        public void testStarted(Description d) {
            if (!d.getClassName().equals(classeAtual)) {
                classeAtual = d.getClassName();
                System.out.println();
                System.out.println(classeAtual);
            }
            inicio = System.nanoTime();
            falhou = false;
        }

        @Override
        public void testFailure(Failure f) {
            falhou = true;
        }

        @Override
        public void testFinished(Description d) {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            System.out.printf("  %s  %s (%d ms)%n", falhou ? "[FALHOU]" : "[ OK ]  ", d.getMethodName(), ms);
            linhas.add(new String[]{d.getClassName(), d.getMethodName(), falhou ? "❌ falhou" : "✅ passou", String.valueOf(ms)});
        }

        void escreverResumo(String arquivo, Result r) throws IOException {
            try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(arquivo, true), StandardCharsets.UTF_8))) {
                w.printf("## Testes do backend %s%n%n", r.wasSuccessful() ? "✅" : "❌");
                w.printf("**%d testes** · %d passaram · %d falharam · %.1f s%n%n",
                        r.getRunCount(), r.getRunCount() - r.getFailureCount(), r.getFailureCount(), r.getRunTime() / 1000.0);
                w.println("| Classe | Teste | Resultado | Tempo |");
                w.println("|---|---|---|---|");
                for (String[] l : linhas) w.printf("| %s | `%s` | %s | %s ms |%n", l[0], l[1], l[2], l[3]);
                w.println();
            }
        }
    }
}
