package service;

import crawler.JsoupCrawler;
import crawler.UrlBuilder;
import model.HistoricoPreco;
import model.Produto;
import model.ResultadoPreco;
import repository.HistoricoRepository;
import repository.ProdutoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço principal do crawler que coordena a busca automática de preços.
 * Não depende de links cadastrados - gera URLs automaticamente.
 */
public class CrawlerService {

    // Repositório de produtos
    private ProdutoRepository produtoRepository;

    // Repositório de histórico
    private HistoricoRepository historicoRepository;

    // Instância do crawler JSoup
    private JsoupCrawler crawler;

    // Contadores para estatísticas
    private int totalBuscas;
    private int precosEncontrados;
    private int erros;

    /**
     * Construtor que inicializa os componentes necessários.
     */
    public CrawlerService() {
        this.produtoRepository = new ProdutoRepository();
        this.historicoRepository = new HistoricoRepository();
        this.crawler = new JsoupCrawler();
        this.totalBuscas = 0;
        this.precosEncontrados = 0;
        this.erros = 0;
    }

    /**
     * Executa o crawler automático em todos os produtos cadastrados.
     * Para cada produto, gera URLs automaticamente e busca em todas as lojas.
     */
    public void executarCrawler() {
        System.out.println("\n========================================");
        System.out.println("    CRAWLER AUTOMÁTICO DE PREÇOS        ");
        System.out.println("========================================\n");

        // Reseta contadores
        totalBuscas = 0;
        precosEncontrados = 0;
        erros = 0;

        // Verifica se há produtos cadastrados
        if (produtoRepository.estaVazio()) {
            System.out.println("Nenhum produto cadastrado. Cadastre produtos antes de executar o crawler.");
            return;
        }

        // Lista todos os produtos
        for (Produto produto : produtoRepository.listarTodos()) {
            processarProdutoAutomatico(produto);
        }

        // Exibe relatório final
        exibirRelatorio();

        System.out.println("\n========================================");
        System.out.println("          CRAWLER FINALIZADO            ");
        System.out.println("========================================\n");
    }

    /**
     * Executa busca automática para um produto específico.
     * Gera URLs automaticamente e busca em todas as lojas.
     * @param nomeProduto Nome do produto para buscar
     */
    public void executarBuscaAutomatica(String nomeProduto) {
        System.out.println("\n========================================");
        System.out.println("    CRAWLER AUTOMÁTICO DE PREÇOS        ");
        System.out.println("========================================\n");

        totalBuscas = 0;
        precosEncontrados = 0;
        erros = 0;

        Produto produtoTemporario = new Produto(nomeProduto);
        processarProdutoAutomatico(produtoTemporario);

        exibirRelatorio();
    }

    /**
     * Processa um produto específico, fazendo busca automática em todas as lojas.
     * @param produto Produto a ser processado
     */
    private void processarProdutoAutomatico(Produto produto) {
        System.out.println("\n>>> Processando: " + produto.getNome());

        // Gera URLs automaticamente
        String[][] urls = UrlBuilder.gerarTodasUrls(produto.getNome());

        // Mostra as URLs que serão acessadas
        System.out.println("\n  URLs geradas automaticamente:");
        for (String[] urlInfo : urls) {
            System.out.println("    - " + urlInfo[0] + ": " + urlInfo[1]);
        }
        System.out.println();

        // Lista para collectar todos os resultados
        List<ResultadoPreco> todosResultados = new ArrayList<>();

        // Busca em cada loja
        for (String[] urlInfo : urls) {
            String nomeLoja = urlInfo[0];
            String url = urlInfo[1];

            totalBuscas++;

            try {
                // Faz a busca na loja
                List<ResultadoPreco> resultados = crawler.buscarEmLoja(nomeLoja, url);

                if (!resultados.isEmpty()) {
                    todosResultados.addAll(resultados);
                    precosEncontrados += resultados.size();
                } else {
                    erros++;
                }
            } catch (Exception e) {
                System.out.println("  [" + nomeLoja + "] Erro: " + e.getMessage());
                erros++;
            }
        }

        // Se encontrou preços, salva todos e exibe comparação
        if (!todosResultados.isEmpty()) {
            salvarTodosPrecos(produto.getNome(), todosResultados);
        } else {
            System.out.println("\n  Nenhum preço encontrado para: " + produto.getNome());
        }
    }

    /**
     * Salva TODOS os preços encontrados no histórico e exibe comparação completa.
     * Identifica o menor preço para destaque, mas não salva apenas o menor.
     * Inclui logs detalhados de cada loja salva.
     * @param nomeProduto Nome do produto
     * @param resultados Lista de todos os preços encontrados
     */
    private void salvarTodosPrecos(String nomeProduto, List<ResultadoPreco> resultados) {
        System.out.println("\n  [CRAWLER] ===============================================");
        System.out.println("  [CRAWLER] SALVANDO PREÇOS PARA: " + nomeProduto);
        System.out.println("  [CRAWLER] Resultados recebidos: " + resultados.size());

        // Filtra apenas resultados válidos
        List<ResultadoPreco> validos = resultados.stream()
                .filter(r -> r.éValido())
                .collect(Collectors.toList());

        System.out.println("  [CRAWLER] Resultados válidos: " + validos.size());

        if (validos.isEmpty()) {
            System.out.println("  [CRAWLER] Nenhum resultado válido para salvar.");
            System.out.println("  [CRAWLER] ===============================================\n");
            return;
        }

        // Mostra todas as lojas encontradas ANTES de salvar
        System.out.println("  [CRAWLER] Lojas encontradas:");
        for (ResultadoPreco r : validos) {
            System.out.println("  [CRAWLER]   - " + r.getLoja() + ": R$ " + r.getPreco());
        }

        // Encontra o menor preço para destaque
        ResultadoPreco menorPreco = validos.stream()
                .min(Comparator.comparing(ResultadoPreco::getPreco))
                .orElse(null);

        // Data/hora atual para usar em todos os registros
        LocalDateTime agora = LocalDateTime.now();
        String dataFormatada = agora.format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // Cabeçalho do produto
        System.out.println("\n  ╔════════════════════════════════════════════════════════════╗");
        System.out.println("  ║  RESULTADO DA BUSCA: " + nomeProduto.substring(0, Math.min(nomeProduto.length(), 40)));
        System.out.println("  ║  Data: " + dataFormatada);
        System.out.println("  ╠════════════════════════════════════════════════════════════╣");
        System.out.println("  ║  LOJA                    PREÇO         STATUS             ║");
        System.out.println("  ╠════════════════════════════════════════════════════════════╣");

        // Ordena por preço e exibe
        validos.stream()
                .sorted(Comparator.comparingDouble(ResultadoPreco::getPreco))
                .forEach(r -> {
                    String status = r.equals(menorPreco) ? "★ MELHOR PREÇO ★" : "                      ";
                    System.out.printf("  ║  %-23s R$ %9.2f  %-18s ║%n",
                            r.getLoja(), r.getPreco(), status);
                });

        System.out.println("  ╚════════════════════════════════════════════════════════════╝");

        // Salva CADA resultado no histórico com logs detalhados
        System.out.println("\n  >> Iniciando salvamento de TODOS os preços no histórico...");
        System.out.println("  >> Serão processados " + validos.size() + " registro(s)...\n");

        int salvos = 0;
        int ignorados = 0;
        for (ResultadoPreco resultado : validos) {
            System.out.println("  [PROCESSANDO] Loja: " + resultado.getLoja() + " | Preço: R$ " + resultado.getPreco());

            HistoricoPreco registro = new HistoricoPreco(
                    nomeProduto,
                    resultado.getPreco(),
                    resultado.getLoja(),
                    agora,
                    resultado.getLinkProduto()
            );

            boolean foiAdicionado = historicoRepository.adicionar(registro);
            if (foiAdicionado) {
                salvos++;
                System.out.println("  [OK] Registrado: " + resultado.getLoja() + " = R$ " + resultado.getPreco());
            } else {
                ignorados++;
                System.out.println("  [IGNORADO] Duplicado: " + resultado.getLoja() + " = R$ " + resultado.getPreco());
            }
        }

        System.out.println("\n  >> RESUMO DO SALVAMENTO:");
        System.out.println("  >>   Salvos: " + salvos);
        System.out.println("  >>   Ignorados (duplicados): " + ignorados);
        System.out.println("  >>   Total processado: " + validos.size());

        // Resumo com estatísticas
        double menor = validos.stream().mapToDouble(ResultadoPreco::getPreco).min().orElse(0);
        double maior = validos.stream().mapToDouble(ResultadoPreco::getPreco).max().orElse(0);
        double mediaPreco = validos.stream().mapToDouble(ResultadoPreco::getPreco).average().orElse(0);
        double economia = maior - menor;

        System.out.println("\n  ── ESTATÍSTICAS DESTA BUSCA ──");
        System.out.printf("     Menor preço:  R$ %.2f (%s)%n", menor, menorPreco.getLoja());
        System.out.printf("     Maior preço: R$ %.2f%n", maior);
        System.out.printf("     Média:       R$ %.2f%n", mediaPreco);
        if (economia > 0) {
            System.out.printf("     Economia máx: R$ %.2f%n", economia);
        }

        System.out.println("  [CRAWLER] ===============================================\n");
    }

    /**
     * Exibe o relatório final do crawler.
     */
    private void exibirRelatorio() {
        System.out.println("\n-------- RELATÓRIO DO CRAWLER --------");
        System.out.println("Total de buscas realizadas: " + totalBuscas);
        System.out.println("Preços encontrados: " + precosEncontrados);
        System.out.println("Falhas/erros: " + erros);
        System.out.println("-------------------------------------");
    }

    /**
     * Retorna o repositório de produtos.
     */
    public ProdutoRepository getProdutoRepository() {
        return produtoRepository;
    }

    /**
     * Retorna o repositório de histórico.
     */
    public HistoricoRepository getHistoricoRepository() {
        return historicoRepository;
    }

    /**
     * Retorna uma lista dos menores preços por produto do histórico.
     */
    public List<HistoricoPreco> getMenoresPrecos() {
        List<HistoricoPreco> historico = historicoRepository.listarTodos();

        // Agrupa por produto e pega o menor preço de cada
        return historico.stream()
                .collect(Collectors.groupingBy(HistoricoPreco::getNomeProduto))
                .entrySet().stream()
                .map(entry -> entry.getValue().stream()
                        .min(Comparator.comparing(HistoricoPreco::getPreco))
                        .orElse(null))
                .filter(h -> h != null)
                .collect(Collectors.toList());
    }
}