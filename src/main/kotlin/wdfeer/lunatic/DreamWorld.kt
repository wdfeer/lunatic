package wdfeer.lunatic

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.ChunkSectionPos
import wdfeer.lunatic.Lunatic.MOD_ID

const val DREAM_WORLD_PATH = "dream_world"
fun Lunatic.initializeDreamWorld() {
    initializeFeatures()
    initializeTeleportation()
    initializeDoremy()
}

private fun Lunatic.initializeFeatures() {
    Registry.register(Registries.FEATURE, Identifier.of(MOD_ID, "dream_grid"), GridFeature())
    Registry.register(Registries.FEATURE, Identifier.of(MOD_ID, "dream_dungeon"), DungeonFeature())
}

private fun initializeTeleportation() =
    ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity: LivingEntity, _: DamageSource, amount: Float ->
        if (entity !is ServerPlayerEntity) return@register true
        if (amount < entity.health) return@register true

        return@register if (entity.isSleeping) {
            val server = entity.server
            val dreamWorld = server.getDreamWorld()

            entity.clearSleepingPosition()
            ChunkSectionPos.from(dreamWorld.getChunk(0, 0)).minPos.let {
                // Teleport on the grid
                entity.teleport(
                    dreamWorld,
                    0.0, // Flag not set - uses player pos
                    40.0,
                    0.0, // Flag not set - uses player pos
                    setOf(PositionFlag.Y),
                    0f,
                    0f
                )
            }


            false
        } else if (entity.world.registryKey.value.path == DREAM_WORLD_PATH) {
            entity.teleport(entity.server.overworld, 0.0, 256.0, 0.0, 0f, 0f)
            false
        } else true
    }

private fun initializeDoremy() =
    ServerTickEvents.START_SERVER_TICK.register { server ->
        if (server.ticks % 20 != 0) return@register

        val world = server.getDreamWorld()
        val noDoremy = world.iterateEntities().none { it.displayName.string == "Doremy Sweet" }

        // Spawn doremy if she is not there
        if (world.isChunkLoaded(0, 0) && noDoremy) {
            world.spawnEntity(Doremy(world))
        }
    }

fun MinecraftServer.getDreamWorld(): ServerWorld = getWorld(
    RegistryKey.of(
        RegistryKeys.WORLD,
        Identifier.of(MOD_ID, DREAM_WORLD_PATH)
    )
)!! // Must be registered