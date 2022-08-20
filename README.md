# Moovy

Minecraft movement 'rewrite' mod, made as part of ModFest : Singularity. It tries to make Minecraft's movement more like traditional movement shooters.

__Requires the Quilt Loader__

# How It Works

To use the new movement system, you need to enchant your boots with Soul Speed. Each level of Soul Speed increases the number of boost charges you can have. This mod will __not__ decrease the durability of your boots while in use.

Moovy has a few new movement mechanics. While these can be used, I've tried to implement them in such a way that movement without them remains more or less unchanged (more notes on that at the end)

* __Sliding.__ If you press shift while moving quickly, instead of slowing down quickly, you'll slide. Sliding has low friction, making it great for keeping built up speed. Sliding in the air has even less friction. Sliding also puts you into the swimming animation, lowering your hitbox and allowing you to slip into small spaces. Taking damage will knock you out of a slide.
* __Boosting.__ If you press shift right before you land, you'll negate all fall damage, and convert some downward velocity to forward velocity. Each level of Soul Speed gives you an extra charge of boost. Charges are visualized by a particle effect. Smoke means no charges, small flames mean one, big flames mean two. No flames means you're full on charges for whatever level you have. __Note that you can cancel fall damage, even without charges, but you won't get the extra forward velocity if you don't have a charge.__
* __Slide Jumping.__ If you jump out of a slide, you'll gain extra height based on your speed. You'll have to slide for at least one tick in order to trigger slide jumping, which means holding jump won't trigger it over and over again. __Note:__ You can easily gain enough height with Slide Jumps to take fall damage and break your slide, so you'll have to be mindful and use boosts to negate fall damage.
* __Wallrunning.__ If you travel fast enough at a wall at a sharp enough angle, you'll catch the wall, and run along it. You get a short period where you won't move down, but after that, gravity will take hold and you'll start to slip. You can also jump out of a wallrun to gain some horizontal speed in the direction you're looking. Once you hit the ground, you'll re-gain the anti-slip.
* __Vaulting.__ If you hold shift while moving towards a ledge, as long as your head has clearance and your feet are against the ledge, you'll vault up over it. Vaulting will preserve the velocity of when you hit the wall, and restore it once you're over the ledge. It also won't stop your sprint, even though you hit a wall.
* __Speed Control.__ Past a certain speed, the movement control system changes. Rather than simply adding a bit of velocity and influencing your speed, you "turn" your speed vector without gaining or losing speed (aside from drag/boosting). If you've ever seen the Source Engine movement, this is what I tried to mimic.


I should also note that as long as you're wearing Soul Speed boots, you are locked into using the new movement system. I tried to mimic the vanilla movement as close as possible, but there are a few changes still. Your sprint jump no longer gives a speed boost, since it made sliding ridiculously OP. Your jump is also slightly lower, you have slightly faster falling, and your movement overall is less slidey than vanilla on normal surfaces, but more slidey on ice and such.

__NOTE FOR SERVER ADMINS:__ I couldn't be bothered to make this mod 'compatible' with vanilla anti-cheat, and by nature of what the mod is, it allows insane speed and crazy movement. As such, I just decided to outright disable vanilla's already terrible movement anti-cheat. This likely has some larger repercussions, but for a ModFest mod, I just didn't really care enough to make it 'properly secure'.

## License

This project is licensed under MIT
