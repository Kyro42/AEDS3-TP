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

> **[Assista ao vídeo de demonstração e explicação do código no YouTube](https://youtu.be/BwO3Ldon7v0)**

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
