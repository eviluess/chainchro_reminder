package eviluess.pkg.Utilities;

public class RootCommands extends ExecuteAsRoot {

	final static public boolean copy(String src, String dst) {

		LinuxCommandsBuilder lcb = new LinuxCommandsBuilder();

		lcb.chmod(777, dst);
		lcb.cp(src, dst);
		lcb.chmod(777, dst);

		return execute(lcb.getCommands());
	}
	final static public boolean mountSystemAsRW() {

		LinuxCommandsBuilder lcb = new LinuxCommandsBuilder();

		return execute(lcb.getCommands());
	}


	final static public boolean delete(String filename) {
		LinuxCommandsBuilder lcb = new LinuxCommandsBuilder();

		lcb.chmod(777, filename);
		lcb.rm(filename);
		lcb.rmdir(filename);

		return execute(lcb.getCommands());
	}

	final public static boolean uninstall(String packageName) {
		LinuxCommandsBuilder lcb = new LinuxCommandsBuilder();
		lcb.uninstall(packageName);

		return execute(lcb.getCommands());
	}

	final public static boolean reboot() {
		LinuxCommandsBuilder lcb = new LinuxCommandsBuilder();
		lcb.reboot();

		return execute(lcb.getCommands());
	}
}
