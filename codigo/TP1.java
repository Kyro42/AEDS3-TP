import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.NumberFormatException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class TP1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        criaMenu();
        int opt = sc.nextInt();
        while (opt != 0) {
            switch (opt) {
                case 1:
                    leitorCSV();
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Numero invalido!");
            }
            criaMenu();
            opt = sc.nextInt();
        }
        sc.close();
    }

    public static void criaMenu() {
        String titulo = "\n-----------Steam Games DB----------";
        String barra = "-----------------------------------\n";
        String opcoes = String.format("\n%s\n%s\n%s\n%s\n%s", "[1] Carregar base de dados", "[2] Ler registro",
                "[3] Atualizar registro", "[4] Deletar registro", "[0] Sair");

        System.out.println(titulo);
        System.out.println("Selecione:");
        System.out.println(opcoes);
        System.out.println(barra);
    }

    public static void leitorCSV() {
        Path caminhoCSV = Paths.get("../dataBase/steam_games.csv");
        String caminhoBinario = "../database/jogos.db";
        int ultimoId = 0;

        try (BufferedReader leitor = Files.newBufferedReader(caminhoCSV)) {
            RandomAccessFile arq = new RandomAccessFile(caminhoBinario, "rw");
            String linha;
            leitor.readLine();
            while ((linha = leitor.readLine()) != null) {
                String[] valores = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); //valores[4] = generos
                if (valores.length == 6) {

                    int id = Integer.parseInt(valores[0]);
                    String nome = valores[1];
                    String lancamento = valores[2];
                    float preco = 0.0f;
                    if(!valores[3].trim().isEmpty()){
                        try{
                            preco = Float.parseFloat(valores[3]);
                        } catch(Exception e){

                        }
                    }

                    String generosRaw = valores[4]; // os generos do jogo estão assim: "[""genero1"",""genero2""]". Quero deixa-los assim: genero1, genero2.

                    String generos = generosRaw.replace("[", "").replace("]", "").replace("\"", "");
                    valores[4] = generos;

                    String descricao = valores[5];

                    if (id > ultimoId) {
                        ultimoId = id;
                    }

                    Jogo temp = new Jogo(id, nome, lancamento, preco, generos, descricao);
                    byte[] ba;

                    try {
                        ba = temp.toByteArray();
                        arq.writeByte(0); // Byte da lápide: 0 = valido, 1 = excluido
                        arq.writeInt(ba.length);
                        arq.write(ba);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
            arq.seek(0);
            arq.writeInt(ultimoId);
            System.out.println("\nBase de dados carregada com sucesso! Ultimo ID: " + ultimoId);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}