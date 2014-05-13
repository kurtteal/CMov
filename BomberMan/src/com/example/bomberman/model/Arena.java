package com.example.bomberman.model;

import java.util.ArrayList;
import java.util.List;

import com.example.bomberman.GamePanel;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.ScoreBoard;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.Log;

//The arena is updated and drawn here
public class Arena {

	private List<Bomberman> deadElements = new ArrayList<Bomberman>();

	public IDrawable[][] pixelMatrix; // Matrix dos objectos para desenhar
	private List<Bomberman> players;
	private List<Bomberman> unjoinedPlayers;
	private List<Robot> robots;
	
	protected GameConfigs gc; // matrix com chars, para verificacao de colisoes
	public GamePanel panel;
	public ScoreBoard scores = new ScoreBoard();
	
	//Cada bomberman so pode por uma bomba de cada vez (enunciado)
	public List<String> bombs; //contem os 'ids' dos players que puseram bombas

	// Os players/robots tem de ser os ultimos a ser desenhados
	// e o chao tem q ser desenhado por baixo deles, antes de estes serem
	// desenhados
	private Bomberman activePlayer;
	public char playerId = '1';
	private Resources resources;
	private int robotIdCounter = 0; //used to attribute an id to each robot (useful for multiplayer)

	private boolean firstUpdate = true;

	private int numPlayers;

	// Cant draw the arena on the constructor because I need
	// the panel dimensions... so draw on the first update
	public Arena(Resources resources, GameConfigs gameConfigs,
			GamePanel panel) {
		players = new ArrayList<Bomberman>();
		unjoinedPlayers = new ArrayList<Bomberman>();
		robots = new ArrayList<Robot>();
		gc = gameConfigs;
		this.panel = panel;
		this.resources = resources;
		bombs = new ArrayList<String>();
	}
	
	//Used in multiplayer: the game master starts the robot behaviour locally that will start their movement in other
	//instances
	public void startRobots(){
		for(Robot r : robots)
			r.startMoving();
	}
	
	public Bomberman getActivePlayer(){
		return activePlayer;
	}
	
	public Bomberman getPlayer(char playerId){
		for(Bomberman player : players){
			if(player.myself == playerId)
				return player;
		}
		return null;
	}
	
	public Robot getRobot(int id){
		for(Robot b : robots)
			if(b.robotId == id)
				return b;
		return null;
	}

	
//	public void setActivePlayer(char id){
//		playerId = id;
//		for(Bomberman b : players){
//			if(b.myself == playerId)
//				activePlayer = b;
//		}
//	}

	private void fillDrawableMatrix() {
		
		playerId = panel.activity.playerId;
		int numLines = gc.getNumLines();
		int numColumns = gc.getNumColumns();
		numPlayers = panel.activity.getNumPlayers();
		pixelMatrix = new IDrawable[numLines][numColumns];
		int i, j;
//		Log.d("Laaaaa", "test , last*sizeX= " 
//				+ ((panel.getWidth() - (panel.getWidth() / sizeX)*sizeX)/2) + "," + (panel.getWidth() - (panel.getWidth() / sizeX)*sizeX));
		int gameRightMargin = (panel.getWidth() - (panel.getWidth() / numColumns)*numColumns)/2;
		int gameBottomMargin = (panel.getHeight() - (panel.getHeight() / numLines)*numLines)/2;
		
		// preenche a matriz de objectos desenhaveis, e as listas de players e
		// robots
		for (i = 0; i < numLines; i++) {
			int previousBottomBorder = gameBottomMargin;
			if (i != 0)
				previousBottomBorder = pixelMatrix[i - 1][0].getDownBorder();
			for (j = 0; j < numColumns; j++) {
				int previousRightBorder = gameRightMargin;
				if (j != 0)
					previousRightBorder = pixelMatrix[i][j - 1]
							.getRightBorder();
				switch (gc.matrix[i][j]) {
				case 'W':
					pixelMatrix[i][j] = new Wall(resources,
							previousRightBorder, previousBottomBorder, numColumns,
							numLines, panel);
					break;
				case '-':
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.FLOOR, i, j, numColumns, numLines, panel);
					break;
				case 'O':
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.OBSTACLE, i, j, numColumns, numLines, panel);
					break;
				case 'B':
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.BOMB, i, j, numColumns, numLines, panel);
					break;
				case 'R':
					robots.add(new Robot(resources, previousRightBorder,
							previousBottomBorder, gameRightMargin, gameBottomMargin, panel, gc.matrix[i][j],
							gc.robotSpeed, numColumns, numLines, robotIdCounter++));
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.FLOOR, i, j, numColumns, numLines, panel);
					break;
				default:
					Bomberman player = new Bomberman(resources, previousRightBorder,
							previousBottomBorder, gameRightMargin, gameBottomMargin, panel, gc.matrix[i][j],
							numColumns, numLines);
					if(gc.matrix[i][j] == playerId)
						activePlayer = player;
					if(Character.getNumericValue(gc.matrix[i][j]) <= numPlayers ){
						players.add(player);
						String playerId = "" + gc.matrix[i][j];
						scores.add(playerId);
					}else{
						unjoinedPlayers.add(player);
					}
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.FLOOR, i, j, numColumns, numLines, panel);
					break;
				}
			}
			System.out.println("");
		}
		
	}
	
	//A new player enters in the middle of the game
	public void newPlayer(char newPlayerId){
		Bomberman joiningBomber = null;
		for(Bomberman b : unjoinedPlayers){
			if(b.myself == newPlayerId){
				joiningBomber = b;
				break;
			}
		}
		//remove from unjoinedList and add to players list
		unjoinedPlayers.remove(joiningBomber);
		players.add(joiningBomber);
		scores.add(""+newPlayerId);
	}

	public void plantBomb(int i, int j, char myself){
		boolean canPlant;
		String planter = "" + myself;
		if(myself != 'R'){ //players so podem por 1 bomba
			//Verificar se este player ja pos uma bomba que ainda nao explodiu
			canPlant = true;
			for(String player : bombs){
				if(player.equals(planter)){
					canPlant = false;
					break;
				}
			}
		}
		else
			canPlant = true; //robots podem por as bombas que quiserem
		
		if(canPlant){
			//gc.writeLogicPosition(j, i,'B'); setState do Path ja faz isto
			writeState(i, j, PathState.BOMB, myself);
			//so os players eh q sao adicionados 
			if(myself != 'R')
				bombs.add(planter);
		}
	}
	
	//Removes the playerId from the list of playerIds who had put a bomb
	protected void bombExploded(char owner){
		String planter = "" + owner;
		if(bombs.contains(planter))
			bombs.remove(planter);
	}
	
	// Proteccao contra acessos concorrentes
	// Escreve o novo estado no objecto Path localizado em i,j
	public void writeState(int i, int j, PathState state, char owner) {
		synchronized (pixelMatrix) {
			pixelMatrix[i][j].setState(state, owner);
		}
	}

	public void removeElement(Bomberman bomber) {
		if (bomber.getId() == 'R')
			robots.remove(bomber);
		else
			players.remove(bomber);
	}

	public void elementHasDied(Bomberman bomber) {

		// marca este elemento como morto
		deadElements.add(bomber);
		char victimId = bomber.myself;
		//vou ah procura do dono da explosao que matou este elemento, e aumento o score se for player
		char killerId = ((Path)pixelMatrix[bomber.i][bomber.j]).getOwner();
		Log.d("Killer id:", "" + killerId);
		//Se nao foi morte por contacto com robot, vai actualizar o score de algum player
		if(killerId != '#'){
			String planter = "" + killerId;
			if(killerId != 'R' && victimId != killerId){ //suicide doesnt count
				if(victimId == 'R')
					scores.update(planter, gc.ptsPerRobot);
				else
					scores.update(planter, gc.ptsPerPlayer);
			}
		}
		//Se o jogador local morreu, deixa de poder usar os controlos!
		if(victimId == playerId){
			Log.d("IN DEAD", "IN DEADDDDDD");
			panel.activity.disableControlsAfterDeath();
		}
		//Log.d("MORREU", "Morreu: " + deadBomberId + " nas coords[i][j]: " + i + " " + j);
		//Log.d("DEAD", "Score do " + planter + " is: " + scores.get(planter));
	}

	/**
	 * Method which updates the arena's internal state every tick
	 */
	public void update(long gameTime) {

		if (firstUpdate) {
			fillDrawableMatrix();
			panel.activity.startGame();
			firstUpdate = false;
		}
		// Removes any dead players/robots from the list, these wont be updated
		// or drawn anymore
		if (!deadElements.isEmpty()) {
			for (Bomberman bomber : deadElements)
				removeElement(bomber);
			deadElements.clear();// limpa a lista de mortos
		}

		int i, j;
		int sizeX = gc.getNumLines();
		int sizeY = gc.getNumColumns();

		for (i = 0; i < sizeX; i++) {
			for (j = 0; j < sizeY; j++) {
				// actualiza os objectos que precisam de se actualizar
				// em termos de posicao e frames, e actualiza a matriz
				// de estados que esta em 'gc' -> importante para as bombas
				pixelMatrix[i][j].update(gameTime, gc);
			}
		}
		for (Robot robot : robots)
			robot.update(gameTime);
		for (Bomberman player : players)
			player.update(gameTime);
	}

	public void draw(Canvas canvas) {
		int i, j;
		int sizeX = gc.getNumLines();
		int sizeY = gc.getNumColumns();

		// desenha primeiro a arena em si
		for (i = 0; i < sizeX; i++) {
			for (j = 0; j < sizeY; j++) {
				pixelMatrix[i][j].draw(canvas);
			}
		}
		// depois os robots e os players
		for (Robot robot : robots)
			robot.draw(canvas);
		for (Bomberman player : players)
			player.draw(canvas);
		
		//JOGO TERMINA SE NAO HOUVER MAIS PLAYERS
		//ou se havendo apenas 1 player nao ha robots
		if(players.isEmpty() || players.size() == 1 && robots.isEmpty()){
			panel.endGame();
		}

	}
}
