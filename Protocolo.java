import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Protocolo {

  private static final int WAITING = 0;
  private static final int AUTENTICACAO = 1;
  private static final int INSTRUCOES_MENU = 2;
  private static final int MENU = 3;
  private static final int INSTRUCOES_TAREFAS = 4;
  private static final int TAREFAS_MAJOR = 5;
  private static final int VALIDA_TAREFA = 6;
  private static final int TAREFAS_CORONEL = 7;
  private static final int TAREFAS_TENENTE = 8;
  private static final int INSTRUCOES_MENSAGEM = 9;
  private static final int MENSAGEM = 10;

  private int state = WAITING;

  private User currentUser;
  private List<User> users = new ArrayList<>();


  private String[] comandos = {
    "LOGIN",
    "REGISTAR",
    "TAREFAS",
    "MENSAGENS",
    "TAREFASVALID",
    "NEWTAREFA",
    "VERMSG",
    "MSGPRIVADA",
    "MSGGERAL"
  };

  private String[] answers = {
    "Comandos Autenticacao:   LOGIN <nome> <pin>   ||   REGISTAR <nome> <pin> <cargo>   ||   SAIR",
    "\n\nComandos Menu:   \nTAREFAS - menu tarefas \nMENSAGENS - menu mensagens \nLOGOUT - sair",
    "\n\nComandos Tarefas (MAJOR):   \nTAREFASVALID - Aprovar tarefas que necessitam de ser aprovadas \nVER TAREFA - ver todas as tarefas realizadas  \nNEWTAREFA:<mensagem> - criar uma tarefa \nMENU ANTERIOR - voltar atras",
    "\n\nComandos Tarefas (CORONEL):   \nVER TAREFA - ver todas as tarefas realizadas \nNEWTAREFA:<mensagem> - criar uma tarefa \nMENU ANTERIOR - voltar atras",
    "\n\nComandos Tarefas (TENENTE):   \nVER TAREFA - ver todas as tarefas realizadas  \nNEWTAREFA:<mensagem> - criar uma tarefa \nMENU ANTERIOR - voltar atras",
    "\n\nComandos Mensagem:   \nVERMSG - ver mensagens privadas \nJOINCHANNEL:<nome>:<password> - entra num grupo \nNEWCHANNEL:<nome>:<password> - cria um canal \nMSGCHANNEL:<nome>:<password>:<mensagem> - envia mensagem a um canal \nMSGPRIVADA:<destinatario>:<mensagem> - enviar mensagem privada \n",
  };

  public String processInput(String theInput) {
    String theOutput = null;

    if (state == WAITING) {
      theOutput = answers[0];
      state = AUTENTICACAO;

    } else if (state == AUTENTICACAO) {
      if (theInput.startsWith("LOGIN")) {
        String[] tokens = theInput.split(" ",3);
        if (tokens.length == 3) {
          String nome = tokens[1];
          String pin = tokens[2];

          carregarUsers();
          if (verificarCredenciais(nome, pin)) {
            theOutput = "Ola " + nome + " " + currentUser.getCargo() + " !";
            state = INSTRUCOES_MENU;         
          } else {
            theOutput = "Utilizador nao existe tente -> LOGIN <nome> <pin>";
            state = AUTENTICACAO;
          }

        } else {
          state = AUTENTICACAO;
        }

      } else if (theInput.startsWith("REGISTAR")) {
        String[] tokens = theInput.split("\\s+");
        if (tokens.length == 4) {
          String nome = tokens[1];
          String pin = tokens[2];
          String cargo = tokens[3];

          if(validRegister(nome, pin) == true){
            currentUser = new User(nome, pin, cargo);
            registarUtilizador(nome, pin, cargo);
            users.add(currentUser);

            theOutput = "Ola " + nome + " " + currentUser.getCargo() + " !";
            state = INSTRUCOES_MENU;
          } else {
            theOutput = "\nO utilizador ja existe.";
            state = WAITING;
          }

        } else {
          theOutput = "Comando nao reconhecido tente ->   REGISTAR <nome> <pin> <cargo>";
          state = AUTENTICACAO;
        }
      } else if(theInput.startsWith("SAIR")){
        theOutput="Adeus";            
      } else {
        theOutput = "Comando nao reconhecido tente ->   LOGIN <nome> <pin>   ||   REGISTAR <nome> <pin> <cargo>";
        state = AUTENTICACAO;
      }

    } else if (state == INSTRUCOES_MENU){
      theOutput = answers[1];
      state = MENU;
    } else if (state == MENU){
      if(!theInput.isBlank()){
        if(theInput.startsWith("TAREFAS")){
          state = INSTRUCOES_TAREFAS;
        }else if (theInput.startsWith("MENSAGENS")){
          state = INSTRUCOES_MENSAGEM;
        } else if(theInput.startsWith("LOGOUT")){
          theOutput="Adeus " + currentUser.getNome();           
        } else {
          theOutput = "Comando nao reconhecido  tente -> \n" + answers[1];
          state = MENU;
        }
      } else {
        state = INSTRUCOES_MENU;
      }
    } else if (state == INSTRUCOES_TAREFAS) {
        switch (currentUser.getCargo()) {
          case "MAJOR":
            theOutput = answers[2];
            state = TAREFAS_MAJOR;
            break;
          case "CORONEL":
            theOutput = answers[3];
            state = TAREFAS_CORONEL;
            break;
          case "TENENTE":
            theOutput = answers[4];
            state = TAREFAS_TENENTE;
            break;   
        }
    } else if (state == TAREFAS_MAJOR) {
        if (theInput != null){
          if(theInput.startsWith("TAREFASVALID")){
            theOutput = "TAREFAS SEM AUTORIZACAO:";
            state = VALIDA_TAREFA;
          } else if(theInput.startsWith("VER TAREFA")){
            theOutput = currentUser.getNome() + ":VER TAREFAS:";
            state = INSTRUCOES_TAREFAS;            
          } else if(theInput.startsWith("NEWTAREFA")){
            String[] tokens = theInput.split(":", 2);
            if (tokens.length == 2){
              String tarefa = tokens[1];

              theOutput = "TAREFA_MAJOR:" + currentUser.getNome() + ":" + tarefa;
              state = INSTRUCOES_MENU;
            }
            
          } else if(theInput.startsWith("MENU ANTERIOR")){
            state = INSTRUCOES_MENU;            
          } else {
            theOutput = "Comando nao reconhecido tente ->\n" + answers[2];
            state = TAREFAS_MAJOR;
          }
        } else {
          state = INSTRUCOES_TAREFAS;
        }
    } else if (state == VALIDA_TAREFA){
      if(!theInput.isBlank()){
          if(theInput.startsWith("APROVAR")){
            String[] tokens = theInput.split(":");
            if(tokens.length == 3){
              String nome = tokens[1];
              String tarefa = tokens[2];

              theOutput = "APROVAR:" + nome + ":" + tarefa;
            }
          } else {
            theOutput = "Comando nao reconhecido tente -> APROVAR:<nome>:<tarefa>";
          }
      } else {
        state = INSTRUCOES_TAREFAS;
      }
    } else if (state == TAREFAS_CORONEL) {
        if (theInput != null){
          if(theInput.startsWith("NEWTAREFA")){
            String[] tokens = theInput.split(":", 2);
            if (tokens.length == 2){
              String tarefa = tokens[1];

              theOutput = "CORONEL_TAREFA:" + currentUser.getNome() + ":" + tarefa;
              state = INSTRUCOES_MENU;
            }
            
          } else if(theInput.startsWith("VER TAREFA")){
            theOutput = "\n"+ currentUser.getNome() + ":VER TAREFAS:";
            state = INSTRUCOES_TAREFAS;            
          } else if(theInput.startsWith("MENU ANTERIOR")){
            state = INSTRUCOES_MENU;            
          } else {
            theOutput = "Comando nao reconhecido tente ->\n" + answers[3];
            state = TAREFAS_CORONEL;
          }
        }
    } else if (state == TAREFAS_TENENTE) {
        if (theInput != null){
          if(theInput.startsWith("NEWTAREFA")){
            String[] tokens = theInput.split(":", 2);
            if (tokens.length == 2){
              String tarefa = tokens[1];

              theOutput = "TAREFA_TENENTE:" + currentUser.getNome() + ":" + tarefa;
              state = INSTRUCOES_MENU;
            }
            
          } else if(theInput.startsWith("VER TAREFA")){
            theOutput = "\n"+ currentUser.getNome() + ":VER TAREFAS:";
            state = INSTRUCOES_TAREFAS;            
          } else if(theInput.startsWith("MENU ANTERIOR")){
            state = INSTRUCOES_MENU;            
          } else {
            theOutput = "Comando nao reconhecido tente ->\n" + answers[4];
            state = TAREFAS_TENENTE;
          }
        }
    } else if (state == INSTRUCOES_MENSAGEM) {
        if (currentUser.getCargo().equals("MAJOR")){
          theOutput = answers[5] + "MSGGERAL:<mensagem> - enviar mensagem geral\n" + currentUser.getCargo() + ":<mensagem> - enviar mensagem para o grupo " + currentUser.getCargo() + "\nMENU ANTERIOR - voltar atras";
        } else {   
          theOutput = answers[5] + currentUser.getCargo() + ":<mensagem> - enviar mensagem para o grupo " + currentUser.getCargo() + "\nMENU ANTERIOR - voltar atras";
        }
        state = MENSAGEM;
    } else if (state == MENSAGEM) {
      if(!theInput.isBlank()){
        if (theInput.startsWith("VERMSG")) {
          theOutput = currentUser.getNome() + ":MENSAGENS PRIVADAS:";
          state=INSTRUCOES_MENSAGEM;
          
        } else if (theInput.startsWith("JOINCHANNEL")) {
          String[] tokens = theInput.split(":");
          if (tokens.length == 3) {
              String nome = tokens[1];
              String password = tokens[2];

              theOutput = "JOINCHANNEL" + ":" + currentUser.getNome() + ":" + nome + ":" + password;
              state = INSTRUCOES_MENSAGEM;
          } else {
              theOutput = "Comando nao reconhecido tente ->   JOINCHANNEL:<nome>:<password> ";
              state = MENSAGEM;
          }
        } else if (theInput.startsWith("NEWCHANNEL")) {
          String[] tokens = theInput.split(":");
          if (tokens.length == 3) {
              String nome = tokens[1];
              String password = tokens[2];

              theOutput = "NEWCHANNEL" + ":" + currentUser.getNome() + ":" + nome + ":" + password;
              state = INSTRUCOES_MENSAGEM;
          } else {
              theOutput = "Comando nao reconhecido tente ->   NEWCHANNEL:<nome>:<password> ";
              state = MENSAGEM;
          }
        } else if (theInput.startsWith("MSGCHANNEL")) {
          String[] tokens = theInput.split(":");
          if (tokens.length == 4) {
              String nome = tokens[1];
              String password = tokens[2];
              String message = tokens[3];

              theOutput = "MSGCHANNEL" + ":" + currentUser.getNome() + ":" + nome + ":" + password + ":" + message;
              state = INSTRUCOES_MENSAGEM;
          } else {
              theOutput = "Comando nao reconhecido tente ->   NEWCHANNEL:<nome>:<password> ";
              state = MENSAGEM;
          }
        } else if (theInput.startsWith("MSGPRIVADA")) {
          String[] tokens = theInput.split(":");
          if (tokens.length == 3) {
              String destinatario = tokens[1];
              String mensagem = tokens[2];
              theOutput = "MSGPRIVADA" + ":" + currentUser.getNome() + ":" + destinatario + ":" + mensagem;
              state = INSTRUCOES_MENSAGEM;
          } else {
              theOutput = "Comando nao reconhecido tente ->   MSGPRIVADA:<destinatário>:<mensagem> ";
              state = MENSAGEM;
          }
        } else if (theInput.startsWith("MSGGERAL")) {
          String[] tokens = theInput.split(":");
          if (tokens.length == 2) {
            String message = tokens[1];
            theOutput = "MSGGERAL" + ":" + currentUser.getNome() + ":" + message;
            state = INSTRUCOES_MENSAGEM;
          } else {
            theOutput = "Comando desconhecido tente ->   MSGGERAL:<mensagem>";
            state = MENSAGEM;
          }
        } else if (theInput.startsWith(currentUser.getCargo())) {
          String[] tokens = theInput.split(":");
          if (tokens.length == 2) {
            String message = tokens[1];
            theOutput = currentUser.getCargo() + ":" + currentUser.getNome() + ":" + message;
            state = INSTRUCOES_MENSAGEM;
          } else {
            theOutput = "Comando desconhecido tente ->   "+ currentUser.getCargo() +":<mensagem>";
            state = MENSAGEM;
          }
        } else if(theInput.startsWith("MENU ANTERIOR")){
          state = INSTRUCOES_MENU;            
        } else {
          theOutput = "Comando desconhecido tente ->   " + answers[5] + currentUser.getCargo() + ":<mensagem>";
          state = MENSAGEM;
        }
      }
    }
    return theOutput;
  }

  public boolean validRegister(String nome, String pin){
    for (int i = 0; i < users.size(); i++) {
      if(users.get(i).getNome().equals(nome) && users.get(i).getPin().equals(pin)){
        return false;
      }
    }
    return true;
  }

  public User registarUtilizador(String nome, String pin, String cargo) {
    if(cargo.toUpperCase().equals("MAJOR") || cargo.toUpperCase().equals("CORONEL") || cargo.toUpperCase().equals("TENENTE")){
      User newUser = new User(nome, pin, cargo.toUpperCase());
      GuardaUserNoArquivo(newUser);
      return newUser;
    }
    return null;
  }

  private void GuardaUserNoArquivo(User user) {
    try (
      BufferedWriter writer = new BufferedWriter(
        new FileWriter("users.txt", true)
      )
    ) {
      String dadosUser = String.format(
        "%s,%s,%s",
        user.getNome(),
        user.getPin(),
        user.getCargo()
      );
      writer.write(dadosUser);
      writer.newLine();
    } catch (IOException e) {
      System.err.println("Erro ao salvar o user no arquivo: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void carregarUsers() {
    try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if (userData.length == 3) {
                String nome = userData[0];
                String pin = userData[1];
                String cargo = userData[2];
                users.add(new User(nome, pin, cargo));
            }
        }
    } catch (IOException e) {
        System.err.println("Erro ao carregar users: " + e.getMessage());
        e.printStackTrace();
    }
}

  public boolean verificarCredenciais(String nome, String pin) {
      for (User user : users) {
          if (user.getNome().equals(nome) && user.getPin().equals(pin)) {
              currentUser = user;
              return true; 
          }
      }
      return false;
  }
}
