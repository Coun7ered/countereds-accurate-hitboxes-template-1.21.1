package net.countered.counteredsaccuratehitboxes.mixin.client.entitymodels;

import net.countered.counteredsaccuratehitboxes.util.ModelPartProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public abstract class ModBipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead, ModelPartProvider {

    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;
    @Unique
    private List<Pair<ModelPart, String>> partTypes; // Speichert die Kategorie jedes Parts

    // Methode zum Initialisieren der parts-Liste
    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelPart;Ljava/util/function/Function;)V", at = @At("TAIL"))
    private void onInit(ModelPart root, Function renderLayerFactory, CallbackInfo ci) {
        this.partTypes = new ArrayList<>();
        partTypes.add(new Pair<>(this.head, EntityModelPartNames.HEAD));
        partTypes.add(new Pair<>(this.body, EntityModelPartNames.BODY));
        partTypes.add(new Pair<>(this.rightArm, EntityModelPartNames.RIGHT_ARM));
        partTypes.add(new Pair<>(this.leftArm, EntityModelPartNames.LEFT_ARM));
        partTypes.add(new Pair<>(this.rightLeg, EntityModelPartNames.RIGHT_LEG));
        partTypes.add(new Pair<>(this.leftLeg, EntityModelPartNames.LEFT_LEG));
    }

    // Methode zum Zufällig Auswählen eines Modellteils
    @Override
    public List<Pair<ModelPart, String>> countereds_accurate_hitboxes_template_1_21_1$getParts() {
        return partTypes; // Zufälligen Key auswählen
    }
}
