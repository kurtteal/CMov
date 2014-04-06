package com.example.bomberman.model;

import java.util.ArrayList;
import java.util.List;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.util.GameMatrix;

import android.content.res.Resources;
import android.graphics.Canvas;

public class Arena {

	private IDrawable[][] arena; //Matrix dos objectos para desenhar
	private GameMatrix gm; //matrix com chars, para verificacao de colisoes
	
	//os players/robots tem de ser os ultimos a ser desenhados
	//e o chao tem q ser desenhado por baixo deles, antes de estes serem desenhados
	private List<Bomberman> players;
	private List<Robot> robots;
	
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
						arena[i][j] = new Wall(resources, previousRightBorder, previousBottomBorder, i, j);
						break;
					case '-':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR, i, j);
						break;
					case 'O':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.OBSTACLE, i, j);
						break;
					case 'B':
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.BOMB, i, j);
						break;
					case 'R':
						robots.add(new Robot(resources, previousRightBorder, previousBottomBorder, panel, gm.matrix[i][j]));
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR, i, j);
						break;
					default:
						players.add(new Bomberman(resources, previousRightBorder, previousBottomBorder, panel, gm.matrix[i][j]));
						arena[i][j] = new Path(resources, previousRightBorder, previousBottomBorder, PathState.FLOOR, i, j);
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
				//actualiza os objectos que precisam de se actualizar
				//em termos de posicao e frames, e actualiza a matriz
				//de estados 'gm' importante para as bombas
				arena[i][j].update(gameTime, gm); 
			}
		}
		for(Robot robot: robots)
			robot.update(gameTime, gm); //actualizam-se comparando-se com a matriz
		for(Bomberman player: players)
			player.update(gameTime, gm);
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
