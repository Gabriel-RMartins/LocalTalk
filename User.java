public class User {
    private String nome;
    private String pin;
    private String cargo;

    User(String nome, String pin, String cargo){
        this.nome = nome;
        this.pin = pin;
        this.cargo = cargo;
    }

    public String getNome(){
        return this.nome;
    }

    public String getPin(){
        return this.pin;
    }

    public String getCargo(){
        return this.cargo;
    }
}
