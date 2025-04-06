package net.countered.counteredsaccuratehitboxes.mixin.server;

import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

        Vec3d direction = max.subtract(min).normalize();

        for (Entity target : world.getOtherEntities(entity, box, predicate)) {
            List<List<Vector3f>> hitboxes = target.getAttached(HitboxAttachment.HITBOXES);

            // Fallback auf normale Hitbox
            if (hitboxes == null || hitboxes.isEmpty()) {
                Box fallback = target.getBoundingBox();
                Optional<Vec3d> optional = fallback.raycast(min, max);

                if (fallback.contains(min)) {
                    double distance = 0.0;
                    if (distance < closestDistance || closestDistance == 0.0) {
                        if (!target.getRootVehicle().equals(entity.getRootVehicle()) || closestDistance == 0.0) {
                            closestEntity = target;
                            hitPos = optional.orElse(min);
                            closestDistance = 0.0;
                        }
                    }
                } else if (optional.isPresent()) {
                    Vec3d intersect = optional.get();
                    double distance = min.squaredDistanceTo(intersect);
                    if (distance < closestDistance || closestDistance == 0.0) {
                        if (!target.getRootVehicle().equals(entity.getRootVehicle()) || closestDistance == 0.0) {
                            closestEntity = target;
                            hitPos = intersect;
                            closestDistance = distance;
                        }
                    }
                }
                continue;
            }

            // Trefferprüfung gegen alle Custom-Hitboxen (pro Quader mit 8 Punkten)
            for (List<Vector3f> cubeVerts : hitboxes) {
                if (cubeVerts.size() != 8) continue;

                List<List<Vector3f>> triangles = buildCubeTriangles(cubeVerts); // 12 Dreiecke

                for (List<Vector3f> tri : triangles) {
                    Optional<Vec3d> optional = rayIntersectsTriangle(min, direction, tri.get(0), tri.get(1), tri.get(2));
                    if (optional.isPresent()) {
                        Vec3d intersect = optional.get();
                        double distance = min.squaredDistanceTo(intersect);
                        if (distance < closestDistance || closestDistance == 0.0) {
                            if (!target.getRootVehicle().equals(entity.getRootVehicle()) || closestDistance == 0.0) {
                                closestEntity = target;
                                hitPos = intersect;
                                closestDistance = distance;
                            }
                        }
                    }
                }
            }
        }

        if (closestEntity != null) {
            cir.setReturnValue(new EntityHitResult(closestEntity, hitPos));
        } else {
            cir.setReturnValue(null);
        }

        cir.cancel();
    }

    @Unique
    private static List<List<Vector3f>> buildCubeTriangles(List<Vector3f> verts) {
        if (verts.size() != 8) throw new IllegalArgumentException("Cube needs 8 vertices!");

        var v = verts; // kürzer
        return List.of(
                List.of(v.get(0), v.get(1), v.get(2)), List.of(v.get(0), v.get(2), v.get(3)),
                List.of(v.get(5), v.get(4), v.get(7)), List.of(v.get(5), v.get(7), v.get(6)),
                List.of(v.get(4), v.get(0), v.get(3)), List.of(v.get(4), v.get(3), v.get(7)),
                List.of(v.get(1), v.get(5), v.get(6)), List.of(v.get(1), v.get(6), v.get(2)),
                List.of(v.get(3), v.get(2), v.get(6)), List.of(v.get(3), v.get(6), v.get(7)),
                List.of(v.get(4), v.get(5), v.get(1)), List.of(v.get(4), v.get(1), v.get(0))
        );
    }


    @Unique
    private static Optional<Vec3d> rayIntersectsTriangle(Vec3d origin, Vec3d direction, Vector3f v0, Vector3f v1, Vector3f v2) {
        final double EPSILON = 1e-6;

        Vec3d edge1 = toVec(v1).subtract(toVec(v0));
        Vec3d edge2 = toVec(v2).subtract(toVec(v0));

        Vec3d h = direction.crossProduct(edge2);
        double a = edge1.dotProduct(h);

        if (a > -EPSILON && a < EPSILON) return Optional.empty(); // Ray parallel

        double f = 1.0 / a;
        Vec3d s = origin.subtract(toVec(v0));
        double u = f * s.dotProduct(h);
        if (u < 0.0 || u > 1.0) return Optional.empty();

        Vec3d q = s.crossProduct(edge1);
        double v = f * direction.dotProduct(q);
        if (v < 0.0 || u + v > 1.0) return Optional.empty();

        double t = f * edge2.dotProduct(q);
        if (t > EPSILON) {
            return Optional.of(origin.add(direction.multiply(t)));
        } else {
            return Optional.empty(); // intersection behind ray
        }
    }

    @Unique
    private static Vec3d toVec(Vector3f vec) {
        return new Vec3d(vec.x, vec.y, vec.z);
    }
}


