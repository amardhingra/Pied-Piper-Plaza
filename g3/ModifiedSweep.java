package pppp.g3;

import pppp.sim.Point;
import pppp.sim.Move;

import pppp.g3.Strategy;

import java.lang.Double;
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

    private int numberOfHunters = 0;
    private int numberOfMagnets = 0;

    private int magnetNumber = 0;

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
            int hNumber = 1;
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
                            dst = findNearestRatForHunter(src, pipers, rats, hNumber, numberOfHunters);//findClosest(pipers[id][p], rats, 1)[0];
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
                            // if we haven't reached the destination recompute the closest rat
                            dst = findNearestRatForHunter(src, pipers, rats, hNumber, numberOfHunters);//findClosest(pipers[id][p], rats, 1)[0];
                            piperStateMachine[p][1] = dst;
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
                            // we are only trying to get within 2.5m of the magnets
                            piperState[p] = 1;
                            dst = findNearestRatForHunter(src, pipers, rats, hNumber, numberOfHunters);//findClosest(pipers[id][p], rats, 1)[0];
                            piperStateMachine[p][1] = dst;
                        } else{
                            // if we haven't reached the destination redecide whether to go to
                            // the gate or the magnets
                            dst = returnToSender(pipers, p);
                            if(dst == gateEntrance) {
                                // if we are returning to gate set state to 2
                                piperState[p] = 2;
                            } else {
                                // if we are going to magnets set state to 4
                                piperStateMachine[p][4] = dst;
                            }
                            play = true;
                        }
                    } else {
                        System.out.println("Piper " + p + " is in state " + state);
                    }
                    hNumber++;
                    moves[p] = Movement.makeMove(src, dst, play);
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

        // finding a magnet
        int magnet = -1;
		for(int i = 0; i < pipers[id].length; i++){
			if(isMagnet(i)){
				magnet = i;
				break;
			}
		}

        // getting the distance to the magnets or the gate entrance
        Point piper = pipers[id][p];
        double distToGate = Movement.distance(piper, gateEntrance);
        double distToMagnet = (magnet >= 0 && piperState[magnet] != 1) ?
                Movement.distance(piper, pipers[id][magnet]) : Double.MAX_VALUE;

        // returning the appropriate destination
        if(distToMagnet < distToGate){
			return pipers[id][magnet];
		} else {
			return gateEntrance;
		}
	}

    public Point findNearestRatForHunter(Point hunterPos, Point[][] pipers, Point[] rats, int hunterNumber, int numberOfHunters){
        if(numberOfHunters > rats.length){
            return findClosest(hunterPos, rats, 1)[0];
        }

        Point closestRat = null;
        double closestDistance = Double.MAX_VALUE;
        Point rat;
        for(int i = 0; i < rats.length; ++i){
            if(i % hunterNumber != 0){
                continue;
            }
            rat = rats[i];
            if(isNearMagnet(rat, pipers, 7.5) || isNearGate(rat, 2.5)){
                continue;
            }
            double distanceToRat = Movement.distance(hunterPos, rat);
            if(distanceToRat < closestDistance){
                closestRat = rat;
                closestDistance = distanceToRat;
            }
        }
        if(closestRat != null){
            return closestRat;
        } else {
            return findClosest(hunterPos, rats, 1)[0];
        }

    }

    public boolean isNearGate(Point p, double distance){
        return (Movement.distance(p, gateEntrance) < distance);
    }

    public boolean isNearMagnet(Point rat, Point[][] pipers, double distance){
        for(int i = 0; i < pipers[id].length; i++){
            if(!isMagnet(i)){
                continue;
            }
            Point piper = pipers[id][i];
            if(Movement.distance(rat, piper) < distance) {
                return true;
            }
        }
        return false;
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
			double e_dist = Movement.distance(start, ends[i]);
			int largest = -1;
			for (int j = 0; j < n; j++) {
				double c_dist = Movement.distance(start, closestPoints[j]);
				if (e_dist < c_dist 
					&& (c_dist >= Movement.distance(start, closestPoints[largest])
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
        return Movement.makeMove(src, piperStateMachine[p][piperState[p]], piperState[p] > 1);
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

    private void setNumberOfHuntersAndMagnets(){
        for(int i = 0; i < numberOfPipers; i++){
            if(isMagnet(i))
                numberOfMagnets++;
            else
                numberOfHunters++;
        }
    }

}