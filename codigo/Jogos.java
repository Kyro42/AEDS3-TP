import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
}
