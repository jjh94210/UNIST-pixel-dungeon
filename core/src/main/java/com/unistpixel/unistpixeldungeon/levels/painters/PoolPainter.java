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
package com.unistpixel.unistpixeldungeon.levels.painters;

import com.unistpixel.unistpixeldungeon.Dungeon;
import com.unistpixel.unistpixeldungeon.actors.mobs.Piranha;
import com.unistpixel.unistpixeldungeon.items.Generator;
import com.unistpixel.unistpixeldungeon.items.Gold;
import com.unistpixel.unistpixeldungeon.items.Heap;
import com.unistpixel.unistpixeldungeon.items.Item;
import com.unistpixel.unistpixeldungeon.items.potions.PotionOfInvisibility;
import com.unistpixel.unistpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.unistpixel.unistpixeldungeon.levels.Level;
import com.unistpixel.unistpixeldungeon.levels.Room;
import com.unistpixel.unistpixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class PoolPainter extends Painter {

	private static final int NPIRANHAS	= 3;
	
	public static void paint( Level level, Room room ) {
		
		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.WATER );
		
		Room.Door door = room.entrance();
		door.set( Room.Door.Type.REGULAR );

		int x = -1;
		int y = -1;
		if (door.x == room.left) {
			
			x = room.right - 1;
			y = room.top + room.height() / 2;
			
		} else if (door.x == room.right) {
			
			x = room.left + 1;
			y = room.top + room.height() / 2;
			
		} else if (door.y == room.top) {
			
			x = room.left + room.width() / 2;
			y = room.bottom - 1;
			
		} else if (door.y == room.bottom) {
			
			x = room.left + room.width() / 2;
			y = room.top + 1;
			
		}
		
		int pos = x + y * level.width();
		level.drop( prize( level ), pos ).type = Heap.Type.CHEST; //Random.Int( 3 ) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP;
		set( level, pos, Terrain.PEDESTAL );
		
		level.addItemToSpawn( new PotionOfInvisibility() );
		
		for (int i=0; i < NPIRANHAS; i++) {
			Piranha piranha = new Piranha();
			do {
				piranha.pos = level.pointToCell(room.random());
			} while (level.map[piranha.pos] != Terrain.WATER|| level.findMob( piranha.pos ) != null);
			level.mobs.add( piranha );
		}
	}
	
	private static Item prize( Level level ) {

		Item prize;
		prize = new Gold( 314 );
		return prize;
		/* 보물상자 안에는 골드 314만 들어가게 수정
		if (Random.Int(3) == 0){
			prize = level.findPrizeItem();
			if (prize != null)
				return prize;
		}

		//1 floor set higher in probability, never cursed
		do {
			if (Random.Int(2) == 0) {
				prize = Generator.randomWeapon((Dungeon.depth / 5) + 1);
			} else {
				prize = Generator.randomArmor((Dungeon.depth / 5) + 1);
			}
		} while (prize.cursed);

		//33% chance for an extra update.
		if (!(prize instanceof MissileWeapon) && Random.Int(3) == 0){
			prize.upgrade();
		}

		return prize;
		*/
	}
}
