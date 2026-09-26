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
    
    // variaveis globais da arvore para o menu todo enxergar
    public static ArvoreB arvore;
    public static long raiz = -1;
    public static RandomAccessFile arqIndice;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        // inicializa a arvore b com ordem 1000 
        try {
            arqIndice = new RandomAccessFile("database/indice.bin", "rw");
            if (arqIndice.length() == 0) {
                arqIndice.writeLong(-1);
            } else {
                arqIndice.seek(0);
                raiz = arqIndice.readLong();
            }
            arvore = new ArvoreB(arqIndice, 510, raiz); 
        } catch (IOException e) {
            System.out.println("Erro ao criar indice: " + e.getMessage());
        }

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
                    System.out.print("\nDigite o ID que deseja buscar (Busca Sequencial): ");
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
                        System.out.println("Registro não encontrado");
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
                        System.out.println("Registro não encontrado!");
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
                case 9:
                    System.out.print("\nDigite o ID que deseja buscar na Árvore B: ");
                    id = sc.nextInt();
                    System.out.println();
                    try {
                        retorno = arvore.buscar(id);
                        if(retorno == -1){
                            System.out.println("Registro não encontrado no índice da Árvore B!");
                        } else{
                            System.out.println("Encontrado no índice!");
                            lerRegistro(retorno); 
                        }
                    } catch (Exception e) {
                        System.out.println("Erro ao buscar no índice: " + e.getMessage());
                    }
                    break;
                default:
                    System.out.println("Numero invalido!");
            }
            
            // salva a nova raiz no cabecalho se ela tiver mudado de lugar
            try {
                if (arvore != null && arvore.getraiz() != raiz) {
                    raiz = arvore.getraiz();
                    arqIndice.seek(0);
                    arqIndice.writeLong(raiz);
                }
            } catch (Exception e) {}

            criaMenu();
            opt = sc.nextInt();
        }
        
        try {
            if (arqIndice != null) arqIndice.close();
        } catch (Exception e) {}
        
        sc.close();
    }

    public static void criaMenu() {
        String titulo = "\n-----------Steam Games DB----------";
        String barra = "-----------------------------------\n";
        String opcoes = String.format("\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s", 
                "[1] Carregar base de dados", 
                "[2] Inserir registro", 
                "[3] Ler registro (Sequencial)",
                "[4] Atualizar registro", 
                "[5] Deletar registro",
                "[6] Ordenar registros",
                "[7] Top 10 (Base)",
                "[8] Top 10 (Ordenado)",
                "[9] Buscar na Árvore B (Indexado)");

        System.out.println(titulo);
        System.out.println("Selecione a opcao (ou 0 para Sair):");
        System.out.println(opcoes);
        System.out.println(barra);
    }
    public static void leitorCSV() {
        int ultimoId = 0;
        int contProgresso = 0; 

        try (BufferedReader leitor = Files.newBufferedReader(caminhoCSV)) {
            RandomAccessFile arq = new RandomAccessFile(caminhoBinario, "rw");
            
            arq.setLength(0); 
            arqIndice.setLength(0);
            arqIndice.writeLong(-1);
            raiz = -1; 
            arvore = new ArvoreB(arqIndice, 50, raiz); 
            
            arq.writeInt(0);
            String linha;
            leitor.readLine();
            
            System.out.println("Iniciando carregamento e indexação");
            
            while ((linha = leitor.readLine()) != null) {
                String[] valores = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); 
                if (valores.length == 6) {

                    int id = Integer.parseInt(valores[0]);
                    String nome = valores[1];
                    String lancamento = valores[2];
                    float preco = 0.0f;
                    if (!valores[3].trim().isEmpty()) {
                        try {
                            preco = Float.parseFloat(valores[3]);
                        } catch (Exception e) {}
                    }

                    String generosRaw = valores[4]; 
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
                        
                        // pega a coordenada antes de gravar a lapide
                        long pos = arq.getFilePointer();
                        
                        arq.writeByte(0); 
                        arq.writeInt(ba.length);
                        arq.write(ba);
                        
                        // insere na arvore
                        arvore.inserir(id, pos);
                        
                        contProgresso++;
                        if (contProgresso % 1000 == 0) {
                            System.out.println(contProgresso + " jogos processados...");
                        }
                        
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
            arq.seek(0);
            arq.writeInt(ultimoId);
            System.out.println("\n Base de dados carregada e Árvore B criada. Ultimo ID: " + ultimoId);
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
            
            // anota o byte exato onde o jogo novo vai ficar
            long pos = arq.getFilePointer();
            
            arq.writeByte(0); 
            arq.writeInt(tam);
            arq.write(ba);
            
            // indexa na arvore b
            arvore.inserir(ultimoId, pos);
            
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
public static class ArvoreB {
        private RandomAccessFile arq;
        private int ordem;
        private long raiz; 

        public static class BNo {
            public boolean isFolha; // define se e folha ou no interno
            public int numIds; // quantidade atual de chaves no no
            public int[] ids; // vetor de chaves para busca
            public long[] registros; // enderecos originais no arquivo de dados
            public long[] ponteiros; // enderecos das paginas filhas no indice
            public int ordem;

            public BNo(int ordem, boolean isFolha) {
                this.ordem = ordem;
                this.isFolha = isFolha;
                this.numIds = 0;
                
                this.ids = new int[ordem - 1];
                this.registros = new long[ordem - 1];
                this.ponteiros = new long[ordem];
            }

            public void salvarDisco(RandomAccessFile arq, long pos) throws IOException {
                arq.seek(pos); // pula direto para o byte exato da pagina
                arq.writeBoolean(this.isFolha);
                arq.writeInt(this.numIds);

                // preenche espacos vazios com -1 garantindo tamanho fixo no disco
                for (int i = 0; i < ordem - 1; i++) {
                    if (i < numIds) arq.writeInt(ids[i]);
                    else arq.writeInt(-1);
                }
                
                for (int i = 0; i < ordem - 1; i++) {
                    if (i < numIds) arq.writeLong(registros[i]);
                    else arq.writeLong(-1);
                }

                for (int i = 0; i < ordem; i++) {
                    if (i <= numIds && !isFolha) arq.writeLong(ponteiros[i]);
                    else arq.writeLong(-1);
                }
            }

            public static BNo lerDisco(RandomAccessFile arq, long pos, int ordem) throws IOException {
                arq.seek(pos);
                boolean isFolha = arq.readBoolean();
                BNo no = new BNo(ordem, isFolha);
                
                no.numIds = arq.readInt();
                
                for (int i = 0; i < ordem - 1; i++) {
                    no.ids[i] = arq.readInt();
                }

                for (int i = 0; i < ordem - 1; i++) {
                    no.registros[i] = arq.readLong();
                }
                for (int i = 0; i < ordem; i++) {
                    no.ponteiros[i] = arq.readLong();
                }
                return no;
            }
        }
        
        public ArvoreB(RandomAccessFile arq, int ordem, long raiz) {
            this.arq = arq;
            this.ordem = ordem;
            this.raiz = raiz;
        }

        public long getraiz() {
            return this.raiz;
        }

        // classe mensageira para transportar a chave promovida no split
        private class Subir {
            int id;
            long registro;
            long filhoDireito;

            public Subir(int id, long registro, long filhoDireito) {
                this.id = id;
                this.registro = registro;
                this.filhoDireito = filhoDireito;
            }
        }

        public long buscar(int idProcurado) throws IOException {
            if (raiz == -1) return -1; // arvore vazia

            long registroAtual = raiz;

            while (registroAtual != -1) {
                BNo no = BNo.lerDisco(arq, registroAtual, ordem);
                int i = 0;
                
                // acha a posicao correta do ponteiro de descida
                while (i < no.numIds && idProcurado > no.ids[i]) {
                    i++;
                }

                if (i < no.numIds && idProcurado == no.ids[i]) {
                    return no.registros[i]; // achou o jogo
                }

                if (no.isFolha) {
                    return -1; // bateu no fundo da arvore e o jogo nao existe
                }

                registroAtual = no.ponteiros[i];
            }
            return -1;
        }

        public void inserir(int id, long registro) throws IOException {
            if (raiz == -1) {
                // cria a primeira raiz se o arquivo estiver zerado
                BNo novaRaiz = new BNo(ordem, true);
                novaRaiz.ids[0] = id;
                novaRaiz.registros[0] = registro;
                novaRaiz.numIds = 1;
                
                this.raiz = arq.length();
                if (this.raiz == 0) this.raiz = 8; // preserva o cabecalho de 8 bytes
                
                novaRaiz.salvarDisco(arq, this.raiz);
                return;
            }

            Subir sobe = inserirRecursivo(raiz, id, registro);

            if (sobe != null) {
                // cria um novo andar no topo se a raiz antiga estourou
                BNo novaRaiz = new BNo(ordem, false); 
                novaRaiz.ids[0] = sobe.id;
                novaRaiz.registros[0] = sobe.registro;
                novaRaiz.ponteiros[0] = this.raiz;
                novaRaiz.ponteiros[1] = sobe.filhoDireito;
                novaRaiz.numIds = 1;
                
                this.raiz = arq.length();
                novaRaiz.salvarDisco(arq, this.raiz);
            }
        }

        private Subir inserirRecursivo(long registroAtual, int id, long registro) throws IOException {
            BNo no = BNo.lerDisco(arq, registroAtual, ordem);
            
            int i = 0;
            while (i < no.numIds && id > no.ids[i]) i++;

            if (i < no.numIds && id == no.ids[i]) return null; // ignora ids duplicados

            if (no.isFolha) {
                if (no.numIds < ordem - 1) {
                    // insere o numero abrindo buraco no vetor pois tem espaco
                    inserirVetor(no, i, id, registro, -1);
                    no.salvarDisco(arq, registroAtual);
                    return null;
                } else {
                    // estouro na folha exige divisao
                    return dividirNo(no, registroAtual, id, registro, -1);
                }
            } else {
                Subir sobe = inserirRecursivo(no.ponteiros[i], id, registro);
                
                if (sobe == null) return null; // tudo resolvido la embaixo
                
                int pos = 0;
                while (pos < no.numIds && sobe.id > no.ids[pos]) pos++;

                if (no.numIds < ordem - 1) {
                    // absorve a chave promovida no no interno
                    inserirVetor(no, pos, sobe.id, sobe.registro, sobe.filhoDireito);
                    no.salvarDisco(arq, registroAtual);
                    return null;
                } else {
                    // estouro no no interno propaga nova chave pra cima
                    return dividirNo(no, registroAtual, sobe.id, sobe.registro, sobe.filhoDireito);
                }
            }
        }

        private void inserirVetor(BNo no, int pos, int id, long reg, long filhoDireito) {
            // empurra os maiores pra direita garantindo a ordenacao crescente
            for (int j = no.numIds; j > pos; j--) {
                no.ids[j] = no.ids[j - 1];
                no.registros[j] = no.registros[j - 1];
                no.ponteiros[j + 1] = no.ponteiros[j];
            }
            no.ids[pos] = id;
            no.registros[pos] = reg;
            no.ponteiros[pos + 1] = filhoDireito;
            no.numIds++;
        }

        private Subir dividirNo(BNo no, long registroAtual, int novoId, long novoReg, long novoFilhoDir) throws IOException {
            // vetores temporarios estendidos para misturar tudo antes de cortar
            int[] tempIds = new int[ordem];
            long[] tempRegs = new long[ordem];
            long[] tempPonts = new long[ordem + 1];

            int pos = 0;
            while (pos < no.numIds && no.ids[pos] < novoId) pos++;

            for (int j = 0, k = 0; j < ordem; j++) {
                if (j == pos) {
                    tempIds[j] = novoId;
                    tempRegs[j] = novoReg;
                } else {
                    tempIds[j] = no.ids[k];
                    tempRegs[j] = no.registros[k];
                    k++;
                }
            }
            
            for (int j = 0, k = 0; j <= ordem; j++) {
                if (j == pos + 1) tempPonts[j] = novoFilhoDir;
                else { tempPonts[j] = no.ponteiros[k]; k++; }
            }

            // identifica exatamente quem fica na divisao para ser promovido
            int meio = ordem / 2;
            Subir sobe = new Subir(tempIds[meio], tempRegs[meio], -1); 

            // mantem a metade inferior no no da esquerda
            no.numIds = meio;
            for (int j = 0; j < meio; j++) {
                no.ids[j] = tempIds[j];
                no.registros[j] = tempRegs[j];
                no.ponteiros[j] = tempPonts[j];
            }
            no.ponteiros[meio] = tempPonts[meio];

            BNo novoNo = new BNo(ordem, no.isFolha);
            novoNo.numIds = ordem - meio - 1; 
            for (int j = 0; j < novoNo.numIds; j++) {
                novoNo.ids[j] = tempIds[meio + 1 + j];
                novoNo.registros[j] = tempRegs[meio + 1 + j];
                novoNo.ponteiros[j] = tempPonts[meio + 1 + j];
            }
            novoNo.ponteiros[novoNo.numIds] = tempPonts[ordem];

            long registroNovoNo = arq.length();
            no.salvarDisco(arq, registroAtual);
            novoNo.salvarDisco(arq, registroNovoNo);

            sobe.filhoDireito = registroNovoNo;
            return sobe;
        }
    }

}