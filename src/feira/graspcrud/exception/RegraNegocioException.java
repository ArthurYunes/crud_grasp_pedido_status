package feira.graspcrud.exception;

/**
 * Exceção de domínio lançada quando uma regra de negócio é violada.
 *
 * <p>Padrão GRASP: Pure Fabrication — classe fabricada exclusivamente para
 * transportar erros de domínio sem carregar dependências de infraestrutura.
 * Capturada no {@link feira.graspcrud.controller.PedidoController} para exibir
 * mensagem amigável no terminal.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a mensagem descritiva da violação.
     *
     * @param mensagem descrição clara do motivo pelo qual a operação foi rejeitada
     */
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
