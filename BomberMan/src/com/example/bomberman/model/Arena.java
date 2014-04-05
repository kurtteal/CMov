package com.example.bomberman.model;

import java.util.ArrayList;
import java.util.List;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.util.GameMatrix;

import android.content.res.Resources;
import android.graphics.Canvas;

public class Arena {

	private IDrawable[][] arena;
	
	//os players/robots tem de ser os ultimos a ser desenhado
	//e o chao tem q ser desenhado por baixo deles, antes de estes serem desenhados
	private List<Bomberman> players;
	private List<Robot> robots;
	private GameMatrix gm;
	
	public Arena (Resources resources, GameMatrix gameMatrix, MainGamePanel panel) {
		players = new ArrayList<Bomberman>();
		robots = new ArrayList<Robot>();
		gm = gameMatrix;
		int i,j;
		int sizeX = gm.getSizeX();
		int sizeY = gm.getSizeY();
		arena = new IDrawable[sizeX][sizeY];

		for(i=0; i< sizeX; i++){
			int previousBottomBorder = 0;
			if(i!=0)
				previousBottomBorder = arena[i-1][0].getDownBorder();	
			for(j=0; j< sizeY; j++){
				int previousRightBorder = 0;
				if(j != 0)
					previousRightBorder = arena[i][j-1].getRightBorder();
				switch(gm.matrix[i][j]){
					case 'W':
						arena[i][j] = new Wall(resources, previousRightBorder, previousBottomBorder);
						break;
					case '-':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR);
						break;
					case 'O':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.OBSTACLE);
						break;
					case 'B':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.BOMB);
						break;
					case 'R':
						robots.add(new Robot(resources, previousRightBorder, previousBottomBorder, panel, i, j));
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR);
						break;
					default:
						players.add(new Bomberman(resources, previousRightBorder, previousBottomBorder, panel, i, j));
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR);
						break;
				}
			}
			System.out.println("");
		}
		
	}

	/**
	 * Method which updates the arena's internal state every tick
	 */
	public void update(long gameTime) {
		
		int i,j;
		int sizeX = gm.getSizeX();
		int sizeY = gm.getSizeY();

		for(i=0; i< sizeX; i++){
			for(j=0; j< sizeY; j++){
				arena[i][j].update(gameTime);
			}
		}
		for(Robot robot: robots)
			robot.update(gameTime);
		for(Bomberman player: players)
			player.update(gameTime);
	}
	
	public void draw(Canvas canvas){
		int i,j;
		int sizeX = gm.getSizeX();
		int sizeY = gm.getSizeY();

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
