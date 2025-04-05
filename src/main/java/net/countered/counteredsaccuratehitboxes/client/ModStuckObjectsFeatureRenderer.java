package net.countered.counteredsaccuratehitboxes.client;

import net.countered.counteredsaccuratehitboxes.util.ModelPartProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class ModStuckObjectsFeatureRenderer <T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public ModStuckObjectsFeatureRenderer(LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    protected abstract void renderAtPart(List<Pair<ModelPart, String>> modelPartWithName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float tickDelta);

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float f, float g, float h, float j, float k, float l) {
        M entityModel = this.getContextModel();
        matrixStack.push();
        if (entityModel instanceof ModelPartProvider) {
            // Wähle einen zufälligen ModelPart aus dem EntityModel (über das implementierte Interface)
            List<Pair<ModelPart, String>> modelParts = ((ModelPartProvider) entityModel).countereds_accurate_hitboxes_template_1_21_1$getParts();

            this.renderAtPart(modelParts, matrixStack, vertexConsumerProvider, light, livingEntity, h);
        }
        matrixStack.pop();
    }
}

