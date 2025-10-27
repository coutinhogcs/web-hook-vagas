# 🤖 Monitor de Vagas LinkedIn para Discord

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?logo=kotlin)
![Ktor](https://img.shields.io/badge/Ktor-2.3.10-0095D5?logo=ktor)
![Jsoup](https://img.shields.io/badge/Jsoup-1.17.2-orange)
![DotEnv](https://img.shields.io/badge/DotEnv-6.4.1-yellow)
![Status](https://img.shields.io/badge/Status-Funcional-brightgreen)

Um bot simples em Kotlin que monitora uma busca de vagas no LinkedIn e envia uma notificação para um canal do Discord quando encontra novas oportunidades.

Ele foi desenhado para rodar 24/7 em um servidor, verificando as vagas em um horário agendado e notificando de forma limpa e eficiente.

## 🚀 O que ele faz?

Quando novas vagas são encontradas na URL de busca monitorada, o bot envia uma única mensagem no seu canal do Discord:

> **Monitor de Vagas** `BOT`
>
> 💼 [Veja as últimas vagas postadas](https://www.linkedin.com/jobs/search/...)

## ✨ Funcionalidades Principais

* **Scraping Inteligente:** Usa o Jsoup para buscar vagas em uma URL de busca específica do LinkedIn.
* **Agendamento Preciso:** Roda a verificação em um horário específico (ex: 8:00 da manhã), e não apenas a cada 24h.
* **Execução Imediata (Run on Start):** Se o horário agendado (ex: 8:00) já passou no dia em que o bot foi iniciado, ele executa uma verificação *imediatamente* antes de agendar a próxima.
* **Notificação Única:** Envia uma *única* mensagem de alerta, em vez de spammar o canal com cada vaga individual.
* **Evita Duplicatas:** Mantém um histórico em memória das vagas já vistas para notificar apenas sobre as que são *realmente* novas.
* **Configuração Segura:** Utiliza um arquivo `.env` para carregar segredos (URL do Webhook), mantendo seu código-fonte limpo e seguro.
* **Servidor de Status:** Roda um servidor Ktor na porta `8080` com um endpoint `/status` para health checks (ex: Uptime Kuma).

## 🛠️ Tecnologias Utilizadas

* **[Kotlin](https://kotlinlang.org/):** Linguagem de programação principal.
* **[Ktor](https://ktor.io/):** Utilizado tanto para o cliente HTTP (enviar o webhook) quanto para o servidor web de status.
* **[Jsoup](https://jsoup.org/):** Biblioteca para fazer o web scraping da página do LinkedIn.
* **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html):** Para rodar o servidor web e o monitor de vagas em paralelo.
* **[Dotenv-kotlin](https://github.com/cdimascio/dotenv-kotlin):** Para carregar variáveis de ambiente (segredos) de um arquivo `.env` de forma segura.

## ⚙️ Configuração e Instalação

Este projeto usa um arquivo `.env` para gerenciar suas URLs. Você **não** precisa editar as funções de lógica para configurar o bot.

### 1. Dependências

Certifique-se de que a biblioteca `dotenv-kotlin` está no seu arquivo `build.gradle.kts`:

```kotlin
dependencies {
    // ...
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}