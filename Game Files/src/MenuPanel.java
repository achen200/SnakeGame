package src;
import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    private static final long serialVersionUID = -2903913975540482354L;
    public MenuPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setSize(MenuLabel.DEFAULT_DIMENSION);
        setPreferredSize(MenuLabel.DEFAULT_DIMENSION);
        setVisible(true);
        setFocusable(true);
    }

    public MenuPanel(LayoutManager layout){
        super();
        setLayout(layout);
        setSize(MenuLabel.DEFAULT_DIMENSION);
        setPreferredSize(MenuLabel.DEFAULT_DIMENSION);
        setVisible(true);
        setFocusable(true);
        
    }
}