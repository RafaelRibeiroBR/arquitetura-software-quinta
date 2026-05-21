package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estratégia de extração de preços específica para cada loja.
 * Cada loja tem sua própria estrutura HTML e forma de exibir preços.
 *
 * Melhorias:
 * - Captura apenas o PRIMEIRO produto válido da busca
 * - Filtra preços de parcelamento (ignora valores muito baixos)
 * - Filtra preços abaixo de R$ 500,00
 * - Ignora preços duplicados
 * - Extrai preço inteiro + decimal corretamente
 */
public class LojaStrategy {

    // Preço mínimo aceito para um produto
    private static final double PRECO_MINIMO = 500.0;

    // Padrão regex para detectar textos de parcelamento (10x, 12x, etc.)
    private static final Pattern PATTERN_PARCELAMENTO = Pattern.compile(
            "\\d+x\\s*(?:sem\\s+)?juros?|parcelado|parcela",
            Pattern.CASE_INSENSITIVE);

    // Padrão regex para detectar texto de frete
    private static final Pattern PATTERN_FRETE = Pattern.compile(
            "frete|entrega|correios|sedex",
            Pattern.CASE_INSENSITIVE);

    // Padrão regex para preços brasileiros (R$ 1.234,56 ou apenas 1.234,56)
    private static final Pattern PATTERN_PRECO = Pattern.compile(
            "(?:R\\$\\s*)?(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            Pattern.CASE_INSENSITIVE);

    /**
     * Resultado da extracao com preco e link.
     */
    public static class Result {
        public final Double preco;
        public final String link;
        public final String titulo;

        public Result(Double preco, String link, String titulo) {
            this.preco = preco;
            this.link = link;
            this.titulo = titulo;
        }

        public Result(Double preco, String link) {
            this(preco, link, null);
        }
    }

    /**
     * Verifica se o texto indica parcelamento ou frete (deve ser ignorado).
     */
    private static boolean éTextoIgnorado(String texto) {
        if (texto == null || texto.isEmpty()) {
            return true;
        }

        // Ignora se contém indicadores de parcelamento
        if (PATTERN_PARCELAMENTO.matcher(texto).find()) {
            return true;
        }

        // Ignora se contém indicadores de frete
        if (PATTERN_FRETE.matcher(texto).find()) {
            return true;
        }

        return false;
    }

    /**
     * Parse de preço brasileiro com validação de valor mínimo.
     * Extrai corretamente integer + decimal (ex: "3.699,00" → 3699.00).
     */
    public static Double parsePrecoBR(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }

        try {
            // Primeiro, verifica se o texto contém valores muito baixos que indicam parcelamento
            // Exemplo: "10x de R$ 123,45" - o valor 123,45 é parcela, não preço cheio
            Matcher parcelaMatcher = Pattern.compile(
                    "\\d+x\\s*(?:de\\s*)?(?:R\\$\\s*)?(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
                    Pattern.CASE_INSENSITIVE).matcher(texto);

            if (parcelaMatcher.find()) {
                // Texto contém parcelamento - tenta encontrar o valor à vista
                String textoSemParcela = texto.substring(0, parcelaMatcher.start());
                Double precoAVista = extrairPrecoAVista(textoSemParcela);
                if (precoAVista != null && precoAVista >= PRECO_MINIMO) {
                    return precoAVista;
                }
            }

            return extrairPrecoAVista(texto);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrai o preço à vista do texto.
     */
    private static Double extrairPrecoAVista(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }

        Matcher matcher = PATTERN_PRECO.matcher(texto);

        while (matcher.find()) {
            String valorStr = matcher.group(1);

            // Converte do formato brasileiro para padrão Java
            // 1.234,56 → 1234.56
            String limpo = valorStr.replace(".", "").replace(",", ".");

            try {
                Double preco = Double.parseDouble(limpo);

                // Valida preço mínimo
                if (preco < PRECO_MINIMO) {
                    continue;
                }

                return preco;
            } catch (NumberFormatException e) {
                // Continua procurando
            }
        }

        return null;
    }

    /**
     * Filtra resultados duplicados, mantendo apenas o primeiro de cada valor.
     */
    private static List<Result> filtrarDuplicatas(List<Result> resultados) {
        Set<Double> precosVistos = new HashSet<>();
        List<Result> filtrados = new ArrayList<>();

        for (Result r : resultados) {
            if (r.preco != null && !precosVistos.contains(r.preco)) {
                precosVistos.add(r.preco);
                filtrados.add(r);
            }
        }

        return filtrados;
    }

    /**
     * Obtém o primeiro resultado válido (melhor preço principal).
     */
    public static Result getPrimeiroValido(List<Result> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            return null;
        }

        for (Result r : resultados) {
            if (r.preco != null && r.preco >= PRECO_MINIMO) {
                return r;
            }
        }

        return null;
    }

    /**
     * Estratégia para Amazon Brasil.
     * Amazon é conhecido por ter bastante conteúdo dinámico.
     */
    public static List<Result> extrairAmazon(Document doc) {
        List<Result> resultados = new ArrayList<>();

        if (doc == null) return resultados;

        try {
            // Seletores específicos da Amazon para busca de produtos
            Elements containers = doc.select("[data-component-type='s-search-result']");

            if (containers.isEmpty()) {
                // Fallback: procura por itens de busca
                containers = doc.select(".s-result-item");
            }

            if (containers.isEmpty()) {
                // Último fallback: procura qualquer div com preço
                containers = doc.select("[class*='s-search']");
            }

            for (Element container : containers) {
                // Extrai o título do produto
                Element tituloEl = container.selectFirst("h2 a span, h2 a .a-size-base, .a-text-normal");
                String titulo = tituloEl != null ? tituloEl.text() : null;

                // Extrai o link do produto
                Element linkEl = container.selectFirst("h2 a");
                String link = linkEl != null ? linkEl.attr("href") : null;
                if (link != null && !link.startsWith("http")) {
                    link = "https://www.amazon.com.br" + link;
                }

                // Extrai preço inteiro
                Element precoInteiroEl = container.selectFirst(".a-price-whole");
                String precoInteiro = precoInteiroEl != null ? precoInteiroEl.text().replace(",", "") : null;

                // Extrai preço decimal
                Element precoDecimalEl = container.selectFirst(".a-price-fraction");
                String precoDecimal = precoDecimalEl != null ? precoDecimalEl.text() : null;

                // Monta o preço completo
                if (precoInteiro != null) {
                    Double preco = null;
                    if (precoDecimal != null) {
                        preco = parsePrecoBR(precoInteiro + "," + precoDecimal);
                    } else {
                        preco = parsePrecoBR(precoInteiro);
                    }

                    if (preco != null && preco >= PRECO_MINIMO) {
                        resultados.add(new Result(preco, link, titulo));
                        break; // Apenas o primeiro produto válido
                    }
                }
            }

            // Se não encontrou via seletor, tenta via regex no HTML completo
            if (resultados.isEmpty()) {
                String html = doc.html();
                Matcher matcher = PATTERN_PRECO.matcher(html);
                Set<Double> precosVistos = new HashSet<>();

                while (matcher.find()) {
                    String valorStr = matcher.group(1);
                    Double preco = parsePrecoBR(valorStr);

                    if (preco != null && preco >= PRECO_MINIMO && !precosVistos.contains(preco)) {
                        precosVistos.add(preco);
                        resultados.add(new Result(preco, null));

                        // Limita a 3 resultados via regex
                        if (resultados.size() >= 3) break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("  [Amazon] Erro na extração: " + e.getMessage());
        }

        return filtrarDuplicatas(resultados);
    }

    /**
     * Estratégia para Kabum.
     * Extrai apenas o primeiro produto da listagem.
     */
    public static List<Result> extrairKabum(Document doc) {
        List<Result> resultados = new ArrayList<>();

        if (doc == null) return resultados;

        try {
            // Seletores específicos da Kabum
            Elements containers = doc.select(".productDataBlock, .productCard, [class*='product-card']");

            for (Element container : containers) {
                // Extrai o título
                Element tituloEl = container.selectFirst(".title, .name, h3 a");
                String titulo = tituloEl != null ? tituloEl.text() : null;

                // Extrai o link
                Element linkEl = container.selectFirst("a[href*='/produto/'], a[href*='/info/']");
                String link = linkEl != null ? linkEl.attr("href") : null;
                if (link != null && !link.startsWith("http")) {
                    link = "https://www.kabum.com.br" + link;
                }

                // Procura preço - formato da Kabum: "R$ 3.699,00"
                Element precoEl = container.selectFirst(".price, .actualPrice, [class*='price']");
                String precoTexto = precoEl != null ? precoEl.text() : null;

                if (precoTexto != null && !éTextoIgnorado(precoTexto)) {
                    Double preco = parsePrecoBR(precoTexto);
                    if (preco != null && preco >= PRECO_MINIMO) {
                        resultados.add(new Result(preco, link, titulo));
                        break; // Apenas o primeiro produto válido
                    }
                }
            }

            // Fallback via regex
            if (resultados.isEmpty()) {
                String html = doc.html();
                Matcher matcher = PATTERN_PRECO.matcher(html);
                Set<Double> precosVistos = new HashSet<>();

                while (matcher.find() && resultados.size() < 3) {
                    String valorStr = matcher.group(1);
                    Double preco = parsePrecoBR(valorStr);

                    if (preco != null && preco >= PRECO_MINIMO && !precosVistos.contains(preco)) {
                        precosVistos.add(preco);
                        resultados.add(new Result(preco, null));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("  [Kabum] Erro na extração: " + e.getMessage());
        }

        return filtrarDuplicatas(resultados);
    }

    /**
     * Estratégia para Mercado Livre.
     * Usa a estrutura padrão do ML com classes andes-money-amount.
     */
    public static List<Result> extrairMercadoLivre(Document doc) {
        List<Result> resultados = new ArrayList<>();

        if (doc == null) return resultados;

        try {
            // Seletores específicos do Mercado Livre
            Elements containers = doc.select(".ui-search-result, .andes-card, [class*='search-result']");

            for (Element container : containers) {
                // Extrai o título
                Element tituloEl = container.selectFirst(".ui-search-item__title, .item__title, h2 a");
                String titulo = tituloEl != null ? tituloEl.text() : null;

                // Extrai o link
                Element linkEl = container.selectFirst("a[href*='.mercadolivre.com.br']");
                String link = linkEl != null ? linkEl.attr("href") : null;

                // Procura preço no elemento principal de preço do ML
                Element precoContainer = container.selectFirst(".ui-price-symbol, .price-symbol");
                Element precoValue = container.selectFirst(".ui-price-bigger, .price-xml-barato, [class*='price']");

                String precoTexto = null;
                if (precoValue != null) {
                    // Monta o texto completo do preço
                    String symbol = precoContainer != null ? precoContainer.text() : "R$ ";
                    precoTexto = symbol + " " + precoValue.text();
                }

                if (precoTexto != null && !éTextoIgnorado(precoTexto)) {
                    Double preco = parsePrecoBR(precoTexto);
                    if (preco != null && preco >= PRECO_MINIMO) {
                        resultados.add(new Result(preco, link, titulo));
                        break; // Apenas o primeiro produto válido
                    }
                }
            }

            // Fallback para estrutura alternativa do ML
            if (resultados.isEmpty()) {
                Element precoEl = doc.selectFirst(".andes-money-amount, .ui-price");
                if (precoEl != null) {
                    String precoTexto = precoEl.text();
                    Double preco = parsePrecoBR(precoTexto);
                    if (preco != null && preco >= PRECO_MINIMO) {
                        resultados.add(new Result(preco, null));
                    }
                }
            }

            // Último fallback via regex
            if (resultados.isEmpty()) {
                String html = doc.html();
                Matcher matcher = PATTERN_PRECO.matcher(html);
                Set<Double> precosVistos = new HashSet<>();

                while (matcher.find() && resultados.size() < 3) {
                    String valorStr = matcher.group(1);
                    Double preco = parsePrecoBR(valorStr);

                    if (preco != null && preco >= PRECO_MINIMO && !precosVistos.contains(preco)) {
                        precosVistos.add(preco);
                        resultados.add(new Result(preco, null));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("  [Mercado Livre] Erro na extração: " + e.getMessage());
        }

        return filtrarDuplicatas(resultados);
    }

    /**
     * Estratégia para Magazine Luiza.
     * Nota: Magazine Luiza pode retornar erro 403 se detectar scrapers.
     * Usa Headers customizados para tentar evitar bloqueios.
     */
    public static List<Result> extrairMagalu(Document doc) {
        List<Result> resultados = new ArrayList<>();

        if (doc == null) return resultados;

        try {
            // Seletores específicos da Magazine Luiza
            Elements containers = doc.select("[data-testid='product-card'], .product-card, [class*='product']");

            for (Element container : containers) {
                // Extrai o título
                Element tituloEl = container.selectFirst("h3, h4, [data-testid='product-title']");
                String titulo = tituloEl != null ? tituloEl.text() : null;

                // Extrai o link
                Element linkEl = container.selectFirst("a[href*='magazineluiza.com.br']");
                String link = linkEl != null ? linkEl.attr("href") : null;
                if (link != null && !link.startsWith("http")) {
                    link = "https://www.magazineluiza.com.br" + link;
                }

                // Procura preço - formato Magalu
                Element precoEl = container.selectFirst("[data-testid='price-value'], .price, [class*='price']");
                String precoTexto = precoEl != null ? precoEl.text() : null;

                if (precoTexto != null && !éTextoIgnorado(precoTexto)) {
                    Double preco = parsePrecoBR(precoTexto);
                    if (preco != null && preco >= PRECO_MINIMO) {
                        resultados.add(new Result(preco, link, titulo));
                        break;
                    }
                }
            }

            // Fallback via regex
            if (resultados.isEmpty()) {
                String html = doc.html();
                Matcher matcher = PATTERN_PRECO.matcher(html);
                Set<Double> precosVistos = new HashSet<>();

                while (matcher.find() && resultados.size() < 3) {
                    String valorStr = matcher.group(1);
                    Double preco = parsePrecoBR(valorStr);

                    if (preco != null && preco >= PRECO_MINIMO && !precosVistos.contains(preco)) {
                        precosVistos.add(preco);
                        resultados.add(new Result(preco, null));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("  [Magazine Luiza] Erro na extração: " + e.getMessage());
        }

        return filtrarDuplicatas(resultados);
    }
}