import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;

public class Jogos{
    protected int game_id;
    protected String game_name;
    protected LocalDate game_release;
    protected float game_price;
    protected String[] game_genres;
    protected String game_description;

    public Jogos(int id, String name, String release, float price, String[] genres, String description){
        this.game_id = id;
        this.game_name = name;

        //transforma a data de lançamento que vem como String para LocalDate
        String[] split = release.split("[\\-]");
        int ano = Integer.parseInt(split[0]);
        int mes = Integer.parseInt(split[1]);
        int dia = Integer.parseInt(split[2]);
        this.game_release = LocalDate.of(ano, Month.of(mes), dia);

        this.game_price = price;
        this.game_genres = genres.clone(); 
        this.game_description = description;
    }
    public Jogos() {
        this.game_id = -1;
        this.game_name = "";

        int ano = 1900;
        int mes = 1;
        int dia = 1;
        this.game_release = LocalDate.of(ano, Month.of(mes), dia);

        this.game_price = 0.0f;
        this.game_genres = new String[0]; 
        this.game_description = "";
    }

    public byte[] inserirBD() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(this.game_id);
        
        // String fixa (100 caracteres)
        // Preenche com espaços vazios à direita se passar de 100 ao inserir
        String nomeFormatado = String.format("%-100s", this.game_name);
        nomeFormatado = nomeFormatado.substring(0, 100);
        dos.writeUTF(nomeFormatado);
        
        // Grava como um long 
        dos.writeLong(this.game_release.toEpochDay());
        
        dos.writeFloat(this.game_price);
        
        // Junta o array de strings colocando o "-"
        String stringGeneros = String.join("-", this.game_genres);
        dos.writeUTF(stringGeneros);
        
        dos.writeUTF(this.game_description);
        return baos.toByteArray();
        
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        
        this.game_id = dis.readInt();
        
        // Lê a string fixa de 100 caracteres e usa trim() para tirar os espaços extras
        this.game_name = dis.readUTF().trim(); 
        
        // Converte a data do long
        this.game_release = LocalDate.ofEpochDay(dis.readLong());
        
        this.game_price = dis.readFloat();
        
        // Lê a string de gêneros e quebra ela de volta para um array usando o "-"
        String stringGeneros = dis.readUTF();
        this.game_genres = stringGeneros.split("\\-");
        
        this.game_description = dis.readUTF();
    }

    public static Jogos parseJogo(String linha) {
        String[] colunas = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        if (colunas.length < 6) return null;

        try {
            int id = Integer.parseInt(colunas[0].trim());
            String nome = colunas[1].trim();
            String data = colunas[2].trim(); 
            float preco = Float.parseFloat(colunas[3].trim());
            
            // Cuida de retirar os colchetes que vem como padrão
            String generosL = colunas[4].replaceAll("[\\[\\]\"]", ""); 
            // Separa os generos pela ,
            String[] generos = generosL.split("\\s*,\\s*"); 
            
            // --- TRATAMENTO DA DESCRIÇÃO ---
            String descricao = colunas[5].trim();
            // Retirar aspas da descrição
            if (descricao.startsWith("\"") && descricao.endsWith("\"")) {
                descricao = descricao.substring(1, descricao.length() - 1);
            }

            return new Jogos(id, nome, data, preco, generos, descricao);
            
        } catch (Exception e) {
            return null; 
        }
    }

}
