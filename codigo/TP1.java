import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.NumberFormatException;

public class TP1 {
    public static void main(String[] args) {
        try{
            File file = new File("../dataBase/steam_games.csv");
            if(!file.exists()){
                System.out.println("CSV não encontrado");
            }
            RandomAccessFile dataBase = new RandomAccessFile(file, "r");
        } catch(IOException e){
            System.out.println("Erro: " + e);
        }
    }
}