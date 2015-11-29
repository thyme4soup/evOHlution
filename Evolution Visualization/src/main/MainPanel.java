package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;

public class MainPanel extends Canvas implements Runnable, KeyListener, MouseWheelListener, MouseMotionListener, MouseListener {
	private BufferedImage back;
	private double zoom = 1;
	private Point location = new Point(0, 0);
	private Point oldMouse = new Point(0, 0);
	private boolean mouseIsPressed = false;
	static int SLEEP_TIME = 25;
	ArrayList<Dot> dots = new ArrayList<Dot>();
	ArrayList<Organism> organisms = new ArrayList<Organism>();
	
	static final double FREE_ENERGY = 0.001 * (double)(MainFrame.ARENA_LENGTH) * (double)(MainFrame.ARENA_LENGTH);
	
	public MainPanel() {
		this.setBounds(0, 0, MainFrame.PANEL_LENGTH, MainFrame.PANEL_LENGTH);
		this.setBackground(Color.lightGray);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		
		DotGenerator dotGen = new DotGenerator(this);
		Thread dotGenThread = new Thread(dotGen);
		dotGenThread.start();
		Checker check = new Checker(this);
		Thread checker = new Thread(check);
		checker.start();
	}

	@Override
	public void run() {
		this.requestFocus();
		while(true) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
			for(int i = 0; i < organisms.size(); i++) {
				try {
					organisms.get(i).updateMovement(organisms, dots);
				} catch(Exception e) {e.printStackTrace();}
			}
			this.repaint();
			this.revalidate();
		}
	}
	
	public void update(Graphics graphics) {
		Graphics2D twoDGraph = (Graphics2D)graphics;
		if(back==null)
		   back = (BufferedImage)(createImage(getWidth(),getHeight()));
		Graphics g = back.createGraphics();
		
		g.setColor(Color.black);
		g.fillRect(0, 0, MainFrame.PANEL_LENGTH, MainFrame.PANEL_LENGTH);
		
		Color arBase = Color.gray;
		Color arena = new Color(arBase.getRed(), arBase.getGreen(), arBase.getBlue(), 230);
		g.setColor(arena);
		g.fillRect(location.x - 5, location.y - 5, (int)(zoom * MainFrame.ARENA_LENGTH) + 5, (int)(zoom * MainFrame.ARENA_LENGTH) + 5);
		
		Rectangle view = getView();
		for(int i = 0; i < dots.size(); i++) {
			try {
				dots.get(i).draw(g, zoom, view);
			} catch(Exception e) {e.printStackTrace();}
		}
		for(int i = 0; i < organisms.size(); i++) {
			try {
				organisms.get(i).draw(g, zoom, view);
			} catch(Exception e) {e.printStackTrace();}
		}
		
		twoDGraph.drawImage(back, null, 0, 0);
	}
	
	public void addDot(Dot dot) {
		dots.add(dot);
	}
	public void removeDot(Dot dot) {
		dots.remove(dot);
	}
	public void addOrganism(Organism o) {
		//o.panel = this;
		organisms.add(o);
		//new Thread(o).start();
	}
	public void removeOrganism(Organism o) {
		o.alive = false;
		organisms.remove(o);
	}
	public void removeOrganism(int i) {
		organisms.remove(i);
	}
	public Organism genOrg() {
		Random r = new Random();
		Point p = new Point(r.nextInt(MainFrame.ARENA_LENGTH-MainFrame.BASE_UNIT), r.nextInt(MainFrame.ARENA_LENGTH-MainFrame.BASE_UNIT));
		return new Organism(p.x, p.y);
	}
	public Organism genOrg(int s) {
		Random r = new Random();
		Point p = new Point(r.nextInt(MainFrame.ARENA_LENGTH-MainFrame.BASE_UNIT), r.nextInt(MainFrame.ARENA_LENGTH-MainFrame.BASE_UNIT));
		return new Organism(p.x, p.y, s);
	}
	public Dot genDot() {
		Random r = new Random();
		Point p = new Point(r.nextInt(MainFrame.ARENA_LENGTH-MainFrame.BASE_UNIT), r.nextInt(MainFrame.ARENA_LENGTH-MainFrame.BASE_UNIT));
		return new Dot(p.x, p.y);
	}
	public Rectangle getView() {
		int side = (int) (MainFrame.PANEL_LENGTH / zoom);
		return new Rectangle(location.x, location.y, side, side);
	}
	
	
	class DotGenerator implements Runnable {
		MainPanel panel;
		public DotGenerator(MainPanel panel) {
			this.panel = panel;
		}
		public void run() {
			double side = MainFrame.ARENA_LENGTH;
			double time = (1000 * 1000 / (side * side));
			while(true) {
				try {
					Thread.sleep((int) (SLEEP_TIME * 10 * time));
				} catch (InterruptedException e) {e.printStackTrace();}
				if(panel.sumEnergy() < FREE_ENERGY) {
					panel.addDot(panel.genDot());
				}
			}
		}
	}
	class Checker implements Runnable {
		MainPanel panel;
		public Checker(MainPanel panel) {
			this.panel = panel;
		}
		public void run() {
			while(true) {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {e.printStackTrace();}
				checkDots();
				checkFood();
				checkLifetime();
				checkHunters();
				checkQuota();
			}
		}
		private void checkDots() {
			for(int i = 0; i < panel.organisms.size(); i++) {
				for(int j = 0; j < dots.size(); j++) {
					if(panel.organisms.get(i).intersects(dots.get(j))) {
						panel.organisms.get(i).eat(dots.get(j));
						panel.removeDot(dots.get(j));
						j--;
					}
				}
			}
		}
		private void checkFood() {
			for(int i = 0; i < panel.organisms.size(); i++) {
				if(panel.organisms.get(i).food <= 0) {
					panel.removeOrganism(i);
					i--;
				}
				else if(panel.organisms.get(i).food >= panel.organisms.get(i).reproductionQuota) {
					Organism o = new Organism(panel.organisms.get(i));
					Organism p = new Organism(panel.organisms.get(i), o);
					panel.removeOrganism(i);
					i--;
					panel.addOrganism(o);
					panel.addOrganism(p);
				}
			}
		}
		private void checkLifetime() {
			for(int i = 0; i < panel.organisms.size(); i++) {
				if(panel.organisms.get(i).lifetime <= 0) {
					panel.removeOrganism(i);
					i--;
				}
			}
		}
		private void checkHunters() {
			for(int i = 0; i < panel.organisms.size() - 1; i++) {
				for(int j = i + 1; j < panel.organisms.size(); j++) {
					if(panel.organisms.get(i).intersects(panel.organisms.get(j))) {
						if(panel.organisms.get(i).getSize() > panel.organisms.get(j).getSize() && panel.organisms.get(j).isEdible()) {
							panel.organisms.get(i).eat(panel.organisms.get(j));
							panel.removeOrganism(j);
							j--;
						}
						else if(panel.organisms.get(i).getSize() < panel.organisms.get(j).getSize() && panel.organisms.get(i).isEdible()) {
							panel.organisms.get(j).eat(panel.organisms.get(i));
							panel.removeOrganism(i);
							i--;
							return;
						}
					}
				}
			}
		}
		private void checkQuota() {
			if(panel.organisms.size() == 0) panel.addOrganism(panel.genOrg());
		}
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		switch(arg0.getKeyCode()) {
		case 38:
			SLEEP_TIME ++;
			break;
		case 40:
			if(SLEEP_TIME > 3) SLEEP_TIME--;
			break;
		}
	}

	public double sumEnergy() {
		double sum = 0;
		for(int i = 0; i < organisms.size(); i++) {
			try {
			sum += organisms.get(i).getSize();
			sum += organisms.get(i).food;
			} catch(Exception e) {e.printStackTrace();}
		}
		sum += dots.size();
		return sum;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if(notches < 0 && zoom > 0.1) zoom += notches * 0.1;
		if(notches > 0 && zoom < 5) zoom += notches * 0.1;
		zoom = (double) Math.round(zoom * 100d) / 100d;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		location.x += e.getX() - oldMouse.x;
		location.y += e.getY() - oldMouse.y;
		oldMouse.x = e.getX();
		oldMouse.y = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(!mouseIsPressed) {
			oldMouse.x = e.getX();
			oldMouse.y = e.getY();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseIsPressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseIsPressed = false;
	}
}
