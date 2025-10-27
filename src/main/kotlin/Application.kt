import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.Duration
import io.github.cdimascio.dotenv.dotenv

fun main() = runBlocking {
    val dotenv = dotenv()

    val webhookUrl = dotenv["DISCORD_WEBHOOK_URL"]
    val urlBusca = dotenv["LINKEDIN_SEARCH_URL"]
    if (webhookUrl == null || urlBusca == null) {
        println("🔥 ERRO FATAL: Variáveis DISCORD_WEBHOOK_URL ou LINKEDIN_SEARCH_URL não encontradas no .env")
        println("   Por favor, verifique se o arquivo .env existe e contém as variáveis.")
        return@runBlocking
    }

    println("🚀 Iniciando o monitor de vagas e o servidor web...")

    launch {
        embeddedServer(Netty, port = 8080) {
            routing {
                get("/status") {
                    call.respondText("✅ Webhook monitor rodando!")
                }
            }
        }.start(wait = true)
    }

    launch {
        monitorarVagas(webhookUrl, urlBusca)
    }

    println("✅ Servidor rodando em http://localhost:8080/status")
    println("✅ Monitor de vagas iniciado. Aguardando a primeira verificação...")
}

suspend fun monitorarVagas(webhookUrl: String, urlBusca: String) {
    val client = HttpClient(CIO)
    val lastJobs = mutableSetOf<String>()

    val zona = ZoneId.systemDefault()
    val agora = ZonedDateTime.now(zona)
    val oitoDaManhaDeHoje = agora.withHour(8).withMinute(0).withSecond(0).withNano(0)

    if (agora.isAfter(oitoDaManhaDeHoje)) {
        println("🔍 8:00 da manhã já passou. Executando verificação de inicialização...")
        try {
            executarVerificacaoVagas(client, urlBusca, webhookUrl, lastJobs)
        } catch (e: Exception) {
            println("⚠️ Erro ao buscar vagas na inicialização: ${e.message}")
        }
    } else {
        println("✅ 8:00 da manhã ainda não passou. Aguardando a primeira execução agendada.")
    }


    while (true) {
        println("💤 Aguardando até as 8:00 da manhã para a próxima verificação...")
        val delayNecessario = calcularDelayAteProximaExecucao(8, 0)
        delay(delayNecessario)

        try {
            println("🔍 Verificando vagas (execução agendada)...")
            executarVerificacaoVagas(client, urlBusca, webhookUrl, lastJobs)
        } catch (e: Exception) {
            println("⚠️ Erro ao buscar vagas (execução agendada): ${e.message}")
            println("   Tentando novamente em 10 minutos...")
            delay(10 * 60 * 1000L)
        }
    }
}
fun buscarNovasVagas(url: String): Set<String> {
    val doc = Jsoup.connect(url)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .get()

    return doc.select("a.base-card__full-link")
        .map { it.attr("href") }
        .toSet()
}
suspend fun enviarWebhook(client: HttpClient, vagaUrl: String, webhookUrl: String) {
    val payload = """{"content": "💼 [Veja as últimas vagas postadas]($vagaUrl)"}"""
    try {
        client.post(webhookUrl) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    } catch (e: Exception) {
        println("   🔥 Erro ao enviar webhook: ${e.message}")
    }
}


fun calcularDelayAteProximaExecucao(hora: Int, minuto: Int): Long {
    val zona = ZoneId.systemDefault()
    val agora = ZonedDateTime.now(zona)
    var proximaExecucao = agora
        .withHour(hora)
        .withMinute(minuto)
        .withSecond(0)
        .withNano(0)

    if (agora.isAfter(proximaExecucao)) {
        proximaExecucao = proximaExecucao.plusDays(1)
    }

    val duracao = Duration.between(agora, proximaExecucao)

    return duracao.toMillis()
}

suspend fun executarVerificacaoVagas(
    client: HttpClient,
    urlBusca: String,
    webhookUrl: String,
    lastJobs: MutableSet<String>
) {
    println("🔍 Verificando vagas em: $urlBusca")
    val novasVagas = buscarNovasVagas(urlBusca)
    val vagasNovas = novasVagas - lastJobs

    if (vagasNovas.isNotEmpty()) {
        println("🚀 ${vagasNovas.size} nova(s) vaga(s) encontrada(s):")
        enviarWebhook(client, urlBusca, webhookUrl)

        lastJobs.addAll(vagasNovas)
    } else {
        println("⏳ Nenhuma vaga nova por enquanto.")
    }

}