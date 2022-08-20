package com.ami.moovy;

import net.minecraft.entity.player.PlayerEntity;

public interface IMoovyPlayerUpdater {

	void setCharge(PlayerEntity entity, int count);
	void setSliding(PlayerEntity entity, boolean state);
	void setWallrunning(PlayerEntity entity, boolean state);
	void setVaulting(PlayerEntity entity, boolean state);

}
