package com.ami.moovy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class MoovyMod implements ModInitializer {
	public static final Identifier setVaulting = new Identifier("moovy", "setvaulting");
	public static final Identifier setSliding = new Identifier("moovy", "setsliding");
	public static final Identifier setWallrunning = new Identifier("moovy", "setwallrunning");
	public static final Identifier setCharge = new Identifier("moovy", "setcharge");

	public static IMoovyPlayerUpdater updater;


	@Override
	public void onInitialize(ModContainer mod) {

		updater = new IMoovyPlayerUpdater() {
			@Override
			public void setCharge(PlayerEntity entity, int count) {
			}

			@Override
			public void setSliding(PlayerEntity entity, boolean state) {
			}

			@Override
			public void setWallrunning(PlayerEntity entity, boolean state) {
			}

			@Override
			public void setVaulting(PlayerEntity entity, boolean state) {
			}
		};

		ServerPlayNetworking.registerGlobalReceiver(setVaulting, (server, player, handler, buf, responseSender) -> {
			boolean state = buf.readBoolean();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setVaulting(state);

			//Send packet to all players but ourself
			for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
				if (serverPlayerEntity == player) continue;

				if (serverPlayerEntity.canSee(player)) {
					var wBuf = PacketByteBufs.create();
					wBuf.writeUuid(player.getUuid());
					wBuf.writeBoolean(state);
					ServerPlayNetworking.send(serverPlayerEntity, setVaulting, wBuf);
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setSliding, (server, player, handler, buf, responseSender) -> {
			boolean state = buf.readBoolean();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setSliding(state);

			//Send packet to all players but ourself
			for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
				if (serverPlayerEntity == player) continue;

				if (serverPlayerEntity.canSee(player)) {
					var wBuf = PacketByteBufs.create();
					wBuf.writeUuid(player.getUuid());
					wBuf.writeBoolean(state);
					ServerPlayNetworking.send(serverPlayerEntity, setSliding, wBuf);
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setWallrunning, (server, player, handler, buf, responseSender) -> {
			boolean state = buf.readBoolean();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setWallrunning(state);

			//Send packet to all players but ourself
			for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
				if (serverPlayerEntity == player) continue;

				if (serverPlayerEntity.canSee(player)) {
					var wBuf = PacketByteBufs.create();
					wBuf.writeUuid(player.getUuid());
					wBuf.writeBoolean(state);
					ServerPlayNetworking.send(serverPlayerEntity, setWallrunning, wBuf);
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(setCharge, (server, player, handler, buf, responseSender) -> {
			int charge = buf.readInt();

			var schmoovyPlayer = (ISchmoovinPlayer) player;
			schmoovyPlayer.moovy_setCharge(charge);

			//Send packet to all players but ourself
			for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
				if (serverPlayerEntity == player) continue;

				if (serverPlayerEntity.canSee(player)) {
					var wBuf = PacketByteBufs.create();
					wBuf.writeUuid(player.getUuid());
					wBuf.writeInt(charge);
					ServerPlayNetworking.send(serverPlayerEntity, setWallrunning, wBuf);
				}
			}
		});
	}
}
