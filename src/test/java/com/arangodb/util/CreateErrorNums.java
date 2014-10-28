package com.arangodb.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts the arangodb errors.dat file into a java file
 * 
 * @author abrandt
 * 
 */
public class CreateErrorNums {

	public static class ErrorNum {
		private String name;
		private Integer num;

		public ErrorNum(String name, Integer num) {
			this.name = name;
			this.num = num;
		}

		public String getName() {
			return name;
		}

		public Integer getNum() {
			return num;
		}

		@Override
		public String toString() {
			return "\tpublic static final int " + name + " = " + num + ";";
		}

	}

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println("");
			System.out.println("CreateErrorNums <path and filename of arangodb errors.dat>");
			System.out.println("");
			System.out.println("Example");
			System.out.println("CreateErrorNums ~/arangodb/lib/BasicsC/errors.dat");
			System.exit(0);
		}

		String filename = args[0];

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.println("File '" + filename + "' not found");
			System.exit(0);
		}

		List<ErrorNum> errorNums = new ArrayList<ErrorNum>();

		String line;
		try {
			while ((line = br.readLine()) != null) {
				// process the line.

				String[] part = line.split(",");
				if (part.length > 2) {
					Integer num = new Integer(part[1]);
					ErrorNum en = new ErrorNum(part[0], num);
					errorNums.add(en);
				}

			}
		} catch (IOException e1) {
		}

		try {
			br.close();
		} catch (IOException e) {
		}

		try {
			String current = new java.io.File(".").getCanonicalPath();
			System.out.println("Current dir: " + current);

			File file = new File("src/main/java/at/orz/arangodb/ErrorNums.java");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));

			output.write("package com.arangodb;\r\n");
			output.write("\r\n");
			output.write("public class ErrorNums {\r\n");
			output.write("\r\n");

			for (ErrorNum en : errorNums) {
				output.write(en.toString() + "\r\n");
			}

			output.write("\r\n");
			output.write("}\r\n");
			output.write("\r\n");

			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("OK.");
	}

}
