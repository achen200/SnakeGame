package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuButton extends JButton {
    private static final long serialVersionUID = 4475609219852802839L;
    public static final Color darkGreen = new Color(0, 77, 0);
       
    private MouseAdapter buttonHover = new MouseAdapter(){
        public void mouseEntered(MouseEvent evt) {
            if(evt.getSource() instanceof JButton){
                ((JButton)evt.getSource()).setBorder(BorderFactory.createLineBorder(Color.WHITE));
                ((JButton)evt.getSource()).setForeground(Color.WHITE);   
            }
        }
        public void mouseExited(MouseEvent evt) {
            if(evt.getSource() instanceof JButton){
                ((JButton)evt.getSource()).setBorder(BorderFactory.createLineBorder(darkGreen));
                ((JButton)evt.getSource()).setForeground(darkGreen);
            }
        }
    };

    public MenuButton(String name, ActionListener al, String actionCommand){
        super(name);

        setAlignmentX(Component.CENTER_ALIGNMENT);
        setMinimumSize(new Dimension(280, 50));
        setMaximumSize(new Dimension(280, 50));
        setForeground(darkGreen);
        setBorder(BorderFactory.createLineBorder(darkGreen));
        setContentAreaFilled(false);
        setFont(GameCanvas.scoreFont);

        addMouseListener(buttonHover);
        addActionListener(al);
        setActionCommand(actionCommand);
    }
}