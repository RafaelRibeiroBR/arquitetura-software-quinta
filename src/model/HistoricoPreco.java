package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Representa o histórico de preço de um produto em uma determinada loja.
 * Armazena informações sobre o preço encontrado durante o crawling,
 * incluindo produto, loja, preço, data/hora e URL opcional.
 */
public class HistoricoPreco implements Serializable {

    // Nome do produto
    private String nomeProduto;

    // Preço encontrado
    private Double preco;

    // Nome da loja onde o preço foi encontrado
    private String loja;

    // Data e hora em que o preço foi coletado
    private LocalDateTime data;

    // URL encontrada (opcional)
    private String url;

    // Construtor padrão (necessário para Gson)
    public HistoricoPreco() {
    }

    /**
     * Construtor completo para criar um registro de histórico de preço.
     * @param nomeProduto Nome do produto
     * @param preco Preço encontrado
     * @param loja Nome da loja
     * @param data Data da coleta do preço
     * @param url URL onde o preço foi encontrado (pode ser null)
     */
    public HistoricoPreco(String nomeProduto, Double preco, String loja, LocalDateTime data, String url) {
        this.nomeProduto = nomeProduto;
        this.preco = preco;
        this.loja = loja;
        this.data = data;
        this.url = url;
    }

    /**
     * Construtor completo sem URL.
     * @param nomeProduto Nome do produto
     * @param preco Preço encontrado
     * @param loja Nome da loja
     * @param data Data da coleta do preço
     */
    public HistoricoPreco(String nomeProduto, Double preco, String loja, LocalDateTime data) {
        this(nomeProduto, preco, loja, data, null);
    }

    /**
     * Construtor simplificado que usa a data e hora atual.
     * @param nomeProduto Nome do produto
     * @param preco Preço encontrado
     * @param loja Nome da loja
     */
    public HistoricoPreco(String nomeProduto, Double preco, String loja) {
        this(nomeProduto, preco, loja, LocalDateTime.now(), null);
    }

    /**
     * Construtor com URL mas usando data atual.
     * @param nomeProduto Nome do produto
     * @param preco Preço encontrado
     * @param loja Nome da loja
     * @param url URL do produto
     */
    public HistoricoPreco(String nomeProduto, Double preco, String loja, String url) {
        this(nomeProduto, preco, loja, LocalDateTime.now(), url);
    }

    // ==================== GETTERS E SETTERS ====================

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getLoja() {
        return loja;
    }

    public void setLoja(String loja) {
        this.loja = loja;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Retorna a data formatada em padrão brasileiro.
     * @return Data formatada (dd/MM/yyyy HH:mm:ss)
     */
    public String getDataFormatada() {
        if (data == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return data.format(formatter);
    }

    /**
     * Retorna a data formatada em formato ISO (para Comparações).
     * @return Data formatada (yyyy-MM-ddTHH:mm:ss)
     */
    public String getDataIso() {
        if (data == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return data.format(formatter);
    }

    /**
     * Retorna a data formatada resumida.
     * @return Data formatada (dd/MM/yyyy HH:mm)
     */
    public String getDataResumida() {
        if (data == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return data.format(formatter);
    }

    /**
     * Calcula a diferença de dias desde este registro até agora.
     * @return Número de dias desde o registro
     */
    public long getDiasDesdeRegistro() {
        if (data == null) {
            return -1;
        }
        return ChronoUnit.DAYS.between(data, LocalDateTime.now());
    }

    /**
     * Verifica se este registro é igual a outro (mesmo produto, loja, preço).
     * Usado para evitar duplicatas consecutivas.
     * NÃO considera duplicatas de lojas diferentes ou preços diferentes.
     * @param outro Outro registro de histórico
     * @return true se forem considerados iguais para fins de duplicação
     */
    public boolean éDuplicadoConsecutivo(HistoricoPreco outro) {
        if (outro == null) {
            System.out.println("  [DEBUG] Registro comparado é null - não é duplicado");
            return false;
        }

        boolean mesmoProduto = Objects.equals(this.nomeProduto, outro.nomeProduto);
        boolean mesmaLoja = Objects.equals(this.loja, outro.loja);
        boolean mesmoPreco = Objects.equals(this.preco, outro.preco);

        System.out.println("  [DEBUG] Comparando duplicata:");
        System.out.println("    Este:  " + this.nomeProduto + " | " + this.loja + " | R$ " + this.preco);
        System.out.println("    Outro: " + outro.nomeProduto + " | " + outro.loja + " | R$ " + outro.preco);
        System.out.println("    Resultado: mesmoProduto=" + mesmoProduto + ", mesmaLoja=" + mesmaLoja + ", mesmoPreco=" + mesmoPreco);

        boolean éDuplicado = mesmoProduto && mesmaLoja && mesmoPreco;
        System.out.println("  [DEBUG] É duplicato consecutivo: " + éDuplicado);

        return éDuplicado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoPreco that = (HistoricoPreco) o;
        return Objects.equals(nomeProduto, that.nomeProduto)
                && Objects.equals(preco, that.preco)
                && Objects.equals(loja, that.loja)
                && Objects.equals(data, that.data)
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomeProduto, preco, loja, data, url);
    }

    @Override
    public String toString() {
        return "Histórico de Preço:\n" +
                "  Produto: " + nomeProduto + "\n" +
                "  Preço: R$ " + String.format("%.2f", preco) + "\n" +
                "  Loja: " + loja + "\n" +
                "  Data: " + getDataFormatada() +
                (url != null ? "\n  URL: " + url : "");
    }

    /**
     * Retorna uma linha formatada para exibição em tabela.
     * @return String formatada
     */
    public String toLineFormat() {
        String precoStr = String.format("R$ %8.2f", preco);
        String dataStr = getDataResumida();
        return String.format("%-20s | %-15s | %s | %s",
                nomeProduto.length() > 20 ? nomeProduto.substring(0, 17) + "..." : nomeProduto,
                loja.length() > 15 ? loja.substring(0, 12) + "..." : loja,
                precoStr,
                dataStr);
    }
}