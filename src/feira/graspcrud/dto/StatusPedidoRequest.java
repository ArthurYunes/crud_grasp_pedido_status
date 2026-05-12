package feira.graspcrud.dto;

/**
 * DTO de entrada para criação e atualização de StatusPedido.
 *
 * <p>Padrão GRASP: Low Coupling — isola os dados brutos de entrada (vindos do
 * menu/teclado) das entidades de domínio, evitando que o domínio conheça
 * detalhes da camada de apresentação.
 */
public class StatusPedidoRequest {

    private String nome;
    private String descricao;

    /**
     * Cria o request com os campos informados pelo usuário.
     *
     * @param nome      nome do status (será validado no domínio)
     * @param descricao descrição opcional
     */
    public StatusPedidoRequest(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    /** @return nome do status */
    public String getNome() { return nome; }

    /** @return descrição do status */
    public String getDescricao() { return descricao; }
}
