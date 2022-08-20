package com.ami.moovy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public interface IMoovyPlayerUpdater {

	void setCharge(PlayerEntity entity, int count);
	void setSliding(PlayerEntity entity, boolean state);
	void setWallrunning(PlayerEntity entity, boolean state, Vec3d normal);
	void setVaulting(PlayerEntity entity, boolean state);
	void setBoostTimer(PlayerEntity entity, int count);
	void setBoostVisualTimer(PlayerEntity entity, int count);

	void spawnWalljumpParticles(PlayerEntity entity);
}
