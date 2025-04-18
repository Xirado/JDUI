package at.xirado.jdui.config

interface PersistenceConfig {
    suspend fun retrieveState(id: Long): ViewData?
    suspend fun save(viewData: ViewData)
}

class ViewData(
    val id: Long,
    val data: ByteArray,
)