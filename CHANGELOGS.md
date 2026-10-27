# Changelog

Este arquivo registra as mudanças do ArmaDuels a partir da migração para Paper 26.2.
As versões seguem o [Semantic Versioning 2.0.0](https://semver.org/lang/pt-BR/):
MAJOR para mudanças incompatíveis, MINOR para funcionalidades compatíveis e PATCH
para correções compatíveis. As seções abaixo descrevem as mudanças; SemVer define
o versionamento, não um formato obrigatório de changelog.

## [4.0.0] — Não lançado

### Mudanças incompatíveis

- Exigir Paper 26.2 e Java 25. Esta versão não suporta os ambientes Minecraft/Java anteriores.
- Compilar os cinco módulos, incluindo a API pública, com bytecode Java 25 e Paper 26.2.
  Consumidores da API precisam de Java 25 e devem validar sua compatibilidade com Paper 26.2.
- Declarar `api-version: '26.2'` no manifesto do plugin.
- Incrementar a versão de `3.5.5-SNAPSHOT` para `4.0.0` devido à mudança incompatível
  dos requisitos de plataforma.

### Alterado

- Substituir as dependências Paper 1.21.11 e Spigot 1.14.4 por Paper `26.2.build.+`.
- Atualizar Gradle para 9.8.2, Shadow para 9.6.1 e Lombok para 1.18.42.
- Configurar toolchain, bytecode e integração contínua para Java 25.
- Atualizar CombatLogX API de `11.6-SNAPSHOT` para `11.7-SNAPSHOT`, disponível
  no repositório oficial, preservando a implementação do hook.
- Atualizar os requisitos e as instruções de build no README.

### Corrigido

- Reconhecer o esquema de versões anuais por meio de `Bukkit.getMinecraftVersion()`.
  A versão `26.2` deixava de ser identificada corretamente e era interpretada como `2`,
  ativando caminhos legados de compatibilidade para itens, kits e reflexão.

### Adicionado

- Testes de regressão para versões tradicionais `1.x` e anuais `26.x`/`27.x`.
- Este histórico de mudanças.

### Migração

- Atualizar o servidor para Paper 26.2 e a JVM para Java 25 antes de instalar o plugin.
- Usar o artefato `out/Duels-4.0.0.jar` produzido pelo build.
- Homologar kits, menus paginados, filas, duelos e integrações opcionais antes da implantação.
- Esta migração não altera comandos, permissões, configurações ou formatos de dados persistidos.

[4.0.0]: https://github.com/ArmaRealms/ArmaDuels/pull/33
