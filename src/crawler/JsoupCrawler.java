package crawler;

import model.ResultadoPreco;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classe responsável por realizar o crawling de páginas web usando Jsoup.
 * Acessa automaticamente as páginas de busca das lojas e extrai preços.
 * <p>
 * Melhorias:
 * - User-Agent realista para evitar bloqueios
 * - Headers adicionais para simular navegador real
 * - Tratamento de erro 403 (Forbidden)
 * - Retry logic para conexões instáveis
 * - Continua funcionando mesmo se uma loja falhar
 */
public class JsoupCrawler {

    // Timeout para conexão em milissegundos (15 segundos)
    private static final int TIMEOUT_MS = 15000;

    // Número máximo de tentativas
    private static final int MAX_TENTATIVAS = 3;

    // User-Agents realistas para rotation
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Edg/124.0.0.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
    };

    // Headers adicionais para simular navegador real
    private static final Map<String, String> DEFAULT_HEADERS;

    static {
        DEFAULT_HEADERS = new HashMap<>();
        DEFAULT_HEADERS.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        DEFAULT_HEADERS.put("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
        DEFAULT_HEADERS.put("Accept-Encoding", "gzip, deflate, br");
        DEFAULT_HEADERS.put("Connection", "keep-alive");
        DEFAULT_HEADERS.put("Upgrade-Insecure-Requests", "1");
    }

    private int userAgentIndex = 0;

    /**
     * Obtém o próximo User-Agent da lista (rotation).
     */
    private String getNextUserAgent() {
        userAgentIndex = (userAgentIndex + 1) % USER_AGENTS.length;
        return USER_AGENTS[userAgentIndex];
    }

    /**
     * Retorna o header "sec-ch-ua" baseado no browser atual.
     */
    private String getSecChUa() {
        return "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"";
    }

    /**
     * Acessa uma URL com retry logic e headers realistas.
     *
     * @param url URL a ser acessada
     * @return Document HTML ou null em caso de erro
     */
    public Document acessarPagina(String url) {
        Exception últimaExceção = null;

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                System.out.println("  Tentativa " + tentativa + "/" + MAX_TENTATIVAS + "...");

                Connection connection = Jsoup.connect(url)
                        .userAgent(getNextUserAgent())
                        .timeout(TIMEOUT_MS)
                        .followRedirects(true)
                        .ignoreHttpErrors(true)
                        .ignoreContentType(true);

                // Adiciona headers extras
                for (Map.Entry<String, String> entry : DEFAULT_HEADERS.entrySet()) {
                    connection.header(entry.getKey(), entry.getValue());
                }

                // Adiciona headers específicos por loja
                if (url.contains("amazon")) {
                    connection.header("sec-ch-ua", getSecChUa());
                    connection.header("sec-ch-ua-mobile", "?0");
                    connection.header("sec-ch-ua-platform", "\"Windows\"");
                }

                Document doc = connection.get();

                // Verifica resposta 403 Forbidden
                if (doc.connection().response().statusCode() == 403) {
                    System.out.println("  [Bloqueio 403 detectado - tentando novamente]");
                    últimaExceção = new IOException("HTTP 403 Forbidden");
                    esperar(tentativa);
                    continue;
                }

                System.out.println("  Página acessada (status: " + doc.connection().response().statusCode() + ")");
                return doc;

            } catch (IOException e) {
                últimaExceção = e;
                System.out.println("  Erro: " + e.getMessage());

                if (e.getMessage() != null && e.getMessage().contains("403")) {
                    System.out.println("  [Bloqueio 403 - tentando com diferentes headers]");
                }

                esperar(tentativa);
            } catch (Exception e) {
                últimaExceção = e;
                System.out.println("  Erro inesperado: " + e.getMessage());
                esperar(tentativa);
            }
        }

        System.out.println("  Falhou após " + MAX_TENTATIVAS + " tentativas");
        return null;
    }

    /**
     * Espera um tempo exponencial entre tentativas.
     */
    private void esperar(int tentativa) {
        try {
            long millis = (long) Math.pow(2, tentativa) * 500;
            System.out.println("  Aguardando " + millis + "ms antes de tentar novamente...");
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Realiza a busca em uma loja específica e retorna APENAS o primeiro produto válido.
     *
     * @param nomeLoja Nome da loja
     * @param url      URL de busca da loja
     * @return Lista com um único resultado (primeiro válido) ou vazia
     */
    public List<ResultadoPreco> buscarEmLoja(String nomeLoja, String url) {
        List<ResultadoPreco> resultados = new ArrayList<>();

        System.out.println("\n--- " + nomeLoja + " ---");
        System.out.println("  URL: " + url);

        // Acessa a página de busca
        Document doc = acessarPagina(url);
        if (doc == null) {
            System.out.println("  [" + nomeLoja + "] ✗ Falha ao acessar a página.");
            return resultados; // Retorna lista vazia - não lança exceção
        }

        // Extrai usando a estratégia específica da loja
        List<LojaStrategy.Result> resultadosRaw;

        try {
            switch (nomeLoja.toLowerCase()) {
                case "amazon":
                    resultadosRaw = LojaStrategy.extrairAmazon(doc);
                    break;
                case "kabum":
                    resultadosRaw = LojaStrategy.extrairKabum(doc);
                    break;
                case "mercado livre":
                case "mercadolivre":
                    resultadosRaw = LojaStrategy.extrairMercadoLivre(doc);
                    break;
                case "magazine luiza":
                case "magalu":
                    resultadosRaw = LojaStrategy.extrairMagalu(doc);
                    break;
                default:
                    resultadosRaw = extrairGenerico(doc);
                    break;
            }
        } catch (Exception e) {
            System.out.println("  [" + nomeLoja + "] Erro na extração: " + e.getMessage());
            return resultados; // Continua para próxima loja
        }

        // Converte para ResultadoPreco
        for (LojaStrategy.Result r : resultadosRaw) {
            if (r.preco != null && r.preco >= 500.0) {
                resultados.add(new ResultadoPreco(nomeLoja, r.preco, r.link));
            }
        }

        if (!resultados.isEmpty()) {
            // Retorna apenas o primeiro resultado válido (menor preço)
            ResultadoPreco melhor = resultados.stream()
                    .filter(r -> r.getPreco() != null)
                    .min((a, b) -> Double.compare(a.getPreco(), b.getPreco()))
                    .orElse(resultados.get(0));

            System.out.println("  [" + nomeLoja + "] ✓ Preço encontrado: R$ " + String.format("%.2f", melhor.getPreco()));

            // Retorna lista com apenas o melhor resultado
            List<ResultadoPreco> melhorUnico = new ArrayList<>();
            melhorUnico.add(melhor);
            return melhorUnico;
        } else {
            System.out.println("  [" + nomeLoja + "] ✗ Nenhum preço válido encontrado (préços < R$ 500 são ignorados).");
        }

        return resultados;
    }

    /**
     * Extração genérica quando a loja não é reconhecida.
     */
    private List<LojaStrategy.Result> extrairGenerico(Document doc) {
        List<LojaStrategy.Result> resultados = new ArrayList<>();

        if (doc == null) return resultados;

        String html = doc.body().html();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "R\\$\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
        java.util.regex.Matcher matcher = pattern.matcher(html);
        Set<Double> precosVistos = new HashSet<>();

        while (matcher.find() && resultados.size() < 3) {
            String valorStr = matcher.group(1);
            Double preco = LojaStrategy.parsePrecoBR(valorStr);
            if (preco != null && preco >= 500.0 && !precosVistos.contains(preco)) {
                precosVistos.add(preco);
                resultados.add(new LojaStrategy.Result(preco, null));
            }
        }

        return resultados;
    }

    /**
     * Busca automática em todas as lojas para um produto.
     * O sistema continua funcionando mesmo se uma loja falhar.
     *
     * @param produto Nome do produto a ser buscado
     * @return Lista de todos os resultados (um por loja)
     */
    public List<ResultadoPreco> buscarEmTodasLojas(String produto) {
        List<ResultadoPreco> todosResultados = new ArrayList<>();

        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("     BUSCA AUTOMÁTICA DE PREÇOS");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Produto: " + produto);
        System.out.println();

        String[][] urls = UrlBuilder.gerarTodasUrls(produto);

        for (String[] urlInfo : urls) {
            String nomeLoja = urlInfo[0];
            String url = urlInfo[1];

            try {
                List<ResultadoPreco> resultados = buscarEmLoja(nomeLoja, url);
                todosResultados.addAll(resultados);
            } catch (Exception e) {
                // Continua para próxima loja - não interrompe o processo
                System.out.println("  [" + nomeLoja + "] Erro: " + e.getMessage());
                System.out.println("  Continuando para próxima loja...\n");
            }
        }

        // Resumo final
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("     RESUMO DA BUSCA");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Lojas consultadas: " + urls.length);
        System.out.println("Preços encontrados: " + todosResultados.size());
        System.out.println();

        if (!todosResultados.isEmpty()) {
            System.out.println("RESULTADOS:");
            System.out.println("───────────────────────────────────────────");
            todosResultados.stream()
                    .sorted((a, b) -> Double.compare(a.getPreco(), b.getPreco()))
                    .forEach(r -> {
                        String menor = todosResultados.indexOf(r) == 0 ? " ◄ MELHOR" : "";
                        System.out.printf("  %-18s R$ %,.2f%s%n", r.getLoja() + ":", r.getPreco(), menor);
                    });
        }

        return todosResultados;
    }
}