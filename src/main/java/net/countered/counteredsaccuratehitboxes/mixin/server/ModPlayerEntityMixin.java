package net.countered.counteredsaccuratehitboxes.mixin.server;

import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class ModPlayerEntityMixin extends LivingEntity {

    protected ModPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 2)
    private boolean modifyCritCondition(boolean original, Entity entity) {
        List<Box> hitboxes = entity.getAttached(HitboxAttachment.HITBOXES);

        if (entity.getAttached(HitboxAttachment.HITBOXES) == null) {
            return original;
        }
        for (Box hitbox : hitboxes) {
            // Prüfen, ob das Fadenkreuz auf dem Weak Spot ist
            if (isCrosshairOnWeakSpot((PlayerEntity) (Object) this, hitbox)) {
                return true;
            }
        }
        return false; // Kritischer Treffer, wenn der Weak Spot getroffen wurde
    }
    // Prüft, ob das Fadenkreuz auf den Weak Spot zeigt
    @Unique
    private boolean isCrosshairOnWeakSpot(PlayerEntity player, Box weakSpot) {
        // Aktuelle Blickrichtung des Spielers (normierter Vektor)
        Vec3d cameraPos = player.getCameraPosVec(1.0f);
        Vec3d lookVec = player.getRotationVec(1.0f); // Blickrichtung
        // Endpunkt des Raycasts (z.B. 5 Blöcke in Blickrichtung)
        Vec3d rayEnd = cameraPos.add(lookVec.multiply(5));
        // Überprüfe, ob der Strahl die Weak Spot Box trifft
        Optional<Vec3d> rayHit = weakSpot.raycast(cameraPos, rayEnd);
        // Gibt true zurück, wenn der Strahl durch die Box geht
        return rayHit.isPresent();
    }
}
