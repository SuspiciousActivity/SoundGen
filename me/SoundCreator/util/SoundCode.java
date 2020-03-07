package me.SoundCreator.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class SoundCode {

	private static final ScriptEngine se = new ScriptEngineManager().getEngineByName("JavaScript");

	static {
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("sin", (Function<Double, Double>) Math::sin);
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("cos", (Function<Double, Double>) Math::cos);
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("tan", (Function<Double, Double>) Math::tan);
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("floor", (Function<Double, Double>) Math::floor);
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("ceil", (Function<Double, Double>) Math::ceil);
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("abs", (Function<Double, Double>) Math::abs);
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("random", (Supplier<Double>) Math::random);
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("PI", Math.PI);
	}

	public static void parse(List<String> funcCode) {
		ExportInfo exportInfo = new ExportInfo();
		exportRead: for (String s : funcCode) {
			String op = getOperator(s);
			if (op == null)
				break;
			switch (op) {
			case "OUT":
			case "FILE":
			case "OUTFILE": {
				exportInfo.setOutFile(new File(s.substring(op.length() + 1).trim()));
				break;
			}
			case "FMT":
			case "FORMAT":
			case "FRQ":
			case "FREQ":
			case "FREQUENCY": {
				exportInfo.setAudioFormat(parseAudioFormat(s.substring(op.length() + 1).trim()));
				break;
			}
			case "LEN":
			case "LENGTH": {
				String lenInfo = s.substring(op.length() + 1).trim();
				exportInfo.setOutSampleLen(parseLen(lenInfo, exportInfo.getAudioFormat()));
				break;
			}
			case "START": {
				break exportRead;
			}
			}
		}

		if (!exportInfo.isComplete())
			throw new IllegalArgumentException("Export info is incomplete!");

		funcCode = cutCodeParts(funcCode);

		int maxLen = 0;
		for (String s : funcCode) {
			if (s.startsWith("#"))
				continue;
			String[] split = s.split(" ");
			int len = parseLen(split[1], exportInfo.getAudioFormat());
			if (maxLen < len)
				maxLen = len;
		}
		int sampleSizeInBytes = exportInfo.getAudioFormat().getSampleSizeInBits() / 8;
		byte[] bytes = new byte[maxLen];
		se.getBindings(ScriptContext.GLOBAL_SCOPE).put("_div_", exportInfo.getAudioFormat().getSampleRate());

		if (!exportInfo.redirectToStdout())
			exportInfo.log("Applying functions...");
		for (String s : funcCode) {
			if (s.startsWith("#"))
				continue;
			String[] split = s.split(" ");
			int min = parseLen(split[0], exportInfo.getAudioFormat());
			int max = parseLen(split[1], exportInfo.getAudioFormat());
			int len = (max - min) / sampleSizeInBytes;
			double[] res = new double[len];

			se.getBindings(ScriptContext.GLOBAL_SCOPE).put("len", len);
			se.getBindings(ScriptContext.GLOBAL_SCOPE).put("res", res);

			try {
				se.eval("\n" + "for (var i = 0; i < len; i++) {\n" + "	var x = i / _div_;\n" + "	res[i] = "
						+ s.substring(split[0].length() + split[1].length() + 2) + ";\n" + "}");
				for (int i = 0; i < len; i++) {
					int b = (int) (res[i] * Short.MAX_VALUE);
//					for (int j = 0; j < sampleSizeInBytes; j++)
//						bytes[min * sampleSizeInBytes + i * sampleSizeInBytes + j] = (byte) (b >> (8 * j));
					bytes[min + i * sampleSizeInBytes] = (byte) b;
					bytes[min + i * sampleSizeInBytes + 1] = (byte) (b >> 8);
				}
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}
		exportInfo.log("Saving...");
		try {
			exportInfo.save(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getOperator(String s) {
		int idx = s.indexOf(' ');
		if (idx < 0)
			return null;
		return s.substring(0, idx).toUpperCase();
	}

	private static final Pattern lengthPattern = Pattern.compile("(\\d*(?:\\.\\d+)?)(\\+?)([sf]?)",
			Pattern.CASE_INSENSITIVE);

	private static int parseLen(String s, AudioFormat format) {
		Matcher m = lengthPattern.matcher(s);
		if (!m.find())
			throw new IllegalArgumentException("Invalid length '" + s + "'!");
		String duration = m.group(1);
		int durationMod = m.group(2).equals("+") ? 1 : 0;
		String specifier = m.group(3).toLowerCase();
		switch (specifier) {
		case "s": {
			if (format == null)
				throw new IllegalArgumentException("Please specify the audio format before any length in seconds.");
			return (int) (Double.parseDouble(duration) * format.getSampleRate() * (format.getSampleSizeInBits() / 8))
					+ durationMod * (format.getSampleSizeInBits() / 8);
		}
		case "f":
		case "": {
			return Integer.parseInt(duration) + durationMod;
		}
		default:
			throw new RuntimeException("Should never happen");
		}
	}

	private static AudioFormat parseAudioFormat(String s) {
		float sampleRate = -1;
		final int sampleSizeInBits = 16;
		final int channels = 1;
		sampleRate = Float.parseFloat(s);
		return new AudioFormat(Encoding.PCM_SIGNED, sampleRate, sampleSizeInBits, channels, sampleSizeInBits * channels,
				sampleRate, false);
	}

	private static List<String> cutCodeParts(List<String> list) {
		int startIdx = -1;
		int endIdx = -1;
		for (int i = 0; i < list.size(); i++) {
			String s = list.get(i);
			if (s.equalsIgnoreCase("START"))
				startIdx = i;
			else if (s.equalsIgnoreCase("END"))
				endIdx = i;
		}

		list.removeIf(new FromToPredicate<String>(startIdx, endIdx));
		return list;
	}

}
