package net.countered.counteredsaccuratehitboxes.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Unique;

public interface ModInteraction {
        ActionResult run(ServerPlayerEntity player, Entity entity, Hand hand);
}
