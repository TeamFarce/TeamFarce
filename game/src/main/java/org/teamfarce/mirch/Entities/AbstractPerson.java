package org.teamfarce.mirch.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import org.teamfarce.mirch.Assets;
import org.teamfarce.mirch.Settings;
import org.teamfarce.mirch.Vector2Int;

import java.util.*;

/**
 * Created by brookehatton on 01/02/2017.
 */
public abstract class AbstractPerson extends MapEntity
{
    /**
     * The height of the texture region for each person
     */
    protected static int SPRITE_HEIGHT = 48;

    /**
     * The width of the texture region for each person
     */
    protected static int SPRITE_WIDTH = 32;

    /**
     * This is whether the NPC can move or not. It is mainly used to not let them move during converstation
     */
    public boolean canMove = true;

    /**
     * This stores the current region of the above texture that is to be drawn
     * to the map
     */
    protected TextureRegion currentRegion;

    /**
     * This stores the sprite sheet of the Player/NPC
     */
    protected Texture spriteSheet;

    /**
     * This stores a list of tiles that have been found by the A* Search Algorithm. The NPC/Player needs
     * to keep following these tiles until empty.
     */
    private List<Vector2Int> toMoveTo = new ArrayList<Vector2Int>();

    Direction direction = Direction.SOUTH;
    PersonState state;
    private Vector2Int startTile = new Vector2Int(0,0);
    private Vector2Int endTile = new Vector2Int(0,0);
    private float animTimer;
    private float animTime = 0.35f;

    /**
     * Initialise the entity.
     *
     * @param name        The name of the entity.
     * @param description The description of the entity.
     * @param filename    The filename of the image to display for the entity.
     */
    public AbstractPerson(String name, String description, String filename)
    {
        super(name, description, filename);
        this.name = name;
        this.spriteSheet = Assets.loadTexture(filename);
        this.currentRegion = new TextureRegion(Assets.loadTexture(filename), 0, 0, SPRITE_WIDTH, SPRITE_HEIGHT);
        this.state = PersonState.STANDING;
    }

    /**
     * This controls the movement of a person
     */
    public abstract void move(Direction dir);

    public PersonState getState()
    {
        return state;
    }
    

    /**
     * This class is to sort a list of AbstractPerson in highest Y coordinate to lowest Y coordinate
     * <p>
     * It is used to render NPCs and the Player in the correct order to avoid it appearing as though someone
     * is standing on top of someone else
     */
    public static class PersonPositionComparator implements Comparator<AbstractPerson>
    {
        /**
         * This method compares the 2 objects.
         *
         * @param o1 - The first object to compare
         * @param o2 - The second object to compare
         * @return (int) if <0 o1 is considered to be first in the list
         */
        @Override
        public int compare(AbstractPerson o1, AbstractPerson o2)
        {
            return o2.getTileCoordinates().y - o1.getTileCoordinates().y;
        }
    }


    /**
     * This is called to update the players position.
     * Called from the game loop, it interpolates the movement so that the person moves smoothly from tile to tile.
     */
    public void update(float delta)
    {
        if (this.state == PersonState.WALKING) {

            this.setPosition(Interpolation.linear.apply(startTile.x * Settings.TILE_SIZE, endTile.x * Settings.TILE_SIZE, animTimer / animTime), Interpolation.linear.apply(startTile.y * Settings.TILE_SIZE, endTile.y * Settings.TILE_SIZE, animTimer / animTime));

            this.animTimer += delta;

            if (animTimer > animTime) {
                this.setTileCoordinates(endTile.x, endTile.y);
                this.finishMove();
            }
        }
        else
        {
            if (!toMoveTo.isEmpty())
            {
                Vector2Int next = toMoveTo.get(0);
                toMoveTo.remove(0);

                int xDiff = next.getX() - getTileX();
                int yDiff = next.getY() - getTileY();

                if (xDiff == 1)
                {
                    move(Direction.EAST);
                }
                else if (xDiff == -1)
                {
                    move(Direction.WEST);
                }
                else if (yDiff == 1)
                {
                    move(Direction.NORTH);
                }
                else if (yDiff == -1)
                {
                    move(Direction.SOUTH);
                }
            }
        }

        updateTextureRegion();
    }

    /**
     * Sets up the move, initialising the start position and destination as well as the state of the person.
     * This allows the movement to be smooth and fluid.
     *
     * @param dir the direction that the person is moving in.
     */
    public void initialiseMove(Direction dir)
    {
        getRoom().lockCoordinate(this.tileCoordinates.x + dir.getDx(), this.tileCoordinates.y + dir.getDy());

        this.direction = dir;

        this.startTile.x = this.tileCoordinates.x;
        this.startTile.y = this.tileCoordinates.y;

        this.endTile.x = this.startTile.x + dir.getDx();
        this.endTile.y = this.startTile.y + dir.getDy();
        this.animTimer = 0f;

        this.state = PersonState.WALKING;
    }


    /**
     * Finalises the move by resetting the animation timer and setting the state back to standing.
     * Called when the player is no longer moving.
     */
    public void finishMove()
    {
        animTimer = 0f;

        this.state = PersonState.STANDING;

        getRoom().unlockCoordinate(tileCoordinates.x, tileCoordinates.y);

        updateTextureRegion();
    }

    /**
     * Updates the texture region based upon how far though the animation time it is.
     */
    public void updateTextureRegion()
    {
        float quarter = animTime / 4;
        float half = animTime / 2;
        float threeQuarters = quarter * 3;

        int row = 1;

        switch (direction) {
            case NORTH:
                row = 3;
                break;
            case EAST:
                row = 2;
                break;
            case SOUTH:
                row = 0;
                break;
            case WEST:
                row = 1;
                break;
        }

        if (animTimer > threeQuarters) {
            setRegion(new TextureRegion(spriteSheet, 96, row * SPRITE_HEIGHT, SPRITE_WIDTH, SPRITE_HEIGHT));
        } else if (animTimer > half) {
            setRegion(new TextureRegion(spriteSheet, 0, row * SPRITE_HEIGHT, SPRITE_WIDTH, SPRITE_HEIGHT));
        } else if (animTimer > quarter) {
            setRegion(new TextureRegion(spriteSheet, 32, row * SPRITE_HEIGHT, SPRITE_WIDTH, SPRITE_HEIGHT));
        } else if (animTimer == 0) {
            setRegion(new TextureRegion(spriteSheet, 0, row * SPRITE_HEIGHT, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
    }

    public void clickedAt(int screenX, int screenY)
    {
        toMoveTo = aStarPath(new Vector2Int(8, 8));
    }

    public List<Vector2Int> aStarPath(Vector2Int destination)
    {
        List<Vector2Int> closedSet = new ArrayList<Vector2Int>();

        List<Vector2Int> openSet = new ArrayList<Vector2Int>();
        openSet.add(new Vector2Int(getTileX(), getTileY()));

        HashMap<Vector2Int, Integer> gScore = new HashMap<Vector2Int, Integer>();
        gScore.put(openSet.get(0), 0);

        HashMap<Vector2Int, Vector2Int> cameFrom = new HashMap<Vector2Int, Vector2Int>();

        HashMap<Vector2Int, Integer> fScore = new HashMap<Vector2Int, Integer>();
        fScore.put(openSet.get(0), heuristic(new Vector2Int(getTileX(), getTileY()), destination));

        while (!openSet.isEmpty())
        {
            Vector2Int current = getLowestFScore(openSet, fScore);

            if (current.equals(destination))
            {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            List<Vector2Int> neighbours = getNeighbours(current);

            for (Vector2Int neighbour : neighbours)
            {
                if (!getRoom().isWalkableTile(neighbour.getX(), neighbour.getY())) continue;

                if (closedSet.contains(neighbour))
                {
                    continue;
                }

                int tentativeGScore = gScore.get(current) + distFromNeighbour(current, neighbour);

                if (!openSet.contains(neighbour))
                {
                    openSet.add(neighbour);
                }
                else
                {
                    int prevScore = gScore.get(neighbour);

                    if (tentativeGScore >= prevScore)
                    {
                        continue;
                    }
                }

                cameFrom.put(neighbour, current);
                gScore.put(neighbour, tentativeGScore);
                fScore.put(neighbour, gScore.get(neighbour) + heuristic(neighbour, destination));
            }
        }

        return null;
    }

    public Vector2Int getLowestFScore(List<Vector2Int> openSet, HashMap<Vector2Int, Integer> fScore)
    {
        if (openSet.isEmpty()) return null;

        Vector2Int lowest = openSet.get(0);
        int lowestInt = fScore.get(lowest);

        for (Vector2Int v : openSet)
        {
            if (fScore.get(v) < lowestInt)
            {
                lowest = v;
                lowestInt = fScore.get(v);
            }
        }

        return lowest;
    }

    public int distFromNeighbour(Vector2Int current, Vector2Int neighbour)
    {
        return Math.abs(current.getX() - neighbour.getX()) + Math.abs(current.getY() - neighbour.getY());
    }

    public List<Vector2Int> reconstructPath(HashMap<Vector2Int, Vector2Int> cameFrom, Vector2Int current)
    {
        List<Vector2Int> path = new ArrayList<Vector2Int>();
        path.add(current);

        while (cameFrom.keySet().contains(current))
        {
            current = cameFrom.get(current);
            path.add(current);
        }

        Collections.reverse(path);

        return path;
    }

    public List<Vector2Int> getNeighbours(Vector2Int current)
    {
        int roomWidth = ((TiledMapTileLayer) getRoom().getTiledMap().getLayers().get(0)).getWidth();
        int roomHeight = ((TiledMapTileLayer) getRoom().getTiledMap().getLayers().get(0)).getHeight();

        List<Vector2Int> neighbours = new ArrayList<Vector2Int>();

        if (current.getX() + 1 <= roomWidth)
        {
            neighbours.add(new Vector2Int(current.getX() + 1, current.getY()));
        }

        if (current.getY() + 1 <= roomHeight)
        {
            neighbours.add(new Vector2Int(current.getX(), current.getY() + 1));
        }

        if (current.getX() - 1 >= 0)
        {
            neighbours.add(new Vector2Int(current.getX() - 1, current.getY()));
        }

        if (current.getY() - 1 >= 0)
        {
            neighbours.add(new Vector2Int(current.getX(), current.getY() - 1));
        }

        return neighbours;
    }

    public int heuristic(Vector2Int start, Vector2Int end)
    {
        return Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY());
    }

    /**
     * The state of the person explains what they are currently doing.
     * <li>{@link #WALKING}</li>
     * <li>{@link #STANDING}</li>
     */
    public enum PersonState
    {
        /**
         * Person is walking.
         */
        WALKING,

        /**
         * Person is standing still.
         */
        STANDING;
    }



}
