import java.util.Objects;
import java.util.Scanner;
import java.sql.*;


public class Lab4 {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        GameControl gc;
        do {
            gc = new GameControl();
            gc.GameRun();

            System.out.println("Создать новую игру? (+/-)");
        } while (scanner.next().equals("+"));
    }
}

class GameControl{
    private final char x = 'x';
    private final char o = 'o';
    private boolean stop;
    private int cord;
    private int step_count = 0;

    private Scanner scanner;
    private Play_Field pf;
    private Player player1;
    private Player player2;

    public GameControl(){
        scanner = new Scanner(System.in);
        pf = new Play_Field();

        player1 = new Player(pf, x);
        System.out.println("Вход в аккаунт первого игрока");
        SignIn(player1);

        player2 = new Player(pf, o);
        System.out.println("Вход в аккаунт второго игрока");
        SignIn(player2);
    }

    public void GameRun(){
        do{
            pf.Restart();
            stop = false;
            GameControl.Rule();
            do{
                System.out.print("Ход "+player1.name+" (введите число от 1 до 9 включительно): ");

                do {
                    cord = scanner.nextInt();
                    while (!(cord < 10 && cord > 0)) {
                        System.out.print("Введено некорректное число, попробуйте еще раз: ");
                        cord = scanner.nextInt();
                    }
                } while (!(player1.Step(cord-1)));
                step_count++;
                pf.Include_data();

                if(pf.Winner(player1.shape)){
                    pf.Include_data();
                    player1.Win();
                    player2.Lose();
                    stop = true;
                }
                else if(step_count == 9) {
                    System.out.println("\nНичья");
                    stop = true;
                }
                else {
                    System.out.print("Ход " + player2.name + " (введите число от 1 до 9 включительно): ");

                    do {
                        cord = scanner.nextInt();
                        while (!(cord < 10 && cord > 0)) {
                            System.out.print("Введено некорректное число, попробуйте еще раз: ");
                            cord = scanner.nextInt();
                        }
                    } while (!(player2.Step(cord - 1)));
                    step_count++;
                    pf.Include_data();

                    if (pf.Winner(player2.shape)) {
                        pf.Include_data();
                        player2.Win();
                        player1.Lose();
                        stop = true;
                    }
                }
            }while (!stop);
            System.out.println("Сыграть еще? (+/-)");
        } while(scanner.next().equals("+"));
    }

    private void SignIn(Player pl){
        String buf_name, buf_pass;
        do {
            System.out.print("Введите логин: ");
            buf_name = scanner.next();
            System.out.print("Введите пароль: ");
            buf_pass = scanner.next();
        } while (!pl.AccVerifi(buf_name, buf_pass));
    }

    public static void Rule(){
        System.out.println("\nЧтоб указать в какую ячейку сетки сделать свой ход - ориентируйтесь по данной таблице, где цифра соответствует координате ячейки");

        int count = 1;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 5; j++){
                if(j == 1 || j == 3) {
                    System.out.print('|');
                }
                else{
                    System.out.print(count++);
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}

class Player {
    private Play_Field pf;
    private int count_W = 0;
    private int count_L = 0;
    Db data_base = new Db();
    String name;
    String pass;
    char shape;

    public Player(Play_Field pf, char shp){
        this.pf = pf;
        shape = shp;
    }

    public boolean AccVerifi(String name, String pass){
        if(data_base.isUserExists(name))
        {
            System.out.println("Login \"" + name + "\" is exists");
            if(data_base.isUserPassword(name, pass))
            {
                System.out.println("You are logged in\n");
                this.name = name;
                this.pass = pass;
                return true;
            }
            else
            {
                System.out.println("Wrong password, try again");
                return false;
            }
        }
        else
        {
            System.out.println("Login \"" + name + "\" doesn't exists, try again");
            return false;
        }
    }

    public boolean Step(int cord){
        if(pf.place_data[cord] == ' '){
            pf.place_data[cord] = shape;
            return true;
        }
        else{
            System.out.print("Ячейка уже занята, выберите другую :");
            return false;
        }
    }

    public void Win(){
        count_W++;
        System.out.println("\n" + name + " - победил");
        System.out.println("Побед: " + count_W + "    поражений: " + count_L);
    }
    public void Lose(){
        count_L++;
        System.out.println("\n" + name + " - проиграл");
        System.out.println("Побед: " + count_W + "    поражений: " + count_L);
    }
}

class Db {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "200213mmm";
    private static final String DbURL = "jdbc:mysql://localhost:3306/myGame?useSSL=false";
    private static Connection connection;
    private static Statement statement;

    public Db() {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DbURL, USER_NAME, PASSWORD);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean isUserExists(String username) {
        try
        {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM users WHERE username = '" + username + "';");

            while(rs.next()) {
                if (rs.getInt(1) == 1)
                    return true;
                else
                    return false;
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return false;
    }

    public boolean isUserPassword(String username, String password){
        try
        {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT password FROM users WHERE username = '" + username + "';");

            while(rs.next()) {
                if (Objects.equals(rs.getString(1),password))
                    return true;
                else
                    return false;
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return false;
    }

    public void Close() {
        try{
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

class Play_Field {
    char[] place_data = {' ', ' ',' ',' ',' ',' ',' ',' ',' ',};
    char[][] canvas = new char[3][5];


    public void Include_data(){
        int count = 0;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 5; j++){
                if(j == 1 || j == 3) {
                    canvas[i][j] = '|';
                }
                else{
                    canvas[i][j] = place_data[count++];
                }
            }
        }
        Print();
    }

    public boolean Winner(char ch){
        if((place_data[0]==ch)&&(place_data[1]==ch)&&(place_data[2]==ch)){
            if (ch == 'x')
                place_data[0] = place_data[1] = place_data[2] = '☒';
            if (ch == 'o')
                place_data[0] = place_data[1] = place_data[2] = '☐';
            return true;
        }
        else if((place_data[3]==ch)&&(place_data[4]==ch)&&(place_data[5]==ch)){
            if (ch == 'x')
                place_data[3] = place_data[4] = place_data[5] = '☒';
            if (ch == 'o')
                place_data[3] = place_data[4] = place_data[5] = '☐';
            return true;
        }
        else if((place_data[6]==ch)&&(place_data[7]==ch)&&(place_data[8]==ch)){
            if (ch == 'x')
                place_data[6] = place_data[7] = place_data[8] = '☒';
            if (ch == 'o')
                place_data[6] = place_data[7] = place_data[8] = '☐';
            return true;
        }
        else if((place_data[0]==ch)&&(place_data[3]==ch)&&(place_data[6]==ch)){
            if (ch == 'x')
                place_data[0] = place_data[3] = place_data[6] = '☒';
            if (ch == 'o')
                place_data[0] = place_data[3] = place_data[6] = '☐';
            return true;
        }
        else if((place_data[1]==ch)&&(place_data[4]==ch)&&(place_data[7]==ch)) {
            if (ch == 'x')
                place_data[1] = place_data[4] = place_data[7] = '☒';
            if (ch == 'o')
                place_data[1] = place_data[4] = place_data[7] = '☐';
            return true;
        }
        else if((place_data[2]==ch)&&(place_data[5]==ch)&&(place_data[8]==ch)){
            if (ch == 'x')
                place_data[2] = place_data[5] = place_data[8] = '☒';
            if (ch == 'o')
                place_data[2] = place_data[5] = place_data[8] = '☐';
            return true;
        }
        else if((place_data[0]==ch)&&(place_data[4]==ch)&&(place_data[8]==ch)){
            if (ch == 'x')
                place_data[0] = place_data[4] = place_data[8] = '☒';
            if (ch == 'o')
                place_data[0] = place_data[4] = place_data[8] = '☐';
            return true;
        }
        else if((place_data[2]==ch)&&(place_data[4]==ch)&&(place_data[6]==ch)){
            if (ch == 'x')
                place_data[2] = place_data[4] = place_data[6] = '☒';
            if (ch == 'o')
                place_data[2] = place_data[4] = place_data[6] = '☐';
            return true;
        }
        else {
            return false;
        }
    }

    public void Restart(){
        canvas = new char[3][5];
        for (int i = 0;i < place_data.length; i++)
            place_data[i] = ' ';
    }

    public void Print(){
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 5; j++){
                System.out.print(canvas[i][j]);
            }
            System.out.println();
        }
    }
}

