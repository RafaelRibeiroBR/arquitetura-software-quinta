package crawler;

import java.net.URLEncoder;

/**
 * Responsavel por gerar URLs de busca automaticamente para diferentes lojas.
 * Substitui espacos por hifens, trata caracteres especiais usando URLEncoder.
 */
public class UrlBuilder {

    // Lojas suportadas e suas URLs de busca
    private static final String AMAZON_URL = "https://www.amazon.com.br/s?k=";
    private static final String KABUM_URL = "https://www.kabum.com.br/busca/";
    private static final String MERCADO_LIVRE_URL = "https://lista.mercadolivre.com.br/";
    private static final String MAGALU_URL = "https://www.magazineluiza.com.br/busca/";

    /**
     * Gera a URL de busca para Amazon.
     * @param produto Nome do produto
     * @return URL formatada para busca na Amazon
     */
    public static String gerarUrlAmazon(String produto) {
        String termoBusca = formatarTermoBusca(produto);
        return AMAZON_URL + termoBusca;
    }

    /**
     * Gera a URL de busca para Kabum.
     * @param produto Nome do produto
     * @return URL formatada para busca na Kabum
     */
    public static String gerarUrlKabum(String produto) {
        String termoBusca = formatarTermoBusca(produto);
        return KABUM_URL + termoBusca;
    }

    /**
     * Gera a URL de busca para Mercado Livre.
     * @param produto Nome do produto
     * @return URL formatada para busca no Mercado Livre
     */
    public static String gerarUrlMercadoLivre(String produto) {
        String termoBusca = formatarTermoBusca(produto);
        return MERCADO_LIVRE_URL + termoBusca;
    }

    /**
     * Gera a URL de busca para Magazine Luiza.
     * @param produto Nome do produto
     * @return URL formatada para busca na Magazine Luiza
     */
    public static String gerarUrlMagalu(String produto) {
        String termoBusca = formatarTermoBusca(produto);
        return MAGALU_URL + termoBusca;
    }

    /**
     * Gera um mapa com todas as URLs de busca para o produto.
     * @param produto Nome do produto
     * @return Array de arrays com [nome da loja, URL]
     */
    public static String[][] gerarTodasUrls(String produto) {
        return new String[][] {
            { "Amazon", gerarUrlAmazon(produto) },
            { "Kabum", gerarUrlKabum(produto) },
            { "Mercado Livre", gerarUrlMercadoLivre(produto) },
            { "Magazine Luiza", gerarUrlMagalu(produto) }
        };
    }

    /**
     * Formata o termo de busca para URL.
     * Transforma "PlayStation 5" em "playstation-5" de forma compativel
     * com Amazon, Kabum, Mercado Livre e Magazine Luiza.
     *
     * Processamento:
     * 1. Remove acentos (PlayStation 5 -> PlayStation 5)
     * 2. Converte para minusculas
     * 3. Substitui espacos por hifens
     * 4. Remove caracteres especiais
     * 5. Codifica para URL seguro (trata acentos residuais, caracteres especiais)
     *
     * @param produto Nome do produto
     * @return Termo formatado para URL (ex: "playstation-5")
     */
    private static String formatarTermoBusca(String produto) {
        if (produto == null || produto.isEmpty()) {
            return "";
        }

        // Etapa 1: Normalizar espacos e converter para minusculas
        String resultado = produto.trim().toLowerCase();

        // Etapa 2: Substituir espacos por hifens
        resultado = resultado.replace(" ", "-");

        // Etapa 3: Remover caracteres especiais mas manter hifens e letras/numeros
        // Mantem: a-z, 0-9, hifens
        resultado = resultado.replaceAll("[^a-z0-9\\-]", "");

        // Etapa 4: Limpar hifens consecutivos
        resultado = resultado.replaceAll("-{2,}", "-");

        // Etapa 5: Remover hifens no inicio e fim
        resultado = resultado.replaceAll("^-+|-+$", "");

        // Etapa 6: Codificar para URL seguro (trata acentos e caracteres especiais restantes)
        // Isso garante compatibilidade com qualquer loja
        try {
            return URLEncoder.encode(resultado, "UTF-8");
        } catch (Exception e) {
            // Fallback: retorna o termo ja formatado se a codificacao falhar
            return resultado;
        }
    }

    /**
     * Formata o termo de busca para URL (versao simples sem URLEncoder).
     * Usada quando a loja ja lida com URL encoding internamente.
     * @param produto Nome do produto
     * @return Termo formatado
     */
    public static String formatarTermoSimples(String produto) {
        if (produto == null || produto.isEmpty()) {
            return "";
        }

        return produto.trim().toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

    /**
     * Exibe todas as URLs que seriam geradas para um produto (util para debug).
     * @param produto Nome do produto
     */
    public static void exibirUrlsGeradas(String produto) {
        System.out.println("\n=== URLs geradas para: \"" + produto + "\" ===");
        System.out.println("----------------------------------");

        String[][] urls = gerarTodasUrls(produto);
        for (String[] urlInfo : urls) {
            System.out.println("  " + urlInfo[0] + ":");
            System.out.println("  " + urlInfo[1]);
            System.out.println();
        }
    }
}