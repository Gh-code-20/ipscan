/*
  This file is a part of Angry IP Scanner source code,
  see http://www.angryip.org/ for more information.
  Licensed under GPLv2.
 */
package net.azib.ipscan.gui.actions;

import net.azib.ipscan.config.LoggerFactory;
import net.azib.ipscan.config.Platform;
import net.azib.ipscan.core.UserErrorException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

/**
 * The cross-platform terminal launcher
 * 
 * @author Anton Keks
 */
public class TerminalLauncher {
	static final Logger LOG = LoggerFactory.getLogger();
	
	/** caches last working terminal emulator */
	private static String workingTerminal;
	
	/**
	 * Launches the execString in the terminal.
	 * Supports Linux/Unix, MacOS, and Windows
	 * @param execString the command to launch
	 * @param workingDir the working directory (or null)
	 */
	public static void launchInTerminal(String execString, File workingDir) {
		try {
			if (Platform.WINDOWS) {
				// generate a command file :-)
				File batFile = File.createTempFile("launch", ".cmd");
				batFile.deleteOnExit();
				try (FileWriter writer = new FileWriter(batFile)) {
					writer.write("@rem This is a temporary file generated by Angry IP Scanner\n" +
							     "@start cmd /k " + execString);
				}

				Runtime.getRuntime().exec(batFile.getAbsolutePath(), null, workingDir);
			}
			else if (Platform.MAC_OS) {
				Runtime.getRuntime().exec(new String[] {"osascript", "-e", "tell application \"Terminal\" to do script \"" + execString + "\""}, null, workingDir);
			}
			else { // assume Linux
				if (workingTerminal == null) workingTerminal = detectWorkingTerminal();
				String shell = System.getenv("SHELL");
				if (shell == null) shell = "sh";
				Runtime.getRuntime().exec(new String[] {workingTerminal, "-e", shell, "-xc", execString + ";" + shell}, null, workingDir);
			}
		}
		catch (Exception e) {
			// log and display the error
			LOG.log(Level.WARNING, "openTerminal.failed", e);
			throw new UserErrorException("openTerminal.failed", execString);
		}
	}

	private static String detectWorkingTerminal() {
		for (String term : asList("x-terminal-emulator", "xdg-terminal", "gnome-terminal", "xfce4-terminal", "konsole", "xterm")) {
			File file = new File("/usr/bin/" + term);
			if (file.exists()) return file.getPath();
		}
		return "xterm";
	}
}
