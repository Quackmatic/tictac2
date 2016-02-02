import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class JButtonGrid extends JPanel {
    private int width, height;
    private JButton[][] buttons;

    public JButtonGrid(int width, int height) {
        super(new GridLayout(height, width));
        this.width = width;
        this.height = height;
        this.buttons = new JButton[width][height];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                add(buttons[x][y] = new JButton());
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                buttons[x][y].setEnabled(enabled);
            }
        }
    }

    public JButton getButton(int x, int y) {
        if(x >= 0 && x < width &&
           y >= 0 && y < height) {
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
