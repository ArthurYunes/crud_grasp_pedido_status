package feira.graspcrud.controller;

import feira.graspcrud.domain.Pedido;
import feira.graspcrud.domain.StatusPedido;
import feira.graspcrud.dto.PedidoRequest;
import feira.graspcrud.dto.PedidoUpdateRequest;
import feira.graspcrud.dto.StatusPedidoRequest;
import feira.graspcrud.exception.RegraNegocioException;
import feira.graspcrud.service.PedidoService;
import feira.graspcrud.service.StatusPedidoService;
import java.util.List;
import java.util.Scanner;

/**
 * Controller responsável por receber as entradas do menu textual e
 * delegar as operações para os serviços de aplicação.
 *
 * <p>Padrão GRASP: Controller — único ponto de entrada das operações do
 * terminal. Não implementa nenhuma regra de negócio; apenas coleta dados
 * do usuário, invoca o serviço adequado e exibe o resultado ou erro.
 *
 * <p>Padrão GRASP: High Cohesion — responsável exclusivamente pela interação
 * com o terminal para o contexto de Pedido e StatusPedido.
 */
public class PedidoController {

    private final PedidoService pedidoService;
    private final StatusPedidoService statusService;
    private final Scanner scanner;

    private static final String SEPARADOR = "=".repeat(55);

    /**
     * Cria o controller com os serviços e o scanner de entrada.
     *
     * @param pedidoService serviço de Pedido
     * @param statusService serviço de StatusPedido
     * @param scanner       leitor de entrada do terminal
     */
    public PedidoController(PedidoService pedidoService,
                            StatusPedidoService statusService,
                            Scanner scanner) {
        this.pedidoService = pedidoService;
        this.statusService = statusService;
        this.scanner = scanner;
    }

    // ── Loop principal ─────────────────────────────────────────────────────

    /**
     * Inicia o loop principal do menu textual, permanecendo ativo até que o
     * usuário escolha a opcao de sair.
     */
    public void iniciar() {
        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();
            System.out.println();
            try {
                switch (opcao) {
                    case "1"  -> cadastrarStatus();
                    case "2"  -> listarStatus();
                    case "3"  -> removerStatus();
                    case "4"  -> cadastrarPedido();
                    case "5"  -> listarPedidos();
                    case "6"  -> buscarPedidoPorId();
                    case "7"  -> atualizarPedido();
                    case "8"  -> avancarStatusPedido();
                    case "9"  -> removerPedido();
                    case "0"  -> rodando = false;
                    default   -> System.out.println("  [!] Opcao invalida. Tente novamente.");
                }
            } catch (RegraNegocioException e) {
                System.out.println("  [ERRO] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [ERRO] Erro inesperado: " + e.getMessage());
            }
            if (rodando) pausar();
        }
        System.out.println("\n  Ate logo! Sistema encerrado.\n");
    }

    // ── StatusPedido ───────────────────────────────────────────────────────

    /**
     * Coleta dados do terminal e cadastra um novo StatusPedido.
     */
    private void cadastrarStatus() {
        System.out.println(SEPARADOR);
        System.out.println("  CADASTRAR STATUS DE PEDIDO");
        System.out.println(SEPARADOR);
        System.out.print("  Nome (ex.: ABERTO, EM_PREPARO, ENTREGUE): ");
        String nome = scanner.nextLine().trim();
        System.out.print("  Descrição (opcional): ");
        String desc = scanner.nextLine().trim();

        StatusPedido criado = statusService.criar(new StatusPedidoRequest(nome, desc.isEmpty() ? null : desc));
        System.out.println("\n  [OK] StatusPedido cadastrado: " + criado);
    }

    /**
     * Exibe todos os StatusPedido cadastrados.
     */
    private void listarStatus() {
        System.out.println(SEPARADOR);
        System.out.println("  LISTA DE STATUS DE PEDIDO");
        System.out.println(SEPARADOR);
        List<StatusPedido> lista = statusService.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("  Nenhum StatusPedido cadastrado.");
        } else {
            lista.forEach(s -> System.out.println("  " + s));
        }
    }

    /**
     * Coleta o id do terminal e remove um StatusPedido (com validação de vínculo).
     */
    private void removerStatus() {
        System.out.println(SEPARADOR);
        System.out.println("  REMOVER STATUS DE PEDIDO");
        System.out.println(SEPARADOR);
        listarStatus();
        System.out.print("\n  ID do StatusPedido a remover: ");
        Long id = lerLong();
        statusService.remover(id);
        System.out.println("\n  [OK] StatusPedido removido com sucesso.");
    }

    // ── Pedido ─────────────────────────────────────────────────────────────

    /**
     * Coleta dados do terminal e cadastra um novo Pedido.
     */
    private void cadastrarPedido() {
        System.out.println(SEPARADOR);
        System.out.println("  CADASTRAR PEDIDO");
        System.out.println(SEPARADOR);
        listarStatus();
        System.out.println();
        System.out.print("  ID do StatusPedido: ");
        Long statusId = lerLong();
        System.out.print("  Nome do Pedido: ");
        String nome = scanner.nextLine().trim();
        System.out.print("  Descrição (opcional): ");
        String desc = scanner.nextLine().trim();

        Pedido criado = pedidoService.criar(new PedidoRequest(nome, desc.isEmpty() ? null : desc, statusId));
        System.out.println("\n  [OK] Pedido cadastrado:\n    " + criado);
    }

    /**
     * Exibe todos os Pedidos cadastrados.
     */
    private void listarPedidos() {
        System.out.println(SEPARADOR);
        System.out.println("  LISTA DE PEDIDOS");
        System.out.println(SEPARADOR);
        List<Pedido> lista = pedidoService.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("  Nenhum Pedido cadastrado.");
        } else {
            lista.forEach(p -> System.out.println("  " + p));
        }
    }

    /**
     * Coleta o id do terminal e exibe os dados do Pedido encontrado.
     */
    private void buscarPedidoPorId() {
        System.out.println(SEPARADOR);
        System.out.println("  BUSCAR PEDIDO POR ID");
        System.out.println(SEPARADOR);
        System.out.print("  ID do Pedido: ");
        Long id = lerLong();
        Pedido p = pedidoService.buscarPorId(id);
        System.out.println("\n  Encontrado:\n    " + p);
    }

    /**
     * Coleta dados do terminal e atualiza nome e descrição de um Pedido.
     * A alteração de status é feita exclusivamente pela opção "Avancar Status".
     */
    private void atualizarPedido() {
        System.out.println(SEPARADOR);
        System.out.println("  ATUALIZAR PEDIDO");
        System.out.println(SEPARADOR);
        listarPedidos();
        System.out.print("\n  ID do Pedido a atualizar: ");
        Long id = lerLong();

        Pedido atual = pedidoService.buscarPorId(id);
        System.out.println("  Pedido atual: " + atual);
        System.out.println("  (Para alterar o status, use a opcao 8 - Avancar Status)");

        System.out.print("  Novo nome [" + atual.getNome() + "]: ");
        String nome = scanner.nextLine().trim();
        if (nome.isEmpty()) nome = atual.getNome();

        System.out.print("  Nova descricao [" + (atual.getDescricao() != null ? atual.getDescricao() : "") + "]: ");
        String desc = scanner.nextLine().trim();
        if (desc.isEmpty()) desc = atual.getDescricao();

        // PedidoUpdateRequest não carrega statusPedidoId — status só avança via opcao 8
        Pedido atualizado = pedidoService.atualizar(id, new PedidoUpdateRequest(nome, desc));
        System.out.println("\n  [OK] Pedido atualizado:\n    " + atualizado);
    }

    /**
     * Coleta dados do terminal e avança o status de um Pedido para o próximo
     * na sequência ABERTO → EM_PREPARO → ENTREGUE.
     */
    private void avancarStatusPedido() {
        System.out.println(SEPARADOR);
        System.out.println("  AVANCAR STATUS DO PEDIDO");
        System.out.println(SEPARADOR);
        listarPedidos();
        System.out.print("\n  ID do Pedido: ");
        Long pedidoId = lerLong();

        listarStatus();
        System.out.print("\n  ID do novo StatusPedido: ");
        Long novoStatusId = lerLong();

        Pedido atualizado = pedidoService.avancarStatus(pedidoId, novoStatusId);
        System.out.println("\n  [OK] Status avancado:\n    " + atualizado);
    }

    /**
     * Coleta o id do terminal e remove um Pedido.
     */
    private void removerPedido() {
        System.out.println(SEPARADOR);
        System.out.println("  REMOVER PEDIDO");
        System.out.println(SEPARADOR);
        listarPedidos();
        System.out.print("\n  ID do Pedido a remover: ");
        Long id = lerLong();
        pedidoService.remover(id);
        System.out.println("\n  [OK] Pedido removido com sucesso.");
    }

    // ── Utilitários de UI ──────────────────────────────────────────────────

    /**
     * Exibe o menu principal com todas as opções disponíveis.
     */
    private void exibirMenu() {
        System.out.println("\n" + SEPARADOR);
        System.out.println("  SISTEMA DE PEDIDOS - FEIRA LIVRE");
        System.out.println(SEPARADOR);
        System.out.println("  --- StatusPedido ---");
        System.out.println("  1. Cadastrar StatusPedido");
        System.out.println("  2. Listar StatusPedido");
        System.out.println("  3. Remover StatusPedido");
        System.out.println("  --- Pedido ---");
        System.out.println("  4. Cadastrar Pedido");
        System.out.println("  5. Listar Pedidos");
        System.out.println("  6. Buscar Pedido por ID");
        System.out.println("  7. Atualizar Pedido");
        System.out.println("  8. Avancar Status do Pedido");
        System.out.println("  9. Remover Pedido");
        System.out.println("  ---");
        System.out.println("  0. Sair");
        System.out.println(SEPARADOR);
        System.out.print("  Opcao: ");
    }

    /**
     * Lê um Long do terminal, repetindo a solicitação em caso de entrada inválida.
     *
     * @return valor Long lido
     */
    private Long lerLong() {
        while (true) {
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("  Valor inválido. Digite um número: ");
            }
        }
    }

    /**
     * Aguarda que o usuário pressione Enter para continuar.
     */
    private void pausar() {
        System.out.print("\n  Pressione Enter para continuar...");
        scanner.nextLine();
    }
}
