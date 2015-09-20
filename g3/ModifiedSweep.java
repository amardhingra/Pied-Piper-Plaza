package pppp.g3;

import pppp.sim.Point;
import pppp.sim.Move;

import pppp.g3.Strategy;

import java.lang.System;

public class ModifiedSweep implements pppp.g3.Strategy {

    private int id = -1;
	private int side = 0;
    private long turns = 0;
    private int numberOfPipers = 0;
	private int[] piperState = null;
	private Point[][] piperStateMachine = null;
	private double door = 0.0;
	private boolean neg_y;
	private boolean swap;

    private Point gateEntrance = null;
    private Point insideGate = null;

	public void init(int id, int side, long turns,
	                 Point[][] pipers, Point[] rats){
		// storing variables
        this.id = id;
		this.side = side;
        this.turns = turns;

        // variables to rotate map
        neg_y = id == 2 || id == 3;
		swap  = id == 1 || id == 3;

        // create gate positions
        gateEntrance = Movement.makePoint(door, side * 0.5, neg_y, swap);
        insideGate = Movement.makePoint(door, side * 0.5 + 7.5, neg_y, swap);

        // create the state machines for the pipers
		numberOfPipers = pipers[id].length;
		piperStateMachine = new Point [numberOfPipers][];
		piperState = new int [numberOfPipers];

		for (int p = 0 ; p != numberOfPipers; ++p) {
			if (isMagnet(p)) {
				piperStateMachine[p] = createMagnetStateMachine();
			} else {
				piperStateMachine[p] = createHunterStateMachine();
			}
			piperState[p] = 0;
		}
	}

	public void play(Point[][] pipers, boolean[][] pipers_played,
	                 Point[] rats, Move[] moves){
		try {
			for (int p = 0 ; p != pipers[id].length ; ++p) {
				if (isMagnet(p)) {

                    Point src = pipers[id][p];
                    Point dst = piperStateMachine[p][piperState[p]];

					// start by checking if we have reached the destination from the last move
					if (isWithinDistance(src, dst, 0.000001)) {

                        // move to next state
                        if (++piperState[p] == (piperStateMachine[p].length)) {
                            piperState[p] = 0;
                        }
                    }
					// assign the move for the magnet player
					moves[p] = makeMagnetMove(src, p);
				} else {

                    Point src = pipers[id][p];
                    Point dst = piperStateMachine[p][piperState[p]];

                    boolean play = false;
                    int state = piperState[p];
                    //System.out.println("Piper " + p + " is in state " + state);
                    if(state == 0){
                        if(isWithinDistance(src, gateEntrance, 0.00001)){
                            piperState[p] = 1;
                            dst = findClosest(pipers[id][p], rats, 1)[0];
                            piperStateMachine[p][1] = dst;
                        }
                        play = false;
                    } else if (state == 1) {
                        if(isWithinDistance(src, dst, 0.00001)){
                            dst = returnToSender(pipers, p);
                            if(dst == gateEntrance) {
                                // if we are returning to gate set state to 2
                                piperState[p] = 2;
                            } else {
                                // if we are going to magnets set state to 4
                                piperState[p] = 4;
                                piperStateMachine[p][4] = dst;
                            }
                            play = true;
                        } else{
                            play = false;
                        }
                    } else if (state == 2) {
                        if(isWithinDistance(src, gateEntrance, 0.00001)){
                            piperState[p] = 3;
                            dst = insideGate;
                        }
                        play = true;
                    } else if (state == 3) {

                        if(isWithinDistance(src, insideGate, 0.00001)){
                            piperState[p] = 0;
                            dst = gateEntrance;
                        }
                        play = true;
                    } else if (state == 4) {
                        if(isWithinDistance(src, dst, 2.5)){
                            piperState[p] = 1;
                            dst = findClosest(pipers[id][p], rats, 1)[0];
                            piperStateMachine[p][1] = dst;
                        } else{
                            play = true;
                        }
                    } else {
                        System.out.println("Piper " + p + " is in state " + state);
                    }

                    moves[p] = Movement.makeMove(src, dst, play);
                    /*
					// if we are currently on a path do not deviate from the path
					if (piperStateMachine[p][piperState[p]] != null) {
						
						// assign current move to piper
						moves[p] = Movement.makeMove(src, dst, piperState[p] > 1);
						
						// if we are close enough to the dst move to the next state
						if (Math.abs(src.x - dst.x) < 0.0005 &&
					    	Math.abs(src.y - dst.y) < 0.0005) {
							
							// reset states
							piperStateMachine[p][1] = piperStateMachine[p][2] = null;

							// move to next state (skipping 0th)
							if (++piperState[p] == (piperStateMachine[p].length)) {
								piperState[p] = 0;
							}
						}
					} else {
						if (piperState[p] == 1) {
							int n_closest = 3;
							int random_closest = (int) Math.round(n_closest * Math.random());
							Point[] cloest_points = findClosest(pipers[id][p], rats, n_closest);
							dst = cloest_points[random_closest];
							piperStateMachine[p][piperState[p]] = dst;
						} else if (piperState[p] == 2) {
							dst = returnToSender(pipers, p);
							if(dst.x == door && dst.y == side * 0.5){
								piperStateMachine[p][piperState[p]] = dst;
							}
							if(distance(src, dst) < 2.5){
								piperState[p] = 1;
							}
						}
						moves[p] = Movement.makeMove(src, dst, piperState[p] > 1);*/
					}
				}


		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

    private boolean isWithinDistance(Point src, Point dst, double error){
        if(dst != null &&
                // checking if we are within a minimum distance of the destination
                Math.abs(src.x - dst.x) < error &&
                Math.abs(src.y - dst.y) < error){
            return true;
        }

        return false;
    }

	private boolean isMagnet(int p){
        int low = numberOfPipers / 4;
        int high = numberOfPipers / 4 * 3;
        return p >= low && p < high;
	}

	private Point returnToSender(Point[][] pipers, int p){
		Point piper = pipers[id][p];
		double distToGate = distance(piper, gateEntrance);
		Point magnetPos = null;
		for(int i = 0; i < pipers[id].length; i++){
			if(isMagnet(i)){
				if(piperState[i] == 1)
					return gateEntrance;
				magnetPos = pipers[id][i];
				break;
			}
		}
		double distToMagnets = distance(piper, magnetPos);
		if(distToMagnets < distToGate){
			return magnetPos;
		} else {
			return gateEntrance;
		}
	}

	// returns array of closest points, ordered by decreasing distance
	public Point[] findClosest(Point start, Point[] ends, int n) {
		if (n <= ends.length) {
			return ends;
		}
		Point[] closestPoints = new Point[n];
		for (int i = 0; i < n; i++) {
			closestPoints[i] = ends[i];
		}
		for (int i = n; i < ends.length; i++) {
			double e_dist = distance(start, ends[i]);
			int largest = -1;
			for (int j = 0; j < n; j++) {
				double c_dist = distance(start, closestPoints[j]);
				if (e_dist < c_dist 
					&& (c_dist >= distance(start, closestPoints[largest])
						|| largest == -1)) {
					largest = j;
				}
			}
			if(largest != -1){
				closestPoints[largest] = ends[i];
			}
		}
		return closestPoints;
	}

	public double distance(Point a, Point b){
		return Math.hypot(a.x - b.x, a.y - b.y);
	}

	private Point[] createMagnetStateMachine() {
		// magnet pipers have 4 states
		Point[] pos = new Point [4];

        // go to gate entrance
		pos[0] = gateEntrance;

        // go to opposite gate
		pos[1] = Movement.makePoint(door, -side * 0.5 + 7.5, neg_y, swap);

        //go back to gate entrance
		pos[2] = pos[0];

        // Move inside the gate to deposit rats
		pos[3] = insideGate;

        //pos[4] = pos[3]; // figure out waiting
		return pos;
	}

    private Move makeMagnetMove(Point src, int p){
        return Movement.makeMove(src, piperStateMachine[p][piperState[p]], piperState[p] >= 1);
    }

	private Point[] createHunterStateMachine() {
		// Hunters have a 5 state machine
        Point[] pos = new Point [5];

        // go to gate entrance
		pos[0] = gateEntrance;

        // chase rat
		pos[1] = null;

        // bring rat to gate
		pos[2] = pos[0];

        // move inside gate
        pos[3] = insideGate;

        // bring rat to magnets
        pos[4] = null;

		return pos;
	}

}