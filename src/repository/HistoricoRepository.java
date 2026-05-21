package repository;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import model.HistoricoPreco;
import util.GsonConfig;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repositório para gerenciar a persistência do histórico de preços em arquivo JSON.
 * Usa GsonConfig com LocalDateTimeAdapter para serializar LocalDateTime corretamente.
 *
 * Tratamento robusto de arquivos JSON:
 * - Cria arquivo automaticamente se não existir
 * - Recria arquivo se estiver vazio ou corrompido
 * - Nunca quebra o sistema mesmo com erros de parsing
 *
 * Compatível com Java 17, 21 e 25 usando Files.readAllBytes() e Files.write()
 */
public class HistoricoRepository {

    private static final String ARQUIVO_HISTORICO = "historico.json";

    private Gson gson;
    private List<HistoricoPreco> historico;

    public HistoricoRepository() {
        this.gson = GsonConfig.getGson();
        this.historico = new ArrayList<>();
        carregarHistorico();
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
     * @param path     Caminho do arquivo a ser escrito
     * @param conteudo Conteúdo a ser escrito
     * @return true se a operação foi bem sucedida, false caso contrário
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
                System.out.println("Arquivo de histórico está vazio.");
                return false;
            }

            String trimmed = conteudo.trim();

            // Verifica se começa com '[' (início de array JSON válido)
            if (!trimmed.startsWith("[")) {
                System.out.println("Arquivo de histórico não é um JSON array válido.");
                return false;
            }

            // Verifica se termina com ']' (fechamento do array)
            if (!trimmed.endsWith("]")) {
                System.out.println("Arquivo de histórico está com formato JSON incompleto.");
                return false;
            }

            return true;

        } catch (Exception e) {
            System.out.println("Erro ao verificar arquivo de histórico: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recria o arquivo JSON com uma lista vazia válida.
     */
    private void recriarArquivoJson() {
        Path path = Paths.get(ARQUIVO_HISTORICO);
        boolean sucesso = escreverArquivoUtf8(path, "[]");
        if (sucesso) {
            System.out.println("Arquivo de histórico recriado com conteúdo válido: []");
        }
    }

    /**
     * Cria o arquivo JSON vazio se não existir.
     */
    private void criarArquivoSeNaoExistir() {
        Path path = Paths.get(ARQUIVO_HISTORICO);
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
                boolean sucesso = escreverArquivoUtf8(path, "[]");
                if (sucesso) {
                    System.out.println("Arquivo de histórico criado automaticamente: " + ARQUIVO_HISTORICO);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de histórico: " + e.getMessage());
        }
    }

    /**
     * Carrega os registros de histórico do arquivo JSON.
     * Tratamento robusto para arquivos vazios, incompletos ou corrompidos.
     * Inclui logs detalhados sobre a quantidade carregada e lojas detectadas.
     */
    public void carregarHistorico() {
        System.out.println("\n  [CARREGAR] ===============================================");
        System.out.println("  [CARREGAR] CARREGANDO HISTÓRICO DO ARQUIVO...");

        Path path = Paths.get(ARQUIVO_HISTORICO);

        // Primeiro passo: criar arquivo se não existir
        if (!Files.exists(path)) {
            criarArquivoSeNaoExistir();
            this.historico = new ArrayList<>();
            System.out.println("  [CARREGAR] Arquivo não encontrado. Lista vazia criada.");
            System.out.println("  [CARREGAR] ===============================================\n");
            return;
        }

        // Segundo passo: verificar se o arquivo é válido
        if (!arquivoJsonValido(path)) {
            System.out.println("  [CARREGAR] Arquivo corrompido ou inválido. Recriando...");
            recriarArquivoJson();
            this.historico = new ArrayList<>();
            System.out.println("  [CARREGAR] ===============================================\n");
            return;
        }

        // Terceiro passo: tentar carregar o conteúdo
        try (FileReader reader = new FileReader(ARQUIVO_HISTORICO)) {
            Type tipoLista = new TypeToken<List<HistoricoPreco>>() {}.getType();
            this.historico = gson.fromJson(reader, tipoLista);

            // Se o parsing resultar em null, inicializa lista vazia
            if (this.historico == null) {
                this.historico = new ArrayList<>();
                System.out.println("  [CARREGAR] Parsing retornou null. Lista vazia iniciada.");
            } else {
                System.out.println("  [CARREGAR] SUCESSO! " + this.historico.size() + " registro(s) carregado(s).");

                // Mostra lojas únicas encontradas
                List<String> lojas = this.historico.stream()
                        .map(HistoricoPreco::getLoja)
                        .distinct()
                        .collect(Collectors.toList());
                System.out.println("  [CARREGAR] Lojas detectadas: " + String.join(", ", lojas));
            }

        } catch (JsonSyntaxException e) {
            // JSON com sintaxe inválida (corrompido)
            System.out.println("  [CARREGAR] ERRO de sintaxe no JSON: " + e.getMessage());
            System.out.println("  [CARREGAR] Recriando arquivo de histórico...");
            recriarArquivoJson();
            this.historico = new ArrayList<>();

        } catch (IOException e) {
            System.out.println("  [CARREGAR] ERRO ao carregar histórico: " + e.getMessage());
            this.historico = new ArrayList<>();
        }

        System.out.println("  [CARREGAR] ===============================================\n");
    }

    /**
     * Salva todos os registros de histórico no arquivo JSON.
     * Inclui logs detalhados sobre a quantidade de registros sendo salvos.
     */
    public void salvarHistorico() {
        int quantidade = historico.size();
        System.out.println("  [SALVAR] Iniciando salvamento de " + quantidade + " registro(s)...");

        try (FileWriter writer = new FileWriter(ARQUIVO_HISTORICO)) {
            gson.toJson(historico, writer);
            System.out.println("  [SALVAR] SUCESSO! " + quantidade + " registro(s) salvo(s) no arquivo " + ARQUIVO_HISTORICO);

            // Log das lojas salvas
            if (quantidade > 0) {
                List<String> lojas = historico.stream()
                        .map(HistoricoPreco::getLoja)
                        .distinct()
                        .collect(Collectors.toList());
                System.out.println("  [SALVAR] Lojas no arquivo: " + String.join(", ", lojas));
            }
        } catch (IOException e) {
            System.err.println("  [SALVAR] ERRO ao salvar histórico: " + e.getMessage());
        }
    }

    /**
     * Adiciona um novo registro de histórico.
     * Implementa prevenção de duplicatas consecutivas (mesmo produto, loja E preço).
     * NÃO ignora registros de lojas diferentes ou preços diferentes.
     * @param registro Registro a ser adicionado
     * @return true se o registro foi adicionado, false se foi ignorado por ser duplicado
     */
    public boolean adicionar(HistoricoPreco registro) {
        System.out.println("\n  [REPO] ===============================================");
        System.out.println("  [REPO] ADICIONAR REGISTRO:");
        System.out.println("  [REPO]   Produto: " + registro.getNomeProduto());
        System.out.println("  [REPO]   Preço:   R$ " + registro.getPreco());
        System.out.println("  [REPO]   Loja:    " + registro.getLoja());
        System.out.println("  [REPO]   Data:    " + registro.getData());

        if (registro == null) {
            System.out.println("  [REPO] ERRO: Registro nulo.");
            return false;
        }

        int tamanhoAntes = historico.size();
        System.out.println("  [REPO] Histórico atual tem " + tamanhoAntes + " registro(s).");

        // Verifica duplicata consecutiva APENAS se há registros
        if (!historico.isEmpty()) {
            HistoricoPreco ultimo = historico.get(historico.size() - 1);
            System.out.println("  [REPO] Último registro no histórico:");
            System.out.println("  [REPO]   Produto: " + ultimo.getNomeProduto());
            System.out.println("  [REPO]   Preço:   R$ " + ultimo.getPreco());
            System.out.println("  [REPO]   Loja:    " + ultimo.getLoja());

            if (ultimo.éDuplicadoConsecutivo(registro)) {
                System.out.println("  [REPO] IGNORADO: Registro duplicado consecutivos (produto + loja + preço iguais)");
                return false;
            } else {
                System.out.println("  [REPO] OK: Registro diferente do anterior, será salvo.");
            }
        } else {
            System.out.println("  [REPO] Histórico vazio, primeiro registro será salvo.");
        }

        // Adiciona o registro
        historico.add(registro);
        int tamanhoDepois = historico.size();
        System.out.println("  [REPO] Registro adicionado. Tamanho: " + tamanhoAntes + " -> " + tamanhoDepois);

        // Salva no arquivo
        salvarHistorico();

        System.out.println("  [REPO] >>> Histórico adicionado para: " + registro.getNomeProduto() + " @ " + registro.getLoja());
        System.out.println("  [REPO] ===============================================\n");
        return true;
    }

    /**
     * Sobrecarga: adiciona um registro de histórico com parâmetros simples.
     * @param nomeProduto Nome do produto
     * @param preco Preço registrado
     * @param loja Nome da loja
     * @return true se o registro foi adicionado, false se foi ignorado
     */
    public boolean adicionar(String nomeProduto, Double preco, String loja) {
        HistoricoPreco registro = new HistoricoPreco(nomeProduto, preco, loja, LocalDateTime.now());
        return adicionar(registro);
    }

    /**
     * Adiciona múltiplos registros de uma vez (usado quando crawler encontra vários preços).
     * Salva TODOS os registros de uma vez no arquivo (batch save).
     * Apenas adiciona registros que não sejam duplicados consecutivos.
     * @param registros Lista de registros a serem adicionados
     * @return Quantidade de registros efetivamente adicionados
     */
    public int adicionarTodos(List<HistoricoPreco> registros) {
        System.out.println("\n  [BATCH] ===============================================");
        System.out.println("  [BATCH] ADICIONAR TODOS - Batch de " + registros.size() + " registro(s)");

        if (registros == null || registros.isEmpty()) {
            System.out.println("  [BATCH] Lista vazia ou nula, nada a adicionar.");
            System.out.println("  [BATCH] ===============================================\n");
            return 0;
        }

        System.out.println("  [BATCH] Registros recebidos:");
        for (HistoricoPreco r : registros) {
            System.out.println("  [BATCH]   - " + r.getNomeProduto() + " @ " + r.getLoja() + " = R$ " + r.getPreco());
        }

        int adicionados = 0;
        int ignorados = 0;

        for (HistoricoPreco registro : registros) {
            if (registro == null) {
                ignorados++;
                continue;
            }

            int tamanhoAntes = historico.size();

            // Verifica duplicata
            if (!historico.isEmpty()) {
                HistoricoPreco ultimo = historico.get(historico.size() - 1);
                if (ultimo.éDuplicadoConsecutivo(registro)) {
                    System.out.println("  [BATCH] IGNORADO (duplicado): " + registro.getNomeProduto() + " @ " + registro.getLoja());
                    ignorados++;
                    continue;
                }
            }

            historico.add(registro);
            adicionados++;
            System.out.println("  [BATCH] ADICIONADO: " + registro.getNomeProduto() + " @ " + registro.getLoja() + " = R$ " + registro.getPreco());
        }

        // Salva todos de uma vez
        System.out.println("  [BATCH] Salvando " + adicionados + " registro(s) no arquivo...");
        salvarHistorico();

        System.out.println("  [BATCH] Resultado: " + adicionados + " adicionado(s), " + ignorados + " ignorado(s)");
        System.out.println("  [BATCH] Total no histórico agora: " + historico.size() + " registro(s)");
        System.out.println("  [BATCH] ===============================================\n");

        return adicionados;
    }

    /**
     * Remove um registro pela posição na lista.
     * @param posicao Posição do registro a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean remover(int posicao) {
        if (posicao >= 0 && posicao < historico.size()) {
            historico.remove(posicao);
            salvarHistorico();
            System.out.println("Registro removido com sucesso!");
            return true;
        }
        System.out.println("Posição inválida para remoção.");
        return false;
    }

    /**
     * Lista todos os registros de histórico.
     * @return Lista com todos os registros
     */
    public List<HistoricoPreco> listarTodos() {
        return new ArrayList<>(historico);
    }

    /**
     * Lista todos os registros de histórico para um produto específico.
     * @param nomeProduto Nome do produto a filtrar
     * @return Lista de registros do produto
     */
    public List<HistoricoPreco> listarPorProduto(String nomeProduto) {
        return historico.stream()
                .filter(h -> h.getNomeProduto().equalsIgnoreCase(nomeProduto))
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os registros de histórico de uma loja específica.
     * @param loja Nome da loja a filtrar
     * @return Lista de registros da loja
     */
    public List<HistoricoPreco> listarPorLoja(String loja) {
        return historico.stream()
                .filter(h -> h.getLoja().equalsIgnoreCase(loja))
                .collect(Collectors.toList());
    }

    /**
     * Lista os últimos preços de cada produto (sem duplicatas).
     * @return Lista com o último preço de cada produto
     */
    public List<HistoricoPreco> listarUltimosPrecos() {
        List<HistoricoPreco> ultimos = new ArrayList<>();
        List<String> produtosJaAdicionados = new ArrayList<>();

        for (int i = historico.size() - 1; i >= 0; i--) {
            HistoricoPreco h = historico.get(i);
            if (!produtosJaAdicionados.contains(h.getNomeProduto())) {
                ultimos.add(h);
                produtosJaAdicionados.add(h.getNomeProduto());
            }
        }

        return ultimos;
    }

    /**
     * Retorna o último preço registrado de um produto.
     * @param nomeProduto Nome do produto
     * @return Último registro ou null se não encontrado
     */
    public HistoricoPreco getUltimoPreco(String nomeProduto) {
        return historico.stream()
                .filter(h -> h.getNomeProduto().equalsIgnoreCase(nomeProduto))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    /**
     * Limpa todos os registros de histórico.
     */
    public void limparTodos() {
        historico.clear();
        salvarHistorico();
        System.out.println("Todo o histórico foi removido.");
    }

    /**
     * Retorna a quantidade de registros.
     * @return Quantidade de registros
     */
    public int getQuantidade() {
        return historico.size();
    }

    /**
     * Verifica se não há registros.
     * @return true se a lista estiver vazia
     */
    public boolean estaVazio() {
        return historico.isEmpty();
    }

    /**
     * Getter para a lista de histórico.
     * @return Lista de histórico
     */
    public List<HistoricoPreco> getHistorico() {
        return historico;
    }

    // ==================== MÉTODOS DE ANÁLISE E AGRUPAMENTO ====================

    /**
     * Agrupa os registros por produto.
     * @return Mapa de produto -> lista de registros
     */
    public java.util.Map<String, List<HistoricoPreco>> agruparPorProduto() {
        return historico.stream()
                .collect(Collectors.groupingBy(HistoricoPreco::getNomeProduto));
    }

    /**
     * Agrupa os registros por loja.
     * @return Mapa de loja -> lista de registros
     */
    public java.util.Map<String, List<HistoricoPreco>> agruparPorLoja() {
        return historico.stream()
                .collect(Collectors.groupingBy(HistoricoPreco::getLoja));
    }

    /**
     * Calcula estatísticas de preço para um produto específico.
     * @param nomeProduto Nome do produto
     * @return Estatísticas calculadas ou null se não houver dados
     */
    public EstatisticaPreco calcularEstatisticas(String nomeProduto) {
        List<HistoricoPreco> registros = listarPorProduto(nomeProduto);
        return calcularEstatisticas(registros);
    }

    /**
     * Calcula estatísticas de preço para uma lista de registros.
     * @param registros Lista de registros
     * @return Estatísticas calculadas ou null se não houver dados
     */
    public EstatisticaPreco calcularEstatisticas(List<HistoricoPreco> registros) {
        if (registros == null || registros.isEmpty()) {
            return null;
        }

        DoubleSummaryStatistics stats = registros.stream()
                .mapToDouble(HistoricoPreco::getPreco)
                .summaryStatistics();

        return new EstatisticaPreco(
                stats.getMin(),
                stats.getMax(),
                stats.getAverage(),
                stats.getCount()
        );
    }

    /**
     * Retorna o menor preço histórico para um produto.
     * @param nomeProduto Nome do produto
     * @return Menor preço encontrado ou null
     */
    public HistoricoPreco getMenorPrecoHistorico(String nomeProduto) {
        return listarPorProduto(nomeProduto).stream()
                .min(Comparator.comparing(HistoricoPreco::getPreco))
                .orElse(null);
    }

    /**
     * Retorna o maior preço histórico para um produto.
     * @param nomeProduto Nome do produto
     * @return Maior preço encontrado ou null
     */
    public HistoricoPreco getMaiorPrecoHistorico(String nomeProduto) {
        return listarPorProduto(nomeProduto).stream()
                .max(Comparator.comparing(HistoricoPreco::getPreco))
                .orElse(null);
    }

    /**
     * Lista todos os produtos únicos no histórico.
     * @return Lista de nomes de produtos
     */
    public List<String> listarProdutosUnicos() {
        return historico.stream()
                .map(HistoricoPreco::getNomeProduto)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Lista todas as lojas únicas no histórico.
     * @return Lista de nomes de lojas
     */
    public List<String> listarLojasUnicas() {
        return historico.stream()
                .map(HistoricoPreco::getLoja)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Retorna o histórico mais recente de cada produto.
     * @return Lista com o registro mais recente de cada produto
     */
    public List<HistoricoPreco> getMaisRecentesPorProduto() {
        return agruparPorProduto().values().stream()
                .map(lista -> lista.stream()
                        .max(Comparator.comparing(HistoricoPreco::getData))
                        .orElse(null))
                .filter(h -> h != null)
                .collect(Collectors.toList());
    }

    /**
     * Classe interna para representar estatísticas de preço.
     */
    public static class EstatisticaPreco {
        private final double menorPreco;
        private final double maiorPreco;
        private final double mediaPreco;
        private final long totalRegistros;

        public EstatisticaPreco(double menorPreco, double maiorPreco, double mediaPreco, long totalRegistros) {
            this.menorPreco = menorPreco;
            this.maiorPreco = maiorPreco;
            this.mediaPreco = mediaPreco;
            this.totalRegistros = totalRegistros;
        }

        public double getMenorPreco() {
            return menorPreco;
        }

        public double getMaiorPreco() {
            return maiorPreco;
        }

        public double getMediaPreco() {
            return mediaPreco;
        }

        public long getTotalRegistros() {
            return totalRegistros;
        }

        @Override
        public String toString() {
            return String.format("Estatísticas: menor=R$ %.2f | maior=R$ %.2f | média=R$ %.2f | registros=%d",
                    menorPreco, maiorPreco, mediaPreco, totalRegistros);
        }
    }
}