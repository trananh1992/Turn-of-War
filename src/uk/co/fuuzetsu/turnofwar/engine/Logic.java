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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/* Class containing things like damage calculation and path finding. */
public final class Logic {

    private static final String TAG = "Logic";

    public List<Position> findValidFieldsNextToUnit(final GameMap map,
            final Unit attackerUnit, final Unit targetUnit) {
        List<Position> potential = getValidNeighbours(map,
                                   targetUnit.getPosition());
        List<Position> dests = destinations(map, attackerUnit);
        Iterator<Position> iter = potential.iterator();

        /* Remove positions we can't reach anyway */
        while (iter.hasNext()) {
            Position p = iter.next();
            GameField gf = map.getField(p);

            if ((gf.hostsUnit() && !gf.getUnit().equals(attackerUnit))
                    || !dests.contains(p)) {
                iter.remove();
            }
        }

        return potential;
    }

    public List<Position> findPath(final GameMap map, final Unit unit,
                                   final Position destination) {
        return AStar(map, unit, destination);
    }

    public Integer calculateMovementCost(final GameMap map, final Unit unit,
                                         final List<Position> path) {
        Double totalCost = 0.0;

        for (Position pos : path) {
            if (pos.equals(unit.getPosition())) {
                continue;
            }

            totalCost += getMovementCost(map, unit, pos);
        }

        // totalCost = Math.ceil(totalCost);

        return totalCost.intValue();
    }

    public List<Position> destinations(final GameMap map, final Unit unit) {
        Set<Position> checked = new HashSet<Position>();
        Set<Position> reachable = new HashSet<Position>();

        Position unitPosition = unit.getPosition();
        List<Node> start = new ArrayList<Node>();
        start.add(new Node(unitPosition, 0.0, 0.0));

        checked.add(unitPosition);
        reachable.add(unitPosition);

        List<Node> next = nextPositions(map, start);
        while (next.size() != 0) {
            List<Node> newNext = new ArrayList<Node>();

            for (Node n : next) {
                checked.add(n.getPosition());

                if (unit.getRemainingMovement() < n.getG()) {
                    continue;
                }

                if (map.getField(n.getPosition()).doesAcceptUnit(unit)) {
                    // if (map.getField(n.getPosition()).hostsUnit() &&
                    // !(map.getField(n.getPosition()).getUnit().getName().equals("Submarine"))
                    // &&
                    // !(map.getField(n.getPosition()).getUnit().getName().equals("Stealth")))
                    // {
                    if ((map.getField(n.getPosition()).hostsUnit()
                            && !(map.getField(n.getPosition()).getUnit()
                                 .getName().equals("Submarine")) && !(map
                                         .getField(n.getPosition()).getUnit().getName()
                                         .equals("Stealth")))
                            || (map.getField(n.getPosition()).hostsUnit()
                                && ((unit.getName().equals("Destroyer") || unit
                                     .getName().equals("Submarine"))) && map
                                .getField(n.getPosition()).getUnit()
                                .getName().equals("Submarine"))
                            || (map.getField(n.getPosition()).hostsUnit()
                                && ((unit.getName().equals("Fighter") || unit
                                     .getName().equals("Stealth"))) && map
                                .getField(n.getPosition()).getUnit()
                                .getName().equals("Stealth"))) {
                        Player op = map.getField(n.getPosition()).getUnit()
                                    .getOwner(); // pass through subs and stealths
                        // || ((!unit.getName().equals("Stealth") ||
                        // !unit.getName().equals("Fighter")) &&
                        // !map.getField(n.getPosition()).getUnit().getName().equals("Stealth"))
                        if (!op.equals(unit.getOwner())) {
                            continue;
                            // }
                        }
                    }

                    reachable.add(n.getPosition());
                    List<Node> thisNext = new ArrayList<Node>(5);

                    thisNext.add(n);
                    thisNext = nextPositions(map, thisNext);

                    for (Node thisNode : thisNext) {
                        if (!checked.contains(thisNode.getPosition())) {
                            newNext.add(thisNode);
                        }
                    }
                }
            }

            next = newNext;
        }

        List<Position> shown = new ArrayList<Position>();

        for (Position p : reachable) {
            if (map.getField(p).canBeStoppedOn()) {

                // if (getMovementCost(map, unit, p) >= 3) { ///find a type of
                // terrain by its movement cost eg trees = 3
                shown.add(p);

                if (!(unit.isFlying() || unit.isBoat())) { // only need to do
                    // this for non
                    // boats and planes
                    Double totalCost = 0.0;
                    List<Position> path = findPath(map, unit, p);
                    for (Position pos : path) {
                        if (pos.equals(unit.getPosition())) {
                            continue;
                        }
                        totalCost += getMovementCost(map, unit, pos);
                        // totalCost += 1;
                    }
                    // String A = "yolo";
                    // Log.v(A, "remaining movement" + totalCost);

                    if (totalCost > unit.getMaxMovement()) {
                        shown.remove(p); // remove places that cannot be reached
                        // (it would be better to not add
                        // them to begin)
                    }
                }
            }
        }
        return shown;
    }

    public List<Node> nextPositions(final GameMap map, final List<Node> toCheck) {

        List<Node> result = new ArrayList<Node>();

        for (Node n : toCheck) {
            Double costSoFar = n.getG();
            Position currentPosition = n.getPosition();
            List<Position> adj = getValidNeighbours(map, currentPosition);

            for (Position pos : adj) {
                GameField cField = map.getField(pos);
                Double newCost = costSoFar + cField.getMovementModifier();
                result.add(new Node(pos, newCost, 0.0));
            }
        }

        return result;
    }

    /* Returns damage as if the attacker was standing on a different position */
    public Pair<Double, Double> calculateDamageFrom(final GameMap map,
            final Unit attacker, final Unit defender, final Position position) {
        /* We can cheat and temporarily set a unit's position to the fake one */

        Position originalPosition = attacker.getPosition();
        attacker.setPosition(position);
        Pair<Double, Double> damage = calculateDamage(map, attacker, defender);
        attacker.setPosition(originalPosition);
        return damage;
    }

    public Pair<Double, Double> calculateDamage(final GameMap map,
            final Unit attacker, final Unit defender) {
        Double a = calculateRawDamage(map, attacker, defender);
        Double b = calculateCounterDamage(map, attacker, defender);
        return new Pair<Double, Double>(a, b);
    }

    public Double calculateRawDamage(final GameMap map, final Unit attacker,
                                     final Unit defender) {
        GameField defenderField = map.getField(defender.getPosition());

        // DAMAGE TO ORIGINAL DEFENDER FROM ATTACKER

        Double fieldDefense = 0.0;
        if (defender.isFlying()) {
            fieldDefense = 0.0;
        } else {
            fieldDefense = defenderField.getDefenseModifier(); // original
            // attackers
            // defense mod
        }

        int damage = 0;
        damage = new UnitDamages().CalculateDamage(attacker, defender);

        // String A = "yolo";
        // Log.v(A, "field defense of target " + fieldDefense + " " + (1 -
        // ((attacker.getHealth() * fieldDefense) / 100)));

        Double finalDamage = ((damage * (Math.ceil(attacker.getHealth()) / 10)) *
                              (1 - ((defender
                                     .getHealth() * fieldDefense) / 100))) / 10;

        Random r = new Random();
        int min = 0;
        int max = 4;
        double crit = (r.nextDouble() * ((max - min) + min)); // if crit ceil
        // the damage

        if (Math.round(finalDamage) == 0) {

        } else {
            if (crit >= 1) {
                finalDamage = (double) Math.ceil(finalDamage);
            } else {
                if (attacker.isRanged()) {
                    finalDamage = (double) Math.floor(finalDamage);
                }
            }
        }

        // String A = "yolo";
        // Log.v(A, "damage to defender " + " " + " " + finalDamage + " atkhp" +
        // attacker.getHealth() + " " + defender.getHealth() + " defhp");
        return attacker.getHealth() > 0.0 ? finalDamage : 0.0;
    }

    public Double calculateCounterDamage(final GameMap map,
                                         final Unit attacker, final Unit defender) {
        Double initialDamage = calculateRawDamage(map, attacker, defender);
        Double defenderHealth = defender.getHealth() - initialDamage;
        defenderHealth = (defenderHealth < 0) ? 0 : defenderHealth;
        // String A = "yolo";
        // Log.v(A, "new defender hp " + defenderHealth);

        return calculateTheoreticalCounterDamage(map, defender, attacker,
                defenderHealth);
    }

    private Double calculateTheoreticalCounterDamage(final GameMap map,
            final Unit attacker, final Unit defender, final Double atkHealth) {
        GameField attackerField = map.getField(defender.getPosition()); // this
        // is
        // swapped
        // to
        // defender

        // DAMAGE TO ORIGINAL ATTACKER FROM DEFENDER
        Double fieldDefense = 0.0;
        if (defender.isFlying()) {
            fieldDefense = 0.0;
        } else {
            fieldDefense = attackerField.getDefenseModifier(); // original
            // attackers
            // defense mod
        }
        int damage = 0;
        damage = new UnitDamages().CalculateDamage(attacker, defender);

        // String A = "yolo";
        // Log.v(A, "field defense of initial attacker " + fieldDefense + " " +
        // (1 - ((attacker.getHealth() * fieldDefense) / 100)));

        double finalDamage;
        if (!defender.isRanged() && !attacker.isRanged()) {
            finalDamage = ((damage * (atkHealth / 10)) * (1 - ((attacker
                           .getHealth() * fieldDefense) / 100))) / 10;
        } else {
            finalDamage = 0;
        }

        Random r = new Random();
        int min = 0;
        int max = 4;
        double crit = (r.nextDouble() * ((max - min) + min)); // if crit ceil
        // the damage
        if (Math.round(finalDamage) == 0) {
        } else {
            if (crit >= 3) {
                finalDamage = (double) Math.ceil(finalDamage);
            } else {
                // if (Math.round(finalDamage) < finalDamage) {
                // finalDamage = (double) Math.floor(finalDamage);//
                // }
            }
        }
        // String A = "yolo";
        // Log.v(A, " damage to attacker " + finalDamage + " atkhp" +
        // attacker.getHealth() + " " + defender.getHealth() + " defhp");
        return attacker.getHealth() > 0 ? finalDamage : 0;
    }

    private List<Position> AStar(final GameMap map, final Unit unit,
                                 final Position destination) {
        if (!map.isValidField(destination)) {
            return new ArrayList<Position>(0);
        }

        PriorityQueue<Node> openSet = new PriorityQueue<Node>(10,
                new AStarComparator());
        Set<Node> closedSet = new HashSet<Node>();

        Node root = new Node(unit.getPosition(), 0.0,
                             1.0 * getManhattanDistance(unit.getPosition(), destination));
        openSet.add(root);

        while (openSet.size() != 0) {
            Node current = openSet.poll();

            if (current.getPosition().equals(destination)) {
                return reconstructPath(current);
            }

            closedSet.add(current);

            for (Position n : getValidNeighbours(map, current.getPosition())) {
                GameField gf = map.getField(n);
                if ((!gf.doesAcceptUnit(unit))
                        || (gf.hostsUnit()
                            && (!(gf.getUnit().getOwner().equals(unit
                                    .getOwner()))) && !((gf.getUnit()
                                                        .getName().equals("Stealth") || (gf.getUnit()
                                                                .getName().equals("Submarine")))))) { // don't
                    // consider
                    // squares
                    // with
                    // enemy
                    // units
                    // in
                    continue;
                }
                Node neigh = new Node(n, gf.getMovementModifier(),
                                      1.0 * getManhattanDistance(unit.getPosition(),
                                              destination));
                Double tentG = current.getG() + neigh.getG();
                if (closedSet.contains(neigh)) {
                    if (tentG >= neigh.getG()) {
                        continue;
                    }
                }

                if ((!openSet.contains(neigh)) || tentG < neigh.getG()) {
                    neigh.setParent(current);

                    if (!openSet.contains(neigh)) {
                        openSet.add(neigh);
                    }
                }
            }
        }
        return new ArrayList<Position>(0); /* Search failed */
    }

    private List<Position> getValidSurroundingPositions(final GameMap map, // Attack
            // directions
            final Position pos) {
        List<Position> positions = getValidNeighbours(map, pos);
        return positions;
    }

    private List<Position> getValidNeighbours(final GameMap map,
            final Position pos) {
        List<Position> positions = new ArrayList<Position>(4);
        positions.add(new Position(pos.getX(), pos.getY() + 1));
        positions.add(new Position(pos.getX(), pos.getY() - 1));
        positions.add(new Position(pos.getX() + 1, pos.getY()));
        positions.add(new Position(pos.getX() - 1, pos.getY()));
        List<Position> validPositions = new ArrayList<Position>(4);

        for (Position p : positions) {
            if (map.isValidField(p)) {
                validPositions.add(p);
            }
        }
        return validPositions;
    }

    private List<Position> getValidSurroundingPositionsRanged(
        final GameMap map, // Attack directions
        final Position pos, String name) {
        List<Position> positions = getValidNeighboursRanged(map, pos, name);
        return positions;
    }

    private List<Position> getValidNeighboursRanged(final GameMap map,
            final Position pos, String name) {
        List<Position> positions = new ArrayList<Position>();
        int min = 0; // min range
        int max = 0; // max range
        int x = 0;
        int y = 0;

        if (name.equals("RocketTruck")) {
            min = 2;
            max = 4;
        } else if (name.equals("AntiAir")) {
            min = 3;
            max = 5;
        } else if (name.equals("AntiAirBoat")) {
            min = 3;
            max = 5;
        } else if (name.equals("Battleship")) {
            min = 2;
            max = 6;
        } else if (name.equals("MissileSub")) {
            min = 3;
            max = 8;
        }
        for (x = -max; x <= +max; x++) {
            for (y = -max; y <= +max; y++) {
                if (((Math.abs(x) + Math.abs(y)) >= min)
                        && ((Math.abs(x) + Math.abs(y)) <= max)) {
                    positions.add(new Position(pos.getX() + x, pos.getY() + y));
                }
            }
        }
        List<Position> validPositions = new ArrayList<Position>();
        for (Position p : positions) {
            if (map.isValidField(p)) {
                validPositions.add(p);
            }
        }
        return validPositions;
    }

    private class AStarComparator implements Comparator<Node> {
        public int compare(final Node a, final Node b) {
            Double t = a.getF() - b.getF();

            if (t > 0) {
                return 1;
            }

            if (t < 0) {
                return -1;
            }

            return 0;
        }
    }

    private List<Position> reconstructPath(final Node node) {
        List<Position> path = new ArrayList<Position>();
        path.add(node.getPosition());
        Node parent = node.getParent();

        while (parent != null) {
            path.add(parent.getPosition());
            parent = parent.getParent();
        }

        return path;
    }

    private class Node {
        private Node parent;
        private Position p;
        private Double g, h;

        public Node(final Position p, final Double g, final Double h) {
            this.p = p;

            this.g = g;
            this.h = h;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(final Node parent) {
            this.parent = parent;
            this.g = this.g + parent.getG();
        }

        public Double getG() {
            return g;
        }

        public Double getF() {
            return h + g;
        }

        public Position getPosition() {
            return p;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Node)) {
                return false;
            }

            Node that = (Node) other;
            return p.equals(that.getPosition());
        }

        @Override
        public int hashCode() {
            return p.hashCode();
        }
    }

    private Double getMovementCost(final GameMap map, final Unit unit,
                                   final Position origin) {
        /* g(x) for search */
        // flying units ignore this; always 1
        Double moveCost = 100.0; // unit wont move if everything isnt set up
        // right
        if (unit.isFlying() || unit.isBoat()) {
            return 1.0;
        } else if (!unit.isBoat() && !unit.isFlying()) {
            if (unit.getMobility().equals("t")) {// tread
                if (map.getField(origin).getName().equals("Grass")) {
                    return 1.0;
                } else if (map.getField(origin).getName().equals("Sand")) {
                    return 1.0;
                } else if (map.getField(origin).getName().equals("Forest")) {
                    return 2.0;
                } else if (map.getField(origin).getName().equals("Mountain")) {
                    return 1.0;
                } else if (map.getField(origin).getName().equals("River")) {
                    return 1.0;
                } else if (map.getField(origin).getName().equals("Highway")) {
                    return 1.0;
                } else {
                    return 1.0;
                }
            } else {
                return 1.0;
            }
        }
        // String yolo = null;
        // Log.d(yolo, "hi" + moveCost);
        return moveCost;
    }

    public Set<Position> getAttackableUnitPositions(final int myGo,
            final GameMap map, final Unit unit, final Position position) {
        Set<Position> atkFields = getAttackableFields(myGo, map, unit, position);
        Set<Position> atkUnits = new HashSet<Position>();

        for (Position p : atkFields) {
            if (map.isValidField(p)) {
                if (myGo == 0 && unit.isRanged) {
                    atkUnits.add(p);
                } else {
                    if (map.getField(p).hostsUnit()) {
                        Unit target = map.getField(p).getUnit();
                        boolean hostile = !map.getField(p).getUnit().getOwner()
                                          .equals(unit.getOwner());

                        // String A = "yolo";
                        // Log.v(A, " " + unit + "   targets:   " + target +
                        // "  "+ "  ");

                        // (unit.getName().equals("")) //attacking units name
                        // (target.getName().equals("")) //defending unit name
                        // (!target.isBoat() && !target.isFlying()) //ground
                        // ((target.getName().equals("BattleCopter"))||(target.getName().equals("TransportCopter")))
                        // //helicopter
                        //

                        if (unit.getName().equals("Tank")
                                || unit.getName().equals("HeavyTank")) {
                            if (hostile
                                    && (!target.isBoat() && !target.isFlying())) { // shoot
                                // at
                                // all
                                // ground
                                // units
                                atkUnits.add(p);
                            }
                        } else if (unit.getName().equals("AntiAir")
                                   || unit.getName().equals("AntiAirBoat")) {
                            if (hostile && !target.getName().equals("Stealth")
                                    && target.isFlying()) { // shoot at all air
                                // units, not
                                // stealth
                                atkUnits.add(p);
                            }
                        } else if (unit.getName().equals("Fighter")) {
                            if (hostile && target.isFlying()) { // shoot at all
                                // air units
                                atkUnits.add(p);
                            }
                        } else if (unit.getName().equals("Stealth")) {
                            if (hostile
                                    && !target.getName().equals("Submarine")
                                    && !target.getName().equals("MissileSub")) { // shoot
                                // at
                                // all
                                // apart
                                // from
                                // subs
                                atkUnits.add(p);
                            }
                        } else if (unit.getName().equals("Destroyer")) {
                            if (hostile && !target.isFlying()) {
                                atkUnits.add(p);
                            }
                        } else if (unit.getName().equals("Submarine")) {
                            if (hostile && target.isBoat()) {
                                atkUnits.add(p);
                            }
                        } else if (unit.getName().equals("Bomber")
                                   || unit.getName().equals("Battleship")
                                   || unit.getName().equals("RocketTruck")
                                   || unit.getName().equals("MissileSub")) {
                            if (hostile && !target.isFlying()
                                    && !target.getName().equals("Submarine")
                                    && !target.getName().equals("MissileSub")) {
                                atkUnits.add(p);
                            }
                        }
                    }
                }
            }
        }
        return atkUnits;
    }

    public Set<Position> getAttackableUnitPositions(int myGo,
            final GameMap map, final Unit unit) {

        return getAttackableUnitPositions(myGo, map, unit, unit.getPosition());
    }

    private Set<Position> getAttackableFields(final int myGo,
            final GameMap map, final Unit unit, final Position position) {
        String name = unit.getName(); // sort out ranged/owned/direct range
        // squares
        if (!unit.isRanged()) {
            return new HashSet<Position>(getValidSurroundingPositions(map,
                                         position));
        } else {
            return new HashSet<Position>(getValidSurroundingPositionsRanged(
                                             map, position, name));
        }
    }

    public Set<Position> getAttackableFields(final GameMap map, final Unit unit) {
        return getAttackableFields(0, map, unit, unit.getPosition());
    }

    /* Used as a heuristic for A* */
    private Integer getManhattanDistance(final Position origin,
                                         final Position destination) {
        /* h(x) */
        Pair<Integer, Integer> distance = getDistanceAway(origin, destination);

        return distance.getLeft() + distance.getRight();
    }

    private Pair<Integer, Integer> getDistanceAway(final Position origin,
            final Position destination) {
        Integer x = Math.abs(origin.getX() - destination.getX());
        Integer y = Math.abs(origin.getY() - destination.getY());
        return new Pair<Integer, Integer>(x, y);
    }

}
