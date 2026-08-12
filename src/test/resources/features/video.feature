Feature: Gerenciamento de videos

  Scenario: Upload de video com sucesso
    Given que o usuario "user@test.com" esta autenticado
    When o usuario envia um arquivo de video "video.mp4"
    Then o sistema registra o video com status "PENDING"
    And publica mensagem de processamento na fila

  Scenario: Listagem de videos do usuario
    Given que o usuario "user@test.com" possui 2 videos cadastrados
    When o usuario solicita a listagem de seus videos
    Then o sistema retorna 2 videos

  Scenario: Consulta de video por id
    Given que existe um video com id conhecido do usuario "user@test.com"
    When o usuario consulta o video por id
    Then o sistema retorna os dados do video
