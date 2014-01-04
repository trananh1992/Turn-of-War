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
package uk.co.fuuzetsu.turnofwar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import uk.co.fuuzetsu.turnofwar.engine.BitmapChanger;
import uk.co.fuuzetsu.turnofwar.engine.Building;
import uk.co.fuuzetsu.turnofwar.engine.DrawableMapObject;
import uk.co.fuuzetsu.turnofwar.engine.FloatPair;
import uk.co.fuuzetsu.turnofwar.engine.GameField;
import uk.co.fuuzetsu.turnofwar.engine.GameFinishedException;
import uk.co.fuuzetsu.turnofwar.engine.GameMap;
import uk.co.fuuzetsu.turnofwar.engine.GameState;
import uk.co.fuuzetsu.turnofwar.engine.Logic;
import uk.co.fuuzetsu.turnofwar.engine.Pair;
import uk.co.fuuzetsu.turnofwar.engine.Player;
import uk.co.fuuzetsu.turnofwar.engine.Position;
import uk.co.fuuzetsu.turnofwar.engine.Unit;

/* Please tell me if the below causes problems, Android/Eclipse
 * suddenly decided to refuse to compile anything without it
 * (apparently because GestureDetector was only introduced in
 * API level 3 (Cupcake), however we've been using it ever since
 * we had scrolling and it's never caused a problem before)
 */
//@TargetApi(Build.VERSION_CODES.CUPCAKE)
public final class GameView extends SurfaceView implements
    SurfaceHolder.Callback, OnGestureListener, OnDoubleTapListener,
    DialogInterface.OnClickListener, OnClickListener {

    private final String TAG = "GameView";

    private final int tilesize = 128; // change for map size //108

    private GameState state;
    private Logic logic;
    private GameMap map = null;
    private Position selected = new Position(0, 0);

    private FloatPair scrollOffset = new FloatPair(0f, 0f);
    private GestureDetector gestureDetector;

    private DrawingThread dt;

    private Paint cornerBoxTextPaint;
    private Paint cornerBoxBackPaint;
    private Paint unitHealthPaint;
    private Paint unitHealthOutlinePaint;
    private Paint movedPaint;
    private Paint movedOutlinePaint;
    private Paint capPaint;
    private Paint capOutlinePaint;

    private List<Pair<Unit, Long>> damagedUnits = new ArrayList<Pair<Unit, Long>>();

    private Context context;
    private Map<String, Map<String, Bitmap>> graphics = new
    HashMap<String, Map<String, Bitmap>>();

    private DecimalFormat decformat = new DecimalFormat("#");

    private GameActivity activity;

    public GameView(final Context ctx, final AttributeSet attrset) {
        super(ctx, attrset);

        context = ctx;
        SurfaceHolder holder = getHolder();

        holder.addCallback(this);

        gestureDetector = new GestureDetector(this.getContext(), this);

        // Initialise paints
        cornerBoxTextPaint = new Paint();
        cornerBoxTextPaint.setColor(Color.WHITE);
        cornerBoxTextPaint.setStyle(Paint.Style.FILL);
        cornerBoxTextPaint.setTextSize(32);
        /* uncomment for better text, worse fps */
        // cornerBoxTextPaint.setAntiAlias(true);

        cornerBoxBackPaint = new Paint();
        cornerBoxBackPaint.setARGB(150, 0, 0, 0);

        unitHealthPaint = new Paint();
        unitHealthPaint.setColor(Color.WHITE);
        unitHealthPaint.setStyle(Paint.Style.FILL);
        unitHealthPaint.setAntiAlias(true);
        unitHealthPaint.setTextSize(48); // unit hp text
        unitHealthPaint.setTextAlign(Align.RIGHT);

        unitHealthOutlinePaint = new Paint();
        unitHealthOutlinePaint.setColor(Color.BLACK);
        unitHealthOutlinePaint.setStyle(Paint.Style.STROKE);
        unitHealthOutlinePaint.setStrokeWidth(4);
        unitHealthOutlinePaint.setAntiAlias(unitHealthPaint.isAntiAlias());
        unitHealthOutlinePaint.setTextSize(unitHealthPaint.getTextSize());
        unitHealthOutlinePaint.setTextAlign(unitHealthPaint.getTextAlign());

        capPaint = new Paint();
        capPaint.setColor(Color.WHITE);
        capPaint.setStyle(Paint.Style.FILL);
        capPaint.setAntiAlias(true);
        capPaint.setTextSize(48); // unit hp text
        capPaint.setTextAlign(Align.RIGHT);

        capOutlinePaint = new Paint();
        capOutlinePaint.setColor(Color.BLACK);
        capOutlinePaint.setStyle(Paint.Style.STROKE);
        capOutlinePaint.setStrokeWidth(4);
        capOutlinePaint.setAntiAlias(capPaint.isAntiAlias());
        capOutlinePaint.setTextSize(capPaint.getTextSize());
        capOutlinePaint.setTextAlign(capPaint.getTextAlign());

        movedPaint = new Paint();
        movedPaint.setColor(-9505945);
        movedPaint.setStyle(Paint.Style.FILL);
        movedPaint.setAntiAlias(true);
        movedPaint.setTextSize(58); // unit hp text
        movedPaint.setTextAlign(Align.RIGHT);

        movedOutlinePaint = new Paint();
        movedOutlinePaint.setColor(Color.BLACK);
        movedOutlinePaint.setStyle(Paint.Style.STROKE);
        movedOutlinePaint.setStrokeWidth(4);
        movedOutlinePaint.setAntiAlias(movedPaint.isAntiAlias());
        movedOutlinePaint.setTextSize(movedPaint.getTextSize());
        movedOutlinePaint.setTextAlign(movedPaint.getTextAlign());

    }

    public void setState(final GameState stateS, final GameActivity activityS) {
        state = stateS;
        map = state.getMap();
        logic = state.getLogic();
        activity = activityS;

        /* Load and colour all the sprites we'll need */
        Log.d(TAG, "Initialising graphics.");
        initialiseGraphics();
        Log.d(TAG, "Done initalising graphics.");
    }

    public void addDamagedUnit(final Unit unit) {
        for (Pair<Unit, Long> uPair : damagedUnits) {
            if (uPair.getLeft().equals(unit)) {
                /* Update time */
                uPair.setRight(System.currentTimeMillis());
                return;
            }
        }

        damagedUnits
        .add(new Pair<Unit, Long>(unit, System.currentTimeMillis()));
    }

    private void initialiseGraphics() {
        final int NEUTRAL_FLAG_COLOUR = Color.rgb(241, 245, 252);// -6513508; //
        // teamcolour
        // //Color.rgb(156,
        // 156, 156)
        // //-6513508

        /* Register game fields */
        putGroup("Fields", map.getGameFieldMap());
        putGroup("Units", map.getUnitMap());
        putGroup("Buildings", map.getBuildingMap());

        putResource("Misc", "devairfac", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "devairfac"); // the buildings to swap
        putResource("Misc", "devlandfac", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "devlandfac");
        putResource("Misc", "devseafac", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "devseafac");
        putResource("Misc", "devoilfield", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "devoilfield");
        putResource("Misc", "devoilrig", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "devoilrig");
        putResource("Misc", "devhq", "drawable", "uk.co.fuuzetsu.turnofwar", "devhq");
        putResource("Misc", "flag", "drawable", "uk.co.fuuzetsu.turnofwar", "flag");
        putResource("Misc", "stealthed", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "stealthed");

        /* Load selector and highlighters */
        putResource("Highlighters", "selector", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "selector");
        putResource("Highlighters", "highlight", "drawable", "uk.co.fuuzetsu.turnofwar",
                    "highlighter");
        putResource("Highlighters", "path_highlight", "drawable",
                    "uk.co.fuuzetsu.turnofwar", "pathHighlighter");
        putResource("Highlighters", "attack_highlight", "drawable",
                    "uk.co.fuuzetsu.turnofwar", "attackHighlighter");
        putResource("Highlighters", "target_highlight", "drawable",
                    "uk.co.fuuzetsu.turnofwar", "targetHighlighter");
        loadBorders();
        /* Prerender combined map */

        map.setImage((BitmapChanger.combineMap(map, tilesize, graphics)));
        recycleBorders();
        /* Colour and save sprites for each player */
        for (Player p : state.getPlayers()) {
            /* Flag */

            Bitmap flagBitmap = graphics.get("Misc").get("flag");
            Bitmap stealthed = graphics.get("Misc").get("stealthed");
            int itsaflag = 1; // dont want to flip the flag
            Bitmap colourFlag = BitmapChanger.changeColour(itsaflag,
                                flagBitmap, NEUTRAL_FLAG_COLOUR, p.getColour());
            p.setFlag(colourFlag);
            itsaflag = 0;
            /* All possible units */
            Map<String, Bitmap> personalUnits = new HashMap<String, Bitmap>();
            for (Map.Entry<String, Bitmap> uGfx : graphics.get("Units")
                    .entrySet()) {

                Bitmap uBmap = uGfx.getValue();

                if (uBmap == null) { // this if statement block does the dev
                    // textures if originals are missing
                    String original = uGfx.getKey();
                    String trimmed = "dev"
                                     + original.toLowerCase(Locale.ENGLISH); // terrible
                    // bugs
                    // otherwise
                    if (uGfx.getKey().equals(original)) {
                        putResource("Misc", trimmed, "drawable",
                                    "uk.co.fuuzetsu.turnofwar", trimmed);
                        uBmap = graphics.get("Misc").get(trimmed);
                        itsaflag = 1; // dont flip
                        Bitmap personal = BitmapChanger.changeColour(itsaflag,
                                          uBmap, NEUTRAL_FLAG_COLOUR, p.getColour());
                        personalUnits.remove(original);
                        personalUnits.put(uGfx.getKey(), personal);
                    }
                }

                else if ((!state.getCurrentPlayer().equals(p))
                         && (uGfx.getKey().equals("Stealth") || uGfx.getKey()
                             .equals("Submarine"))) {
                    personalUnits.put(uGfx.getKey(), stealthed);
                } else {
                    Bitmap personal = BitmapChanger.changeColour(itsaflag,
                                      uBmap, NEUTRAL_FLAG_COLOUR, p.getColour());
                    personalUnits.put(uGfx.getKey(), personal);
                }
                // personalUnits.put(uGfx.getKey(), personal);
            }
            p.setUnitSprites(personalUnits);
        }
    }

    private void stealth() { // slim clone of initialise graphics()
        final int NEUTRAL_FLAG_COLOUR = Color.rgb(241, 245, 252);// -6513508; //
        // teamcolour
        // //Color.rgb(156,
        // 156, 156)
        // //-6513508
        Bitmap stealthed = graphics.get("Misc").get("stealthed");
        int itsaflag = 0;
        for (Player p : state.getPlayers()) {

            Map<String, Bitmap> personalUnits = new HashMap<String, Bitmap>();
            for (Map.Entry<String, Bitmap> uGfx : graphics.get("Units")
                    .entrySet()) {
                Bitmap uBmap = uGfx.getValue();

                if (uBmap == null) { // this if statement does the dev textures
                    // if originals are missing
                    String original = uGfx.getKey();
                    String trimmed = "dev"
                                     + original.toLowerCase(Locale.ENGLISH); // terrible
                    // bugs
                    // otherwise
                    if (uGfx.getKey().equals(original)) {
                        putResource("Misc", trimmed, "drawable",
                                    "uk.co.fuuzetsu.turnofwar", trimmed);
                        uBmap = graphics.get("Misc").get(trimmed);
                        itsaflag = 1; // dont flip
                        Bitmap personal = BitmapChanger.changeColour(itsaflag,
                                          uBmap, NEUTRAL_FLAG_COLOUR, p.getColour());
                        personalUnits.remove(original);
                        personalUnits.put(uGfx.getKey(), personal);
                    }
                }

                if ((!state.getCurrentPlayer().equals(p))
                        && (uGfx.getKey().equals("Stealth") || uGfx.getKey()
                            .equals("Submarine"))) {
                    personalUnits.put(uGfx.getKey(), stealthed);
                } else {
                    Bitmap personal = BitmapChanger.changeColour(itsaflag,
                                      uBmap, NEUTRAL_FLAG_COLOUR, p.getColour());
                    personalUnits.put(uGfx.getKey(), personal);
                }
            }
            p.setUnitSprites(personalUnits);
        }
    }

    private <T extends DrawableMapObject> void putGroup(final String category,
            final Map<Character, T> objMap) {
        graphics.put(category, new HashMap<String, Bitmap>());

        for (Map.Entry<Character, T> ent : objMap.entrySet()) {
            T f = ent.getValue();
            putResource(category, f.getSpriteLocation(), f.getSpriteDir(),
                        f.getSpritePack(), f.getName());
        }
    }

    private void putResource(final String category, final String resName,
                             final String resDir, final String resPack, final String regName) {
        Integer resourceID = getResources().getIdentifier(resName, resDir,
                             resPack);
        Bitmap bMap = BitmapFactory.decodeResource(context.getResources(),
                      resourceID);

        if (!graphics.containsKey(category)) {
            graphics.put(category, new HashMap<String, Bitmap>());
        }

        graphics.get(category).put(regName, bMap);
    }

    /* Helper for loadBorders() */
    private void loadField(final String resName, final String regName) {
        putResource("Fields", resName, "drawable", "uk.co.fuuzetsu.turnofwar", regName);
    }

    private void loadBorders() {
        List<String> borderList = new ArrayList<String>();
        borderList.add("sand");
        borderList.add("grass");
        borderList.add("forest");
        borderList.add("mountain");
        for (String b : borderList) {
            for (Integer i = 1; i <= 4; i++) {
                loadField(String.format("border_%s_%d", b, i),
                          String.format("border %s %d", b, i));
                loadField(String.format("corner_%s_%d", b, i),
                          String.format("corner %s %d", b, i));
                loadField(String.format("fullcorner_%s_%d", b, i),
                          String.format("fullcorner %s %d", b, i));
            }
        }
    }

    private void recycleBorders() {
        List<String> borderList = new ArrayList<String>();
        borderList.add("sand");
        borderList.add("grass");
        borderList.add("forest");
        borderList.add("mountain");

        for (String b : borderList) {
            for (Integer i = 1; i <= 4; i++) {
                graphics.get("Fields").get(String.format("border %s %d", b, i))
                .recycle();
                graphics.get("Fields").get(String.format("corner %s %d", b, i))
                .recycle();
                graphics.get("Fields")
                .get(String.format("fullcorner %s %d", b, i)).recycle();
            }
        }
    }

    @Override
    public void surfaceChanged(final SurfaceHolder arg0, final int arg1,
                               final int arg2, final int arg3) {
    }

    @Override
    public void surfaceCreated(final SurfaceHolder arg0) {
        dt = new DrawingThread(arg0, context, this);
        dt.setRunning(true);
        dt.start();
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder arg0) {
        dt.setRunning(false);

        try {
            dt.join();
        } catch (InterruptedException e) {
            return;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        /* Functionality moved to onSingleTapConfirmed() */
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public RectF getSquare(final float x, final float y, final float length) {
        return new RectF(x, y, x + length, y + length);
    }

    public void doDraw(final Canvas canvas) {
        state.startFrame();

        if (map == null || state.isGameFinished()) {
            return; /* don't bother drawing */
        }

        canvas.drawColor(Color.BLACK);

        GameField selectedField;

        if (map.isValidField(selected)) {
            selectedField = map.getField(selected);
        } else {
            selectedField = map.getField(0, 0);
        }

        List<Position> unitDests = state.getUnitDestinations(selectedField);

        Player player = state.getCurrentPlayer();

        Paint playerPaint = new Paint();
        playerPaint.setColor(player.getColour());

        canvas.drawBitmap(map.getImage(), scrollOffset.getX(),
                          scrollOffset.getY(), null);

        for (int i = 0; i < map.getWidth(); ++i) {
            for (int j = 0; j < map.getHeight(); j++) {
                GameField gf = map.getField(i, j);
                RectF dest = getSquare(tilesize * i + scrollOffset.getX(),
                                       tilesize * j + scrollOffset.getY(), tilesize);

                if (gf.hostsBuilding()) {
                    Player owner = gf.getBuilding().getOwner();

                    /* TODO proper Gaia handling */
                    Integer remainingHp = gf.getBuilding()
                                          .getRemainingCaptureTime();
                    if (owner.getName().equals("Gaia")) {
                        canvas.drawBitmap(graphics.get("Misc").get("flag"),
                                          null, dest, null);
                    } else {
                        canvas.drawBitmap(owner.getFlag(), null, dest, null);

                    }
                    if (remainingHp != 20) { // show capture text
                        String captureText = decformat.format(remainingHp);
                        canvas.drawText(captureText, dest.left + 115,
                                        dest.top + 40, capOutlinePaint);
                        canvas.drawText(captureText, dest.left + 115,
                                        dest.top + 40, capPaint);
                    }
                }

                if (gf.hostsUnit()) {
                    Unit unit = gf.getUnit();

                    String un = unit.toString();
                    Player owner = unit.getOwner();

                    /* TODO proper Gaia handling */
                    if (owner.getName().equals("Gaia")) {

                        canvas.drawBitmap(graphics.get("Units").get(un), null,
                                          dest, null);
                    } else {
                        Bitmap unitGfx = owner.getUnitSprite(un);

                        if (unit.isBoat()) { // raise boats
                            dest = getSquare(
                                       tilesize * i + scrollOffset.getX(),
                                       tilesize * j + scrollOffset.getY() - 30,
                                       tilesize);
                        }

                        canvas.drawBitmap(unitGfx, null, dest, null);
                        Double ceilHealth = Math.ceil(unit.getHealth()); //
                        String healthText;
                        if (unit.getHealth() > 1) {
                            healthText = decformat.format(Math.round(unit
                                                          .getHealth()));//
                        } else {
                            healthText = decformat.format(ceilHealth);
                        }

                        double fullHpCheck = Double.parseDouble(healthText);
                        if (unit.hasFinishedTurn() == true
                                && unit.getOwner() == state.getCurrentPlayer()) {
                            canvas.drawText("x", dest.left + 40,
                                            dest.bottom - 2, movedOutlinePaint);
                            canvas.drawText("x", dest.left + 40,
                                            dest.bottom - 2, movedPaint);

                        }
                        if (fullHpCheck != 10) {
                            if ((unit.getOwner() != state.getCurrentPlayer())
                                    && (unit.getName().equals("Stealth") || (unit
                                            .getName().equals("Submarine")))) {

                            } else {
                                canvas.drawText(healthText, dest.left + 115,
                                                dest.bottom - 1, unitHealthOutlinePaint);
                                canvas.drawText(healthText, dest.left + 115,
                                                dest.bottom - 1, unitHealthPaint);
                            }
                        }
                    }
                }
            }
        }

        Iterator<Pair<Unit, Long>> iter = damagedUnits.iterator();

        while (iter.hasNext()) {
            Pair<Unit, Long> uPair = iter.next();
            Unit currentUnit = uPair.getLeft();

            if (currentUnit == null || currentUnit.isDead()) {
                // MediaPlayer mp = MediaPlayer.create(context, R.raw.cannhvy1);
                // // death sound

                // mp.start();
            }
            iter.remove();
            continue;
        }

        highlightPositions(canvas, unitDests, "highlighter");
        highlightPositions(canvas, state.getCurrentPath(), "pathHighlighter");
        highlightPositions(canvas, state.getAttackables(), "attackHighlighter");
        highlightPositions(canvas, state.getTargets(), "targetHighlighter");

        RectF selectDest = getSquare(
                               tilesize * selected.getX() + scrollOffset.getX(), tilesize
                               * selected.getY() + scrollOffset.getY(), tilesize);
        canvas.drawBitmap(graphics.get("Highlighters").get("selector"), null,
                          selectDest, null);

        double offsetTiles = -scrollOffset.getX() / (double) tilesize;
        double tpw = canvas.getWidth() / (double) tilesize;
        boolean infoLeft = (selected.getX() - offsetTiles) > (tpw / 2);

        if (selectedField.hostsUnit()) {
            drawInfoBox(canvas, selectedField.getUnit(), selectedField,
                        infoLeft);
        } else {
            drawInfoBox(canvas, null, selectedField, infoLeft);
        }

        drawCornerBox(canvas, true, true, "Turn " + state.getTurns());

        String fpsS = decformat.format(state.getFps());
        drawCornerBox(canvas, false, true,
                      player.getName() + " - $" + player.getGoldAmount() + " Mil"
                      + "\nFPS: " + fpsS, true, playerPaint);

        state.endFrame();
    }

    public void highlightPositions(final Canvas canvas,
                                   final Collection<Position> positions,
                                   final String highlighter) { // /////////////////////////
        for (Position pos : positions) {
            RectF dest = getSquare(tilesize * pos.getX() + scrollOffset.getX(),
                                   tilesize * pos.getY() + scrollOffset.getY(), tilesize);
            canvas.drawBitmap(graphics.get("Highlighters").get(highlighter),
                              null, dest, null);
        }
    }

    public float getMapDrawWidth() {
        return map.getWidth() * tilesize;
    }

    public float getMapDrawHeight() {
        return map.getHeight() * tilesize;
    }

    public void drawInfoBox(final Canvas canvas, final Unit unit,
                            final GameField field, final boolean left) {

        String info = field.getInfo();
        if (!field.hostsBuilding()) {
            info += field.getName() + "\n";
        } else if (field.hostsBuilding()) {
            info += field.getBuilding().getInfo();
        }
        if (unit != null && !unit.isRanged()
                && !unit.getName().equals("Stealth")
                && !unit.getName().equals("Submarine")) {
            info += unit.getInfo();

        } else if (unit != null && unit.getName().equals("Stealth")
                   && (unit.getOwner() == state.getCurrentPlayer())) {
            info += unit.getInfo();
        } else if (unit != null && unit.getName().equals("Submarine")
                   && (unit.getOwner() == state.getCurrentPlayer())) {
            info += unit.getInfo();
        } else if (unit != null && unit.getName().equals("RocketTruck")) {
            info += unit.getInfo() + "Fires 2-4 Vs Ground + Naval non sub";
        } else if (unit != null && unit.getName().equals("AntiAir")) {
            info += unit.getInfo() + "Fires 3-5 Vs Air, non stealth";
        } else if (unit != null && unit.getName().equals("AntiAirBoat")) {
            info += unit.getInfo() + "Fires 3-5 Vs Air, non stealth";
        } else if (unit != null && unit.getName().equals("Battleship")) {
            info += unit.getInfo() + "Fires 2-6 Vs Ground + Naval non sub";
        } else if (unit != null && unit.getName().equals("MissileSub")) {
            info += unit.getInfo() + "Fires 3-8 Vs Ground + Naval non sub";
        }
        drawCornerBox(canvas, left, false, info);
    }

    public void drawCornerBox(final Canvas canvas, final boolean left,
                              final boolean top, final String text) {
        drawCornerBox(canvas, left, top, text, false, null);
    }

    public void drawCornerBox(final Canvas canvas, final boolean left,
                              final boolean top, final String text, final boolean box,
                              final Paint boxPaint) {

        if (left) {
            cornerBoxTextPaint.setTextAlign(Paint.Align.LEFT);
        } else {
            cornerBoxTextPaint.setTextAlign(Paint.Align.RIGHT);
        }

        String[] ss = text.split("\n");
        String longestLine = "";

        for (String s : ss) {
            if (s.length() > longestLine.length()) {
                longestLine = s;
            }
        }

        Rect bounds = new Rect();
        cornerBoxTextPaint.getTextBounds(longestLine, 0, longestLine.length(),
                                         bounds);
        Integer boxWidth = bounds.width(); // Might have to Math.ceil first
        Integer boxHeight = ss.length * bounds.height();

        Rect backRect = new Rect(left ? 0 : canvas.getWidth() - boxWidth,
                                 top ? 0 : canvas.getHeight() - boxHeight, left ? boxWidth
                                 : canvas.getWidth(), top ? boxHeight
                                 : canvas.getHeight());
        float radius = 5f;
        RectF backRectF = new RectF(backRect.left - radius
                                    - (box && !left ? radius + boxHeight : 0), backRect.top
                                    - radius, backRect.right + radius
                                    + (box && left ? radius + boxHeight : 0), backRect.bottom
                                    + radius);
        canvas.drawRoundRect(backRectF, 5f, 5f, cornerBoxBackPaint);

        if (box) {
            canvas.drawRect(new RectF(left ? backRect.right + radius
                                      : backRect.left - radius - boxHeight, backRect.top,
                                      left ? backRect.right + radius + boxHeight : backRect.left
                                      - radius, backRect.top + boxHeight), boxPaint);
        }

        for (Integer i = 0; i < ss.length; ++i) {
            canvas.drawText(ss[i], 0, ss[i].length(), left ? backRect.left
                            : backRect.right, backRect.top
                            + (bounds.height() * (i + 1)), cornerBoxTextPaint);
        }
    }

    @Override
    public boolean onDown(final MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2,
                           final float velocityX, final float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
    }

    @Override
    public void onShowPress(final MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(final MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event) {
        /* Coordinates of the pressed tile */
        int touchX = (int)((event.getX() - scrollOffset.getX()) / tilesize);
        int touchY = (int)((event.getY() - scrollOffset.getY()) / tilesize);

        Position newselected = new Position(touchX, touchY);

        if (this.map.isValidField(touchX, touchY)) {

            if (!state.getCurrentPlayer().isAi()) {
                GameField newselectedField = map.getField(newselected);
                // String A = "yolo";
                // Log.v(A, "state" + state.getAttackables());
                if (!state.getAttackables().contains(newselected)) { // allow
                    // units
                    // to
                    // move
                    // to
                    // squares
                    // which
                    // are
                    // highlighted
                    // by
                    // attack
                    // if
                    // (!state.getAttackables().contains(newselected)
                    // ||
                    // !(state.getAttackables().contains(newselected)
                    // &&
                    // !(map.getField(selected).hostsUnit())))
                    // {
                    if (map.getField(selected).hostsUnit()) {
                        // Log.d(TAG, "A unit is selected!");
                        /*
                         * If the user currently has a unit selected and selects
                         * a field that this unit could move to (and the unit
                         * has not finished it's turn)
                         */
                        GameField selectedField = map.getField(selected);
                        Unit unit = selectedField.getUnit();

                        if (unit.getOwner().equals(state.getCurrentPlayer())
                                && !unit.hasFinishedTurn()
                                && (!map.getField(newselected).hostsUnit() || selected
                                    .equals(newselected))) {
                            List<Position> unitDestinations = state
                                                              .getUnitDestinations(selectedField);
                            if (unit.hasMoved()
                                    && (((unit.getName() != "Infantry") || (unit
                                            .getName() != "Mech"))
                                        && !newselectedField
                                        .hostsBuilding() && !selectedField
                                        .hostsBuilding())) { // infantry
                                // on
                                // building
                                // dont
                                // get
                                // marked
                                // as
                                // moved
                                unit.setFinishedTurn(true);
                            } else {// if the building belongs to the player
                                // then mark the infantry as moved anyway
                                if (unit.hasMoved()
                                        && (selectedField.hostsBuilding())) {
                                    if (selectedField.getBuilding().getOwner()
                                            .equals(state.getCurrentPlayer())) {
                                        unit.setFinishedTurn(true);
                                    }
                                }
                            }

                            if (unitDestinations.contains(newselected)
                                    || selected.equals(newselected)) {
                                if (state.getCurrentPath()
                                        .contains(newselected)) {
                                    state.move(unit, newselected);
                                    if (unit.isRanged()) {
                                        unit.setFinishedTurn(true);
                                    }
                                } else {
                                    state.setPath(logic.findPath(map, unit,
                                                                 newselected));
                                    newselected = selected;
                                }
                            }
                        }
                    } else if (!newselectedField.hostsUnit()
                               && newselectedField.hostsBuilding()) {
                        state.setPath(null);
                        Building building = map.getField(newselected)
                                            .getBuilding();

                        if (building.getOwner()
                                .equals(state.getCurrentPlayer())
                                && building.canProduceUnits()) {
                            AlertDialog.Builder buildmenuBuilder = new AlertDialog.Builder(
                                this.getContext());
                            String menuTitle = "";
                            if (building.getName().equals("airfac")) {
                                menuTitle = "Airport";
                            } else if ((building.getName()).equals("landfac")) {
                                menuTitle = "Land Factory";
                            } else if ((building.getName()).equals("seafac")) {
                                menuTitle = "Shipyard";
                            }

                            buildmenuBuilder.setTitle(menuTitle);

                            List<Unit> units = building.getProducibleUnits();
                            String[] buildableNames = new String[units.size() + 1];

                            for (int i = 0; i < units.size(); ++i) {
                                Unit cU = units.get(i);
                                buildableNames[i] = String.format(
                                                        "%s   $%s Million", cU.toString(),
                                                        cU.getProductionCost());
                            }

                            buildableNames[units.size()] = "Cancel";
                            buildmenuBuilder.setItems(buildableNames, this);
                            buildmenuBuilder.create().show();

                        } // build menu isn't shown if it isn't the user's turn
                    }
                } else { // attack
                    GameField field = map.getField(selected);
                    Unit attacker = field.getUnit();
                    Unit defender = map.getField(newselected).getUnit();

                    if (state.getCurrentPlayer().equals(attacker.getOwner())) { // if
                        // (state.getCurrentPlayer().equals(attacker.getOwner())
                        // &&
                        // defender
                        // !=
                        // null)
                        // {
                        // you
                        // can
                        // only
                        // attack
                        // with
                        // your
                        // own
                        // units
                        // on
                        // your
                        // own
                        // go
                        // and
                        // there
                        // has
                        // to
                        // be
                        // a
                        // defending
                        // unit
                        Log.d(TAG, "attack(!): " + attacker + " attacks "
                              + defender);
                        state.attack(attacker, defender);
                        attacker.setFinishedTurn(true);
                    }
                }
            }
            selected = newselected;
        }

        return true;
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
                            final float distanceX, final float distanceY) {

        float newX = scrollOffset.getX() - distanceX;

        if (this.getWidth() >= this.getMapDrawWidth()) {
            newX = 0;
        } else if ((-newX) > (getMapDrawWidth() - getWidth())) {
            newX = -(getMapDrawWidth() - getWidth());
        } else if (newX > 0) {
            newX = 0;
        }

        float newY = scrollOffset.getY() - distanceY;

        if (this.getHeight() >= this.getMapDrawHeight()) {
            newY = 0;
        } else if ((-newY) > (getMapDrawHeight() - getHeight())) {
            newY = -(getMapDrawHeight() - getHeight());
        } else if (newY > 0) {
            newY = 0;
        }

        scrollOffset = new FloatPair(newX, newY);

        return true;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        Log.d(TAG, "selected option: " + which);

        // switch (whichMenu) {
        // case BUILD:
        GameField field = map.getField(selected);

        if (field.hostsBuilding()
                && (field.getBuilding().getProducibleUnits().size() > which)
                && state.getCurrentPlayer().equals(
                    field.getBuilding().getOwner())) {
            Unit unit = map.getField(selected).getBuilding()
                        .getProducibleUnits().get(which);
            Log.d(TAG, "building a " + unit);
            Boolean result = state.produceUnit(map.getField(selected), unit);

            if (!result) {
                alertMessage(String.format("Insufficient funds to buy %s",
                                           unit, unit.getProductionCost()));
            }
        } else if (field.getBuilding().getProducibleUnits().size() != which) {
            // how did the user manage that?
            alertMessage("It's not the building's owner's turn"
                         + ", or there is no building");
        }
    }

    private void alertMessage(final String text) {
        new AlertDialog.Builder(context).setMessage(text)
        .setPositiveButton("OK", null).show();
    }

    @Override
    public void onClick(final View arg0) {
        // This onClick is for the Menu button
        try {
            state.nextPlayer();
            stealth(); // stealth planes and subs
            Log.d(TAG, "advancing player");
            Toast.makeText(context,
                           String.format("%s's turn!", state.getCurrentPlayer()),
                           Toast.LENGTH_SHORT).show();
        } catch (GameFinishedException e) {
            Player winner = e.getWinner();
            Toast.makeText(context,
                           String.format("%s has won the game!", winner.getName()),
                           Toast.LENGTH_SHORT).show();
            Log.d(TAG, state.getStatistics().toString());
            dt.setRunning(false);
            activity.endGame();
        }
    }
}
