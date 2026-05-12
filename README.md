# crud-grasp-pedido-status

Sistema de gestão de **Pedidos** de uma feira livre, implementado em Java puro com persistência em JSON e menu textual no terminal.

**Tema:** Pedido / StatusPedido — Grupo G

---

## Integrantes do Grupo

| Nome | Matrícula |
|------|-----------|
| _Arthur Yunes_ | _2512843_ |

---

## Pré-requisitos

- JDK 11 ou superior (testado com OpenJDK 21)

---

## Como compilar e executar

### Linux / macOS

```bash
chmod +x compile.sh run.sh
./compile.sh
./run.sh
```

### Windows (Prompt de Comando)

```cmd
mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out -Dfile.encoding=UTF-8 feira.graspcrud.Main
```

### Manual (qualquer sistema)

```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out feira.graspcrud.Main
```

---

## Estrutura do projeto

```
crud-grasp-pedido-status/
├── src/feira/graspcrud/
│   ├── Main.java                              # Bootstrap (Creator)
│   ├── controller/
│   │   └── PedidoController.java             # Controller GRASP
│   ├── domain/
│   │   ├── Pedido.java                        # Information Expert
│   │   └── StatusPedido.java                 # Information Expert
│   ├── dto/
│   │   ├── PedidoRequest.java                # DTO de entrada
│   │   └── StatusPedidoRequest.java          # DTO de entrada
│   ├── exception/
│   │   └── RegraNegocioException.java        # Pure Fabrication
│   ├── repository/
│   │   ├── PedidoRepository.java             # Interface (Protected Variations)
│   │   └── StatusPedidoRepository.java       # Interface (Protected Variations)
│   ├── repository/json/
│   │   ├── PedidoRepositoryJson.java         # Pure Fabrication + Indirection
│   │   └── StatusPedidoRepositoryJson.java   # Pure Fabrication + Indirection
│   ├── service/
│   │   ├── PedidoService.java                # Low Coupling + High Cohesion
│   │   └── StatusPedidoService.java          # Low Coupling + High Cohesion
│   └── util/
│       └── JsonMini.java                     # Pure Fabrication
├── data/
│   ├── pedidos.json                          # Gerado automaticamente
│   └── status-pedido.json                   # Gerado automaticamente
├── compile.sh
├── run.sh
└── README.md
```

---

## Padrões GRASP aplicados

| Padrão GRASP | Classe(s) | Como foi aplicado |
|---|---|---|
| **Information Expert** | `Pedido`, `StatusPedido` | Validações de nome (mín. 3 chars), obrigatoriedade de status, e toda a lógica de transição de status (`avancarStatus`) ficam nas próprias entidades |
| **Creator** | `Main`, `PedidoService` | `Main` instancia e conecta todos os objetos; `PedidoService` recebe o `PedidoRequest` e instancia o `Pedido` com os dados necessários |
| **Controller** | `PedidoController` | Único ponto de entrada do terminal; coleta dados do usuário e delega para os serviços, sem conter nenhuma regra de negócio |
| **Low Coupling** | `PedidoService`, `StatusPedidoService` | Os serviços dependem das interfaces `PedidoRepository` e `StatusPedidoRepository`, sem importar as classes `*RepositoryJson` |
| **High Cohesion** | Todas as classes | Cada classe tem responsabilidade única e bem definida; nenhum método faz mais de uma coisa |
| **Pure Fabrication** | `JsonMini`, `*RepositoryJson`, `RegraNegocioException` | Classes criadas para necessidades técnicas sem correspondência no domínio real da feira livre |
| **Indirection** | `*RepositoryJson` | As implementações ficam atrás das interfaces, mediando o acesso entre serviços e sistema de arquivos |
| **Protected Variations** | `PedidoRepository`, `StatusPedidoRepository` | As interfaces protegem o domínio e os serviços de variações na implementação de persistência; trocar JSON por outro mecanismo exige apenas uma nova classe |

---

## Regras de negócio implementadas

- `Pedido.nome` obrigatório, mínimo 3 caracteres
- `Pedido.statusPedido` obrigatório no cadastro
- `StatusPedido.nome` obrigatório, mínimo 3 caracteres e **único** no sistema
- **Transição de status** segue a ordem: `ABERTO → EM_PREPARO → ENTREGUE`
  - Não é permitido retroceder
  - Não é permitido pular etapas
- Não é permitido remover um `StatusPedido` associado a algum `Pedido`
- `dataCriacao` do Pedido é preenchida automaticamente no cadastro

---

## Persistência

Os dados são salvos automaticamente na pasta `data/` após cada operação de escrita:

- `data/status-pedido.json` — lista de StatusPedido
- `data/pedidos.json` — lista de Pedidos (com StatusPedido embutido)

Ao reiniciar, os dados são recarregados automaticamente do JSON.

---

## Menu do sistema

```
=======================================================
  SISTEMA DE PEDIDOS - FEIRA LIVRE
=======================================================
  --- StatusPedido ---
  1. Cadastrar StatusPedido
  2. Listar StatusPedido
  3. Remover StatusPedido
  --- Pedido ---
  4. Cadastrar Pedido
  5. Listar Pedidos
  6. Buscar Pedido por ID
  7. Atualizar Pedido
  8. Avancar Status do Pedido
  9. Remover Pedido
  ---
  0. Sair
=======================================================
```
