package vistweet.graphics;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class PINFrame extends JFrame {

    private JLabel     label;
    private JTextField pinField;
    private JButton    button;
    
    public PINFrame() {
        super("Authrization");
        
        setSize(new Dimension(220, 80));
        setResizable(false);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        
        label = new JLabel("Authorize and input PIN code.");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBounds(10, 0, 200, 30);
        add(label);
        
        pinField = new JTextField();
        pinField.setBounds(20, 30, 130, 20);
        add(pinField);
        
        button = new JButton("Go");
        button.setBounds(160, 30, 40, 20);
        add(button);
    }
    
    public final String getPIN() {
        return pinField.getText();
    }
    
    public final void addActionListenerToButton(ActionListener listener) {
        button.addActionListener(listener);
    }
}
