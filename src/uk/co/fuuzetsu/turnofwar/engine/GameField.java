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

import java.text.DecimalFormat;

public final class GameField extends DrawableMapObject {

    private Unit hostedUnit;
    private Building hostedBuilding;
    private final Double movementModifier;
    private final Double defenseModifier, attackModifier;
    private final Boolean flightOnly, boatOnly;
	private Boolean accessible;

    private static DecimalFormat decformat = new DecimalFormat("#.##");

    public Boolean doesAcceptUnit(Unit unit) {
        Boolean canStep = false;
        this.accessible = false;

        if (this.flightOnly && unit.isBoat() || unit.isFlying()) { // match
            // units to
            // their
            // correct
            // terrains
            this.accessible = true;
        } else {
            canStep = false;
            this.accessible = false;
        }
        if (!this.flightOnly && !unit.isBoat() && !unit.isFlying()
                && !this.getName().equals("Mountain")
                && !this.getName().equals("River")) { // land units cant move on
            // mountain and rivers
            this.accessible = true;
        }

        if ((this.getName().equals("Mountain") || this.getName()
                .equals("River"))
                && (unit.getMobility().equals("i") || unit.getMobility()
                    .equals("m"))) { // infantry can go on
            // mountains and rivers
            this.accessible = true;
        }

        if (this.hostsBuilding() && this.flightOnly && !unit.isFlying()) { // allow
            // ports to be stepped on by land units (test for a building in the sea)
            this.accessible = true;
        }

        return this.accessible || canStep;
    }

    public GameField(final String fieldName, final Double movementModifier,
                     final Double attackModifier, final Double defenseModifier,
                     final Boolean accessible, final Boolean flightOnly,
                     final Boolean boatOnly, final String spriteLocation,
                     final String spriteDir, final String spritePack) {
        super(fieldName, spriteLocation, spriteDir, spritePack);

        this.hostedUnit = null;
        this.hostedBuilding = null;
        this.movementModifier = movementModifier;
        this.attackModifier = attackModifier;
        this.defenseModifier = defenseModifier;
        this.accessible = accessible;
        this.flightOnly = flightOnly;
        this.boatOnly = boatOnly;

        generateInfo();
    }

    public Double getDefenseModifier() {
        if (this.hostsBuilding()) {
            Double mod = this.hostedBuilding.getDefenseBonus();
            // mod += this.defenseModifier;
            return mod;
        }

        return this.defenseModifier;
    }

    public Double getAttackModifier() {
        if (this.hostsBuilding()) {
            Double mod = this.hostedBuilding.getAttackBonus();
            mod += this.attackModifier;
            return mod / 2;
        }

        return this.attackModifier;
    }

    public Double getMovementModifier() {
        return this.movementModifier;
    }

    public Boolean canBeStoppedOn() {
        return accessible;
    }

    public Boolean hostsUnit() {
        return this.hostedUnit != null;
    }

    public Boolean hostsBuilding() {
        /* Could be used by the drawing routine. */
        return this.hostedBuilding != null;
    }

    public Unit getUnit() {
        return this.hostedUnit;
    }

    public Building getBuilding() {
        return this.hostedBuilding;
    }

    /* This will clobber old units/buildings as it is now. */
    public void setBuilding(final Building building) {
        this.hostedBuilding = building;
    }

    public void setUnit(final Unit unit) {
        this.hostedUnit = unit;
    }

    @Override
	public String toString() {
        return getName();
    }

    @Override
    public void generateInfo() {
        String r = ""; // getName() +
        // r += "Defense: " + decformat.format(getDefenseModifier())
        // + "Move: " + decformat.format(getMovementModifier()) + "\n";
        this.info = r;
    }
}
