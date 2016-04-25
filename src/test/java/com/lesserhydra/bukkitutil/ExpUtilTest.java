package com.lesserhydra.bukkitutil;

import static org.junit.Assert.*;
import java.util.stream.IntStream;
import org.junit.Test;

public class ExpUtilTest {
	
	//Small test to check accuracy of calculateXpFromLevel()
	@Test public void experienceCalculation() {
		for (int i = 0; i <= 1000; i++) {
			int formula = ExpUtil.calculateXpFromLevel(i+1);
			int iteration = IntStream.range(0, i)
					.map(ExpUtil::calculateXpForNextLevel)
					.sum();
			assertEquals("Level-" + i, iteration, formula);
		}
	}
	
}
