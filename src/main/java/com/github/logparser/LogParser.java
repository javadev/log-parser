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
        java.util.Map<String, String> outMessages = new java.util.LinkedHashMap<String, String>();
        java.util.Map<String, String> inMessages = new java.util.LinkedHashMap<String, String>();
        while ((line = reader.readLine()) != null) {
            if (line.contains("OUT") && line.contains(grep)) {
                String outMessage = line;
                String outMessageId = line.replaceFirst(".*?messageId=(\\d+).*", "$1");
                outMessages.put(outMessageId, outMessage);
            } else if (line.contains("IN")) {
                String inMessageId = line.replaceFirst(".*?messageId=(\\d+).*", "$1");
                inMessages.put(inMessageId, line);
            }
        }
        for (java.util.Map.Entry<String, String> entry : outMessages.entrySet()) {
            System.out.println(entry.getValue());
            System.out.println(inMessages.get(entry.getKey()));
        }
    }
}
