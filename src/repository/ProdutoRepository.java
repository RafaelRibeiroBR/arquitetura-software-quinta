package repository;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import model.Produto;
import util.GsonConfig;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositório para gerenciar a persistência de produtos em arquivo JSON.
 * Responsável por salvar e carregar produtos do arquivo produtos.json.
 *
 * Tratamento robusto de arquivos JSON:
 * - Cria arquivo automaticamente se não existir
 * - Recria arquivo se estiver vazio ou corrompido
 * - Nunca quebra o sistema mesmo com erros de parsing
 *
 * Compatível com Java 17, 21 e 25 usando Files.readAllBytes() e Files.write()
 */
public class ProdutoRepository {

    // Nome do arquivo onde os produtos serão salvos
    private static final String ARQUIVO_PRODUTOS = "produtos.json";

    // Instância do Gson para manipulação de JSON
    private Gson gson;

    // Lista de produtos em memória
    private List<Produto> produtos;

    /**
     * Construtor que inicializa o repositório e carrega os produtos do arquivo.
     */
    public ProdutoRepository() {
        this.gson = GsonConfig.getGson();
        this.produtos = new ArrayList<>();
        carregarProdutos();
    }

    /**
     * Lê o conteúdo de um arquivo usando codificação UTF-8.
     * Método compatível com todas as versões Java (17+).
     *
     * @param path Caminho do arquivo a ser lido
     * @return Conteúdo do arquivo como String, ou string vazia em caso de erro
     */
    private String lerArquivoUtf8(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
            return "";
        }
    }

    /**
     * Escreve conteúdo em um arquivo usando codificação UTF-8.
     * Método compatível com todas as versões Java (17+).
     *
     * @param path    Caminho do arquivo a ser escrito
     * @param conteudo Conteúdo a ser escrito
     * @return true se a operação foi bem sucedido, false caso contrário
     */
    private boolean escreverArquivoUtf8(Path path, String conteudo) {
        try {
            Files.write(path, conteudo.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao escrever arquivo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o arquivo JSON existe e é válido.
     * @return true se o arquivo existe e tem conteúdo válido
     */
    private boolean arquivoJsonValido(Path path) {
        try {
            if (!Files.exists(path)) {
                return false;
            }

            String conteudo = lerArquivoUtf8(path);

            // Verifica se está vazio
            if (conteudo == null || conteudo.trim().isEmpty()) {
                System.out.println("Arquivo de produtos está vazio.");
                return false;
            }

            String trimmed = conteudo.trim();

            // Verifica se começa com '[' (início de array JSON válido)
            if (!trimmed.startsWith("[")) {
                System.out.println("Arquivo de produtos não é um JSON array válido.");
                return false;
            }

            // Verifica se termina com ']' (fechamento do array)
            if (!trimmed.endsWith("]")) {
                System.out.println("Arquivo de produtos está com formato JSON incompleto.");
                return false;
            }

            return true;

        } catch (Exception e) {
            System.out.println("Erro ao verificar arquivo de produtos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recria o arquivo JSON com uma lista vazia válida.
     */
    private void recriarArquivoJson() {
        Path path = Paths.get(ARQUIVO_PRODUTOS);
        boolean sucesso = escreverArquivoUtf8(path, "[]");
        if (sucesso) {
            System.out.println("Arquivo de produtos recriado com conteúdo válido: []");
        }
    }

    /**
     * Cria o arquivo JSON vazio se não existir.
     */
    private void criarArquivoSeNaoExistir() {
        Path path = Paths.get(ARQUIVO_PRODUTOS);
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
                boolean sucesso = escreverArquivoUtf8(path, "[]");
                if (sucesso) {
                    System.out.println("Arquivo de produtos criado automaticamente: " + ARQUIVO_PRODUTOS);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de produtos: " + e.getMessage());
        }
    }

    // ==================== OPERAÇÕES DE PERSISTÊNCIA ====================

    /**
     * Carrega os produtos do arquivo JSON.
     * Tratamento robusto para arquivos vazios, incompletos ou corrompidos.
     */
    public void carregarProdutos() {
        Path path = Paths.get(ARQUIVO_PRODUTOS);

        // Primeiro passo: criar arquivo se não existir
        if (!Files.exists(path)) {
            criarArquivoSeNaoExistir();
            this.produtos = new ArrayList<>();
            System.out.println("Arquivo de produtos não encontrado. Lista vazia criada.");
            return;
        }

        // Segundo passo: verificar se o arquivo é válido
        if (!arquivoJsonValido(path)) {
            System.out.println("Arquivo de produtos corrompido ou inválido. Recriando...");
            recriarArquivoJson();
            this.produtos = new ArrayList<>();
            return;
        }

        // Terceiro passo: tentar carregar o conteúdo
        try (FileReader reader = new FileReader(ARQUIVO_PRODUTOS)) {
            Type tipoLista = new TypeToken<List<Produto>>() {}.getType();
            this.produtos = gson.fromJson(reader, tipoLista);

            // Se o parsing resultar em null, inicializa lista vazia
            if (this.produtos == null) {
                this.produtos = new ArrayList<>();
            }

            System.out.println("Carregados " + this.produtos.size() + " produto(s) do arquivo.");

        } catch (JsonSyntaxException e) {
            // JSON com sintaxe inválida (corrompido)
            System.out.println("Erro de sintaxe no JSON de produtos: " + e.getMessage());
            System.out.println("Recriando arquivo de produtos...");
            recriarArquivoJson();
            this.produtos = new ArrayList<>();

        } catch (IOException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
            this.produtos = new ArrayList<>();
        }
    }

    /**
     * Salva todos os produtos no arquivo JSON.
     */
    public void salvarProdutos() {
        try (FileWriter writer = new FileWriter(ARQUIVO_PRODUTOS)) {
            gson.toJson(produtos, writer);
            System.out.println("Produtos salvos com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao salvar produtos: " + e.getMessage());
        }
    }

    // ==================== OPERAÇÕES CRUD ====================

    /**
     * Adiciona um novo produto à lista e salva no arquivo.
     * @param produto Produto a ser adicionado
     */
    public void adicionar(Produto produto) {
        if (produto == null) {
            System.out.println("Não é possível adicionar um produto nulo.");
            return;
        }
        produtos.add(produto);
        salvarProdutos();
        System.out.println("Produto adicionado: " + produto.getNome());
    }

    /**
     * Remove um produto da lista pelo ID.
     * @param id ID do produto a ser removido
     * @return true se o produto foi removido, false caso contrário
     */
    public boolean remover(String id) {
        boolean removed = produtos.removeIf(p -> p.getId().equals(id));
        if (removed) {
            salvarProdutos();
            System.out.println("Produto removido com sucesso!");
        } else {
            System.out.println("Produto não encontrado com o ID: " + id);
        }
        return removed;
    }

    /**
     * Busca um produto pelo ID.
     * @param id ID do produto a ser buscado
     * @return O produto encontrado ou null se não existir
     */
    public Produto buscarPorId(String id) {
        return produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Busca um produto pelo nome (busca parcial, case-insensitive).
     * @param nome Nome ou parte do nome do produto
     * @return Lista de produtos que contêm o nome buscado
     */
    public List<Produto> buscarPorNome(String nome) {
        List<Produto> resultados = new ArrayList<>();
        String nomeLower = nome.toLowerCase();

        for (Produto p : produtos) {
            if (p.getNome().toLowerCase().contains(nomeLower)) {
                resultados.add(p);
            }
        }

        return resultados;
    }

    /**
     * Lista todos os produtos cadastrados.
     * @return Lista de todos os produtos
     */
    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos);
    }

    /**
     * Atualiza um produto existente.
     * @param produto Produto com as informações atualizadas
     * @return true se o produto foi atualizado, false se não foi encontrado
     */
    public boolean atualizar(Produto produto) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getId().equals(produto.getId())) {
                produtos.set(i, produto);
                salvarProdutos();
                System.out.println("Produto atualizado: " + produto.getNome());
                return true;
            }
        }
        System.out.println("Produto não encontrado para atualização.");
        return false;
    }

    /**
     * Limpa todos os produtos cadastrados.
     */
    public void limparTodos() {
        produtos.clear();
        salvarProdutos();
        System.out.println("Todos os produtos foram removidos.");
    }

    /**
     * Retorna a quantidade de produtos cadastrados.
     * @return Quantidade de produtos
     */
    public int getQuantidade() {
        return produtos.size();
    }

    /**
     * Verifica se existe algum produto cadastrado.
     * @return true se não houver produtos cadastrados
     */
    public boolean estaVazio() {
        return produtos.isEmpty();
    }

    // ==================== GETTER ====================

    public List<Produto> getProdutos() {
        return produtos;
    }
}