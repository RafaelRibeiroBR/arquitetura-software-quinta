import service.MenuService;

/**
 * Classe principal do Sistema de Crawler de Preços Automático.
 *
 * O sistema pesquisa automaticamente produtos nas principais lojas online:
 * - Amazon, Kabum, Mercado Livre, Magazine Luiza
 *
 * FUNCIONAMENTO:
 * 1. Usuario cadastra apenas o nome do produto (ex: "PlayStation 5")
 * 2. O sistema gera automaticamente as URLs de busca
 * 3. O crawler acessa cada loja e extrai os preços
 * 4. O sistema compara os valores e encontra o menor preço
 * 5. O resultado é salvo no histórico
 *
 * COMO EXECUTAR:
 * ------------------
 *
 * Via Maven:
 *   mvn compile
 *   mvn exec:java
 *
 * Via IntelliJ IDEA:
 *   1. Abra o projeto no IntelliJ
 *   2. Vá em Run > Run... > Edit Configurations
 *   3. Configure o Maven com goal: "compile"
 *   4. Depois Another Configuration com goal: "exec:java"
 *   5. Ou clique com botão direito na classe Main > Run 'Main'
 *
 * ARQUIVOS DE DADOS:
 *   - produtos.json: lista de produtos cadastrados
 *   - historico.json: histórico de preços encontrados
 */
public class Main {

    public static void main(String[] args) {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║                                           ║");
        System.out.println("║   CRAWLER DE PREÇOS AUTOMÁTICO             ║");
        System.out.println("║   Busque produtos nas melhores lojas       ║");
        System.out.println("║                                           ║");
        System.out.println("║   Amazon | Kabum | Mercado Livre | Magalu ║");
        System.out.println("║                                           ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();

        System.out.println("Iniciando sistema...");
        System.out.println();

        // Inicia o menu interativo
        MenuService menu = new MenuService();
        menu.exibirMenu();
    }
}