package something.minesweeper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class MinesweeperGUI extends JFrame{
	private static int[][] mineLocations;
	FieldPanel fP;
	InfoPanel iP;
	public MinesweeperGUI(int[][] mineLocs){
		mineLocations = mineLocs;
		
		setTitle("JMinesweeper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		fP = new FieldPanel();
		iP = new InfoPanel();
		
		add(fP);
		add(iP, BorderLayout.SOUTH);
		
		setSize(500,500);
		setVisible(true);
	}
	/*
	 * Provides user with amount of time elapsed, amount of flags left, and a 'new game' button
	 */
	class InfoPanel extends JPanel{
		int buttonXStart;
		int buttonYStart;
		int buttonXLength;
		int buttonYLength;
		int curTime;
		Timer t;
		public InfoPanel(){
			/*
			 * Updates time elapsed every 1000ms (1 second)
			 */
			t = new Timer(1000, new ActionListener(){
				public void actionPerformed(ActionEvent ev){
					curTime++;
					repaint();
				}
			});
			/*
			 * Checks if mouse click was in bounds of the 'new game' button
			 */
			this.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent e) {
					if(SwingUtilities.isLeftMouseButton(e)){
						if(e.getX() >= buttonXStart && e.getX()<=buttonXLength+buttonXStart
								&& e.getY() >= buttonYStart && e.getY()<=buttonYLength+buttonYStart){
							//System.out.println("Starting newgame");
							fP.startNewGame();
							resetPanel();
						}
					}
				}
				public void mousePressed(MouseEvent e) {
				}
				public void mouseReleased(MouseEvent e) {
				}
				public void mouseEntered(MouseEvent e) {
				}
				public void mouseExited(MouseEvent e) {
				}
			
			});
		}
		public void resetPanel(){
			stopTimer();
			repaint();
		}
		public void startTimer(){
			if(!t.isRunning()){
				curTime = 0;
				t.start();
			}
		}
		public void stopTimer(){
			curTime = 0;
			t.stop();
			//repaint();
		}
		public void paint(Graphics g){
			int fieldSize = fP.getFieldSize();
			int width = getWidth();
			int height = getHeight();
			this.setPreferredSize(new Dimension(200, fieldSize));
			
			
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, width, height);
			
			int panelsWidth = width/3;
			int panelsHeight = (int)(height/1.5);
			
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fieldSize/2));
			g.setColor(Color.RED);
			g.drawString("Time: "+Integer.toString(curTime), 0, panelsHeight);
			
			buttonXStart = panelsWidth;
			buttonYStart = (int)(panelsHeight/2);
			buttonXLength = (int)(panelsWidth/1.3);
			buttonYLength = (int)(panelsHeight*1.5);
			
			//System.out.println("BOUNDS:"+buttonXStart+","+buttonYStart+"->"+(buttonXLength+buttonXStart)+","+(buttonYStart+buttonYLength));
			g.drawRect(panelsWidth, buttonYStart, buttonXLength, buttonYLength);
			g.drawString("new game",panelsWidth, panelsHeight);
				
			g.drawString("Flags left: "+fP.getFlagsLeft(), panelsWidth*2, panelsHeight);
			
		}
	}
	/*
	 * Holds the 9x9 square
	 */
	class FieldPanel extends JPanel{
		int size;
		boolean gameOver = false;
		Image imageFlag = new ImageIcon(getClass().getResource("flag.png")).getImage();
		//Image imageFlag = new ImageIcon("flag.png").getImage();
		FlagTypes[][] squareStatus = new FlagTypes[9][9];
		int[][] surroundingMines = new int[9][9]; //Amount of mines around uncovered square
		int[] losingSquare;
		int flagsLeft = 10;
		boolean wasFirstMove = true;
		int squaresRevealed = 0;
		public void startNewGame(){
			gameOver = false;
			wasFirstMove = true; //Allows timer to be started
			//Makes sure all squares are covered up/set to default value
			for(int i=0; i<squareStatus.length; i++){
				for(int j=0; j<squareStatus[i].length; j++){
					squareStatus[i][j] = null;
				}
			}
			mineLocations = FieldGenerator.getRandomGrid(); //Gets a new mine grid
			surroundingMines = surroundingMines(mineLocations); //Calculates amount of mines around each square
			flagsLeft = 10;
			squaresRevealed = 0;
			iP.resetPanel();
			repaint();
		}
		public FieldPanel(){
			surroundingMines = surroundingMines(mineLocations);
			this.addMouseListener(new MouseListener(){
				public void mousePressed(MouseEvent e) {
					if(!gameOver){ //Makes sure game is not over
						int x = e.getY()/size; //x and y coordinates of mouse clicks
						int y = e.getX()/size;
						try{
							if(squareStatus[x][y] == FlagTypes.CLICKED){
								//do nothing
							}else if(squareStatus[x][y] == FlagTypes.FLAGGED){ //Square is flagged
								if(SwingUtilities.isRightMouseButton(e)){ //Remove flag
									//System.out.println("Removing flag @ "+x+","+y);
									flagsLeft++;
									squareStatus[x][y] = null;
									iP.repaint();
								}
							}else if(squareStatus[x][y] == null){ //Square is untouched
								if(SwingUtilities.isLeftMouseButton(e)){ //Uncover square
									//System.out.println("\nClicking "+x+","+y);
									squareStatus[x][y] = FlagTypes.CLICKED;
									squaresRevealed++;
									//	surroundingMines[x][y] = getSurroundingMines(x,y);
									if(surroundingMines[x][y]==0){ //Exposes surrounding mines								
										digMoreMines(x,y);
									}
									if(mineLocations[x][y] == 1){ //Mine was hit
									//	System.out.println("Hit mine.");
										gameOver = true;
										losingSquare = new int[]{x,y};
									}
									
								}else if(SwingUtilities.isRightMouseButton(e)){ //Adds flag
									//System.out.println("Adding flag @ "+x+","+y);
									if(flagsLeft <= 0)
										return;
									squareStatus[x][y] = FlagTypes.FLAGGED;
									flagsLeft--;
									iP.repaint();
								}
							}
						}catch(ArrayIndexOutOfBoundsException ex){
							/*
							 * Just so unnecessary errors are not thrown when
							 * the player clicks outside of the grid
							 */
						}
						repaint();
					}
					if(wasFirstMove)
						iP.startTimer();
					wasFirstMove = false; //So the timer does not restart
					
					checkIfWon();
				}
				public void mouseClicked(MouseEvent e) {
				}
				public void mouseReleased(MouseEvent e) {
				}
				public void mouseEntered(MouseEvent e) {
				}
				public void mouseExited(MouseEvent e) {
				}
			
			});
		}
		public void checkIfWon(){
			//System.out.println(squaresRevealed+"<-");
			int choice = 1337;
			String[] opts = {"Play Again", "Exit"};
			if(gameOver){
				iP.stopTimer();
				choice = JOptionPane.showOptionDialog(this, "You lost!", "Game over", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
			}else if(squaresRevealed>=71){
				int timeElapsed = iP.curTime;
				iP.stopTimer();
				choice = JOptionPane.showOptionDialog(this, "Congratulations, you won in "+timeElapsed+" seconds!", "Game complete", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
			}
			if(choice==1337){ //Default option, nothing changed
				
			}else if(choice==1){ //Player wants to exit game
				System.exit(0);
			}else{
				startNewGame(); //Player wants to play again
			}
		}
		/*
		 * Finds the amount of surrounding mines for each square on the minefield/grid
		 */
		public int[][] surroundingMines(int[][] grid){	
			int[][] minesAround = new int[9][9];
			
			for(int i=0; i<grid.length; i++){
				for(int j=0; j<grid[i].length; j++){
					if(mineLocations[i][j] == 0)
						minesAround[i][j] = getSurroundingMines(i, j);
					else
						minesAround[i][j] = 9;	
				}
			}
			return minesAround;
		}
		/*
		 * Recursive algorithm to expose empty mines
		 */
		private void digMoreMines(int x, int y){
			int curX = 0, curY = 0;
			for(int r = -1; r <= 1; r++){
				curX = x+r;
				for(int c = -1; c <= 1; c++){
					curY = y+c;
					if(inBounds(curX,curY) && squareStatus[curX][curY] != FlagTypes.FLAGGED){
						int surMines = getSurroundingMines(curX,curY);
						if(squareStatus[curX][curY] != FlagTypes.CLICKED){
							squareStatus[curX][curY] = FlagTypes.CLICKED;
							squaresRevealed++;
							//System.out.println("opening @ "+curX+","+curY+"("+surMines+")");
						if(surMines==0)
							digMoreMines(curX,curY);
						}
					}
				}
			}
		}
		public boolean inBounds(int x, int y){
			if(x>=0 && x<9 && y>=0 && y<9)
				return true;
			return false;
		}
		public int getFlagsLeft(){
			//System.out.println("Sending "+flagsLeft);
			return flagsLeft;
		}
		/*
		 * Returns the amount of surrounding mines around a specific square
		 */
		public int getSurroundingMines(int x, int y){
			int surrMineCount = 0;
			int curX, curY;
			for(int r = -1; r <= 1; r++){
				curX = x+r;
					for(int c = -1; c <= 1; c++){
						curY = y+c;
						if(y+c < 0 || y+c >= 9 || x+r < 0 || x+r >= 9)
							continue;
						if(mineLocations[x+r][y+c]==1)
							surrMineCount++;
					}
				}
			return surrMineCount;
		}
		/*
		 * Used to find the best size for grid and other components to fit in the window
		 */
		public int getFieldSize(){
			return this.getWidth()>this.getHeight() ? this.getHeight()/9 : this.getWidth()/9;
		}
		public void paint(Graphics g){
			size = getFieldSize();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			for(int y=0; y<9; y++){
				for(int x=0; x<9; x++){
					if(squareStatus[y][x] == null){
						g.setColor(Color.BLUE);
						g.fillRect(x*size, y*size, size-1, size-1);
					}else if(squareStatus[y][x] == FlagTypes.CLICKED){
						g.setColor(Color.LIGHT_GRAY);
						g.fillRect(x*size, y*size, size-1, size-1);
					}else if(squareStatus[y][x] == FlagTypes.FLAGGED){
						g.setColor(Color.BLUE);
						g.fillRect(x*size, y*size, size-1, size-1);
						g.drawImage(imageFlag, x*size, y*size, size, size, null);
					}
					
					/*g.setFont(new Font("Dialog", Font.BOLD, 18));
					g.setColor(Color.RED);
					g.drawString(y+","+x, x*size, (y+1)*size);
					debugging stuff, shows each coordinate on each sq ^*/
					
					String surrMines = Integer.toString(surroundingMines[y][x]);
					
					
					if(!surrMines.equals("0") && squareStatus[y][x] == FlagTypes.CLICKED){
						g.setColor(Color.BLACK);
						g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, size));
						g.drawString(surrMines+"", x*size+(size/4), y*size+(size));
					}//else{
						//start digging for more empties..
						//go up, down, left, right, and click, probably recursive?
					//}
				}
			}
			if(gameOver){
				iP.stopTimer();
				wasFirstMove = true;
				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
				for(int i=0; i<mineLocations.length; i++){
					for(int j=0; j<mineLocations[i].length; j++){
						if(mineLocations[j][i] == 1){
							
								//g.drawImage(imageFlag, i*size, j*size, size, size, null);
							g.setColor(Color.RED);
							g.fillRect(i*size, j*size, size-1, size-1);
							if(squareStatus[j][i]==FlagTypes.FLAGGED)
								g.setColor(Color.BLUE);
							else
								g.setColor(Color.BLACK);
							g.drawString("*", i*size+(size/3), j*size+size);
							if(losingSquare[0] == j && losingSquare[1] == i){
								g.setColor(Color.ORANGE);
								g.drawString("!",i*size+(size/3),j*size+size);
							}
						}
						
						
					}
				};
			}
		}
		
	}
	enum FlagTypes{
			FLAGGED, CLICKED;
	}
}
