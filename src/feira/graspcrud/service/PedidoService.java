package feira.graspcrud.service;

import feira.graspcrud.domain.Pedido;
import feira.graspcrud.domain.StatusPedido;
import feira.graspcrud.dto.PedidoRequest;
import feira.graspcrud.dto.PedidoUpdateRequest;
import feira.graspcrud.exception.RegraNegocioException;
import feira.graspcrud.repository.PedidoRepository;
import feira.graspcrud.repository.StatusPedidoRepository;

import java.util.List;

/**
 * Serviço de aplicação responsável pelos casos de uso de Pedido.
 *
 * <p>Padrão GRASP: Low Coupling — depende de {@link PedidoRepository} e
 * {@link StatusPedidoRepository} por suas interfaces, sem conhecer a implementação
 * concreta de persistência.
 *
 * <p>Padrão GRASP: High Cohesion — responsável exclusivamente pelos casos de uso
 * de Pedido. Não contém lógica de apresentação nem acessa diretamente arquivos.
 *
 * <p>Padrão GRASP: Creator — o serviço recebe o {@link PedidoRequest} com todos
 * os dados necessários e é responsável por instanciar o {@link Pedido}.
 */
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final StatusPedidoRepository statusRepo;

    /**
     * Cria o serviço com as dependências fornecidas pelo Main (Creator).
     *
     * @param pedidoRepo repositório de Pedido
     * @param statusRepo repositório de StatusPedido
     */
    public PedidoService(PedidoRepository pedidoRepo, StatusPedidoRepository statusRepo) {
        this.pedidoRepo = pedidoRepo;
        this.statusRepo = statusRepo;
    }

    /**
     * Cadastra um novo Pedido associado ao StatusPedido informado.
     *
     * <p>Regra de negócio: o StatusPedido deve existir. O nome é obrigatório
     * e validado pela própria entidade {@link Pedido} (Information Expert).
     *
     * @param request dados de entrada com nome, descrição e id do status
     * @return Pedido persistido com id e data de criação gerados
     * @throws RegraNegocioException se o StatusPedido não for encontrado
     */
    public Pedido criar(PedidoRequest request) {
        StatusPedido status = statusRepo.buscarPorId(request.getStatusPedidoId())
                .orElseThrow(() -> new RegraNegocioException(
                        "StatusPedido com id " + request.getStatusPedidoId() + " nao encontrado."));

        Pedido novo = new Pedido(null, request.getNome(), request.getDescricao(), status);
        return pedidoRepo.salvar(novo);
    }

    /**
     * Retorna todos os pedidos cadastrados.
     *
     * @return lista de Pedido
     */
    public List<Pedido> listarTodos() {
        return pedidoRepo.listarTodos();
    }

    /**
     * Busca um Pedido pelo id, lançando exceção se não encontrado.
     *
     * @param id identificador único
     * @return Pedido encontrado
     * @throws RegraNegocioException se não existir pedido com o id informado
     */
    public Pedido buscarPorId(Long id) {
        return pedidoRepo.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Pedido com id " + id + " nao encontrado."));
    }

    /**
     * Atualiza o nome e a descrição de um Pedido existente.
     *
     * <p>O StatusPedido nao é alterado por este método — a transicao de status
     * é feita exclusivamente por {@link #avancarStatus(Long, Long)}.
     *
     * @param id      identificador do pedido a atualizar
     * @param request dados com os novos valores de nome e descrição
     * @return Pedido atualizado
     * @throws RegraNegocioException se o pedido nao for encontrado ou o nome for invalido
     */
    public Pedido atualizar(Long id, PedidoUpdateRequest request) {
        Pedido pedido = buscarPorId(id);
        pedido.setNome(request.getNome());         // validado pelo Information Expert
        pedido.setDescricao(request.getDescricao());
        return pedidoRepo.atualizar(pedido);
    }

    /**
     * Avança o status de um Pedido para o próximo na sequência obrigatória:
     * ABERTO → EM_PREPARO → ENTREGUE.
     *
     * <p>Regra de negócio: a transição segue a ordem da sequência e não permite
     * retrocesso. A validação da ordem é delegada ao próprio {@link Pedido}
     * (Information Expert), que conhece sua progressão válida.
     *
     * @param pedidoId      id do pedido a avançar
     * @param novoStatusId  id do StatusPedido destino
     * @return Pedido com o status atualizado
     * @throws RegraNegocioException se o pedido ou o status não forem encontrados,
     *                               ou se a transição for inválida
     */
    public Pedido avancarStatus(Long pedidoId, Long novoStatusId) {
        Pedido pedido = buscarPorId(pedidoId);
        StatusPedido novoStatus = statusRepo.buscarPorId(novoStatusId)
                .orElseThrow(() -> new RegraNegocioException(
                        "StatusPedido com id " + novoStatusId + " nao encontrado."));

        pedido.avancarStatus(novoStatus); // Information Expert valida a transição
        return pedidoRepo.atualizar(pedido);
    }

    /**
     * Remove um Pedido pelo id.
     *
     * @param id identificador do pedido a remover
     * @throws RegraNegocioException se o pedido não for encontrado
     */
    public void remover(Long id) {
        buscarPorId(id); // valida existência antes de remover
        pedidoRepo.remover(id);
    }
}
