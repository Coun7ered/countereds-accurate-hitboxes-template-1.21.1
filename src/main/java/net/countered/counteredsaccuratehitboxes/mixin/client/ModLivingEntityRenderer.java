package net.countered.counteredsaccuratehitboxes.mixin.client;

import net.countered.counteredsaccuratehitboxes.client.HitboxFeatureRenderer;
import net.countered.counteredsaccuratehitboxes.mixin.accessors.ModLivingEntityRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class ModLivingEntityRenderer <T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityRendererFactory.Context ctx, EntityModel model, float shadowRadius, CallbackInfo ci) {
        ModLivingEntityRendererAccessor<T, M> accessor = (ModLivingEntityRendererAccessor<T, M>) (Object) this;
        accessor.invokeAddFeature(new HitboxFeatureRenderer<>(ctx, (LivingEntityRenderer<T, M>) (Object) this));
    }
}
