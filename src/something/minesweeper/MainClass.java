package something.minesweeper;

public class MainClass {

	public static void main(String[] args) {
		new MinesweeperGUI(FieldGenerator.getRandomGrid());
	}

}
