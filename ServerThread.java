import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerThread extends Thread {

  private Socket socket = null;
  private PrintWriter out;

  //private static Map<Canal, PrintWriter> channels = new HashMap<>();
  private static List<Canal> channels = new ArrayList<>();

  private InetAddress geralAddress;
  private MulticastSocket geralSocket;
  private static Map<String, PrintWriter> allUsers = new HashMap<>();
  private String geralAddressString;
  private int geralPort;

  private InetAddress majorAddress;
  private MulticastSocket majorSocket;
  private static Map<String, PrintWriter> majorGroup = new HashMap<>();
  private String majorAddressString;
  private int majorPort;

  private InetAddress coronelAddress;
  private MulticastSocket coronelSocket;
  private static Map<String, PrintWriter> coronelGroup = new HashMap<>();
  private String coronelAddressString;
  private int coronelPort;

  private InetAddress tenenteAddress;
  private MulticastSocket tenenteSocket;
  private static Map<String, PrintWriter> tenenteGroup = new HashMap<>();
  private String tenenteAddressString;
  private int tenentePort;

  public ServerThread(Socket socket) {
    super("ServerThread");
    this.socket = socket;

    try {
      geralAddressString = "230.0.0.1";
      geralPort = 2222;
      geralSocket = new MulticastSocket(geralPort);
      geralAddress = InetAddress.getByName(geralAddressString);

      majorAddressString = "230.0.0.2";
      majorPort = 2223;
      majorSocket = new MulticastSocket(majorPort);
      majorAddress = InetAddress.getByName(majorAddressString);

      coronelAddressString = "230.0.0.3";
      coronelPort = 2224;
      coronelSocket = new MulticastSocket(coronelPort);
      coronelAddress = InetAddress.getByName(coronelAddressString);

      tenenteAddressString = "230.0.0.4";
      tenentePort = 2225;
      tenenteSocket = new MulticastSocket(tenentePort);
      tenenteAddress = InetAddress.getByName(tenenteAddressString);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() {
    try {
      out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(
        new InputStreamReader(socket.getInputStream())
      );

      Thread activeUsersThread = new Thread(() -> {
        while (true) {
          try {
            sendGroupMessage(majorAddressString, majorPort,"[NOTIFICACAO]: " + allUsers.size() + " utilizadores ATIVOS.");
            Thread.sleep(180000);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });

      //count Tasks
      Thread countTasks = new Thread(() -> {
        while (true) {
          try {
            sendGroupMessage(geralAddressString, geralPort,"[NOTIFICACAO]: " + getCountTasks() + " solicitacoes realizadas.\n[NOTIFICACAO]: " + getAprovedTasks() + " solicitacoes aprovadas.");
            Thread.sleep(120000);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
      

      System.out.println("Server running...");
      String inputLine, outputLine;
      Protocolo kkp = new Protocolo();
      outputLine = kkp.processInput(null);
      out.println(outputLine);

      while ((inputLine = in.readLine()) != null) {
        outputLine = kkp.processInput(inputLine);

        if (outputLine != null) {
          out.println(outputLine);

          if (outputLine.startsWith("Ola")) {
            countTasks.start();

            String[] tokens = outputLine.split("\\s+");
            if (tokens.length == 4) {
              User temp = new User(tokens[1], "", tokens[2]);
              allUsers.put(temp.getNome(), out);
              geralSocket.joinGroup(geralAddress);

              switch (temp.getCargo()) {
                case "MAJOR":
                  majorGroup.put(temp.getNome(), out);
                  majorSocket.joinGroup(majorAddress);
                  out.println("Entrou no grupo MAJOR");

                  activeUsersThread.start();

                  sendGroupMessage(majorAddressString, majorPort, temp.getNome() + " esta ONLINE!");
                  break;
                case "CORONEL":
                  coronelGroup.put(temp.getNome(), out);
                  coronelSocket.joinGroup(coronelAddress);
                  out.println("Entrou no grupo CORONEL");

                  sendGroupMessage(coronelAddressString, coronelPort, temp.getNome() + " esta ONLINE!");
                  break;
                case "TENENTE":
                  tenenteGroup.put(temp.getNome(), out);
                  tenenteSocket.joinGroup(tenenteAddress);
                  out.println("Entrou no grupo TENENTE");
                  
                  sendGroupMessage(tenenteAddressString, tenentePort, temp.getNome() + " esta ONLINE!");
                  break;
                default:
                  break;
              }
            }
          }

          if (outputLine.startsWith("MSGPRIVADA")) {
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String to = tokens[2];
            String message = tokens[3];

            String templateMessage = "[PRIVADA] ("+ from +"): " + message;

            if (allUsers.containsKey(to)) {
              sendPrivMessage(from, to, templateMessage);
              out.println(to + " esta ONLINE!");
            } else {
              out.println(to + " nao esta ONLINE!");
            }

            saveMessage(from, to, message);
          }

          if (outputLine.startsWith("TAREFA_MAJOR")){
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String tarefa = tokens[2];

            saveTask("MAJOR", from, tarefa);
            out.println("Tarefa aprovada!");
            String messageTemplate = "[TAREFA] " + from.toUpperCase() + " solicita autorizacao da tarefa ("+ tarefa +")";
            sendGroupMessage(majorAddressString, majorPort, messageTemplate);
          }

          if (outputLine.startsWith("TAREFA_CORONEL")){
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String tarefa = tokens[2];

            saveTask("CORONEL", from, tarefa);
            majorSocket.joinGroup(majorAddress);
            String messageTemplate = "[TAREFA] " + from.toUpperCase() + " solicita autorizacao da tarefa ("+ tarefa +")";
            sendGroupMessage(majorAddressString, majorPort, messageTemplate);
            majorSocket.leaveGroup(majorAddress);
          }

          if (outputLine.startsWith("TAREFA_TENENTE")){
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String tarefa = tokens[2];

            
            saveTask("TENENTE", from, tarefa);
            majorSocket.joinGroup(majorAddress);
            String messageTemplate = "[TAREFA] " + from.toUpperCase() + " solicita autorizacao da tarefa ("+ tarefa +")";
            sendGroupMessage(majorAddressString, majorPort, messageTemplate);
            majorSocket.leaveGroup(majorAddress);
          }

          if (outputLine.startsWith("MSGGERAL")) {
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String message = tokens[2];

            String templateMessage = "[MENSAGEM GERAL] (" + from +"): " + message;
            sendGroupMessage(geralAddressString, geralPort, templateMessage);
          }

          if (outputLine.startsWith("MAJOR")){
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String message = tokens[2];

            String templateMensagem = "[GROUP MAJOR] " + "(" + from + "): " + message;
            sendGroupMessage(majorAddressString, majorPort, templateMensagem);
          }

          if (outputLine.startsWith("CORONEL")){
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String message = tokens[2];

            String templateMensagem = "[GROUP CORONEL] " + "(" + from + "): " + message;
            sendGroupMessage(coronelAddressString, coronelPort, templateMensagem);
          }

          if (outputLine.startsWith("TENENTE")){
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String message = tokens[2];

            String templateMensagem = "[GROUP TENENTE] " + "(" + from + "): " + message;
            sendGroupMessage(tenenteAddressString, tenentePort, templateMensagem);
          }

          if (outputLine.endsWith("MENSAGENS PRIVADAS:")){
            String[] tokensOutput = outputLine.split(":");
            String nome = tokensOutput[0];
            try (BufferedReader reader = new BufferedReader(new FileReader("mensagens.txt"))) {
              String line;
              while ((line = reader.readLine()) != null) {
                  String[] tokens = line.split(":");
                  if (tokens.length == 3) {
                      String from = tokens[0];
                      String to = tokens[1];
                      String message = tokens[2];
      
                      if(to.equals(nome)){
                        out.println(from.toUpperCase() + ": " + message);                
                      }
                  }
              }
            } catch (IOException e) {
              System.err.println("Erro ao carregar mensagens: " + e.getMessage());
              e.printStackTrace();
            }
          }

          if (outputLine.endsWith("TAREFAS SEM AUTORIZACAO:")){
            try (BufferedReader reader = new BufferedReader(new FileReader("tarefas.txt"))) {
              String line;
              int count = 0;
              while ((line = reader.readLine()) != null) {
                  String[] tokens = line.split(":");
                  if (tokens.length == 4) {
                      count++;
                      String group = tokens[0];
                      String from = tokens[1];
                      String tarefa = tokens[2];
                      String check = tokens[3];

                      if("UNCHECKED".equals(check)){
                        out.println(count + "-> [" + group + "] (" + from + "): " + tarefa);
                      }
                           
                  }
              }
              if(count == 0){
                out.println("(sem tarefas)");
              }

              out.println("Selecione tarefa a autorizar -> (APROVAR:<nome>:<tarefa>)");
            } catch (IOException e) {
              System.err.println("Erro ao carregar mensagens: " + e.getMessage());
              e.printStackTrace();
            }
          }

          if (outputLine.endsWith("VER TAREFAS:")){
            String[] tokens = outputLine.split(":");
            String nome = tokens[0];

            out.println();
            out.println("TAREFAS:");
            getTasksByName(nome);
          }

          if (outputLine.startsWith("APROVAR")){
            String[] tokens = outputLine.split(":");
            String nome = tokens[1];
            String tarefa = tokens[2];

            aproveTask(nome, tarefa);
            out.println("Tarefa aprovada!");
            PrintWriter userPrintWriter = allUsers.get(nome);

            if (userPrintWriter != null) {
                userPrintWriter.println("A tarefa '" + tarefa + "' foi aprovada!");
                userPrintWriter.flush();
            } else {
                System.out.println("User not found: " + nome);
            }
          }
          
          if (outputLine.startsWith("JOINCHANNEL")){
            String[] tokens = outputLine.split(":");
            String nomeUser = tokens[1];
            String nomeGrupo = tokens[2];
            String password = tokens[3];

            addUsertoChannel(nomeUser, nomeGrupo, password);
            sendChannelMessage("", nomeGrupo, password,"\n[GRUPO "+ nomeGrupo +"] "+ nomeUser + " entrou no grupo!");
          }

          if (outputLine.startsWith("NEWCHANNEL")){
            String[] tokens = outputLine.split(":");
            String nomeUser = tokens[1];
            String nomeGrupo = tokens[2];
            String password = tokens[3];

            if(verificarCanal(nomeGrupo, password) == null){
                channels.add(new Canal(nomeGrupo, password));
                addUsertoChannel(nomeUser, nomeGrupo, password);

                out.println("\nGrupo criado.");
            } else {
              out.println("\nGrupo ja existe tente novamente.");
            }
          }

          if (outputLine.startsWith("MSGCHANNEL")){
            String[] tokens = outputLine.split(":");
            String from = tokens[1];
            String nomeGrupo = tokens[2];
            String password = tokens[3];
            String message = tokens[4];

            sendChannelMessage(from, nomeGrupo, password, "[GRUPO " + nomeGrupo + "] (" + from.toUpperCase() + "): " + message);
          }

          if (outputLine.startsWith("Adeus")){
            String[] tokens = outputLine.split(" ");
            if (tokens.length == 2){
              allUsers.remove(tokens[1]);
            }
            break;
          } 
        }
      }
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int getAprovedTasks(){
    int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("tarefas.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(":");
                if (partes.length > 0 && "UNCHECKED".equals(partes[partes.length - 1].trim())) {
                  count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
  }

  public int getCountTasks(){
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader("tarefas.txt"))) {
      while (br.readLine() != null) {
        count++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return count;
  }

  public static void sendChannelMessage(String from, String nomeGrupo, String passwordGrupo, String mensagem) {
    Canal canal = verificarCanal(nomeGrupo, passwordGrupo);
    if(canal != null){
      List<String> users = canal.getlist();
      for (int i = 0; i < users.size(); i++) {
        sendPrivMessage(from, users.get(i), mensagem);
      }
    }
  }

  public void addUsertoChannel(String nomeUser, String nomeGrupo, String passwordGrupo) {
    Canal canal = verificarCanal(nomeGrupo, passwordGrupo);
    if (canal != null) {
        canal.addUser(nomeUser);
    } else {
        System.out.println("Canal não encontrado.");
    }
  }

  public static Canal verificarCanal(String nome, String password) {
    for(Canal c: channels){
      if(c.getNome().equals(nome) && c.getPassword().equals(password)){
        return c;
      }
    }
    return null;
  }

  public void getTasksByName(String nome){
    try (BufferedReader reader = new BufferedReader(new FileReader("tarefas.txt"))) {
      String line;
      int count = 0;
      while ((line = reader.readLine()) != null) {
          String[] taskInfo = line.split(":");
          if (taskInfo.length == 4 && taskInfo[1].equals(nome)) {
              count++;
              String group = taskInfo[0];
              String tarefa = taskInfo[2];
              String check = taskInfo[3];
              out.println(count + "-> [" + group + "] (" + nome + "): " + tarefa + " (" + check + ")");
          }
      }
      if(count == 0){
        out.println("(sem tarefas)");
      }

    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static  synchronized void aproveTask(String nome, String tarefa) {
    try (BufferedReader reader = new BufferedReader(new FileReader("tarefas.txt"))) {
        String line;
        StringBuilder fileContent = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            String[] taskInfo = line.split(":");
            if (taskInfo.length == 4 && taskInfo[1].equals(nome) && taskInfo[2].equals(tarefa)) {

                taskInfo[3] = "APROVED";
                line = String.join(":", taskInfo);
            }

            fileContent.append(line).append(System.lineSeparator());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tarefas.txt"))) {
            writer.write(fileContent.toString());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }

    } catch (IOException e) {
        System.err.println("Error reading file: " + e.getMessage());
        e.printStackTrace();
    }
}

  public synchronized void saveTask(String group, String from, String task){
    try (
      BufferedWriter writer = new BufferedWriter(
        new FileWriter("tarefas.txt", true)
      )
    ) {
      String tarefa = String.format("%s:%s:%s:%s", group, from, task, "UNCHECKED");
      writer.write(tarefa);
      writer.newLine();
    } catch (IOException e) {
      System.err.println("Erro ao salvar o user no arquivo: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private synchronized void saveMessage(String from, String to, String message) {
    try (
      BufferedWriter writer = new BufferedWriter(
        new FileWriter("mensagens.txt", true)
      )
    ) {
      String mensagem = String.format("%s:%s:%s", from, to, message);
      writer.write(mensagem);
      writer.newLine();
    } catch (IOException e) {
      System.err.println("Erro ao salvar o user no arquivo: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void sendPrivMessage(String from, String To, String message) {
    PrintWriter destinatorPrinter = allUsers.get(To);
    if (destinatorPrinter != null) {
      destinatorPrinter.println("\n"+message);
    }
  }

  public static void sendGroupMessage(
    String groupAddress,
    Integer port,
    String message
  ) throws IOException {
    DatagramSocket socket = new DatagramSocket();
    InetAddress group = InetAddress.getByName(groupAddress);
    byte[] msgBytes = message.getBytes();
    DatagramPacket packet = new DatagramPacket(
      msgBytes,
      msgBytes.length,
      group,
      port
    );
    socket.send(packet);
    socket.close();
  }
}
