package at.xirado.jdui.example

import at.xirado.jdui.JDUIListener
import at.xirado.jdui.config.PersistenceConfig
import at.xirado.jdui.config.Secret
import at.xirado.jdui.config.ViewData
import at.xirado.jdui.config.jdui
import at.xirado.jdui.context
import at.xirado.jdui.example.view.*
import at.xirado.jdui.utils.hexStringToByteArray
import at.xirado.jdui.view.replyView
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.OkHttpClient
import java.sql.Connection
import java.sql.DriverManager
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists

val queries = listOf(
    "CREATE TABLE IF NOT EXISTS views (id BIGINT NOT NULL PRIMARY KEY, data BLOB NOT NULL)"
)

fun main(args: Array<String>) {
    val token = args[0]

    val dbFile = Path("example.db")
    if (!dbFile.exists())
        dbFile.createFile()

    Class.forName("org.sqlite.JDBC")
    val connection = DriverManager.getConnection("jdbc:sqlite:example.db")

    queries.forEach { runQuery(connection, it) }

    val persistenceConfig = SqlitePersistenceConfig(connection)

    val okHttpClient = OkHttpClient()

    val password = "verysecurepassword"
    val salt = hexStringToByteArray("deadbeefdeadbeef0123012301234567")

    val jduiConfig = jdui {
        this.persistenceConfig = persistenceConfig
        this.secret = Secret(password, salt)
        provideContext(okHttpClient)
    }

    val jda = JDABuilder.createLight(token)
        .addEventListeners(JDUIListener(jduiConfig), CommandListener)
        .build()

    jda.updateCommands()
        .addCommands(Commands.slash("cat", "Show a cat")
            .setIntegrationTypes(IntegrationType.USER_INSTALL).setContexts(InteractionContextType.ALL)
        )
        .addCommands(Commands.slash("counter", "Cookie clicker on withdrawal")
            .setIntegrationTypes(IntegrationType.USER_INSTALL).setContexts(InteractionContextType.ALL)
        )
        .addCommands(Commands.slash("separator", "Separator test")
            .setIntegrationTypes(IntegrationType.USER_INSTALL).setContexts(InteractionContextType.ALL)
        )
        .addCommands(Commands.slash("test", "Component test")
            .setIntegrationTypes(IntegrationType.USER_INSTALL).setContexts(InteractionContextType.ALL)
        )
        .addCommands(Commands.slash("multi-menu", "Test multi-menu")
            .setIntegrationTypes(IntegrationType.USER_INSTALL).setContexts(InteractionContextType.ALL)
        )
        .addCommands(Commands.slash("urban", "Urban dictionary")
            .addOptions(OptionData(OptionType.STRING, "term", "What to search for", true))
            .setIntegrationTypes(IntegrationType.USER_INSTALL).setContexts(InteractionContextType.ALL)
        ).queue()
}

private fun runQuery(connection: Connection, query: String) {
    connection.prepareStatement(query).use { it.execute() }
}

class SqlitePersistenceConfig(val connection: Connection) : PersistenceConfig {
    override suspend fun retrieveState(id: Long): ViewData? {
        connection.prepareStatement("SELECT data FROM views WHERE id = ?").use {
            it.setLong(1, id)
            it.executeQuery().use { rs ->
                if (!rs.next())
                    return null

                val data = rs.getBytes("data")
                return ViewData(id, data)
            }
        }
    }

    override suspend fun save(viewData: ViewData) {
        val query = "INSERT INTO views(id, data) VALUES(?, ?) ON CONFLICT(id) DO UPDATE SET data = excluded.data"
        connection.prepareStatement(query).use {
            it.setLong(1, viewData.id)
            it.setBytes(2, viewData.data)
            it.execute()
        }
    }
}

object CommandListener : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        GlobalScope.launch {
            when (event.name) {
                "cat" -> event.replyView<CatView>().await()
                "counter" -> event.replyView(::counterView).await()
                "separator" -> event.replyView(::separatorTestView).await()
                "test" -> event.replyView(::componentTest).await()
                "multi-menu" -> event.replyView<MultipleMenusView>().await()
                "urban" -> {
                    val term = event.options[0].asString
                    val context = context { +UrbanDictionaryQuery(term) }
                    event.replyView<UrbanDictionaryView>(context = context).await()
                }
            }
        }
    }
}