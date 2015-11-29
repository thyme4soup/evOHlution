package main;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JFrame;

public class MainFrame {
	public static final int BASE_UNIT = 10;
	public static final int PANEL_LENGTH = 700;
	public static final int ARENA_LENGTH = 2000;
	
	public static Polygon getRotation(Point center, Polygon p, double theta) {
		Polygon rot = new Polygon();
		for(int i = 0; i < p.npoints; i++) {
			Point q = getRotation(center, new Point(p.xpoints[i], p.ypoints[i]), theta);
			rot.addPoint(q.x, q.y);
		}
		return rot;
	}
	public static Point getRotation(Point center, Point p, double theta) {
		double r = getDist(p, center);
		double aTheta = getAngle(center, p);
		aTheta += theta;
		double x = r * Math.cos(aTheta);
		double y = r * Math.sin(aTheta);
		return new Point((int) x, (int) y);
	}
	public static double getDist(Point p, Point q) {
		double x = p.x - q.x;
		double y = p.y - q.y;
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	public static double getAngle(Point source, Point dest) {
		try {
			double deltY = dest.y - source.y;
			double deltX = dest.x - source.x;
			double theta = 0;
			double baseTheta;
			baseTheta = Math.atan(deltY / deltX);
			
			if(deltY < 0) {
				if(deltX < 0) theta = baseTheta + Math.PI;
				else if(deltX == 0) theta = 3 * Math.PI / 2;
				else theta = Math.PI * 2 + baseTheta;
			}
			else {
				if(deltX < 0) theta = Math.PI + baseTheta;
				else if(deltX == 0) theta = Math.PI / 2;
				else theta = baseTheta;
			}
			if(theta != theta) return 0;
			return theta;
		} catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static ArrayList<Rectangle> addUnit(ArrayList<Rectangle> skeleton) {
		return skeleton;
	}
	public static boolean isValid(Rectangle r, ArrayList<Rectangle> skeleton) {
		if(intersects(r, skeleton)) return false;
		if(isConnected(r, skeleton)) return true;
		return false;
	}
	public static boolean intersects(Rectangle r, ArrayList<Rectangle> skeleton) {
		for(int i = 0; i < skeleton.size(); i++) {
			if(r.intersects(skeleton.get(i))) return true;
		}
		return false;
	}
	public static boolean isConnected(Rectangle r, ArrayList<Rectangle> skeleton) {
		Point[] dirs = new Point[] {
			new Point(0, -1),
			new Point(-1, 0),
			new Point(0, 1),
			new Point(1, 0),
		};
		for(Point p : dirs) {
			if(intersects(shift(r, p), skeleton));
		}
		return true;
	}
	public static Rectangle shift(Rectangle r, Point s) {
		return new Rectangle(r.x + s.x, r.y + s.y, r.width, r.height);
	}
	public static ArrayList<Rectangle> copyList(ArrayList<Rectangle> list) {
		ArrayList<Rectangle> arrCopy = new ArrayList<Rectangle>();
		for(int i = 0; i < list.size(); i++) {
			try {
			arrCopy.add(list.get(i));
			} catch(Exception e) {e.printStackTrace();}
		}
		return arrCopy;
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setBounds(50, 50, PANEL_LENGTH + 10, PANEL_LENGTH + 35);
		frame.setBackground(Color.black);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		
		MainPanel panel = new MainPanel();
		frame.add(panel);
		Thread mainThread = new Thread(panel);
		mainThread.start();
	}
}
