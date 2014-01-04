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

package uk.co.fuuzetsu.turnofwar.engine.GoalArbitration;

import java.util.List;

import android.util.Log;

import uk.co.fuuzetsu.turnofwar.engine.Building;
import uk.co.fuuzetsu.turnofwar.engine.GameField;
import uk.co.fuuzetsu.turnofwar.engine.GameMap;
import uk.co.fuuzetsu.turnofwar.engine.GameState;
import uk.co.fuuzetsu.turnofwar.engine.Logic;
import uk.co.fuuzetsu.turnofwar.engine.Pair;
import uk.co.fuuzetsu.turnofwar.engine.Player;
import uk.co.fuuzetsu.turnofwar.engine.Position;
import uk.co.fuuzetsu.turnofwar.engine.Unit;

public class StateTree {
    private int maxSize = 200;
    private GameState gameState;
    private Node base = null;
    private Player stateTreeOwner;
    private Logic logic = new Logic();
    private List<AtomicAction> actions;

    public StateTree(final GameState gamestate, final int maxsize,
                     final Player owner) {
        gameState = gamestate;
        stateTreeOwner = owner;
    }

    private void Explore() {
        base = new Node(null, 0, 0, null);
        base.setSize(1);

        for (Player player : gameState.getPlayers()) {
            if (player.equals(stateTreeOwner)) {
                continue;
            }

            for (Unit playerUnit : stateTreeOwner.getOwnedUnits()) {
                float bestValue = -1;
                AtomicAction currentBest = null;

                for (Unit unit : player.getOwnedUnits()) {
                    if (base.getSize() >= maxSize) {
                        break;
                    }

                    // Evaluate cost and gain, don't add if below threshold
                    List<Position> vfs = logic.findValidFieldsNextToUnit(
                                             gameState.getMap(), playerUnit, unit);

                    if (vfs.isEmpty()) {
                        continue;
                    }

                    Pair<Pair<Double, Double>, Position> dmgpos = getBestAttackPosition(
                                gameState.getMap(), playerUnit, unit, vfs);
                    float damageRatio = (float)(dmgpos.getLeft().getLeft() / dmgpos
                                                .getLeft().getRight());

                    if (damageRatio < 0) { // In enemy's favour
                        continue;
                    }

                    if (damageRatio > bestValue) {
                        currentBest = new AttackAt(gameState, playerUnit, unit,
                                                   damageRatio, dmgpos.getRight());
                        bestValue = damageRatio;
                    }
                }

                if (currentBest == null) { /* No unit to attack */
                    GameField curField = gameState.getMap().getField(
                                             playerUnit.getPosition());

                    if (!(curField.hostsBuilding() && !curField.getBuilding()
                            .getOwner().equals(stateTreeOwner))) {
                        /* We're standing on a building we don't own */
                        List<Position> dests = logic.destinations(
                                                   gameState.getMap(), playerUnit);

                        for (Position p : dests) {
                            GameField gf = gameState.getMap().getField(p);

                            if (gf.hostsBuilding()
                                    && !gf.getBuilding().getOwner()
                                    .equals(stateTreeOwner)
                                    && !gf.hostsUnit()) {
                                currentBest = new MoveTo(gameState, playerUnit,
                                                         p, 1);
                                break; /* Naive building picking */
                            }
                        }
                    }
                }

                if (base.getSize() >= maxSize) {
                    break;
                }

                base.AddChildNode(bestValue, currentBest);
            }

        }

        int goldAmount = stateTreeOwner.getGoldAmount();

        for (Building building : stateTreeOwner.getOwnedBuildings()) {
            Position p = building.getPosition();

            if (!gameState.getMap().getField(p).hostsUnit()) {
                Log.d("StateTree", "Trying to build at " + building.getName());
                Unit bestBuildable = getBestBuildableUnit(building, goldAmount);

                if (bestBuildable == null) {
                    continue;
                }

                goldAmount -= bestBuildable.getProductionCost();
                AtomicAction bestAction = new BuildUnit(gameState,
                                                        bestBuildable, building.getPosition(),
                                                        bestBuildable.getProductionCost());
                base.AddChildNode(bestBuildable.getProductionCost(), bestAction);
            }
        }

        actions = base.getActions();
        base.Collapse();

    }

    public List<AtomicAction> getActions() {
        Explore();
        return actions;
    }

    private Pair<Pair<Double, Double>, Position> getBestAttackPosition(
        final GameMap map, final Unit attacker, final Unit defender,
        final List<Position> validPositions) {
        if (validPositions.size() == 1) {
            Position p = validPositions.get(0);
            return new Pair<Pair<Double, Double>, Position>(
                       logic.calculateDamageFrom(map, attacker, defender, p), p);
        } else {
            Double ratio = null; /* Damn it Java */
            Pair<Double, Double> bestRatioDamage = null;
            Position movePos = null;

            for (Position p : validPositions) {
                Pair<Double, Double> damageExchange = logic
                                                      .calculateDamageFrom(map, attacker, defender, p);
                Double pRatio = damageExchange.getLeft()
                                / damageExchange.getRight();

                if (pRatio == null || bestRatioDamage == null
                        || movePos == null) {
                    ratio = pRatio;
                    bestRatioDamage = damageExchange;
                    movePos = p;

                } else if (pRatio > ratio) {
                    ratio = pRatio;
                    bestRatioDamage = damageExchange;
                    movePos = p;
                }
            }

            return new Pair<Pair<Double, Double>, Position>(bestRatioDamage,
                    movePos);
        }
    }

    private Unit getBestBuildableUnit(final Building building,
                                      final int goldAmount) {

        List<Unit> buildable = building.getProducibleUnits();

        if (buildable.size() == 0) {
            return null;
        }

        Unit bestUnit = buildable.get(0);

        for (Unit unit : buildable) {
            int cost = unit.getProductionCost();

            if (cost <= goldAmount && cost > bestUnit.getProductionCost()) {
                bestUnit = unit;
            }
        }

        if (bestUnit.getProductionCost() > goldAmount) {
            return null;
        } else {
            return bestUnit;
        }
    }
}
