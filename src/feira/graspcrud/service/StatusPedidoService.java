package feira.graspcrud.service;

import feira.graspcrud.domain.StatusPedido;
import feira.graspcrud.dto.StatusPedidoRequest;
import feira.graspcrud.exception.RegraNegocioException;
import feira.graspcrud.repository.PedidoRepository;
import feira.graspcrud.repository.StatusPedidoRepository;

import java.util.List;

/**
 * Serviço de aplicação responsável pelos casos de uso de StatusPedido.
 *
 * <p>Padrão GRASP: Low Coupling — depende de {@link StatusPedidoRepository} e
 * {@link PedidoRepository} por suas interfaces, sem conhecer a implementação
 * concreta de persistência.
 *
 * <p>Padrão GRASP: High Cohesion — responsável exclusivamente pelos casos de uso
 * relacionados ao StatusPedido. Não contém lógica de apresentação nem acessa
 * diretamente o sistema de arquivos.
 */
public class StatusPedidoService {

    private final StatusPedidoRepository statusRepo;
    private final PedidoRepository pedidoRepo;

    /**
     * Cria o serviço com as dependências fornecidas pelo Main (Creator).
     *
     * @param statusRepo repositório de StatusPedido
     * @param pedidoRepo repositório de Pedido (usado na validação de remoção)
     */
    public StatusPedidoService(StatusPedidoRepository statusRepo, PedidoRepository pedidoRepo) {
        this.statusRepo = statusRepo;
        this.pedidoRepo = pedidoRepo;
    }

    /**
     * Cria um StatusPedido somente se ainda não existir um com o mesmo nome.
     * Usado para garantir os status padrão da sequência obrigatória na inicialização.
     *
     * <p>Regra de negócio: operação idempotente — não lança exceção se o status
     * já existir, simplesmente ignora.
     *
     * @param nome      nome do status a garantir
     * @param descricao descrição padrão a usar na criação
     */
    public void criarSeNaoExistir(String nome, String descricao) {
        String nomeTrimado = nome != null ? nome.trim() : "";
        if (statusRepo.buscarPorNome(nomeTrimado).isEmpty()) {
            statusRepo.salvar(new StatusPedido(null, nome, descricao));
        }
    }

    /**
     * Cadastra um novo StatusPedido garantindo unicidade do nome.
     *
     * <p>Regra de negócio: o nome do StatusPedido é obrigatório e único no cadastro.
     *
     * @param request dados de entrada com nome e descrição
     * @return StatusPedido persistido com id gerado
     * @throws RegraNegocioException se já existir um status com o mesmo nome
     */
    public StatusPedido criar(StatusPedidoRequest request) {
        // Usa o nome com trim para bater exatamente com o que o domínio vai armazenar,
        // evitando que "ABERTO " passe na checagem mas seja salvo como "ABERTO"
        String nomeTrimado = request.getNome() != null ? request.getNome().trim() : "";
        statusRepo.buscarPorNome(nomeTrimado).ifPresent(s -> {
            throw new RegraNegocioException(
                "Ja existe um StatusPedido com o nome '" + nomeTrimado + "'.");
        });
        // Creator: o serviço possui todos os dados e instancia o domínio
        StatusPedido novo = new StatusPedido(null, request.getNome(), request.getDescricao());
        return statusRepo.salvar(novo);
    }

    /**
     * Retorna a lista completa de StatusPedido cadastrados.
     *
     * @return lista de status
     */
    public List<StatusPedido> listarTodos() {
        return statusRepo.listarTodos();
    }

    /**
     * Busca um StatusPedido pelo id, lançando exceção se não encontrado.
     *
     * @param id identificador único
     * @return StatusPedido encontrado
     * @throws RegraNegocioException se não existir status com o id informado
     */
    public StatusPedido buscarPorId(Long id) {
        return statusRepo.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "StatusPedido com id " + id + " nao encontrado."));
    }

    /**
     * Remove um StatusPedido verificando previamente se está em uso por algum Pedido.
     *
     * <p>Regra de negócio: não é permitido remover um StatusPedido que esteja
     * associado a ao menos um Pedido.
     *
     * @param id identificador do status a remover
     * @throws RegraNegocioException se o status estiver em uso ou não existir
     */
    public void remover(Long id) {
        buscarPorId(id); // valida existência
        if (pedidoRepo.existePorStatusId(id)) {
            throw new RegraNegocioException(
                "Nao e possivel remover o StatusPedido pois existem Pedidos associados a ele.");
        }
        statusRepo.remover(id);
    }
}
