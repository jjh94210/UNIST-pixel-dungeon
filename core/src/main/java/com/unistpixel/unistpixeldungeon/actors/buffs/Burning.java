/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.unistpixel.unistpixeldungeon.actors.buffs;

import com.unistpixel.unistpixeldungeon.Badges;
import com.unistpixel.unistpixeldungeon.Dungeon;
import com.unistpixel.unistpixeldungeon.actors.Char;
import com.unistpixel.unistpixeldungeon.actors.blobs.Blob;
import com.unistpixel.unistpixeldungeon.actors.blobs.Fire;
import com.unistpixel.unistpixeldungeon.actors.hero.Hero;
import com.unistpixel.unistpixeldungeon.actors.mobs.Thief;
import com.unistpixel.unistpixeldungeon.effects.particles.ElmoParticle;
import com.unistpixel.unistpixeldungeon.items.Heap;
import com.unistpixel.unistpixeldungeon.items.Item;
import com.unistpixel.unistpixeldungeon.items.armor.glyphs.Brimstone;
import com.unistpixel.unistpixeldungeon.items.food.ChargrilledMeat;
import com.unistpixel.unistpixeldungeon.items.food.MysteryMeat;
import com.unistpixel.unistpixeldungeon.items.rings.RingOfElements.Resistance;
import com.unistpixel.unistpixeldungeon.items.scrolls.Scroll;
import com.unistpixel.unistpixeldungeon.items.scrolls.ScrollOfMagicalInfusion;
import com.unistpixel.unistpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.unistpixel.unistpixeldungeon.levels.Level;
import com.unistpixel.unistpixeldungeon.messages.Messages;
import com.unistpixel.unistpixeldungeon.scenes.GameScene;
import com.unistpixel.unistpixeldungeon.sprites.CharSprite;
import com.unistpixel.unistpixeldungeon.ui.BuffIndicator;
import com.unistpixel.unistpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Burning extends Buff implements Hero.Doom {
	
	private static final float DURATION = 8f;
	
	private float left;
	
	private static final String LEFT	= "left";

	{
		type = buffType.NEGATIVE;
	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		left = bundle.getFloat( LEFT );
	}

	@Override
	public boolean act() {
		
		if (target.isAlive()) {

			//maximum damage scales from 6 to 2 depending on remaining hp.
			int maxDmg = 3 + Math.round( 4 * target.HP / (float)target.HT );
			int damage = Random.Int( 1, maxDmg );
			Buff.detach( target, Chill.class);

			if (target instanceof Hero) {

				Hero hero = (Hero)target;

				if (hero.belongings.armor != null && hero.belongings.armor.hasGlyph(Brimstone.class)){

					Buff.affect(target, Brimstone.BrimstoneShield.class);

				} else {

					hero.damage( damage, this );
					Item item = hero.belongings.randomUnequipped();
					if (item instanceof Scroll
							&& !(item instanceof ScrollOfUpgrade || item instanceof ScrollOfMagicalInfusion)) {

						item = item.detach( hero.belongings.backpack );
						GLog.w( Messages.get(this, "burnsup", Messages.capitalize(item.toString())) );

						Heap.burnFX( hero.pos );

					} else if (item instanceof MysteryMeat) {

						item = item.detach( hero.belongings.backpack );
						ChargrilledMeat steak = new ChargrilledMeat();
						if (!steak.collect( hero.belongings.backpack )) {
							Dungeon.level.drop( steak, hero.pos ).sprite.drop();
						}
						GLog.w( Messages.get(this, "burnsup", item.toString()) );

						Heap.burnFX( hero.pos );

					}

				}
				
			} else {
				target.damage( damage, this );
			}

			if (target instanceof Thief) {

				Item item = ((Thief) target).item;

				if (item instanceof Scroll &&
						!(item instanceof ScrollOfUpgrade || item instanceof ScrollOfMagicalInfusion)) {
					target.sprite.emitter().burst( ElmoParticle.FACTORY, 6 );
					((Thief)target).item = null;
				}

			}

		} else {

			Brimstone.BrimstoneShield brimShield = target.buff(Brimstone.BrimstoneShield.class);
			if (brimShield != null)
				brimShield.startDecay();

			detach();
		}
		
		if (Level.flamable[target.pos] && Blob.volumeAt(target.pos, Fire.class) == 0) {
			GameScene.add( Blob.seed( target.pos, 4, Fire.class ) );
		}
		
		spend( TICK );
		left -= TICK;
		
		if (left <= 0 ||
			(Level.water[target.pos] && !target.flying)) {
			
			detach();
		}
		
		return true;
	}
	
	public void reignite( Char ch ) {
		left = duration( ch );
	}
	
	@Override
	public int icon() {
		return BuffIndicator.FIRE;
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.BURNING);
		else target.sprite.remove(CharSprite.State.BURNING);
	}

	@Override
	public String heroMessage() {
		return Messages.get(this, "heromsg");
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	public static float duration( Char ch ) {
		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() * DURATION : DURATION;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(left));
	}

	@Override
	public void onDeath() {
		
		Badges.validateDeathFromFire();
		
		Dungeon.fail( getClass() );
		GLog.n( Messages.get(this, "ondeath") );
	}
}
