package com.ami.moovy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.LinkedList;
import java.util.Queue;

public class MoovyMod implements ModInitializer {
	public static final Identifier setVaulting = new Identifier("moovy", "setvaulting");
	public static final Identifier setSliding = new Identifier("moovy", "setsliding");
	public static final Identifier setWallrunning = new Identifier("moovy", "setwallrunning");
	public static final Identifier setCharge = new Identifier("moovy", "setcharge");
	public static final Identifier setBoostTimer = new Identifier("moovy", "setboosttimer");
	public static final Identifier setBoostVisualTimer = new Identifier("moovy", "setboostvisualtimer");
	public static final Identifier spawnWallrunningParticles = new Identifier("moovy", "spawnwallrunningparticles");


	private static final Object _queueLock = new Object();
	private static final Queue<Runnable> _actionQueue = new LinkedList<>();

	public static IMoovyPlayerUpdater updater;

	public static final CancelStatusEffect CANCEL_STATUS_EFFECT = new CancelStatusEffect();


	@Override
	public void onInitialize(ModContainer mod) {

		Registry.register(Registry.STATUS_EFFECT, new Identifier("moovy", "cancel"), CANCEL_STATUS_EFFECT);

		updater = new IMoovyPlayerUpdater() {
			@Override
			public void setCharge(PlayerEntity entity, int count) {
			}

			@Override
			public void setSliding(PlayerEntity entity, boolean state) {
			}

			@Override
			public void setWallrunning(PlayerEntity entity, boolean state, Vec3d normal) {
			}

			@Override
			public void setVaulting(PlayerEntity entity, boolean state) {
			}

			@Override
			public void setBoostTimer(PlayerEntity entity, int count) {
			}

			@Override
			public void setBoostVisualTimer(PlayerEntity entity, int count) {

			}

			@Override
			public void spawnWalljumpParticles(PlayerEntity entity) {

			}
		};

		ServerTickEvents.END.register((server) -> {
			synchronized (_queueLock) {
				while (_actionQueue.size() > 0) {
					_actionQueue.poll().run();
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setVaulting, (server, player, handler, buf, responseSender) -> {
			boolean state = buf.readBoolean();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setVaulting(state);

			synchronized (_queueLock) {
				_actionQueue.add(() -> {
					//Send packet to all players but ourself
					for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
						if (serverPlayerEntity == player)
							continue;

						if (serverPlayerEntity.canSee(player)) {
							var wBuf = PacketByteBufs.create();
							wBuf.writeUuid(player.getUuid());
							wBuf.writeBoolean(state);
							ServerPlayNetworking.send(serverPlayerEntity, setVaulting, wBuf);
						}
					}
				});
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setSliding, (server, player, handler, buf, responseSender) -> {
			boolean state = buf.readBoolean();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setSliding(state);

			synchronized (_queueLock) {
				_actionQueue.add(() -> {
					//Send packet to all players but ourself
					for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
						if (serverPlayerEntity == player)
							continue;

						if (serverPlayerEntity.canSee(player)) {
							var wBuf = PacketByteBufs.create();
							wBuf.writeUuid(player.getUuid());
							wBuf.writeBoolean(state);
							ServerPlayNetworking.send(serverPlayerEntity, setSliding, wBuf);
						}
					}
				});
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setWallrunning, (server, player, handler, buf, responseSender) -> {
			boolean state = buf.readBoolean();
			Vec3d normal = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setWallrunning(state, normal);

			synchronized (_queueLock) {
				_actionQueue.add(() -> {
					//Send packet to all players but ourself
					for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
						if (serverPlayerEntity == player)
							continue;

						if (serverPlayerEntity.canSee(player)) {
							var wBuf = PacketByteBufs.create();
							wBuf.writeUuid(player.getUuid());
							wBuf.writeBoolean(state);
							wBuf.writeDouble(normal.x);
							wBuf.writeDouble(normal.y);
							wBuf.writeDouble(normal.z);

							ServerPlayNetworking.send(serverPlayerEntity, setWallrunning, wBuf);
						}
					}
				});
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setCharge, (server, player, handler, buf, responseSender) -> {
			int charge = buf.readInt();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setCharge(charge);

			synchronized (_queueLock) {
				_actionQueue.add(() -> {
					//Send packet to all players but ourself
					for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
						if (serverPlayerEntity == player)
							continue;

						if (serverPlayerEntity.canSee(player)) {
							var wBuf = PacketByteBufs.create();
							wBuf.writeUuid(player.getUuid());
							wBuf.writeInt(charge);
							ServerPlayNetworking.send(serverPlayerEntity, setCharge, wBuf);
						}
					}
				});
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setBoostTimer, (server, player, handler, buf, responseSender) -> {
			int charge = buf.readInt();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setBoostTimer(charge);

			synchronized (_queueLock) {
				_actionQueue.add(() -> {
					//Send packet to all players but ourself
					for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
						if (serverPlayerEntity == player)
							continue;

						if (serverPlayerEntity.canSee(player)) {
							var wBuf = PacketByteBufs.create();
							wBuf.writeUuid(player.getUuid());
							wBuf.writeInt(charge);
							ServerPlayNetworking.send(serverPlayerEntity, setBoostTimer, wBuf);
						}
					}
				});
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setBoostVisualTimer, (server, player, handler, buf, responseSender) -> {
			int charge = buf.readInt();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setBoostVisualTimer(charge);

			synchronized (_queueLock) {
				_actionQueue.add(() -> {
					//Send packet to all players but ourself
					for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
						if (serverPlayerEntity == player)
							continue;

						if (serverPlayerEntity.canSee(player)) {
							var wBuf = PacketByteBufs.create();
							wBuf.writeUuid(player.getUuid());
							wBuf.writeInt(charge);
							ServerPlayNetworking.send(serverPlayerEntity, setBoostVisualTimer, wBuf);
						}
					}
				});
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(spawnWallrunningParticles, (server, player, handler, buf, responseSender) -> {
			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_spawnWallrunningParticles();

			synchronized (_queueLock) {
				_actionQueue.add(() -> {
					//Send packet to all players but ourself
					for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
						if (serverPlayerEntity == player)
							continue;

						if (serverPlayerEntity.canSee(player)) {
							var wBuf = PacketByteBufs.create();
							wBuf.writeUuid(player.getUuid());
							ServerPlayNetworking.send(serverPlayerEntity, spawnWallrunningParticles, wBuf);
						}
					}
				});
			}
		});
	}
}
