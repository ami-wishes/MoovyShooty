package com.ami.moovy.mixins;

import com.ami.moovy.ISchmoovinPlayer;
import com.ami.moovy.MoovyMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements ISchmoovinPlayer {

	@Shadow
	public abstract PlayerAbilities getAbilities();

	@Shadow
	public abstract boolean damage(DamageSource source, float amount);


	@Shadow
	protected abstract void closeHandledScreen();

	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);

	@Shadow
	protected abstract float getVelocityMultiplier();

	private int moovy_boostTimer = 0;
	private double moovy_boostForce = 0;

	private boolean moovy_wallrunning = false;
	private boolean moovy_sliding = false;
	private boolean moovy_isVaulting = false;

	private boolean moovy_wasWallrunning = false;
	private boolean moovy_wasSliding = false;
	private boolean moovy_wasVaulting = false;
	private boolean moovy_canVault = false;

	private Vec3d moovy_wallrunVel;
	private Vec3d moovy_wallRunNormal;

	private boolean moovy_wasJumping = false;
	private boolean moovy_wasSneaking = false;
	private boolean moovy_wasSprinting = false;


	private int moovy_boostVisualTimer = 0;
	private int moovy_boostCharges = 3;
	private int moovy_prevBoostCharges = 3;

	private int moovy_boostCooldown = 0;


	private Vec3d moovy_vaultSpeed;
	private Vec3d moovy_vaultNormal;
	private float moovy_vaultTargetY;
	private Vec3d moovy_prevVel;
	private boolean moovy_prevHorizontalCollision;


	private int moovy_wallrunStickTimer = 0;

	private static final Vec3d[] moovy_wallrunAngles = new Vec3d[]{new Vec3d(1, 0, 0), new Vec3d(0, 0, 1), new Vec3d(-1, 0, 0), new Vec3d(0, 0, -1),};

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "jump", at = @At("HEAD"))
	public void moovy_jumpHead(CallbackInfo ci) {
		moovy_wasSprinting = isSprinting();
		if (isSneaking()) setSprinting(false);
	}

	@Inject(method = "jump", at = @At("RETURN"))
	public void moovy_jumpReturn(CallbackInfo ci) {
		setSprinting(moovy_wasSprinting);

		//Slide-jump
		if (moovy_sliding && !moovy_wasJumping) {
			var vel = getVelocity();
			var flat = new Vec3d(vel.x, 0, vel.z);

			setVelocity(flat.add(0, MathHelper.clamp((vel.length() * vel.length()) * 0.7f, 0.5, 0.9f), 0));
		}
	}

	@Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
	public void moovy_handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
		if (moovy_boostTimer > 0) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}

	@Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
	public void updatePose(CallbackInfo ci) {
		if (moovy_sliding) {
			setPose(EntityPose.SWIMMING);
			ci.cancel();
		}
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	public void travel(Vec3d movementInput, CallbackInfo ci) {

		//If touching liquids, or fall-flying, use vanilla
		FluidState fluidState = this.world.getFluidState(this.getBlockPos());
		if ((isUsingItem()) || (this.getAbilities().flying) || (this.isTouchingWater() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState)) || (this.isInLava() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState)) || (this.isFallFlying()))
			return;

		if (isClimbing())
			return;

		if (!EnchantmentHelper.hasSoulSpeed(this)) return;

		if (!world.isClient) {
			if (moovy_boostTimer > 0)
				moovy_boostTimer--;
		} else {

			int maxCharges = EnchantmentHelper.getEquipmentLevel(Enchantments.SOUL_SPEED, this);
			//Recharge boost
			if (moovy_boostCharges < maxCharges && moovy_boostCooldown < 50) {
				moovy_boostCooldown++;

				if (moovy_boostCooldown == 50) {
					moovy_boostCooldown = 0;
					moovy_boostCharges++;
				}
			}

			if (moovy_isVaulting) {
				moovy_vault();
			} else if (moovy_wallrunning) {
				moovy_wallrunning(movementInput);
			} else {
				moovy_newMovement(movementInput);
			}


			//Update packets
			{
				if (moovy_prevBoostCharges != moovy_boostCharges) {
					MoovyMod.updater.setCharge((PlayerEntity) (Object) this, moovy_boostCharges);
				}

				if (moovy_wasSliding != moovy_sliding) {
					MoovyMod.updater.setSliding((PlayerEntity) (Object) this, moovy_sliding);
				}

				if (moovy_wasWallrunning != moovy_wallrunning) {
					MoovyMod.updater.setWallrunning((PlayerEntity) (Object) this, moovy_wallrunning, moovy_wallRunNormal);
				}

				if (moovy_wasVaulting != moovy_isVaulting) {
					MoovyMod.updater.setVaulting((PlayerEntity) (Object) this, moovy_isVaulting);
				}
			}


			//Do this because we cancelled the movement code
			updateLimbs(this, this instanceof Flutterer);

			//Store for next frame
			moovy_wasJumping = jumping;
			moovy_wasSneaking = isSneaking();

			//If we collided this frame, ignore, unless we collided last frame too.
			if (!horizontalCollision || moovy_prevHorizontalCollision)
				moovy_prevVel = getVelocity();

			moovy_prevHorizontalCollision = horizontalCollision;

			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo ci) {
		Vec3d pos = getPos();

		{
			int maxCharges = EnchantmentHelper.getEquipmentLevel(Enchantments.SOUL_SPEED, this);

			//Spawn boost recharge particles

			if (moovy_boostCharges < maxCharges) {
				if (moovy_boostCharges == 0) {
					if (age % 5 == 0) {
						world.addParticle(ParticleTypes.SMOKE, pos.x + (random.nextDouble() - 0.5f), pos.y + 0.1f, pos.z + (random.nextDouble() - 0.5f), 0, random.nextDouble() * 0.05f, 0);
					}
				} else if (moovy_boostCharges == 1) {
					if (age % 5 == 0) {
						world.addParticle(ParticleTypes.SMALL_FLAME, pos.x + (random.nextDouble() - 0.5f), pos.y + 0.1f, pos.z + (random.nextDouble() - 0.5f), 0, random.nextDouble() * 0.05f, 0);
					}
				} else if (moovy_boostCharges == 2) {
					if (age % 5 == 0) {
						world.addParticle(ParticleTypes.FLAME, pos.x + (random.nextDouble() - 0.5f), pos.y + 0.1f, pos.z + (random.nextDouble() - 0.5f), 0, random.nextDouble() * 0.05f, 0);
					}
				}
			}

			//Spawn soul flames when boosting
			if (moovy_boostVisualTimer > 0) {
				moovy_boostVisualTimer--;

				Vec3d right = pos.add(Math.sin(Math.toRadians(getYaw()) + Math.PI / 2.0f) * 0.2f, 0, -Math.cos(Math.toRadians(getYaw()) + Math.PI / 2.0f) * 0.2f);
				Vec3d left = pos.subtract(Math.sin(Math.toRadians(getYaw()) + Math.PI / 2.0f) * 0.2f, 0, -Math.cos(Math.toRadians(getYaw()) + Math.PI / 2.0f) * 0.2f);

				world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, right.x, right.y + 0.1f, right.z, 0, 0.1, 0);
				world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, left.x, left.y + 0.1f, left.z, 0, 0.1, 0);
			}

			//Putter for out of charges
			if (moovy_prevBoostCharges > 0 && moovy_boostCharges == 0) {
				for (int i = 0; i < 16; i++) {
					world.addParticle(ParticleTypes.SMOKE,
							pos.x + (random.nextDouble() - 0.5f) * 0.5f, pos.y, pos.z + (random.nextDouble() - 0.5f) * 0.5f,
							(random.nextDouble() - 0.5f) * 0.1f, 0.05f, (random.nextDouble() - 0.5f) * 0.1f
					);
				}
			} else if (moovy_prevBoostCharges != moovy_boostCharges && moovy_boostCharges == maxCharges) {
				for (int i = 0; i < 16; i++) {
					world.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
							pos.x + (random.nextDouble() - 0.5f) * 0.5f, pos.y, pos.z + (random.nextDouble() - 0.5f) * 0.5f,
							(random.nextDouble() - 0.5f) * 0.1f, 0.05f, (random.nextDouble() - 0.5f) * 0.1f
					);
				}
			}

			if (moovy_wallrunning && moovy_wallRunNormal != null) {
				//Spawn particles on wall
				world.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
						(getPos().x + (random.nextDouble() - 0.5f) * 0.1f) - (moovy_wallRunNormal.x * 0.15),
						getPos().y + 0.1f,
						(getPos().z + (random.nextDouble() - 0.5f) * 0.1f) - (moovy_wallRunNormal.z * 0.15),

						(random.nextDouble() - 0.5f) * 0.06f, 0.05f, (random.nextDouble() - 0.5f) * 0.06f
				);
			}
		}

		moovy_prevBoostCharges = moovy_boostCharges;
		moovy_wasSliding = moovy_sliding;
		moovy_wasWallrunning = moovy_wallrunning;
		moovy_wasVaulting = moovy_isVaulting;
	}

	public void moovy_newMovement(Vec3d movementInput) {

		//Gravity calculation
		double d = 0.08;
		boolean bl = this.getVelocity().y <= 0.0;
		if (bl && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
			d = 0.01;
			this.onLanding();
		}

		//Boost state decay
		if (moovy_boostTimer > 0) moovy_boostTimer--;

		//If player starts sneaking in air, set to boost state
		if (!moovy_wasSneaking && isSneaking() && !onGround && moovy_boostTimer == 0 && moovy_boostCharges > 0) {

			//Lower vertical velocity = higher boost
			moovy_boostTimer = 50;

			MoovyMod.updater.setBoostTimer((PlayerEntity) (Object) this, 50);
		}

		//Grab common values
		Vec3d correctedInput = moovy_movementInputToVelocity(movementInput, 1, getYaw());
		Vec3d normedInput = correctedInput.normalize();
		Vec3d velocity = getVelocity();
		Vec3d flat = new Vec3d(velocity.x, 0, velocity.z);
		float speed = isSprinting() ? 0.3f : 0.22f;

		if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
			speed = 0.96F;
		}

		float speedThreshold = (speed * speed) + (speed * speed);

		//If on the ground...
		if (onGround) {
			moovy_canVault = true;

			BlockPos blockPos = this.getVelocityAffectingPos();
			float slipperiness = (this.world.getBlockState(blockPos).getBlock().getSlipperiness() * 0.9f) + 0.05f;

			moovy_sliding = false;
			moovy_wallrunStickTimer = 12;


			if (isSneaking() && flat.lengthSquared() > speedThreshold * 0.3f) {
				// -- Sliding --

				//Boost
				if (moovy_boostTimer > 0) {
					moovy_boostTimer = 0;
					moovy_boostCharges--;
					moovy_boostVisualTimer += 4;

					//Get velocity's addition to boost
					double velocityBias = (-moovy_prevVel.y) - 0.5f;
					velocityBias = MathHelper.clamp(velocityBias, 0, 5);

					moovy_boostForce = 1 + (velocityBias * 2.0f);

					velocity = velocity.add(normedInput.x * 0.31 * moovy_boostForce, 0, normedInput.z * 0.31 * moovy_boostForce);
					flat = new Vec3d(velocity.x, 0, velocity.z);
				}

				var normd = flat.normalize();
				var len = flat.length() * 0.98f; //Drag

				//Calculate the angle we want to travel at, and the angle we are actually travelling at.
				double actualAngle = Math.toDegrees(Math.atan2(normd.x, normd.z));
				double desiredAngle = Math.toDegrees(Math.atan2(correctedInput.x, correctedInput.z));
				if (correctedInput.lengthSquared() < 0.001f)
					desiredAngle = actualAngle;

				//Move from current angle towards desired angle, decreasing control as the velocity increases.
				double newAngle = MathHelper.lerpAngleDegrees((float) MathHelper.lerp(MathHelper.clamp((2 + len) / 12, 0, 1), 0.15f, 0.05f), (float) actualAngle, (float) desiredAngle);

				//Set velocity.
				velocity = new Vec3d(Math.sin(Math.toRadians(newAngle)) * len, velocity.y, Math.cos(Math.toRadians(newAngle)) * len);
				velocity = new Vec3d(
						velocity.x * MathHelper.clamp(0.85 + (slipperiness * slipperiness * slipperiness), 0, 1),
						velocity.y,
						velocity.z * MathHelper.clamp(0.85 + (slipperiness * slipperiness * slipperiness), 0, 1)
				);

				moovy_sliding = true;
			} else {
				// -- Non-Sliding -- //
				moovy_boostTimer = 0;

				//Just move velocity towards desired velocity based on slipperiness.
				velocity = new Vec3d(MathHelper.lerp(1 - (slipperiness * slipperiness), velocity.x, correctedInput.x * speed), velocity.y, MathHelper.lerp(1 - (slipperiness * slipperiness), velocity.z, correctedInput.z * speed));
			}
		} else { // If in the air...
			//Try to detect wallrunning starting
			if (moovy_couldWallrun(flat)) {

				//Start wallrunning
				moovy_wallrunning = true;
				//Reset vertical velocity, helps with control.
				velocity = new Vec3d(velocity.x, MathHelper.clamp(velocity.y, -0.05f, 0.2f), velocity.z);
				moovy_wallrunVel = velocity;
			}

			//Check for vault
			if (isSneaking() && moovy_canVault) {
				Vec3d lookDir = new Vec3d(-Math.sin(Math.toRadians(getYaw())), 0, Math.cos(Math.toRadians(getYaw())));

				Vec3d eyePos = getEyePos();
				Vec3d kneePos = getPos();

				Vec3d eyeEnd = eyePos.add(lookDir.multiply(1));
				Vec3d kneeEnd = kneePos.add(lookDir.multiply(1));

				var hitA = this.world.raycast(new RaycastContext(eyePos, eyeEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
				var hitB = this.world.raycast(new RaycastContext(kneePos, kneeEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

				//If eyes missed, but knees hit, we're up against a ledge.
				if (hitA.getType() == HitResult.Type.MISS && hitB.getType() == HitResult.Type.BLOCK) {
					moovy_isVaulting = true;
					moovy_vaultSpeed = moovy_prevVel;
					moovy_vaultNormal = new Vec3d(hitB.getSide().getUnitVector()).negate();
					moovy_vaultTargetY = (float) Math.floor(getEyeY());
					moovy_canVault = false;

					moovy_emitVaultParticles();
				}
			}

			// -- Normal Movement -- //

			//If movement is below the normal movement speed, get 'normal' air control.
			if (flat.lengthSquared() <= speedThreshold) {
				velocity = new Vec3d(MathHelper.lerp(0.15, velocity.x, correctedInput.x * speed), velocity.y, MathHelper.lerp(0.15, velocity.z, correctedInput.z * speed));
			} else if (correctedInput.lengthSquared() > 0.001f) {
				//If movement is too fast, and we're inputting a value, turn our velocity towards that value.
				//NOTE - Does not have drag, maybe it should?
				var normd = flat.normalize();
				var len = flat.length();

				double desiredAngle = Math.toDegrees(Math.atan2(correctedInput.x, correctedInput.z));
				double actualAngle = Math.toDegrees(Math.atan2(normd.x, normd.z));

				double newAngle = MathHelper.lerpAngleDegrees((float) MathHelper.lerp(MathHelper.clamp((2 + len) / 12, 0, 1), 0.15f, 0.01f), (float) actualAngle, (float) desiredAngle);

				velocity = new Vec3d(Math.sin(Math.toRadians(newAngle)) * len, velocity.y, Math.cos(Math.toRadians(newAngle)) * len);

				if (isSneaking())
					moovy_sliding = true;
			}
		}

		//Set the velocity to the value we've modified with movement code.
		setVelocity(velocity);
		//Apply gravity. (MAGIC)
		velocity = new Vec3d(velocity.x, velocity.y - d * 0.87f, velocity.z);

		//Vertical drag (MAGIC)
		setVelocity(new Vec3d(velocity.x, velocity.y * 0.99f, velocity.z));
		// Scale vertical velocity for movement (MAGIC!!!!)
		Vec3d finalVel = new Vec3d(velocity.x, velocity.y * 1.14f, velocity.z);
		this.move(MovementType.SELF, finalVel);
	}

	public void moovy_vault() {

		//Cancel vault if you let go of shift
		if (!isSneaking()) {
			moovy_isVaulting = false;
			setVelocity(0, 0, 0);
			return;
		}
		Vec3d vel = getVelocity();

		Vec3d eyePos = getEyePos();
		Vec3d kneePos = getPos();

		Vec3d eyeEnd = eyePos.add(moovy_vaultNormal.multiply(1));
		Vec3d kneeEnd = kneePos.add(moovy_vaultNormal.multiply(1));

		var hitA = this.world.raycast(new RaycastContext(eyePos, eyeEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
		var hitB = this.world.raycast(new RaycastContext(kneePos, kneeEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

		//If eyes missed, but knees hit, we're up against a ledge still.
		if (hitA.getType() == HitResult.Type.MISS && hitB.getType() == HitResult.Type.BLOCK) {

			// "lift" player up onto block by moving them up, and into the block.
			vel = vel.add(moovy_vaultNormal.x * 0.01f, 0, moovy_vaultNormal.z * 0.01f);
			vel = new Vec3d(vel.x, 0.4f, vel.z);

			setVelocity(vel);
			this.move(MovementType.SELF, vel);

			if (Math.abs(moovy_vaultTargetY - getPos().y) < 0.0001f) {
				moovy_isVaulting = false;
				setVelocity(new Vec3d(moovy_vaultSpeed.x, vel.y, moovy_vaultSpeed.z));

				if (moovy_wasSprinting)
					setSprinting(true);
			}
		} else {
			setVelocity(new Vec3d(moovy_vaultSpeed.x, vel.y, moovy_vaultSpeed.z));
			moovy_isVaulting = false;

			if (moovy_wasSprinting)
				setSprinting(true);
		}
	}

	public void moovy_wallrunning(Vec3d movementInput) {

		// If player presses jump, wallkick
		if (!moovy_wasJumping && jumping) {
			//Stop wallrunning
			moovy_wallrunning = false;

			//Calculate look direction (flat)
			Vec3d lookDir = new Vec3d(-Math.sin(Math.toRadians(getYaw())), 0, Math.cos(Math.toRadians(getYaw()))).multiply(0.3f);

			//Kick off of wall (normal), add vertical velocity, and add look direction
			setVelocity(getVelocity().add(moovy_wallRunNormal.multiply(0.2f)).add(0, 0.37f, 0).add(lookDir.multiply(0.3f)));

			MoovyMod.updater.spawnWalljumpParticles((PlayerEntity) (Object) this);
			moovy_spawnWallrunningParticles();

			//Move, then cancel wall running.
			this.move(MovementType.SELF, getVelocity());
			return;
		}

		if (!moovy_wasSneaking && isSneaking()) {
			moovy_wallrunning = false;
			this.move(MovementType.SELF, getVelocity());
			return;
		}

		//Set velocity to static wallrun speed, keeps us from having problems grinding against the wall.
		setVelocity(moovy_wallrunVel);

		//Grab common values
		Vec3d correctedInput = moovy_movementInputToVelocity(movementInput, 1, getYaw());
		Vec3d velocity = getVelocity();
		Vec3d flat = new Vec3d(velocity.x, 0, velocity.z);

		//If we're on the ground, can't wallrun with out current wallrun speed, or release input, stop wallrunning.
		if (isOnGround() || !moovy_couldWallrun(flat) || correctedInput.lengthSquared() < 0.001f)
			moovy_wallrunning = false;

		if (moovy_wallrunStickTimer > 0) moovy_wallrunStickTimer--;

		//Apply a small amount of gravity
		if (moovy_wallrunStickTimer == 0)
			moovy_wallrunVel = moovy_wallrunVel.add(0, -0.03f, 0).add(0, -moovy_wallrunVel.y * 0.01f, 0);
		else moovy_wallrunVel = new Vec3d(moovy_wallrunVel.x, 0, moovy_wallrunVel.z);

		velocity = velocity.add(moovy_wallRunNormal.multiply(0.1).negate());

		//Set velocity for other apis and stuff.
		setVelocity(velocity);

		//When we move, also move us towards the wall a little, so we hug it.
		this.move(MovementType.SELF, velocity);
	}

	public boolean moovy_couldWallrun(Vec3d flat) {
		if (flat.length() > 0.23f || moovy_wallrunning) {
			Vec3d closestVec = null;

			//Find closest of the 4 cardinal directions
			{
				flat = new Vec3d(flat.x, 0, flat.z);
				Vec3d flatNorm = flat.normalize();

				//System.out.println("FN:" + flatNorm);

				double leastAbs = 9999;
				for (int i = 0; i < moovy_wallrunAngles.length; i++) {
					//flat normalized vecor . cardinal direction
					double dot = flatNorm.dotProduct(moovy_wallrunAngles[i]);

					//Ignore negative dot products, they're too far to be considered.
					if (dot < 0) continue;

					if (Math.abs(dot) < leastAbs) {
						leastAbs = Math.abs(dot);
						closestVec = moovy_wallrunAngles[i];
					}
				}
			}

			Vec3d eyePos = getEyePos();
			Vec3d feetPos = getPos().add(0, 0.4f, 0);

			Vec3d flatNorm = closestVec.multiply(0.7f);

			//System.out.println("CLOSEST:" + closestVec);

			Vec3d eyeEnd = eyePos.add(flatNorm);
			Vec3d feetEnd = feetPos.add(flatNorm);

			var hitA = this.world.raycast(new RaycastContext(eyePos, eyeEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
			var hitB = this.world.raycast(new RaycastContext(feetPos, feetEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

			if (hitA.getType() == HitResult.Type.MISS || hitB.getType() == HitResult.Type.MISS) {
				//System.out.println(hitA.getType() + "|" + hitB.getType());
				//System.out.println(eyePos + "|" + eyeEnd);

				return false;
			}

			var hitANormal = new Vec3d(hitA.getSide().getUnitVector());
			var hitBNormal = new Vec3d(hitB.getSide().getUnitVector());
			var oppositeFlat = flat.normalize().negate();

			var aDot = oppositeFlat.dotProduct(hitANormal);
			var bDot = oppositeFlat.dotProduct(hitBNormal);

			if (aDot >= 0 && aDot < 0.9f && bDot >= 0 && bDot < 0.9f) {
				moovy_wallRunNormal = hitANormal;
				return true;
			} else {
				//System.out.println("Angle not right! " + aDot);
			}
		}

		//System.out.println("OTHER");
		return false;
	}

	private static Vec3d moovy_movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
		double d = movementInput.lengthSquared();
		if (d < 1.0E-7) {
			return Vec3d.ZERO;
		} else {
			Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double) speed);
			float f = MathHelper.sin(yaw * (float) (Math.PI / 180.0));
			float g = MathHelper.cos(yaw * (float) (Math.PI / 180.0));
			return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
		}
	}

	@Override
	public void moovy_setWallrunning(boolean state, Vec3d normal) {
		moovy_wallrunning = state;
		moovy_wallRunNormal = normal;
	}

	@Override
	public void moovy_setSliding(boolean state) {
		moovy_sliding = state;
	}

	@Override
	public void moovy_setCharge(int charge) {

		if (moovy_boostCharges > charge) moovy_boostVisualTimer = 4;

		moovy_boostCharges = charge;
	}

	@Override
	public void moovy_setVaulting(boolean state) {
		if (!moovy_isVaulting && state) moovy_emitVaultParticles();

		moovy_isVaulting = state;
	}

	@Override
	public void moovy_setBoostTimer(int count) {
		moovy_boostTimer = count;
	}

	@Override
	public void moovy_setBoostVisualTimer(int value) {
		if (!world.isClient)
			return;
		moovy_boostVisualTimer = value;
	}

	@Override
	public void moovy_spawnWallrunningParticles() {
		if (!world.isClient)
			return;
		moovy_emitVaultParticles();
	}

	private void moovy_emitVaultParticles() {
		var pos = getPos();

		for (int i = 0; i < 16; i++) {
			world.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
					pos.x + (random.nextDouble() - 0.5f) * 0.5f, pos.y, pos.z + (random.nextDouble() - 0.5f) * 0.5f,
					(random.nextDouble() - 0.5f) * 0.01f, -0.1f, (random.nextDouble() - 0.5f) * 0.01f
			);
		}
	}
}
