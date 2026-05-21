package model;

import java.io.Serializable;

/**
 * Representa um link de um produto em uma loja específica.
 * Cada produto pode ter múltiplos links de diferentes lojas.
 */
public class LinkProduto implements Serializable {

    // Identificador único do link
    private String id;

    // Nome da loja (ex: Amazon, Kabum, Mercado Livre)
    private String loja;

    // URL do produto na loja
    private String url;

    // Construtor padrão (necessário para Gson)
    public LinkProduto() {
    }

    /**
     * Construtor completo para criar um novo link.
     * @param id Identificador único do link
     * @param loja Nome da loja
     * @param url URL do produto
     */
    public LinkProduto(String id, String loja, String url) {
        this.id = id;
        this.loja = loja;
        this.url = url;
    }

    /**
     * Construtor simplificado sem ID (gera um ID temporário).
     * @param loja Nome da loja
     * @param url URL do produto
     */
    public LinkProduto(String loja, String url) {
        this.id = java.util.UUID.randomUUID().toString();
        this.loja = loja;
        this.url = url;
    }

    // ==================== GETTERS E SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoja() {
        return loja;
    }

    public void setLoja(String loja) {
        this.loja = loja;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    @Override
    public String toString() {
        return "LinkProduto{" +
                "id='" + id + '\'' +
                ", loja='" + loja + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}