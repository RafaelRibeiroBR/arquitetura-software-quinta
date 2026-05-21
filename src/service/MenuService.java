package service;

import model.HistoricoPreco;
import model.Produto;
import repository.HistoricoRepository;
import repository.ProdutoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Serviço responsável por exibir e gerenciar o menu do sistema.
 * Sistema SIMPLIFICADO: usuário cadastra só o nome do produto e o crawler faz o resto.
 */
public class MenuService {

    // Scanner para ler entrada do usuário
    private Scanner scanner;

    // Repositório de produtos
    private ProdutoRepository produtoRepository;

    // Repositório de histórico
    private HistoricoRepository historicoRepository;

    // Serviço de crawler
    private CrawlerService crawlerService;

    // Flag para controlar o loop do menu
    private boolean executando;

    /**
     * Construtor que inicializa os componentes do menu.
     */
    public MenuService() {
        this.scanner = new Scanner(System.in);
        this.produtoRepository = new ProdutoRepository();
        this.historicoRepository = new HistoricoRepository();
        this.crawlerService = new CrawlerService();
        this.executando = true;
    }

    /**
     * Exibe o menu principal e processa a entrada do usuário.
     */
    public void exibirMenu() {
        while (executando) {
            exibirOpcoes();
            int opcao = lerOpcao();

            // Processa a opção elegida usando switch-case
            switch (opcao) {
                case 1:
                    cadastrarProduto();
                    break;
                case 2:
                    listarProdutos();
                    break;
                case 3:
                    executarCrawler();
                    break;
                case 4:
                    verHistorico();
                    break;
                case 0:
                    sair();
                    break;
                default:
                    System.out.println("Opção inválida! Digite um número entre 0 e 4.");
            }

            if (executando) {
                System.out.println();
            }
        }
    }

    /**
     * Exibe as opções do menu principal.
     */
    private void exibirOpcoes() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║     CRAWLER DE PREÇOS DE PRODUTOS          ║");
        System.out.println("║     (Busca Automática nas Lojas)          ║");
        System.out.println("╠═══════════════════════════════════════════╣");
        System.out.println("║  1 - Cadastrar produto                    ║");
        System.out.println("║  2 - Listar produtos                      ║");
        System.out.println("║  3 - Executar crawler                     ║");
        System.out.println("║  4 - Ver histórico de preços              ║");
        System.out.println("║  0 - Sair                                 ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();
        System.out.print("Escolha uma opção: ");
    }

    /**
     * Lê a opção escolhida pelo usuário.
     * @return Opção numérica
     */
    private int lerOpcao() {
        try {
            String entrada = scanner.nextLine();
            return Integer.parseInt(entrada.trim());
        } catch (NumberFormatException e) {
            return -1; // Opção inválida
        }
    }

    // ==================== OPÇÕES DO MENU ====================

    /**
     * Opção 1: Cadastrar um novo produto.
     * O usuário informa apenas o nome do produto.
     */
    private void cadastrarProduto() {
        System.out.println("\n--- CADASTRO DE PRODUTO ---");
        System.out.println("Informe apenas o nome do produto.");
        System.out.println("Exemplos: PlayStation 5, RTX 4060, iPhone 15");
        System.out.println();

        System.out.print("Nome do produto: ");
        String nome = scanner.nextLine().trim();

        if (nome.isEmpty()) {
            System.out.println("Nome não pode ser vazio!");
            return;
        }

        if (nome.length() < 3) {
            System.out.println("Nome muito curto! Digite o nome completo do produto.");
            return;
        }

        // Cria o novo produto (sem links - o crawler gera URLs automaticamente)
        Produto produto = new Produto(nome);

        // Salva no repositório
        produtoRepository.adicionar(produto);

        System.out.println();
        System.out.println("Produto cadastrado com sucesso!");
        System.out.println("O sistema irá buscar automaticamente em:");
        System.out.println("  - Amazon, Kabum, Mercado Livre, Magazine Luiza");
    }

    /**
     * Opção 2: Listar todos os produtos cadastrados.
     */
    private void listarProdutos() {
        System.out.println("\n--- LISTA DE PRODUTOS ---");

        List<Produto> produtos = produtoRepository.listarTodos();

        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }

        System.out.println("Total de produtos: " + produtos.size());
        System.out.println();

        for (int i = 0; i < produtos.size(); i++) {
            System.out.println("----------------------------------------");
            System.out.println("[" + (i + 1) + "] " + produtos.get(i).getNome());
            System.out.println("----------------------------------------");
        }
    }

    /**
     * Opção 3: Executar o crawler em todos os produtos.
     * Busca automaticamente em todas as lojas (sem precisar de links cadastrados).
     */
    private void executarCrawler() {
        System.out.println();

        // Verifica se há produtos cadastrados
        if (produtoRepository.estaVazio()) {
            System.out.println("Nenhum produto cadastrado!");
            System.out.println("Cadastre um produto primeiro (opção 1).");
            return;
        }

        System.out.println("=== EXECUTANDO CRAWLER AUTOMÁTICO ===");
        System.out.println("O sistema vai buscar automaticamente em:");
        System.out.println("  - Amazon");
        System.out.println("  - Kabum");
        System.out.println("  - Mercado Livre");
        System.out.println("  - Magazine Luiza");
        System.out.println();

        // Recarrega os repositórios para garantir dados atualizados
        crawlerService = new CrawlerService();

        // Executa o crawler
        crawlerService.executarCrawler();
    }

    /**
     * Opção 4: Visualizar o histórico de preços.
     */
    private void verHistorico() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           SISTEMA DE MONITORAMENTO DE PREÇOS                 ║");
        System.out.println("║                    HISTÓRICO COMPLETO                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<HistoricoPreco> historico = historicoRepository.listarTodos();

        if (historico.isEmpty()) {
            System.out.println("║                                                          ║");
            System.out.println("║  Nenhum registro de histórico encontrado.                 ║");
            System.out.println("║  Execute o crawler primeiro para gerar histórico.         ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            return;
        }

        System.out.println("║  Total de registros: " + historico.size());
        System.out.println("║  Produtos únicos: " + historicoRepository.listarProdutosUnicos().size());
        System.out.println("║  Lojas únicas: " + historicoRepository.listarLojasUnicas().size());
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Como deseja visualizar o histórico?                        ║");
        System.out.println("║  1 - Todos os registros (completo)                          ║");
        System.out.println("║  2 - Por produto (com estatísticas)                         ║");
        System.out.println("║  3 - Por loja (comparativo)                                 ║");
        System.out.println("║  4 - Resumo geral (melhores preços)                         ║");
        System.out.println("║  0 - Voltar ao menu principal                               ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Opção: ");

        int opcao = lerOpcao();

        switch (opcao) {
            case 1:
                listarTodosRegistrosProfissional(historico);
                break;
            case 2:
                listarPorProdutoAgrupado();
                break;
            case 3:
                listarPorLojaAgrupado();
                break;
            case 4:
                listarResumoGeral();
                break;
            case 0:
                return;
            default:
                System.out.println("Opção inválida!");
        }
    }

    /**
     * Lista todos os registros de histórico com formatação profissional.
     */
    private void listarTodosRegistrosProfissional(List<HistoricoPreco> historico) {
        System.out.println("\n┌─────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                    HISTÓRICO COMPLETO DE PREÇOS                        │");
        System.out.println("├──────────────────┼──────────────────┼────────────┬─────────────────────┤");
        System.out.println("│ PRODUTO          │ LOJA             │ PREÇO     │ DATA/HORA           │");
        System.out.println("├──────────────────┼──────────────────┼────────────┼─────────────────────┤");

        for (HistoricoPreco h : historico) {
            String produto = h.getNomeProduto();
            if (produto.length() > 16) produto = produto.substring(0, 13) + "...";
            String loja = h.getLoja();
            if (loja.length() > 16) loja = loja.substring(0, 13) + "...";

            System.out.printf("│ %-16s │ %-16s │ R$ %7.2f │ %-19s │%n",
                    produto, loja, h.getPreco(), h.getDataResumida());
        }

        System.out.println("└──────────────────┴──────────────────┴────────────┴─────────────────────┘");
    }

    /**
     * Lista o histórico agrupado por produto com estatísticas.
     */
    private void listarPorProdutoAgrupado() {
        Map<String, List<HistoricoPreco>> porProduto = historicoRepository.agruparPorProduto();

        System.out.println("\n┌─────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                   HISTÓRICO POR PRODUTO                                │");
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");

        for (Map.Entry<String, List<HistoricoPreco>> entry : porProduto.entrySet()) {
            String produto = entry.getKey();
            List<HistoricoPreco> registros = entry.getValue();

            // Estatísticas
            HistoricoRepository.EstatisticaPreco estatisticas = historicoRepository.calcularEstatisticas(registros);
            HistoricoPreco menorHistorico = historicoRepository.getMenorPrecoHistorico(produto);
            HistoricoPreco maiorHistorico = historicoRepository.getMaiorPrecoHistorico(produto);

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════╗");
            System.out.println("║  PRODUTO: " + produto);
            System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");
            System.out.println("║  Total de registros: " + registros.size());
            System.out.printf("║  Menor preço histórico: R$ %.2f (%s)%n",
                    menorHistorico.getPreco(), menorHistorico.getLoja());
            System.out.printf("║  Maior preço histórico: R$ %.2f (%s)%n",
                    maiorHistorico.getPreco(), maiorHistorico.getLoja());
            System.out.printf("║  Média de preços:      R$ %.2f%n", estatisticas.getMediaPreco());
            System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");
            System.out.println("║  EVOLUÇÃO DE PREÇOS (do mais antigo para o mais recente)              ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");

            // Ordena por data e mostra evolução
            registros.stream()
                    .sorted(Comparator.comparing(HistoricoPreco::getData))
                    .forEach(h -> {
                        String jours = "";
                        if (h.getDiasDesdeRegistro() > 0) {
                            jours = " (" + h.getDiasDesdeRegistro() + " dias atrás)";
                        }
                        System.out.printf("║  %-15s │ R$ %8.2f │ %s%s%n",
                                h.getLoja(), h.getPreco(), h.getDataResumida(), jours);
                    });

            System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
        }
    }

    /**
     * Lista o histórico agrupado por loja com comparativos.
     */
    private void listarPorLojaAgrupado() {
        Map<String, List<HistoricoPreco>> porLoja = historicoRepository.agruparPorLoja();

        System.out.println("\n┌─────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                      HISTÓRICO POR LOJA                                │");
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");

        for (Map.Entry<String, List<HistoricoPreco>> entry : porLoja.entrySet()) {
            String loja = entry.getKey();
            List<HistoricoPreco> registros = entry.getValue();

            HistoricoRepository.EstatisticaPreco estatisticas = historicoRepository.calcularEstatisticas(registros);

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════╗");
            System.out.printf("║  LOJA: %s", loja);
            // Preenche com espaços
            for (int i = loja.length(); i < 66; i++) System.out.print(" ");
            System.out.println("║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");
            System.out.println("║  Produtos monitorados: " + registros.stream()
                    .map(HistoricoPreco::getNomeProduto)
                    .distinct()
                    .count());
            System.out.println("║  Total de registros: " + registros.size());
            System.out.printf("║  Menor preço praticado: R$ %.2f%n",
                    estatisticas.getMenorPreco());
            System.out.printf("║  Maior preço praticado: R$ %.2f%n",
                    estatisticas.getMaiorPreco());
            System.out.printf("║  Média de preços:       R$ %.2f%n",
                    estatisticas.getMediaPreco());
            System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");
            System.out.println("║  ÚLTIMOS REGISTROS                                                  ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");

            // Mostra últimos registros (máximo 10)
            registros.stream()
                    .sorted(Comparator.comparing(HistoricoPreco::getData).reversed())
                    .limit(10)
                    .forEach(h -> {
                        System.out.printf("║  %-20s │ R$ %8.2f │ %s%n",
                                h.getNomeProduto(), h.getPreco(), h.getDataResumida());
                    });

            System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
        }
    }

    /**
     * Exibe um resumo geral com os melhores preços por produto.
     */
    private void listarResumoGeral() {
        List<String> produtos = historicoRepository.listarProdutosUnicos();

        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        RESUMO GERAL DE PREÇOS                         ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Melhores preços por produto (último registro de cada loja)          ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");

        for (String produto : produtos) {
            // Busca o registro mais recente para cada loja deste produto
            List<HistoricoPreco> registrosProduto = historicoRepository.listarPorProduto(produto);

            if (registrosProduto.isEmpty()) continue;

            System.out.println("║                                                                       ║");
            System.out.printf("║  PRODUTO: %s%n", produto);
            System.out.println("║  ────────────────────────────────────────────────────────────────────║");

            // Agrupa por loja e pega o mais recente de cada
            Map<String, List<HistoricoPreco>> porLoja = registrosProduto.stream()
                    .collect(Collectors.groupingBy(HistoricoPreco::getLoja));

            for (Map.Entry<String, List<HistoricoPreco>> entry : porLoja.entrySet()) {
                HistoricoPreco maisRecente = entry.getValue().stream()
                        .max(Comparator.comparing(HistoricoPreco::getData))
                        .orElse(null);

                if (maisRecente != null) {
                    System.out.printf("║    %-15s │ R$ %8.2f │ %s%n",
                            maisRecente.getLoja(),
                            maisRecente.getPreco(),
                            maisRecente.getDiasDesdeRegistro() + " dias");
                }
            }

            // Estatísticas do produto
            HistoricoRepository.EstatisticaPreco est = historicoRepository.calcularEstatisticas(produto);
            if (est != null) {
                System.out.printf("║  ────────────────────────────────────────────────────────────────────║%n");
                System.out.printf("║  Menor: R$ %.2f  │  Maior: R$ %.2f  │  Média: R$ %.2f%n",
                        est.getMenorPreco(), est.getMaiorPreco(), est.getMediaPreco());
            }
        }

        System.out.println("║                                                                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Opção 0: Sair do sistema.
     */
    private void sair() {
        System.out.println("\nObrigado por usar o sistema!");
        System.out.println("Até logo!");
        executando = false;
    }
}