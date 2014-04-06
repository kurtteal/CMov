package com.example.bomberman.model;

import java.util.ArrayList;
import java.util.List;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.util.GameConfigs;

import android.content.res.Resources;
import android.graphics.Canvas;

public class Arena {

	protected IDrawable[][] arena; //Matrix dos objectos para desenhar
	protected GameConfigs gc; //matrix com chars, para verificacao de colisoes
	
	//os players/robots tem de ser os ultimos a ser desenhados
	//e o chao tem q ser desenhado por baixo deles, antes de estes serem desenhados
	private List<Bomberman> players;
	private List<Robot> robots;
	
	public Arena (Resources resources, GameConfigs gameConfigs, MainGamePanel panel) {
		players = new ArrayList<Bomberman>();
		robots = new ArrayList<Robot>();
		gc = gameConfigs;
		int i,j;
		int sizeX = gc.getSizeX();
		int sizeY = gc.getSizeY();
		arena = new IDrawable[sizeX][sizeY];

		//preenche a matriz de objectos desenhaveis, e as listas de players e robots
		for(i=0; i< sizeX; i++){
			int previousBottomBorder = 0;
			if(i!=0)
				previousBottomBorder = arena[i-1][0].getDownBorder();	
			for(j=0; j< sizeY; j++){
				int previousRightBorder = 0;
				if(j != 0)
					previousRightBorder = arena[i][j-1].getRightBorder();
				switch(gc.matrix[i][j]){
					case 'W':
						arena[i][j] = new Wall(resources, previousRightBorder, previousBottomBorder, i, j);
						break;
					case '-':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR, i, j, this);
						break;
					case 'O':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.OBSTACLE, i, j, this);
						break;
					case 'B':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.BOMB, i, j, this);
						break;
					case 'R':
						robots.add(new Robot(resources, previousRightBorder, previousBottomBorder, panel, gc.matrix[i][j], gc.robotSpeed));
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR, i, j, this);
						break;
					default:
						players.add(new Bomberman(resources, previousRightBorder, previousBottomBorder, panel, gc.matrix[i][j]));
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR, i, j, this);
						break;
				}
			}
			System.out.println("");
		}
		
	}
	
	public void elementHasDied(Bomberman bomber){
		if(bomber.getId() == 'R')
			robots.remove(bomber);
		else
			players.remove(bomber);
	}

	/**
	 * Method which updates the arena's internal state every tick
	 */
	public void update(long gameTime) {
		
		int i,j;
		int sizeX = gc.getSizeX();
		int sizeY = gc.getSizeY();

		for(i=0; i< sizeX; i++){
			for(j=0; j< sizeY; j++){
				//actualiza os objectos que precisam de se actualizar
				//em termos de posicao e frames, e actualiza a matriz
				//de estados que esta em 'gc' importante para as bombas
				arena[i][j].update(gameTime, gc); 
			}
		}
		for(Robot robot: robots)
			robot.update(gameTime, gc); //actualizam-se comparando-se com a matriz
		for(Bomberman player: players)
			player.update(gameTime, gc);
	}
	
	public void draw(Canvas canvas){
		int i,j;
		int sizeX = gc.getSizeX();
		int sizeY = gc.getSizeY();

		//primeiro a arena em si
		for(i=0; i< sizeX; i++){
			for(j=0; j< sizeY; j++){
				arena[i][j].draw(canvas);
			}
		}
		//depois os robots e os players
		for(Robot robot: robots)
			robot.draw(canvas);
		for(Bomberman player: players)
			player.draw(canvas);

	}
}
