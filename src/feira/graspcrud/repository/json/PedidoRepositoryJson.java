package feira.graspcrud.repository.json;

import feira.graspcrud.domain.Pedido;
import feira.graspcrud.domain.StatusPedido;
import feira.graspcrud.repository.PedidoRepository;
import feira.graspcrud.repository.StatusPedidoRepository;
import feira.graspcrud.util.JsonMini;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementação JSON do repositório de Pedido.
 *
 * <p>Padrão GRASP: Pure Fabrication — classe fabricada para isolar detalhes de
 * persistência do domínio. Não existe analogia no mundo real da feira livre.
 *
 * <p>Padrão GRASP: Indirection — age como intermediária entre o serviço e o
 * sistema de arquivos. O StatusPedido é salvo de forma embutida (inline) no JSON
 * do pedido para permitir a recarga completa sem dependência de ordem de leitura.
 *
 * <p>Os dados são persistidos em {@code data/pedidos.json}.
 */
public class PedidoRepositoryJson implements PedidoRepository {

    private static final Path ARQUIVO = Path.of("data", "pedidos.json");

    private final List<Pedido> cache = new ArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);
    private final StatusPedidoRepository statusRepo;

    /**
     * Cria o repositório recebendo o repositório de StatusPedido para
     * rehidratar a referência ao status durante a carga do arquivo JSON.
     *
     * @param statusRepo repositório de StatusPedido (injetado pelo Main)
     */
    public PedidoRepositoryJson(StatusPedidoRepository statusRepo) {
        this.statusRepo = statusRepo;
        carregarDoArquivo();
    }

    // ── CRUD ───────────────────────────────────────────────────────────────

    /**
     * Persiste um novo Pedido gerando id automaticamente.
     *
     * @param pedido entidade sem id definido
     * @return entidade com id gerado
     */
    @Override
    public Pedido salvar(Pedido pedido) {
        pedido.setId(proximoId.getAndIncrement());
        cache.add(pedido);
        persistir();
        return pedido;
    }

    /**
     * Retorna todos os pedidos cadastrados.
     *
     * @return lista não modificável
     */
    @Override
    public List<Pedido> listarTodos() {
        return Collections.unmodifiableList(cache);
    }

    /**
     * Busca um Pedido pelo id.
     *
     * @param id identificador único
     * @return Optional com o pedido, ou vazio se não encontrado
     */
    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return cache.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    /**
     * Substitui o Pedido existente pelo objeto atualizado e persiste.
     *
     * @param pedido pedido com dados atualizados (mesmo id)
     * @return pedido atualizado
     */
    @Override
    public Pedido atualizar(Pedido pedido) {
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId().equals(pedido.getId())) {
                cache.set(i, pedido);
                break;
            }
        }
        persistir();
        return pedido;
    }

    /**
     * Remove o Pedido com o id informado e atualiza o arquivo JSON.
     *
     * @param id identificador do pedido a remover
     */
    @Override
    public void remover(Long id) {
        cache.removeIf(p -> p.getId().equals(id));
        persistir();
    }

    /**
     * Verifica se algum Pedido referencia o StatusPedido informado.
     *
     * @param statusId id do StatusPedido a verificar
     * @return true se houver ao menos um Pedido com esse status
     */
    @Override
    public boolean existePorStatusId(Long statusId) {
        return cache.stream()
                .anyMatch(p -> p.getStatusPedido() != null
                        && p.getStatusPedido().getId().equals(statusId));
    }

    // ── Persistência ───────────────────────────────────────────────────────

    /**
     * Serializa o cache em memória para o arquivo JSON.
     */
    private void persistir() {
        List<Map<String, Object>> lista = cache.stream()
                .map(this::paraMap)
                .collect(Collectors.toList());
        JsonMini.escrever(ARQUIVO, lista);
    }

    /**
     * Converte um Pedido em mapa para serialização JSON,
     * incluindo o StatusPedido embutido.
     *
     * @param p pedido a converter
     * @return mapa de campos e valores
     */
    private Map<String, Object> paraMap(Pedido p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("nome", p.getNome());
        m.put("descricao", p.getDescricao());
        m.put("ativo", p.getAtivo());
        m.put("dataCriacao", p.getDataCriacao());

        if (p.getStatusPedido() != null) {
            Map<String, Object> sp = new LinkedHashMap<>();
            sp.put("id", p.getStatusPedido().getId());
            sp.put("nome", p.getStatusPedido().getNome());
            sp.put("descricao", p.getStatusPedido().getDescricao());
            m.put("statusPedido", sp);
        } else {
            m.put("statusPedido", null);
        }
        return m;
    }

    /**
     * Carrega os dados do arquivo JSON para o cache em memória,
     * recompondo as referências de StatusPedido via repositório.
     *
     * <p>Registros corrompidos (nome nulo ou muito curto) são ignorados
     * com aviso no console, evitando que o sistema falhe ao iniciar.
     */
    @SuppressWarnings("unchecked")
    private void carregarDoArquivo() {
        List<Map<String, Object>> lista = JsonMini.ler(ARQUIVO);
        long maxId = 0;
        for (Map<String, Object> m : lista) {
            try {
                Pedido p = new Pedido();
                p.setId(toLong(m.get("id")));
                p.setNome((String) m.get("nome"));       // pode lançar RegraNegocioException
                p.setDescricao((String) m.get("descricao"));
                p.setAtivo((Boolean) m.get("ativo"));
                p.setDataCriacao((String) m.get("dataCriacao"));

                // Rehidrata o StatusPedido pelo id armazenado no repositório de status
                Object spObj = m.get("statusPedido");
                if (spObj instanceof Map) {
                    Map<String, Object> spMap = (Map<String, Object>) spObj;
                    Long spId = toLong(spMap.get("id"));
                    StatusPedido sp = statusRepo.buscarPorId(spId).orElse(null);
                    if (sp == null) {
                        // Fallback: reconstrói snapshot do JSON caso o status tenha sido removido
                        String nomeSp = (String) spMap.get("nome");
                        String descSp = (String) spMap.get("descricao");
                        sp = (nomeSp != null && !nomeSp.isBlank())
                            ? new StatusPedido(spId, nomeSp, descSp)
                            : new StatusPedido(spId, "REMOVIDO", "Status removido do sistema");
                    }
                    p.setStatusPedido(sp);
                }
                cache.add(p);
                if (p.getId() > maxId) maxId = p.getId();
            } catch (Exception e) {
                System.err.println("[AVISO] Pedido ignorado por dados invalidos no JSON: " + e.getMessage());
            }
        }
        proximoId.set(maxId + 1);
    }

    /**
     * Converte um valor numérico (Number) para Long com segurança.
     *
     * @param obj objeto a converter
     * @return valor Long correspondente
     */
    private Long toLong(Object obj) {
        if (obj instanceof Long)   return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return 0L;
    }
}
