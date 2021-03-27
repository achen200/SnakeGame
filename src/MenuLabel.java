package src;

import java.awt.*;
import javax.swing.*;

public class MenuLabel extends JLabel {
    private static final long serialVersionUID = -4641561189183837828L;
    public static final Dimension DEFAULT_DIMENSION 
        = new Dimension(GameCanvas.DEFAULT_WIDTH, GameCanvas.DEFAULT_HEIGHT);

    public MenuLabel(ImageIcon icon){
        super(icon);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setPreferredSize(DEFAULT_DIMENSION);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public MenuLabel(String title, Font font, Color fg){
        super(title, SwingConstants.CENTER);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setFont(font);
        setForeground(fg);
    }

}