package net.countered.counteredsaccuratehitboxes.mixin.server;

import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.countered.counteredsaccuratehitboxes.util.ModInteraction;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ModServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler
        implements ServerPlayPacketListener,
        PlayerAssociatedNetworkHandler,
        TickablePacketListener {

    @Shadow public ServerPlayerEntity player;

    public ModServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }
    /*
    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"), cancellable = true)
    public void onPlayerInteractEntityCustom(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler self = (ServerPlayNetworkHandler)(Object)this;
        ServerPlayerEntity player = self.player;
        ServerWorld serverWorld = player.getServerWorld();
        Entity entity = packet.getEntity(serverWorld);

        List<Box> hitboxes = entity.getAttached(HitboxAttachment.HITBOXES);

        if (hitboxes == null || hitboxes.isEmpty()) {
            return;
        }
        for (Box box : hitboxes) {
            if (this.player.canInteractWithEntityIn(box, 1.0)) {
                packet.handle(
                        new PlayerInteractEntityC2SPacket.Handler() {
                            private void processInteract(Hand hand, ModInteraction action) {
                                ItemStack itemStack = self.player.getStackInHand(hand);
                                if (itemStack.isItemEnabled(serverWorld.getEnabledFeatures())) {
                                    ItemStack itemStack2 = itemStack.copy();
                                    ActionResult actionResult = action.run(self.player, entity, hand);
                                    if (actionResult.isAccepted()) {
                                        Criteria.PLAYER_INTERACTED_WITH_ENTITY
                                                .trigger(self.player, actionResult.shouldIncrementStat() ? itemStack2 : ItemStack.EMPTY, entity);
                                        if (actionResult.shouldSwingHand()) {
                                            self.player.swingHand(hand, true);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void interact(Hand hand) {
                                this.processInteract(hand, PlayerEntity::interact);
                            }

                            @Override
                            public void interactAt(Hand hand, Vec3d pos) {
                                this.processInteract(hand, (player, entityxx, handx) -> entityxx.interactAt(player, pos, handx));
                            }

                            @Override
                            public void attack() {
                                if (!(entity instanceof ItemEntity)
                                        && !(entity instanceof ExperienceOrbEntity)
                                        && entity != self.player
                                        && !(entity instanceof PersistentProjectileEntity persistentProjectileEntity && !persistentProjectileEntity.isAttackable())) {
                                    ItemStack itemStack = self.player.getStackInHand(Hand.MAIN_HAND);
                                    if (itemStack.isItemEnabled(serverWorld.getEnabledFeatures())) {
                                        self.player.attack(entity);
                                    }
                                } else {
                                    //self.disconnect(Text.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                                    System.out.println("Player tried to attack an invalid entity");
                                }
                            }
                        }
                );
                ci.cancel();
                return;
            }
        }
        ci.cancel();
    }

     */
}


