package feira.graspcrud.repository;

import feira.graspcrud.domain.Pedido;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositório para Pedido.
 *
 * <p>Padrão GRASP: Protected Variations — os serviços dependem desta abstração,
 * não da implementação concreta. Para trocar a persistência JSON por outra
 * tecnologia basta criar uma nova classe que implemente esta interface, sem
 * alterar domínio ou serviços.
 */
public interface PedidoRepository {

    /**
     * Persiste um novo Pedido.
     *
     * @param pedido entidade a ser salva
     * @return entidade salva com id gerado
     */
    Pedido salvar(Pedido pedido);

    /**
     * Retorna todos os pedidos cadastrados.
     *
     * @return lista (possivelmente vazia) de Pedido
     */
    List<Pedido> listarTodos();

    /**
     * Busca um Pedido pelo seu identificador.
     *
     * @param id identificador único
     * @return Optional com o pedido encontrado, ou vazio se não existir
     */
    Optional<Pedido> buscarPorId(Long id);

    /**
     * Atualiza um Pedido já persistido.
     *
     * @param pedido entidade com dados atualizados
     * @return entidade atualizada
     */
    Pedido atualizar(Pedido pedido);

    /**
     * Remove o Pedido com o id informado.
     *
     * @param id identificador do pedido a remover
     */
    void remover(Long id);

    /**
     * Verifica se algum Pedido está associado ao StatusPedido informado.
     *
     * @param statusId id do StatusPedido a verificar
     * @return true se houver ao menos um Pedido com esse status
     */
    boolean existePorStatusId(Long statusId);
}
