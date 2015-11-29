package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

import javax.swing.JPanel;

public class Dot {
	final int FOOD_VALUE = 1;
	int side;
	int x;
	int y;
	public Dot(int x, int y) {
		this.x = x;
		this.y = y;
		side = MainFrame.BASE_UNIT;
	}
	public void draw(Graphics g, double zoom, Rectangle view) {
		g.setColor(Color.black);
		g.fillOval((int)(x * zoom) + view.x, (int)(y * zoom) + view.y, (int)Math.ceil(side * zoom), (int)Math.ceil(side * zoom));
	}
	public Point getLocation() {
		return new Point(x, y);
	}
	public Rectangle getBounds() {
		return new Rectangle(x, y, side, side);
	}
}
