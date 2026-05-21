package util;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilitário para formatação de valores exibidos ao usuário.
 */
public class FormatUtil {

    // Formatador de moeda brasileira
    private static final NumberFormat FORMATADOR_MOEDA =
        NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // Formatador de data brasileiro
    private static final DateTimeFormatter FORMATADOR_DATA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Formata um valor double como moeda brasileira.
     * @param valor Valor a ser formatado
     * @return String formatada (ex: "R$ 1.234,56")
     */
    public static String formatarMoeda(double valor) {
        return FORMATADOR_MOEDA.format(valor);
    }

    /**
     * Formata uma data para o padrão brasileiro.
     * @param data Data a ser formatada
     * @return String formatada (ex: "21/05/2026 14:30:00")
     */
    public static String formatarData(LocalDateTime data) {
        if (data == null) {
            return "N/A";
        }
        return data.format(FORMATADOR_DATA);
    }

    /**
     * Limpa uma string de URL, removendo protocolos e retornando apenas o domínio.
     * @param url URL completa
     * @return Domínio da URL
     */
    public static String extrairDominio(String url) {
        if (url == null || url.isEmpty()) {
            return "Desconhecido";
        }

        try {
            // Remove protocolo (http:// ou https://)
            String semProtocolo = url.replaceFirst("https?://", "");

            // Remove caminho e fica apenas com o domínio
            String dominio = semProtocolo.split("/")[0];

            return dominio;
        } catch (Exception e) {
            return url;
        }
    }
}