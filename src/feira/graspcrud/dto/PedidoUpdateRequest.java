package feira.graspcrud.dto;

/**
 * DTO de entrada para atualização de dados de um Pedido existente.
 *
 * <p>Padrão GRASP: Low Coupling — isola os dados mutáveis (nome e descrição)
 * das entidades de domínio. A transição de status é uma operação separada,
 * tratada por {@link PedidoRequest} em conjunto com {@code avancarStatus}.
 *
 * <p>O statusPedidoId é intencionalmente ausente neste DTO: status só avança
 * via operação dedicada {@code avancarStatus}, nunca via atualização de dados.
 */
public class PedidoUpdateRequest {

    private String nome;
    private String descricao;

    /**
     * Cria o request de atualização com os novos valores.
     *
     * @param nome      novo nome do pedido (será validado no domínio)
     * @param descricao nova descrição opcional
     */
    public PedidoUpdateRequest(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    /** @return novo nome do pedido */
    public String getNome() { return nome; }

    /** @return nova descrição do pedido */
    public String getDescricao() { return descricao; }
}
