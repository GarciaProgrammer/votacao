# Votação

## Como executar
****
### Pré-requisitos

- Docker e Docker Compose.

```bash
docker compose up --build
```

### Localmente, sem Docker Compose

1. Suba um Postgres com as credenciais esperadas (usuário, senha e banco `votacao`):
   ```bash
   docker run --name votacao-db -e POSTGRES_USER=votacao -e POSTGRES_PASSWORD=votacao -e POSTGRES_DB=votacao -p 5432:5432 -d postgres:16
   ```
2. Rode a aplicação (o Maven Wrapper já está no repositório, não precisa instalar Maven):
   ```bash
   ./mvnw spring-boot:run
   ```
   No Windows (PowerShell/cmd): `.\mvnw.cmd spring-boot:run`.

A aplicação usa por padrão usuário/senha `votacao`/`votacao` (sobrescrevíveis pelas variáveis de ambiente `DB_USER`/`DB_PASSWORD`) e espera o Postgres em `localhost:5432` — ajuste `spring.datasource.url` em `src/main/resources/application.properties` se o seu banco estiver em outro host/porta.

### Rodando os testes

```bash
./mvnw test
```

Os testes de integração e de concorrência usam [Testcontainers](https://testcontainers.com/) — sobem um Postgres real em container automaticamente durante a execução, então **é necessário ter o Docker rodando** para esses dois (os testes unitários dos services não precisam).

Cobertura da suíte:
- `PautaServiceTest`, `SessaoServiceTest`, `VotoServiceTest` — unitários (Mockito), cobrindo as regras de negócio de cada service isoladamente.
- `VotacaoIntegrationTest` — sobe a aplicação completa + Postgres real (Testcontainers) e exercita o fluxo via HTTP: cadastrar pauta, abrir sessão, votar, resultado, e voto duplicado.
- `VotoConcorrenciaTest` (bônus 2) — dispara 20 threads votando simultaneamente com o mesmo `associadoId` na mesma sessão, confirmando que só 1 voto é persistido (prova a constraint única do banco sob concorrência real).

## Objetivo

No cooperativismo, cada associado possui um voto e as decisões são tomadas em assembleias, por votação. Imagine que você deve criar uma solução we para gerenciar e participar dessas sessões de votação.
Essa solução deve ser executada na nuvem e promover as seguintes funcionalidades através de uma API REST / Front:

- Cadastrar uma nova pauta
- Abrir uma sessão de votação em uma pauta (a sessão de votação deve ficar aberta por
  um tempo determinado na chamada de abertura ou 1 minuto por default)
- Receber votos dos associados em pautas (os votos são apenas 'Sim'/'Não'. Cada associado
  é identificado por um id único e pode votar apenas uma vez por pauta)
- Contabilizar os votos e dar o resultado da votação na pauta

Para fins de exercício, a segurança das interfaces pode ser abstraída e qualquer chamada para as interfaces pode ser considerada como autorizada. A solução deve ser construída em java com Spring-boot e Angular/React conforme orientação, mas os frameworks e bibliotecas são de livre escolha (desde que não infrinja direitos de uso).

É importante que as pautas e os votos sejam persistidos e que não sejam perdidos com o restart da aplicação.

## Como proceder

Por favor, realize o FORK desse repositório e implemente sua solução no FORK em seu repositório GItHub, ao final, notifique da conclusão para que possamos analisar o código implementado.

Lembre de deixar todas as orientações necessárias para executar o seu código.

### Tarefas bônus

- Tarefa Bônus 1 - Integração com sistemas externos
  - Criar uma Facade/Client Fake que retorna aleátoriamente se um CPF recebido é válido ou não.
  - Caso o CPF seja inválido, a API retornará o HTTP Status 404 (Not found). Você pode usar geradores de CPF para gerar CPFs válidos
  - Caso o CPF seja válido, a API retornará se o usuário pode (ABLE_TO_VOTE) ou não pode (UNABLE_TO_VOTE) executar a operação. Essa operação retorna resultados aleatórios, portanto um mesmo CPF pode funcionar em um teste e não funcionar no outro.

```
// CPF Ok para votar
{
    "status": "ABLE_TO_VOTE
}
// CPF Nao Ok para votar - retornar 404 no client tb
{
    "status": "UNABLE_TO_VOTE
}
```

Exemplos de retorno do serviço

### Tarefa Bônus 2 - Performance

- Imagine que sua aplicação possa ser usada em cenários que existam centenas de
  milhares de votos. Ela deve se comportar de maneira performática nesses
  cenários
- Testes de performance são uma boa maneira de garantir e observar como sua
  aplicação se comporta

### Tarefa Bônus 3 - Versionamento da API

○ Como você versionaria a API da sua aplicação? Que estratégia usar?

## O que será analisado

- Simplicidade no design da solução (evitar over engineering)
- Organização do código
- Arquitetura do projeto
- Boas práticas de programação (manutenibilidade, legibilidade etc)
- Possíveis bugs
- Tratamento de erros e exceções
- Explicação breve do porquê das escolhas tomadas durante o desenvolvimento da solução
- Uso de testes automatizados e ferramentas de qualidade
- Limpeza do código
- Documentação do código e da API
- Logs da aplicação
- Mensagens e organização dos commits
- Testes
- Layout responsivo

## Decisões de arquitetura

- **Versionamento de API (bônus 3)**: por URI (`/api/v1/...`). É a estratégia mais simples de implementar e a mais visível pra quem testa (Postman/curl).
- **Concorrência e performance (bônus 2)**: a garantia de "um voto por associado por sessão" não depende só da checagem em código — existe uma constraint única composta no banco (`sessao_id`, `associado_id`), que é quem garante a integridade de fato sob concorrência (dois requests simultâneos podem passar pela checagem em Java ao mesmo tempo; só o banco impede a dupla escrita). A contagem de votos usa uma query agregada (`GROUP BY`) no banco, não uma contagem em memória — importante para o cenário de centenas de milhares de votos citado no desafio.
- **Sessão aberta/fechada**: calculado comparando o relógio atual com o horário de fechamento (`SessaoVotacao.isAberta()`), nunca um campo booleano persistido — um flag salvo ficaria desatualizado assim que o tempo passasse, exigindo um job para "fechar" a sessão.

## Documentação da API

Todos os endpoints estão sob o prefixo `/api/v1`.

### Pautas

| Método | Rota | Corpo (request) | Resposta |
|---|---|---|---|
| `POST` | `/api/v1/pautas` | `{"titulo": "string", "descricao": "string"}` | `201`/`200` com `PautaResponse` |
| `GET` | `/api/v1/pautas/{id}` | — | `200` com `PautaResponse`, ou `404` se não existir |

### Sessões

| Método | Rota | Corpo (request) | Resposta |
|---|---|---|---|
| `POST` | `/api/v1/sessoes/{pautaId}/create/{duracao}` | — (`duracao` em minutos, na própria URL) | `200` com `SessaoResponse`, `404` se a pauta não existir, `409` se a pauta já tiver sessão |

### Votos

| Método | Rota | Corpo (request) | Resposta |
|---|---|---|---|
| `POST` | `/api/v1/sessoes/{sessaoId}/votos` | `{"associadoId": "CPF", "opcao": "SIM"\|"NAO"}` | `201` sem corpo; `404` se sessão/CPF inválido, `403` se `UNABLE_TO_VOTE`, `409` se sessão encerrada ou voto duplicado |

### Resultado

| Método | Rota | Corpo (request) | Resposta |
|---|---|---|---|
| `GET` | `/api/v1/pautas/{pautaId}/resultado` | — | `200` com `ResultadoResponse` (`totalSim`, `totalNao`, `resultado`: `APROVADA`/`REJEITADA`/`EMPATE`) |

### Erros

Erros de negócio retornam um corpo simples (`string`) e o status HTTP correspondente, tratados centralmente em `GlobalExceptionHandler`:

| Exceção | Status |
|---|---|
| `PautaNotFoundException` | 404 |
| `SessaoNotFoundException` | 404 |
| `CpfInvalidoException` | 404 |
| `SessaoJaExisteException` | 409 |
| `SessaoEncerradaException` | 409 |
| `VotoDuplicadoException` | 409 |
| `AssociadoNaoAptoException` | 403 |

## Dicas

- Teste bem sua solução, evite bugs

  Observações importantes
- Não inicie o teste sem sanar todas as dúvidas
- Iremos executar a aplicação para testá-la, cuide com qualquer dependência externa e
  deixe claro caso haja instruções especiais para execução do mesmo
  Classificação da informação: Uso Interno



# desafio-votacao
