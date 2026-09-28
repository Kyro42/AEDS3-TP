# Trabalho Prático - Steam Games Database

**Alunos:**
* Eduardo Henrique Mendes Torres
* Kairo Viana de Paula

**Professor:**
* Hayala Nepomuceno Curto

**Instituição:** PUC Minas  
**Curso:** Ciência da Computação  

---

## Apresentação do Projeto

> **[Assista ao vídeo de demonstração e explicação do código no YouTube - TP1](https://youtu.be/BwO3Ldon7v0)**

> **[Assista ao vídeo de demonstração e explicação do código no YouTube - TP2](https://youtu.be/XZ43-Km3MD0)**

---

## Sobre o Projeto
Este projeto consiste na implementação de um sistema de gerenciamento de banco de dados baseado em arquivos para um catálogo de jogos da Steam. O programa realiza a carga inicial lendo dados de um arquivo estruturado `.csv`, converte essas informações para um arquivo binário (`jogos.db`) e permite a realização de operações de gerenciamento e pesquisa.

## Funcionalidades Implementadas
- **Carga de Dados:** Processamento do arquivo `steam_games.csv` e escrita estruturada no arquivo binário.
- **Operações CRUD:**
  - **Create (Criação):** Inserção de novos registros de jogos no final do arquivo binário, garantindo um novo ID sequencial.
  - **Read (Leitura):** Busca de um jogo específico através do seu ID, lendo os bytes do arquivo de forma sequencial.
  - **Update (Atualização):** Alteração dos dados de um jogo existente. Caso o novo registro seja maior que o antigo, ele é realocado para o final do arquivo.
  - **Delete (Remoção):** Remoção lógica de um registro utilizando uma marcação (flag/lápide), evitando a necessidade de reescrever todo o arquivo.


Tamanho Fixo Constante: Para permitir a navegação algorítmica pelo disco via seek(), o tamanho dos nós (arrays de IDs, registros e ponteiros) é estritamente fixo. Espaços vazios dentro dos nós são preenchidos com -1.

## Estrutura de Índice: Árvore B 
Para resolver o gargalo de I/O (leitura/escrita no disco rígido) inerente à busca sequencial, implementamos uma Árvore B persistida fisicamente em disco.

A Escolha da Ordem (Ordem 50): O desempenho do RandomAccessFile é fortemente impactado pela arquitetura do Sistema Operacional, que lê discos em blocos (clusters) de 4KB. Ao definir a Ordem 50, cada nó da nossa Árvore B possui um tamanho serializado fixo de aproximadamente 1 KB. Isso garante que a leitura de uma página da árvore caiba perfeitamente dentro de um bloco nativo do disco, evitando fragmentação e travamentos (congelamentos) de I/O durante divisões (splits).

### Índice Secundário: Lista Invertida (Pesquisa Textual)
Para garantir uma busca full-text (pesquisa por fragmentos do título do jogo, simulando mecanismos de busca comuns), foi implementada uma estrutura de *Lista Invertida Booleana*, distribuída arquiteturalmente em dois arquivos para evitar fragmentação:
* *Arquivo de Dicionário:* Responsável por guardar os termos isolados (ex: zelda, legend, mario) extraídos dos títulos lidos em lower-case e sanitizados.
* *Arquivo de Blocos (Cestos):* Armazena sequências puras de IDs (4 bytes) vinculadas às ocorrências daqueles termos. Foram adotados blocos encadeados, limitados a guardar 500 registros cada. O dimensionamento foi pensado para gerar "pacotes" (blocos) em torno de *4 Kilobytes*, minimizando desperdício computacional ao espelhar o tamanho de página ideal lido nativamente pelos Sistemas Operacionais modernos.
* *Warm-up Cache (RAM):* O maior impacto no tempo de carregamento da base ocorria na verificação se uma determinada palavra já constava no Dicionário em disco. Para resolver o problema, o algoritmo executa um cache imediato via HashMap na inicialização do sistema, espelhando todo o Dicionário em memória. A supressão das repetidas varreduras físicas de busca fez o tempo de indexação global da base despencar drasticamente.

##  Algoritmo de Ordenação
O sistema implementa a ordenação dos registros armazenados no arquivo binário. Como a manipulação direta em disco é custosa e lenta em comparação com a memória principal, a estratégia de ordenação foi dividida em etapas (Ordenação Externa):

1. **Geração de Blocos Ordenados:** O algoritmo lê os registros do arquivo binário principal em blocos que cabem na memória RAM. Esses registros são ordenados internamente (utilizando a chave de busca, como o ID) e gravados em arquivos temporários.
2. **Intercalação:** O sistema aplica um processo de intercalação balanceada. Ele abre os arquivos temporários gerados na etapa anterior, compara os primeiros elementos de cada arquivo e grava o menor valor no arquivo de saída, avançando os ponteiros de leitura sequencialmente.
3. **Resultado:** O processo se repete até que todos os registros estejam combinados e ordenados em um único e novo arquivo binário, substituindo a base de dados original de forma otimizada.

##  Como Executar

1. Certifique-se de ter o **Java** instalado e configurado nas variáveis de ambiente.
2. O projeto deve possuir a seguinte estrutura de diretórios:
   ```text
   raiz_do_projeto/
   ├── codigo/
   │   └── TP1.java (e outras classes)
   └── database/
       └── steam_games.csv

##  ESTRUTURA DE REGISTRO
```text
[ Cabeçalho: 4 bytes (Último ID) ] 
(Início da sequência de jogos) 
  ├── [ Lápide: 1 byte (boolean) ]             <-- gravado por raf.writeBoolean(false) 
  ├── [ Tamanho do Registro: 4 bytes (int) ]   <-- gravado por raf.writeInt(ba.length) 
  └── [ Registro Serializado: N bytes ]        <-- gravado por raf.write(ba) 
       ├── ID: 4 bytes (int) 
       ├── Nome: 2 bytes (tamanho) + 100 bytes (texto UTF-8 fixo) - Fixo de até 100 caracteres
       ├── Lançamento: 8 bytes (long) 
       ├── Preço: 4 bytes (float) 
       ├── Gêneros: 2 bytes (tamanho) + P bytes (texto UTF-8) 
       └── Descrição: 2 bytes (tamanho) + M bytes (texto UTF-8) 
