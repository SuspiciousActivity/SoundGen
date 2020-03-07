package me.SoundCreator.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import me.SoundCreator.util.SoundCode;

public class Main {

	public static void main(String[] args) {
		List<String> funcCode = null;
		if (args.length == 1) {
			try {
				funcCode = Files.readAllLines(new File(args[0]).toPath(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Specified file '" + args[0] + "' can not be read: " + e.getMessage());
				System.exit(1);
			}
		} else {
			if (System.console() == null)
				System.exit(2);
			try (Scanner scanner = new Scanner(System.in)) {
				funcCode = new ArrayList<String>();
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (!line.isEmpty())
						funcCode.add(line);
					if (line.equalsIgnoreCase("END"))
						break;
				}
			}
		}
		SoundCode.parse(funcCode);
	}

}
