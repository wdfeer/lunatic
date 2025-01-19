package wdfeer.lunatic

import com.google.common.collect.ImmutableSet
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.ladysnake.blabber.Blabber

fun Lunatic.initializeDoremyEntity() {
    Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "doremy"), DoremyEntityType)
}

class Doremy(world: World) : Entity(DoremyEntityType, world) {
    init {
        setPosition(0.5, 217.0, 0.5)
        customName = Text.literal("Doremy Sweet")
    }

    override fun initDataTracker() {}
    override fun readCustomDataFromNbt(nbt: NbtCompound?) {}
    override fun writeCustomDataToNbt(nbt: NbtCompound?) {}

    // FIXME this doesn't run on right-click for some reason
    override fun interact(player: PlayerEntity?, hand: Hand?): ActionResult {
        return if (player is ServerPlayerEntity) {
            Blabber.startDialogue(player, Identifier.of(Lunatic.MOD_ID, "doremy"))
            ActionResult.SUCCESS
        } else super.interact(player, hand)
    }

    // TODO: workaround for the rightclick not working
    override fun tick() {
        if ((world.time % 60).toInt() == 0)
            world.players.forEach { player ->
                if (player is ServerPlayerEntity && player.distanceTo(this) < 6)
                    Blabber.startDialogue(player, Identifier.of(Lunatic.MOD_ID, "doremy"))
            }
        super.tick()
    }
}

object DoremyEntityType : EntityType<Doremy>(
    { _, world -> Doremy(world) },
    SpawnGroup.MISC,
    true,
    false,
    true,
    false,
    ImmutableSet.of(),
    EntityDimensions(0.6f, 1.8f, true),
    64,
    1,
    FeatureSet.empty()
)