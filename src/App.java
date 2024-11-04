import javax.swing.JFrame;

public class App extends JFrame {
    public App() {
        setTitle("Drug Dealer");
        setSize(960, 720);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        add(new Scene());
    }

    public static void main(String[] args) throws Exception {
        new App();
    }
}
