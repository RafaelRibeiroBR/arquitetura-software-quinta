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
 * Servico responsavel por exibir e gerenciar o menu do sistema.
 * Sistema SIMPLIFICADO: usuario cadastra so o nome do produto e o crawler faz o resto.
 */
public class MenuService {

    // Scanner para ler entrada do usuario
    private Scanner scanner;

    // Repositorio de produtos
    private ProdutoRepository produtoRepository;

    // Repositorio de historico
    private HistoricoRepository historicoRepository;

    // Servico de crawler
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
     * Exibe o menu principal e processa a entrada do usuario.
     */
    public void exibirMenu() {
        while (executando) {
            exibirOpcoes();
            int opcao = lerOpcao();

            // Processa a opcao elegida usando switch-case
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
                case 5:
                    removerProduto();
                    break;
                case 0:
                    sair();
                    break;
                default:
                    System.out.println("Opcao invalida! Digite um numero entre 0 e 5.");
                    break;
            }

            if (executando) {
                System.out.println();
            }
        }
    }

    /**
     * Exibe as opcoes do menu principal.
     */
    private void exibirOpcoes() {
        System.out.println();
        System.out.println("+----------------------------------------------------+");
        System.out.println("|     CRAWLER DE PRECOS DE PRODUTOS                   |");
        System.out.println("|     (Busca Automatica nas Lojas)                   |");
        System.out.println("+----------------------------------------------------+");
        System.out.println("|  1 - Cadastrar produto                             |");
        System.out.println("|  2 - Listar produtos                               |");
        System.out.println("|  3 - Executar crawler                              |");
        System.out.println("|  4 - Ver historico de precos                       |");
        System.out.println("|  5 - Remover produto                               |");
        System.out.println("|  0 - Sair                                          |");
        System.out.println("+----------------------------------------------------+");
        System.out.println();
        System.out.print("Escolha uma opcao: ");
    }

    /**
     * Le a opcao escolhida pelo usuario.
     * @return Opcao numerica
     */
    private int lerOpcao() {
        try {
            String entrada = scanner.nextLine();
            return Integer.parseInt(entrada.trim());
        } catch (NumberFormatException e) {
            return -1; // Opcao invalida
        }
    }

    // ==================== OPCOES DO MENU ====================

    /**
     * Opcao 1: Cadastrar um novo produto.
     * O usuario informa apenas o nome do produto.
     */
    private void cadastrarProduto() {
        System.out.println("\n--- CADASTRO DE PRODUTO ---");
        System.out.println("Informe apenas o nome do produto.");
        System.out.println("Exemplos: PlayStation 5, RTX 4060, iPhone 15");
        System.out.println();

        System.out.print("Nome do produto: ");
        String nome = scanner.nextLine().trim();

        if (nome.isEmpty()) {
            System.out.println("Nome nao pode ser vazio!");
            return;
        }

        if (nome.length() < 3) {
            System.out.println("Nome muito curto! Digite o nome completo do produto.");
            return;
        }

        // Cria o novo produto (sem links - o crawler gera URLs automaticamente)
        Produto produto = new Produto(nome);

        // Salva no repositorio
        produtoRepository.adicionar(produto);

        System.out.println();
        System.out.println("Produto cadastrado com sucesso!");
        System.out.println("O sistema ira buscar automaticamente em:");
        System.out.println("  - Amazon, Kabum, Mercado Livre, Magazine Luiza");
    }

    /**
     * Opcao 2: Listar todos os produtos cadastrados.
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
     * Opcao 3: Executar o crawler em todos os produtos.
     * Busca automaticamente em todas as lojas (sem precisar de links cadastrados).
     */
    private void executarCrawler() {
        System.out.println();

        // Verifica se ha produtos cadastrados
        if (produtoRepository.estaVazio()) {
            System.out.println("Nenhum produto cadastrado!");
            System.out.println("Cadastre um produto primeiro (opcao 1).");
            return;
        }

        System.out.println("=== EXECUTANDO CRAWLER AUTOMATICO ===");
        System.out.println("O sistema vai buscar automaticamente em:");
        System.out.println("  - Amazon");
        System.out.println("  - Kabum");
        System.out.println("  - Mercado Livre");
        System.out.println("  - Magazine Luiza");
        System.out.println();

        // Recarrega os repositorios para garantir dados atualizados
        crawlerService = new CrawlerService();

        // Executa o crawler
        crawlerService.executarCrawler();
    }

    /**
     * Opcao 4: Visualizar o historico de precos.
     */
    private void verHistorico() {
        System.out.println("\n+--------------------------------------------------------+");
        System.out.println("|           SISTEMA DE MONITORAMENTO DE PRECOS          |");
        System.out.println("|                    HISTORICO COMPLETO                  |");
        System.out.println("+--------------------------------------------------------+");

        // DEBUG: Log detalhado de carregamento do histórico
        System.out.println("  [DEBUG-MENU] Carregando histórico via repository...");
        List<HistoricoPreco> historico = historicoRepository.listarTodos();
        System.out.println("  [DEBUG-MENU] Registros retornados pelo repository: " + historico.size());

        if (historico.isEmpty()) {
            System.out.println("|                                                        |");
            System.out.println("|  Nenhum registro de historico encontrado.              |");
            System.out.println("|  Execute o crawler primeiro para gerar historico.     |");
            System.out.println("+--------------------------------------------------------+");
            return;
        }

        // DEBUG: Log de produtos únicos
        List<String> produtosUnicos = historicoRepository.listarProdutosUnicos();
        List<String> lojasUnicas = historicoRepository.listarLojasUnicas();
        System.out.println("  [DEBUG-MENU] Produtos únicos encontrados: " + produtosUnicos.size());
        System.out.println("  [DEBUG-MENU] Lojas únicas encontradas: " + lojasUnicas.size());
        System.out.println("  [DEBUG-MENU] Lista de produtos: " + String.join(", ", produtosUnicos));

        System.out.println("|  Total de registros: " + historico.size());
        System.out.println("|  Produtos unicos: " + produtosUnicos.size());
        System.out.println("|  Lojas unicas: " + lojasUnicas.size());
        System.out.println("+--------------------------------------------------------+");
        System.out.println("|  Como deseja visualizar o historico?                    |");
        System.out.println("|  1 - Todos os registros (completo)                      |");
        System.out.println("|  2 - Por produto (com estatisticas)                     |");
        System.out.println("|  3 - Por loja (comparativo)                             |");
        System.out.println("|  4 - Resumo geral (melhores precos)                     |");
        System.out.println("|  0 - Voltar ao menu principal                           |");
        System.out.println("+--------------------------------------------------------+");
        System.out.print("Opcao: ");

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
                System.out.println("Opcao invalida!");
        }
    }

    /**
     * Lista todos os registros de historico com formatacao profissional.
     */
    private void listarTodosRegistrosProfissional(List<HistoricoPreco> historico) {
        System.out.println("\n+------------------------------------------------------------------+");
        System.out.println("|                    HISTORICO COMPLETO DE PRECOS                  |");
        System.out.println("+------------------+------------------+------------+-----------------+");
        System.out.println("| PRODUTO          | LOJA             | PRECO     | DATA/HORA        |");
        System.out.println("+------------------+------------------+------------+-----------------+");

        for (HistoricoPreco h : historico) {
            String produto = h.getNomeProduto();
            if (produto.length() > 16) produto = produto.substring(0, 13) + "...";
            String loja = h.getLoja();
            if (loja.length() > 16) loja = loja.substring(0, 13) + "...";

            System.out.printf("| %-16s | %-16s | R$ %7.2f | %-16s |%n",
                    produto, loja, h.getPreco(), h.getDataResumida());
        }

        System.out.println("+------------------+------------------+------------+-----------------+");
    }

    /**
     * Lista o historico agrupado por produto com estatisticas.
     */
    private void listarPorProdutoAgrupado() {
        Map<String, List<HistoricoPreco>> porProduto = historicoRepository.agruparPorProduto();

        System.out.println("\n  [DEBUG] ========== LISTAR POR PRODUTO ==========");
        System.out.println("  [DEBUG] Grupos de produtos criados: " + porProduto.size());
        System.out.println("  [DEBUG] Produtos no agrupamento: " + String.join(", ", porProduto.keySet()));

        System.out.println("\n+------------------------------------------------------------------+");
        System.out.println("|                   HISTORICO POR PRODUTO                          |");
        System.out.println("+------------------------------------------------------------------+");

        for (Map.Entry<String, List<HistoricoPreco>> entry : porProduto.entrySet()) {
            String produto = entry.getKey();
            List<HistoricoPreco> registros = entry.getValue();

            System.out.println("  [DEBUG] Processando produto: " + produto + " | Registros: " + registros.size());

            // Estatisticas
            HistoricoRepository.EstatisticaPreco estatisticas = historicoRepository.calcularEstatisticas(registros);
            HistoricoPreco menorHistorico = historicoRepository.getMenorPrecoHistorico(produto);
            HistoricoPreco maiorHistorico = historicoRepository.getMaiorPrecoHistorico(produto);

            System.out.println("\n+======================================================================+");
            System.out.println("|  PRODUTO: " + produto);
            System.out.println("+======================================================================+");
            System.out.println("|  Total de registros: " + registros.size());
            System.out.printf("|  Menor preco historico: R$ %.2f (%s)%n",
                    menorHistorico.getPreco(), menorHistorico.getLoja());
            System.out.printf("|  Maior preco historico: R$ %.2f (%s)%n",
                    maiorHistorico.getPreco(), maiorHistorico.getLoja());
            System.out.printf("|  Media de precos:      R$ %.2f%n", estatisticas.getMediaPreco());
            System.out.println("+======================================================================+");
            System.out.println("|  EVOLUCAO DE PRECOS (do mais antigo para o mais recente)           |");
            System.out.println("+======================================================================+");

            // Ordena por data e mostra evolucao
            registros.stream()
                    .sorted(Comparator.comparing(HistoricoPreco::getData))
                    .forEach(h -> {
                        String jours = "";
                        if (h.getDiasDesdeRegistro() > 0) {
                            jours = " (" + h.getDiasDesdeRegistro() + " dias atras)";
                        }
                        System.out.printf("|  %-15s | R$ %8.2f | %s%s%n",
                                h.getLoja(), h.getPreco(), h.getDataResumida(), jours);
                    });

            System.out.println("+======================================================================+");
        }
    }

    /**
     * Lista o historico agrupado por loja com comparativos.
     */
    private void listarPorLojaAgrupado() {
        Map<String, List<HistoricoPreco>> porLoja = historicoRepository.agruparPorLoja();

        System.out.println("\n  [DEBUG] ========== LISTAR POR LOJA ==========");
        System.out.println("  [DEBUG] Grupos de lojas criados: " + porLoja.size());
        System.out.println("  [DEBUG] Lojas no agrupamento: " + String.join(", ", porLoja.keySet()));

        System.out.println("\n+------------------------------------------------------------------+");
        System.out.println("|                      HISTORICO POR LOJA                           |");
        System.out.println("+------------------------------------------------------------------+");

        for (Map.Entry<String, List<HistoricoPreco>> entry : porLoja.entrySet()) {
            String loja = entry.getKey();
            List<HistoricoPreco> registros = entry.getValue();

            System.out.println("  [DEBUG] Processando loja: " + loja + " | Registros: " + registros.size());

            HistoricoRepository.EstatisticaPreco estatisticas = historicoRepository.calcularEstatisticas(registros);

            System.out.println("\n+======================================================================+");
            System.out.printf("|  LOJA: %s", loja);
            // Preenche com espacos
            for (int i = loja.length(); i < 66; i++) System.out.print(" ");
            System.out.println("|");
            System.out.println("+======================================================================+");
            System.out.println("|  Produtos monitorados: " + registros.stream()
                    .map(HistoricoPreco::getNomeProduto)
                    .distinct()
                    .count());
            System.out.println("|  Total de registros: " + registros.size());
            System.out.printf("|  Menor preco praticado: R$ %.2f%n",
                    estatisticas.getMenorPreco());
            System.out.printf("|  Maior preco praticado: R$ %.2f%n",
                    estatisticas.getMaiorPreco());
            System.out.printf("|  Media de precos:       R$ %.2f%n",
                    estatisticas.getMediaPreco());
            System.out.println("+======================================================================+");
            System.out.println("|  ULTIMOS REGISTROS                                                 |");
            System.out.println("+======================================================================+");

            // Mostra ultimos registros (maximo 10)
            registros.stream()
                    .sorted(Comparator.comparing(HistoricoPreco::getData).reversed())
                    .limit(10)
                    .forEach(h -> {
                        System.out.printf("|  %-20s | R$ %8.2f | %s%n",
                                h.getNomeProduto(), h.getPreco(), h.getDataResumida());
                    });

            System.out.println("+======================================================================+");
        }
    }

    /**
     * Exibe um resumo geral com os melhores precos por produto.
     */
    private void listarResumoGeral() {
        List<String> produtos = historicoRepository.listarProdutosUnicos();

        System.out.println("\n  [DEBUG] ========== RESUMO GERAL ==========");
        System.out.println("  [DEBUG] Produtos únicos para exibir: " + produtos.size());
        System.out.println("  [DEBUG] Lista de produtos: " + String.join(", ", produtos));

        System.out.println("\n+======================================================================+");
        System.out.println("|                        RESUMO GERAL DE PRECOS                        |");
        System.out.println("+======================================================================+");
        System.out.println("|  Melhores precos por produto (ultimo registro de cada loja)         |");
        System.out.println("+======================================================================+");

        for (String produto : produtos) {
            System.out.println("  [DEBUG] Processando produto no resumo: " + produto);
            // Busca o registro mais recente para cada loja deste produto
            List<HistoricoPreco> registrosProduto = historicoRepository.listarPorProduto(produto);

            if (registrosProduto.isEmpty()) continue;

            System.out.println("|                                                                      |");
            System.out.printf("|  PRODUTO: %s%n", produto);
            System.out.println("|  -----------------------------------------------------------------------|");

            // Agrupa por loja e pega o mais recente de cada
            Map<String, List<HistoricoPreco>> porLoja = registrosProduto.stream()
                    .collect(Collectors.groupingBy(HistoricoPreco::getLoja));

            for (Map.Entry<String, List<HistoricoPreco>> entry : porLoja.entrySet()) {
                HistoricoPreco maisRecente = entry.getValue().stream()
                        .max(Comparator.comparing(HistoricoPreco::getData))
                        .orElse(null);

                if (maisRecente != null) {
                    System.out.printf("|    %-15s | R$ %8.2f | %s%n",
                            maisRecente.getLoja(),
                            maisRecente.getPreco(),
                            maisRecente.getDiasDesdeRegistro() + " dias");
                }
            }

            // Estatisticas do produto
            HistoricoRepository.EstatisticaPreco est = historicoRepository.calcularEstatisticas(produto);
            if (est != null) {
                System.out.printf("|  -----------------------------------------------------------------------|%n");
                System.out.printf("|  Menor: R$ %.2f  |  Maior: R$ %.2f  |  Media: R$ %.2f%n",
                        est.getMenorPreco(), est.getMaiorPreco(), est.getMediaPreco());
            }
        }

        System.out.println("|                                                                      |");
        System.out.println("+======================================================================+");
    }

    /**
     * Opcao 5: Remover um produto cadastrado.
     */
    private void removerProduto() {
        System.out.println("\n+==============================================================+");
        System.out.println("|                    REMOVER PRODUTO                            |");
        System.out.println("+==============================================================+");

        List<Produto> produtos = produtoRepository.listarTodos();

        // Validacao: lista vazia
        if (produtos.isEmpty()) {
            System.out.println("|                                                              |");
            System.out.println("|  Nenhum produto cadastrado.                                  |");
            System.out.println("|  Cadastre um produto primeiro (opcao 1 do menu).             |");
            System.out.println("+==============================================================+");
            return;
        }

        // Listar todos os produtos com indice
        System.out.println("|                                                              |");
        System.out.println("|  PRODUTOS CADASTRADOS:                                       |");
        System.out.println("|  ------------------------------------------------------------|");

        for (int i = 0; i < produtos.size(); i++) {
            System.out.printf("|  [%d] %-53s |%n", (i + 1), produtos.get(i).getNome());
        }

        System.out.println("|                                                              |");
        System.out.println("+==============================================================+");
        System.out.println();
        System.out.print("Digite o numero do produto para remover (0 para cancelar): ");

        int escolha = lerOpcao();

        // Validacao: Cancelar
        if (escolha == 0) {
            System.out.println("Operacao cancelada pelo usuario.");
            return;
        }

        // Validacao: indice invalido (negativo ou maior que o tamanho)
        if (escolha < 1 || escolha > produtos.size()) {
            System.out.println();
            System.out.println("+==============================================================+");
            System.out.println("|  ERRO: Indice invalido!                                       |");
            System.out.println("|  Digite um numero entre 1 e " + produtos.size() + ".                              |");
            System.out.println("+==============================================================+");
            return;
        }

        // Obter o produto selecionado (indice e escolha - 1)
        Produto produtoSelecionado = produtos.get(escolha - 1);
        String nomeProduto = produtoSelecionado.getNome();

        // Confirmar remocao
        System.out.println();
        System.out.println("+==============================================================+");
        System.out.println("|  CONFIRMACAO DE REMOCAO                                      |");
        System.out.println("+==============================================================+");
        System.out.printf("|  Produto: %-49s |%n", nomeProduto);
        System.out.println("|                                                              |");
        System.out.println("|  Deseja remover tambem o historico deste produto?            |");
        System.out.println("|  1 - Sim (remover historico completo)                        |");
        System.out.println("|  2 - Nao (manter historico apenas)                          |");
        System.out.println("+==============================================================+");
        System.out.println();
        System.out.print("Escolha uma opcao (1-2): ");

        int opcaoHistorico = lerOpcao();

        // Validacao da opcao de historico
        if (opcaoHistorico != 1 && opcaoHistorico != 2) {
            System.out.println();
            System.out.println("+==============================================================+");
            System.out.println("|  ERRO: Opcao invalida!                                       |");
            System.out.println("|  Digite 1 para SIM ou 2 para NAO.                           |");
            System.out.println("+==============================================================+");
            return;
        }

        // Remover o produto
        boolean removido = produtoRepository.remover(produtoSelecionado.getId());

        if (removido) {
            System.out.println();
            System.out.println("+==============================================================+");
            System.out.println("|  [OK] PRODUTO REMOVIDO COM SUCESSO                           |");
            System.out.println("+==============================================================+");
            System.out.printf("|  Produto removido: %-45s |%n", nomeProduto);

            // Remover historico se solicitado
            if (opcaoHistorico == 1) {
                int quantidadeRemovida = historicoRepository.removerPorProduto(nomeProduto);
                System.out.printf("|  Registros de historico removidos: %-27d |%n", quantidadeRemovida);
            } else {
                System.out.println("|  Historico preservado.                                       |");
            }

            System.out.println("|                                                              |");
            System.out.println("|  Arquivos JSON atualizados automaticamente.                   |");
            System.out.println("+==============================================================+");
        } else {
            System.out.println();
            System.out.println("+==============================================================+");
            System.out.println("|  [ERRO] AO REMOVER PRODUTO                                   |");
            System.out.println("|  O produto nao foi encontrado no repositorio.                 |");
            System.out.println("+==============================================================+");
        }
    }

    /**
     * Opcao 0: Sair do sistema.
     */
    private void sair() {
        System.out.println("\nObrigado por usar o sistema!");
        System.out.println("Ate logo!");
        executando = false;
    }
}