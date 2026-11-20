# PROJETO 180 DIAS

Projeto Android nativo em Kotlin, Jetpack Compose e Material Design 3. Esta entrega parte do ZIP completo da Etapa 3 e adiciona a Etapa 4: calendário e checklists diários.

## Tecnologias
- Kotlin e AndroidX
- Jetpack Compose + Material Design 3
- Room (SQLite) com migração 2 → 3
- DataStore
- KAPT para Room compiler
- Java 17, minSdk 26, compileSdk/targetSdk 36

## Etapa 4 implementada
- Calendário mensal com navegação entre meses e seleção de dias.
- Navegação semanal por dias e visualização diária.
- Período de jornada destacado: 20/11/2026 a 18/05/2027.
- Indicadores no calendário para dias com tarefas/eventos e tarefas concluídas.
- Checklists diários persistidos no Room, com categorias iniciais: Saúde, Estudos, Academia, Família, Casa, Autocuidado, Hidratação e Lazer.
- Criar, editar, excluir, duplicar e concluir/desmarcar tarefas.
- Campos de descrição, categoria, emoji, observações, prioridade, horário e duração.
- Recorrência diária, semanal e dias úteis, materializada em ocorrências independentes até 18/05/2027.
- Categorias personalizadas persistidas no Room.
- Modelos de checklist salvos no Room e reaplicáveis a outros dias.
- Registro diário de meta, avaliação e anotações.
- Progresso diário, progresso por categoria, resumo semanal e histórico recente de registros.
- Eventos/compromissos continuam com criação, edição, exclusão e conclusão; consulta de intervalo considera interseção de duração e eventos que atravessam a meia-noite.
- Dashboard lê tarefas e compromissos do Room; marcar tarefa como concluída no Dashboard ou na Rotina atualiza o mesmo registro.

## Migração do banco
A versão do Room foi elevada para 3. A migração `2 → 3` acrescenta campos de checklist às tarefas, campos de meta/avaliação aos registros diários e cria as tabelas de categorias e modelos. A migração `1 → 2` existente foi preservada.

## Abrir
1. Extraia o ZIP.
2. Abra a pasta `PROJETO_180_DIAS` no Android Studio.
3. Use JDK 17 e instale Android SDK 36.
4. Sincronize o Gradle e execute `app` em emulador ou Samsung Z Flip 7 FE.

O projeto-fonte não inclui `gradlew`/`gradle-wrapper.jar`. É necessário gerar o Gradle Wrapper no Android Studio ou usar uma instalação Gradle compatível antes de executar `assembleDebug` e `test`.

## Validação realizada nesta entrega
- Inspeção da estrutura da Etapa 3, Room, DAOs, repositórios, Dashboard e Minha Rotina.
- Compilação isolada do utilitário Kotlin de regras de calendário/recorrência.
- Execução de cinco cenários de smoke test para interseção de intervalos, limites semiabertos, evento atravessando meia-noite, recorrência semanal e dias úteis.
- Execução da migração SQL 2 → 3 em SQLite em memória, verificando colunas e tabelas criadas.
- Verificação sintática dos fontes Kotlin sem classpath Android/Compose (não substitui Gradle Sync nem compilação Android).
- Integridade do ZIP verificada.

## Pendências de validação
- Gradle Sync, `./gradlew clean`, `./gradlew assembleDebug` e `./gradlew test` não foram executados: o ambiente de construção não tem Gradle/Wrapper nem Android SDK configurados.
- Testes instrumentados e teste físico no Samsung Z Flip 7 FE permanecem pendentes.
- As abas Hábitos, Evolução e Perfil continuam como áreas reservadas, conforme o escopo anterior.
