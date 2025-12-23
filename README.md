# Gestão de Estacionamento - Jump Park

Aplicativo Android de gestão de estacionamento desenvolvido em Kotlin usando Clean Architecture, MVVM e SOLID principles.

## Tecnologias Utilizadas

- **Kotlin** - Linguagem de programação
- **Kotlin Multiplatform (KMP)** - Compartilhamento de código entre Android e Web
- **Jetpack Compose** - Framework de UI
- **Room Database** - Banco de dados local (Android)
- **Ktor** - Cliente HTTP para requisições à API
- **Koin** - Injeção de dependências
- **Coroutines & Flow** - Programação assíncrona
- **Navigation Compose** - Navegação entre telas
- **Material Design 3** - Design system
- **MockK** - Framework de mocking para testes
- **Turbine** - Biblioteca para testar Flows

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

## Testes Unitários

O projeto possui uma suíte de testes unitários cobrindo os principais componentes da aplicação. Atualmente, **27 testes estão passando** com sucesso.

### Testes Implementados e Funcionais

1. **CalculateParkingFeeUseCaseTest** (8 testes) - Testa a lógica de cálculo de tarifas de estacionamento:
   - Cálculo com tolerância inicial
   - Aplicação de regras "até" e "a partir de"
   - Cálculo de períodos adicionais
   - Aplicação de valor máximo
   - Arredondamento de períodos parciais

2. **LoginViewModelTest** (8 testes) - Testa a lógica de autenticação:
   - Inicialização com estado vazio
   - Atualização de email e senha
   - Validação de campos obrigatórios (email e senha vazios)
   - Fluxo de login bem-sucedido
   - Tratamento de erros de autenticação
   - Limpeza de erros
   - Remoção de espaços em branco do email

3. **VehicleEntryViewModelTest** (7 testes) - Testa a lógica de entrada de veículos:
   - Inicialização com estado vazio
   - Atualização de placa e conversão para maiúsculas
   - Atualização de modelo e cor
   - Seleção de tabela de preços
   - Carregamento de tabelas de preços do repositório
   - Remoção de espaços em branco dos campos ao registrar

4. **VehicleDetailViewModelTest** (11 testes) - Testa a lógica de detalhes e saída de veículos:
   - Inicialização com estado vazio
   - Carregamento de veículo e cálculo de valor
   - Exibição de erro quando veículo não é encontrado
   - Recalculo de valor
   - Seleção de método de pagamento
   - Validação de método de pagamento na saída
   - Processamento de saída com sucesso
   - Formatação de data e hora
   - Limpeza de erros
   - Reset de estado de sucesso
   - Permissão de saída com valor zero (dentro da tolerância)

### Executando os Testes

Para executar todos os testes unitários no Android Studio:
1. Clique com o botão direito na pasta `app/src/test`
2. Selecione "Run 'Tests in 'test''"

Ou via linha de comando:
```bash
./gradlew test
```

Para executar um teste específico:
```bash
./gradlew test --tests "com.example.gestodeestacionamento.domain.usecase.CalculateParkingFeeUseCaseTest"
```

### Dependências de Teste

- **JUnit 4** - Framework de testes
- **MockK** - Mocking framework para Kotlin
- **Turbine** - Teste de Flows
- **kotlinx-coroutines-test** - Teste de coroutines
- **androidx.arch.core:core-testing** - Teste de ViewModels

## Kotlin Multiplatform (KMP)

O projeto está configurado para suportar Kotlin Multiplatform, permitindo compartilhar código entre Android e Web.

### Estrutura KMP

```
app/src/
├── commonMain/          # Código compartilhado entre plataformas
│   └── kotlin/.../platform/  # Abstrações expect/actual
├── androidMain/         # Implementações específicas do Android
│   └── kotlin/.../platform/  # Implementações Android
└── jsMain/             # Implementações específicas da Web
    └── kotlin/.../platform/  # Implementações Web
```

### Abstrações Expect/Actual Implementadas

1. **PlatformStorage** - Armazenamento local:
   - Android: DataStore
   - Web: LocalStorage

2. **DatabaseFactory** - Factory de banco de dados:
   - Android: Room Database
   - Web: LocalStorage (simplificado)

3. **Navigation** - Navegação:
   - Android: Navigation Compose
   - Web: Navigation Compose

4. **ViewModel** - ViewModels:
   - Android: Koin ViewModel
   - Web: Koin ViewModel

### Executando no Android

```bash
./gradlew :app:assembleDebug
```

### Executando na Web

```bash
# Build para desenvolvimento
./gradlew :app:jsBrowserDevelopmentWebpack

# Build para produção
./gradlew :app:jsBrowserProductionWebpack
```

Os arquivos gerados estarão em:
- Desenvolvimento: `app/build/dist/js/developmentExecutable/`
- Produção: `app/build/dist/js/productionExecutable/`

Para mais detalhes sobre a migração KMP, consulte:
- `KMP_MIGRATION_GUIDE.md` - Guia de migração
- `README_KMP.md` - Documentação completa do KMP

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

## FOTOS DO PROJETO ANDROID

<img width="300" height="700" alt="Screenshot_20251220_002039" src="https://github.com/user-attachments/assets/e20fd059-150b-4d2b-9649-7b79e82c0ecc" />
<img width="300" height="700" alt="Screenshot_20251220_192254" src="https://github.com/user-attachments/assets/18531f2c-212e-4cd9-8073-4b1a701139f5" />
<img width="300" height="700" alt="Screenshot_20251220_192241" src="https://github.com/user-attachments/assets/65eb322d-b871-490d-9d6a-90e5df0be966" />
<img width="300" height="700" alt="Screenshot_20251220_192232" src="https://github.com/user-attachments/assets/acc2d534-13f0-472b-957a-981940f0acfa" />
<img width="300" height="700" alt="Screenshot_20251220_202706" src="https://github.com/user-attachments/assets/212b5dfb-da13-42f5-9df4-2b6be1f8281f" />
<img width="300" height="700" alt="Screenshot_20251220_202908" src="https://github.com/user-attachments/assets/dfba2a20-dbdc-4948-ad45-70c12f1c6435" />
<img width="300" height="700" alt="Screenshot_20251220_002051" src="https://github.com/user-attachments/assets/609727d8-c4bd-4fb5-8d4c-db8809d9cfa5" />
<img width="600" height="700" alt="Screenshot_1" src="https://github.com/user-attachments/assets/a8d927a7-e6f9-414c-8b2b-2dbfdd61e28b" />
<img width="400" height="100" alt="image" src="https://github.com/user-attachments/assets/909bc89a-5df8-45c2-8f34-f4068f9c0646" />

