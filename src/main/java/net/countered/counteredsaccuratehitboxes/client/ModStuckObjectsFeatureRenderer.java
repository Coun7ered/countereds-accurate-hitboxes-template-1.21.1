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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class ModStuckObjectsFeatureRenderer <T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public ModStuckObjectsFeatureRenderer(LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    protected abstract void renderAtPart(List<Pair<ModelPart, String>> modelPartWithName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float tickDelta);

    private static final List<EntityType> excludedEntities = new ArrayList<>();
    static {
        excludedEntities.add(EntityType.VILLAGER);
        excludedEntities.add(EntityType.ALLAY);
        excludedEntities.add(EntityType.BAT);
        excludedEntities.add(EntityType.CAMEL);
        excludedEntities.add(EntityType.COD);
        excludedEntities.add(EntityType.DOLPHIN);
        excludedEntities.add(EntityType.GUARDIAN);
        excludedEntities.add(EntityType.ELDER_GUARDIAN);
        excludedEntities.add(EntityType.EVOKER);
        excludedEntities.add(EntityType.FROG);
        excludedEntities.add(EntityType.GOAT);
        excludedEntities.add(EntityType.PANDA);
        excludedEntities.add(EntityType.PARROT);
        excludedEntities.add(EntityType.PHANTOM);
        excludedEntities.add(EntityType.POLAR_BEAR);
        excludedEntities.add(EntityType.RAVAGER);
        excludedEntities.add(EntityType.SNOW_GOLEM);
        excludedEntities.add(EntityType.TURTLE);
        excludedEntities.add(EntityType.VEX);
        excludedEntities.add(EntityType.VINDICATOR);
        excludedEntities.add(EntityType.WANDERING_TRADER);
        excludedEntities.add(EntityType.WITCH);
        excludedEntities.add(EntityType.BLAZE);
    }
    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float f, float g, float h, float j, float k, float l) {
        if (excludedEntities.contains(livingEntity.getType())) {
            return;
        }
        M entityModel = this.getContextModel();
        if (entityModel instanceof ModelPartProvider) {
            // Wähle einen zufälligen ModelPart aus dem EntityModel (über das implementierte Interface)
            List<Pair<ModelPart, String>> modelParts = ((ModelPartProvider) entityModel).countereds_accurate_hitboxes_template_1_21_1$getParts();

            this.renderAtPart(modelParts, matrixStack, vertexConsumerProvider, light, livingEntity, h);
        }
    }
}

