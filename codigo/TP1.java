import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.NumberFormatException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.RandomAccessFile;

class Arquivo {
    private RandomAccessFile arquivo;

    public Arquivo(String arq) throws Exception {
        this.arquivo = new RandomAccessFile(arq, "rw");
        
        // Na primeira vez , grava o ID 0 no cabeçalho
        if (this.arquivo.length() == 0) {
            this.arquivo.writeInt(0);
        }
    }

    public int inserir(Jogos jogo) throws Exception {
        // Lê o cabeçalho para saber o último ID
        this.arquivo.seek(0);
        int ultimoId = this.arquivo.readInt();
        
        // Vai para o final do arquivo para adicionar o novo registro
        this.arquivo.seek(this.arquivo.length());
        
        // Chama o método que transforma o objeto em bytes
        byte[] ba = jogo.inserirBD(); 
        
        // lapide, tamanho e dados
        this.arquivo.writeBoolean(false); // lapide
        this.arquivo.writeInt(ba.length); // tamanho do registro
        this.arquivo.write(ba);           // inserção de bytes
        
        // Atualiza o cabeçalho 
        if (jogo.game_id > ultimoId) {
            this.arquivo.seek(0);
            this.arquivo.writeInt(jogo.game_id);
        }
        
        return jogo.game_id;
    }
}

class CarregaDadosCsv {
    
    // Recebe o CSV e trasfere pro arquivo binario
    public void lerCsv(String path, Arquivo db) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            
            String linha = br.readLine(); // Pula a primeira linha 
            int cont = 0;

            while ((linha = br.readLine()) != null) {
                // Transforma os dados do csv num objeto Jogo pra poder inserir
                Jogos jogo = Jogos.parseJogo(linha);
                    if (jogo != null) {
                    db.inserir(jogo);
                    cont++;
                }
                    cont++;
                
            }
            
            System.out.println("Total de jogos: " + cont);

        } catch (Exception e) {
             e.printStackTrace();
            System.err.println("Erro: " + e.getMessage());
        }
    }
}


public class TP1 {

    
    public static void main(String[] args) {
        
       try {
            Arquivo db = new Arquivo("dataBase/jogos.db");
           // CarregaDadosCsv carga = new CarregaDadosCsv();
           // carga.lerCsv("dataBase/steam_games.csv", db);

           
            
        } catch (Exception e) {
            e.printStackTrace();
             System.err.println("Erro: " + e.getMessage());
        }
    }
}