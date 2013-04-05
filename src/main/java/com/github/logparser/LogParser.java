/*
 * $Id$
 *
 * Copyright 2013 Valentyn Kolesnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.logparser;

/**.
 * @author Valentyn Kolesnikov
 * @version $Revision$ $Date$
 */
public class LogParser {
    private static final String USAGE = "Usage: java -jar log-parser.jar ums.log --grep=\"text\"";
    private String grep;
    private String logFileName;
    private java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MM-dd HH:mm:ss,SSS");
    private java.text.DecimalFormat decFormat = new java.text.DecimalFormat("00");
    private java.text.DecimalFormat decFormat3 = new java.text.DecimalFormat("000");

    /**.
     * @param args - the arguments
     * @throws Exception in case of error
     */
    public static void main(String[] args) throws Exception {
        new LogParser().process(args);
    }

    /**.
     * @param args - the arguments
     * @throws Exception in case of error
     */
    public void process(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(USAGE);
            return;
        }
        for (String arg : args) {
            if (arg.startsWith("--grep=")) {
                grep = arg.substring(7).replaceFirst("\"(.*?)\"", "$1");
            } else {
                logFileName = arg;
            }
        }
        if (grep == null || logFileName == null) {
            System.out.println(USAGE);
            return;
        }
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(logFileName));
        String line = null;
        java.util.List<String> messages = new java.util.LinkedList<String>();
        java.util.Map<String, Integer> outMessages = new java.util.LinkedHashMap<String, Integer>();
        java.util.Map<String, Integer> inMessages = new java.util.LinkedHashMap<String, Integer>();
        while ((line = reader.readLine()) != null) {
            if (line.contains("OUT") && line.contains(grep)) {
                String outMessage = line;
                String outMessageId = line.replaceFirst(".*?messageId=(\\d+).*", "$1");
                outMessages.put(outMessageId, messages.size());
                messages.add(line);
            } else if (line.contains("IN")) {
                String inMessageId = line.replaceFirst(".*?messageId=(\\d+).*", "$1");
                inMessages.put(inMessageId, messages.size());
                messages.add(line);
            }
        }
        for (java.util.Map.Entry<String, Integer> entry : outMessages.entrySet()) {
            if (inMessages.get(entry.getKey()) != null) {
                String diffTime = calcDiffTime(messages.get(entry.getValue()), messages.get(inMessages.get(entry.getKey())));
                System.out.println(diffTime + "|" + messages.get(entry.getValue()));
                System.out.println(diffTime + "|" + messages.get(inMessages.get(entry.getKey())));
            }
        }
    }

    public String calcDiffTime(String startTime, String endTime) {
        String result = "";
        String startDate = startTime.replaceFirst(".*?(\\d{2}\\-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}).*", "$1");
        String endDate = endTime.replaceFirst(".*?(\\d{2}\\-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}).*", "$1");
        try {
            long start = formatter.parse(startDate).getTime();
            long end = formatter.parse(endDate).getTime();
            long diff = end - start;
            long millis = diff % 1000;
            diff /= 1000;
            long seconds = diff % 60;
            diff /= 60;
            long minutes = diff % 60;
            diff /= 60;
            long hours = diff;
            result = decFormat.format(hours) + ":" + decFormat.format(minutes) + ":" + decFormat.format(seconds) + "," + decFormat3.format(millis);
        } catch (java.text.ParseException ex) {
                ex.printStackTrace();
        };
        return result;
    }
}
