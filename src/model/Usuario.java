package model;

public class Usuario {
    private int uid;
    private String nome;
    private String email;
    private int gid;
    private String senhaHash;
    private byte[] tokenKey;
    private int kid;
    private int totalAcessos;
    private int totalConsultas;
    private int errosSenha;
    private int errosToken;
    private long bloqueadoAte;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getGrupoNome() {
        return gid == 1 ? "Administrador" : "Usuário";
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public byte[] getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(byte[] tokenKey) {
        this.tokenKey = tokenKey;
    }

    public int getKid() {
        return kid;
    }

    public void setKid(int kid) {
        this.kid = kid;
    }

    public int getTotalAcessos() {
        return totalAcessos;
    }

    public void setTotalAcessos(int totalAcessos) {
        this.totalAcessos = totalAcessos;
    }

    public int getTotalConsultas() {
        return totalConsultas;
    }

    public void setTotalConsultas(int totalConsultas) {
        this.totalConsultas = totalConsultas;
    }

    public int getErrosSenha() {
        return errosSenha;
    }

    public void setErrosSenha(int errosSenha) {
        this.errosSenha = errosSenha;
    }

    public int getErrosToken() {
        return errosToken;
    }

    public void setErrosToken(int errosToken) {
        this.errosToken = errosToken;
    }

    public long getBloqueadoAte() {
        return bloqueadoAte;
    }

    public void setBloqueadoAte(long bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }

    public boolean estaBloqueado() {
        return bloqueadoAte > System.currentTimeMillis();
    }
}