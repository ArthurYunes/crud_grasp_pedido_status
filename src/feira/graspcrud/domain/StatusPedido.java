package feira.graspcrud.domain;

import feira.graspcrud.exception.RegraNegocioException;

/**
 * Entidade de domínio que representa o status de um pedido na feira livre.
 *
 * <p>Padrão GRASP: Information Expert — esta classe conhece todos os dados
 * necessários para validar suas próprias regras de consistência, como a
 * obrigatoriedade e tamanho mínimo do nome.
 *
 * <p>Os status predefinidos formam a sequência obrigatória de transição:
 * ABERTO → EM_PREPARO → ENTREGUE.
 */
public class StatusPedido {

    private Long id;
    private String nome;
    private String descricao;

    /** Construtor padrão exigido para desserialização JSON. */
    public StatusPedido() {}

    /**
     * Cria um StatusPedido com os dados fornecidos, aplicando validações de domínio.
     *
     * @param id       identificador único
     * @param nome     nome do status (obrigatório, mínimo 3 caracteres)
     * @param descricao descrição opcional do status
     * @throws RegraNegocioException se o nome for inválido
     */
    @SuppressWarnings("this-escape") // seguro: StatusPedido nao e subclassificado
    public StatusPedido(Long id, String nome, String descricao) {
        this.id = id;
        setNome(nome);
        this.descricao = descricao;
    }

    // ── Information Expert: validação de nome ──────────────────────────────

    /**
     * Define o nome do status, aplicando a regra de negócio de obrigatoriedade
     * e tamanho mínimo de 3 caracteres.
     *
     * @param nome nome a ser atribuído
     * @throws RegraNegocioException se nulo, em branco ou com menos de 3 caracteres
     */
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do StatusPedido e obrigatorio.");
        }
        if (nome.trim().length() < 3) {
            throw new RegraNegocioException("O nome do StatusPedido deve ter ao menos 3 caracteres.");
        }
        this.nome = nome.trim();
    }

    /**
     * Retorna a ordem numérica deste status na sequência obrigatória
     * ABERTO(0) → EM_PREPARO(1) → ENTREGUE(2).
     *
     * <p>Padrão GRASP: Information Expert — a própria entidade conhece sua posição
     * na sequência, evitando lógica espalhada em outros lugares.
     *
     * @return índice de ordem, ou -1 se o nome não corresponder a um status padrão
     */
    public int ordemTransicao() {
        return switch (nome.toUpperCase()) {
            case "ABERTO"     -> 0;
            case "EM_PREPARO" -> 1;
            case "ENTREGUE"   -> 2;
            default           -> -1;
        };
    }

    // ── Getters ────────────────────────────────────────────────────────────

    /** @return identificador único do status */
    public Long getId() { return id; }

    /** @return nome do status */
    public String getNome() { return nome; }

    /** @return descrição do status */
    public String getDescricao() { return descricao; }

    // ── Setters auxiliares (para desserialização) ──────────────────────────

    /** @param id identificador a ser atribuído */
    public void setId(Long id) { this.id = id; }

    /** @param descricao descrição a ser atribuída */
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s", id, nome, descricao != null ? descricao : "");
    }
}
