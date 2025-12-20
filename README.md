# Gestão de Estacionamento - Jump Park

Aplicativo Android de gestão de estacionamento desenvolvido em Kotlin usando Clean Architecture, MVVM e SOLID principles.

## Tecnologias Utilizadas

- **Kotlin** - Linguagem de programação
- **Jetpack Compose** - Framework de UI
- **Room Database** - Banco de dados local
- **Ktor** - Cliente HTTP para requisições à API
- **Koin** - Injeção de dependências
- **Coroutines & Flow** - Programação assíncrona
- **Navigation Compose** - Navegação entre telas
- **Material Design 3** - Design system

## Arquitetura

O projeto segue os princípios de **Clean Architecture** com as seguintes camadas:

- **Presentation**: ViewModels e Telas (Compose)
- **Domain**: Use Cases, Models e Interfaces de Repositórios
- **Data**: Implementação de Repositórios, API Service, Database e Mappers

## Funcionalidades

- ✅ Login de usuário
- ✅ Tela inicial com estatísticas (veículos no pátio e pagamentos)
- ✅ Entrada de veículo
- ✅ Lista de veículos no pátio
- ✅ Detalhamento e saída de veículo com cálculo automático de valor
- ✅ Encerramento de sessão de trabalho
- ✅ Sincronização com API Operacional
- ✅ Persistência local de dados

## Configuração do Ambiente

### Pré-requisitos

- Android Studio Narwhal Feature Drop | 2025.1.2 Patch 2 ou superior
- JDK 11 ou superior
- Android SDK 24+ (minSdk)
- Emulador Android ou dispositivo físico

### Passos para Executar

1. **Clone o repositório**
   ```bash
   git clone <url-do-repositório>
   cd GestodeEstacionamento
   ```

2. **Abra o projeto no Android Studio**
   - File > Open > Selecione a pasta do projeto

3. **Sincronize as dependências**
   - O Gradle irá baixar automaticamente todas as dependências

4. **Configure a conta de usuário**
   - Acesse: https://teste-admin.jumppark.com.br
   - Crie uma conta de usuário
   - Certifique-se de ter apenas **um estabelecimento** vinculado à conta
   - No estabelecimento, crie ao menos **uma tabela de estacionamento** (Negócio > Tabela de estacionamento)

5. **Execute o aplicativo**
   - Conecte um dispositivo ou inicie um emulador
   - Clique em Run (Shift+F10) ou use o botão de play

## Como Testar

### 1. Login
- Abra o aplicativo
- Informe o email e senha da conta criada no site
- Clique em "Entrar"
- O aplicativo irá automaticamente sincronizar os dados (tabelas de preços e formas de pagamento)

### 2. Tela Inicial (Home)
- Visualize o total de veículos no pátio
- Visualize os pagamentos agrupados por forma de pagamento
- Visualize o total geral de pagamentos
- Use o botão de sincronização (ícone de refresh) para atualizar os dados da API

### 3. Entrada de Veículo
- Clique em "Entrada de Veículo"
- Preencha:
  - Placa (ex: ABC1234)
  - Modelo (ex: Honda Civic)
  - Cor (ex: Branco)
  - Selecione uma tabela de preços
- Clique em "Cadastrar Entrada"
- O veículo será salvo localmente e aparecerá na lista

### 4. Lista de Veículos no Pátio
- Clique em "Lista de Veículos no Pátio"
- Visualize todos os veículos que estão no pátio
- Clique em um veículo para ver os detalhes

### 5. Saída de Veículo
- Na tela de detalhes do veículo:
  - Visualize as informações do veículo
  - O valor da estadia será calculado automaticamente baseado na tabela de preços
  - Selecione uma forma de pagamento
  - Clique em "Dar Saída"
  - Confirme a ação no diálogo
  - O veículo será removido do pátio e o pagamento será registrado

### 6. Encerrar Sessão
- Na tela inicial, clique em "Encerrar Sessão"
- Confirme a ação no diálogo
- Todos os dados locais serão apagados
- Você será deslogado e retornará à tela de login

## Estrutura do Banco de Dados

O banco de dados local utiliza Room e possui as seguintes tabelas:

- **vehicles**: Armazena informações dos veículos
- **price_tables**: Armazena as tabelas de preços sincronizadas da API
- **payment_methods**: Armazena as formas de pagamento sincronizadas da API
- **payments**: Armazena os pagamentos realizados

Veja o diagrama UML completo em `DATABASE_UML.md`

## API

O aplicativo se integra com a API Operacional da Jump Park:

- **Base URL**: `https://dev.app.jumpparkapi.com.br/api`
- **Endpoints utilizados**:
  - `POST /user/login` - Login de usuário
  - `GET /{userId}/establishment/{establishmentId}/sync/manual` - Sincronização manual
  - `POST /{userId}/establishment/{establishmentId}/session/close/{sessionId}` - Encerrar sessão

## Cores do Tema

O aplicativo utiliza as cores da identidade visual da Jump Park:
- **Verde**: `#4CAF50` (Primary)
- **Azul Escuro**: `#1A237E` (Secondary)
- **Branco**: `#FFFFFF` (Background)

## Geração de APK

Para gerar o APK de debug:

```bash
./gradlew assembleDebug
```

O APK estará em: `app/build/outputs/apk/debug/app-debug.apk`

Para gerar o APK de release:

```bash
./gradlew assembleRelease
```

O APK estará em: `app/build/outputs/apk/release/app-release.apk`

## Observações Importantes

1. **Sessão Única**: A API permite apenas uma sessão aberta por usuário em um estabelecimento. Se houver problemas, encerre a sessão diretamente no site em "Registro de sessões > Encerrar a sessão".

2. **Estabelecimento Único**: Para o funcionamento correto do teste, a conta deve ter apenas um estabelecimento vinculado.

3. **Tabela de Preços**: É necessário ter ao menos uma tabela de estacionamento criada no estabelecimento antes de usar o aplicativo.

4. **Dados Locais**: Todos os veículos e pagamentos são armazenados localmente. Ao encerrar a sessão, todos os dados são apagados.

## Conta de Teste

**Email**: [guga.santospadilha@gmail.com]  
**Senha**: [av03dguug]

## Diagrama UML do Banco de Dados

Consulte o arquivo `DATABASE_UML.md` para visualizar o diagrama completo do banco de dados.

