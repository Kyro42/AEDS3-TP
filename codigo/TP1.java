import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class TP1 {
    private static Path caminhoCSV = Paths.get("dataBase/steam_games.csv");
    private static String caminhoBinario = "database/jogos.db";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        criaMenu();
        int opt = sc.nextInt();
        while (opt != 0) {
            int id;
            long retorno;
            switch (opt) {
                case 1:
                    leitorCSV();
                    break;
                case 2:
                    insereRegistro();
                    break;
                case 3:
                    System.out.print("\nDigite o ID que deseja buscar: ");
                    id = sc.nextInt();
                    System.out.println();
                    retorno = buscador(id);
                    if(retorno == -1){
                        System.out.println("Registro não encontrado :(");
                    } else{
                        lerRegistro(retorno);
                    }
                    break;
                case 4:
                    System.out.print("\nDigite o ID que deseja atualizar: ");
                    id = sc.nextInt();
                    System.out.println();
                    retorno = buscador(id);
                    if(retorno == -1){
                        System.out.println("Registro não encontrado :(");
                    } else{
                        atualizaRegistro(retorno);
                    }
                    break;
                case 5:
                    System.out.print("\nDigite o ID que deseja deletar: ");
                    id = sc.nextInt();
                    System.out.println();
                    retorno = buscador(id);
                    if(retorno == -1){
                        System.out.println("Registro não encontrado! :(");
                    } else{
                        removerRegistro(retorno);
                    }
                    break;
                case 6:
                    try{
                        chamaOrdenacao();
                    }
                    catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                    break;
                case 7:
                    imprimirTop10(caminhoBinario);
                    break;
                case 8:
                    imprimirTop10("dataBase/jogos_ordenado.db");
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
        String opcoes = String.format("\n%s\n%s\n%s\n%s\n%s\n%s\n%s", "[1] Carregar base de dados", "[2] Inserir registro" , "[3] Ler registro",
                "[4] Atualizar registro", "[5] Deletar registro","[6] Ordenar registros" , "[0] Sair");

        System.out.println(titulo);
        System.out.println("Selecione:");
        System.out.println(opcoes);
        System.out.println(barra);
    }

    public static void leitorCSV() {

        int ultimoId = 0;

        try (BufferedReader leitor = Files.newBufferedReader(caminhoCSV)) {
            RandomAccessFile arq = new RandomAccessFile(caminhoBinario, "rw");
            arq.writeInt(0);
            String linha;
            leitor.readLine();
            while ((linha = leitor.readLine()) != null) {
                String[] valores = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); //valores[4] = generos
                if (valores.length == 6) {

                    int id = Integer.parseInt(valores[0]);
                    String nome = valores[1];
                    String lancamento = valores[2];
                    float preco = 0.0f;
                    if (!valores[3].trim().isEmpty()) {
                        try {
                            preco = Float.parseFloat(valores[3]);
                        } catch (Exception e) {

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
            arq.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void insereRegistro() {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile(caminhoBinario, "rw");
            arq.seek(0);
            int ultimoId = arq.readInt();
            arq.seek(arq.length());
            Jogo jogo = new Jogo();
            jogo.setID(++ultimoId);
            jogo = solicitaDados(jogo);
            byte[] ba = jogo.toByteArray();
            int tam = ba.length;
            arq.writeByte(0); //lápide. 0 = registro valido, 1 = registro excluido
            arq.writeInt(tam);
            arq.write(ba);
            arq.seek(0);
            arq.writeInt(ultimoId);
            System.out.println("Jogo inserido com sucesso! id: " + ultimoId);
            arq.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static long buscador(int id) {
        RandomAccessFile arq;
        int tam;
        try {
            arq = new RandomAccessFile(caminhoBinario, "r");
            arq.seek(0);
            int ultimoId = arq.readInt();
            long pos = arq.getFilePointer();
            if (id > ultimoId) {
                //System.out.println("ID não encontrado. :(");
                arq.close();
                return -1;
            }

            while ((pos = arq.getFilePointer()) < arq.length()) {
                byte lapide = arq.readByte();
                tam = arq.readInt();
                if (lapide == 0) {
                    byte[] ba = new byte[tam];
                    arq.readFully(ba);

                    ByteArrayInputStream bytes = new ByteArrayInputStream(ba);
                    DataInputStream dados = new DataInputStream(bytes);

                    int game_id = dados.readInt();
                    if (game_id == id) {
                        arq.close();
                        return pos;
                    }
                } else {
                    long posAtual = arq.getFilePointer();
                    arq.seek(posAtual + tam);
                }
            }
            //System.out.println("ID não encontrado. :(");
            arq.close();
            return -1;
        } catch (Exception e) {
            System.out.println("Erro durante a busca: " + e.getMessage());
            return -1;
        }
    }
 
    public static int achaPenultimoId() {
        int penultimoId = -1, maior = -1;
        int idAtual, ultimoId;
        try(RandomAccessFile arq = new RandomAccessFile(caminhoBinario, "r");){
            arq.seek(0);
            ultimoId = arq.readInt();
            long pos;
            while ((pos = arq.getFilePointer()) < arq.length()) {
                byte lapide = arq.readByte();
                int tam = arq.readInt();
                if (lapide == 0) {
                    byte[] ba = new byte[tam];
                    arq.readFully(ba);

                    ByteArrayInputStream bytes = new ByteArrayInputStream(ba);
                    DataInputStream dados = new DataInputStream(bytes);

                    idAtual = dados.readInt();

                    if (idAtual > maior) {
                        penultimoId = maior;
                        maior = idAtual;
                    }else if(idAtual > penultimoId && idAtual < maior){
                        penultimoId = idAtual;
                    }
                } else {
                    long posAtual = arq.getFilePointer();
                    arq.seek(posAtual + tam);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro procurando penultimo ID: " + e.getMessage());
        }
        return penultimoId;
    }

    public static void lerRegistro(long pointer) {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile(caminhoBinario, "r");
            arq.seek(pointer);
            byte lapide = arq.readByte();
            int tam = arq.readInt();
            byte[] ba = new byte[tam];
            arq.readFully(ba);
            Jogo temp = new Jogo();
            temp.fromByteArray(ba);
            System.out.println(temp.toString());
            arq.close();
        } catch (Exception e) {
            System.out.println("Erro durante a leitura: " + e.getMessage());
        }
    }

    public static void removerRegistro(long pointer) {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile(caminhoBinario, "rw");
            arq.seek(0);
            int ultimoId = arq.readInt();
            arq.seek(pointer);
            arq.writeByte(1);
            int tam = arq.readInt();
            byte[] ba = new byte[tam];
            arq.readFully(ba);
            Jogo jogo = new Jogo();
            jogo.fromByteArray(ba);
            if(jogo.game_id == ultimoId){
                ultimoId = achaPenultimoId();
                arq.seek(0);
                arq.writeInt(ultimoId);
            }
            System.out.println("Jogo deletado com sucesso! :D");
            System.out.println("Ultimo ID: " + ultimoId);
            arq.close();
        } catch (Exception e) {
            System.out.println("Erro durante a remoção: " + e.getMessage());
        }
    }

    public static Jogo solicitaDados(Jogo antigo) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\nPor favor, digite os dados para o jogo!");
        System.out.print("\nNome: ");
        String nome = sc.nextLine();
        System.out.print("\nData de lançamento (yyyy-mm-dd): ");
        String lancamento = sc.nextLine();
        System.out.print("\nPreço (0,0): ");
        float preco = sc.nextFloat();
        System.out.print("\nGeneros (Ex: \"Casual, indie\"): ");
        sc.nextLine();
        String generos = sc.nextLine();
        System.out.print("\nDescrição: ");
        String descricao = sc.nextLine();
        Jogo res = new Jogo(antigo.game_id, nome, lancamento, preco, generos, descricao);
        return res;
    }

    public static void atualizaRegistro(long pointer) {
        if (pointer < 0) {
            System.out.println("Registro não encontrado para atualização.");
            return;
        }
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile(caminhoBinario, "rw");
            arq.seek(pointer);
            byte lapide = arq.readByte();
            int tam = arq.readInt();
            byte[] ba = new byte[tam];
            arq.readFully(ba);

            Jogo atual = new Jogo();

            atual.fromByteArray(ba);

            Jogo atualizado = solicitaDados(atual);
            byte[] novoBa = atualizado.toByteArray();

            if (novoBa.length <= ba.length) {
                arq.seek(pointer);
                arq.writeByte(0); // Byte da lápide: 0 = valido, 1 = excluido
                arq.writeInt(ba.length);
                arq.write(novoBa);
            } else {
                arq.seek(pointer);
                arq.writeByte(1);
                arq.seek(arq.length());
                arq.writeByte(0); // Byte da lápide: 0 = valido, 1 = excluido
                arq.writeInt(novoBa.length);
                arq.write(novoBa);
            }

            System.out.println("\nJogo atualizado com sucesso! :D\n");
            arq.close();
        } catch (Exception e) {
            System.out.println("Erro durante a atualização: " + e.getMessage());
        }
    }

    private static void imprimirTop10(String caminho) {
        try {
            java.io.RandomAccessFile arq = new java.io.RandomAccessFile(caminho, "r");
            arq.seek(4); 

            int cont = 0;
            while (arq.getFilePointer() < arq.length() && cont < 10) {
                byte lapide = arq.readByte(); 
                int tamanho = arq.readInt();
                byte[] ba = new byte[tamanho];
                arq.readFully(ba);


                if (lapide == 0) {
                    Jogo jogo = new Jogo();
                    jogo.fromByteArray(ba);
                    System.out.println(jogo.toString() + "\n");
                    cont++;
                }
            }
            arq.close();
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: " + caminho);
        } catch (Exception e) {
            System.out.println("Erro ao ler " + caminho + ": " + e.getMessage());
        }
    }

public static void chamaOrdenacao() throws Exception{
    OrdenacaoExterna ordenacao = new OrdenacaoExterna();
    long tempoInicio = System.currentTimeMillis();
    int arquivos = ordenacao.criaArquivos(10000);
    ordenacao.intercalacao(arquivos);
    long tempoFim = System.currentTimeMillis();
            System.out.println("Tempo total: " + (tempoFim - tempoInicio) + " ms");
}

public static class OrdenacaoExterna {

    
    public int criaArquivos(int tamanho) throws Exception {
        RandomAccessFile arquivoOriginal = new RandomAccessFile("dataBase/jogos.db", "r");
        arquivoOriginal.seek(4); 

        int numArqTemp = 1;
        boolean fimDoArquivo = false;


        while (!fimDoArquivo) {
            List<Jogo> bm = new ArrayList<>();

            // insere os jogos no array
            for (int i = 0; i < tamanho; i++) {
                if (arquivoOriginal.getFilePointer() < arquivoOriginal.length()) {
                    Jogo jogo = lerProxJogo(arquivoOriginal);
                    if (jogo != null) {
                        bm.add(jogo);
                    } else {
                        i--; 
                    }
                } else {
                    fimDoArquivo = true;
                    break;
                }
            }

            // ordena a lista na memória e salva no arquivo temporário
            if (!bm.isEmpty()) {
                // ordena pelo ID 
                bm.sort((j1, j2) -> Integer.compare(j1.game_id, j2.game_id));

                String nomeArquivoTemp = "dataBase/temp" + numArqTemp + ".db";
                salvarTemp(bm, nomeArquivoTemp);
                
                numArqTemp++;
            }
        }
        
        arquivoOriginal.close();
        
        return numArqTemp - 1; // retorna quantos arquivos foram gerados
    }


    public void intercalacao(int qtdArqTemp) throws Exception {

        RandomAccessFile[] arquivosTemps = new RandomAccessFile[qtdArqTemp];
        Jogo[] jogosAtuais = new Jogo[qtdArqTemp];

        // verifica o primeiro jogo de cada arquivo
        for (int i = 0; i < qtdArqTemp; i++) {
            arquivosTemps[i] = new RandomAccessFile("dataBase/temp" + (i + 1) + ".db", "r");
            jogosAtuais[i] = lerProxJogo(arquivosTemps[i]);
        }

        RandomAccessFile arquivoFinal = new RandomAccessFile("dataBase/jogos_ordenado.db", "rw");
        arquivoFinal.writeInt(0); 
        int maiorId = 0;

        while (true) {
            int arqMenor = -1;
            int menorId = 1000000000;

            // procura o menor ID entre os jogos atuais de cada arquivo
            for (int i = 0; i < qtdArqTemp; i++) {
                if (jogosAtuais[i] != null && jogosAtuais[i].game_id < menorId) {
                    menorId = jogosAtuais[i].game_id;
                    arqMenor = i;
                }
            }

            // se não achou nenhum, acabou os qruivos
          if (arqMenor == -1) {
                break;
            }

            // grava no arquivo final
            Jogo menorJogo = jogosAtuais[arqMenor];
            byte[] ba = menorJogo.toByteArray();
            arquivoFinal.writeBoolean(false); 
            arquivoFinal.writeInt(ba.length);
            arquivoFinal.write(ba);

            if (menorJogo.game_id > maiorId){ 
                maiorId = menorJogo.game_id;
            }

            jogosAtuais[arqMenor] = lerProxJogo(arquivosTemps[arqMenor]);
        }

        arquivoFinal.seek(0);
        arquivoFinal.writeInt(maiorId);
        arquivoFinal.close();

        // apaga os arquivos temporários
        for (int i = 0; i < qtdArqTemp; i++) {
            arquivosTemps[i].close();
            new File("dataBase/temp" + (i + 1) + ".db").delete();
        }

        System.out.println("Intercalação concluída.");
    }

    // Auxiliares

    private void salvarTemp(List<Jogo> bloco, String nomeArquivo) throws Exception {
        RandomAccessFile rafTemp = new RandomAccessFile(nomeArquivo, "rw");
        for (Jogo jogo : bloco) {
            byte[] ba = jogo.toByteArray();
            rafTemp.writeBoolean(false); 
            rafTemp.writeInt(ba.length); 
            rafTemp.write(ba);           
        }
        rafTemp.close();
    }

    private Jogo lerProxJogo(RandomAccessFile raf) throws Exception {
        while (raf.getFilePointer() < raf.length()) {
            boolean lapide = raf.readBoolean();
            int tamanho = raf.readInt();
            byte[] ba = new byte[tamanho];
            raf.read(ba);

            if (!lapide) {
                Jogo jogo = new Jogo();
                jogo.fromByteArray(ba);
                return jogo;
            }
        }
        return null;
    }
}


public static class BMaisNo {


    public boolean isFolha;
    public int numIds;
    public int[] ids;          // IDs dos jogos
    public long[] ponteiros;     // Se for nó interno - ponteiro dos nós filhos 
    public long[] registros;  // Se for folha - posições dos registros no arquivo de dados
    public long proxFolha;       // Se for lista encadeada das folhas
    public int ordem;          // Ordem - m 

    // Construtor 
    public BMaisNo(int ordem, boolean isFolha) {
        this.ordem = ordem;
        this.isFolha = isFolha;
        this.numIds = 0;
        this.proxFolha = -1; 
        
        this.ids = new int[ordem - 1];
        
        if (this.isFolha) {
            this.registros = new long[ordem - 1];
            this.ponteiros = null;
        } else {
            this.ponteiros = new long[ordem];
            this.registros = null;
        }
    }

    // escreve o nó no arquivo 
    
    public void salvarDisco(RandomAccessFile arq, long pos) throws IOException {
        arq.seek(pos);
        
        arq.writeBoolean(this.isFolha);
        arq.writeInt(this.numIds);
        
        // Grava o vetor de chaves (sempre grava o limite máximo para manter o tamanho fixo da página)
        for (int i = 0; i < ordem - 1; i++) {
            if (i < numIds) {
                arq.writeInt(ids[i]);
            } else {
                arq.writeInt(-1); // Espaço vazio
            }
        }
        
        // grava os ponteiros específicos garantindo o mesmo tamanho de bytes para folha ou interno
        if (this.isFolha) {
            for (int i = 0; i < ordem - 1; i++) {
                if (i < numIds) {
                    arq.writeLong(registros[i]);
                } else {
                    arq.writeLong(-1);
                }
            }
            arq.writeLong(this.proxFolha);
        } else {
            for (int i = 0; i < ordem; i++) {
                // nós internos têm (numIds + 1) ponteiros ativos
                if (i <= numIds) {
                    arq.writeLong(ponteiros[i]);
                } else {
                    arq.writeLong(-1);
                }
            }
        }
    }

    // lê um nó do arquivo inicial
    public static BMaisNo lerDisco(RandomAccessFile arq, long pos, int ordem) throws IOException {
        arq.seek(pos);
        
        boolean isFolha = arq.readBoolean();
        BMaisNo no = new BMaisNo(ordem, isFolha);
        
        no.numIds = arq.readInt();
        
        for (int i = 0; i < ordem - 1; i++) {
            no.ids[i] = arq.readInt();
        }
        
        if (isFolha) {
            for (int i = 0; i < ordem - 1; i++) {
                no.registros[i] = arq.readLong();
            }
            no.proxFolha = arq.readLong();
        } else {
            for (int i = 0; i < ordem; i++) {
                no.ponteiros[i] = arq.readLong();
            }
        }
        
        return no;
    }
}

public class ArvoreBMais {
    private RandomAccessFile arq;
    private int ordem;
    private long raiz; 

    public ArvoreBMais(RandomAccessFile arq, int ordem, long raiz) {
        this.arq = arq;
        this.ordem = ordem;
        this.raiz = raiz;
    }

    // Busca um ID na árvore e retorna a posição do registro
    
    public long buscar(int idProcurado) throws IOException {
        // Se a árvore estiver vazia 
        if (raiz == -1) {
            return -1; 
        }

        long registroAtual = raiz;
        BMaisNo noAtual = BMaisNo.lerDisco(arq, registroAtual, ordem);

        // while que faz ir até a folha
        while (!noAtual.isFolha) {
            int i = 0;
            
            // while pra guardar o indice do array de ponteiros pra descer pela arvore
            while (i < noAtual.numIds && idProcurado >= noAtual.ids[i]) {
                i++;
            }
            
            registroAtual = noAtual.ponteiros[i];
            noAtual = BMaisNo.lerDisco(arq, registroAtual, ordem);
        }

        // ao chegar na folha faz uma busca linear 
        for (int i = 0; i < noAtual.numIds; i++) {
            if (noAtual.ids[i] == idProcurado) {
                return noAtual.registros[i]; 
            }
        }

        // se percorreu a folha inteira e não achou o ID, o jogo não existe.
        return -1; 
    }
}

}