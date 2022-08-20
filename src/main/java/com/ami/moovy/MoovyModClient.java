package com.ami.moovy;

import net.minecraft.entity.player.PlayerEntity;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class MoovyModClient extends MoovyMod implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {

		MoovyMod.updater = new IMoovyPlayerUpdater() {
			@Override
			public void setCharge(PlayerEntity entity, int count) {
				var buf = PacketByteBufs.create();
				buf.writeInt(count);
				ClientPlayNetworking.send(MoovyMod.setCharge, buf);
			}

			@Override
			public void setSliding(PlayerEntity entity, boolean state) {
				var buf = PacketByteBufs.create();
				buf.writeBoolean(state);
				ClientPlayNetworking.send(MoovyMod.setSliding, buf);
			}

			@Override
			public void setWallrunning(PlayerEntity entity, boolean state) {
				var buf = PacketByteBufs.create();
				buf.writeBoolean(state);
				ClientPlayNetworking.send(MoovyMod.setWallrunning, buf);
			}

			@Override
			public void setVaulting(PlayerEntity entity, boolean state) {
				var buf = PacketByteBufs.create();
				buf.writeBoolean(state);
				ClientPlayNetworking.send(MoovyMod.setVaulting, buf);
			}
		};

		ClientPlayNetworking.registerGlobalReceiver(MoovyMod.setVaulting, (client, handler, buf, responseSender) -> {
			var uuid = buf.readUuid();
			var value = buf.readBoolean();

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_setVaulting(value);
			} catch (Exception e) {
				// -- ignore
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(MoovyMod.setSliding, (client, handler, buf, responseSender) -> {
			var uuid = buf.readUuid();
			var value = buf.readBoolean();

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_setSliding(value);
			} catch (Exception e) {
				// -- ignore
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(MoovyMod.setWallrunning, (client, handler, buf, responseSender) -> {
			var uuid = buf.readUuid();
			var value = buf.readBoolean();

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_setWallrunning(value);
			} catch (Exception e) {
				// -- ignore
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(MoovyMod.setCharge, (client, handler, buf, responseSender) -> {
			var uuid = buf.readUuid();
			var value = buf.readInt();

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_setCharge(value);
			} catch (Exception e) {
				// -- ignore
			}
		});
	}
}
