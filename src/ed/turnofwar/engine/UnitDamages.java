/*
 * This file is part of Turn of War which is a fork of Dragon Wars
 * as of 20/11/2013.
 *
 * Copyright (C) 2013 Ed Woodhouse <edwoodhou@gmail.com>
 *
 * Turn of War is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Turn of War is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Turn of War. If not, see <http://www.gnu.org/licenses/>.
 */
/* This file is part of Dragon Wars.
 *
 * Dragon Wars is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dragon Wars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dragon Wars. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.fuuzetsu.turnofwar.engine;

public class UnitDamages {

	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Infantry"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Mech"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Recon"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Tank"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("MdTank"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Neotank"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Megatank"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("AntiAir"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Piperunner"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Missiles"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Carrier"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Fighter"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("BattleCopter"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Stealth"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Cruiser"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Bomber"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Battleship"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Rockets"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Artillery"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Submarine"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("BlackBomb"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("APC"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("TransportCopter"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("Lander"))) {
	// damage = 0;
	// }
	// else if (attacker.getName().equals("") &&
	// (defender.getName().equals("BlackBoat"))) {
	// damage = 0;
	// }
	public int CalculateDamage(Unit attacker, Unit defender) {
		int damage = 0;
		if (attacker.getName().equals("Tank")
				&& (defender.getName().equals("Tank"))) {
			damage = 50;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("HeavyTank"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("RocketTruck"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("AntiAir"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("CommandVehicle"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("Fighter"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("Bomber"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("Stealth"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals("Destroyer"))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals(""))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals(""))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals(""))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals(""))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals(""))) {
			damage = 0;
		} else if (attacker.getName().equals("")
				&& (defender.getName().equals(""))) {
			damage = 0;
		}

		return damage;
	}
}
