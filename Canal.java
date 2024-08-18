import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Canal {
    //private static Map<String, PrintWriter> allUsers = new HashMap<>();~
    private List<String> users = new ArrayList<>();
    private String nome;
    private String password; 

    public Canal(String nome, String password){
        this.nome = nome;
        this.password = password;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getNome(){
        return this.nome;
    }

    public String getPassword(){
        return this.password;
    }

    public List<String> getlist(){
        return this.users;
    }

    public void addUser(String user){
        users.add(user);
    }
}
