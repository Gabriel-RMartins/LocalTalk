import java.io.*;
import java.net.*;

public class Client {

    private static String cargo;
    

    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java Client <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);



        try (
            Socket kkSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(kkSocket.getInputStream()));
        ) {
            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));
            
            Thread cargosThread = new Thread(() -> {
                switch (cargo) {
                    case "MAJOR":
                        try {
                            receiveMessageMulticastGroup("230.0.0.2", 2223);
                        } catch (IOException e) {
                            e.printStackTrace();
                        };
                        break;
                    case "CORONEL":
                        try {
                            receiveMessageMulticastGroup("230.0.0.3", 2224);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "TENENTE":
                        try {
                            receiveMessageMulticastGroup("230.0.0.4", 2225);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.err.println("ERRO: Nao consegue receber mensagens Multicast.");
                        break;
                }
            });

            Thread messageReceiverThread = new Thread(() -> {
                String message;
                try {
                    while((message = in.readLine()) != null){
                        if(message.startsWith("Adeus")){
                            System.exit(0);
                        }

                        if(message.startsWith("Ola")){
                            String[] tokens = message.split("\\s+");
                            if (tokens.length == 4) {
                                cargo = tokens[2];
                                cargosThread.start();
                            }

                        }

                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageReceiverThread.start();

            Thread broadcastReceiverThread = new Thread(() -> {
                try {
                    receiveMessageMulticastGroup("230.0.0.1", 2222);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            broadcastReceiverThread.start();

            String fromServer;
            while (true) { 
                if((fromServer = stdIn.readLine()) != null){
                    out.println(fromServer);
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
    }

    public static void receiveMessageMulticastGroup(String Address, int Port) throws IOException{
        MulticastSocket socket = new MulticastSocket(Port);
        InetAddress group = InetAddress.getByName(Address);
        socket.joinGroup(group);

        byte[] buffer = new byte[1000];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            System.out.println(new String(packet.getData(), 0, packet.getLength()));
        }
    }
}
