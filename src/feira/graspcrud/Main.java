package feira.graspcrud;

import feira.graspcrud.controller.PedidoController;
import feira.graspcrud.repository.PedidoRepository;
import feira.graspcrud.repository.StatusPedidoRepository;
import feira.graspcrud.repository.json.PedidoRepositoryJson;
import feira.graspcrud.repository.json.StatusPedidoRepositoryJson;
import feira.graspcrud.service.PedidoService;
import feira.graspcrud.service.StatusPedidoService;

import java.util.Scanner;

/**
 * Ponto de entrada da aplicação — Sistema de Pedidos da Feira Livre.
 *
 * <p>Padrão GRASP: Creator — o Main é o único responsável por instanciar e
 * conectar manualmente todas as dependências (repositórios, serviços e controller),
 * substituindo um framework de injeção de dependências.
 *
 * <p>Ordem de criação:
 * <ol>
 *   <li>Repositórios concretos (JSON) são criados e carregam os dados do disco.</li>
 *   <li>Serviços recebem os repositórios por suas interfaces (Low Coupling).</li>
 *   <li>Controller recebe os serviços e o Scanner.</li>
 *   <li>Loop principal é iniciado.</li>
 * </ol>
 */
public class Main {

    /**
     * Método principal que inicializa e executa o sistema.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        // 1. Repositórios — implementações JSON (Pure Fabrication)
        StatusPedidoRepository statusRepo = new StatusPedidoRepositoryJson();
        PedidoRepository pedidoRepo = new PedidoRepositoryJson(statusRepo);

        // 2. Serviços — dependem das interfaces (Low Coupling / Protected Variations)
        StatusPedidoService statusService = new StatusPedidoService(statusRepo, pedidoRepo);
        PedidoService pedidoService = new PedidoService(pedidoRepo, statusRepo);

        // 3. Pré-popula os status padrão da sequência obrigatória na primeira execução.
        //    O serviço ignora silenciosamente se já existirem (unicidade de nome).
        inicializarStatusPadrao(statusService);

        // 4. Controller — ponto único de entrada do terminal (Controller GRASP)
        Scanner scanner = new Scanner(System.in);
        PedidoController controller = new PedidoController(pedidoService, statusService, scanner);

        // 5. Início do loop principal
        controller.iniciar();
    }

    /**
     * Garante que os três StatusPedido da sequência obrigatória existam no sistema.
     * Chamado uma única vez na inicialização; não lança exceção se já existirem.
     *
     * <p>Padrão GRASP: Creator — o Main, como responsável pela composição do sistema,
     * é também quem fornece os dados de seed necessários para o funcionamento correto.
     *
     * @param statusService serviço utilizado para verificar e criar os status padrão
     */
    private static void inicializarStatusPadrao(StatusPedidoService statusService) {
        String[][] padrao = {
            {"ABERTO",     "Pedido recebido aguardando preparo"},
            {"EM_PREPARO", "Pedido em preparo na barraca"},
            {"ENTREGUE",   "Pedido entregue ao cliente"}
        };
        for (String[] sp : padrao) {
            statusService.criarSeNaoExistir(sp[0], sp[1]);
        }
    }
}
