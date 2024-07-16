# Cofre Digital

## Descrição do Projeto

O projeto Cofre Digital é um sistema desenvolvido em Java para proteger uma pasta de arquivos secretos, utilizando um banco de dados relacional MySQL, autenticação forte bifator, e controle de integridade, autenticidade e sigilo.

## Funcionalidades Principais

- **Cadastro do Administrador**: Na primeira execução, realiza o cadastro do usuário administrador.
- **Autenticação de Usuários**: Implementa autenticação bifator utilizando senha pessoal e token do Google Authenticator.
- **Proteção de Arquivos**: Garante a integridade, autenticidade e sigilo dos arquivos secretos armazenados na pasta protegida.
- **Controle de Acesso**: Define políticas de acesso baseadas nos privilégios do usuário.

## Banco de Dados

O banco de dados é organizado em cinco tabelas principais:

1. **Usuarios**: Armazena informações pessoais e credenciais dos usuários.
2. **Chaveiro**: Armazena os pares de chave privada e certificado digital dos usuários.
3. **Grupos**: Armazena os grupos do sistema.
4. **Mensagens**: Armazena mensagens do sistema para fins de registro.
5. **Registros**: Armazena logs das operações realizadas no sistema.

## Processo de Autenticação

1. **Primeira Etapa**: Solicita a identificação do usuário através de um e-mail válido.
2. **Segunda Etapa**: Verifica a senha pessoal utilizando um teclado virtual numérico.
3. **Terceira Etapa**: Verifica o token gerado pelo Google Authenticator.


### Controle de Acesso

- **Arquivo Index**: Contém as informações dos arquivos secretos, criptografado com AES e assinado digitalmente.
- **Política de Acesso**: O usuário só pode acessar arquivos dos quais é o dono.


## Ferramentas e Bibliotecas Utilizadas

- **JDK SE 1.8.0**
- **BouncyCastle**: Para manipulação de criptografia.
- **javax.crypto**: Para operações criptográficas.
- **Google Authenticator**: Para geração de tokens TOTP.
