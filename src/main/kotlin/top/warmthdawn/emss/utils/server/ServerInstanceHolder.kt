package top.warmthdawn.emss.utils.server

import top.warmthdawn.emss.utils.server.api.ServerContainerEvent
import top.warmthdawn.emss.utils.server.api.ServerInstance

/**
 *
 * @author WarmthDawn
 * @since 2021-07-20
 */
class ServerInstanceHolder(
    private val factory: ServerInstanceFactory
) {
    private val _objects = mutableMapOf<Long, ServerInstance>()

    fun add(instance: ServerInstance) {
        _objects[instance.id] = instance
        instance.on(ServerContainerEvent.REMOVED) {
            remove(it.id)
        }
    }

    suspend fun getOrCreate(id: Long): ServerInstance {
        if(_objects.containsKey(id)){
            return _objects[id]!!
        }
        val instance = factory.create(id)
        add(instance)
        return instance
    }

    fun remove(id: Long) {
        _objects.remove(id)
    }
}