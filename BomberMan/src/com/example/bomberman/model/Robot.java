package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.R;
import com.example.bomberman.model.components.Speed;
import com.example.bomberman.util.GameMatrix;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against
 * the wall and done other terrible things with.
 * Wait till it gets a weapon!
 * 
 * @author impaler
 *
 */
public class Robot extends Bomberman{
	
	public Robot(){
		super();
	}
	
	public Robot (Resources resources, int x, int y, MainGamePanel panel, int i, int j, int myself) {
		super(resources, x, y ,panel, i, j, myself);
	}
	
	@Override
	public void checkAndResolveCollision(char[][] matrix){

		int i,j;
		int width = getWidth();
		int height = getHeight();
		//Log.d("COORDS", "x,y = "+x+","+y+", width,height ="+width+","+height);
		if(speed.getxDirection() == speed.DIRECTION_RIGHT){ 
			i=x/width;
			if(x%width != 0) i++;
			j=y/width;
			//teste de colisao
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (x/width)*width; //divisao inteira!! nao se anulam as operacoes!!
				y = j*height;
				speed.randomDirection();
			}
		}
		else if(speed.getxDirection() == speed.DIRECTION_LEFT){ 
			i=x/width;
			j=y/width;
			//teste de colisao
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i+1)*width; 
				y = j*height;
				speed.randomDirection();
			}
		}
		else if(speed.getyDirection() == speed.DIRECTION_DOWN){ 
			i=x/height;
			j=y/height;
			if(y%height != 0) j++;
			//teste de colisao
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i)*width; 
				y = (y/height)*height;
				speed.randomDirection();
			}
		}
		else if(speed.getyDirection() == speed.DIRECTION_UP){
			i=x/height;
			j=y/height;
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i)*width; 
				y = (j+1)*height;
				speed.randomDirection();
			}
		}			

	}

}
