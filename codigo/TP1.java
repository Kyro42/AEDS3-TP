import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.NumberFormatException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TP1 {
    public static void main(String[] args) {
        FileOutputStream arq; //arquivo de saída
        DataOutputStream flux; //fluxo de saida de dados
        FileInputStream arq2; //arquivo de entrada
        DataInputStream flux2; //fluxo de entrada de dados
        try {
            arq = new FileOutputStream("../dataBase/jogos.db");
            flux = new DataOutputStream(arq); //conecta o fluxo de dados com o arquivo

            File file = new File("../dataBase/steam_games.csv");
            if (!file.exists()) {
                System.out.println("CSV não encontrado");
            }
            
            RandomAccessFile dataBase = new RandomAccessFile(file, "r"); //acessa o arquivo csv com modo leitura

        } catch(IOException e){
            System.out.println("Erro: " + e);
        }
    }
}