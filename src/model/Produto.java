package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um produto com múltiplos links de lojas.
 * Cada produto possui um nome e uma lista de links para diferentes lojas.
 */
public class Produto implements Serializable {

    // Identificador único do produto
    private String id;

    // Nome do produto
    private String nome;

    // Lista de links do produto em diferentes lojas
    private List<LinkProduto> links;

    // Construtor padrão (necessário para Gson)
    public Produto() {
        this.links = new ArrayList<>();
    }

    /**
     * Construtor para criar um novo produto.
     * @param id Identificador único
     * @param nome Nome do produto
     */
    public Produto(String id, String nome) {
        this.id = id;
        this.nome = nome;
        this.links = new ArrayList<>();
    }

    /**
     * Construtor simplificado que gera ID automaticamente.
     * @param nome Nome do produto
     */
    public Produto(String nome) {
        this.id = java.util.UUID.randomUUID().toString();
        this.nome = nome;
        this.links = new ArrayList<>();
    }

    // ==================== GETTERS E SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<LinkProduto> getLinks() {
        return links;
    }

    public void setLinks(List<LinkProduto> links) {
        this.links = links;
    }

    // ==================== MÉTODOS DE GERENCIAMENTO DE LINKS ====================

    /**
     * Adiciona um novo link à lista de links do produto.
     * @param link Link a ser adicionado
     */
    public void adicionarLink(LinkProduto link) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(link);
    }

    /**
     * Adiciona um novo link à lista de links do produto.
     * @param loja Nome da loja
     * @param url URL do produto
     */
    public void adicionarLink(String loja, String url) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(new LinkProduto(loja, url));
    }

    /**
     * Remove um link da lista de links do produto.
     * @param link Link a ser removido
     * @return true se o link foi removido, false se não foi encontrado
     */
    public boolean removerLink(LinkProduto link) {
        if (this.links == null) {
            return false;
        }
        return this.links.remove(link);
    }

    /**
     * Remove um link da lista pelo nome da loja.
     * @param loja Nome da loja cujo link será removido
     * @return true se algum link foi removido
     */
    public boolean removerLinkPorLoja(String loja) {
        if (this.links == null) {
            return false;
        }
        return this.links.removeIf(link -> link.getLoja().equalsIgnoreCase(loja));
    }

    /**
     * Retorna a quantidade de links cadastrados.
     * @return Quantidade de links
     */
    public int getQuantidadeLinks() {
        return links != null ? links.size() : 0;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Verifica se o produto tem links cadastrados.
     * @return true se tiver pelo menos um link
     */
    public boolean temLinks() {
        return links != null && !links.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Produto: ").append(nome).append("\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Links: ").append(getQuantidadeLinks()).append(" cadastrado(s)\n");

        if (temLinks()) {
            sb.append("Detalhes dos Links:\n");
            for (LinkProduto link : links) {
                sb.append("  - ").append(link.getLoja()).append(": ").append(link.getUrl()).append("\n");
            }
        }

        return sb.toString();
    }
}