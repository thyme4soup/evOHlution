package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;

public class Organism implements Runnable {
	MainPanel panel;
	
	ArrayList<Polygon> rotation = new ArrayList<Polygon>();
	ArrayList<Rectangle> skeleton = new ArrayList<Rectangle>();
	
	Organism sister;
	boolean alive = true;
	
	double edCounter = 15;
	
	double lifetime = 20;
	double intelligence = 1.0;
	double aggression = 1.0;
	double metabolism = 0.01;
	double reproductionQuota;
	int vision = 100;
	
	double orientation;
	double food = 1;
	double velX = 0;
	double velY = 0;
	double velMax = 0;
	double centerX;
	double centerY;
	double acceleration;
	Point destination;
	boolean esc = true;
	
	public Organism(Organism parent) {
		Random rand = new Random();
		
		centerX = parent.centerX + rand.nextInt(MainFrame.BASE_UNIT) - MainFrame.BASE_UNIT/2;
		centerY = parent.centerY + rand.nextInt(MainFrame.BASE_UNIT) - MainFrame.BASE_UNIT/2;
		
		skeleton = parent.getSkeleton();

		for(Rectangle r : skeleton) {
			rotation.add(getPolygon(r));
		}
		mutate();
		
		food = skeleton.size();
		setAcceleration();
		setMaxVelocity();
		setMetabolism();
		setReproductionQuota();
		setEdibilityCounter();
		setLifetime();
	}
	public Organism(Organism parent, Organism sister) {
		Random rand = new Random();
		
		centerX = parent.centerX + rand.nextInt(MainFrame.BASE_UNIT) - MainFrame.BASE_UNIT/2;
		centerY = parent.centerY + rand.nextInt(MainFrame.BASE_UNIT) - MainFrame.BASE_UNIT/2;
		
		skeleton = parent.getSkeleton();

		for(Rectangle r : skeleton) {
			rotation.add(getPolygon(r));
		}
		mutate();
		
		food = skeleton.size();
		setAcceleration();
		setMaxVelocity();
		setMetabolism();
		setReproductionQuota();
		setEdibilityCounter();
		setLifetime();
		
		this.sister = sister;
		sister.sister = this;
	}
	private void mutate() {
		Random r = new Random();
		if(r.nextInt(10) == 0) skeleton.add(getAddition());
		else if(r.nextInt(10) == 0 && skeleton.size() > 1) {
			int i = getRemovalIndex();
			if(i > 0) skeleton.remove(i);
		}
		
		double dShift = 0.1;
		
		intelligence += r.nextDouble() * dShift - dShift / 2;
		if(intelligence < 0) intelligence = 0;
		if(intelligence > 1.0) intelligence = 1.0;
		
		aggression += r.nextDouble() * dShift - dShift / 2;
		if(aggression < 0) aggression = 0;
		if(aggression > 1.0) aggression = 1.0;
		
		vision += r.nextDouble() * 3;
		if(vision < 0) vision = 0;
		if(vision > MainFrame.ARENA_LENGTH) vision = MainFrame.ARENA_LENGTH;
		
		recenter();
	}
	public Organism(int x, int y, int s) {
		
		centerX = x;
		centerY = y;
		int u = MainFrame.BASE_UNIT;
		Rectangle r = new Rectangle(-u/2, -u, u, u);
		skeleton.add(r);
		rotation.add(getPolygon(r));
		r = new Rectangle(-u/2, 0, u, u);
		skeleton.add(r);
		rotation.add(getPolygon(r));
		
		food = skeleton.size();
		setAcceleration();
		setMaxVelocity();
		setMetabolism();
		setReproductionQuota();
		setEdibilityCounter();
		setLifetime();
		
	}
	public Organism(int x, int y) {
		
		centerX = x;
		centerY = y;
		int u = MainFrame.BASE_UNIT;
		Rectangle r = new Rectangle(-u/2, -u/2, u, u);
		skeleton.add(r);
		rotation.add(getPolygon(r));
		
		food = skeleton.size();
		setAcceleration();
		setMaxVelocity();
		setMetabolism();
		setReproductionQuota();
		setEdibilityCounter();
		setLifetime();
		
	}
	public Polygon getPolygon(Rectangle r) {
		Polygon p = new Polygon();
		p.addPoint(r.x, r.y);
		p.addPoint(r.x, r.y + r.height);
		p.addPoint(r.x + r.width, r.y + r.height);
		p.addPoint(r.x + r.width, r.y);
		return p;
	}
	public void updateMovement(ArrayList<Organism> organisms, ArrayList<Dot> dots) {
		if(edCounter > 0) edCounter --;
		if(lifetime > 0) lifetime -= 0.05;
		applyAcceleration(organisms, dots);
		updateRotation();
		if(edCounter <= 0) {
			food -= metabolism;
		}
	}
	public Dot getClosestDot(ArrayList<Dot> dots) {
		Dot dest = null;
		double distance = Double.MAX_VALUE;
			for(int i = 0; i < dots.size(); i++) {
				try {
					double dist = MainFrame.getDist(getCenter(), dots.get(i).getLocation());
					if(dist < distance) {
						dest = dots.get(i);
						distance = dist;
					}
				} catch(Exception e) {}
			}
		return dest;
	}
	public Organism getClosestOrganism(ArrayList<Organism> organisms) {
		Organism o = null;
		double distance = Double.MAX_VALUE;
		for(int i = 0; i < organisms.size(); i++) {
			try {
				if(!organisms.get(i).equals(this)) {
					double dist = MainFrame.getDist(getCenter(), organisms.get(i).getCenter());
					if(dist < distance) {
						o = organisms.get(i);
						distance = dist;
					}
				}
			} catch(Exception e) {e.printStackTrace();}
		}
		return o;
	}
	public void applyAcceleration(ArrayList<Organism> organisms, ArrayList<Dot> dots) {
		double theta = findAngle(organisms, dots);
		velX += acceleration * Math.cos(theta);
		velY += acceleration * Math.sin(theta);
		double vel = Math.sqrt(velX * velX + velY * velY);
		double nTheta = MainFrame.getAngle(new Point(0, 0), new Point((int) velX, (int) velY));
		orientation = nTheta;
		if(vel > velMax) {
			vel = velMax;
			velX = vel * Math.cos(nTheta);
			velY = vel * Math.sin(nTheta);
		}
		centerX += velX;
		centerY += velY;
	}
	public double findAngle(ArrayList<Organism> organisms, ArrayList<Dot> dots) {
		if(edCounter > 0 && sister != null && sister.alive) {
			destination = sister.getCenter();
			esc = true;
		}
		else {
			for(int i = 0; i < organisms.size(); i++) {
				if(organisms.get(i).getSize() == getSize() && intersects(organisms.get(i))) {
					return MainFrame.getAngle(organisms.get(i).getCenter(), getCenter());
				}
			}
			Organism o = getClosestOrganism(organisms);
			int a = getAction(o);
			if(a != 0) {
				destination = o.getCenter();
				if(a == 1) esc = false;
				if(a == -1) esc = true;
			}
			else {
				Dot d = getClosestDot(dots);
				if(d != null) {
					Point dot = d.getLocation();
					destination = new Point(dot.x + MainFrame.BASE_UNIT/2, dot.y + MainFrame.BASE_UNIT/2);
					esc = false;
				} else {
					destination = null;
					esc = false;
				}
			}
		}
		if(destination != null) {
			if(esc)
				return MainFrame.getAngle(destination, getCenter());
			else
				return MainFrame.getAngle(getCenter(), destination);
		}
		//catch all
		Random r = new Random();
		Point rand = new Point(r.nextInt(MainFrame.ARENA_LENGTH), r.nextInt(MainFrame.ARENA_LENGTH));
		return MainFrame.getAngle(getCenter(), rand);
	}
	public void updateRotation() {
		rotation = new ArrayList<Polygon>();
		for(Rectangle r : skeleton) {
			rotation.add(shiftPoly(MainFrame.getRotation(new Point(0, 0), getPolygon(r), orientation), getCenter()));
		}
	}
	
	
	public void draw(Graphics g, double zoom, Rectangle view) {
		if(edCounter > 0) g.setColor(Color.blue);
		else if(food < metabolism * 20) g.setColor(Color.red);
		else g.setColor(Color.black);
		for(int i = 0; i < rotation.size(); i++) {
			g.drawPolygon(getZoom(rotation.get(i), zoom, view));
		}
	}
	private Polygon getZoom(Polygon p, double zoom, Rectangle view) {
		Polygon n = new Polygon();
		
		for(int i = 0; i < p.npoints; i++) {
			n.addPoint((int)(p.xpoints[i] * zoom) + view.x, (int)(p.ypoints[i] * zoom) + view.y);
		}
		
		return n;
	}
	private int getRemovalIndex() {
		Random r = new Random();
		int giveUp = 100;
		while(giveUp > 0) {
			ArrayList<Rectangle> temp = MainFrame.copyList(skeleton);
			int i = r.nextInt(skeleton.size());
			temp.remove(i);
			if(isOnePiece(temp)) {
				return i;
			}
			giveUp --;
		}
		return -1;
	}
	private boolean isOnePiece(ArrayList<Rectangle> body) {
		boolean b = body.size() == getGrouping(body, new ArrayList<Rectangle>(), body.get(0)).size();
		return b;
	}
	private ArrayList<Rectangle> getGrouping(ArrayList<Rectangle> body, ArrayList<Rectangle> grouping, Rectangle r) {
		grouping.add(r);
		for(int i = 0; i < body.size(); i++) {
			if(!grouping.contains(body.get(i)) && areAdjacent(r, body.get(i))) {
				getGrouping(body, grouping, body.get(i));
			}
		}
		return grouping;
	}
	private boolean areAdjacent(Rectangle a, Rectangle r) {
		Point[] dirs = new Point[] {
			new Point(0, -1),
			new Point(-1, 0),
			new Point(0, 1),
			new Point(1, 0),
		};
		for(Point p : dirs) {
			if(a.intersects(MainFrame.shift(r, p))) return true;
		}
		return false;
	}
	private Rectangle getAddition() {
		Rectangle add;
		Rectangle bounds = getBounds();
		while(true) {
			add = genRect(bounds);
			if(MainFrame.isValid(add, skeleton)) return add;
		}
	}
	private Rectangle genRect(Rectangle bounds) {
		Random r = new Random();
		int x = r.nextInt(bounds.width) + bounds.x;
		int y = r.nextInt(bounds.height) + bounds.y;
		return new Rectangle(x, y, MainFrame.BASE_UNIT, MainFrame.BASE_UNIT);
	}
	private Rectangle getBounds() {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
		for(int i = 0; i < skeleton.size(); i++) {
			try {
				Rectangle r = skeleton.get(i);
				if(r.x < minX) minX = r.x;
				if(r.y < minY) minY = r.y;
				if(r.x + r.width > maxX) maxX = r.x + r.width;
				if(r.y + r.height > maxY) maxY = r.y + r.height;
			} catch(Exception e) {}
		}
		return new Rectangle(minX - MainFrame.BASE_UNIT, minY - MainFrame.BASE_UNIT, maxX - minX, maxY - minY);
	}
	private void recenter() {
		Rectangle bounds = getBounds();
		int shiftX = -(bounds.width / 2 + bounds.x);
		int shiftY = -(bounds.height / 2 + bounds.y);
		ArrayList<Rectangle> nSkeleton = new ArrayList<Rectangle>();
		for(int i = 0; i < skeleton.size(); i++) {
			nSkeleton.add(MainFrame.shift(skeleton.get(i), new Point(shiftX, shiftY)));
		}
		skeleton = nSkeleton;
	}
	public Point getCenter() {
		return new Point((int) centerX, (int) centerY);
	}
	public Polygon shiftPoly(Polygon p, Point shift) {
		Polygon q = new Polygon();
		for(int i = 0; i < p.npoints; i++) {
			q.addPoint(p.xpoints[i] + shift.x, p.ypoints[i] + shift.y);
		}
		return q;
	}
	private void setAcceleration() {
		double baseAcc = 3;
		acceleration = baseAcc / (0.25 * Math.sqrt(skeleton.size()) + 1);
	}
	private void setMaxVelocity() {
		double minY = Double.MAX_VALUE;
		for(Rectangle r : skeleton) {
			if(r.y < minY) minY = r.y;
		}
		double maxY = Double.MIN_VALUE;
		for(Rectangle r : skeleton) {
			if(r.y + r.height > maxY) maxY = r.y + r.height;
		}
		double width = maxY - minY;
		double baseMaxVel = 7;
		velMax = baseMaxVel / (0.25 * Math.sqrt(width) + 1);
	}
	private void setMetabolism() {
		double baseMet = 0.005;
		metabolism = baseMet * (int) (0.25 * skeleton.size());
	}
	private void setEdibilityCounter() {
		double baseEd = 10;
		edCounter = baseEd * Math.sqrt(skeleton.size());
	}
	private void setReproductionQuota() {
		reproductionQuota = skeleton.size() * 2;
	}
	private void setLifetime() {
		lifetime = 25 + 5 * skeleton.size();
	}
	public void eat(Dot d) {
		food += d.FOOD_VALUE;
	}
	public void eat(Organism o) {
		food += o.getSize();
	}
	public boolean intersects(Dot d) {
		try {
			for(int i = 0; i  < rotation.size(); i++) {
				if(rotation.get(i).intersects(d.getBounds())) {
					return true;
				}
			}
		} catch(Exception e) {}
		return false;
	}
	public boolean intersects(Organism o) {
		try {
			if(this.equals(o)) return false;
			for(Polygon p : rotation) {
				for(Polygon q : o.rotation) {
					if(p.intersects(q.getBounds2D())) {
						return true;
					}
				}
			}
		} catch(Exception e) {}
		return false;
	}
	public ArrayList<Rectangle> getSkeleton() {
		ArrayList<Rectangle> skeletonC = new ArrayList<Rectangle>();
		for(Rectangle r : skeleton) {
			skeletonC.add(r);
		}
		return skeletonC;
	}
	public boolean isEdible() {
		return edCounter <= 0;
	}
	public int getSize() {
		return skeleton.size();
	}
	private boolean isVisible(Organism o) {
		if(!o.isEdible()) return false;
		//System.out.println(MainFrame.getDist(getCenter(), o.getCenter()));
		return MainFrame.getDist(getCenter(), o.getCenter()) < vision;
	}
	private int appraiseTarget(Organism o) {
		int appraisalMin = (int) (o.getSize() * Math.pow(intelligence, 0.25));
		int appraisalMax = Integer.MAX_VALUE;
		if(intelligence != 0 ) appraisalMax = (int) (o.getSize() / Math.pow(intelligence, 0.25));
		if(getSize() > appraisalMax) return 1;
		if(getSize() < appraisalMin) return -1;
		return 0;
	}
	private boolean chase(Organism o) {
		return MainFrame.getDist(getCenter(), o.getCenter()) < vision * aggression;
	}
	public int getAction(Organism o) {
		if(o == null) return 0;
		if(isVisible(o)) {
			int a = appraiseTarget(o);
			if(a == 1 && chase(o)) return 1;
			if(a == -1) return -1;
		}
		return 0;
	}

	
	@Override
	public void run() {
		while(alive) {
			try {
				Thread.sleep(MainPanel.SLEEP_TIME);
			} catch (InterruptedException e) {}
			updateMovement(panel.organisms, panel.dots);
		}
	}
}
