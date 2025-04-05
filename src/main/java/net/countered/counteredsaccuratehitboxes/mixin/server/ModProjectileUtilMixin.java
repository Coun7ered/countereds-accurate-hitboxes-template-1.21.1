package net.countered.counteredsaccuratehitboxes.mixin.server;

import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public abstract class ModProjectileUtilMixin {

    @Inject(
            method = "raycast", at = @At("HEAD"),
            cancellable = true
    )
    private static void injectCustomHitboxRaycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance, CallbackInfoReturnable<EntityHitResult> cir) {
        World world = entity.getWorld();
        double closestDistance = maxDistance;
        Entity closestEntity = null;
        Vec3d hitPos = null;

        for (Entity entity1 : world.getOtherEntities(entity, box, predicate)) {
            // Hole die Custom Hitboxen

            List<Box> hitboxes = entity1.getAttached(HitboxAttachment.HITBOXES);

            if (hitboxes == null || hitboxes.isEmpty()) {
                // Fallback auf normale Hitbox
                Box fallback = entity1.getBoundingBox().expand(entity1.getTargetingMargin());
                Optional<Vec3d> optional = fallback.raycast(min, max);
                if (fallback.contains(min) || optional.isPresent()) {
                    closestEntity = entity1;
                    hitPos = optional.orElse(min);
                    closestDistance = 0.0;
                }
                continue;
            }

            for (Box customBox : hitboxes) {
                Optional<Vec3d> optional = customBox.raycast(min, max);

                if (customBox.contains(min)) {
                    closestEntity = entity1;
                    hitPos = min;
                    closestDistance = 0.0;
                    break;
                } else if (optional.isPresent()) {
                    Vec3d intersect = optional.get();
                    double distance = min.squaredDistanceTo(intersect);
                    if (distance < closestDistance) {
                        closestEntity = entity1;
                        hitPos = intersect;
                        closestDistance = distance;
                    }
                }
            }
        }

        if (closestEntity != null) {
            cir.setReturnValue(new EntityHitResult(closestEntity, hitPos));
        } else {
            cir.setReturnValue(null); // Niemand getroffen
        }
        cir.cancel();
    }
}


