package com.ami.moovy.mixins;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

	@ModifyVariable(at = @At("STORE"), method = "onPlayerMove", ordinal = 2)
	private boolean modifyCheatDetector(boolean var){
		return false;
	}

}
