package com.example.bomberman.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.util.GameConfigs;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.Log;

//The arena is updated and drawn here
public class Arena {

	private List<Bomberman> deadElements = new ArrayList<Bomberman>();

	public IDrawable[][] pixelMatrix; // Matrix dos objectos para desenhar
	protected GameConfigs gc; // matrix com chars, para verificacao de colisoes
	public MainGamePanel panel;
	public Map<String, Integer> scores = new HashMap<String, Integer>();

	// os players/robots tem de ser os ultimos a ser desenhados
	// e o chao tem q ser desenhado por baixo deles, antes de estes serem
	// desenhados
	private Bomberman activePlayer;
	private List<Bomberman> players;
	private List<Robot> robots;
	private Resources resources;

	private boolean firstUpdate = true;

	// cant draw the arena on the constructor because I need
	// the panel dimensions... so draw on the first update

	public Arena(Resources resources, GameConfigs gameConfigs,
			MainGamePanel panel) {
		players = new ArrayList<Bomberman>();
		robots = new ArrayList<Robot>();
		gc = gameConfigs;
		this.panel = panel;
		this.resources = resources;
	}
	
	public Bomberman getActivePlayer(){
		return activePlayer;
	}

	private void fillDrawableMatrix() {

		int sizeX = gc.getSizeX();
		int sizeY = gc.getSizeY();
		pixelMatrix = new IDrawable[sizeX][sizeY];
		int i, j;
//		Log.d("Laaaaa", "test , last*sizeX= " 
//				+ ((panel.getWidth() - (panel.getWidth() / sizeX)*sizeX)/2) + "," + (panel.getWidth() - (panel.getWidth() / sizeX)*sizeX));
		int gameRightMargin = (panel.getWidth() - (panel.getWidth() / sizeY)*sizeY)/2;
		int gameBottomMargin = (panel.getHeight() - (panel.getHeight() / sizeX)*sizeX)/2;
		
		// preenche a matriz de objectos desenhaveis, e as listas de players e
		// robots
		for (i = 0; i < sizeX; i++) {
			int previousBottomBorder = gameBottomMargin;
			if (i != 0)
				previousBottomBorder = pixelMatrix[i - 1][0].getDownBorder();
			for (j = 0; j < sizeY; j++) {
				int previousRightBorder = gameRightMargin;
				if (j != 0)
					previousRightBorder = pixelMatrix[i][j - 1]
							.getRightBorder();
				switch (gc.matrix[i][j]) {
				case 'W':
					pixelMatrix[i][j] = new Wall(resources,
							previousRightBorder, previousBottomBorder, sizeY,
							sizeX, panel);
					break;
				case '-':
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.FLOOR, i, j, sizeY, sizeX, panel);
					break;
				case 'O':
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.OBSTACLE, i, j, sizeY, sizeX, panel);
					break;
				case 'B':
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.BOMB, i, j, sizeY, sizeX, panel);
					break;
				case 'R':
					robots.add(new Robot(resources, previousRightBorder,
							previousBottomBorder, gameRightMargin, gameBottomMargin, panel, gc.matrix[i][j],
							gc.robotSpeed, sizeY, sizeX));
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.FLOOR, i, j, sizeY, sizeX, panel);
					break;
				default:
					Bomberman player = new Bomberman(resources, previousRightBorder,
							previousBottomBorder, gameRightMargin, gameBottomMargin, panel, gc.matrix[i][j],
							sizeY, sizeX);
					if(gc.matrix[i][j] == '1')
						activePlayer = player;
					players.add(player);
					pixelMatrix[i][j] = new Path(resources,
							previousRightBorder, previousBottomBorder,
							PathState.FLOOR, i, j, sizeY, sizeX, panel);
					break;
				}
			}
			System.out.println("");
		}
	}

	public void plantBomb(int i, int j, char myself){
		//gc.writeLogicPosition(j, i,'B'); write state ja faz isto
		writeState(j, i, PathState.BOMB, myself);
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

	// marca este elemento como morto
	public void elementHasDied(Bomberman bomber) {
		deadElements.add(bomber);
	}

	/**
	 * Method which updates the arena's internal state every tick
	 */
	public void update(long gameTime) {

		if (firstUpdate) {
			fillDrawableMatrix();
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
		int sizeX = gc.getSizeX();
		int sizeY = gc.getSizeY();

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
		int sizeX = gc.getSizeX();
		int sizeY = gc.getSizeY();

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

	}
}
