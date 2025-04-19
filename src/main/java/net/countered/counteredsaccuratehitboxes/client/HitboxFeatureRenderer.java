package net.countered.counteredsaccuratehitboxes.client;

import net.countered.counteredsaccuratehitboxes.mixin.accessors.AnimalModelAccessor;
import net.countered.counteredsaccuratehitboxes.mixin.accessors.MixinCuboidAccessor;
import net.countered.counteredsaccuratehitboxes.mixin.accessors.ModelPartAccessor;
import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

    @Override
    protected void renderAtPart(List<ModelPart> modelPartList, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float tickDelta) {
        List<List<Vector3f>> vertexCol = new ArrayList<>();

        if (entity.isBaby() && !babyAllowed.contains(entity.getType())) return;
        M model = getContextModel();
        for (ModelPart modelPart : modelPartList) {
            Set<ModelPart> excludedParts = new HashSet<>();
            vertexCol.addAll(createHitboxes(matrices, modelPart, entity, vertexConsumers, light, excludedParts));
        }

        entity.setAttached(HitboxAttachment.HITBOXES, vertexCol);
    }

    private static final List<EntityType> babyAllowed = new ArrayList<>();
    static {
        babyAllowed.add(EntityType.ZOMBIE);
        babyAllowed.add(EntityType.ZOMBIE_VILLAGER);
        babyAllowed.add(EntityType.PIGLIN);
        babyAllowed.add(EntityType.ZOMBIFIED_PIGLIN);
        babyAllowed.add(EntityType.COW);
        babyAllowed.add(EntityType.PIG);
        babyAllowed.add(EntityType.SHEEP);
    }
    private List<List<Vector3f>> createHitboxes(MatrixStack matrices, ModelPart modelPart, T entity, VertexConsumerProvider vertexConsumers, int light, Set<ModelPart> excludedParts) {
        if (modelPart.hasChild(EntityModelPartNames.HAT_RIM)) {
            excludedParts.add(modelPart.getChild(EntityModelPartNames.HAT_RIM));
        }
        if (excludedParts.contains(modelPart)) {
            return Collections.emptyList();
        }
        List<List<Vector3f>> vertexList = new ArrayList<>();
        matrices.push();
        if (entity.isBaby()) {
            scaleBabyMatices(entity, modelPart, matrices);
        }
        if (entity.getType().equals(EntityType.RABBIT)) {
            matrices.scale(0.6F, 0.6F, 0.6F);
            matrices.translate(0, 1, 0);
        }
        if (modelPart.visible) {
            List<ModelPart.Cuboid> cuboids = ((ModelPartAccessor) (Object) modelPart).getCuboids();
            modelPart.rotate(matrices);
            for (ModelPart.Cuboid cuboid : cuboids) {
                // Debug
                //makePartGlow(entity, vertexConsumers, matrices, OverlayTexture.DEFAULT_UV, light, cuboid, modelPart);
                List<Vector3f> vertices = getCuboidHitbox(cuboid, modelPart, matrices);
                Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
                Vec3d cameraPos = camera.getPos();

                for (Vector3f vertex : vertices) {
                    vertex.add(cameraPos.toVector3f());
                }
                vertexList.add(vertices);
            }
            for (ModelPart modelChildren : ((ModelPartAccessor) (Object) modelPart).getChildren().values()) {
                vertexList.addAll(createHitboxes(matrices, modelChildren, entity, vertexConsumers, light, excludedParts));
            }
        }
        matrices.pop();
        return vertexList;
    }

    private void scaleBabyMatices(T entity, ModelPart modelPart, MatrixStack matrices) {
        if (getContextModel() instanceof BipedEntityModel model) {
            if (modelPart.equals(model.head)) {
                matrices.scale(0.75F, 0.75F, 0.75F);
                matrices.translate(0, 1, 0);
                return;
            }
        }
        else if (getContextModel() instanceof AnimalModel model) {
            List<ModelPart> headPartList = new ArrayList<>();
            ((AnimalModelAccessor)model).getHeadParts().forEach((part)->headPartList.add((ModelPart) part));
            if (!headPartList.isEmpty()) {
                if (headPartList.get(0).equals(modelPart)) {
                    matrices.translate(0, ((AnimalModelAccessor) model).getChildHeadYOffset() / 16, ((AnimalModelAccessor) model).getChildHeadZOffset() / 16);
                    return;
                }
            }
        }
        matrices.scale(0.5F, 0.5F, 0.5F);
        matrices.translate(0, 1.5, 0);
    }

    public List<Vector3f> getCuboidHitbox(ModelPart.Cuboid cuboid, ModelPart modelPart, MatrixStack matrices) {
        float x1 = cuboid.minX / 16.0f;
        float y1 = cuboid.minY / 16.0f;
        float z1 = cuboid.minZ / 16.0f;
        float x2 = cuboid.maxX / 16.0f;
        float y2 = cuboid.maxY / 16.0f;
        float z2 = cuboid.maxZ / 16.0f;

        Vector3f[] cubeVerts = {
                new Vector3f(x1, y1, z1),
                new Vector3f(x2, y1, z1),
                new Vector3f(x1, y2, z1),
                new Vector3f(x2, y2, z1),
                new Vector3f(x1, y1, z2),
                new Vector3f(x2, y1, z2),
                new Vector3f(x1, y2, z2),
                new Vector3f(x2, y2, z2),
        };

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        for (int i = 0; i < cubeVerts.length; i++) {
            cubeVerts[i] = matrix.transformPosition(cubeVerts[i]);
        }

        return Arrays.asList(cubeVerts);
    }

    private void makePartGlow(Entity entity, VertexConsumerProvider vertexConsumers, MatrixStack matrices, int overlay, int light, ModelPart.Cuboid cuboid, ModelPart modelPart) {
        // Berechne einen pulsierenden Alpha-Wert basierend auf der Entity-Alterung (age).
        float pulse = (MathHelper.sin(entity.age * 0.25f) + 1.0f) / 2.0f; // Wert zwischen 0 und 1
        int alpha = (int) (pulse * 80) + 100; // Alpha zwischen 0 und 255

        // Definiere die Farbe: Rot pulsierend (ARGB)
        int red = 250;
        int green = 30;
        int blue = 30;
        int color = (alpha << 24) | (red << 16) | (green << 8) | blue;

        // Hole einen VertexConsumer aus einem passenden RenderLayer.
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ModRenderLayers.WEAK_SPOT_LAYER);

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
    }
}
