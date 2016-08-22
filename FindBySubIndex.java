package com.baby.prac;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FindBySubIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inputStr = genText();
		System.err.println("inputStr:\n" + inputStr + "\n");
		int indexStart = 0;
		int indexEnd = 0;
		int sub1 = 0;
		int sub2 = 0;
		List<String> resultList = new ArrayList<String>();

		while (true) {
			if (indexStart >= inputStr.length()) {
				break;
			}
			// System.err.println("indexStart:" + indexStart);
			sub1 = inputStr.indexOf("$", indexStart);
			// System.err.println("sub1:" + sub1);

			if (sub1 > -1) {
				boolean isCmd = isCommand(inputStr.substring(sub1, sub1 + 3));

				if (isCmd) {
					indexEnd = sub1 + 1;
					// System.err.println("sub2:" + sub2);

					while (true) {
						if (indexEnd >= inputStr.length()) {
							break;
						}

						sub2 = inputStr.indexOf("$", indexEnd);

						if (sub2 > -1) {
							boolean isCmd2 = isCommand(inputStr.substring(sub2,
									sub2 + 3));

							if (isCmd2) {
								String result = inputStr.substring(sub1, sub2);
								resultList.add(result);

								indexStart = sub2;
								break;
							} else {
								indexEnd++;
							}

						} else {
							// 如果有找到第一個開頭卻找不到第二個結尾，則代表是最後一個命令.
							String resultOnly1 = inputStr.substring(sub1);
							// System.err.println("Only1:" + resultOnly1);
							resultList.add(resultOnly1);
							
							// 要一次跳出兩個 while 迴圈.
							indexStart = inputStr.length();
							break;
						}
					}

				} else {
					indexStart++;
				}
			} else {
				break;
			}
		}

		System.out.println("Finally:" + resultList);

	}

	private static boolean isCommand(String threeCharacters) {
		// System.out.println("Do isCommand, threeCharacters:" +
		// threeCharacters);
		String patternStr = "\\$[UP]\\s{1,}.*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(threeCharacters);
		boolean matchFound = matcher.find();
		return matchFound;
	}

	private static String genText() {
		StringBuilder sb = new StringBuilder();
		sb.append("$P utes n" + "\n");
		sb.append("<test>  utes n");
		sb.append("$U  utes n" + "\n");
		sb.append("$1 yyyy n");
		sb.append("aaaaaaa   ");
		sb.append("$P yyyy n");
		return sb.toString();
	}

}
