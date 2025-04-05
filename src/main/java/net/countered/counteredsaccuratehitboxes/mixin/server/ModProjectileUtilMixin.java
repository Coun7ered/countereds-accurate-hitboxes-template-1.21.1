package net.countered.counteredsaccuratehitboxes.mixin.server;

import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public abstract class ModProjectileUtilMixin {

    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private static void customRaycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance, CallbackInfoReturnable<EntityHitResult> cir) {
        World world = entity.getWorld();
        double d = maxDistance;
        Entity bestHitEntity = null;
        Vec3d bestHitPos = null;

        for (Entity potentialTarget : world.getOtherEntities(entity, box, predicate)) {
            List<Box> hitboxes = potentialTarget.getAttached(HitboxAttachment.HITBOXES); // Custom Hitboxen abrufen
            if (hitboxes != null && !hitboxes.isEmpty()) {
                for (Box hitbox : hitboxes) {
                    Optional<Vec3d> optionalHit = hitbox.raycast(min, max);
                    if (optionalHit.isPresent()) {
                        Vec3d hitPos = optionalHit.get();
                        double distance = min.squaredDistanceTo(hitPos);
                        if (distance < d) {
                            bestHitEntity = potentialTarget;
                            bestHitPos = hitPos;
                            d = distance;
                        }
                    }
                }
            } else {
                // Fallback auf normale Bounding Box
                Box defaultBox = potentialTarget.getBoundingBox().expand(potentialTarget.getTargetingMargin());
                Optional<Vec3d> optional = defaultBox.raycast(min, max);
                if (optional.isPresent()) {
                    Vec3d hitPos = optional.get();
                    double distance = min.squaredDistanceTo(hitPos);
                    if (distance < d) {
                        bestHitEntity = potentialTarget;
                        bestHitPos = hitPos;
                        d = distance;
                    }
                }
            }
        }

        // Falls ein Treffer mit einer Custom-Hitbox gefunden wurde, setzen wir das als Ergebnis
        if (bestHitEntity != null) {
            cir.setReturnValue(new EntityHitResult(bestHitEntity, bestHitPos));
        }
    }
}

