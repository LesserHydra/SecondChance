package com.lesserhydra.secondchance.compat;

import com.lesserhydra.secondchance.compat.v1_8_R1.Compat1_8_R1;
import com.lesserhydra.secondchance.compat.v1_8_R2.Compat1_8_R2;
import com.lesserhydra.secondchance.compat.v1_8_R3.Compat1_8_R3;
import com.lesserhydra.secondchance.compat.v1_9_Plus.Compat1_9_Plus;

public class CompatHandler {
	
	public static Compat getVersion(String version) {
		if (version.equals("v1_8_R1")) return new Compat1_8_R1();
		else if (version.equals("v1_8_R2")) return new Compat1_8_R2();
		else if (version.equals("v1_8_R3")) return new Compat1_8_R3();
		else return new Compat1_9_Plus();
	}
	
}
