import javax.swing.JFrame;

public class App extends JFrame {
    public App() {
        setTitle("Drug Dealer");
        setSize(960, 720);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        add(new Scene());

        setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        new App();
    }
}
