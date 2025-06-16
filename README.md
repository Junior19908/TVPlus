# TVPlus - Aplicativo de Streaming de TV Ao Vivo (Android)

Bem-vindo ao TVPlus, um aplicativo Android para assistir canais de TV ao vivo via streaming. Este projeto foi desenvolvido como um exemplo de aplicativo de IPTV para a plataforma Android, com foco em aprendizado e demonstração de funcionalidades comuns em aplicativos de mídia.

## Funcionalidades Implementadas

*   **Navegação por Grade de Canais:** Exibição dos canais disponíveis em uma grade visual.
*   **Player de Vídeo Integrado:** Utiliza o ExoPlayer para reprodução dos streams de vídeo HLS (`.m3u8`).
*   **Tela de Splash:** Tela de inicialização seguindo as diretrizes do Android.
*   **Autenticação de Usuário (Firebase):**
    *   Login com Chave de Acesso Ex.:TVPLUS-BAF2-B539-1F46.
    *   Sessão de usuário com expiração (atualmente configurada para 48 horas, exigindo novo login).
*   **Permissão de Tráfego HTTP:** Configurado para permitir tráfego HTTP para domínios específicos de streaming (via `network_security_config.xml`).
*   **(Opcional, se implementado) Categorias de Canais:** Organização dos canais por categorias.
*   **(Opcional, se implementado) Busca de Canais:** Funcionalidade para buscar canais.
*   **(Para Teste) Exibição do Tempo Restante para Login:** Mostra um contador regressivo na tela principal indicando o tempo até o próximo login obrigatório.

## Tecnologias e Bibliotecas Utilizadas

*   **Linguagem:** Kotlin
*   **Arquitetura:** (Descreva brevemente sua arquitetura, ex: MVVM, MVI, ou "baseada em Fragments e Activities")
*   **Interface do Usuário:**
    *   Jetpack Compose (para a interface principal de canais e player, se aplicável)
    *   XML Layouts (para telas como Login, Splash, etc.)
*   **Player de Mídia:** ExoPlayer (Media3)
*   **Autenticação:** Firebase Authentication (E-mail/Senha)
*   **Comunicação com Rede:** (Mencione bibliotecas como Retrofit/OkHttp se usar para buscar listas de canais de um backend, ou apenas "Conexões HTTP diretas" se for o caso)
*   **Carregamento de Imagens:** Coil (para carregar as miniaturas dos canais)
*   **Componentes Android Jetpack:**
    *   `SplashScreen API` (`androidx.core:core-splashscreen`)
    *   `ViewModel` (Se estiver usando)
    *   `LiveData` ou `StateFlow` (Se estiver usando para gerenciamento de estado)
    *   `Navigation Component` (Se estiver usando para navegação entre telas)
    *   `SharedPreferences` (Para armazenar o timestamp do login)
*   **Outras:**
    *   Firebase BoM

## Configuração do Projeto

Siga os passos abaixo para configurar e executar o projeto em seu ambiente de desenvolvimento.

### Pré-requisitos

*   Android Studio (versão mais recente recomendada, ex: Iguana | 2023.2.1 ou superior)
*   JDK (versão compatível com o Android Studio)
*   Dispositivo Android ou Emulador (API nível XX ou superior - *Especifique o `minSdkVersion`*)

### Passos para Configuração

1.  **Clonar o Repositório:**

2.  2.  **Configurar o Firebase:**
    *   Crie um projeto no [Firebase Console](https://console.firebase.google.com/).
    *   Adicione um aplicativo Android ao seu projeto Firebase com o nome do pacote: `com.example.livetv` (ou o `applicationId` real do seu projeto).
    *   Faça o download do arquivo `google-services.json` gerado pelo Firebase.
    *   Copie o arquivo `google-services.json` para o diretório `app/` do seu projeto Android.
    *   No Firebase Console, vá para "Authentication" -> "Sign-in method" e habilite o provedor "E-mail/Senha".

3.  **Configurar Chaves de API/Variáveis de Ambiente (se houver):**
    *   *(Se você tiver chaves de API além do Firebase, como para um serviço de lista de canais, descreva como configurá-las. Por exemplo, em `local.properties` ou como constantes no código - embora não seja o ideal para chaves sensíveis).*
    *   Atualmente, o projeto não requer chaves de API adicionais além da configuração do Firebase.

4.  **Abrir no Android Studio:**
    *   Abra o Android Studio.
    *   Selecione "Open an Existing Project" e navegue até a pasta do projeto clonado.
    *   Aguarde o Android Studio sincronizar e construir o projeto.

5.  **Executar o Aplicativo:**
    *   Selecione um dispositivo ou emulador compatível.
    *   Clique no botão "Run" (ícone de play verde) no Android Studio.

## Estrutura do Projeto (Opcional, mas útil)

Descreva brevemente a organização das pastas e pacotes principais:

*   `app/src/main/java/com/skysinc/tvplus/`: Código fonte principal.
    *   `ui/`: Classes relacionadas à interface do usuário (Activities, Fragments, Composables).
        *   `LoginActivity.kt`
        *   `HomeActivity.kt`
        *   `MainFragment.kt`
        *   `PlayerActivity.kt`
        *   `SplashActivity.kt` (Se ainda existir)
    *   `model/` (ou `data/model/`): Classes de modelo de dados (ex: `Channel.kt`).
    *   `player/` (ou `videoplayer/`): Componentes relacionados ao player de vídeo.
    *   `auth/` (ou `firebase/`): Classes relacionadas à autenticação.
    *   `utils/`: Classes utilitárias.
*   `app/src/main/res/`: Recursos do aplicativo.
    *   `layout/`: Arquivos de layout XML.
    *   `drawable/`: Imagens e drawables.
    *   `mipmap/`: Ícones do aplicativo.
    *   `values/`: Strings, cores, dimensões, temas.
    *   `xml/`: Configurações XML (ex: `network_security_config.xml`).
*   `app/build.gradle`: Configurações de build do módulo do aplicativo (dependências, plugins).
*   `google-services.json`: Arquivo de configuração do Firebase.

## Como Usar o Aplicativo

1.  Ao iniciar o aplicativo, você será direcionado para a tela de Login.
2.  Crie uma nova conta usando um e-mail e senha válidos ou faça login com uma conta existente.
3.  Após o login, a tela principal exibirá a lista de canais disponíveis.
4.  Clique em um canal para iniciar a reprodução do stream.
5.  A sessão de login é válida por 48 horas. Após esse período, você precisará fazer login novamente.
6.  (Para fins de teste) Um contador regressivo na tela principal indica o tempo restante até o próximo login.

## Contribuições (Opcional)

Se este fosse um projeto de código aberto, você poderia adicionar informações sobre como contribuir:

*Como este é um projeto pessoal/de aprendizado, contribuições não estão sendo aceitas no momento.* (Ou, se você quiser, descreva como as pessoas podem contribuir, como abrir issues, pull requests, etc.)

## Problemas Conhecidos e Melhorias Futuras

*   **(Exemplo) A lista de canais é atualmente fixa no código. Idealmente, deveria ser carregada de um backend/API.**
*   **(Exemplo) Melhorar o tratamento de erros de rede e de reprodução do player.**
*   **(Exemplo) Adicionar funcionalidade de "Canais Favoritos".**
*   **(Exemplo) Implementar busca e filtragem de canais mais robusta.**
*   **(Exemplo) Refatorar a lógica de gerenciamento de estado usando ViewModels e StateFlow/LiveData de forma mais consistente.**

---

Desenvolvido por CARLOS JÚNIOR - Tel.(82) 9 9962-3274
