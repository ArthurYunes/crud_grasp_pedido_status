package feira.graspcrud.repository;

import feira.graspcrud.domain.StatusPedido;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositório para StatusPedido.
 *
 * <p>Padrão GRASP: Protected Variations — os serviços dependem desta abstração,
 * não da implementação concreta. Para trocar a persistência JSON por outra
 * tecnologia basta criar uma nova classe que implemente esta interface, sem
 * alterar domínio ou serviços.
 */
public interface StatusPedidoRepository {

    /**
     * Persiste um novo StatusPedido.
     *
     * @param status entidade a ser salva
     * @return entidade salva com id gerado
     */
    StatusPedido salvar(StatusPedido status);

    /**
     * Retorna todos os status cadastrados.
     *
     * @return lista (possivelmente vazia) de StatusPedido
     */
    List<StatusPedido> listarTodos();

    /**
     * Busca um StatusPedido pelo seu identificador.
     *
     * @param id identificador único
     * @return Optional com o status encontrado, ou vazio se não existir
     */
    Optional<StatusPedido> buscarPorId(Long id);

    /**
     * Busca um StatusPedido pelo nome (comparação sem distinção de maiúsculas).
     *
     * @param nome nome a pesquisar
     * @return Optional com o status encontrado, ou vazio se não existir
     */
    Optional<StatusPedido> buscarPorNome(String nome);

    /**
     * Remove o StatusPedido com o id informado.
     *
     * @param id identificador do status a remover
     */
    void remover(Long id);
}
