package pppp.g3;

import pppp.sim.Point;
import pppp.sim.Move;

import java.util.*;
import pppp.g3.Strategy;

public class ModifiedSweep implements pppp.g3.Strategy {

	// see details below
	private int id = -1;
	private int side = 0;
	private int[] pos_index = null;
	private Point[][] pos = null;
	private double door = 0.0;
	private boolean neg_y;
	private boolean swap;
	private int n_pipers, low, high;

	public void init(int id, int side, long turns,
	                 Point[][] pipers, Point[] rats){
		this.id = id;
		this.side = side;
		neg_y = id == 2 || id == 3;
		swap  = id == 1 || id == 3;
		n_pipers = pipers[id].length;
		pos = new Point [n_pipers][];
		pos_index = new int [n_pipers];
		low = n_pipers / 4;
		high = n_pipers / 4 * 3;
		for (int p = 0 ; p != n_pipers; ++p) {
			if (isMagnet(p)) { // half for now
				pos[p] = moveMainPipers();
			} else {
				pos[p] = moveHunters();
			}
			pos_index[p] = 0;
		}
	}

	public void play(Point[][] pipers, boolean[][] pipers_played,
	                 Point[] rats, Move[] moves){
		try {
			for (int p = 0 ; p != pipers[id].length ; ++p) {
				Point src = pipers[id][p];
				Point dst = pos[p][pos_index[p]];

				if (isMagnet(p)) {
					// if position is reached
					if (Math.abs(src.x - dst.x) < 0.000001 &&
					    Math.abs(src.y - dst.y) < 0.000001) {
						// move to next state
						if (++pos_index[p] == (pos[p].length)){
							pos_index[p] = 0;
						}
						dst = pos[p][pos_index[p]];
					}
					// assign the move for the central player
					moves[p] = Movement.makeMove(src, dst, pos_index[p] >= 1);
				} else {
					// if we are currently on a path do not deviate from the path
					if (pos[p][pos_index[p]] != null) {
						
						// assign current move to piper
						moves[p] = Movement.makeMove(src, dst, pos_index[p] > 1);
						
						// if we are close enough to the dst move to the next state
						if (Math.abs(src.x - dst.x) < 0.0005 &&
					    	Math.abs(src.y - dst.y) < 0.0005) {
							
							// reset states
							pos[p][1] = pos[p][2] = null;

							// move to next state (skipping 0th)
							if (++pos_index[p] == (pos[p].length)) {
								pos_index[p] = 1;
							}
						}
					} else {
						if (pos_index[p] == 1) {
							dst = findClosestRat(pipers[id][p], rats);
							pos[p][pos_index[p]] = dst;
						} else if (pos_index[p] == 2) {
							dst = returnToSender()
						}
						moves[p] = Movement.makeMove(src, dst, pos_index[p] > 1);
					}
				}
			}

		} catch (NullPointerException e) {
			System.out.println("null pointer exception");
		}
	}

	private boolean isMagnet(int p){
		return p >= low && p < high;
	}

	public Point[] findClosestRat(Point piper, Point[] rats, int n){
		
		Point[] closestPoints = new Point[n];
		// Maybe find the closest rat to the magnet
		double closestDist = Double.MAX_VALUE;
		Point closestPoint = null;
		for(Point r : rats){
			double dist = distance(piper, r);
			if(dist < closestDist){
				closestPoint = r;
				closestDist = dist;
			}
		}
		return closestPoint;
	}

	public double distance(Point a, Point b){
		return Math.hypot(a.x - b.x, a.y - b.y);
	}

	public Point[] moveMainPipers() {
		Point[] pos = new Point [5];
		pos[0] = Movement.makePoint(door, side * 0.5, neg_y, swap);
		pos[1] = Movement.makePoint(door, -side * 0.5 + 10, neg_y, swap);
		pos[2] = pos[0];
		pos[3] = Movement.makePoint(door, side * 0.5 + 2, neg_y, swap);
		pos[4] = pos[3]; // figure out waiting
		return pos;
	}

	public Point[] moveHunters() {
		Point[] pos = new Point [3];
		pos[0] = Movement.makePoint(door, side * 0.5, neg_y, swap);
		pos[1] = null;
		pos[2] = null;
		return pos;
	}

}