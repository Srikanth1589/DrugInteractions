package com.drug.interaction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DrugDrugInteraction {
	static Map<Integer, String> severityIntegerToStringMap = severityISMap();
	static Map<String, Integer> severityStringToIntegerMap = severitySIMap();
	static List<List<String>> input = new ArrayList<>();
	static Map<String, Integer> drugIndexMap = new HashMap<>();
	static Map<String, Map<String, String>> severityDrugDescriptionMap = readFromFile();

	public static void main(String[] args) throws Exception {
		// Read from console
		readScanner();
		// Drug interactions classification
		List<String> interactions = identifyDrugInteractions();
		// Writing to console
		outputWriter(interactions);
	}

	/**
	 * Reading the input from Stdin. Adding a sentence as list and lines as list of list of  string
	 * @throws IOException
	 */
	private static void readScanner() throws IOException {
		System.out.println(
				"Start typing. Pressing Return key will consider as a new line. When you are done entering drugs data, press Return key twice.\n");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = in.readLine();
		while (!line.isEmpty()) {
			List<String> sentenceStringsArray = Arrays.asList(line.split(" "));
			List<String> formattedSentenceStringsArray = sentenceStringsArray.stream().map((str) -> str.toLowerCase()).collect(Collectors.toList());
			if (formattedSentenceStringsArray.size() < 1 || formattedSentenceStringsArray.size() > 20) {
				System.out.println("\nNumber of medications per line should be between 1 and 20. Its going to stop taking input from here on");
				break;
			}
			// Continuing here if the list per sentence is more than 20 or less than 1
			input.add(formattedSentenceStringsArray);
			if (input.size() > 10000) {
				System.out.println("Number of lines per execution should be between 1 and 10,000");
				break;
			}
			line = in.readLine();
		}
		System.out.println("\nDrugs entering completed\n");
		in.close();
	}

	/**
	 * Reading from file `interactions.json` and create a map of (drug,drug) to (severity, description) map.
	 * The concatenated string for drugs help with making it as a key value instead of two different drugs
	 * 
	 * @return the map of drug to map of severity and description
	 */
	private static Map<String, Map<String, String>> readFromFile() {
		Map<String, Map<String, String>> drugSeverityDescriptionMap = new HashMap<>();
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("interactions.json"));

			JSONArray fullObject = (JSONArray) obj;

			Iterator<JSONObject> iterator = fullObject.iterator();
			int index = 0;
			while (iterator.hasNext()) {
				JSONObject jsonObject = iterator.next();
				String severity = (String) jsonObject.get("severity");
				Map<String, String> severityDescription = new HashMap<>();

				List<String> drugs = (List<String>) jsonObject.get("drugs");
				String joinedDrugs = drugs.stream().sorted().collect(Collectors.joining(","));
				if (drugSeverityDescriptionMap.containsKey(joinedDrugs)) {
					severityDescription = drugSeverityDescriptionMap.get(joinedDrugs);
				}
				severityDescription.put(severity, (String) jsonObject.get("description"));
				drugSeverityDescriptionMap.put(joinedDrugs, severityDescription);
				drugIndexMap.put(joinedDrugs, index++);
			}
			System.out.printf("Read full data from json file with %d interactions %n%n", drugSeverityDescriptionMap.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return drugSeverityDescriptionMap;
	}

	/**
	 * The main function to take the input, create subsets of two, concatenated the two drugs,
	 * check if the json list has the concatenated drug, if so create a sorted map of index (ascending) to description
	 * and then create a map of drug to above created map,
	 * 
	 * These maps puts the entries in the form ready to process. So pick the top entries in each map
	 * 
	 * @return the list of interactions in the order we took the input
	 */
	private static List<String> identifyDrugInteractions() {
		List<String> outputIteractions = new ArrayList<>();
		List<List<String>> enteredInput = input;
		Comparator<Integer> keyComparatorDescending = (a, b) -> b.compareTo(a);
        Comparator<Integer> keyComparatorAscending = (a, b) -> a.compareTo(b);  

        for (List<String> drugs : enteredInput) {
    		// Using Treemap so that sorting the severity in the needed order
    		//SortedMap<Integer, List<String>> mapOfSeverityDescriptions = new TreeMap<>(keyComparator);
            SortedMap<Integer, SortedMap<Integer, String>> mapOfSeverityDescriptions = new TreeMap<>(keyComparatorDescending);
			List<String> drugSubsets = createSubsets(drugs);
			for (String drug : drugSubsets) {
				if (severityDrugDescriptionMap.containsKey(drug)) {
					Map<String, String> severityDescription = severityDrugDescriptionMap.get(drug);
					Map.Entry<String, String> entryTemp = severityDescription.entrySet().iterator().next();
					Integer intSeverity = severityStringToIntegerMap.get(entryTemp.getKey());
					SortedMap<Integer, String> severityDes = new TreeMap<>(keyComparatorAscending);
					if (mapOfSeverityDescriptions.containsKey(intSeverity)) {
						severityDes = mapOfSeverityDescriptions.get(intSeverity);
					}
					severityDes.put(drugIndexMap.get(drug), entryTemp.getValue());
					mapOfSeverityDescriptions.put(intSeverity, severityDes);
				}
			}
			//	If the sentence has drugs which have interactions vs no interactions
			if (mapOfSeverityDescriptions.size() > 0) {
				Map.Entry<Integer, SortedMap<Integer, String>> entryL = mapOfSeverityDescriptions.entrySet().iterator().next();
				Map.Entry<Integer, String> entryInsideL = entryL.getValue().entrySet().iterator().next();
				outputIteractions.add(formatOutput(entryL.getKey(), entryInsideL.getValue()));
			} else {
				outputIteractions.add(formatOutput(Integer.MIN_VALUE, null));
			}
		}
		return outputIteractions;
	}
	
	/**
	 * Writing the interactions to output console
	 */
	private static void outputWriter(List<String> interactions) {
		System.out.println("Output:");
		for (String interaction: interactions) {
			System.out.println(interaction);
		}
		System.out.println("\nCompleted writing to console");
	}
	
	/**
	 * Helper function to format the output as needed in console
	 * 
	 * @param key severity in integer form
	 * @param value description in string form
	 * @return formatted output in string form
	 */
	private static String formatOutput(Integer key, String value) {
		if (key >= 0 && value != null) {
			return severityIntegerToStringMap.get(key).toUpperCase().concat(": ").concat(value);
		}
		return "No interaction";
	}

	/**
	 * Takes a list of strings. Creates a subset of two and returns as a single string separated by comma
	 * The time complexity is not much to be affected as it has max of 20 elements
	 * 
	 * @param superset list of strings
	 * @return list of strings separated by comma
	 */
	private static List<String> createSubsets(List<String> superset) {
		List<String> sortedSuperset = superset.stream().sorted().collect(Collectors.toList());
		List<String> subsetCollection = new ArrayList<>();
		for (int i = 0; i < sortedSuperset.size(); i++) {
			for (int j = i + 1; j < sortedSuperset.size(); j++) {
				if (sortedSuperset.get(i) != sortedSuperset.get(j)) {
					subsetCollection.add(sortedSuperset.get(i).concat(",").concat(sortedSuperset.get(j)));
				}
			}
		}
		return subsetCollection;
	}

	/**
	 * This is a map function to create an integer for a severity. 
	 * This helps in sorting the map
	 * 
	 * @return the severity in integer form. range (0-2)
	 */
	private static Map<Integer, String> severityISMap() {
		Map<Integer, String> temp = new HashMap<>();
		temp.put(2, "major");
		temp.put(1, "moderate");
		temp.put(0, "minor");
		return temp;
	}

	/**
	 * This is a map function to create the severity String back from Integer. 
	 * 
	 * @return the severity in string form. ('major', 'moderate', 'minor')
	 */
	private static Map<String, Integer> severitySIMap() {
		return severityIntegerToStringMap.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));
	}
}
