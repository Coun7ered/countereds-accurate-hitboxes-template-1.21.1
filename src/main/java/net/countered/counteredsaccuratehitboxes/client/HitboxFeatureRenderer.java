package net.countered.counteredsaccuratehitboxes.client;

import net.countered.counteredsaccuratehitboxes.mixin.accessors.AnimalModelAccessor;
import net.countered.counteredsaccuratehitboxes.mixin.accessors.MixinCuboidAccessor;
import net.countered.counteredsaccuratehitboxes.mixin.accessors.ModelPartAccessor;
import net.countered.counteredsaccuratehitboxes.networking.HitboxPayload;
import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

@Environment(EnvType.CLIENT)
public class HitboxFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends ModStuckObjectsFeatureRenderer<T, M> {

    public HitboxFeatureRenderer(EntityRendererFactory.Context context, LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }
    private final Map<UUID, Integer> lastAges = new HashMap<>();
    @Override
    protected void renderAtPart(List<Pair<ModelPart, String>> modelPartListWithName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float tickDelta) {
        List<List<Vector3f>> vertexCol = new ArrayList<>();

        for (Pair<ModelPart, String> modelPart : modelPartListWithName) {
            matrices.push();
            if (entity.isBaby()) {
                if (modelPart.getRight().equals(EntityModelPartNames.HEAD) || modelPart.getRight().equals("AnimalHEAD")) {
                    if (modelPart.getLeft().equals(EntityModelPartNames.HEAD)) {
                        matrices.scale(0.75F, 0.75F, 0.75F);
                        matrices.translate(0, 1, 0); // Optional, falls Position korrigiert werden muss
                    } else {
                        AnimalModel animalModel = (AnimalModel) this.getContextModel();
                        matrices.translate(0, ((AnimalModelAccessor) animalModel).getChildHeadYOffset() / 16, ((AnimalModelAccessor) animalModel).getChildHeadZOffset() / 16);
                    }
                } else {
                    matrices.scale(0.5F, 0.5F, 0.5F); // Modell entsprechend der Babyskalierung anpassen
                    matrices.translate(0, 1.5, 0); // Optional, falls Position korrigiert werden muss
                }
            }
            // Berechne einen pulsierenden Alpha-Wert basierend auf der Entity-Alterung (age).
            float pulse = (MathHelper.sin(entity.age * 0.25f) + 1.0f) / 2.0f; // Wert zwischen 0 und 1
            int alpha = (int) (pulse * 80) + 80; // Alpha zwischen 0 und 255

            // Definiere die Farbe: Rot pulsierend (ARGB)
            int red = 250;
            int green = 30;
            int blue = 30;
            int color = (alpha << 24) | (red << 16) | (green << 8) | blue;

            // Hole einen VertexConsumer aus einem passenden RenderLayer.
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ModRenderLayers.WEAK_SPOT_LAYER);

            if (((ModelPartAccessor) (Object) modelPart.getLeft()).getCuboids().isEmpty()) {
                matrices.pop();
                continue;
            }
            List<ModelPart.Cuboid> cuboids = ((ModelPartAccessor) (Object) modelPart.getLeft()).getCuboids();
            for (ModelPart.Cuboid cuboid : cuboids) {
                matrices.push();
                List<Vector3f> vertices = getCuboidHitbox(cuboid, vertexConsumer, modelPart.getLeft(), matrices, color, OverlayTexture.DEFAULT_UV, light);

                Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
                Vec3d cameraPos = camera.getPos();

                for (Vector3f vertex : vertices) {
                    vertex  = vertex.add(cameraPos.toVector3f());
                }
                vertexCol.add(vertices);

                matrices.pop();
            }
            matrices.pop();
        }
        //renderBoundingBoxes(boxes, matrices, vertexConsumers, entity);

        int lastAge = lastAges.getOrDefault(entity.getUuid(), 0);
        if (entity.age - lastAge < 1) {
            return; // Verhindert das Rendern, wenn die Altersdifferenz zu klein ist
        }
        lastAges.put(entity.getUuid(), entity.age); // Aktualisiere den gespeicherten Wert
        entity.setAttached(HitboxAttachment.HITBOXES, vertexCol);
        sendHitboxesToClient(vertexCol, entity);
    }
    // DebugRenderer für das Zeichnen von Linien verwenden
    private void renderBoundingBoxes(List<Box> boxes, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity) {
        // Iteriere über alle Boxen
        for (Box box : boxes) {

            // Zeichne Linien, um die Box anzuzeigen
            DebugRenderer.drawBox(matrices, vertexConsumers,
                    -box.minX, -box.minY, -box.minZ,
                    -box.maxX, -box.maxY, -box.maxZ,
                    1,1,0,1);
        }
    }
    public List<Vector3f> getCuboidHitbox(ModelPart.Cuboid cuboid, VertexConsumer vertexConsumer, ModelPart modelPart, MatrixStack matrices, int color, int overlay, int light) {
        Set<Vector3f> transformedVertices = new HashSet<>();

        matrices.push(); // Speichert den aktuellen Transformationszustand
        modelPart.rotate(matrices); // Transformation des ModelParts

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Vector3f vector3f = new Vector3f();
        // Min/Max-Koordinaten initialisieren mit extremen Werten
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        for (ModelPart.Quad quad : ((MixinCuboidAccessor) (Object) cuboid).getSides()) {
            Vector3f vector3f2 = entry.transformNormal(quad.direction, vector3f);
            float f = vector3f2.x();
            float g = vector3f2.y();
            float h = vector3f2.z();

            for (ModelPart.Vertex vertex : quad.vertices) {
                // Transformiere die Position des Vertex
                Vector3f transformedPos = new Vector3f(
                        vertex.pos.x() / 16.0F,
                        vertex.pos.y() / 16.0F,
                        vertex.pos.z() / 16.0F
                );
                transformedPos = matrix4f.transformPosition(transformedPos);
                transformedVertices.add(transformedPos);

                // Rendering der Vertex-Punkte für den Würfel
                vertexConsumer.vertex(transformedPos.x(), transformedPos.y(), transformedPos.z(), color, vertex.u, vertex.v, overlay, light, f, g, h);
            }
        }
        matrices.pop(); // Setzt den Transformationszustand zurück
        return transformedVertices.stream().toList();
    }

    private void sendHitboxesToClient(List<List<Vector3f>> boxList, Entity entity) {
        HitboxPayload payload = new HitboxPayload(boxList, entity.getId());
        // Paket senden
        ClientPlayNetworking.send(payload);
    }
}
