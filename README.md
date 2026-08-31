Trabalho Prático de AEDS 3 
Base de dados usada: Jogos da Steam
Alunos: 
- Eduardo Henrique Mendes Torres
- Kairo Viana de Paula

Professor:
- Hayala Nepomuceno Curto



  *ESTRUTURA DE REGISTRO*
[ Cabeçalho: 4 bytes (Último ID) ]
(Início da sequência de jogos)
  ├── [ Lápide: 1 byte (boolean) ]             <-- gravado por raf.writeBoolean(false)
  ├── [ Tamanho do Registro: 4 bytes (int) ]   <-- gravado por raf.writeInt(ba.length)
  └── [ Registro Serializado: N bytes ]        <-- gravado por raf.write(ba)
       ├── ID: 4 bytes (int)
       ├── Nome: 2 bytes (tamanho) + 100 bytes (texto UTF-8 fixo)
       ├── Lançamento: 8 bytes (long )
       ├── Preço: 4 bytes (float)
       ├── Gêneros: 2 bytes (tamanho) + P bytes (texto UTF-8)
       └── Descrição: 2 bytes (tamanho) + 100 bytes (texto UTF-8) - Fixo de até 100 caracteres
