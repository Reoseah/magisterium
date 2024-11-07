package io.github.reoseah.magisterium.mixin.client;

import io.github.reoseah.magisterium.data.SpellPageLoader;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin(ReloadableResourceManagerImpl.class)
//public class ReloadableResourceManagerImplMixin {
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void onInit(CallbackInfo info) {
//        System.out.println("Registering spell page loader");
//        ((ReloadableResourceManagerImpl) (Object) this).registerReloader(new SpellPageLoader());
//    }
//}
