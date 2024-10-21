import javax.swing.JFrame;

public class App extends JFrame {
    final int windowWidth = 960,
              windowHeight = 720;
    
    public App() {
        setTitle("Drug Dealer");
        setSize(windowWidth, windowHeight);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        add(new Scene());
    }

    public static void main(String[] args) throws Exception {
        new App();
    }
}
