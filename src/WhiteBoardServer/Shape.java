// Benjamin Yi - 1152795

package WhiteBoardServer;

import remote.IShape;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Shape object that defines whiteboard drawn objects.
 * Also can be a text object.
 */
public class Shape extends UnicastRemoteObject implements IShape {
    private int x;
    private int y;
    private int width = 0;
    private int height = 0;
    private Color colour;
    private String text = "";
    private ShapeType shape;

    protected Shape(int x, int y, int width, int height, Color colour, ShapeType shape) throws RemoteException {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colour = colour;
        this.shape = shape;
    }

    protected Shape(int x, int y, Color colour, String text) throws RemoteException {
        super();
        this.x = x;
        this.y = y;
        this.colour = colour;
        this.text = text;
        this.shape = ShapeType.TEXT;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getColour() {return colour;}

    public String getText() {return text;}

    public ShapeType getShape() {return shape;}
}
