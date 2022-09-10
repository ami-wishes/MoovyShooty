package com.ami.moovy;

import net.minecraft.client.util.ColorUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

public class CancelStatusEffect extends StatusEffect {
	protected CancelStatusEffect() {
		super(StatusEffectType.HARMFUL, ColorUtil.ARGB32.getArgb(255, 123, 250, 254));
	}
}
