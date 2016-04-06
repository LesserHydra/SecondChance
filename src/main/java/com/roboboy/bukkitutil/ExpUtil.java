package com.roboboy.bukkitutil;

public final class ExpUtil {
	
	/**
	 * Calculates the amount of exp needed to reach the given level.
	 * @param currentLevel Current level
	 * @return See above
	 */
	public static int calculateXpFromLevel(int currentLevel) {
		if (currentLevel < 17) return (int) (currentLevel*currentLevel + 6*currentLevel);
		if (currentLevel < 32) return (int) (2.5*currentLevel*currentLevel - 40.5*currentLevel + 360);
		return (int) (4.5*currentLevel*currentLevel - 162.5*currentLevel + 2220);
	}
	
	/**
	 * Calculates the amount of exp needed to move from the current level to the next.
	 * @param currentLevel Current level
	 * @return See above
	 */
	public static int calculateXpForNextLevel(int currentLevel) {
		if (currentLevel < 17) return 2*currentLevel + 7;
		if (currentLevel < 32) return 5*currentLevel - 38;
		return 9*currentLevel - 158;
	}
	
	/**
	 * Calculates the amount of exp needed to reach the given level and progress.
	 * @param currentLevel Current level
	 * @param progress Progress towards the next level, 0-1 inclusive
	 * @return See above
	 */
	public static int calculateXpFromProgress(int currentLevel, double progress) {
		return (int) (progress*calculateXpForNextLevel(currentLevel));
	}
	
}
