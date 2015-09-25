package pppp.g3;

import pppp.sim.Point;
import pppp.sim.Move;

import pppp.g3.Strategy;

import java.lang.Double;
import java.lang.System;

public class TenPiperStrategy implements pppp.g3.Strategy {

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
    private Point outsideGate = null;

    private final int MAX_CIRCLE_COUNT = 10;
    private int circleCounter = 0;

    private int minSweepNumber = 40;

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
        outsideGate = Movement.makePoint(door, side * 0.5 - 7.5, neg_y, swap);

        // create the state machines for the pipers
		numberOfPipers = pipers[id].length;
		piperStateMachine = new Point [numberOfPipers][];
		piperState = new int [numberOfPipers];

		for (int p = 0 ; p != numberOfPipers; ++p) {
			piperStateMachine[p] = generateStateMachine(p);
            piperState[p] = 0;
		    if(rats.length <= minSweepNumber){
                piperState[p] = 7;
            }
        }
	}

	public void play(Point[][] pipers, boolean[][] pipers_played,
	                 Point[] rats, Move[] moves) {
        Point src, dst;

        sortRats(rats);

        short[] pipersAssignedToRat = new short[rats.length];

        for(int p = 0; p < numberOfPipers; p++){
            int state = piperState[p];
            boolean play = false;
            src = pipers[id][p];
            dst = piperStateMachine[p][state];

            //if(state > 6)
                //System.err.println(state);

            if(state == 0){
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 1;
                    dst = piperStateMachine[p][state];
                }
            }

            else if (state == 1) {
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 2;
                    dst = src;
                    play = true;
                }
            }

            else if (state == 2) {
                if(allPipersAreAtLeastState(2)){
                    piperState[p] = state = 3;
                    dst = piperStateMachine[p][state];
                } else {
                    dst = src;
                }
                play = true;

            }

            else if (state == 3) {
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 4;
                    dst = piperStateMachine[p][state];
                    play = true;
                }
                play = true;
            }

            else if (state == 4) {
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 5;
                    dst = piperStateMachine[p][state];
                    play = true;
                }
                play = true;
            }

            else if (state == 5) {
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 6;
                    dst = src;
                }
                play = true;
            }

            else if (state == 6) {
                if(allPipersAreAtLeastState(6)){
                    piperState[p] = state = 7;
                    dst = piperStateMachine[p][state];
                } else {
                    dst = src;
                }
                play = true;
            }

            else if (state == 7) {
                dst = gateEntrance;
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 8;
                    dst = findNearestRat(pipers, rats, p);
                    piperStateMachine[p][8] = dst;
                }
            }

            else if (state == 8) {
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 9;
                    dst = piperStateMachine[p][state];
                    play = true;
                } else {
                    dst = findNearestRat(pipers, rats, p);
                    piperStateMachine[p][8] = dst;
                }
            }

            else if (state == 9) {
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 10;
                    dst = piperStateMachine[p][state];
                    play = true;
                }
                else if(noRatsAreWithinRange(pipers[id][p], rats, 5)){
                    piperState[p] = state = 8;
                    dst = findNearestRat(pipers, rats, p);
                    piperStateMachine[p][8] = dst;
                }
                play = true;
            } else if (state == 10) {
                if(isWithinDistance(src, dst, 0.00001)){
                    piperState[p] = state = 7;
                    dst = piperStateMachine[p][state];
                    play = true;
                }

                play = true;
            }


            else {
                System.out.println("Piper " + p + " is in state " + state);
            }


            if(isWithinDistance(src, dst, 0.00001) && state == 0){
                piperState[p] = ++piperState[p] % piperStateMachine[p].length;
                state = piperState[p];
                dst = piperStateMachine[p][state];
            }

            moves[p] = Movement.makeMove(src, dst, play);
        }
    }

    private void sortRats(Point[] rats) {
        Point closestRat = null;
        double minDist = 0;
        int index = -1;
        for (int i = 0; i < rats.length; i++) {
            minDist = Movement.distance(gateEntrance, rats[i]);
            closestRat = rats[i];
            index = i;
            for (int j = i; j < rats.length; j++) {
                double dist = Movement.distance(gateEntrance, rats[j]);
                if (dist < minDist) {
                    index = j;
                    minDist = dist;
                    closestRat = rats[j];
                }
            }
            Point temp = rats[i];
            rats[i] = rats[index];
            rats[index] = temp;
        }
    }

    private Point findNearestRat(Point[][] pipers, Point[] rats, short[] pipersAssigned, int p){


        if(rats.length < numberOfPipers){
            return rats[p%rats.length];
        }

        int i = 0;
        while(pipersAssigned[i] == i){
            i++;
        }
        pipersAssigned[i]++;
        return rats[i];

    }

    private Point findNearestRat(Point[][] pipers, Point[] rats, int p){

        Point piper = pipers[id][p];
        double minDist = Double.MAX_VALUE;
        Point closestRat = null;
        int index = -1;
        for(int i = 0; i < rats.length; ++i){
            if(i%(numberOfPipers - (p)) != 0){
                continue;
            }

            Point rat = rats[i];
            if(rat == null){
                continue;
            }
            double dist = Movement.distance(piper, rat);
            if(dist < minDist){
                minDist = dist;
                closestRat = rat;
            }
        }

        if(minDist <= side/5){
            return closestRat;
        }

        return rats[p % rats.length];

    }

    private boolean allPipersAreAtLeastState(int state){
        for(int i = 0; i < numberOfPipers; i++){
            if(piperState[i] < state)
                return false;
        }
        return true;
    }

    private boolean isWithinDistance(Point src, Point dst, double error){
        if(src != null && dst != null &&
                // checking if we are within a minimum distance of the destination
                Math.abs(src.x - dst.x) < error &&
                Math.abs(src.y - dst.y) < error){
            return true;
        }

        return false;
    }

    private boolean noRatsAreWithinRange(Point piper, Point[] rats, double distance){
        for(Point rat:rats){
            if(rat == null){
                continue;
            }
            if(Movement.distance(piper, rat) < distance){
                return false;
            }
        }
        return true;
    }

    private boolean teamPiperIsHelping(Point[][] pipers, int p, double distance){
        for (int i = 0; i < pipers[id].length; ++i) {
                if(i == p)
                    continue;
                if(Movement.distance(pipers[id][i], pipers[id][p]) < distance)
                    return true;
            }
        return false;
    }

	private Point[] generateStateMachine(int p){

        Point[] states = new Point[11];

        states[0] = gateEntrance;

        if(p == 1) p = 0;
        if(p == numberOfPipers - 2) p = numberOfPipers - 1;

        double theta = Math.toRadians(p * 90.0/(numberOfPipers - 1) + 45);

        states[1] = Movement.makePoint(side/2 * Math.cos(theta), side/2 - (side * 0.5 * Math.sin(theta)), neg_y, swap);

        states[2] = null;

        states[3] = gateEntrance;

        states[4] = gateEntrance;

        states[5] = insideGate;

        states[6] = null;

        states[7] = gateEntrance;

        states[8] = null;

        states[9] = gateEntrance;

        states[10] = insideGate;

        return states;

    }
}