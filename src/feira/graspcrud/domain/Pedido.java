package feira.graspcrud.domain;

import feira.graspcrud.exception.RegraNegocioException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidade de domínio que representa um pedido na feira livre.
 *
 * <p>Padrão GRASP: Information Expert — esta classe conhece e aplica todas as
 * regras de negócio sobre seus próprios dados:
 * <ul>
 *   <li>Nome obrigatório com mínimo de 3 caracteres.</li>
 *   <li>StatusPedido obrigatório.</li>
 *   <li>Data de criação preenchida automaticamente no momento da construção.</li>
 *   <li>Transição de status somente na ordem ABERTO → EM_PREPARO → ENTREGUE,
 *       sem retrocesso permitido.</li>
 * </ul>
 */
public class Pedido {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private StatusPedido statusPedido;
    private String dataCriacao; // armazenado como String para facilitar JSON puro

    /** Construtor padrão exigido para desserialização JSON. */
    public Pedido() {}

    /**
     * Cria um novo pedido preenchendo automaticamente a data de criação
     * e aplicando todas as validações de domínio.
     *
     * @param id          identificador único
     * @param nome        nome do pedido (obrigatório, mínimo 3 caracteres)
     * @param descricao   descrição complementar (opcional)
     * @param statusPedido status inicial obrigatório
     * @throws RegraNegocioException se nome ou statusPedido forem inválidos
     */
    @SuppressWarnings("this-escape") // seguro: Pedido nao e subclassificado
    public Pedido(Long id, String nome, String descricao, StatusPedido statusPedido) {
        this.id = id;
        setNome(nome);
        this.descricao = descricao;
        setStatusPedido(statusPedido);
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now().format(FMT);
    }

    // ── Information Expert: validações e regras de estado ──────────────────

    /**
     * Define o nome do pedido aplicando a regra de mínimo 3 caracteres.
     *
     * @param nome nome a ser atribuído
     * @throws RegraNegocioException se nulo, em branco ou com menos de 3 caracteres
     */
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do Pedido e obrigatorio.");
        }
        if (nome.trim().length() < 3) {
            throw new RegraNegocioException("O nome do Pedido deve ter ao menos 3 caracteres.");
        }
        this.nome = nome.trim();
    }

    /**
     * Define o StatusPedido, garantindo que não seja nulo.
     *
     * @param statusPedido status a ser atribuído
     * @throws RegraNegocioException se nulo
     */
    public void setStatusPedido(StatusPedido statusPedido) {
        if (statusPedido == null) {
            throw new RegraNegocioException("O StatusPedido e obrigatorio para o Pedido.");
        }
        this.statusPedido = statusPedido;
    }

    /**
     * Avança o pedido para o próximo status na sequência obrigatória:
     * ABERTO → EM_PREPARO → ENTREGUE.
     *
     * <p>Padrão GRASP: Information Expert — somente o próprio Pedido sabe qual é
     * sua progressão válida, pois detém tanto o status atual quanto o candidato.
     *
     * <p>Regra de negócio: não é permitido voltar a um status anterior nem
     * pular etapas da sequência.
     *
     * @param novoStatus status para o qual se deseja avançar
     * @throws RegraNegocioException se a transição for inválida (retrocesso ou salto)
     */
    public void avancarStatus(StatusPedido novoStatus) {
        if (novoStatus == null) {
            throw new RegraNegocioException("O novo status nao pode ser nulo.");
        }
        if (this.statusPedido == null) {
            // Pedido sem status (carregado de JSON corrompido): aceita qualquer status
            this.statusPedido = novoStatus;
            return;
        }
        int ordemAtual = this.statusPedido.ordemTransicao();
        int ordemNova  = novoStatus.ordemTransicao();

        // Ambos sao status padrao da sequencia: exige progressao estrita sem retrocesso nem salto
        if (ordemAtual != -1 && ordemNova != -1) {
            if (ordemNova <= ordemAtual) {
                throw new RegraNegocioException(
                    "Transicao invalida: nao e permitido voltar de '"
                    + this.statusPedido.getNome() + "' para '" + novoStatus.getNome() + "'.");
            }
            if (ordemNova != ordemAtual + 1) {
                throw new RegraNegocioException(
                    "Transicao invalida: e necessario passar por todos os status intermediarios. "
                    + "Status atual: " + this.statusPedido.getNome() + ".");
            }
        }
        // Status customizados (ordemTransicao == -1): aceita sem restricao de ordem
        this.statusPedido = novoStatus;
    }

    // ── Getters ────────────────────────────────────────────────────────────

    /** @return identificador único do pedido */
    public Long getId() { return id; }

    /** @return nome do pedido */
    public String getNome() { return nome; }

    /** @return descrição complementar */
    public String getDescricao() { return descricao; }

    /** @return true se o pedido está ativo */
    public Boolean getAtivo() { return ativo; }

    /** @return status atual do pedido */
    public StatusPedido getStatusPedido() { return statusPedido; }

    /** @return data de criação formatada (dd/MM/yyyy HH:mm) */
    public String getDataCriacao() { return dataCriacao; }

    // ── Setters auxiliares (para desserialização e atualização) ───────────

    /** @param id identificador a ser atribuído */
    public void setId(Long id) { this.id = id; }

    /** @param descricao descrição a ser atribuída */
    public void setDescricao(String descricao) { this.descricao = descricao; }

    /** @param ativo estado ativo/inativo */
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    /** @param dataCriacao data de criação (usada na recarga do JSON) */
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    @Override
    public String toString() {
        return String.format("[%d] %s | Status: %s | Criado: %s | Ativo: %s%s",
            id, nome,
            statusPedido != null ? statusPedido.getNome() : "N/A",
            dataCriacao != null ? dataCriacao : "N/A",
            Boolean.TRUE.equals(ativo) ? "Sim" : "Nao",
            descricao != null && !descricao.isBlank() ? " | Desc: " + descricao : "");
    }
}
