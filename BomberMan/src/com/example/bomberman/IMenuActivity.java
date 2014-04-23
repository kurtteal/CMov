package com.example.bomberman;

import java.util.TreeMap;

import com.example.bomberman.csclient.ClientAsyncTask;

public interface IMenuActivity {
	//method called by the client service on the activity with server response/update
	void createResponse(boolean result);
	void joinResponse(boolean result, char playerId);
	void updatePlayerList(TreeMap<Integer, String> clientsNames);
	void updateMap(char mapNumber);
	void startGameOrder();
}
