package something.minesweeper;
import java.util.Random;


public class FieldGenerator {
	/*
	 * Generates a random, 9x9 minefield
	 */
	public static int[][] getRandomGrid(){
		int[][] g = new int[9][9];
		
		int mineCount = 1;
		
		Random r = new Random();
		
		while(mineCount<=10){
			int x = r.nextInt(9);
			int y = r.nextInt(9);
			
			if(g[x][y] != 1){
				g[x][y] = 1;
				mineCount++;
			}
		}
		//showGrid(g);
		return g;
	}
	public static void showGrid(int[][] g){
		System.out.println("\n  Current grid: ");
		for(int i=0; i<g.length; i++){
			for(int j=0; j<g[i].length; j++){
				System.out.print(g[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}

}
