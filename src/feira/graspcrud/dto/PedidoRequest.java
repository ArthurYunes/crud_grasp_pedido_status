package feira.graspcrud.dto;

/**
 * DTO de entrada para criação e atualização de Pedido.
 *
 * <p>Padrão GRASP: Low Coupling — isola os dados brutos de entrada (vindos do
 * menu/teclado) das entidades de domínio, evitando que o domínio conheça
 * detalhes da camada de apresentação.
 */
public class PedidoRequest {

    private String nome;
    private String descricao;
    private Long statusPedidoId;

    /**
     * Cria o request com os campos informados pelo usuário.
     *
     * @param nome          nome do pedido (será validado no domínio)
     * @param descricao     descrição opcional
     * @param statusPedidoId id do StatusPedido ao qual o pedido será associado
     */
    public PedidoRequest(String nome, String descricao, Long statusPedidoId) {
        this.nome = nome;
        this.descricao = descricao;
        this.statusPedidoId = statusPedidoId;
    }

    /** @return nome do pedido */
    public String getNome() { return nome; }

    /** @return descrição do pedido */
    public String getDescricao() { return descricao; }

    /** @return id do StatusPedido associado */
    public Long getStatusPedidoId() { return statusPedidoId; }
}
