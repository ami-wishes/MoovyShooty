package com.ami.moovy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
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
			public void setWallrunning(PlayerEntity entity, boolean state, Vec3d normal) {
				var buf = PacketByteBufs.create();
				buf.writeBoolean(state);
				buf.writeDouble(normal.x);
				buf.writeDouble(normal.y);
				buf.writeDouble(normal.z);
				ClientPlayNetworking.send(MoovyMod.setWallrunning, buf);
			}

			@Override
			public void setVaulting(PlayerEntity entity, boolean state) {
				var buf = PacketByteBufs.create();
				buf.writeBoolean(state);
				ClientPlayNetworking.send(MoovyMod.setVaulting, buf);
			}

			@Override
			public void setBoostTimer(PlayerEntity entity, int count) {
				var buf = PacketByteBufs.create();
				buf.writeInt(count);
				ClientPlayNetworking.send(MoovyMod.setBoostTimer, buf);
			}

			@Override
			public void setBoostVisualTimer(PlayerEntity entity, int count) {
				var buf = PacketByteBufs.create();
				buf.writeInt(count);
				ClientPlayNetworking.send(MoovyMod.setBoostVisualTimer, buf);
			}

			@Override
			public void spawnWalljumpParticles(PlayerEntity entity) {
				var buf = PacketByteBufs.create();
				ClientPlayNetworking.send(MoovyMod.spawnWallrunningParticles, buf);
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
			var normal = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_setWallrunning(value, normal);
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

		ClientPlayNetworking.registerGlobalReceiver(MoovyMod.setBoostTimer, (client, handler, buf, responseSender) -> {
			var uuid = buf.readUuid();
			var value = buf.readInt();

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_setBoostTimer(value);
			} catch (Exception e) {
				// -- ignore
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(MoovyMod.setBoostVisualTimer, (client, handler, buf, responseSender) -> {
			var uuid = buf.readUuid();
			var value = buf.readInt();

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_setBoostVisualTimer(value);
			} catch (Exception e) {
				// -- ignore
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(MoovyMod.spawnWallrunningParticles, (client, handler, buf, responseSender) -> {
			var uuid = buf.readUuid();

			try {
				var schmoovin = (ISchmoovinPlayer) client.world.getPlayerByUuid(uuid);
				schmoovin.moovy_spawnWallrunningParticles();
			} catch (Exception e) {
				// -- ignore
			}
		});
	}
}
