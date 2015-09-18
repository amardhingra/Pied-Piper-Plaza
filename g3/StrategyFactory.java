package pppp.g3;

import pppp.g3.Strategy;

public static class StrategyFactory{

	final public static double piperPivot = 1;
	final public static double ratPivot = 1;

	private Strategy currentStrategy = null;

	public static Strategy getStrategy(int id, int side, long turns,
	                 Point[][] pipers, Point[] rats){
		return null;
	}

	private double getRatDensity(Point[] rats){
		return 0;
	}

	private double getPiperDensity(Point[][] pipers, Point[] rats){
		return 0;
	}

}