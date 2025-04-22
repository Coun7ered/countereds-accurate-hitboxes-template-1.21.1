package net.countered.counteredsaccuratehitboxes.client;

import net.countered.counteredsaccuratehitboxes.mixin.accessors.ModelPartAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.*;

@Environment(EnvType.CLIENT)
public abstract class ModStuckObjectsFeatureRenderer <T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public ModStuckObjectsFeatureRenderer(LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    protected abstract void renderAtPart(List<ModelPart> modelPartWithName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float tickDelta);

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float f, float g, float h, float j, float k, float l) {

        List<ModelPart> modelParts = getModelRoots(this.getContextModel());

        this.renderAtPart(modelParts, matrixStack, vertexConsumerProvider, light, livingEntity, h);
    }

    private static List<ModelPart> getModelRoots(EntityModel<?> model) {
        List<ModelPart> roots = new ArrayList<>();

        if (model instanceof SinglePartEntityModel<?> singlePartModel) {
            roots.add(singlePartModel.getPart());
            return roots;
        }

        Set<ModelPart> allParts = new HashSet<>();
        Map<ModelPart, ModelPart> childToParent = new IdentityHashMap<>();

        Class<?> currentClass = model.getClass();
        while (currentClass != null && EntityModel.class.isAssignableFrom(currentClass)) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (ModelPart.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        ModelPart part = (ModelPart) field.get(model);

                        if (field.getName().equals("cloak") || field.getName().equals("ear")) {
                            continue;
                        }
                        if (part != null && allParts.add(part)) {
                            collectChildrenRecursive(part, childToParent);
                        }
                    } catch (IllegalAccessException ignored) {}
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        for (ModelPart part : allParts) {
            if (!childToParent.containsKey(part)) {
                roots.add(part);
            }
        }

        return roots;
    }
    private static void collectChildrenRecursive(ModelPart parent, Map<ModelPart, ModelPart> childToParent) {
        for (ModelPart child : ((ModelPartAccessor) (Object) parent).getChildren().values()) {
            childToParent.put(child, parent);
            collectChildrenRecursive(child, childToParent);
        }
    }
}

