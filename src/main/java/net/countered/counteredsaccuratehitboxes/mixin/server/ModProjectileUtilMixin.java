package net.countered.counteredsaccuratehitboxes.mixin.server;

import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.countered.counteredsaccuratehitboxes.util.Triangle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
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

        Vec3d rayDir = max.subtract(min).normalize();
        double rayLength = min.distanceTo(max);

        for (Entity target : world.getOtherEntities(entity, box, predicate)) {
            List<List<Vector3f>> hitboxes = target.getAttached(HitboxAttachment.HITBOXES);

            if (hitboxes == null || hitboxes.isEmpty()) {
                Box fallback = target.getBoundingBox().expand(target.getTargetingMargin());
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

            for (List<Vector3f> cubeVerts : hitboxes) {
                if (cubeVerts.size() == 4) {
                    cubeVerts = inflateQuadToBox(cubeVerts, 0.01f); // → künstlich „dicke“ Box machen
                }
                if (cubeVerts.size() != 8) {
                    continue;
                }
                List<Vector3f> sortedVerts = sortVertices(cubeVerts);
                //showHitboxVertices(world, sortedVerts);


                List<Triangle> triangles = buildCubeTriangles(sortedVerts);

                for (Triangle triangle : triangles) {
                    Optional<Vec3d> intersection = rayTriangleIntersect(min, rayDir, rayLength, triangle);
                    if (intersection.isPresent()) {
                        Vec3d intersect = intersection.get();
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
    private static List<Vector3f> inflateQuadToBox(List<Vector3f> quad, float thickness) {
        if (quad.size() != 4) throw new IllegalArgumentException("Expected 4 vertices for quad");

        Vector3f a = quad.get(0);
        Vector3f b = quad.get(1);
        Vector3f c = quad.get(2);

        Vector3f ab = new Vector3f(b.x() - a.x(), b.y() - a.y(), b.z() - a.z());
        Vector3f ac = new Vector3f(c.x() - a.x(), c.y() - a.y(), c.z() - a.z());

        Vector3f normal = new Vector3f(
                ab.y() * ac.z() - ab.z() * ac.y(),
                ab.z() * ac.x() - ab.x() * ac.z(),
                ab.x() * ac.y() - ab.y() * ac.x()
        );

        normal.normalize();
        float halfThickness = thickness / 2f;

        normal = new Vector3f(
                normal.x() * halfThickness,
                normal.y() * halfThickness,
                normal.z() * halfThickness
        );

        List<Vector3f> inflated = new ArrayList<>(8);
        for (Vector3f v : quad) {
            Vector3f plus = new Vector3f(
                    v.x() + normal.x(),
                    v.y() + normal.y(),
                    v.z() + normal.z()
            );
            Vector3f minus = new Vector3f(
                    v.x() - normal.x(),
                    v.y() - normal.y(),
                    v.z() - normal.z()
            );
            inflated.add(plus);
            inflated.add(minus);
        }

        return inflated;
    }


    @Unique
    private static List<Vector3f> sortVertices(List<Vector3f> verts) {
        if (verts.size() != 8) {
            throw new IllegalArgumentException("Expected exactly 8 vertices");
        }

        Vector3f center = new Vector3f();
        for (Vector3f v : verts) {
            center.add(v);
        }
        center.div(8.0f);

        return verts.stream()
                .sorted(Comparator
                        .comparing((Vector3f v) -> v.y < center.y)
                        .thenComparing(v -> v.z < center.z)
                        .thenComparing(v -> v.x < center.x)
                )
                .toList();
    }
    @Unique
    private static List<Triangle> buildCubeTriangles(List<Vector3f> verts) {
        List<Triangle> triangles = new ArrayList<>(12);

        int[][] triIndices = {
                // Bottom face (-Y): 0, 1, 4, 5
                {0, 1, 4}, {1, 5, 4},
                // Top face (+Y): 2, 3, 6, 7
                {2, 6, 3}, {3, 6, 7},
                // Front face (-Z): 0, 1, 2, 3
                {0, 2, 1}, {1, 2, 3},
                // Back face (+Z): 4, 5, 6, 7
                {4, 5, 6}, {5, 7, 6},
                // Left face (-X): 0, 2, 4, 6
                {0, 4, 2}, {2, 4, 6},
                // Right face (+X): 1, 3, 5, 7
                {1, 3, 5}, {3, 7, 5}
        };
        // Create triangles using the indices
        for (int[] indices : triIndices) {
            triangles.add(new Triangle(
                    verts.get(indices[0]),
                    verts.get(indices[1]),
                    verts.get(indices[2])
            ));
        }
        return triangles;
    }

    /**
     * Möller–Trumbore algorithm for ray-triangle intersection with improvements
     */
    @Unique
    private static Optional<Vec3d> rayTriangleIntersect(Vec3d rayOrigin, Vec3d rayDir, double rayLength, Triangle triangle) {
        final float EPSILON = 0.01f;
        Vector3f v0 = triangle.v0();
        Vector3f v1 = triangle.v1();
        Vector3f v2 = triangle.v2();
        // Vectors for two edges sharing v0
        Vector3f edge1 = new Vector3f(v1).sub(v0);
        Vector3f edge2 = new Vector3f(v2).sub(v0);
        // Calculate determinant
        Vector3f rayDirV = new Vector3f((float) rayDir.x, (float) rayDir.y, (float) rayDir.z);
        Vector3f pvec = new Vector3f();
        rayDirV.cross(edge2, pvec);
        float det = edge1.dot(pvec);
        // Backface culling disabled - we want to detect hits from all directions
        if (det > -EPSILON && det < EPSILON) {
            return Optional.empty(); // Ray parallel to triangle
        }
        float invDet = 1.0f / det;
        // Calculate distance from v0 to ray origin
        Vector3f tvec = new Vector3f(
                (float) rayOrigin.x - v0.x,
                (float) rayOrigin.y - v0.y,
                (float) rayOrigin.z - v0.z
        );
        // Calculate u parameter
        float u = tvec.dot(pvec) * invDet;
        // Check bounds - use a small epsilon for tolerance at edges
        if (u < -EPSILON || u > 1.0f + EPSILON) {
            return Optional.empty();
        }
        // Calculate v parameter
        Vector3f qvec = new Vector3f();
        tvec.cross(edge1, qvec);
        float v = rayDirV.dot(qvec) * invDet;
        // Check bounds with tolerance
        if (v < -EPSILON || u + v > 1.0f + EPSILON) {
            return Optional.empty();
        }
        // Calculate t - distance along ray
        float t = edge2.dot(qvec) * invDet;
        // Check if intersection is within the ray segment
        if (t < EPSILON || t > rayLength) {
            return Optional.empty();
        }
        // Return the intersection point
        return Optional.of(rayOrigin.add(rayDir.multiply(t)));
    }

    @Unique
    private static void showHitboxVertices(World world, List<Vector3f> verts) {
        for (Vector3f v : verts) {
            world.addParticle(ParticleTypes.END_ROD, v.x, v.y, v.z, 0, 0, 0);
        }
    }
}