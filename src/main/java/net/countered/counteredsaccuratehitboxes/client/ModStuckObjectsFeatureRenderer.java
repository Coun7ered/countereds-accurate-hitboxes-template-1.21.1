package net.countered.counteredsaccuratehitboxes.client;

import net.countered.counteredsaccuratehitboxes.mixin.accessors.ModelPartAccessor;
import net.countered.counteredsaccuratehitboxes.util.ModelPartProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Pair;

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

    public static List<ModelPart> getModelRoots(EntityModel<?> model) {
        List<ModelPart> roots = new ArrayList<>();

        if (model instanceof SinglePartEntityModel<?> singlePartModel) {
            roots.add(singlePartModel.getPart()); // einzig wahrer Root
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
                        if (part != null && allParts.add(part)) {
                            for (ModelPart child : ((ModelPartAccessor) (Object) part).getChildren().values()) {
                                childToParent.put(child, part);
                            }
                        }
                    } catch (IllegalAccessException ignored) {}
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        // Root-Parts = alle Parts, die **keinen** Parent in der Map haben
        for (ModelPart part : allParts) {
            if (!childToParent.containsKey(part)) {
                roots.add(part);
            }
        }

        return roots;
    }



}

