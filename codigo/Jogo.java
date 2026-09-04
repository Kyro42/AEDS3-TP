import java.text.DecimalFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;

public class Jogo{
    protected int game_id;
    protected String game_name;
    protected LocalDate game_release;
    protected float game_price;
    protected String[] game_genres;
    protected String game_description;

    //construtores
    public Jogo(int id, String name, String release, float price, String genres, String description) {
        this.game_id = id;
        this.game_name = name;

        //transforma a data de lançamento que vem como String para LocalDate
        //se na data de lançamento tiver algo como "Coming soon" ou vier incompleta, define a data como 01/01/1970
        this.game_release = LocalDate.of(1970, 1, 1);
        
        if (release != null && !release.trim().isEmpty()) {
            
            //caso a data esteja completa, faz a atribuição normalmente
            try{
                String[] split = release.split("[\\-]");
                int ano = Integer.parseInt(split[0]);
                int mes = Integer.parseInt(split[1]);
                int dia = Integer.parseInt(split[2]);
                this.game_release = LocalDate.of(ano, Month.of(mes), dia);
            }catch(Exception e){

            }
        }

        this.game_price = price;
        String[] generos = genres.split(", ");
        this.game_genres = generos.clone();
        this.game_description = description;
    }

    public Jogo() {
        this.game_id = -1;
        this.game_name = "";

        int ano = 1970;
        int mes = 1;
        int dia = 1;
        this.game_release = LocalDate.of(ano, Month.of(mes), dia);

        this.game_price = 0;
        this.game_genres = new String[] { "" };
        this.game_description = "";
    }

    public void setID(int id){
        this.game_id = id;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dados = new DataOutputStream(baos);
        
        dados.writeInt(this.game_id);
        
        // string fixa (100 caracteres)
        // preenche com espaços vazios à direita e passar de 100 para de inserir
        String nome = String.format("%-100s", this.game_name);
        nome = nome.substring(0, 100);
        dados.writeUTF(nome);
        
        // grava como um long 
        dados.writeLong(this.game_release.toEpochDay());
        
        dados.writeFloat(this.game_price);
        
        // junta o array de strings colocando o "-"
        String generos = String.join("-", this.game_genres);
        dados.writeUTF(generos);
        
        dados.writeUTF(this.game_description);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.game_id = dis.readInt();

        // lê a string fixa de 100 caracteres e usa trim() para tirar os espaços extras
        this.game_name = dis.readUTF().trim();

        // converte a data do long
        this.game_release = LocalDate.ofEpochDay(dis.readLong());

        this.game_price = dis.readFloat();

        // lê a string de gêneros e quebra ela de volta para um array usando o "-"
        String stringGeneros = dis.readUTF();
        this.game_genres = stringGeneros.split("\\-");

        this.game_description = dis.readUTF();
    }

    public String toString() {
        DecimalFormat preco = new DecimalFormat("#,##0.00");
        String result = "\nID: " + game_id + "\nNome: " + game_name + "\nData de lançamento: " + game_release
                + "\nPreço: US$ " + preco.format(game_price) + "\nGeneros: " + String.join(", ", game_genres) + "\nDescrição: " + game_description;
        
        return result;
    }
}
