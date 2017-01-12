package eviluess.pkg.Utilities;

import java.util.ArrayList;

public class LinuxCommandsBuilder {

	ArrayList<String> commands;

	public LinuxCommandsBuilder() {
		commands = new ArrayList<String>();

		mountSystem();
	}

	public final String mountSystem() {
		String cmd = "mount -o remount rw /system";
		commands.add(cmd);
		return cmd;
	}

	public final String chmod(int mod, String filename) {
		String cmd = "chmod " + mod + " " + filename;
		commands.add(cmd);
		return cmd;
	}

	public final String cp(String src, String dst) {
		String cmd = "cp " + src + " " + dst;
		commands.add(cmd);
		return cmd;
	}

	public final ArrayList<String> getCommands() {
		return commands;
	}

	public final String rm(String filename) {
		String cmd = "rm " + filename;
		commands.add(cmd);
		return cmd;
	}
	public final String rmdir(String filename) {
		String cmd = "rm -fr " + filename;
		commands.add(cmd);
		return cmd;
	}
	public final String uninstall(String packageName) {
		String cmd = "pm uninstall " + packageName;
		commands.add(cmd);
		return cmd;
	}

	public final String reboot() {
		String cmd = "reboot";
		commands.add(cmd);
		return cmd;
	}

}
