import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridLayout;

/**
 * Represents a grid of {@code javax.swing.JButton}s.
 */
public class JButtonGrid extends JPanel {
    private int columns, rows;
    private JButton[][] buttons;

    /**
     * Initialize a new JButtonGrid with the given column and row count.
     *
     * @param columns The number of columns in this grid.
     * @param rows The number of rows in this grid.
     */
    public JButtonGrid(int columns, int rows) {
        super(new GridLayout(rows, columns));
        this.columns = columns;
        this.rows = rows;
        this.buttons = new JButton[columns][rows];

        for(int y = 0; y < rows; y++) {
            for(int x = 0; x < columns; x++) {
                add(buttons[x][y] = new JButton());
            }
        }
    }

    /**
     * Get the number of columns in this button grid.
     *
     * @return The number of columns in this button grid.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Get the number of rows in this button grid.
     *
     * @return The number of rows in this button grid.
     */
    public int getRows() {
        return rows;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for(int y = 0; y < rows; y++) {
            for(int x = 0; x < columns; x++) {
                buttons[x][y].setEnabled(enabled);
            }
        }
    }

    /**
     * Get the button in this grid at the given X and Y (column and row).
     *
     * @param x The column of the button to get.
     * @param y The row of the button to get.
     */
    public JButton getButton(int x, int y) {
        if(x >= 0 && x < columns &&
           y >= 0 && y < rows) {
            return buttons[x][y];
        } else {
            throw new IllegalArgumentException(
                    String.format(
                        "The given co-ordinate (%d, %d) is not within the " +
                        "boundaries of the button grid.",
                        x, y
                        )
                    );
        }
    }
}
