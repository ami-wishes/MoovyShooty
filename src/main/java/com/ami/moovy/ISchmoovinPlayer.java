package com.ami.moovy;

import net.minecraft.util.math.Vec3d;

public interface ISchmoovinPlayer {

	boolean getWallrunning();
	Vec3d getWallNormal();

    void moovy_setVaulting(boolean state);

	void moovy_setWallrunning(boolean state);

	void moovy_setSliding(boolean state);

	void moovy_setCharge(int charge);
}
