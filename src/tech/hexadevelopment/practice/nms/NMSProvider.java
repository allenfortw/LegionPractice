package tech.hexadevelopment.practice.nms;

import org.bukkit.Bukkit;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.nms.access.NMSAccess;
import tech.hexadevelopment.practice.nms.access.NMS_1_10_R1;
import tech.hexadevelopment.practice.nms.access.NMS_1_11_R1;
import tech.hexadevelopment.practice.nms.access.NMS_1_12_R1;
import tech.hexadevelopment.practice.nms.access.NMS_1_13_R2;
import tech.hexadevelopment.practice.nms.access.NMS_1_13_R1;
import tech.hexadevelopment.practice.nms.access.NMS_1_7_R2;
import tech.hexadevelopment.practice.nms.access.NMS_1_7_R3;
import tech.hexadevelopment.practice.nms.access.NMS_1_7_R4;
import tech.hexadevelopment.practice.nms.access.NMS_1_8_R1;
import tech.hexadevelopment.practice.nms.access.NMS_1_8_R2;
import tech.hexadevelopment.practice.nms.access.NMS_1_8_R3;
import tech.hexadevelopment.practice.nms.access.NMS_1_9_R1;
import tech.hexadevelopment.practice.nms.access.NMS_1_9_R2;

public class NMSProvider {

	private NMSAccess access;
	private String version;
	public boolean laterThan1_8, versionHasNoItemIDs;
	
	public void setup() {
		switch(version = Bukkit.getServer().getClass().getPackage()
				.getName().substring(23)) {
		case "v1_7_R2":
			access = new NMS_1_7_R2();
			break;
		case "v1_7_R3":
			access = new NMS_1_7_R3();
			break;
		case "v1_7_R4":
			access = new NMS_1_7_R4();
			break;
		case "v1_8_R1":
			access = new NMS_1_8_R1();
			break;
		case "v1_8_R2":
			access = new NMS_1_8_R2();
			break;
		case "v1_8_R3":
			access = new NMS_1_8_R3();
			break;
		case "v1_9_R1":
			access = new NMS_1_9_R1();
			break;
		case "v1_9_R2":
			access = new NMS_1_9_R2();
			break;
		case "v1_0_R1":
			access = new NMS_1_10_R1();
			break;
		case "v1_11_R1":
			access = new NMS_1_11_R1();
			break;
		case "v1_12_R1":
			access = new NMS_1_12_R1();
			break;
		case "v1_13_R1":
			access = new NMS_1_13_R2();
			versionHasNoItemIDs = true;
			break;
		case "v1_13_R2":
			access = new NMS_1_13_R1();
			versionHasNoItemIDs = true;
			break;
		default:
			break;
		}
		if(access != null) {
			Bukkit.getLogger().info(LegionPractice.getInstance().getName() + ">> Version supported! (" + version + ")");
			if(!version.contains("v1_7_R") && !version.contains("v1_8_R")) {
				laterThan1_8 = true;
			}
		}
		else {
			Bukkit.getLogger().warning(LegionPractice.getInstance().getName() + ">> Version is not supported! (" + version + ")");
		}
	}
	
	public NMSAccess getAccess() {
		return access;
	}
	
	public String getVersion() {
		return version;
	}
}
