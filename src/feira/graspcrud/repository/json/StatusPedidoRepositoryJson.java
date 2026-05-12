package feira.graspcrud.repository.json;

import feira.graspcrud.domain.StatusPedido;
import feira.graspcrud.repository.StatusPedidoRepository;
import feira.graspcrud.util.JsonMini;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementação JSON do repositório de StatusPedido.
 *
 * <p>Padrão GRASP: Pure Fabrication — classe fabricada para isolar detalhes de
 * persistência do domínio. Não existe analogia no mundo real da feira livre.
 *
 * <p>Padrão GRASP: Indirection — age como intermediária entre o serviço e o
 * sistema de arquivos, de modo que o serviço nunca sabe como os dados são
 * armazenados fisicamente.
 *
 * <p>Os dados são persistidos em {@code data/status-pedido.json}.
 */
public class StatusPedidoRepositoryJson implements StatusPedidoRepository {

    private static final Path ARQUIVO = Path.of("data", "status-pedido.json");

    private final List<StatusPedido> cache = new ArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    /**
     * Cria o repositório carregando os dados do arquivo JSON (se existir).
     */
    public StatusPedidoRepositoryJson() {
        carregarDoArquivo();
    }

    // ── CRUD ───────────────────────────────────────────────────────────────

    /**
     * Persiste um novo StatusPedido gerando id automaticamente.
     *
     * @param status entidade sem id definido
     * @return entidade com id gerado
     */
    @Override
    public StatusPedido salvar(StatusPedido status) {
        status.setId(proximoId.getAndIncrement());
        cache.add(status);
        persistir();
        return status;
    }

    /**
     * Retorna todos os status cadastrados.
     *
     * @return cópia da lista interna
     */
    @Override
    public List<StatusPedido> listarTodos() {
        return Collections.unmodifiableList(cache);
    }

    /**
     * Busca um StatusPedido pelo id.
     *
     * @param id identificador único
     * @return Optional com o status, ou vazio se não encontrado
     */
    @Override
    public Optional<StatusPedido> buscarPorId(Long id) {
        return cache.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    /**
     * Busca um StatusPedido pelo nome (sem distinção de maiúsculas).
     *
     * @param nome nome a pesquisar
     * @return Optional com o status, ou vazio se não encontrado
     */
    @Override
    public Optional<StatusPedido> buscarPorNome(String nome) {
        return cache.stream()
                .filter(s -> s.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    /**
     * Remove o StatusPedido com o id informado e atualiza o arquivo JSON.
     *
     * @param id identificador do status a remover
     */
    @Override
    public void remover(Long id) {
        cache.removeIf(s -> s.getId().equals(id));
        persistir();
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
     * Converte um StatusPedido em mapa para serialização JSON.
     *
     * @param s status a converter
     * @return mapa de campos e valores
     */
    private Map<String, Object> paraMap(StatusPedido s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("nome", s.getNome());
        m.put("descricao", s.getDescricao());
        return m;
    }

    /**
     * Carrega os dados do arquivo JSON para o cache em memória.
     * Reajusta o gerador de id para evitar colisões.
     *
     * <p>Registros corrompidos (nome nulo ou muito curto) são ignorados
     * com aviso no console, evitando que o sistema falhe ao iniciar.
     */
    private void carregarDoArquivo() {
        List<Map<String, Object>> lista = JsonMini.ler(ARQUIVO);
        long maxId = 0;
        for (Map<String, Object> m : lista) {
            try {
                StatusPedido s = new StatusPedido();
                s.setId(toLong(m.get("id")));
                s.setNome((String) m.get("nome"));   // pode lançar RegraNegocioException
                s.setDescricao((String) m.get("descricao"));
                cache.add(s);
                if (s.getId() > maxId) maxId = s.getId();
            } catch (Exception e) {
                System.err.println("[AVISO] StatusPedido ignorado por dados invalidos no JSON: " + e.getMessage());
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
