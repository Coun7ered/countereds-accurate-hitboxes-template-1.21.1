package net.countered.counteredsaccuratehitboxes.client;

import net.countered.counteredsaccuratehitboxes.mixin.accessors.AnimalModelAccessor;
import net.countered.counteredsaccuratehitboxes.mixin.accessors.MixinCuboidAccessor;
import net.countered.counteredsaccuratehitboxes.mixin.accessors.ModelPartAccessor;
import net.countered.counteredsaccuratehitboxes.networking.HitboxPayload;
import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.*;
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
    private final Map<Integer, Integer> lastAges = new HashMap<>();
    @Override
    protected void renderAtPart(List<Pair<ModelPart, String>> modelPartListWithName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float tickDelta) {
        List<List<Vector3f>> vertexCol = new ArrayList<>();
        for (Pair<ModelPart, String> modelPart : modelPartListWithName) {
            matrices.push();
            if (entity.isBaby()) {
                if (modelPart.getRight().equals(EntityModelPartNames.HEAD) || modelPart.getRight().equals("AnimalHEAD")) {
                    if (modelPart.getRight().equals(EntityModelPartNames.HEAD)) {
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

            List<ModelPart.Cuboid> cuboids = ((ModelPartAccessor) (Object) modelPart.getLeft()).getCuboids();
            if (cuboids.isEmpty()) {
                matrices.pop();
                continue;
            }

            for (ModelPart.Cuboid cuboid : cuboids) {
                matrices.push();
                // Debug
                //makePartGlow(entity, vertexConsumers, matrices, OverlayTexture.DEFAULT_UV, light, cuboid, modelPart.getLeft());
                List<Vector3f> vertices = getCuboidHitbox(cuboid, modelPart.getLeft(), matrices);

                Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
                Vec3d cameraPos = camera.getPos();

                for (Vector3f vertex : vertices) {
                    vertex.add(cameraPos.toVector3f());
                }
                vertexCol.add(vertices);

                matrices.pop();
            }
            matrices.pop();
        }
        //renderBoundingBoxes(boxes, matrices, vertexConsumers, entity);

        int lastAge = lastAges.getOrDefault(entity.getId(), 0);
        if (entity.age - lastAge < 1) {
            return; // Verhindert das Rendern, wenn die Altersdifferenz zu klein ist
        }
        lastAges.put(entity.getId(), entity.age); // Aktualisiere den gespeicherten Wert
        entity.setAttached(HitboxAttachment.HITBOXES, vertexCol);
        //sendHitboxesToServer(vertexCol, entity);
    }

    private void makePartGlow(Entity entity, VertexConsumerProvider vertexConsumers, MatrixStack matrices, int overlay, int light, ModelPart.Cuboid cuboid, ModelPart modelPart) {
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

        matrices.push(); // Speichert den aktuellen Transformationszustand
        modelPart.rotate(matrices); // Transformation des ModelParts

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Vector3f vector3f = new Vector3f();

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

                // Rendering der Vertex-Punkte für den Würfel
                vertexConsumer.vertex(transformedPos.x(), transformedPos.y(), transformedPos.z(), color, vertex.u, vertex.v, overlay, light, f, g, h);
            }
        }
        matrices.pop(); // Setzt den Transformationszustand zurück
    }


    public List<Vector3f> getCuboidHitbox(ModelPart.Cuboid cuboid, ModelPart modelPart, MatrixStack matrices) {
        Set<Vector3f> transformedVertices = new HashSet<>();

        matrices.push(); // Speichert den aktuellen Transformationszustand
        modelPart.rotate(matrices); // Transformation des ModelParts

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();

        for (ModelPart.Quad quad : ((MixinCuboidAccessor) (Object) cuboid).getSides()) {

            for (ModelPart.Vertex vertex : quad.vertices) {
                // Transformiere die Position des Vertex
                Vector3f transformedPos = new Vector3f(
                        vertex.pos.x() / 16.0F,
                        vertex.pos.y() / 16.0F,
                        vertex.pos.z() / 16.0F
                );
                transformedPos = matrix4f.transformPosition(transformedPos);
                transformedVertices.add(transformedPos);
            }
        }
        matrices.pop(); // Setzt den Transformationszustand zurück
        return transformedVertices.stream().toList();
    }

    private void sendHitboxesToServer(List<List<Vector3f>> boxList, Entity entity) {
        HitboxPayload payload = new HitboxPayload(boxList, entity.getId());
        // Paket senden
        ClientPlayNetworking.send(payload);
    }
}
