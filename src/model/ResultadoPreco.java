package model;

import java.io.Serializable;

/**
 * Representa o resultado de uma busca de preço em uma loja específica.
 * Armazena a loja, o preço encontrado e o link do produto.
 */
public class ResultadoPreco implements Serializable {

    // Nome da loja onde o preço foi encontrado
    private String loja;

    // Preço encontrado
    private Double preco;

    // Link do produto na loja (se disponível)
    private String linkProduto;

    // Construtor padrão (necessário para Gson)
    public ResultadoPreco() {
    }

    /**
     * Construtor completo para criar um resultado de preço.
     * @param loja Nome da loja
     * @param preco Preço encontrado
     * @param linkProduto Link do produto (pode ser null)
     */
    public ResultadoPreco(String loja, Double preco, String linkProduto) {
        this.loja = loja;
        this.preco = preco;
        this.linkProduto = linkProduto;
    }

    /**
     * Construtor simplificado sem link.
     * @param loja Nome da loja
     * @param preco Preço encontrado
     */
    public ResultadoPreco(String loja, Double preco) {
        this.loja = loja;
        this.preco = preco;
        this.linkProduto = null;
    }

    // ==================== GETTERS E SETTERS ====================

    public String getLoja() {
        return loja;
    }

    public void setLoja(String loja) {
        this.loja = loja;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getLinkProduto() {
        return linkProduto;
    }

    public void setLinkProduto(String linkProduto) {
        this.linkProduto = linkProduto;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Verifica se o resultado é válido (tem preço maior que zero).
     * @return true se o resultado é válido
     */
    public boolean éValido() {
        return preco != null && preco > 0;
    }

    @Override
    public String toString() {
        return "ResultadoPreco{" +
                "loja='" + loja + '\'' +
                ", preco=" + String.format("%.2f", preco) +
                ", link='" + (linkProduto != null ? linkProduto : "N/A") + '\'' +
                '}';
    }

    /**
     * Retorna uma representação em string formatada para exibição.
     * @return String formatada (ex: "Amazon: R$ 3.699,00")
     */
    public String toStringFormatado() {
        StringBuilder sb = new StringBuilder();
        sb.append(loja).append(": R$ ").append(String.format("%.2f", preco));
        if (linkProduto != null) {
            sb.append(" (").append(linkProduto).append(")");
        }
        return sb.toString();
    }
}