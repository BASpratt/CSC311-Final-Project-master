/*
 * Copyright (c) 2015. Anthony DeDominic
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Edited by B. Spratt 4/27/2015
 */

package pw.dedominic.csc311_final_project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;

import java.util.Random;

/**
 * A view where the battle event occurs
 */
public class BattleView extends View
{
    private final Random random = new Random();

    private boolean isGameRunning = false;
    private boolean isStateController = false;

    // game pieces
    private Artillery artillery_enemy;
    private Artillery artillery_player;
    private Paint color;

    // game layout constants
    private float artillery_height;
    private float artillery_width;
    private float artillery_space;

    private float building_height;
    private float building_width;

    private boolean IS_READY = false;
    private boolean IS_READY2 = false;

    private Missile mMissile;

    private Building mBuilding;

    private DrawTimer mDrawTimer = new DrawTimer();

    double first_x, first_y, last_x, last_y;

    public BattleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        color = new Paint();
        color.setColor(Constants.PAINT_COLOR);
    }

    private void newGame()
    {
        float x1 = newArtilleryPos() + artillery_space;
        float x2 = newArtilleryPos() + artillery_space;
        float y = getHeight() - artillery_space;
        artillery_player = new Artillery(x1, y);
        artillery_enemy = new Artillery(getWidth() - x2, y);

        building_height = newBuildingHeight();
        building_width = newBuildingWidth();

        float xB = getWidth() / 2;
        float yB = getHeight();
        mBuilding = new Building(xB, yB);

        //set roots
        artillery_player.setRootX(x1);
        artillery_player.setRootY(y);
        artillery_enemy.setRootX(x2);
        artillery_enemy.setRootY(y);
    }

    public float newArtilleryPos()
    {
        float max = getWidth() / 4;
        return (float)(random.nextInt(((int)max)) + 1);
    }

    public float newBuildingHeight()
    {
        float max = getHeight() / 2;
        float min = getHeight() / 4;
        return ((random.nextInt((int)(max - min) + 1) + min));
    }

    public float newBuildingWidth()
    {
        float min = getWidth() / 14;
        float max = getWidth() / 10;
        return ((random.nextInt((int)(max - min) + 1) + min));
    }

    private void setLayoutConstants()
    {
        artillery_height = getWidth() / Constants.ARTILLERY_HEIGHT_FRAC;
        artillery_width = getWidth() / Constants.ARTILLERY_WIDTH_FRAC;
        artillery_space = getWidth() / Constants.ARTILLERY_SPACE_FRAC;
    }

    private void checkMissileCollision()
    {
        if (mMissile.y >= mBuilding.getTop() &&
                mMissile.y <= mBuilding.getBottom() &&
                mMissile.x >= mBuilding.getLeft() &&
                mMissile.x <= mBuilding.getRight())
        {
            mMissile.setDeleteMissile(true);
        }

        if (mMissile.getX_vel()<0)
        {
            if (mMissile.getY() >= artillery_player.getTop() &&
                    mMissile.getY() <= artillery_player.getBottom() &&
                    mMissile.getX() <= artillery_player.getRight() &&
                    mMissile.getX() >= artillery_player.getLeft())
            {
                mMissile.setDeleteMissile(true);
                gameOver();
            }
        }
        else
        {
            if (mMissile.getY() >= artillery_enemy.getTop() &&
                    mMissile.getY() <= artillery_enemy.getBottom() &&
                    mMissile.getX() <= artillery_enemy.getRight() &&
                    mMissile.getX() >= artillery_enemy.getLeft())
            {
                mMissile.setDeleteMissile(true);
                gameOver();
            }
        }
    }

    public void update()
    {
        if (getHeight() == 0 || getWidth() == 0)
        {
            mDrawTimer.sleep(1);
            return;
        }

        if (!isGameRunning)
        {
            setLayoutConstants();
            newGame();
            mDrawTimer.sleep(1);
            isGameRunning = true;
            return;
        }
        if (isStateController)
        {
            checkMissileCollision();
        }

        if (!IS_READY)
        {
            IS_READY = true;
        }

        mDrawTimer.sleep(1000 / 60);
    }

    public void gameOver()
    {
        newGame();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (!isGameRunning)
        {
            return;
        }

        artillery_player.draw(canvas);
        artillery_enemy.draw(canvas);
        mBuilding.draw(canvas);

        if (!IS_READY)
        {
            return;
        }

        if (IS_READY2)
        {
            if (!mMissile.getDeleteMissile())
            {
                mMissile.draw(canvas);
                isStateController = true;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        switch (e.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                first_x = e.getRawX();
                first_y = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                last_x = e.getRawX();
                last_y = e.getRawY();

                double distance = getPointDistance(first_x, first_y, last_x, last_y);
                double radian = Math.atan2(first_y - last_y, first_x - last_x);
                setMissile(distance, radian);
                break;
        }
        return true;
    }

    public double getPointDistance(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public void setMissile(double distance, double rads)
    {
        float x_vel = (float)Math.cos(rads) * (float)(distance * .1);
        float y_vel = (float)Math.sin(rads) * (float)(distance * .1);

        mMissile = new Missile(artillery_player.getRootX(), artillery_player.getRootY(), x_vel, y_vel, 0xFF000000);
        IS_READY2 = true;
    }

    private class DrawTimer extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            invalidate();
            update();
        }

        public void sleep(long milliseconds)
        {
            removeMessages(0);
            sendMessageDelayed(obtainMessage(0), milliseconds);
        }
    }

    private class Missile
    {
        private float x;
        private float y;

        private float x_vel;
        private float y_vel;

        private boolean deleteMissile;

        private float radius = 50;

        Paint paint;

        public Missile(float _x, float _y, float velX, float velY, int color)
        {
            x = _x;
            y = _y;
            x_vel = velX;
            y_vel = velY;

            paint = new Paint();
            paint.setColor(color);
        }

        public float getX_vel()
        {
            return x_vel;
        }

        public float getY()
        {
            return y;
        }

        public float getX()
        {
            return x;
        }

        public void setDeleteMissile(boolean _deleteMissile)
        {
            deleteMissile = _deleteMissile;
        }

        public boolean getDeleteMissile()
        {
            return deleteMissile;
        }

        public void draw(Canvas canvas)
        {
            canvas.drawCircle(x, y, radius, paint);

            x += x_vel;
            y += y_vel;
            y_vel += .5; // gravity
        }
    }

    private class Artillery
    {
        private float x;
        private float y;
        private float rootX;
        private float rootY;

        public Artillery(float _x, float _y)
        {
            x = _x;
            y = _y;
        }

        public void setRootX(float newX)
        {
            rootX = newX;
        }

        public void setRootY(float newY)
        {
            rootY = newY;
        }

        public float getRootX()
        {
            return rootX;
        }

        public float getRootY()
        {
            return rootY;
        }

        public float getBottom()
        {
            return y + artillery_height;
        }

        public float getTop()
        {
            return y - artillery_height;
        }

        public float getLeft()
        {
            return x - artillery_width;
        }

        public float getRight()
        {
            return x + artillery_width;
        }

        public void draw(Canvas screen)
        {
            screen.drawRect(getLeft(),
                    getTop(),
                    getRight(),
                    getBottom(),
                    color);
        }
    }

    private class Building
    {
        private float x;
        private float y;

        public Building(float _x, float _y)
        {
            x = _x;
            y = _y;
        }

        public float getBottom()
        {
            return y + building_height;
        }

        public float getTop()
        {
            return y - building_height;
        }

        public float getLeft()
        {
            return x - building_width;
        }

        public float getRight()
        {
            return x + building_width;
        }

        public void draw(Canvas screen)
        {
            screen.drawRect(getLeft(),
                    getTop(),
                    getRight(),
                    getBottom(),
                    color);
        }
    }
}