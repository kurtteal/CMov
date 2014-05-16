package com.example.bomberman.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ScoreBoard implements Serializable {

	private static final long serialVersionUID = -8749964416361074903L;
	private Map<String, Integer> scores;
	
	public ScoreBoard() {
		scores = new HashMap<String, Integer>();
	}
	
	public void add(String playerId) {
		scores.put(playerId, 0);
	}
	
	public void add(String playerId, int score) {
		scores.put(playerId, score);
	}
	
	public void update(String playerId, int score) {
		scores.put(playerId, scores.get(playerId) + score);
	}
	
	public void remove(String playerId){
		scores.remove(playerId);
	}
	
	public Integer get(char playerId) {
		return scores.get(playerId+"");
	}
	
	public Set<Entry<String, Integer>> entrySet() {
		return scores.entrySet();
	}

	public Map<String, Integer> getSortedMap() {
        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(scores.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
	
}
