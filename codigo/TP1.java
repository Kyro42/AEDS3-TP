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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.nio.file.StandardCopyOption;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.util.PriorityQueue;

public class TP1 {
    private static final Scanner sc = new Scanner(System.in);
   private static final String DIR_DADOS = "dataBase/";

private static Path caminhoCSV = Paths.get(DIR_DADOS + "steam_games.csv");
private static String caminhoBinario = DIR_DADOS + "jogos.db";
private static final String caminhoBlocos = DIR_DADOS + "blocos.listainv.db";
private static final String caminhoDicionario = DIR_DADOS + "dicionario.listainv.db";

    // variaveis globais da arvore para o menu todo enxergar
    public static ArvoreB arvore;
    public static RandomAccessFile arqIndice;
    public static ListaInvertida listaInvertida;
    
    public static void main(String[] args) {


        // inicializa a arvore b com ordem 1000 
        try {
    Files.createDirectories(Paths.get(DIR_DADOS));
    arqIndice = new RandomAccessFile(DIR_DADOS + "indice.bin", "rw");
    arvore = new ArvoreB(arqIndice, 510); // o construtor lê ou cria o cabeçalho sozinho
} catch (IOException e) {
    System.out.println("Erro ao criar indice: " + e.getMessage());
}

        //inicializa a lista invertida
        try {
            listaInvertida = new ListaInvertida();
        } catch (Exception e) {
            System.out.println("Erro ao criar lista invertida: " + e.getMessage());
        }

        criaMenu();
        int opt = Integer.parseInt(sc.nextLine().trim());
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
                    id = Integer.parseInt(sc.nextLine().trim());
                    System.out.println();
                    retorno = buscador(id);

                    if (retorno == -1) {
                        System.out.println("Registro não encontrado :(");
                    } else {
                        lerRegistro(retorno);
                    }
                    break;
                case 4:
                    System.out.print("\nDigite o ID que deseja atualizar: ");
                    id = Integer.parseInt(sc.nextLine().trim());
                    System.out.println();
                    retorno = buscador(id);
                    if (retorno == -1) {
                        System.out.println("Registro não encontrado");
                    } else {
                        atualizaRegistro(retorno);
                    }
                    break;
                case 5:
                    System.out.print("\nDigite o ID que deseja deletar: ");
                    id = Integer.parseInt(sc.nextLine().trim());
                    System.out.println();
                    retorno = buscador(id);
                    if (retorno == -1) {
                        System.out.println("Registro não encontrado!");
                    } else {
                        removerRegistro(retorno);
                    }
                    break;
                case 6:
                    try {
                        chamaOrdenacao();
                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                    break;
                case 7:
                    System.out.print("\nDigite o ID que deseja buscar na Árvore B: ");
                    id = Integer.parseInt(sc.nextLine().trim());
                    System.out.println();
                    try {
                        retorno = arvore.buscar(id);
                        if (retorno == -1) {
                            System.out.println("Registro não encontrado no índice da Árvore B!");
                        } else {
                            System.out.println("Encontrado no índice!");
                            lerRegistro(retorno);
                        }
                    } catch (Exception e) {
                        System.out.println("Erro ao buscar no índice: " + e.getMessage());
                    }
                    break;
                case 8:
                    buscarListaInv();
                    break;
                    case 9:
                comprimirBase();
                break;
                default:
                    System.out.println("Numero invalido!");
            }

    
            criaMenu();
            opt = Integer.parseInt(sc.nextLine().trim());
        }

        try {
            if (arqIndice != null)
                arqIndice.close();
        } catch (Exception e) {
        }
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
        "[7] Buscar na Árvore B (Indexado)",
        "[8] Buscar na lista invertida",
        "[9] Comprimir base de dados");

        System.out.println(titulo);
        System.out.println("Selecione a opção (ou 0 para Sair):");
        System.out.println(opcoes);
        System.out.println(barra);
    }

    public static void leitorCSV() {
        int ultimoId = 0;
        int contProgresso = 0;

            try (BufferedReader leitor = Files.newBufferedReader(caminhoCSV)) {
            RandomAccessFile arq = new RandomAccessFile(caminhoBinario, "rw");

            arq.setLength(0);     
            try {
                listaInvertida.limpar();
            } catch (Exception e) {
                System.out.println("Aviso ao limpar lista: " + e.getMessage());
            }
            
            arqIndice.setLength(0);
            arvore = new ArvoreB(arqIndice, 510); 
            
            arq.writeInt(0); 
            String linha;
            leitor.readLine(); 

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
                        } catch (Exception e) {
                        }
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

                        //separa as palavras do nome do jogo para fazer a lista invertida
                        ArrayList<String> palavras = separadorListaInv(nome);
                        for (String palavra : palavras) {
                            try {
                                ElementoLista elemento = new ElementoLista(id);
                                listaInvertida.create(palavra, elemento);
                            } catch (Exception e) {
                                System.out.println("Erro ao indexar palavra " + palavra + ": " + e.getMessage());
                            }
                        }

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
            System.out.println("\n Base de dados carregada! Árvore B e Lista invertida criadas. Ultimo ID: " + ultimoId);
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

            //separa as palavras do nome do novo jogo
            ArrayList<String> palavras = separadorListaInv(jogo.game_name);
            for (String palavra : palavras) {
                try {
                    listaInvertida.create(palavra, new ElementoLista(ultimoId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            arq.seek(0);
            arq.writeInt(ultimoId);
            System.out.println("Jogo inserido com sucesso! id: " + ultimoId);
            arq.close();
        } catch (Exception e) {
            e.printStackTrace();
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
        try (RandomAccessFile arq = new RandomAccessFile(caminhoBinario, "r");) {
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
                    } else if (idAtual > penultimoId && idAtual < maior) {
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
            
            arq.seek(pointer);
            arq.writeByte(1); // Lápide de exclusão ativada
            int tam = arq.readInt();
            byte[] ba = new byte[tam];
            arq.readFully(ba);

            Jogo jogo = new Jogo();
            jogo.fromByteArray(ba);

            // remove ponteiro da arvore
            arvore.atualizar(jogo.game_id, -1);

            //separa as palavras do nome do jogo
            ArrayList<String> palavras = separadorListaInv(jogo.game_name);
            for (String palavra : palavras) {
                try {
                    listaInvertida.delete(palavra, jogo.game_id);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            System.out.println("Jogo deletado!");
            arq.close();
        } catch (Exception e) {
            System.out.println("Erro durante a remoção: " + e.getMessage());
        }
    }

    public static Jogo solicitaDados(Jogo antigo) {
        System.out.println("\nPor favor, digite os dados para o jogo!");
        System.out.print("\nNome: ");
        String nome = sc.nextLine().trim();
        System.out.print("\nData de lançamento (yyyy-mm-dd): ");
        String lancamento = sc.nextLine().trim();
        System.out.print("\nPreço (0,0): ");
        float preco = Float.parseFloat(sc.nextLine().trim().replace(',', '.'));
        System.out.print("\nGeneros (Ex: \"Casual, indie\"): ");
        String generos = sc.nextLine().trim();
        System.out.print("\nDescrição: ");
        String descricao = sc.nextLine().trim();
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
            arq.skipBytes(1); 
            
            int tam = arq.readInt();
            byte[] ba = new byte[tam];
            arq.readFully(ba);

            Jogo atual = new Jogo();
            atual.fromByteArray(ba);

            Jogo atualizado = solicitaDados(atual);

            //verifica se o nome do jogo permanece o mesmo. Caso seja diferente, atualiza
            if (!atual.game_name.equalsIgnoreCase(atualizado.game_name)) {
                //apaga as palavras antigas
                ArrayList<String> antigas = separadorListaInv(atual.game_name);
                for (String p : antigas) {
                    listaInvertida.delete(p, atual.game_id);
                }
                //insere as novas palavras
                ArrayList<String> palavrasNovas = separadorListaInv(atualizado.game_name);
                for (String p : palavrasNovas) {
                    ElementoLista elemento = new ElementoLista(atualizado.game_id);
                    listaInvertida.create(p, elemento);
                }
            }

            byte[] novoBa = atualizado.toByteArray();

            if (novoBa.length <= ba.length) {
                arq.seek(pointer);
                arq.writeByte(0); // Byte da lápide: 0 = valido, 1 = excluido
                arq.writeInt(ba.length);
                arq.write(novoBa);
            } else {
                arq.seek(pointer);
                arq.writeByte(1); // Exclui o antigo

                long novaPosicao = arq.length(); // Anota o byte do final do arquivo

                arq.seek(novaPosicao);
                arq.writeByte(0);
                arq.writeInt(novoBa.length);
                arq.write(novoBa);

                // muda o ponteiro da arvore
                arvore.atualizar(atual.game_id, novaPosicao);
            }

            System.out.println("\nJogo atualizado com sucesso!\n");
            arq.close();
        } catch (Exception e) {
            System.out.println("Erro durante a atualização: " + e.getMessage());
        }
    }

    public static ArrayList<String> separadorListaInv(String nome) {
        ArrayList<String> res = new ArrayList<>();
        //converte para minusculo e remove tudo que não é letra ou numero
        String[] palavras = nome.toLowerCase().split("\\W+");

        for (String p : palavras) {

            //garante que so vai adicionar palavras maiores que 2 caracteres. Isso evita salvar palavras como: "de", "ou", "se", etc.
            if (p.length() > 2) {
                res.add(p);
            }
        }
        return res;
    }

    public static void buscarListaInv() {
        System.out.print("\nDigite uma palavra para buscar o(s) jogo(s): ");
        String termo = sc.nextLine().trim();    
        String palavraLimpa = termo.toLowerCase().trim();
        
        try {
            ElementoLista[] res = listaInvertida.read(palavraLimpa);
            if (res.length == 0) {
                System.out.println("Nenhum jogo encontrado com a palavra: '" + palavraLimpa + "'");
                return;
            }
            System.out.println("\n--- Jogos Encontrados ---");

            //usa a busca sequencial (buscador) para achar o registro completo
            for (ElementoLista elemento : res) {
                long posicao = buscador(elemento.getId());
                if(posicao != -1) {
                    lerRegistro(posicao);
                }
            }
            System.out.println("-------------------------\n");
            
        } catch (Exception e) {
            System.out.println("Erro na busca: " + e.getMessage());
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

    public static void chamaOrdenacao() throws Exception {
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
            arquivoFinal.setLength(0);
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

                if (menorJogo.game_id > maiorId) {
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
                    if (i < numIds)
                        arq.writeInt(ids[i]);
                    else
                        arq.writeInt(-1);
                }

                for (int i = 0; i < ordem - 1; i++) {
                    if (i < numIds)
                        arq.writeLong(registros[i]);
                    else
                        arq.writeLong(-1);
                }

                for (int i = 0; i < ordem; i++) {
                    if (i <= numIds && !isFolha)
                        arq.writeLong(ponteiros[i]);
                    else
                        arq.writeLong(-1);
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

        public ArvoreB(RandomAccessFile arq, int ordemParametro) throws IOException {
            this.arq = arq;
            
            // se o arquivo já existe e tem o cabeçalho (12 bytes)
            if (this.arq.length() >= 12) {
                this.arq.seek(0);
                this.ordem = this.arq.readInt(); 
                this.raiz = this.arq.readLong();
            } else {
                this.ordem = ordemParametro;
                this.raiz = -1;
                salvarCabecalho();
            }
        }

        public void salvarCabecalho() throws IOException {
            arq.seek(0);
            arq.writeInt(this.ordem);
            arq.writeLong(this.raiz);
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
            if (raiz == -1)
                return -1; // arvore vazia

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
                if (this.raiz == 0)
                    this.raiz = 12; // CORREÇÃO: preserva o cabeçalho de 12 bytes (4 int + 8 long)

                novaRaiz.salvarDisco(arq, this.raiz);
                salvarCabecalho(); // Salva a nova raiz no byte 4 do arquivo
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
                salvarCabecalho(); // Salva a nova raiz no byte 4 do arquivo
            }
        }

        private Subir inserirRecursivo(long registroAtual, int id, long registro) throws IOException {
            BNo no = BNo.lerDisco(arq, registroAtual, ordem);

            int i = 0;
            while (i < no.numIds && id > no.ids[i])
                i++;

            if (i < no.numIds && id == no.ids[i])
                return null; // ignora ids duplicados

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

                if (sobe == null)
                    return null; // tudo resolvido la embaixo

                int pos = 0;
                while (pos < no.numIds && sobe.id > no.ids[pos])
                    pos++;

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

        private Subir dividirNo(BNo no, long registroAtual, int novoId, long novoReg, long novoFilhoDir)
                throws IOException {
            // vetores temporarios estendidos para misturar tudo antes de cortar
            int[] tempIds = new int[ordem];
            long[] tempRegs = new long[ordem];
            long[] tempPonts = new long[ordem + 1];

            int pos = 0;
            while (pos < no.numIds && no.ids[pos] < novoId)
                pos++;

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
                if (j == pos + 1)
                    tempPonts[j] = novoFilhoDir;
                else {
                    tempPonts[j] = no.ponteiros[k];
                    k++;
                }
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

        public boolean atualizar(int idProcurado, long novoRegistro) throws IOException {
            if (raiz == -1)
                return false;

            long registroAtual = raiz;

            while (registroAtual != -1) {
                BNo no = BNo.lerDisco(arq, registroAtual, ordem);
                int i = 0;

                while (i < no.numIds && idProcurado > no.ids[i]) {
                    i++;
                }

                if (i < no.numIds && idProcurado == no.ids[i]) {
                    // achou o ID, agora faz a troca de ponteiros
                    no.registros[i] = novoRegistro;
                    no.salvarDisco(arq, registroAtual);
                    return true;
                }

                if (no.isFolha) {
                    return false; // não achou
                }

                registroAtual = no.ponteiros[i];
            }
            return false;
        }
    }

    public static class ElementoLista implements Comparable<ElementoLista>, Cloneable {
        private int id;
        
        public ElementoLista(int i) {
            this.id = i;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "("+this.id+")";
        }
        @Override
        public ElementoLista clone() {
            try {
                return (ElementoLista) super.clone();
            } catch (CloneNotSupportedException e) {
                // Tratamento de exceção se a clonagem falhar
                e.printStackTrace();
                return null;
            }
        }
        @Override
        public int compareTo(ElementoLista outro) {
            return Integer.compare(this.id, outro.id);
        }
    }

    public static class ListaInvertida {

        String nomeArquivoDicionario;
        String nomeArquivoBlocos;
        RandomAccessFile arqDicionario;
        RandomAccessFile arqBlocos;
        int quantidadeDadosPorBloco;
        private HashMap<String, Long> cacheDicionario;

        class Bloco {
            short quantidade; // quantidade de dados presentes na lista
            short quantidadeMaxima; // quantidade máxima de dados que a lista pode conter
            ElementoLista[] elementos; // sequência de dados armazenados no bloco
            long proximo; // ponteiro para o bloco sequinte do mesmo termo
            int bytesPorBloco; // size fixo do cesto em bytes

            public Bloco(int qtdmax) throws Exception {
            quantidade = 0;
            quantidadeMaxima = (short) qtdmax;
            elementos = new ElementoLista[quantidadeMaxima];
            proximo = -1;
            bytesPorBloco = (2 + 4 * quantidadeMaxima + 8);  // 4 do INT 
            }

            public byte[] toByteArray() throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeShort(quantidade);
                int i = 0;
                while (i < quantidade) {
                    dos.writeInt(elementos[i].getId());
                    i++;
                }
                while (i < quantidadeMaxima) {
                    dos.writeInt(-1);
                    i++;
                }
                dos.writeLong(proximo);
                return baos.toByteArray();
            }

            public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            quantidade = dis.readShort();
            int i = 0;
            while (i < quantidadeMaxima) {
                elementos[i] = new ElementoLista(dis.readInt());
                i++;
            }
            proximo = dis.readLong();
            }

            // Insere um valor no bloco
            public boolean create(ElementoLista e) {
                if (full()) {
                    return false;
                }
                int i = quantidade - 1;
                while (i >= 0 && e.getId() < elementos[i].getId()) {
                    elementos[i + 1] = elementos[i];
                    i--;
                }
                i++;
                elementos[i] = e.clone();
                quantidade++;
                return true;
            }

            // Testa se um valor existe no bloco
            public boolean test(int id) {
                if (empty()) {
                    return false;
                }
                int i = 0;
                while (i < quantidade && id > elementos[i].getId()) {
                    i++;
                }
                if (i < quantidade && id == elementos[i].getId()) {
                    return true;
                } 
                else {
                    return false;
                }
            }

            // Lê um valor existente no bloco
            public ElementoLista read(int id) {
                if (empty()) {
                    return null;
                }
                int i = 0;
                while (i < quantidade && id > elementos[i].getId()) {
                    i++;
                }
                if (i < quantidade && id == elementos[i].getId()) {
                    return elementos[i].clone();
                } 
                else {
                    return null;
                }
            }

            // Remove um valor do bloco
            public boolean delete(int id) {
                if (empty()) {
                    return false;
                }
                int i = 0;
                while (i < quantidade && id > elementos[i].getId()){
                    i++;
                }
                if (i < quantidade && id == elementos[i].getId()) {
                    while (i < quantidade - 1) {
                        elementos[i] = elementos[i + 1];
                        i++;
                    }
                    quantidade--;
                    return true;
                } else {
                    return false;
                }
            }

            public ElementoLista last() {
            return elementos[quantidade - 1];
            }

            public ElementoLista[] list() {
            ElementoLista[] lista = new ElementoLista[quantidade];
            for (int i = 0; i < quantidade; i++)
                lista[i] = elementos[i].clone();
            return lista;
            }

            public boolean empty() {
            return quantidade == 0;
            }

            public boolean full() {
            return quantidade == quantidadeMaxima;
            }

            public String toString() {
            String s = "\nQuantidade: " + quantidade + "\n| ";
            int i = 0;
            while (i < quantidade) {
                s += elementos[i] + " | ";
                i++;
            }
            while (i < quantidadeMaxima) {
                s += "- | ";
                i++;
            }
            return s;
            }

            public long next() {
            return proximo;
            }

            public void setNext(long p) {
            proximo = p;
            }

            public int size() {
            return bytesPorBloco;
            }
        }

        public ListaInvertida() throws Exception {
            quantidadeDadosPorBloco = 1000;

            arqDicionario = new RandomAccessFile(caminhoDicionario, "rw");
            if(arqDicionario.length()<4) {    // cabeçalho do arquivo com número de entidades
            arqDicionario.seek(0);
            arqDicionario.writeInt(0);
            }
            arqBlocos = new RandomAccessFile(caminhoBlocos, "rw");

            cacheDicionario = new HashMap<>();
            arqDicionario.seek(4);
            while (arqDicionario.getFilePointer() < arqDicionario.length()) {
                String termo = arqDicionario.readUTF();
                long end = arqDicionario.readLong();
                cacheDicionario.put(termo, end);
            }
        }

        // Incrementa o número de entidades
        public void incrementaEntidades() throws Exception {
            arqDicionario.seek(0);
            int n = arqDicionario.readInt();
            arqDicionario.seek(0);
            arqDicionario.writeInt(n+1);    
        }

        // Decrementa o número de entidades
        public void decrementaEntidades() throws Exception {
            arqDicionario.seek(0);
            int n = arqDicionario.readInt();
            arqDicionario.seek(0);
            arqDicionario.writeInt(n-1);    
        }

        // Retorna o número de entidades
        public int numeroEntidades() throws Exception {
            arqDicionario.seek(0);
            return arqDicionario.readInt();
        }

        // Insere um dado na lista do termo de forma NÃO ORDENADA
        public boolean create(String c, ElementoLista e) throws Exception {
            //busca no dicionario
            long endereco = cacheDicionario.getOrDefault(c, -1L);

            //se não encontrou, cria um novo bloco para esse termo e atualiza o dicionario
            if (endereco == -1L) {
                Bloco b = new Bloco(quantidadeDadosPorBloco);
                endereco = arqBlocos.length();
                arqBlocos.seek(endereco);
                arqBlocos.write(b.toByteArray());
                arqDicionario.seek(arqDicionario.length());
                arqDicionario.writeUTF(c);
                arqDicionario.writeLong(endereco);
                
                //salva para não ler o disco na proxima vez
                cacheDicionario.put(c, endereco); 
            }

            //cria um laço para percorrer os blocos encadeados nesse endereço
            Bloco b = new Bloco(quantidadeDadosPorBloco);
            byte[] bd;
            
            while (endereco != -1) {
                long proximo = -1;

                //carrega o bloco
                arqBlocos.seek(endereco);
                bd = new byte[b.size()];
                arqBlocos.read(bd);
                b.fromByteArray(bd);

                //ve se o ID já existe 
                if (b.test(e.getId())) {
                    return false; 
                }

                // Testa se o dado cabe nesse bloco
                if (!b.full()) {
                    b.create(e);
                    arqBlocos.seek(endereco);
                    arqBlocos.write(b.toByteArray());
                    return true;
                } else {
                    //vai para o proximo bloco
                    proximo = b.next();
                    
                    if (proximo == -1) {
                        //se não existir um novo bloco, cria e anexa ao final
                        Bloco b1 = new Bloco(quantidadeDadosPorBloco);
                        proximo = arqBlocos.length();
                        arqBlocos.seek(proximo);
                        arqBlocos.write(b1.toByteArray());

                        //atualiza o ponteiro do bloco anterior para apontar para o novo
                        b.setNext(proximo);
                        arqBlocos.seek(endereco);
                        arqBlocos.write(b.toByteArray());
                    }
                }
                endereco = proximo; // Continua a varredura se não coube
            }
            return true;
        }

        // Retorna a lista de entidades completa de um determinado termo
        public ElementoLista[] read(String c) throws Exception {
            ArrayList<ElementoLista> lista = new ArrayList<>();
            long endereco = cacheDicionario.getOrDefault(c, -1L);

            if (endereco == -1L) {
                return new ElementoLista[0];
            }
            
            // Cria um laço para percorrer todos os blocos encadeados nesse endereço
            Bloco b = new Bloco(quantidadeDadosPorBloco);
            byte[] bd;
            while (endereco != -1) {
                // Carrega o bloco
                arqBlocos.seek(endereco);
                bd = new byte[b.size()];
                arqBlocos.read(bd);
                b.fromByteArray(bd);

                // Acrescenta cada valor à lista
                ElementoLista[] lb = b.list();
                for (int i = 0; i < lb.length; i++) {
                    lista.add(lb[i]);
                }
                // Avança para o próximo bloco
                endereco = b.next();
            }

            // Constrói o vetor de respostas
            lista.sort(null);
            ElementoLista[] resposta = new ElementoLista[lista.size()];
            for (int j = 0; j < lista.size(); j++) {
                resposta[j] = lista.get(j);
            }
            return resposta;
        }

        // Remove o dado de um termo (mas não apaga o termo nem apaga blocos)
        public boolean delete(String c, int id) throws Exception {
            long endereco = cacheDicionario.getOrDefault(c, -1L);
            if (endereco == -1L) {
                return false;
            }

            // Cria um laço para percorrer todos os blocos encadeados nesse endereço
            Bloco b = new Bloco(quantidadeDadosPorBloco);
            byte[] bd;
            while (endereco != -1) {

                //carrega o bloco
                arqBlocos.seek(endereco);
                bd = new byte[b.size()];
                arqBlocos.read(bd);
                b.fromByteArray(bd);

                //testa se o valor está neste bloco e sai do laço
                if (b.test(id)) {
                    b.delete(id);
                    arqBlocos.seek(endereco);
                    arqBlocos.write(b.toByteArray());
                    return true;
                }

                //avança para o próximo bloco
                endereco = b.next();
            }

            //termo não encontrado
            return false;
        }

        public void print() throws Exception {
            System.out.println("\nLISTAS INVERTIDAS:");

            // Percorre todos os termos
            arqDicionario.seek(4);
            while (arqDicionario.getFilePointer() != arqDicionario.length()) {
                String termo = arqDicionario.readUTF();
                long endereco = arqDicionario.readLong();

                // Percorre a lista deste termo
                ArrayList<ElementoLista> lista = new ArrayList<>();
                Bloco b = new Bloco(quantidadeDadosPorBloco);
                byte[] bd;
                while (endereco != -1) {
                    // Carrega o bloco
                    arqBlocos.seek(endereco);
                    bd = new byte[b.size()];
                    arqBlocos.read(bd);
                    b.fromByteArray(bd);

                    // Acrescenta cada valor à lista
                    ElementoLista[] lb = b.list();
                    for (int i = 0; i < lb.length; i++)
                    lista.add(lb[i]);

                    // Avança para o próximo bloco
                    endereco = b.next();
                }

                // Imprime o termo e sua lista
                System.out.print(termo + ": ");
                lista.sort(null);
                for (int j = 0; j < lista.size(); j++)
                    System.out.print(lista.get(j) + " ");
                System.out.println();
            }
        }

        public void limpar() throws Exception {
            // 1. Limpa o cache (HashMap) da memória
            cacheDicionario.clear();
            
            // 2. Trunca (zera) o tamanho dos arquivos físicos no disco
            arqDicionario.setLength(0);
            arqBlocos.setLength(0);
            
            // 3. Recria o cabeçalho inicial do dicionário
            arqDicionario.seek(0);
            arqDicionario.writeInt(0);
        }

    }



public final class Huffman {

    public static final String NOME = "Huffman";

    private Huffman() {}


    private static final class No implements Comparable<No> {
        final int simbolo;   // 0..255 nas folhas, -1 nos nós internos
        final long freq;
        final int ordem;     // desempate determinístico
        final No esq, dir;

        No(int simbolo, long freq, int ordem, No esq, No dir) {
            this.simbolo = simbolo;
            this.freq = freq;
            this.ordem = ordem;
            this.esq = esq;
            this.dir = dir;
        }

        @Override
        public int compareTo(No o) {
            if (freq != o.freq) return Long.compare(freq, o.freq);
            return Integer.compare(ordem, o.ordem);
        }
    }

    private static No construirArvore(long[] freq) {
        PriorityQueue<No> fila = new PriorityQueue<>();
        for (int s = 0; s < 256; s++) {
            if (freq[s] > 0) fila.add(new No(s, freq[s], s, null, null));
        }
        if (fila.isEmpty()) return null; // arquivo vazio

        // Caso especial: só existe 1 símbolo distinto. Sem pai, o código teria
        // tamanho 0. Criamos um pai falso para o código ser "0" (1 bit).
        if (fila.size() == 1) {
            No unico = fila.poll();
            return new No(-1, unico.freq, 256, unico, null);
        }

        int ordem = 256;
        while (fila.size() > 1) {
            No a = fila.poll();
            No b = fila.poll();
            fila.add(new No(-1, a.freq + b.freq, ordem++, a, b));
        }
        return fila.poll();
    }

    private static void gerarCodigos(No no, long codigo, int tamanho, long[] codigos, int[] tamanhos) {
        if (no == null) return;
        if (no.simbolo >= 0) {
            codigos[no.simbolo] = codigo;
            tamanhos[no.simbolo] = tamanho;
            return;
        }
        if (tamanho >= 63) {
            // Só aconteceria com um arquivo gigantesco e distribuição patológica.
            throw new IllegalStateException("Código de Huffman maior que 63 bits");
        }
        gerarCodigos(no.esq, codigo << 1, tamanho + 1, codigos, tamanhos);
        gerarCodigos(no.dir, (codigo << 1) | 1, tamanho + 1, codigos, tamanhos);
    }

    // compressão

    public static Compressao comprimir(Path origem, Path destino) throws IOException {
        long inicio = System.nanoTime();

        byte[] dados = Files.readAllBytes(origem);

        long[] freq = new long[256];
        for (byte b : dados) freq[b & 0xFF]++;

        No raiz = construirArvore(freq);
        long[] codigos = new long[256];
        int[] tamanhos = new int[256];
        gerarCodigos(raiz, 0L, 0, codigos, tamanhos);

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(destino)))) {
            // cabeçalho do arquivo comprimido
            out.writeInt(dados.length);
            int distintos = 0;
            for (int s = 0; s < 256; s++) if (freq[s] > 0) distintos++;
            out.writeShort(distintos);
            for (int s = 0; s < 256; s++) {
                if (freq[s] > 0) {
                    out.writeByte(s);
                    out.writeInt((int) freq[s]);
                }
            }

            // dados codificados, bit a bit (MSB primeiro)
            int buffer = 0;
            int nBits = 0;
            for (byte b : dados) {
                int s = b & 0xFF;
                long cod = codigos[s];
                for (int i = tamanhos[s] - 1; i >= 0; i--) {
                    buffer = (buffer << 1) | (int) ((cod >>> i) & 1L);
                    if (++nBits == 8) {
                        out.write(buffer);
                        buffer = 0;
                        nBits = 0;
                    }
                }
            }
            if (nBits > 0) out.write(buffer << (8 - nBits)); // completa com zeros
        }

        long fim = System.nanoTime();
        return new Compressao(NOME, Files.size(origem), Files.size(destino), fim - inicio);
    }

    //  descompressão

    public static Compressao descomprimir(Path origem, Path destino) throws IOException {
        long inicio = System.nanoTime();

        byte[] saida;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(origem)))) {
            int tamanhoOriginal = in.readInt();
            int distintos = in.readUnsignedShort();

            long[] freq = new long[256];
            for (int i = 0; i < distintos; i++) {
                int s = in.readUnsignedByte();
                freq[s] = in.readInt() & 0xFFFFFFFFL;
            }

            No raiz = construirArvore(freq);
            saida = new byte[tamanhoOriginal];

            No atual = raiz;
            int escritos = 0;
            while (escritos < tamanhoOriginal) {
                int b = in.read();
                if (b < 0) throw new EOFException("Arquivo comprimido truncado");
                for (int bit = 7; bit >= 0 && escritos < tamanhoOriginal; bit--) {
                    atual = ((b >> bit) & 1) == 0 ? atual.esq : atual.dir;
                    if (atual.simbolo >= 0) {
                        saida[escritos++] = (byte) atual.simbolo;
                        atual = raiz;
                    }
                }
            }
        }

        Files.write(destino, saida);

        long fim = System.nanoTime();
        return new Compressao(NOME, Files.size(origem), Files.size(destino), fim - inicio);
    }
}



public static void comprimirBase() {
    Path dados = Paths.get(caminhoBinario);   // dataBase/jogos.db

    if (!Files.exists(dados)) {
        System.out.println("Arquivo " + caminhoBinario + " não encontrado. Carregue a base primeiro (opção 1).");
        return;
    }

    try {
        int x = AuxCompressao.proximaVersao(dados, "Huffman");
        Path destino = AuxCompressao.nomeComprimido(dados, "Huffman", x);

        Compressao r = Huffman.comprimir(dados, destino);

        System.out.println("\n=== Compressão (versão " + x + ") ===");
        System.out.println("Arquivo gerado: " + destino.getFileName());
        System.out.println(r);

        // ---- TESTE: remover depois que confirmar que funciona ----
        Path tmp = Paths.get(DIR_DADOS + "teste_descomprimido.tmp");
        Compressao d = Huffman.descomprimir(destino, tmp);
        boolean igual = Files.mismatch(dados, tmp) == -1;
        Files.delete(tmp);
        System.out.println("Descompressão: " + d.tempoMs() + " ms");
        System.out.println(igual ? "Verificação: OK (idêntico ao original)" : "Verificação: FALHOU");
        // -----------------------------------------------------------

    } catch (IOException e) {
        System.out.println("Erro ao comprimir: " + e.getMessage());
    }
}


public record Compressao(String algoritmo, long tamanhoEntrada, long tamanhoSaida, long tempoNs) {

    /** Positivo = ganho (arquivo diminuiu). Negativo = perda (arquivo aumentou). */
    public double ganhoPercentual() {
        if (tamanhoEntrada == 0) return 0.0;
        return (1.0 - (double) tamanhoSaida / tamanhoEntrada) * 100.0;
    }

    public double tempoMs() {
        return tempoNs / 1_000_000.0;
    }

    @Override
    public String toString() {
        return String.format("%-8s | %d -> %d bytes | %s%.2f%% | %.3f ms",
                algoritmo, tamanhoEntrada, tamanhoSaida,
                ganhoPercentual() >= 0 ? "ganho " : "perda ", Math.abs(ganhoPercentual()), tempoMs());
    }
}


// Funções auxiliares usadas pelos dois algoritmos (Huffman e LZW).
public static final class AuxCompressao{

    private AuxCompressao() {}

    // Gera o caminho do arquivo comprimido no formato solicitado

    public static Path nomeComprimido(Path original, String algoritmo, int versao) {
        String nome = original.getFileName().toString();
        int ponto = nome.lastIndexOf('.');
        String base = ponto > 0 ? nome.substring(0, ponto) : nome;
        String ext = ponto > 0 ? nome.substring(ponto) : "";
        return original.resolveSibling(base + algoritmo + "Compressao" + versao + ext);
    }

    // Primeira versão X ainda não usada por NENHUM dos algoritmos informados.
    public static int proximaVersao(Path original, String... algoritmos) {
        int versao = 1;
        while (true) {
            boolean usada = false;
            for (String alg : algoritmos) {
                if (Files.exists(nomeComprimido(original, alg, versao))) {
                    usada = true;
                    break;
                }
            }
            if (!usada) return versao;
            versao++;
        }
    }

    // Substitui o arquivo de dados pelo arquivo descomprimido.
    
    public static void substituirArquivoDeDados(Path descomprimido, Path arquivoDeDados) throws IOException {
        Files.move(descomprimido, arquivoDeDados, StandardCopyOption.REPLACE_EXISTING);
    }
}
}